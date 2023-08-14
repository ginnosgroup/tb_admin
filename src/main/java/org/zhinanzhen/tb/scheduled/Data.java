package org.zhinanzhen.tb.scheduled;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zhinanzhen.b.service.CommissionOrderService;
import org.zhinanzhen.b.service.RefundService;
import org.zhinanzhen.b.service.VisaService;
import org.zhinanzhen.b.service.impl.CommissionOrderServiceImpl;
import org.zhinanzhen.b.service.impl.VisaServiceImpl;
import org.zhinanzhen.b.service.pojo.CommissionOrderReportDTO;
import org.zhinanzhen.b.service.pojo.DataDTO;
import org.zhinanzhen.b.service.pojo.RefoundReportDTO;
import org.zhinanzhen.b.service.pojo.VisaReportDTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;
import org.zhinanzhen.tb.utils.SendEmailUtil;

import com.ikasoa.core.utils.ObjectUtil;
import com.ikasoa.core.utils.StringUtil;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: bsz
 * Date: 2020/09/27 17:02
 * Description:
 * Version: V1.0
 */
@Component
public class Data extends BaseService {

    @Autowired
    private  CommissionOrderService commissionOrderService ;

    @Autowired
    private  VisaService visaService ;
    
    @Autowired
    private RefundService refundService;

    public  List<DataDTO> dataReport(String startDate,String endDate,String opt,String dateMethod){
        try {
            List<DataDTO> dataDTOList = new ArrayList<DataDTO>();//数据
            List<DataDTO> areaDataList = new ArrayList<DataDTO>();//area的数据
            List<DataDTO> _dataDTOList = new ArrayList<DataDTO>();
            //List<DataDTO> areaRankDataList = new ArrayList<DataDTO>();//area排名的数据

            List<CommissionOrderReportDTO> commissionOrderReportDtoList= commissionOrderService.listCommissionOrderReport(startDate,endDate,"A",dateMethod == null?"M":"Y",0,0,null);
            List<VisaReportDTO> VisaReportList = visaService.listVisaReport(startDate,endDate,"A", dateMethod == null?"M":"Y",0,0,null);
            List<RefoundReportDTO> refundReportList = refundService.listRefundReport2(startDate, endDate, "A", dateMethod == null?"M":"Y", 0, 0, null);

            if (commissionOrderReportDtoList != null) {
                commissionOrderReportDtoList.forEach(commissionOrderReportDto -> _dataDTOList
                        .add(mapper.map(commissionOrderReportDto, DataDTO.class)));

            }

            //赋值ServiceFee
            _dataDTOList.forEach(result -> {
                VisaReportList.forEach(visaReport ->{
					if (result.getDate() != null && result.getDate().equals(visaReport.getDate())
							&& result.getAdviserId() == visaReport.getAdviserId()) {
						result.setServiceFee(visaReport.getServiceFee() + result.getServiceFee());
					}
                });
            });

            //将全部数据汇聚起来,VisaReportList和commissionOrderReportDtoList里面汇聚在 dataDTOList
            VisaReportList.forEach(visaReport ->{
                boolean flag = false;
                for(int index = 0 ;index < commissionOrderReportDtoList.size() ; index ++){
                    if (visaReport.getDate().equals(commissionOrderReportDtoList.get(index).getDate()) && visaReport.getAdviserId() == commissionOrderReportDtoList.get(index).getAdviserId()){
                        flag = true;
                    }
                }
                if(flag == false){
					DataDTO dto = new DataDTO(visaReport.getDate(), visaReport.getRegionId(), visaReport.getArea(),
							visaReport.getAdviserId(), visaReport.getConsultant(), visaReport.getServiceFee(),
							getRefunded(refundReportList, null, visaReport));
					_dataDTOList.add(dto);
                }
            });

            HashSet<Integer> adviserIdSet = new HashSet();
            HashSet areaset = new HashSet();
            _dataDTOList.forEach(dataDTO -> {
                areaset.add(dataDTO.getArea());
                adviserIdSet.add(dataDTO.getAdviserId());
            });

            // _dataDTOList 数据中可能有相同顾问 但是date不同的。 合并
            adviserIdSet.forEach(adviserId ->{
                double serviceFee = 0;
                double deductionCommission = 0;
                double claimCommission = 0;
                double claimedCommission = 0;
                double adjustments = 0;
                int regionId = 0;
                String area = "";
                String consultant = "";
                for (DataDTO dataDTO : _dataDTOList){
                    if (adviserId == dataDTO.getAdviserId()){
                        serviceFee = serviceFee + dataDTO.getServiceFee();
                        deductionCommission = deductionCommission + dataDTO.getDeductionCommission();
                        claimCommission = claimCommission + dataDTO.getClaimCommission();
                        claimedCommission = claimedCommission + dataDTO.getClaimedCommission();
                        adjustments = adjustments + dataDTO.getAdjustments();
                        regionId = dataDTO.getRegionId();
                        area = dataDTO.getArea();
                        consultant = dataDTO.getConsultant();
                    }
                }
                dataDTOList.add(new DataDTO(null,regionId,area,adviserId,consultant,serviceFee,deductionCommission,claimCommission,claimedCommission,adjustments));
            });




            //每一个area的数据
            areaset.forEach(area -> {
                String date = "";
                double serviceFee = 0;
                double deductionCommission = 0;
                double claimCommission = 0;
                double claimedCommission = 0;
                double adjustments = 0;
                int regionId = 0;
                double refunded = 0;
                for( int index = 0 ;index < dataDTOList.size(); index++){
                    if(dataDTOList.get(index).getArea().equals(area)){
                        serviceFee = serviceFee +dataDTOList.get(index).getServiceFee();
                        deductionCommission = deductionCommission +dataDTOList.get(index).getDeductionCommission();
                        claimCommission = claimCommission +dataDTOList.get(index).getClaimCommission();
                        claimedCommission = claimedCommission +dataDTOList.get(index).getClaimedCommission();
                        adjustments = adjustments +dataDTOList.get(index).getAdjustments();
                        date = dataDTOList.get(index).getDate();
                        regionId = dataDTOList.get(index).getRegionId();
						refunded = getRefunded(refundReportList, dataDTOList.get(index), null);
System.out.println("DataDebug-getRefunded(refundReportList, dataDTOList.get(index), null):" + getRefunded(refundReportList, dataDTOList.get(index), null));
                    }
                }
                areaDataList.add(new DataDTO(date,regionId, (String) area,serviceFee,deductionCommission,claimCommission,claimedCommission,adjustments,refunded));
            });

            //计算dataDTOList每一行的total值
            dataDTOList.forEach(dataDTO -> {
                //dataDTO.setTotal(dataDTO.getServiceFee()+dataDTO.getClaimCommission()+dataDTO.getDeductionCommission()+dataDTO.getAdjustments());
System.out.println("DataDebug-getRefunded(refundReportList, dataDTO, null):" + getRefunded(refundReportList, dataDTO, null));
                dataDTO.setTotal(dataDTO.getServiceFee()+dataDTO.getClaimCommission()+dataDTO.getDeductionCommission()-getRefunded(refundReportList, dataDTO, null)); // 临时去掉adjustments＆减去refunded
            });

            //开始计算全地区的顾问total排名    ---->dataDTOList
            Collections.sort(dataDTOList, new Comparator<DataDTO>() {
                @Override
                public int compare(DataDTO o1, DataDTO o2) {
                    if(o1.getTotal() > o2.getTotal())
                        return -1;
                    else if(o1.getTotal() < o2.getTotal())
                        return 1;
                    else
                        return 0;


                }
            });

            //计算areaDataList每一行的total值
            areaDataList.forEach(area->{
                area.setServiceFee(new BigDecimal(area.getServiceFee()).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
                area.setDeductionCommission(new  BigDecimal(area.getDeductionCommission()).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
                area.setClaimCommission(new BigDecimal(area.getClaimCommission()).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
                area.setClaimedCommission(new BigDecimal(area.getClaimedCommission()).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
                area.setAdjustments(new BigDecimal(area.getAdjustments()).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
//                area.setTotal(new BigDecimal(area.getServiceFee()+area.getClaimCommission()+area.getDeductionCommission()+area.getAdjustments())
//                        .setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
System.out.println("DataDebug-getRefunded(refundReportList, area, null):" + getRefunded(refundReportList, area, null));
				area.setTotal(
						new BigDecimal(area.getServiceFee() + area.getClaimCommission() + area.getDeductionCommission() - getRefunded(refundReportList, area, null))
								.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()); // 临时去掉adjustments＆减去refunded
System.out.println("DataDebug-area.getTotal():" + area.getTotal());
            });




            //输出查看
            //VisaReportList.forEach(aa -> System.out.println(aa.toString()));
            //commissionOrderReportDtoList.forEach(aa -> System.out.println(aa.toString()));

            if(opt.equals("A")){
                return areaDataList;
            }
            if(opt.equals("R")){
                return dataDTOList;
            }

        } catch (ServiceException e) {
            e.printStackTrace();
        }

        return  null;
    }
    
	private Double getRefunded(List<RefoundReportDTO> refundReportList, DataDTO data, VisaReportDTO visaReport) {
		for (RefoundReportDTO refundReport : refundReportList) {
			if (!ObjectUtil.orIsNull(data, refundReport) && StringUtil.equals(refundReport.getDate(), data.getDate())
					&& StringUtil.equals(refundReport.getRegionId() + "", data.getRegionId() + "")
					&& StringUtil.equals(refundReport.getAdviserId() + "", data.getAdviserId() + ""))
				return refundReport.getRefunded();
			if (!ObjectUtil.orIsNull(visaReport, refundReport)
					&& StringUtil.equals(refundReport.getDate(), visaReport.getDate())
					&& StringUtil.equals(refundReport.getRegionId() + "", visaReport.getRegionId() + "")
					&& StringUtil.equals(refundReport.getAdviserId() + "", visaReport.getAdviserId() + ""))
				return refundReport.getRefunded();
		}
		return 0.00;
	}

}
