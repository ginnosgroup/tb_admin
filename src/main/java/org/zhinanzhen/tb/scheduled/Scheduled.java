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
/*
boss邮件组：paul@zhinanzhen.org;elvin@zhinanzhen.org;jiaheng.xu@zhinanzhen.org
Sydney：juntao@zhinanzhen.org;jiaheng.xu@zhinanzhen.org
Canberra ：juntao@zhinanzhen.org;jiaheng.xu@zhinanzhen.org
sydeny2:kevin@zhinanzhen.org;jiaheng.xu@zhinanzhen.org
Melbourne:lisa@zhinanzhen.org;jiaheng.xu@zhinanzhen.org
Brisbane：vicky@zhinanzhen.org;jiaheng.xu@zhinanzhen.org
Adelaide:caroline.wang@zhinanzhen.org;jiaheng.xu@zhinanzhen.org
Hobart:lorrain.pan@zhinanzhen.org;jiaheng.xu@zhinanzhen.org
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

    //String startDate = "2020-09-01";
    //String lastSaturdayDate = "2020-09-19";
    //String endDate = "2020-09-25";

    //    1-25号
    @org.springframework.scheduling.annotation.Scheduled(cron = "0 0 9 ?  *  SAT")
    public void everyWeek(){


        String startDate = DateClass.thisMonthFirstDay(Calendar.getInstance());   //本月一号的时间
        String lastSaturdayDate = DateClass.lastSaturday(Calendar.getInstance()); //上周六
        String endDate = DateClass.thisMonthFriday(Calendar.getInstance());            //也就是当前时间


        //月初截止到本周五的
        List<DataDTO> areaTodayDataList = data.dataReport(startDate,endDate,"A"); //   A  全area地区的area数据   数据
        List<DataDTO> dataTOdayDTOList = data.dataReport(startDate,endDate,"R"); //  R 全area顾问倒序排名的数据  顾问

        //上周六截止到到本周五的数据
        List<DataDTO> lastWeekAreaDataList = data.dataReport(lastSaturdayDate,endDate,"A"); //   A  地区的数据

        //所有顾问按照地区分类（已经排好名次）
        List<List<DataDTO>> regionList = RegionClassification.classification(dataTOdayDTOList);//  按照地区将顾问进行分组(顾问排名是1-当前日期)

        {
            content = EmailModel.start();

            content.append("全澳截止到本周五："+startDate+"  至  "+endDate+"号 Data Report");
            content.append(EmailModel.areaModelHaveDate(areaTodayDataList));//area各地区的数据

            content.append("全澳本周数据:"+lastSaturdayDate+"  至  "+endDate+"号 Data Report");
            content.append(EmailModel.areaModelNoDate(lastWeekAreaDataList));

            content.append("全澳"+startDate+"  至  "+endDate+"号的顾问业绩排名");
            content.append(EmailModel.rankModelHave(dataTOdayDTOList)); //顾问业绩排名

            content.append(EmailModel.end());
            SendEmailUtil.send("paul@zhinanzhen.org","全澳的 Data Report",content.toString());
            SendEmailUtil.send("elvin@zhinanzhen.org","全澳的 Data Report",content.toString());
            SendEmailUtil.send("jiaheng.xu@zhinanzhen.org","全澳的 Data Report",content.toString());
        }

        {   //发送给每个Manager的总数据
            for(DataDTO area : areaTodayDataList){
                content = EmailModel.start();

                content.append(area.getArea()+"截止到本周五:"+startDate+"  至  "+endDate+"号  Data Report");
                content.append(EmailModel.areaModelHaveDate(area));         //一号到本周五的 Manager 地区的数据

                for(DataDTO lastWeekArea : lastWeekAreaDataList){
                    if(area.getArea() .equals(lastWeekArea.getArea()) ) {
                        content.append(area.getArea() + " 本周数据:" + lastSaturdayDate + "  至  " + endDate + "号 Data Report");
                        content.append(EmailModel.areaModelNoDate(lastWeekArea));   // 19-25号Manager该地区的总数据
                    }
                }

                for(List<DataDTO> regionDataList:regionList){
                    if(area.getArea() .equals(regionDataList.get(0).getArea()) ){
                        content.append(area.getArea()+" 截止到本周五:"+startDate+"  至  "+endDate+"的顾问业绩排名");
                        content.append(EmailModel.rankModelHave(regionDataList)); //一号到本周五Manager地区的排名
                    }
                }

                content.append("全澳截止到本周五:"+startDate+"  至  "+endDate+"号的顾问业绩排名");
                content.append(EmailModel.rankModelNo(dataTOdayDTOList)); // 全澳顾问业绩排名(No money)

                content.append(EmailModel.end());

                if (area.getArea() .equals("Sydney")){
                    SendEmailUtil.send("juntao@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                    SendEmailUtil.send("jiaheng.xu@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                }
                if (area.getArea() .equals("Canberra")){
                    SendEmailUtil.send("juntao@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                    SendEmailUtil.send("jiaheng.xu@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                }
                if (area.getArea() .equals("Sydney2")){
                    SendEmailUtil.send("kevin@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                    SendEmailUtil.send("jiaheng.xu@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                }
                if (area.getArea() .equals("Melbourne")){
                    SendEmailUtil.send("lisa@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                    SendEmailUtil.send("jiaheng.xu@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                }
                if (area.getArea() .equals("Brisbane")){
                    SendEmailUtil.send("vicky@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                    SendEmailUtil.send("jiaheng.xu@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                }
                if (area.getArea() .equals("Adelaide")){
                    SendEmailUtil.send("caroline.wang@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                    SendEmailUtil.send("jiaheng.xu@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                }
                if (area.getArea() .equals("Hobart")){
                    SendEmailUtil.send("lorrain.pan@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                    SendEmailUtil.send("jiaheng.xu@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                }
            }
        }


    }

    //上月一号到上月末(每月触发)
    @org.springframework.scheduling.annotation.Scheduled(cron = "0 0 9  1 * ?  ")
    public void everyMonth(){

        String startDate = DateClass.lastMonthFirstDay(Calendar.getInstance());   //就是上个月一号的时间
        String endDate = DateClass.lastMonthLastDay(Calendar.getInstance());     //上个月最后一天

        //下面的数据是上月初到上月末的
        List<DataDTO> areaTodayDataList = data.dataReport(startDate,endDate,"A"); //   A  全area地区的area数据   数据
        List<DataDTO> dataTOdayDTOList = data.dataReport(startDate,endDate,"R"); //  R 全area顾问倒序排名的数据  顾问
        List<List<DataDTO>> regionList = RegionClassification.classification(dataTOdayDTOList);//  按照地区将顾问进行分组

        {
            System.out.println(content+"content");
            content = EmailModel.start();

            content.append("上月全澳的 Data Report，截止日期:"+startDate+"  至  "+endDate);
            content.append(EmailModel.areaModelHaveDate(areaTodayDataList));//area各地区的数据

            content.append("上月全澳的顾问业绩排名，截止日期:"+startDate+"  至  "+endDate);
            content.append(EmailModel.rankModelHave(dataTOdayDTOList)); //顾问业绩排名

            content.append(EmailModel.end());
            SendEmailUtil.send("paul@zhinanzhen.org","全澳的 Data Report",content.toString());
            SendEmailUtil.send("elvin@zhinanzhen.org","全澳的 Data Report",content.toString());
            SendEmailUtil.send("jiaheng.xu@zhinanzhen.org","全澳的 Data Report",content.toString());
        }
        {   //发送给每个Manager的总数据
            for(DataDTO area : areaTodayDataList){
                content = EmailModel.start();

                content.append(area.getArea()+"上个月的  Data Report，截止日期:"+startDate+"  至  "+endDate);
                content.append(EmailModel.areaModelHaveDate(area));         //一号到本周五的 Manager 地区的数据

                for(List<DataDTO> regionDataList:regionList){
                    if(area.getArea() .equals(regionDataList.get(0).getArea()) ){
                        content.append(area.getArea()+"的顾问业绩排名");
                        content.append(EmailModel.rankModelHave(regionDataList)); //一号到本周五Manager地区的排名
                    }
                }

                content.append("上月全澳顾问业绩排名，截止日期："+startDate+"  至  "+endDate);
                content.append(EmailModel.rankModelNo(dataTOdayDTOList)); // 全澳顾问业绩排名(No money)

                content.append(EmailModel.end());
                if (area.getArea() .equals("Sydney")){
                    SendEmailUtil.send("juntao@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                    SendEmailUtil.send("jiaheng.xu@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                }
                if (area.getArea() .equals("Canberra")){
                    SendEmailUtil.send("juntao@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                    SendEmailUtil.send("jiaheng.xu@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                }
                if (area.getArea() .equals("Sydney2")){
                    SendEmailUtil.send("kevin@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                    SendEmailUtil.send("jiaheng.xu@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                }
                if (area.getArea() .equals("Melbourne")){
                    SendEmailUtil.send("lisa@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                    SendEmailUtil.send("jiaheng.xu@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                }
                if (area.getArea() .equals("Brisbane")){
                    SendEmailUtil.send("vicky@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                    SendEmailUtil.send("jiaheng.xu@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                }
                if (area.getArea() .equals("Adelaide")){
                    SendEmailUtil.send("caroline.wang@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                    SendEmailUtil.send("jiaheng.xu@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                }
                if (area.getArea() .equals("Hobart")){
                    SendEmailUtil.send("lorrain.pan@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                    SendEmailUtil.send("jiaheng.xu@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                }

            }
        }


    }

}
