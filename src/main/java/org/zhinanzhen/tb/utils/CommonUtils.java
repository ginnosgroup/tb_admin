package org.zhinanzhen.tb.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zhinanzhen.b.controller.ServiceOrderController;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * Date: 2021/06/28 下午 6:01
 * Description:常用的工具类
 * Version: V1.0
 */
public class CommonUtils {

    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    private static final Logger LOG = LoggerFactory.getLogger(CommonUtils.class);

    public static int getDateDays(Date date1, Date date2) {
        try {
             date1 =  sdf.parse(sdf.format(date1));
             date2 =  sdf.parse(sdf.format(date2));
        } catch (ParseException e) {
            LOG.debug("时间转换出现异常");
            e.printStackTrace();
        }
        System.out.println(date1.getTime());
        return (int) ((date1.getTime() - date2.getTime()) / (3600 * 24 * 1000));
    }
}
