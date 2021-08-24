package org.zhinanzhen.tb.scheduled;

import org.zhinanzhen.b.service.pojo.DataDTO;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.tb.utils.CommonUtils;

import java.text.DecimalFormat;
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


    private static DecimalFormat df = new DecimalFormat("#,##0.00");


    //这里是areaModel
    public  static StringBuilder areaModelHaveDate(List<DataDTO> areaDataList){
        StringBuilder content = new StringBuilder();
        content.append("<table border=\"5\" style=\"border:solid 1px #E8F2F9;font-size=14px;;font-size:18px;\">");
        content.append("<tr style=\"background-color: #428BCA; color:#ffffff\">" +
                "<th>Date</th><th>Area</th><th>serviceFee</th><th>deductionCommission</th><th>claimCommission</th><th>claimedCommission</th><th  style=\"background-color: #FFB800; color:#ffffff\">total</th>" +
                "</tr>");
        for (DataDTO data : areaDataList) {
            content.append("<tr>");
            content.append("<td>" + data.getDate() + "</td>");//第一列
            content.append("<td>" + data.getArea() + "</td>"); //第二列
            content.append("<td>" + df.format(data.getServiceFee()) + "</td>");
            content.append("<td>" + df.format(data.getDeductionCommission()) + "</td>");
            content.append("<td>" + df.format(data.getClaimCommission()) + "</td>");
            content.append("<td>" + df.format(data.getClaimedCommission()) + "</td>");
            content.append("<td  style=\"background-color: #FDF3D7; color:#000000\">" + df.format(data.getTotal()) + "</td>");
            content.append("</tr>");
        }
        content.append("<tr>");
        content.append("<td></td>");
        content.append("<td  style=\"background-color: #FFB800; color:#ffffff\">total</td>");
        content.append(sum(areaDataList));
        content.append("</tr>");
        content.append("</table>");
        return content;
    }
    public  static StringBuilder areaModelHaveDate(DataDTO data){
        StringBuilder content = new StringBuilder();
        content.append("<table border=\"5\" style=\"border:solid 1px #E8F2F9;font-size=14px;;font-size:18px;\">");
        content.append("<tr style=\"background-color: #428BCA; color:#ffffff\">" +
                "<th>Date</th><th>Area</th><th>serviceFee</th><th>deductionCommission</th><th>claimCommission</th><th>claimedCommission</th><th  style=\"background-color: #FFB800; color:#ffffff\">total</th>" +
                "</tr>");

        content.append("<tr>");
        content.append("<td>" + data.getDate() + "</td>");//第一列
        content.append("<td>" + data.getArea() + "</td>"); //第二列
        content.append("<td>" + df.format(data.getServiceFee()) + "</td>");
        content.append("<td>" + df.format(data.getDeductionCommission()) + "</td>");
        content.append("<td>" + df.format(data.getClaimCommission()) + "</td>");
        content.append("<td>" + df.format(data.getClaimedCommission()) + "</td>");
        content.append("<td  style=\"background-color: #FDF3D7; color:#000000\">" + df.format(data.getTotal()) + "</td>");
        content.append("</tr>");
        content.append("</table>");
        return content;
    }

    //这里是weekModel
    public static StringBuilder areaModelNoDate(List<DataDTO> areaDataList){
         StringBuilder content = new StringBuilder();
        content.append("<table border=\"5\" style=\"border:solid 1px #E8F2F9;font-size=14px;;font-size:18px;\">");
        content.append("<tr style=\"background-color: #428BCA; color:#ffffff\">" +
                "<th>Area</th><th>serviceFee</th><th>deductionCommission</th><th>claimCommission</th><th>claimedCommission</th><th  style=\"background-color: #FFB800; color:#ffffff\">total</th>" +
                "</tr>");
        for (DataDTO data : areaDataList) {
            content.append("<tr>");
            content.append("<td>" + data.getArea() + "</td>"); //第二列
            content.append("<td>" + df.format(data.getServiceFee()) + "</td>");
            content.append("<td>" + df.format(data.getDeductionCommission()) + "</td>");
            content.append("<td>" + df.format(data.getClaimCommission()) + "</td>");
            content.append("<td>" + df.format(data.getClaimedCommission()) + "</td>");
            content.append("<td  style=\"background-color: #FDF3D7; color:#000000\">" + df.format(data.getTotal()) + "</td>");
            content.append("</tr>");
        }
        content.append("<tr>");
        content.append("<td  style=\"background-color: #FFB800; color:#ffffff\">total</td>");
        content.append(sum(areaDataList));
        content.append("</tr>");
        content.append("</table>");
        return content;
    }

    public static StringBuilder areaModelNoDate(DataDTO data){
        StringBuilder content = new StringBuilder();
        content.append("<table border=\"5\" style=\"border:solid 1px #E8F2F9;font-size=14px;;font-size:18px;\">");
        content.append("<tr style=\"background-color: #428BCA; color:#ffffff\">" +
                "<th>Area</th><th>serviceFee</th><th>deductionCommission</th><th>claimCommission</th><th>claimedCommission</th><th  style=\"background-color: #FFB800; color:#ffffff\">total</th>" +
                "</tr>");

        content.append("<tr>");
        content.append("<td>" + data.getArea() + "</td>"); //第二列
        content.append("<td>" + df.format(data.getServiceFee()) + "</td>");
        content.append("<td>" + df.format(data.getDeductionCommission()) + "</td>");
        content.append("<td>" + df.format(data.getClaimCommission()) + "</td>");
        content.append("<td>" + df.format(data.getClaimedCommission()) + "</td>");
        content.append("<td  style=\"background-color: #FDF3D7; color:#000000\">" + df.format(data.getTotal()) + "</td>");
        content.append("</tr>");
        content.append("</table>");
        return content;
    }


    //全area地区的RankModel   有金额     dataDTOList
    public  static StringBuilder rankModelHave(List<DataDTO> areaRankModel){
        StringBuilder content = new StringBuilder();
        content.append("<table border=\"5\" style=\"border:solid 1px #E8F2F9;font-size=14px;;font-size:18px;\">");
        content.append("<tr style=\"background-color: #428BCA; color:#ffffff\">" +
                "<th>Rank</th><th>Date</th><th>Area</th><th>Consultant</th><th>serviceFee</th><th>deductionCommission</th><th>claimCommission</th><th>claimedCommission</th><th  style=\"background-color: #FFB800; color:#ffffff\">total</th>" +
                "</tr>");
        for (int index = 0 ;index <areaRankModel.size() ; index++) {
            content.append("<tr>");
            content.append("<td>" + (index+1) + "</td>");
            content.append("<td>" + areaRankModel.get(index).getDate() + "</td>");
            content.append("<td>" + areaRankModel.get(index).getArea() + "</td>"); //第二列
            content.append("<td>" + areaRankModel.get(index).getConsultant() + "</td>");
            content.append("<td>" + df.format(areaRankModel.get(index).getServiceFee()) + "</td>");
            content.append("<td>" + df.format(areaRankModel.get(index).getDeductionCommission()) + "</td>");
            content.append("<td>" + df.format(areaRankModel.get(index).getClaimCommission()) + "</td>");
            content.append("<td>" + df.format(areaRankModel.get(index).getClaimedCommission()) + "</td>");
            content.append("<td  style=\"background-color: #FDF3D7; color:#000000\">" + df.format(areaRankModel.get(index).getTotal()) + "</td>");
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
        content.append("<td  style=\"background-color: #FDF3D7; color:#000000\">" + df.format(serviceFee) + "</td>");
        content.append("<td  style=\"background-color: #FDF3D7; color:#000000\">" + df.format(deductionCommission) + "</td>");
        content.append("<td  style=\"background-color: #FDF3D7; color:#000000\">" + df.format(claimCommission) + "</td>");
        content.append("<td  style=\"background-color: #FDF3D7; color:#000000\">" + df.format(claimedCommission) + "</td>");
        content.append("<td  style=\"background-color: #FDF3D7; color:#000000\">" + df.format(deductionCommission+ claimCommission + claimedCommission + serviceFee) +"</td>");
        return content;
    }

    /**
     * 导出服务订单中未支付的服务类型的信息
     * @param list
     * @return content
     */
    public  static StringBuilder officialApprovalServicecAndIsPayModule(List<ServiceOrderDTO> list){
        StringBuilder content = new StringBuilder();
        content.append("<table border='1' style='border:solid 1px #E8F2F9;font-size=14px;;font-size:18px;'>");
        content.append("<tr style='background-color: #428BCA; color:#ffffff'><th>提交审核时间</th><th>服务订单ID</th><th>服务项目</th><th>顾问名称</th><th>顾问地区</th><th>文案</th><th>MARA</th></tr>");
        for (ServiceOrderDTO so : list){
            String str = "";
            if ("VISA".equalsIgnoreCase(so.getType())){
                if (so.getService() != null )
                    str = so.getService().getName() + so.getService().getCode();
                if (so.getServicePackage() != null)
                    str = str + "-" + CommonUtils.getTypeStrOfServicePackageDTO(so.getServicePackage().getType()) ;
                if (so.getServiceAssessDO() != null)
                    str = str + "-" + so.getServiceAssessDO().getName();
            }
            if ("ZX".equalsIgnoreCase(so.getType())){
                str = "咨询 - " + so.getService().getCode();
            }
            if ("OVST".equalsIgnoreCase(so.getType())){
                //str = "留学 - " + so.getSchool().getName();
                continue;
            }
            content.append("<tr>");
            content.append("<td>" + CommonUtils.sdf.format(so.getOfficialApprovalDate()) + "</td>");
            content.append("<td>" + so.getId() + "</td>");
            content.append("<td>" + str + "</td>");
            if (so.getAdviser() != null){
                content.append("<td>" + so.getAdviser().getName() + "</td>");
                content.append("<td>" + so.getAdviser().getRegionName() + "</td>");
            }
            if (so.getOfficial() != null)
                content.append("<td>" + so.getOfficial().getName() + "</td>");
            if (so.getMara() != null)
                content.append("<td>" + so.getMara().getName() + "</td>");

            content.append("</tr>");
        }
        content.append("</table>");
        return content;
    }


}
