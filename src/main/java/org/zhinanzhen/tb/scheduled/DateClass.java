package org.zhinanzhen.tb.scheduled;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: bsz
 * Date: 2020/09/27 10:45
 * Description:
 * Version: V1.0
 */
public class DateClass {

    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    //上月一号
    public static String lastMonthFirstDay(Calendar calendar){

        calendar.add(Calendar.MONTH, - 1);

        calendar.set(Calendar.DAY_OF_MONTH, 1);

        System.out.println("上月第一天"+sdf.format(calendar.getTime()));

        return sdf.format(calendar.getTime());
    }

    //上月最后一天
    public static String lastMonthLastDay(Calendar calendar){

        calendar=Calendar.getInstance();

        int month=calendar.get(Calendar.MONTH);

        calendar.set(Calendar.MONTH, month-1);

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));

        System.out.println("上月最后一天"+sdf.format(calendar.getTime()));

        return sdf.format(calendar.getTime());
    }

    public static void main(String[] args) {

        lastSaturday(Calendar.getInstance());
        thisMonthFirstDay(Calendar.getInstance());
        thisMonthFriday(Calendar.getInstance());

        lastMonthFirstDay(Calendar.getInstance());
        lastMonthLastDay(Calendar.getInstance());
        System.out.println("当前时间"+sdf.format(Calendar.getInstance().getTime()));

    }

    //上周6
    public static String lastSaturday(Calendar instance) {

        // 当前日期
        instance = Calendar.getInstance();
        instance.set(Calendar.DAY_OF_WEEK,7);
        instance.add(Calendar.DAY_OF_MONTH, -7);

        System.out.println(sdf.format(instance.getTime()));

        return sdf.format(instance.getTime());
    }

    //本周五
    public static String thisMonthFriday(Calendar instance) {

        // 当前日期
        instance.add(Calendar.DATE, -1);

        System.out.println(sdf.format(instance.getTime()));

        return sdf.format(instance.getTime());
    }

    //本月1号
    public static String thisMonthFirstDay(Calendar instance) {

        // 当前日期
        instance.add(Calendar.MONTH, 0);
        instance.set(Calendar.DAY_OF_MONTH, 1);

        System.out.println(sdf.format(instance.getTime()));

        return sdf.format(instance.getTime());
    }

    public static String lastLastSaturday() {
        // 上上周六
        Calendar instance = Calendar.getInstance();
        instance.set(Calendar.DAY_OF_WEEK,7);
        instance.add(Calendar.DAY_OF_MONTH, -14);
        System.out.println(sdf.format(instance.getTime()));
        return sdf.format(instance.getTime());
    }

    public static String lastFriday() {
        // 上周五
        Calendar instance = Calendar.getInstance();
        instance.set(Calendar.DAY_OF_WEEK,6);
        instance.add(Calendar.DAY_OF_MONTH, -7);
        System.out.println(sdf.format(instance.getTime()));
        return sdf.format(instance.getTime());
    }
}
