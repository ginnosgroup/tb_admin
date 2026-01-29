package org.zhinanzhen.tb.utils;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class MonthDateUtils {
    
    private static final DateTimeFormatter MONTH_FORMATTER = 
            DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter DATE_FORMATTER = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * 获取指定年月当月的第一天
     * @param yearMonth 格式：yyyy-MM
     * @return 当月第一天，格式：yyyy-MM-dd
     */
    public static String getFirstDayOfMonth(String yearMonth) {
        try {
            YearMonth ym = YearMonth.parse(yearMonth, MONTH_FORMATTER);
            return ym.atDay(1).format(DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("日期格式错误，请使用 yyyy-MM 格式", e);
        }
    }
    
    /**
     * 获取指定年月当月的最后一天
     * @param yearMonth 格式：yyyy-MM
     * @return 当月最后一天，格式：yyyy-MM-dd
     */
    public static String getLastDayOfMonth(String yearMonth) {
        try {
            YearMonth ym = YearMonth.parse(yearMonth, MONTH_FORMATTER);
            return ym.atEndOfMonth().format(DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("日期格式错误，请使用 yyyy-MM 格式", e);
        }
    }
    
    /**
     * 获取指定年月的第一天和最后一天
     * @param yearMonth 格式：yyyy-MM
     * @return 包含第一天和最后一天的字符串数组
     */
    public static String[] getMonthDateRange(String yearMonth) {
        return new String[] {
            getFirstDayOfMonth(yearMonth),
            getLastDayOfMonth(yearMonth)
        };
    }
    
    public static void main(String[] args) {
        String input = "2026-01";
        
        System.out.println("输入: " + input);
        System.out.println("当月第一天: " + getFirstDayOfMonth(input));
        System.out.println("当月最后一天: " + getLastDayOfMonth(input));
        
        // 或者获取范围
        String[] range = getMonthDateRange(input);
        System.out.println("\n日期范围:");
        System.out.println("开始: " + range[0]);
        System.out.println("结束: " + range[1]);
        
        // 测试其他月份
        testOtherMonths();
    }
    
    private static void testOtherMonths() {
        String[] testCases = {"2026-02", "2026-12", "2024-02"}; // 测试闰年
        
        System.out.println("\n测试其他月份:");
        for (String test : testCases) {
            String[] range = getMonthDateRange(test);
            System.out.println(test + ": " + range[0] + " ~ " + range[1]);
        }
    }
}