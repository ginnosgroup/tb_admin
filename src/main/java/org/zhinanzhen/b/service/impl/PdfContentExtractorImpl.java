package org.zhinanzhen.b.service.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.annotation.Resource;
import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.extern.slf4j.Slf4j;
import org.zhinanzhen.b.service.LowPriceApprovalImageAnalyzer;
import org.zhinanzhen.b.service.PdfContentExtractor;

/**
 * PDF内容提取实现：逐页判断是否需要视觉识别，需要时按150DPI渲染页面图片交给DeepSeek视觉模型。
 * 视觉识别失败的页面，只要文字层有内容就回退到文字层；纯扫描页没有文字层时才向上报错。
 */
@Service
@Slf4j
public class PdfContentExtractorImpl implements PdfContentExtractor {

    /** 单份PDF最多处理页数，超过时要求拆分，避免一次上传占用过多时间和内存。 */
    static final int MAX_PAGES = 100;
    /** 单份PDF提取文字总量上限，避免异常文件产生超大JSON。 */
    static final int MAX_TEXT_LENGTH = 1_000_000;
    private static final int RENDER_DPI = 150;
    private static final int MAX_IMAGE_BYTES = 32 * 1024 * 1024;
    private static final String METHOD_PDF_TEXT = "pdfText";
    private static final String METHOD_DEEPSEEK = "deepseek";

    private final ObjectMapper json = new ObjectMapper();

    @Resource
    private LowPriceApprovalImageAnalyzer imageAnalyzer;

    @Override
    public ObjectNode extract(byte[] pdfBytes) throws IOException {
        return extract(pdfBytes, Integer.MAX_VALUE);
    }

    @Override
    public ObjectNode extract(byte[] pdfBytes, int maxVisionPages) throws IOException {
        if (pdfBytes == null || pdfBytes.length == 0)
            throw new IOException("PDF文件为空，无法提取内容");
        try (PDDocument document = PDDocument.load(pdfBytes)) {
            if (document.isEncrypted() && !document.getCurrentAccessPermission().canExtractContent())
                throw new IOException("PDF禁止提取内容，请提供未加密且允许提取的文件");
            if (document.getNumberOfPages() > MAX_PAGES)
                throw new IOException("PDF超过100页，请拆分文件后上传");

            ObjectNode result = json.createObjectNode();
            result.put("pageCount", document.getNumberOfPages());
            result.put("extractionStatus", "success");
            result.put("fullText", "");
            result.put("visionLimitReached", false);
            result.putArray("pages");

            PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
            if (acroForm != null) {
                ObjectNode fields = result.putObject("formFields");
                for (PDField field : acroForm.getFieldTree())
                    fields.put(field.getFullyQualifiedName(), field.getValueAsString());
            }
            // 表单填写内容存放在AcroForm控件中，PDFTextStripper默认读不到，先扁平化再抽取文字。
            flattenAcroForm(document);

            PDFTextStripper textStripper = new PDFTextStripper();
            textStripper.setSortByPosition(true);
            PDFRenderer renderer = new PDFRenderer(document);
            int visionPages = 0;
            for (int i = 0; i < document.getNumberOfPages(); i++) {
                textStripper.setStartPage(i + 1);
                textStripper.setEndPage(i + 1);
                String nativeText = textStripper.getText(document).trim();
                if (!nativeText.isEmpty() && !containsImage(document.getPage(i).getResources(), 0)) {
                    addPage(result, i + 1, textContent(nativeText), METHOD_PDF_TEXT);
                    continue;
                }
                if (visionPages >= maxVisionPages) {
                    result.put("visionLimitReached", true);
                    addPage(result, i + 1, textContent(nativeText), METHOD_PDF_TEXT);
                    continue;
                }
                visionPages++;
                ObjectNode content;
                try {
                    content = extractPageImage(document, renderer, i);
                } catch (IOException e) {
                    // 视觉识别失败时，有文字层的页面继续用文字层；纯扫描页没有文字层才向上报错。
                    if (nativeText.isEmpty()) throw e;
                    log.warn("PDF第{}页视觉识别失败，改用文字层, pageCount={}", i + 1, document.getNumberOfPages(), e);
                    addPage(result, i + 1, textContent(nativeText), METHOD_PDF_TEXT);
                    continue;
                }
                if (!nativeText.isEmpty() && !content.path("text").asText().contains(nativeText)) {
                    content = content.deepCopy();
                    content.put("text", content.path("text").asText() + "\n" + nativeText);
                }
                addPage(result, i + 1, content, METHOD_DEEPSEEK);
            }
            if (result.path("fullText").asText().trim().isEmpty())
                result.put("extractionStatus", "empty");
            return result;
        }
    }

    @Override
    public String extractNativeText(byte[] pdfBytes) throws IOException {
        if (pdfBytes == null || pdfBytes.length == 0)
            throw new IOException("PDF文件为空，无法提取内容");
        try (PDDocument document = PDDocument.load(pdfBytes)) {
            if (document.isEncrypted() && !document.getCurrentAccessPermission().canExtractContent())
                throw new IOException("PDF禁止提取内容，请提供未加密且允许提取的文件");
            flattenAcroForm(document);
            PDFTextStripper textStripper = new PDFTextStripper();
            textStripper.setSortByPosition(true);
            String text = textStripper.getText(document);
            return text == null ? "" : text.trim();
        }
    }

    private ObjectNode extractPageImage(PDDocument document, PDFRenderer renderer, int index) throws IOException {
        if (imageAnalyzer == null)
            throw new IOException("视觉识别组件未初始化，无法识别PDF页面图片");
        PDPage page = document.getPage(index);
        double width = page.getCropBox().getWidth() / 72.0 * RENDER_DPI;
        double height = page.getCropBox().getHeight() / 72.0 * RENDER_DPI;
        checkImageSize(width, height);
        BufferedImage image = renderer.renderImageWithDPI(index, RENDER_DPI, ImageType.RGB);
        ObjectNode content = imageAnalyzer.extractImageContent(png(image));
        if (content == null) throw new IOException("DeepSeek未返回第" + (index + 1) + "页内容");
        return content;
    }

    private void flattenAcroForm(PDDocument document) {
        PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
        if (acroForm == null) return;
        try {
            acroForm.flatten();
        } catch (Exception e) {
            // 扁平化失败只影响表单填写内容，页面文字层仍然可用，不阻断提取。
            log.warn("PDF表单扁平化失败，表单填写内容可能缺失, pageCount={}", document.getNumberOfPages(), e);
        }
    }

    private boolean containsImage(PDResources resources, int depth) throws IOException {
        if (resources == null || depth > 3) return false;
        for (org.apache.pdfbox.cos.COSName name : resources.getXObjectNames()) {
            PDXObject object = resources.getXObject(name);
            if (object instanceof PDImageXObject) return true;
            if (object instanceof PDFormXObject && containsImage(((PDFormXObject) object).getResources(), depth + 1))
                return true;
        }
        return false;
    }

    private ObjectNode textContent(String text) {
        ObjectNode content = json.createObjectNode();
        content.put("text", text);
        content.putArray("fields");
        content.putArray("tables");
        return content;
    }

    private void addPage(ObjectNode result, int page, ObjectNode content, String method) throws IOException {
        if (content == null || !content.path("text").isTextual()
                || !content.path("fields").isArray() || !content.path("tables").isArray())
            throw new IOException("内容提取未返回有效JSON结构，请重试");
        ObjectNode output = content.deepCopy();
        output.put("pageNumber", page);
        output.put("method", method);
        ((ArrayNode) result.get("pages")).add(output);
        String previous = result.path("fullText").asText();
        String text = content.path("text").asText();
        if (previous.length() + text.length() > MAX_TEXT_LENGTH)
            throw new IOException("文字过多，请拆分文件后上传");
        result.put("fullText", previous.isEmpty() ? text : previous + "\n\n" + text);
    }

    private void checkImageSize(double width, double height) throws IOException {
        if (width <= 0 || height <= 0 || width > 8192 || height > 8192 || width * height > 20_000_000L)
            throw new IOException("识别图片尺寸过大，请压缩或拆分页面后上传");
    }

    private byte[] png(BufferedImage image) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        if (!ImageIO.write(image, "png", output) || output.size() > MAX_IMAGE_BYTES)
            throw new IOException("图片无法转换或超过DeepSeek识别大小限制");
        return output.toByteArray();
    }
}
