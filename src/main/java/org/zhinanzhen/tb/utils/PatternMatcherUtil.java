package org.zhinanzhen.tb.utils;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.ArrayList;
import java.util.List;

public class PatternMatcherUtil {

    // 预编译正则表达式以提高性能
    private static final Pattern AT_SEVEN_DIGITS_PATTERN = Pattern.compile("@\\d{7}");
    private static final Pattern TEXT_AT_SEVEN_DIGITS_PATTERN = Pattern.compile("(.+?)@\\d{7}");

    /**
     * 检查字符串是否包含@加7位数字的模式
     */
    public static boolean containsPattern(String input) {
        if (input == null) return false;
        return AT_SEVEN_DIGITS_PATTERN.matcher(input).find();
    }

    /**
     * 获取第一个匹配的模式
     */
    public static String getFirstMatch(String input) {
        if (input == null) return null;

        Matcher matcher = AT_SEVEN_DIGITS_PATTERN.matcher(input);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    /**
     * 获取所有匹配的模式
     */
    public static List<String> getAllMatches(String input) {
        List<String> matches = new ArrayList<>();
        if (input == null) return matches;

        Matcher matcher = AT_SEVEN_DIGITS_PATTERN.matcher(input);
        while (matcher.find()) {
            matches.add(matcher.group());
        }
        return matches;
    }

    /**
     * 统计匹配模式的数量
     */
    public static int countMatches(String input) {
        if (input == null) return 0;

        int count = 0;
        Matcher matcher = AT_SEVEN_DIGITS_PATTERN.matcher(input);
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    /**
     * 替换所有匹配的模式
     */
    public static String replaceAll(String input, String replacement) {
        if (input == null) return null;
        return AT_SEVEN_DIGITS_PATTERN.matcher(input).replaceAll(replacement);
    }

    /**
     * 获取@前面的字符串（第一个匹配）
     * 例如：从"CE Shi@1018775"中获取"CE Shi"
     */
    public static String getTextBeforeAt(String input) {
        if (input == null) return null;

        Matcher matcher = TEXT_AT_SEVEN_DIGITS_PATTERN.matcher(input);
        if (matcher.find()) {
            return matcher.group(1); // 获取第一个捕获组（@前面的内容）
        }
        return null;
    }

    /**
     * 获取所有@前面的字符串
     * 例如：从"CE Shi@1018775 and Zhang San@1234567"中获取["CE Shi", "Zhang San"]
     */
    public static List<String> getAllTextsBeforeAt(String input) {
        List<String> texts = new ArrayList<>();
        if (input == null) return texts;

        Matcher matcher = TEXT_AT_SEVEN_DIGITS_PATTERN.matcher(input);
        while (matcher.find()) {
            texts.add(matcher.group(1)); // 获取第一个捕获组（@前面的内容）
        }
        return texts;
    }

    /**
     * 同时获取@前面和后面的完整匹配
     * 例如：从"CE Shi@1018775"中获取{"text": "CE Shi", "digits": "1018775"}
     */
    public static MatchResult getFullMatch(String input) {
        if (input == null) return null;

        Matcher matcher = TEXT_AT_SEVEN_DIGITS_PATTERN.matcher(input);
        if (matcher.find()) {
            String fullMatch = matcher.group(0); // 完整匹配，如"CE Shi@1018775"
            String textBeforeAt = matcher.group(1); // @前面的文本
            String digitsAfterAt = fullMatch.substring(textBeforeAt.length() + 1); // @后面的数字

            return new MatchResult(textBeforeAt, digitsAfterAt, fullMatch);
        }
        return null;
    }

    /**
     * 获取所有完整匹配
     */
    public static List<MatchResult> getAllFullMatches(String input) {
        List<MatchResult> results = new ArrayList<>();
        if (input == null) return results;

        Matcher matcher = TEXT_AT_SEVEN_DIGITS_PATTERN.matcher(input);
        while (matcher.find()) {
            String fullMatch = matcher.group(0);
            String textBeforeAt = matcher.group(1);
            String digitsAfterAt = fullMatch.substring(textBeforeAt.length() + 1);

            results.add(new MatchResult(textBeforeAt, digitsAfterAt, fullMatch));
        }
        return results;
    }

    /**
     * 匹配结果封装类
     */
    public static class MatchResult {
        private final String textBeforeAt;
        private final String digitsAfterAt;
        private final String fullMatch;

        public MatchResult(String textBeforeAt, String digitsAfterAt, String fullMatch) {
            this.textBeforeAt = textBeforeAt;
            this.digitsAfterAt = digitsAfterAt;
            this.fullMatch = fullMatch;
        }

        public String getTextBeforeAt() {
            return textBeforeAt;
        }

        public String getDigitsAfterAt() {
            return digitsAfterAt;
        }

        public String getFullMatch() {
            return fullMatch;
        }

        @Override
        public String toString() {
            return "MatchResult{" +
                    "textBeforeAt='" + textBeforeAt + '\'' +
                    ", digitsAfterAt='" + digitsAfterAt + '\'' +
                    ", fullMatch='" + fullMatch + '\'' +
                    '}';
        }
    }
}