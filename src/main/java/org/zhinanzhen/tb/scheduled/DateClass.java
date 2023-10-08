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
        
        thisMonth(Calendar.getInstance());
        lastMonth(Calendar.getInstance());
        
        lastMonthFirstDay(Calendar.getInstance());
        lastMonthLastDay(Calendar.getInstance());
        System.out.println(thisMonth(Calendar.getInstance()));

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

        return sdf.format(instance.getTime());
    }
    
	public static String thisMonth(Calendar instance) {
		return instance.get(Calendar.MONTH) + 1 + "";
	}

	public static String lastMonth(Calendar instance) {
		int m = instance.get(Calendar.MONTH);
		if (m == 0)
			m = 12;
		return m + "";
	}

    //本月1号
    public static String thisMonthFirstDay(Calendar instance) {

        // 当前日期
        instance.add(Calendar.MONTH, 0);
        instance.set(Calendar.DAY_OF_MONTH, 1);

        return sdf.format(instance.getTime());
    }

    public static String lastLastSaturday() {
        // 上上周六
        Calendar instance = Calendar.getInstance();
        instance.set(Calendar.DAY_OF_WEEK,7);
        instance.add(Calendar.DAY_OF_MONTH, -14);
        return sdf.format(instance.getTime());
    }

    public static String lastFriday() {
        // 上周五
        Calendar instance = Calendar.getInstance();
        instance.set(Calendar.DAY_OF_WEEK,6);
        instance.add(Calendar.DAY_OF_MONTH, -7);
        return sdf.format(instance.getTime());
    }

    public static String today() {
        // 今天,因为今天时分秒是 00:00:00,表示截止到昨天
        Calendar instance = Calendar.getInstance();
        return sdf.format(instance.getTime());
    }

    public static String lastYearThisMonthFirstDay(Calendar calendar){
        // 去年的同月1号
        //calendar.add(Calendar.MONTH, - 1);
        calendar.add(Calendar.YEAR, - 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return sdf.format(calendar.getTime());
    }

    public static String lastYearThisMonthLastDay(){
        Calendar calendar=Calendar.getInstance();
        int month=calendar.get(Calendar.MONTH);
        //calendar.set(Calendar.MONTH, month-1);
        calendar.add(Calendar.YEAR, -1);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return sdf.format(calendar.getTime());
    }

    public static String thisYearFirstDay(){
        Calendar calendar=Calendar.getInstance();
        calendar.set(Calendar.MONTH,0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return sdf.format(calendar.getTime());
    }

    public static String _7_1(){
        Calendar calendar = Calendar.getInstance();
        long time1 = calendar.getTimeInMillis();

        //6月最后一天
        calendar.set(Calendar.MONTH, 6);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long time2 = calendar.getTimeInMillis();

        if (time1 < time2){
            calendar.add(Calendar.YEAR, -1);
            return sdf.format(new Date(calendar.getTimeInMillis()));
        }

        return sdf.format(new Date(time2));
    }
}
