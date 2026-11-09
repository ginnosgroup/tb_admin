package org.zhinanzhen.b.utils;

import java.io.IOException;
import java.text.Normalizer;
import java.util.Locale;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/** 规范护照姓名，供客户表单直接拼接familyName和givenNames。 */
public final class PassportNameNormalizer {
    private static final String INVALID_NAME_MESSAGE = "无法可靠识别护照英文姓名，请上传包含清晰英文姓名和机读区的护照资料页。";

    private PassportNameNormalizer() {
    }

    public static void normalize(ObjectNode passport) throws IOException {
        JsonNode details = passport.path("passportDetails");
        if (!details.isObject())
            throw new IOException(INVALID_NAME_MESSAGE);

        String[] names = namesFromMachineReadableZone(passport.path("machineReadableZone").path("line1"));
        if (names == null) {
            names = new String[] {latinName(details.get("familyName")), latinName(details.get("givenNames"))};
        }
        if (names[0].isEmpty() && names[1].isEmpty())
            throw new IOException(INVALID_NAME_MESSAGE);

        ObjectNode normalized = (ObjectNode) details;
        // 单名护照的另一部分用空字符串，避免前端拼出字面量"null"。
        normalized.put("familyName", names[0]);
        normalized.put("givenNames", names[1]);
        normalized.put("fullName", (names[1] + " " + names[0]).trim());
    }

    private static String[] namesFromMachineReadableZone(JsonNode value) {
        if (!value.isTextual())
            return null;
        String line = Normalizer.normalize(value.asText(), Normalizer.Form.NFKC)
                .replaceAll("\\s+", "").toUpperCase(Locale.ENGLISH);
        // 只接受完整的44字符护照首行，避免误把残缺OCR文字当成机读区。
        if (!line.matches("P[A-Z<][A-Z<]{3}[A-Z<]{39}"))
            return null;
        int separator = line.indexOf("<<", 5);
        if (separator <= 5)
            return null;
        return new String[] {mrzName(line.substring(5, separator)), mrzName(line.substring(separator + 2))};
    }

    private static String mrzName(String name) {
        return name.replace('<', ' ').trim().replaceAll("\\s+", " ");
    }

    private static String latinName(JsonNode value) throws IOException {
        if (value == null || value.isNull())
            return "";
        if (!value.isTextual())
            throw new IOException(INVALID_NAME_MESSAGE);
        String original = Normalizer.normalize(value.asText(), Normalizer.Form.NFKC).trim();
        if (original.isEmpty() || "null".equalsIgnoreCase(original) || "N/A".equalsIgnoreCase(original))
            return "";
        // 中英并列时只保留已有英文，不根据中文猜测拼音；保留合法姓名中的连字符、撇号和重音。
        String name = original.replaceAll("[\\p{IsHan}/,;:()\\[\\]{}<>\u00b7\u30fb]+", " ")
                .trim().replaceAll("\\s+", " ").toUpperCase(Locale.ENGLISH);
        if (!name.matches("[\\p{IsLatin}\\p{M} .'\u2019-]+") || !name.matches(".*\\p{IsLatin}.*"))
            throw new IOException(INVALID_NAME_MESSAGE);
        return name;
    }
}
