package org.zhinanzhen.tb.scheduled;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.zhinanzhen.b.controller.TestController;
import org.zhinanzhen.b.service.pojo.DataDTO;
import org.zhinanzhen.tb.utils.SendEmailUtil;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: bsz
 * Date: 2020/09/27 16:48
 * Description:
 * Version: V1.0
 */
@Component
@EnableScheduling
@Lazy(false)
public class Scheduled {

    @Autowired
    Data data;

    private Calendar calendar ;

    private StringBuilder content = null;

    private  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    String startDate = "2020-09-01";
    String lastSaturdayDate = "2020-09-19";
    String endDate = "2020-09-25";

    //    1-25号
    @org.springframework.scheduling.annotation.Scheduled(cron = " 0 0 12 * * ?")
    public void everyMonth(){
        calendar  = Calendar.getInstance();
        //String endDate = sdf.format(calendar.getTime());            //也就是当前时间
        //String startDate = DateClass.thisMonthFirstDay(calendar);   //本月一号的时间
        List<DataDTO> areaDataList = data.dataReport(startDate,endDate,"A"); //   A  全area地区的数据
        List<DataDTO> dataDTOList = data.dataReport(startDate,endDate,"R"); //  R 全area倒序排名的数据
        {
            content = EmailModel.start();
            content.append("1-25号  每个地区的数据 boss");
            content.append(EmailModel.areaModelHaveDate(areaDataList));//area各地区的数据
            content.append("1-25号  全澳本月业绩顾问排名(有money) boss");
            content.append(EmailModel.rankModelHave(dataDTOList)); //顾问业绩排名
            content.append(EmailModel.end());
            SendEmailUtil.send("jiaheng.xu@zhinanzhen.org","Data Report",content.toString());
        }
        {
            content = EmailModel.start();
            content.append("1-25号  全澳本月业绩顾问排名(No money) boss");
            content.append(EmailModel.rankModelNo(dataDTOList)); //顾问业绩排名
            content.append(EmailModel.end());
            SendEmailUtil.send("jiaheng.xu@zhinanzhen.org","Data Report",content.toString());
        }
        {
            for(DataDTO area : areaDataList){
                content = EmailModel.start();
                content.append("1-25号  Manager收到的地区总业绩");
                content.append(EmailModel.areaModelNoDate(area)); //顾问业绩排名
                content.append(EmailModel.end());
                SendEmailUtil.send("jiaheng.xu@zhinanzhen.org","Data Report",content.toString());
            }
        }
        {
            List<List<DataDTO>> regionList = RegionClassification.classification(dataDTOList);
            for(List<DataDTO> regionDataList:regionList){
                content = EmailModel.start();
                content.append("1-25号  Manager收到的地区顾问业绩");
                content.append(EmailModel.rankModelNo(regionDataList));
                content.append(EmailModel.end());
                SendEmailUtil.send("jiaheng.xu@zhinanzhen.org",regionDataList.get(0).getArea()+"的 Data report",content.toString());
            }
        }
    }

    //上周六到本周五
    @org.springframework.scheduling.annotation.Scheduled(cron = " 0 0 12 * * ?")
    public void everyWeek(){
        /**
         * 上周五
         calendar  = Calendar.getInstance();
         String endDate = sdf.format(calendar.getTime());
         String startDate = DateClass.lastFirday(calendar);
         */
        calendar  = Calendar.getInstance();
        //String endDate = sdf.format(calendar.getTime());            //也就是当前时间
        //String startDate = DateClass.lastMonthFirstDay(calendar);   //就是上个月一号的时间
        System.out.println("======"+lastSaturdayDate+"=========="+endDate);
        List<DataDTO> areaDataList = data.dataReport(lastSaturdayDate,endDate,"A"); //   A  地区的数据
        {
            content = EmailModel.start();
            content.append("上周六到本周五  boss 收到地区的总业绩");
            content.append(EmailModel.areaModelNoDate(areaDataList));
            content.append(EmailModel.end());
            SendEmailUtil.send("jiaheng.xu@zhinanzhen.org","Data report",content.toString());
        }
        {
            for(DataDTO area : areaDataList){
                content = EmailModel.start();
                content.append("1-上周六到本周五  Manager收到的地区总业绩");
                content.append(EmailModel.areaModelNoDate(area)); //顾问业绩排名
                content.append(EmailModel.end());
                SendEmailUtil.send("jiaheng.xu@zhinanzhen.org","Data Report",content.toString());
            }
        }
    }

}