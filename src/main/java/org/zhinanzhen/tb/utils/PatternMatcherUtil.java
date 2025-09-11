package org.zhinanzhen.tb.utils;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.ArrayList;
import java.util.List;

public class PatternMatcherUtil {
    
    // 预编译正则表达式以提高性能
    private static final Pattern AT_SEVEN_DIGITS_PATTERN = Pattern.compile("@\\d{7}");
    
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
}