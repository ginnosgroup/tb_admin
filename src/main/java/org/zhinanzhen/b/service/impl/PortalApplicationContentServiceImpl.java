package org.zhinanzhen.b.service.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.zhinanzhen.b.service.LowPriceApprovalImageAnalyzer;
import org.zhinanzhen.b.service.PortalApplicationContentService;
import org.zhinanzhen.b.utils.ImageResolutionInspector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class PortalApplicationContentServiceImpl implements PortalApplicationContentService {
    private static final int MAX_FILE_BYTES = 20 * 1024 * 1024;
    private static final int MAX_JSON_BYTES = 12 * 1024 * 1024;
    private static final int MAX_TEXT_LENGTH = 1_000_000;
    private final ObjectMapper json = new ObjectMapper();

    @Resource
    private LowPriceApprovalImageAnalyzer imageAnalyzer;

    @Override
    public ObjectNode extract(byte[] bytes, String name, String mime) throws IOException {
        try {
            ObjectNode result = extractFile(bytes, name, mime, 0, new ArchiveBudget());
            if (json.writeValueAsBytes(result).length > MAX_JSON_BYTES)
                throw new IOException("提取结果超过保存大小限制，请拆分文件后上传");
            return result;
        } catch (IOException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new IOException("文件内容无法解析，请检查文件是否损坏或加密", e);
        }
    }

    private ObjectNode extractFile(byte[] bytes, String name, String mime, int depth, ArchiveBudget budget) throws IOException {
        if (bytes == null || bytes.length == 0) throw new IOException("文件为空，无法提取内容");
        if (bytes.length > MAX_FILE_BYTES) throw new IOException("内容提取文件最大支持20MB，请拆分后上传");
        String extension = extension(name);
        ObjectNode result = json.createObjectNode();
        result.put("fileName", name);
        result.put("fileFormat", extension);
        result.put("extractionStatus", "success");
        result.put("fullText", "");
        result.putArray("pages");
        result.putArray("sheets");
        if (ImageResolutionInspector.isImageUpload(bytes, name, mime)) {
            result.put("fileFormat", "image");
            addPage(result, 1, imageAnalyzer.extractImageContent(prepareImage(bytes)), "deepseek");
        } else if (startsWith(bytes, "%PDF-")) {
            result.put("fileFormat", "pdf");
            extractPdf(bytes, result);
        } else if ("docx".equals(extension)) {
            try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(bytes));
                    XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
                ObjectNode content = textContent(extractor.getText());
                ArrayNode tables = (ArrayNode) content.get("tables");
                for (org.apache.poi.xwpf.usermodel.XWPFTable table : document.getTables()) {
                    ObjectNode output = tables.addObject();
                    output.putNull("title");
                    output.putArray("headers");
                    ArrayNode rows = output.putArray("rows");
                    for (org.apache.poi.xwpf.usermodel.XWPFTableRow row : table.getRows()) {
                        ArrayNode cells = rows.addArray();
                        for (org.apache.poi.xwpf.usermodel.XWPFTableCell cell : row.getTableCells()) cells.add(cell.getText());
                    }
                }
                addPage(result, 1, content, "word");
                for (org.apache.poi.xwpf.usermodel.XWPFPictureData picture : document.getAllPictures())
                    addEmbeddedImage(result, picture.getFileName(), picture.getData());
            }
        } else if ("doc".equals(extension)) {
            try (HWPFDocument document = new HWPFDocument(new ByteArrayInputStream(bytes));
                    WordExtractor extractor = new WordExtractor(document)) {
                addText(result, extractor.getText(), "word");
                for (org.apache.poi.hwpf.usermodel.Picture picture : document.getPicturesTable().getAllPictures())
                    addEmbeddedImage(result, "embedded-image", picture.getContent());
            }
        } else if ("xls".equals(extension) || "xlsx".equals(extension)) {
            extractWorkbook(bytes, result);
        } else if ("zip".equals(extension)) {
            extractZip(bytes, result, depth, budget);
        } else if (isText(extension, mime)) {
            if ("rtf".equals(extension)) {
                javax.swing.text.rtf.RTFEditorKit kit = new javax.swing.text.rtf.RTFEditorKit();
                javax.swing.text.Document document = kit.createDefaultDocument();
                try {
                    kit.read(new ByteArrayInputStream(bytes), document, 0);
                    addText(result, document.getText(0, document.getLength()), "rtf");
                } catch (javax.swing.text.BadLocationException e) { throw new IOException("RTF内容无法解析", e); }
            } else addText(result, decodeText(bytes), "text");
        } else {
            throw new IOException("暂不支持提取此文件格式：" + extension
                    + "，请转换为图片、PDF、Word、Excel或文本文件");
        }
        if (result.path("fullText").asText().length() > MAX_TEXT_LENGTH)
            throw new IOException("文件文字超过提取保存限制，请拆分文件后上传");
        if (result.path("fullText").asText().trim().isEmpty()) result.put("extractionStatus", "empty");
        return result;
    }

    private void extractPdf(byte[] bytes, ObjectNode result) throws IOException {
        try (PDDocument document = PDDocument.load(bytes)) {
            if (document.isEncrypted() && !document.getCurrentAccessPermission().canExtractContent())
                throw new IOException("PDF禁止提取内容，请提供未加密且允许提取的文件");
            if (document.getNumberOfPages() > 100) throw new IOException("PDF超过100页，请拆分文件后上传");
            if (document.getDocumentCatalog().getAcroForm() != null) {
                ObjectNode fields = result.putObject("formFields");
                for (PDField field : document.getDocumentCatalog().getAcroForm().getFieldTree())
                    fields.put(field.getFullyQualifiedName(), field.getValueAsString());
                document.getDocumentCatalog().getAcroForm().flatten();
            }
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            PDFRenderer renderer = new PDFRenderer(document);
            for (int i = 0; i < document.getNumberOfPages(); i++) {
                stripper.setStartPage(i + 1);
                stripper.setEndPage(i + 1);
                String nativeText = stripper.getText(document).trim();
                PDPage page = document.getPage(i);
                if (nativeText.isEmpty() || containsImage(page.getResources(), 0)) {
                    double width = page.getCropBox().getWidth() / 72.0 * 150;
                    double height = page.getCropBox().getHeight() / 72.0 * 150;
                    checkImageSize(width, height);
                    BufferedImage image = renderer.renderImageWithDPI(i, 150, ImageType.RGB);
                    ObjectNode content = imageAnalyzer.extractImageContent(png(image));
                    if (content == null) throw new IOException("DeepSeek未返回第" + (i + 1) + "页内容");
                    if (!nativeText.isEmpty() && !content.path("text").asText().contains(nativeText)) {
                        content = content.deepCopy();
                        content.put("text", content.path("text").asText() + "\n" + nativeText);
                    }
                    addPage(result, i + 1, content, "deepseek");
                } else addPage(result, i + 1, textContent(nativeText), "pdfText");
            }
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

    private void extractWorkbook(byte[] bytes, ObjectNode result) throws IOException {
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            DataFormatter formatter = new DataFormatter();
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            StringBuilder fullText = new StringBuilder();
            ArrayNode sheets = (ArrayNode) result.get("sheets");
            for (Sheet sheet : workbook) {
                ObjectNode output = sheets.addObject();
                output.put("sheetName", sheet.getSheetName());
                ArrayNode rows = output.putArray("rows");
                ObjectNode formulas = output.putObject("formulas");
                fullText.append(sheet.getSheetName()).append('\n');
                for (Row row : sheet) {
                    ObjectNode rowOutput = rows.addObject();
                    rowOutput.put("rowNumber", row.getRowNum() + 1);
                    ArrayNode cells = rowOutput.putArray("cells");
                    for (int column = 0; column < row.getLastCellNum(); column++) {
                        Cell cell = row.getCell(column);
                        String value;
                        try { value = formatter.formatCellValue(cell, evaluator); }
                        catch (RuntimeException e) { value = formatter.formatCellValue(cell); }
                        cells.add(value);
                        fullText.append(value).append('\t');
                        if (cell != null && cell.getCellType() == CellType.FORMULA)
                            formulas.put(cell.getAddress().formatAsString(), cell.getCellFormula());
                    }
                    fullText.append('\n');
                    if (fullText.length() > MAX_TEXT_LENGTH) throw new IOException("Excel内容过多，请拆分后上传");
                }
            }
            result.put("fullText", fullText.toString());
        }
    }

    private void extractZip(byte[] bytes, ObjectNode result, int depth, ArchiveBudget budget) throws IOException {
        if (depth >= 2) throw new IOException("压缩包嵌套过多，请解压后上传");
        ArrayNode files = result.putArray("files");
        StringBuilder fullText = new StringBuilder();
        try (ZipInputStream archive = new ZipInputStream(new ByteArrayInputStream(bytes), Charset.forName("GB18030"))) {
            ZipEntry entry;
            while ((entry = archive.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;
                if (++budget.fileCount > 50) throw new IOException("压缩包文件超过50个，请拆分后上传");
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                byte[] buffer = new byte[8192];
                int count;
                while ((count = archive.read(buffer)) != -1) {
                    budget.expandedBytes += count;
                    if (budget.expandedBytes > MAX_FILE_BYTES) throw new IOException("压缩包解压内容超过20MB，请拆分后上传");
                    output.write(buffer, 0, count);
                }
                ObjectNode child = extractFile(output.toByteArray(), entry.getName(), null, depth + 1, budget);
                files.add(child);
                fullText.append(entry.getName()).append('\n').append(child.path("fullText").asText()).append('\n');
                if (fullText.length() > MAX_TEXT_LENGTH) throw new IOException("压缩包文字过多，请拆分后上传");
            }
        }
        if (files.size() == 0) throw new IOException("压缩包为空或不是有效ZIP文件");
        result.put("fullText", fullText.toString());
    }

    private void addText(ObjectNode result, String text, String method) throws IOException {
        addPage(result, 1, textContent(text == null ? "" : text), method);
    }

    private void addEmbeddedImage(ObjectNode result, String name, byte[] bytes) throws IOException {
        ArrayNode images = result.withArray("embeddedImages");
        if (images.size() >= 50) throw new IOException("Word内嵌图片超过50张，请拆分后上传");
        ObjectNode content = imageAnalyzer.extractImageContent(prepareImage(bytes));
        if (content == null || !content.path("text").isTextual()
                || !content.path("fields").isArray() || !content.path("tables").isArray())
            throw new IOException("Word内嵌图片内容提取失败，请重试");
        ObjectNode image = images.addObject();
        image.put("fileName", name);
        image.set("content", content);
        String previous = result.path("fullText").asText();
        String text = content.path("text").asText();
        if (previous.length() + text.length() > MAX_TEXT_LENGTH) throw new IOException("文字过多，请拆分后上传");
        result.put("fullText", previous.isEmpty() ? text : previous + "\n\n" + text);
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
        if (previous.length() + text.length() > MAX_TEXT_LENGTH) throw new IOException("文字过多，请拆分文件后上传");
        result.put("fullText", previous.isEmpty() ? text : previous + "\n\n" + text);
    }

    private byte[] prepareImage(byte[] bytes) throws IOException {
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw new IOException("图片格式不支持内容提取，请转换为JPEG或PNG");
            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                checkImageSize(reader.getWidth(0), reader.getHeight(0));
                BufferedImage image = reader.read(0);
                String format = reader.getFormatName();
                if ("JPEG".equalsIgnoreCase(format) || "PNG".equalsIgnoreCase(format)) return bytes;
                return png(image);
            } finally { reader.dispose(); }
        }
    }

    private void checkImageSize(double width, double height) throws IOException {
        if (width <= 0 || height <= 0 || width > 8192 || height > 8192 || width * height > 20_000_000L)
            throw new IOException("识别图片尺寸过大，请压缩或拆分页面后上传");
    }

    private byte[] png(BufferedImage image) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        if (!ImageIO.write(image, "png", output) || output.size() > 32 * 1024 * 1024)
            throw new IOException("图片无法转换或超过DeepSeek识别大小限制");
        return output.toByteArray();
    }

    private boolean isText(String extension, String mime) {
        return extension.matches("txt|csv|tsv|json|xml|html?|md|log|rtf")
                || (mime != null && mime.toLowerCase(Locale.ROOT).startsWith("text/"));
    }

    private String decodeText(byte[] bytes) throws IOException {
        if (bytes.length >= 2 && bytes[0] == (byte) 255 && bytes[1] == (byte) 254)
            return new String(bytes, 2, bytes.length - 2, StandardCharsets.UTF_16LE);
        if (bytes.length >= 2 && bytes[0] == (byte) 254 && bytes[1] == (byte) 255)
            return new String(bytes, 2, bytes.length - 2, StandardCharsets.UTF_16BE);
        int offset = bytes.length >= 3 && bytes[0] == (byte) 239 && bytes[1] == (byte) 187 && bytes[2] == (byte) 191 ? 3 : 0;
        try {
            return StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes, offset, bytes.length - offset)).toString();
        } catch (CharacterCodingException e) {
            return Charset.forName("GB18030").newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes)).toString();
        }
    }

    private boolean startsWith(byte[] bytes, String prefix) {
        return bytes.length >= prefix.length()
                && prefix.equals(new String(bytes, 0, prefix.length(), StandardCharsets.US_ASCII));
    }

    private String extension(String name) {
        return name == null || name.lastIndexOf('.') < 0 ? "" : name.substring(name.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    private static final class ArchiveBudget {
        int expandedBytes;
        int fileCount;
    }
}
