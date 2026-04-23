package org.zhinanzhen.tb.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 时间工具类 - 获取当前时刻和上个月同一时刻
 */
public class TimeUtil {
    
    private static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_PATTERN);
    
    /**
     * 获取当前时刻
     * @return LocalDateTime
     */
    public static LocalDateTime getCurrentDateTime() {
        return LocalDateTime.now();
    }
    
    /**
     * 获取上个月的同一时刻
     * 注意：如果当前是3月31日，上个月同一时刻是2月28日（或29日）的相同时间
     * @return LocalDateTime
     */
    public static LocalDateTime getLastMonthDateTime() {
        return getCurrentDateTime().minusMonths(1);
    }
    
    /**
     * 获取当前时刻（Date类型）
     * @return Date
     */
    public static Date getCurrentDate() {
        return new Date();
    }
    
    /**
     * 获取上个月的同一时刻（Date类型）
     * @return Date
     */
    public static Date getLastMonthDate() {
        return Date.from(getLastMonthDateTime().atZone(ZoneId.systemDefault()).toInstant());
    }
    
    /**
     * 获取当前时刻（字符串格式）
     * @return 格式：yyyy-MM-dd HH:mm:ss
     */
    public static String getCurrentTimeStr() {
        return getCurrentDateTime().format(DEFAULT_FORMATTER);
    }
    
    /**
     * 获取当前时刻（自定义格式）
     * @param pattern 格式，如：yyyy/MM/dd HH:mm:ss
     * @return 格式化的时间字符串
     */
    public static String getCurrentTimeStr(String pattern) {
        return getCurrentDateTime().format(DateTimeFormatter.ofPattern(pattern));
    }
    
    /**
     * 获取上个月的同一时刻（字符串格式）
     * @return 格式：yyyy-MM-dd HH:mm:ss
     */
    public static String getLastMonthTimeStr() {
        return getLastMonthDateTime().format(DEFAULT_FORMATTER);
    }
    
    /**
     * 获取上个月的同一时刻（自定义格式）
     * @param pattern 格式，如：yyyy/MM/dd HH:mm:ss
     * @return 格式化的时间字符串
     */
    public static String getLastMonthTimeStr(String pattern) {
        return getLastMonthDateTime().format(DateTimeFormatter.ofPattern(pattern));
    }
    
    /**
     * 获取当前时刻的时间戳（毫秒）
     * @return 时间戳
     */
    public static long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }
    
    /**
     * 获取上个月同一时刻的时间戳（毫秒）
     * @return 时间戳
     */
    public static long getLastMonthTimestamp() {
        return getLastMonthDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
    
    /**
     * 获取当前和上个月的时间对（数组形式）
     * @return [当前时间字符串, 上个月时间字符串]
     */
    public static String[] getCurrentAndLastMonth() {
        return new String[]{getCurrentTimeStr(), getLastMonthTimeStr()};
    }
    
    /**
     * 获取当前和上个月的时间对（Map形式）
     * @return 包含current和lastMonth的Map
     */
    public static java.util.Map<String, String> getCurrentAndLastMonthMap() {
        java.util.Map<String, String> map = new java.util.HashMap<>();
        map.put("current", getCurrentTimeStr());
        map.put("lastMonth", getLastMonthTimeStr());
        return map;
    }
    
    // ==================== 新增比较方法 ====================
    
    /**
     * 判断传入的时间是否在当前时刻和上个月同一时刻之间（包含边界）
     * @param dateTime 要判断的时间（LocalDateTime类型）
     * @return true-在区间内，false-不在区间内
     */
    public static boolean isBetweenCurrentAndLastMonth(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        LocalDateTime lastMonth = getLastMonthDateTime();
        LocalDateTime current = getCurrentDateTime();
        
        // 确保上个月时间小于当前时间
        LocalDateTime start = lastMonth.isBefore(current) ? lastMonth : current;
        LocalDateTime end = lastMonth.isBefore(current) ? current : lastMonth;
        
        return (dateTime.isEqual(start) || dateTime.isAfter(start)) && 
               (dateTime.isEqual(end) || dateTime.isBefore(end));
    }
    
    /**
     * 判断传入的时间是否在当前时刻和上个月同一时刻之间（包含边界）
     * @param date 要判断的时间（Date类型）
     * @return true-在区间内，false-不在区间内
     */
    public static boolean isBetweenCurrentAndLastMonth(Date date) {
        if (date == null) {
            return false;
        }
        return isBetweenCurrentAndLastMonth(date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime());
    }
    
    /**
     * 判断传入的时间是否在当前时刻和上个月同一时刻之间（包含边界）
     * @param timeStr 要判断的时间字符串（格式：yyyy-MM-dd HH:mm:ss）
     * @return true-在区间内，false-不在区间内
     */
    public static boolean isBetweenCurrentAndLastMonth(String timeStr) {
        return isBetweenCurrentAndLastMonth(timeStr, DEFAULT_PATTERN);
    }
    
    /**
     * 判断传入的时间是否在当前时刻和上个月同一时刻之间（包含边界）
     * @param timeStr 要判断的时间字符串
     * @param pattern 时间字符串的格式
     * @return true-在区间内，false-不在区间内
     */
    public static boolean isBetweenCurrentAndLastMonth(String timeStr, String pattern) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return false;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            LocalDateTime dateTime = LocalDateTime.parse(timeStr, formatter);
            return isBetweenCurrentAndLastMonth(dateTime);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 判断传入的时间戳是否在当前时刻和上个月同一时刻之间（包含边界）
     * @param timestamp 要判断的时间戳（毫秒）
     * @return true-在区间内，false-不在区间内
     */
    public static boolean isBetweenCurrentAndLastMonth(long timestamp) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(
                new Date(timestamp).toInstant(), 
                ZoneId.systemDefault()
        );
        return isBetweenCurrentAndLastMonth(dateTime);
    }
    
    /**
     * 判断传入的时间是否在当前时刻和上个月同一时刻之间（不包含边界）
     * @param dateTime 要判断的时间（LocalDateTime类型）
     * @return true-在区间内，false-不在区间内
     */
    public static boolean isBetweenCurrentAndLastMonthExclusive(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        LocalDateTime lastMonth = getLastMonthDateTime();
        LocalDateTime current = getCurrentDateTime();
        
        LocalDateTime start = lastMonth.isBefore(current) ? lastMonth : current;
        LocalDateTime end = lastMonth.isBefore(current) ? current : lastMonth;
        
        return dateTime.isAfter(start) && dateTime.isBefore(end);
    }
    
    /**
     * 判断传入的时间是否在当前时刻和上个月同一时刻之间（不包含边界）- Date版本
     */
    public static boolean isBetweenCurrentAndLastMonthExclusive(Date date) {
        if (date == null) {
            return false;
        }
        return isBetweenCurrentAndLastMonthExclusive(date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime());
    }
    
    /**
     * 判断传入的时间是否在当前时刻和上个月同一时刻之间（不包含边界）- 字符串版本
     */
    public static boolean isBetweenCurrentAndLastMonthExclusive(String timeStr) {
        return isBetweenCurrentAndLastMonthExclusive(timeStr, DEFAULT_PATTERN);
    }
    
    /**
     * 判断传入的时间是否在当前时刻和上个月同一时刻之间（不包含边界）- 字符串自定义格式版本
     */
    public static boolean isBetweenCurrentAndLastMonthExclusive(String timeStr, String pattern) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return false;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            LocalDateTime dateTime = LocalDateTime.parse(timeStr, formatter);
            return isBetweenCurrentAndLastMonthExclusive(dateTime);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 判断传入的时间是否在当前时刻和上个月同一时刻之间（包含边界）
     * 返回详细信息（包含区间范围）
     * @param dateTime 要判断的时间
     * @return 包含判断结果和区间信息的Map
     */
    public static java.util.Map<String, Object> checkBetweenWithDetail(LocalDateTime dateTime) {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        
        if (dateTime == null) {
            result.put("isBetween", false);
            result.put("error", "时间为空");
            return result;
        }
        
        LocalDateTime lastMonth = getLastMonthDateTime();
        LocalDateTime current = getCurrentDateTime();
        
        LocalDateTime start = lastMonth.isBefore(current) ? lastMonth : current;
        LocalDateTime end = lastMonth.isBefore(current) ? current : lastMonth;
        
        boolean isBetween = (dateTime.isEqual(start) || dateTime.isAfter(start)) && 
                           (dateTime.isEqual(end) || dateTime.isBefore(end));
        
        result.put("isBetween", isBetween);
        result.put("startTime", start.format(DEFAULT_FORMATTER));
        result.put("endTime", end.format(DEFAULT_FORMATTER));
        result.put("checkTime", dateTime.format(DEFAULT_FORMATTER));
        result.put("compareResult", isBetween ? "在区间内" : "不在区间内");
        
        return result;
    }

    // ==================== 上上个月范围判断方法 ====================

    /**
     * 获取上上个月的第一天（00:00:00）
     * 例如：当前是2026年4月，上上个月是2026年2月，返回 2026-02-01 00:00:00
     * @return LocalDateTime
     */
    public static LocalDateTime getFirstDayOfLastLastMonth() {
        LocalDateTime current = getCurrentDateTime();
        // 先减去2个月得到上上个月，然后设置为该月的第一天
        LocalDateTime lastLastMonth = current.minusMonths(2);
        return lastLastMonth.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
    }

    /**
     * 获取上上个月的最后一天（23:59:59）
     * 例如：当前是2026年4月，上上个月是2026年2月，返回 2026-02-28 23:59:59（或2026-02-29如果是闰年）
     * @return LocalDateTime
     */
    public static LocalDateTime getLastDayOfLastLastMonth() {
        LocalDateTime current = getCurrentDateTime();
        // 先减去2个月得到上上个月
        LocalDateTime lastLastMonth = current.minusMonths(2);
        // 获取上上个月的第一天，然后加上1个月再减去1天，得到上上个月的最后一天
        LocalDateTime firstDayOfLastLastMonth = lastLastMonth.withDayOfMonth(1);
        LocalDateTime lastDayOfLastLastMonth = firstDayOfLastLastMonth.plusMonths(1).minusDays(1);
        return lastDayOfLastLastMonth.withHour(23).withMinute(59).withSecond(59).withNano(999999999);
    }

    /**
     * 判断传入的时间是否在上上个月的第一天和最后一天之间（包含边界）
     * @param dateTime 要判断的时间（LocalDateTime类型）
     * @return true-在区间内，false-不在区间内
     */
    public static boolean isBetweenLastLastMonth(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        LocalDateTime start = getFirstDayOfLastLastMonth();
        LocalDateTime end = getLastDayOfLastLastMonth();
        return (dateTime.isEqual(start) || dateTime.isAfter(start)) &&
                (dateTime.isEqual(end) || dateTime.isBefore(end));
    }

    /**
     * 判断传入的时间是否在上上个月的第一天和最后一天之间（包含边界）
     * @param date 要判断的时间（Date类型）
     * @return true-在区间内，false-不在区间内
     */
    public static boolean isBetweenLastLastMonth(Date date) {
        if (date == null) {
            return false;
        }
        return isBetweenLastLastMonth(date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime());
    }

    /**
     * 判断传入的时间是否在上上个月的第一天和最后一天之间（包含边界）
     * @param timeStr 要判断的时间字符串（格式：yyyy-MM-dd HH:mm:ss）
     * @return true-在区间内，false-不在区间内
     */
    public static boolean isBetweenLastLastMonth(String timeStr) {
        return isBetweenLastLastMonth(timeStr, DEFAULT_PATTERN);
    }

    /**
     * 判断传入的时间是否在上上个月的第一天和最后一天之间（包含边界）
     * @param timeStr 要判断的时间字符串
     * @param pattern 时间字符串的格式
     * @return true-在区间内，false-不在区间内
     */
    public static boolean isBetweenLastLastMonth(String timeStr, String pattern) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return false;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            LocalDateTime dateTime = LocalDateTime.parse(timeStr, formatter);
            return isBetweenLastLastMonth(dateTime);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断传入的时间戳是否在上上个月的第一天和最后一天之间（包含边界）
     * @param timestamp 要判断的时间戳（毫秒）
     * @return true-在区间内，false-不在区间内
     */
    public static boolean isBetweenLastLastMonth(long timestamp) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(
                new Date(timestamp).toInstant(),
                ZoneId.systemDefault()
        );
        return isBetweenLastLastMonth(dateTime);
    }

    /**
     * 判断传入的时间是否在上上个月的第一天和最后一天之间（不包含边界）
     * @param dateTime 要判断的时间（LocalDateTime类型）
     * @return true-在区间内，false-不在区间内
     */
    public static boolean isBetweenLastLastMonthExclusive(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        LocalDateTime start = getFirstDayOfLastLastMonth();
        LocalDateTime end = getLastDayOfLastLastMonth();
        return dateTime.isAfter(start) && dateTime.isBefore(end);
    }

    /**
     * 判断传入的时间是否在上上个月的第一天和最后一天之间（不包含边界）- Date版本
     */
    public static boolean isBetweenLastLastMonthExclusive(Date date) {
        if (date == null) {
            return false;
        }
        return isBetweenLastLastMonthExclusive(date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime());
    }

    /**
     * 判断传入的时间是否在上上个月的第一天和最后一天之间（不包含边界）- 字符串版本
     */
    public static boolean isBetweenLastLastMonthExclusive(String timeStr) {
        return isBetweenLastLastMonthExclusive(timeStr, DEFAULT_PATTERN);
    }

    /**
     * 判断传入的时间是否在上上个月的第一天和最后一天之间（不包含边界）- 字符串自定义格式版本
     */
    public static boolean isBetweenLastLastMonthExclusive(String timeStr, String pattern) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return false;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            LocalDateTime dateTime = LocalDateTime.parse(timeStr, formatter);
            return isBetweenLastLastMonthExclusive(dateTime);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断传入的时间是否在上上个月的第一天和最后一天之间（包含边界）
     * 返回详细信息（包含区间范围）
     * @param dateTime 要判断的时间
     * @return 包含判断结果和区间信息的Map
     */
    public static java.util.Map<String, Object> checkBetweenLastLastMonthWithDetail(LocalDateTime dateTime) {
        java.util.Map<String, Object> result = new java.util.HashMap<>();

        if (dateTime == null) {
            result.put("isBetween", false);
            result.put("error", "时间为空");
            return result;
        }

        LocalDateTime start = getFirstDayOfLastLastMonth();
        LocalDateTime end = getLastDayOfLastLastMonth();

        boolean isBetween = (dateTime.isEqual(start) || dateTime.isAfter(start)) &&
                (dateTime.isEqual(end) || dateTime.isBefore(end));

        result.put("isBetween", isBetween);
        result.put("startTime", start.format(DEFAULT_FORMATTER));
        result.put("endTime", end.format(DEFAULT_FORMATTER));
        result.put("checkTime", dateTime.format(DEFAULT_FORMATTER));
        result.put("compareResult", isBetween ? "在区间内" : "不在区间内");

        return result;
    }


    /**
     * 主方法 - 示例用法
     */
    public static void main(String[] args) {
        // 示例：获取当前时刻和上个月同一时刻
        System.out.println("=== 基础功能示例 ===");
        System.out.println("当前时刻: " + getCurrentTimeStr());
        System.out.println("上个月同一时刻: " + getLastMonthTimeStr());
        
        System.out.println("\n=== 比较方法示例 ===");
        
        // 测试1：判断当前时间
        LocalDateTime now = getCurrentDateTime();
        System.out.println("判断当前时间: " + isBetweenCurrentAndLastMonth(now)); // true
        
        // 测试2：判断上个月时间
        LocalDateTime lastMonth = getLastMonthDateTime();
        System.out.println("判断上个月时间: " + isBetweenCurrentAndLastMonth(lastMonth)); // true
        
        // 测试3：判断中间某个时间
        LocalDateTime middle = getLastMonthDateTime().plusDays(15);
        System.out.println("判断中间时间: " + isBetweenCurrentAndLastMonth(middle)); // true
        
        // 测试4：判断未来的时间
        LocalDateTime future = getCurrentDateTime().plusDays(1);
        System.out.println("判断未来时间: " + isBetweenCurrentAndLastMonth(future)); // false
        
        // 测试5：判断过去的时间
        LocalDateTime past = getLastMonthDateTime().minusDays(1);
        System.out.println("判断过去时间: " + isBetweenCurrentAndLastMonth(past)); // false
        
        // 测试6：使用字符串判断
        String testTime = "2024-03-20 15:30:00";
        System.out.println("\n判断字符串时间 '" + testTime + "': " + isBetweenCurrentAndLastMonth(testTime));
        
        // 测试7：不包含边界的判断
        System.out.println("\n不包含边界判断:");
        System.out.println("当前时间(边界): " + isBetweenCurrentAndLastMonthExclusive(now)); // false
        System.out.println("上个月时间(边界): " + isBetweenCurrentAndLastMonthExclusive(lastMonth)); // false
        System.out.println("中间时间: " + isBetweenCurrentAndLastMonthExclusive(middle)); // true
        
        // 测试8：详细判断信息
        System.out.println("\n详细判断信息:");
        java.util.Map<String, Object> detail = checkBetweenWithDetail(middle);
        detail.forEach((key, value) -> System.out.println(key + ": " + value));
        
        // 测试9：时间戳判断
        long timestamp = System.currentTimeMillis();
        System.out.println("\n时间戳判断: " + isBetweenCurrentAndLastMonth(timestamp));

        // 测试上上个月范围判断
        System.out.println("\n=== 上上个月范围判断示例 ===");
        System.out.println("上上个月第一天: " + getFirstDayOfLastLastMonth().format(DEFAULT_FORMATTER));
        System.out.println("上上个月最后一天: " + getLastDayOfLastLastMonth().format(DEFAULT_FORMATTER));

        // 测试边界值
        LocalDateTime firstDay = getFirstDayOfLastLastMonth();
        LocalDateTime lastDay = getLastDayOfLastLastMonth();
        System.out.println("第一天(边界): " + isBetweenLastLastMonth(firstDay)); // true
        System.out.println("最后一天(边界): " + isBetweenLastLastMonth(lastDay)); // true

        // 测试区间内时间
        LocalDateTime middle1 = firstDay.plusDays(5);
        System.out.println("区间内时间: " + isBetweenLastLastMonth(middle1)); // true

        // 测试区间外时间
        LocalDateTime outOfRange = lastDay.plusDays(1);
        System.out.println("区间外时间: " + isBetweenLastLastMonth(outOfRange)); // false
    }
}