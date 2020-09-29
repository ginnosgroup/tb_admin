package org.zhinanzhen.tb.scheduled;

import org.zhinanzhen.b.service.pojo.DataDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: bsz
 * Date: 2020/09/25 9:46
 * Description:
 * Version: V1.0
 */
public class EmailModel {




    //这里是areaModel
    public  static StringBuilder areaModelHaveDate(List<DataDTO> areaDataList){
        StringBuilder content = new StringBuilder();
        content.append("<table border=\"5\" style=\"border:solid 1px #E8F2F9;font-size=14px;;font-size:18px;\">");
        content.append("<tr style=\"background-color: #428BCA; color:#ffffff\">" +
                "<th>Date</th><th>Area</th><th>deductionCommission</th><th>claimCommission</th><th>claimedCommission</th><th>serviceFee</th><th>total</th>" +
                "</tr>");
        for (DataDTO data : areaDataList) {
            content.append("<tr>");
            content.append("<td>" + data.getDate() + "</td>");//第一列
            content.append("<td>" + data.getArea() + "</td>"); //第二列
            content.append("<td>" + data.getDeductionCommission() + "</td>");
            content.append("<td>" + data.getClaimCommission() + "</td>");
            content.append("<td>" + data.getClaimedCommission() + "</td>");
            content.append("<td>" + data.getServiceFee() + "</td>");
            content.append("<td>" + data.getTotal() + "</td>");
            content.append("</tr>");
        }
        content.append("<tr>");
        content.append("<td></td>");
        content.append("<td>total</td>");
        content.append(sum(areaDataList));
        content.append("</tr>");
        content.append("</table>");
        return content;
    }

    //这里是weekModel
    public static StringBuilder areaModelNoDate(List<DataDTO> areaDataList){
         StringBuilder content = new StringBuilder();
        content.append("<table border=\"5\" style=\"border:solid 1px #E8F2F9;font-size=14px;;font-size:18px;\">");
        content.append("<tr style=\"background-color: #428BCA; color:#ffffff\">" +
                "<th>Area</th><th>deductionCommission</th><th>claimCommission</th><th>claimedCommission</th><th>serviceFee</th><th>total</th>" +
                "</tr>");
        for (DataDTO data : areaDataList) {
            content.append("<tr>");
            content.append("<td>" + data.getArea() + "</td>"); //第二列
            content.append("<td>" + data.getDeductionCommission() + "</td>");
            content.append("<td>" + data.getClaimCommission() + "</td>");
            content.append("<td>" + data.getClaimedCommission() + "</td>");
            content.append("<td>" + data.getServiceFee() + "</td>");
            content.append("<td>" + data.getTotal() + "</td>");
            content.append("</tr>");
        }
        content.append("<tr>");
        content.append("<td>total</td>");
        content.append(sum(areaDataList));
        content.append("</tr>");
        content.append("</table>");
        return content;
    }

    //全area地区的RankModel   有金额     dataDTOList
    public  static StringBuilder rankModelHave(List<DataDTO> areaRankModel){
        StringBuilder content = new StringBuilder();
        content.append("<table border=\"5\" style=\"border:solid 1px #E8F2F9;font-size=14px;;font-size:18px;\">");
        content.append("<tr style=\"background-color: #428BCA; color:#ffffff\">" +
                "<th>Rank</th><th>Date</th><th>Area</th><th>Consultant</th><th>deductionCommission</th><th>claimCommission</th><th>claimedCommission</th><th>serviceFee</th><th>total</th>" +
                "</tr>");
        for (int index = 0 ;index <areaRankModel.size() ; index++) {
            content.append("<tr>");
            content.append("<td>" + (index+1) + "</td>");
            content.append("<td>" + areaRankModel.get(index).getDate() + "</td>");
            content.append("<td>" + areaRankModel.get(index).getArea() + "</td>"); //第二列
            content.append("<td>" + areaRankModel.get(index).getConsultant() + "</td>");
            content.append("<td>" + areaRankModel.get(index).getDeductionCommission() + "</td>");
            content.append("<td>" + areaRankModel.get(index).getClaimCommission() + "</td>");
            content.append("<td>" + areaRankModel.get(index).getClaimedCommission() + "</td>");
            content.append("<td>" + areaRankModel.get(index).getServiceFee() + "</td>");
            content.append("<td>" + areaRankModel.get(index).getTotal() + "</td>");
            content.append("</tr>");
        }
        content.append("</table>");
        return content;
    }


    //这里是regionRankModel     dataDTOList
    public  static StringBuilder rankModelNo(List<DataDTO> areaRankModel){
        StringBuilder content = new StringBuilder();
        content.append("<table border=\"5\" style=\"border:solid 1px #E8F2F9;font-size=14px;;font-size:18px;\">");
        content.append("<tr style=\"background-color: #428BCA; color:#ffffff\">" +
                "<th>Rank</th><th>Date</th><th>Area</th><th>Consultant</th>" +
                "</tr>");
        for (int index = 0 ;index <areaRankModel.size() ; index++) {
            content.append("<tr>");
            content.append("<td>" + (index+1) + "</td>");
            content.append("<td>" + areaRankModel.get(index).getDate() + "</td>");
            content.append("<td>" + areaRankModel.get(index).getArea() + "</td>"); //第二列
            content.append("<td>" + areaRankModel.get(index).getConsultant() + "</td>");
            content.append("</tr>");
        }
        content.append("</table>");
        return content;
    }




    public  static StringBuilder start (){
        StringBuilder content = new StringBuilder("<html><head></head><body>");
        return content;
    }

    public  static StringBuilder end (){
        StringBuilder content = new StringBuilder("</body></html>");
        return content;
    }


    //计算serviceFee,ClaimCommission,ClaimedCommission,DeductionCommission 的total
    public  static StringBuilder sum(List<DataDTO> dataDTOList){
        StringBuilder content = new StringBuilder();
        List<Double> list = new ArrayList<>();
        double deductionCommission = 0;
        double claimCommission = 0;
        double claimedCommission = 0;
        double serviceFee = 0;
        for(DataDTO dataDTO:dataDTOList){
            serviceFee = serviceFee + dataDTO.getServiceFee();
            claimCommission = claimCommission + dataDTO.getClaimCommission();
            claimedCommission  = claimedCommission + dataDTO.getClaimedCommission();
            deductionCommission = deductionCommission+ dataDTO.getDeductionCommission();
        }
        content.append("<td>" + deductionCommission + "</td>");
        content.append("<td>" + claimCommission + "</td>");
        content.append("<td>" + claimedCommission + "</td>");
        content.append("<td>" + serviceFee + "</td>");
        content.append("<td>" + (deductionCommission+ claimCommission + claimedCommission + serviceFee) +"</td>");
        return content;
    }

}
