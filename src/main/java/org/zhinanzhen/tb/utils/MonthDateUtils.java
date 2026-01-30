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
     * 获取上一个月的第一天
     * @param yearMonth 格式：yyyy-MM
     * @return 上一个月的第一天，格式：yyyy-MM-dd
     */
    public static String getFirstDayOfPreviousMonth(String yearMonth) {
        try {
            YearMonth currentYearMonth = YearMonth.parse(yearMonth, MONTH_FORMATTER);
            // 获取上一个月
            YearMonth previousMonth = currentYearMonth.minusMonths(1);
            return previousMonth.atDay(1).format(DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("日期格式错误，请使用 yyyy-MM 格式", e);
        }
    }

    /**
     * 获取上一个月的最后一天
     * @param yearMonth 格式：yyyy-MM
     * @return 上一个月的最后一天，格式：yyyy-MM-dd
     */
    public static String getLastDayOfPreviousMonth(String yearMonth) {
        try {
            YearMonth currentYearMonth = YearMonth.parse(yearMonth, MONTH_FORMATTER);
            // 获取上一个月
            YearMonth previousMonth = currentYearMonth.minusMonths(1);
            return previousMonth.atEndOfMonth().format(DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("日期格式错误，请使用 yyyy-MM 格式", e);
        }
    }

    /**
     * 获取上一年的同月第一天
     * @param yearMonth 格式：yyyy-MM
     * @return 上一年的同月第一天，格式：yyyy-MM-dd
     */
    public static String getFirstDayOfSameMonthLastYear(String yearMonth) {
        try {
            YearMonth currentYearMonth = YearMonth.parse(yearMonth, MONTH_FORMATTER);
            // 获取上一年的同月
            YearMonth lastYearMonth = currentYearMonth.minusYears(1);
            return lastYearMonth.atDay(1).format(DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("日期格式错误，请使用 yyyy-MM 格式", e);
        }
    }

    /**
     * 获取上一年的同月最后一天
     * @param yearMonth 格式：yyyy-MM
     * @return 上一年的同月最后一天，格式：yyyy-MM-dd
     */
    public static String getLastDayOfSameMonthLastYear(String yearMonth) {
        try {
            YearMonth currentYearMonth = YearMonth.parse(yearMonth, MONTH_FORMATTER);
            // 获取上一年的同月
            YearMonth lastYearMonth = currentYearMonth.minusYears(1);
            return lastYearMonth.atEndOfMonth().format(DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("日期格式错误，请使用 yyyy-MM 格式", e);
        }
    }

    /**
     * 获取上一个7月1日日期
     * @param yearMonthStr
     * @return
     */
    public static String getPreviousJulyFirst(String yearMonthStr) {
        // 解析输入的年月字符串
        YearMonth yearMonth = YearMonth.parse(yearMonthStr);

        // 创建该月的第一天
        LocalDate inputDate = yearMonth.atDay(1);

        // 获取上一个7月1日
        LocalDate previousJulyFirst;

        // 如果当前月份大于等于7月，则上一个7月1日在当前年份
        if (inputDate.getMonthValue() >= 7) {
            previousJulyFirst = LocalDate.of(inputDate.getYear(), 7, 1);
        } else {
            // 如果当前月份小于7月，则上一个7月1日在上一年
            previousJulyFirst = LocalDate.of(inputDate.getYear() - 1, 7, 1);
        }

        // 格式化为yyyy-MM-dd
        return previousJulyFirst.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    /**
     * 方法二：使用 substring 和 indexOf
     */
    public static String getMonthFromDate(String dateStr) {
        if (dateStr == null || dateStr.length() < 7) {  // 至少需要 "2025-01"
            return "";
        }

        try {
            // 查找第一个 "-" 的位置
            int firstDashIndex = dateStr.indexOf("-");
            if (firstDashIndex == -1) {
                return "";
            }

            // 查找第二个 "-" 的位置
            int secondDashIndex = dateStr.indexOf("-", firstDashIndex + 1);
            if (secondDashIndex == -1) {
                return "";
            }

            // 提取月份部分 (YYYY-MM-DD 中的 MM)
            String month = dateStr.substring(firstDashIndex + 1, secondDashIndex);

            // 使用正则表达式去除开头的0
            return month.replaceFirst("^0+(?!$)", "");
        } catch (Exception e) {
            return "";
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