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

    //上月的第一天
    public static String lastMonthFirstDay(Calendar calendar){

        int maxCurrentMonthDay=0;

        System.out.println("当前时间: "+sdf.format(calendar.getTime()));

        maxCurrentMonthDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        System.out.println("maxCurrentMonthDay   " + maxCurrentMonthDay);
        System.out.println("Calendar.DAY_OF_MONTH    " + Calendar.DAY_OF_MONTH);

        calendar.add(Calendar.DAY_OF_MONTH, - maxCurrentMonthDay);

        calendar.set(Calendar.DAY_OF_MONTH, 1);

        System.out.println("上月第一天: "+sdf.format(calendar.getTime()));

        return sdf.format(calendar.getTime());
    }


    public static void main(String[] args) {
        Calendar instance = Calendar.getInstance();
        lastSaturday(instance);
    }

    //上周五
    public static String lastSaturday(Calendar instance) {

        // 当前日期
        instance = Calendar.getInstance();
        instance.set(Calendar.DAY_OF_WEEK,7);
        instance.add(Calendar.DAY_OF_MONTH, -7);

        System.out.println(sdf.format(instance.getTime()));

        return sdf.format(instance.getTime());
    }

    //本月一号
    public static String thisMonthFirstDay(Calendar instance) {

        // 当前日期
        instance.add(Calendar.MONTH, 0);
        instance.set(Calendar.DAY_OF_MONTH, 1);

        System.out.println(sdf.format(instance.getTime()));

        return sdf.format(instance.getTime());
    }

}
