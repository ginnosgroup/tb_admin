package org.zhinanzhen.b.utils;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.bmp.BmpHeaderDirectory;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.jfif.JfifDirectory;
import com.drew.metadata.png.PngDirectory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import org.springframework.web.multipart.MultipartFile;

/** 从原始图片的物理分辨率元数据读取 PPI，不能用像素尺寸或 AI 分数代替。 */
public final class ImageResolutionInspector {
    private ImageResolutionInspector() { }

    public static boolean isImageUpload(MultipartFile file) throws IOException {
        if (file == null) return false;
        String mime = file.getContentType();
        if (mime != null && mime.toLowerCase(Locale.ROOT).startsWith("image/")) return true;
        String name = file.getOriginalFilename();
        if (name != null && name.toLowerCase(Locale.ROOT)
                .matches(".*\\.(jpe?g|png|bmp|gif|tiff?|webp|heic|heif|avif|ico|wbmp)$")) return true;
        // 同时检查文件内容，防止图片改扩展名后跳过审查。
        try (InputStream input = file.getInputStream()) {
            if (input == null) return false;
            byte[] header = new byte[16];
            int length = 0;
            while (length < header.length) {
                int read = input.read(header, length, header.length - length);
                if (read <= 0) break;
                length += read;
            }
            return hasImageHeader(header, length);
        }
    }

    public static boolean isImageUpload(byte[] bytes, String name, String mime) {
        if (mime != null && mime.toLowerCase(Locale.ROOT).startsWith("image/")) return true;
        if (name != null && name.toLowerCase(Locale.ROOT)
                .matches(".*\\.(jpe?g|png|bmp|gif|tiff?|webp|heic|heif|avif|ico|wbmp)$")) return true;
        return bytes != null && hasImageHeader(bytes, bytes.length);
    }

    private static boolean hasImageHeader(byte[] h, int size) {
        if (size < 2) return false;
        if ((h[0] & 255) == 255 && (h[1] & 255) == 216) return true;
        if (h[0] == 'B' && h[1] == 'M') return true;
        if (size >= 4 && ((h[0] == 'I' && h[1] == 'I' && h[2] == 42 && h[3] == 0)
                || (h[0] == 'M' && h[1] == 'M' && h[2] == 0 && h[3] == 42))) return true;
        if (size >= 6 && h[0] == 'G' && h[1] == 'I' && h[2] == 'F'
                && h[3] == '8' && (h[4] == '7' || h[4] == '9') && h[5] == 'a') return true;
        if (size >= 8 && (h[0] & 255) == 137 && h[1] == 'P' && h[2] == 'N'
                && h[3] == 'G' && h[4] == 13 && h[5] == 10 && h[6] == 26 && h[7] == 10) return true;
        if (size >= 12 && h[0] == 'R' && h[1] == 'I' && h[2] == 'F' && h[3] == 'F'
                && h[8] == 'W' && h[9] == 'E' && h[10] == 'B' && h[11] == 'P') return true;
        if (size >= 12 && h[4] == 'f' && h[5] == 't' && h[6] == 'y' && h[7] == 'p') {
            String brand = new String(h, 8, 4, java.nio.charset.StandardCharsets.US_ASCII);
            return brand.equals("heic") || brand.equals("heix") || brand.equals("heif")
                    || brand.equals("mif1") || brand.equals("avif") || brand.equals("avis");
        }
        return false;
    }

    public static Resolution inspect(byte[] bytes) throws IOException {
        Metadata metadata;
        try {
            metadata = ImageMetadataReader.readMetadata(new ByteArrayInputStream(bytes));
        } catch (ImageProcessingException e) {
            throw new IOException("无法读取图片分辨率，请上传有效图片", e);
        }
        // EXIF 是相机/扫描仪记录的物理分辨率，优先于 JPEG 编码器的默认 JFIF 值。
        for (ExifIFD0Directory directory : metadata.getDirectoriesOfType(ExifIFD0Directory.class)) {
            Integer unit = directory.getInteger(ExifIFD0Directory.TAG_RESOLUTION_UNIT);
            if (unit == null || (unit != 2 && unit != 3)) continue;
            Double x = directory.getDoubleObject(ExifIFD0Directory.TAG_X_RESOLUTION);
            Double y = directory.getDoubleObject(ExifIFD0Directory.TAG_Y_RESOLUTION);
            Resolution resolution = of(x, y, unit == 3 ? 2.54 : 1.0, "EXIF");
            if (resolution != null) return resolution;
        }
        for (JfifDirectory directory : metadata.getDirectoriesOfType(JfifDirectory.class)) {
            Integer unit = directory.getInteger(JfifDirectory.TAG_UNITS);
            if (unit == null || (unit != 1 && unit != 2)) continue;
            Resolution resolution = of(directory.getDoubleObject(JfifDirectory.TAG_RESX),
                    directory.getDoubleObject(JfifDirectory.TAG_RESY), unit == 2 ? 2.54 : 1.0, "JFIF");
            if (resolution != null) return resolution;
        }
        for (PngDirectory directory : metadata.getDirectoriesOfType(PngDirectory.class)) {
            if (!Integer.valueOf(1).equals(directory.getInteger(PngDirectory.TAG_UNIT_SPECIFIER))) continue;
            Resolution resolution = fromMeters(directory.getDoubleObject(PngDirectory.TAG_PIXELS_PER_UNIT_X),
                    directory.getDoubleObject(PngDirectory.TAG_PIXELS_PER_UNIT_Y), "PNG-pHYs");
            if (resolution != null) return resolution;
        }
        for (BmpHeaderDirectory directory : metadata.getDirectoriesOfType(BmpHeaderDirectory.class)) {
            Resolution resolution = fromMeters(directory.getDoubleObject(BmpHeaderDirectory.TAG_X_PIXELS_PER_METER),
                    directory.getDoubleObject(BmpHeaderDirectory.TAG_Y_PIXELS_PER_METER), "BMP");
            if (resolution != null) return resolution;
        }
        return null;
    }

    private static Resolution of(Double x, Double y, double factor, String source) {
        if (x == null || y == null || !Double.isFinite(x) || !Double.isFinite(y) || x <= 0 || y <= 0) return null;
        return new Resolution(x * factor, y * factor, source);
    }

    private static Resolution fromMeters(Double x, Double y, String source) {
        Resolution resolution = of(x, y, 0.0254, source);
        if (resolution == null) return null;
        // pHYs/BMP 只能记录整数像素/米；还原整数 PPI 的量化误差，避免 72 写成 2835 后误放行。
        return new Resolution(removeMeterQuantization(resolution.getHorizontalPpi()),
                removeMeterQuantization(resolution.getVerticalPpi()), source);
    }

    private static double removeMeterQuantization(double value) {
        double nearest = Math.rint(value);
        return Math.abs(value - nearest) <= 0.0127 + 1e-8 ? nearest : value;
    }

    public static final class Resolution {
        private final double horizontalPpi;
        private final double verticalPpi;
        private final String source;
        private Resolution(double horizontalPpi, double verticalPpi, String source) {
            this.horizontalPpi = horizontalPpi;
            this.verticalPpi = verticalPpi;
            this.source = source;
        }
        public double getHorizontalPpi() { return horizontalPpi; }
        public double getVerticalPpi() { return verticalPpi; }
        public String getSource() { return source; }
        public boolean exceeds(double minimumPpi) {
            return horizontalPpi > minimumPpi && verticalPpi > minimumPpi;
        }
    }
}
