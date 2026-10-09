package org.zhinanzhen.b.utils;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/** 报告编号优先使用Test Report Form Number，缺失时使用Score Report Code。 */
public final class ReportFormNumberExtractor {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String FIELD = "testReportFormNumber";
    private static final Pattern REPORT_NUMBER = labelPattern("Test[\\s_-]*Report[\\s_-]*Form[\\s_-]*Number");
    private static final Pattern SCORE_CODE = labelPattern("Score[\\s_-]*Report[\\s_-]*Code");

    private ReportFormNumberExtractor() {
    }

    public static String resolve(JsonNode record, JsonNode sharedSource) {
        String value = findField(record, FIELD);
        if (value == null)
            value = findField(sharedSource, FIELD);
        if (value == null)
            value = findField(record, "scoreReportCode");
        if (value == null)
            value = findField(sharedSource, "scoreReportCode");
        return value;
    }

    /** 使用未截断的PDF/OCR文字补齐单份报告，多个记录时不把同一个编号复制给所有记录。 */
    public static String supplementFromText(String result, String text) throws IOException {
        JsonNode root = JSON.readTree(result);
        if (root == null || !root.isObject())
            return result;
        ObjectNode record = singleRecord(root);
        if (record == null)
            return result;

        Set<String> reportNumbers = findValues(text, REPORT_NUMBER);
        Set<String> scoreCodes = findValues(text, SCORE_CODE);
        // 多个不同报告编号无法关联到单条AI记录，不进行跨报告回填。
        if (reportNumbers.size() > 1)
            return result;
        String value = reportNumbers.size() == 1 ? reportNumbers.iterator().next() : null;
        if (value == null)
            value = findField(record, FIELD);
        if (value == null && record != root)
            value = findField(root, FIELD);
        if (value == null && scoreCodes.size() == 1)
            value = scoreCodes.iterator().next();
        if (value == null)
            value = resolve(record, record == root ? null : root);
        if (value == null)
            return result;
        record.put(FIELD, value);
        return JSON.writeValueAsString(root);
    }

    private static ObjectNode singleRecord(JsonNode root) {
        ObjectNode single = null;
        boolean hasForm = false;
        for (String name : new String[] {"language", "education"}) {
            JsonNode form = root.path(name);
            if (!form.isMissingNode())
                hasForm = true;
            if (form.isObject()) {
                if (single != null)
                    return null;
                single = (ObjectNode) form;
            } else if (form.isArray()) {
                for (JsonNode item : form) {
                    if (item.isObject()) {
                        if (single != null)
                            return null;
                        single = (ObjectNode) item;
                    }
                }
            }
        }
        return hasForm ? single : (ObjectNode) root;
    }

    private static String findField(JsonNode source, String name) {
        if (source == null || !source.isObject())
            return null;
        String value = textValue(source.get(name));
        if (value != null)
            return value;
        String expected = normalizeName(name);
        Iterator<Map.Entry<String, JsonNode>> fields = source.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            if (expected.equals(normalizeName(field.getKey()))) {
                value = textValue(field.getValue());
                if (value != null)
                    return value;
            }
        }
        return null;
    }

    private static String normalizeName(String name) {
        return name.replaceAll("[\\s_-]", "").toLowerCase(Locale.ENGLISH);
    }

    private static String textValue(JsonNode value) {
        if (value == null || (!value.isTextual() && !value.isNumber()))
            return null;
        String text = value.asText().trim();
        return text.isEmpty() || "null".equalsIgnoreCase(text) || "N/A".equalsIgnoreCase(text) ? null : text;
    }

    private static Pattern labelPattern(String label) {
        return Pattern.compile("\\b" + label + "\\b[ \\t]*[:：#=]?[ \\t]*(?:\\r?\\n[ \\t]*)?"
                + "([A-Za-z0-9][A-Za-z0-9._/+-]{0,99})(?=[ \\t]*(?:\\r?\\n|$))", Pattern.CASE_INSENSITIVE);
    }

    private static Set<String> findValues(String text, Pattern pattern) {
        Set<String> values = new LinkedHashSet<String>();
        if (text == null)
            return values;
        Matcher matcher = pattern.matcher(text.replace('\u00a0', ' '));
        while (matcher.find()) {
            String value = matcher.group(1);
            if (!value.equalsIgnoreCase("null") && !value.equalsIgnoreCase("N/A")
                    && !value.equalsIgnoreCase("Score") && !value.equalsIgnoreCase("Test")
                    && !value.equalsIgnoreCase("Overall"))
                values.add(value);
        }
        return values;
    }
}
