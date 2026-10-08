package org.zhinanzhen.b.service.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.service.LowPriceApprovalImageAnalyzer;
import org.zhinanzhen.b.service.PortalApplicationImageReviewService;
import org.zhinanzhen.b.utils.ImageResolutionInspector;
import org.zhinanzhen.b.utils.ImageResolutionInspector.Resolution;

@Service
public class PortalApplicationImageReviewServiceImpl implements PortalApplicationImageReviewService {
    private static final double MINIMUM_PPI = 72.0;
    private static final int MAX_IMAGE_BYTES = 20 * 1024 * 1024;
    private static final long MAX_IMAGE_PIXELS = 20_000_000L;
    private static final int MAX_AI_IMAGE_BYTES = 32 * 1024 * 1024;

    @Resource
    private LowPriceApprovalImageAnalyzer imageAnalyzer;

    @Override
    public ReviewResult review(byte[] imageBytes) throws IOException {
        Map<String, Object> details = new LinkedHashMap<String, Object>();
        details.put("minimumPpi", MINIMUM_PPI);
        details.put("horizontalPpi", null);
        details.put("verticalPpi", null);
        details.put("resolutionSource", null);
        details.put("clarityScore", null);
        details.put("aiProvider", "deepseek");
        if (imageBytes == null || imageBytes.length == 0)
            return rejected("图片为空，请重新上传", details);
        if (imageBytes.length > MAX_IMAGE_BYTES)
            return rejected("申请材料图片超过20MB，请压缩后重新上传", details);

        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(imageBytes))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext())
                return rejected("图片格式不支持清晰度审查或文件已损坏，请转换为JPEG、PNG或BMP后上传", details);
            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                details.put("width", width);
                details.put("height", height);
                if (width <= 0 || height <= 0 || (long) width * height > MAX_IMAGE_PIXELS)
                    return rejected("图片尺寸无效或超过2000万像素，请压缩后重新上传", details);
                if (width > 8192 || height > 8192)
                    return rejected("DeepSeek图片审查要求每边不超过8192像素，请调整尺寸后上传", details);

                Resolution resolution = ImageResolutionInspector.inspect(imageBytes);
                if (resolution == null)
                    return rejected("图片未记录有效PPI，无法确认大于72PPI，请提供带分辨率信息的原始图片", details);
                details.put("horizontalPpi", resolution.getHorizontalPpi());
                details.put("verticalPpi", resolution.getVerticalPpi());
                details.put("resolutionSource", resolution.getSource());
                if (!resolution.exceeds(MINIMUM_PPI))
                    return rejected("图片横向和纵向PPI必须都大于72，当前为"
                            + resolution.getHorizontalPpi() + " × " + resolution.getVerticalPpi(), details);

                // 实际解码校验有效图片；不能仅凭可伪造的分辨率元数据放行损坏文件。
                BufferedImage image = reader.read(0);
                byte[] aiImage = prepareAiImage(imageBytes, reader.getFormatName(), image);
                long clarityScore;
                try {
                    clarityScore = imageAnalyzer.assessClarity(aiImage);
                } catch (IOException e) {
                    return rejected(e.getMessage(), details);
                }
                if (clarityScore < 0 || clarityScore > 100)
                    return rejected("AI清晰度审查未返回有效评分，请重试", details);
                details.put("clarityScore", clarityScore);
                details.put("aiImageConverted", aiImage != imageBytes);
                // 按业务要求以 PPI > 72 为准，AI 评分仅供查看，不能当作 PPI 或另设门槛。
                return new ReviewResult(true, "图片PPI符合要求，AI清晰度审查完成", details);
            } finally {
                reader.dispose();
            }
        } catch (IOException | IllegalArgumentException e) {
            return rejected("图片无法完成清晰度审查：" + e.getMessage(), details);
        }
    }

    private ReviewResult rejected(String message, Map<String, Object> details) {
        return new ReviewResult(false, message, details);
    }

    private byte[] prepareAiImage(byte[] original, String format, BufferedImage image) throws IOException {
        if (original.length <= MAX_AI_IMAGE_BYTES
                && ("JPEG".equalsIgnoreCase(format) || "JPG".equalsIgnoreCase(format)
                    || "PNG".equalsIgnoreCase(format))) return original;

        // BMP 等格式无损转换为 PNG 审查副本；不再为腾讯云的大小限制进行有损压缩。
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        if (!ImageIO.write(image, "png", bytes)) throw new IOException("图片无法转换为PNG进行审查");
        if (bytes.size() > MAX_AI_IMAGE_BYTES)
            throw new IOException("图片超过DeepSeek审查大小限制，请压缩后重新上传");
        return bytes.toByteArray();
    }
}
