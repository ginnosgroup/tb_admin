package org.zhinanzhen.tb.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class AustraliaTimeUtil {

    private static final ZoneId SYDNEY_ZONE = ZoneId.of("Australia/Sydney");
    private static final ZoneId SYSTEM_ZONE = ZoneId.systemDefault();
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter SYDNEY_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");

    /**
     * 获取当前澳洲悉尼时间
     */
    public static ZonedDateTime getCurrentSydneyTime() {
        return ZonedDateTime.now(SYDNEY_ZONE);
    }

    /**
     * 将指定ZonedDateTime时间转换为澳洲悉尼时间
     */
    public static ZonedDateTime toSydneyTime(ZonedDateTime dateTime) {
        return dateTime.withZoneSameInstant(SYDNEY_ZONE);
    }

    /**
     * 将字符串格式的时间（yyyy-MM-dd HH:mm:ss）转换为澳洲悉尼时间
     * @param dateTimeStr 字符串时间，格式：yyyy-MM-dd HH:mm:ss
     * @return 转换后的悉尼时间
     * @throws DateTimeParseException 如果字符串格式不正确
     */
    public static ZonedDateTime toSydneyTime(String dateTimeStr) {
        // 将字符串解析为LocalDateTime（不带时区信息）
        LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStr, FORMATTER);

        // 假设输入的字符串是系统默认时区的时间
        ZonedDateTime systemZonedDateTime = localDateTime.atZone(SYSTEM_ZONE);

        // 转换为悉尼时间
        return systemZonedDateTime.withZoneSameInstant(SYDNEY_ZONE);
    }

    /**
     * 将字符串格式的时间（指定时区）转换为澳洲悉尼时间
     * @param dateTimeStr 字符串时间，格式：yyyy-MM-dd HH:mm:ss
     * @param sourceZone 源时区
     * @return 转换后的悉尼时间
     */
    public static ZonedDateTime toSydneyTime(String dateTimeStr, ZoneId sourceZone) {
        LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStr, FORMATTER);
        ZonedDateTime sourceZonedDateTime = localDateTime.atZone(sourceZone);
        return sourceZonedDateTime.withZoneSameInstant(SYDNEY_ZONE);
    }

    /**
     * 将字符串格式的时间转换为澳洲悉尼时间，并返回格式化结果
     * @param dateTimeStr 字符串时间，格式：yyyy-MM-dd HH:mm:ss
     * @return 格式化后的悉尼时间（包含时区信息）
     */
    public static String toSydneyTimeAndFormat(String dateTimeStr) {
        try {
            ZonedDateTime sydneyTime = toSydneyTime(dateTimeStr);
            return sydneyTime.format(SYDNEY_FORMATTER);
        } catch (DateTimeParseException e) {
            return "时间格式错误，请使用格式：yyyy-MM-dd HH:mm:ss";
        }
    }

    /**
     * 获取格式化的澳洲悉尼时间
     */
    public static String getFormattedSydneyTime() {
        return getCurrentSydneyTime().format(SYDNEY_FORMATTER);
    }

    public static void main(String[] args) {
        // 测试用例1：转换当前系统时间为悉尼时间
        System.out.println("当前澳洲悉尼时间: " + getFormattedSydneyTime());

        // 测试用例2：转换字符串时间为悉尼时间（假设字符串是系统时区）
        String testTime = "2024-01-15 14:30:00";
        ZonedDateTime sydneyTime1 = toSydneyTime(testTime);
        System.out.println("\n测试转换字符串时间（系统时区）：");
        System.out.println("输入时间: " + testTime + " (系统时区)");
        System.out.println("悉尼时间: " + sydneyTime1.format(SYDNEY_FORMATTER));

        // 测试用例3：转换字符串时间为悉尼时间（指定源时区）
        String testTime2 = "2024-01-15 14:30:00";
        ZoneId beijingZone = ZoneId.of("Asia/Shanghai");
        ZonedDateTime sydneyTime2 = toSydneyTime(testTime2, beijingZone);
        System.out.println("\n测试转换字符串时间（指定北京时区）：");
        System.out.println("输入时间: " + testTime2 + " (北京时间)");
        System.out.println("悉尼时间: " + sydneyTime2.format(SYDNEY_FORMATTER));

        // 测试用例4：直接获取格式化结果
        String testTime3 = "2024-01-15 09:00:00";
        String formattedResult = toSydneyTimeAndFormat(testTime3);
        System.out.println("\n测试直接获取格式化结果：");
        System.out.println("输入时间: " + testTime3 + " (系统时区)");
        System.out.println("格式化悉尼时间: " + formattedResult);

        // 测试用例5：错误格式测试
        String wrongTime = "2024/01/15 14:30:00";
        String errorResult = toSydneyTimeAndFormat(wrongTime);
        System.out.println("\n错误格式测试：");
        System.out.println("输入时间: " + wrongTime);
        System.out.println("结果: " + errorResult);

        // 测试用例6：演示不同时区的影响
        System.out.println("\n=== 演示不同源时区对转换结果的影响 ===");
        String sameTime = "2024-01-15 12:00:00";

        // 假设是UTC时间
        ZonedDateTime fromUTC = toSydneyTime(sameTime, ZoneId.of("UTC"));
        System.out.println("输入时间: " + sameTime + " (UTC时间) -> 悉尼时间: " +
                fromUTC.format(SYDNEY_FORMATTER));

        // 假设是北京时间 (UTC+8)
        ZonedDateTime fromBeijing = toSydneyTime(sameTime, ZoneId.of("Asia/Shanghai"));
        System.out.println("输入时间: " + sameTime + " (北京时间) -> 悉尼时间: " +
                fromBeijing.format(SYDNEY_FORMATTER));

        // 假设是系统默认时区
        ZonedDateTime fromSystem = toSydneyTime(sameTime);
        System.out.println("输入时间: " + sameTime + " (系统时区: " + SYSTEM_ZONE + ") -> 悉尼时间: " +
                fromSystem.format(SYDNEY_FORMATTER));
    }
}