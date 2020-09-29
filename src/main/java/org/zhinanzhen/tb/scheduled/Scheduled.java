package org.zhinanzhen.tb.scheduled;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
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

    //每月发送一次邮件
    //@org.springframework.scheduling.annotation.Scheduled(cron = "0 */5 * * * ?")
    public void everyMonth(){
        calendar  = Calendar.getInstance();
        System.out.println("邮件开始发送");
        String endDate = sdf.format(calendar.getTime());            //也就是当前时间
        String startDate = DateClass.lastMonthFirstDay(calendar);   //就是上个月一号的时间
        List<DataDTO> areaDataList = data.dataReport(startDate,endDate,"A"); //   A  全area地区的数据
        List<DataDTO> dataDTOList = data.dataReport(startDate,endDate,"R"); //  R 全area倒序排名的数据
        {
            content = EmailModel.start();
            content.append(EmailModel.areaModelHaveDate(areaDataList));//area各地区的数据
            content.append(EmailModel.rankModelHave(dataDTOList)); //顾问业绩排名
            content.append(EmailModel.end());
            SendEmailUtil.send("jiaheng.xu@zhinanzhen.org","每月,Boss：Area各地区的数据+顾问业绩排名(money)",content.toString());
        }
        /*
        System.out.println("======"+startDate+"=========="+endDate);
        {                                                               //这里发送的是manager的每个顾问的排名，没有业绩数据列（每月）
            content = EmailModel.start();
            content.append(EmailModel.end());
            SendEmailUtil.send("baoshanzhou@hotmail.com","每月，boss：顾问业绩排名(money)",content.toString());
        }
         */
    }

    //每周发送
    //@org.springframework.scheduling.annotation.Scheduled(cron = " 0 */5 * * * ?")
    public void everyWeek(){

         //* 上周五
         calendar  = Calendar.getInstance();
         String endDate = sdf.format(calendar.getTime());
         String startDate = DateClass.lastFirday(calendar);

        /*calendar  = Calendar.getInstance();
        String endDate = sdf.format(calendar.getTime());            //也就是当前时间
        String startDate = DateClass.lastMonthFirstDay(calendar);   //就是上个月一号的时间

         */

        List<DataDTO> areaDataList = data.dataReport(startDate,endDate,"A"); //   A  地区的数据
        List<DataDTO> dataDTOList = data.dataReport(startDate,endDate,"R"); //    R  排名的数据

        System.out.println("======"+startDate+"=========="+endDate);
        {
            content = EmailModel.start();
            content.append(EmailModel.areaModelNoDate(areaDataList));
            content.append(EmailModel.rankModelNo(dataDTOList));
            content.append(EmailModel.end());
            SendEmailUtil.send("jiaheng.xu@zhinanzhen.org","每周，boss：顾问业绩排名(NO money)",content.toString());
        }
        {
            List<List<DataDTO>> regionList = RegionClassification.classification(dataDTOList);
            for(List<DataDTO> regionDataList:regionList){
                content = new StringBuilder("<html><head></head><body>");
                content.append(EmailModel.rankModelNo(regionDataList));
                content.append(EmailModel.end());
                //if(regionDataList.get(0).getArea().equals("NSW & ACT")){
                    SendEmailUtil.send("jiaheng.xu@zhinanzhen.org",regionDataList.get(0).getArea(),content.toString());
                //}
                //if(regionDataList.get(0).getArea().equals("Sydney")){
                //    SendEmailUtil.send("baoshanzhou@hotmail.com","Sydney",content.toString());
                //}
                //if(regionDataList.get(0).getArea().equals("Hobart")){
                //   SendEmailUtil.send("baoshanzhou@hotmail.com","Hobart",content.toString());
                //}
            }
        }
    }

    //给每个地区的manager发送的排名
    //@org.springframework.scheduling.annotation.Scheduled(cron = "0 */1 * * * ?")
    public void tset(){
        calendar  = Calendar.getInstance();
        System.out.println("邮件开始发送");
        String endDate = sdf.format(calendar.getTime());            //也就是当前时间
        String startDate = DateClass.lastMonthFirstDay(calendar);   //就是上个月一号的时间
        List<DataDTO> dataDTOList = data.dataReport(startDate,endDate,"R"); //  R 排名的数据
        System.out.println("每月开始发送的邮件======"+startDate+"=========="+endDate);
        dataDTOList.forEach(a-> System.out.println(a.toString()));
        List<List<DataDTO>> regionList = RegionClassification.classification(dataDTOList);

        for(List<DataDTO> regionDataList:regionList){
            content = new StringBuilder("<html><head></head><body>");
            regionDataList.forEach(aa-> System.out.println("dddddddddd"+aa.toString()+regionDataList.size()));
            content.append(EmailModel.rankModelNo(regionDataList));
            content.append(EmailModel.end());
            if(regionDataList.get(0).getArea().equals("NSW & ACT")){
                SendEmailUtil.send("815124560@qq.com","title",content.toString());
            }
            if(regionDataList.get(0).getArea().equals("Sydney")){
                SendEmailUtil.send("baoshanzhou@hotmail.com","title",content.toString());
            }
            if(regionDataList.get(0).getArea().equals("Hobart")){
                SendEmailUtil.send("baoshanzhou@hotmail.com","title",content.toString());
            }
        }
    }



}