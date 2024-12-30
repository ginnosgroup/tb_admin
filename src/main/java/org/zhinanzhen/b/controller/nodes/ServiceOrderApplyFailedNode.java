package org.zhinanzhen.b.controller.nodes;

import com.alibaba.fastjson.JSONArray;
import com.ikasoa.core.utils.ObjectUtil;
import com.ikasoa.core.utils.StringUtil;
import com.ikasoa.web.workflow.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zhinanzhen.b.dao.*;
import org.zhinanzhen.b.dao.pojo.*;
import org.zhinanzhen.b.service.ServiceOrderService;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.b.service.pojo.ServicePackagePriceV2DTO;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Date: 2021/08/05 下午 12:06
 * Description:
 * Version: V1.0
 */
//文案申请失败
@Component
public class ServiceOrderApplyFailedNode extends SODecisionNode{

    // 文案


    @Autowired
    private VisaOfficialDao visaOfficialDao;

    @Autowired
    private ServiceDAO serviceDAO;

    @Autowired
    private ServicePackageDAO servicePackageDAO;

    @Autowired
    private OfficialDAO officialDAO;

    @Autowired
    private VisaDAO visaDAO;

    @Autowired
    private ServicePackagePriceDAO servicePackagePriceDAO;

    @Autowired
    private RefundDAO refundDAO;

    public static ServiceOrderApplyFailedNode serviceOrderApplyFailedNode;

    @PostConstruct
    public void init(){
        serviceOrderApplyFailedNode=this;
        serviceOrderApplyFailedNode.visaOfficialDao=this.visaOfficialDao;
        serviceOrderApplyFailedNode.serviceDAO=this.serviceDAO;
        serviceOrderApplyFailedNode.servicePackageDAO=this.servicePackageDAO;
        serviceOrderApplyFailedNode.officialDAO=this.officialDAO;
        serviceOrderApplyFailedNode.servicePackagePriceDAO=this.servicePackagePriceDAO;
        serviceOrderApplyFailedNode.visaDAO=this.visaDAO;
        serviceOrderApplyFailedNode.refundDAO=this.refundDAO;
    }

    public ServiceOrderApplyFailedNode(ServiceOrderService serviceOrderService) {
        super.serviceOrderService = serviceOrderService;
    }

    @Override
    public String getName() {
        return "APPLY_FAILED";
    }

    @Override
    protected String decide(Context context) {
        try {
            Boolean isSiv = Boolean.FALSE;
            ServiceOrderDTO serviceOrderDto = serviceOrderApplyFailedNode.serviceOrderService.getServiceOrderById(getServiceOrderId(context));
            ServicePackagePriceDO servicePackagePriceDO = serviceOrderApplyFailedNode.servicePackagePriceDAO.getByServiceId(serviceOrderDto.getServiceId());
            ServicePackagePriceV2DTO servicePackagePriceV2DTO = new ServicePackagePriceV2DTO();
            if (!"OVST".equalsIgnoreCase(serviceOrderDto.getType())) {
                servicePackagePriceV2DTO = closeJugdNew(serviceOrderDto.getOfficialId(), servicePackagePriceDO);
            }
            if (!"WA".equalsIgnoreCase(getAp(context))){
                context.putParameter("response", new Response<ServiceOrderDTO>(1, "仅限文案操作!", null));
                return null;
            }
            if (serviceOrderDto.getApplicantParentId() > 0) {
                ServiceOrderDTO serviceOrderParent = serviceOrderApplyFailedNode.serviceOrderService.getServiceOrderById(serviceOrderDto.getApplicantParentId());
                isSiv = "SIV".equalsIgnoreCase(serviceOrderParent.getType());
                if (isSiv) {
                    ServicePackageDO servicePackageDO = serviceOrderApplyFailedNode.servicePackageDAO.getById(serviceOrderDto.getServicePackageId());
                    if (ObjectUtil.isNotNull(servicePackageDO) && "VA".equalsIgnoreCase(servicePackageDO.getType())) {
                        List<VisaOfficialListDO> list = serviceOrderApplyFailedNode.visaOfficialDao.list(null, null, null,
                                null, null, null,
                                null, null, null, null, null, null, null, 0,
                                10, null, null, serviceOrderDto.getId(), null);
                        VisaOfficialDO visaOfficialDO = new VisaOfficialDO();
                        VisaOfficialDO visaOfficialDO1 = new VisaOfficialDO();

                        VisaDO visaByServiceOrderIdFirst = serviceOrderApplyFailedNode.visaDAO.getFirstVisaByServiceOrderId(serviceOrderParent.getId());
                        VisaDO visaByServiceOrderIdSecond = serviceOrderApplyFailedNode.visaDAO.getSecondVisaByServiceOrderId(serviceOrderParent.getId());
                        double remainingAmount = visaByServiceOrderIdFirst.getAmount();
                        if (ObjectUtil.isNotNull(visaByServiceOrderIdSecond)) {
                            remainingAmount = visaByServiceOrderIdSecond.getAmount();
                            RefundDO SecondRefundByVisaId = serviceOrderApplyFailedNode.refundDAO.getRefundByVisaId(visaByServiceOrderIdSecond.getId());
                            remainingAmount = remainingAmount - SecondRefundByVisaId.getAmount();
                        } else {
                            remainingAmount = remainingAmount * 0.5;
                        }
                        RefundDO firstRefundByVisaId = serviceOrderApplyFailedNode.refundDAO.getRefundByVisaId(visaByServiceOrderIdFirst.getId());
                        if (ObjectUtil.isNotNull(firstRefundByVisaId)) {
                            remainingAmount = remainingAmount - firstRefundByVisaId.getAmount();
                        }
                        visaOfficialDO = serviceOrderApplyFailedNode.visaOfficialDao.getByServiceOrderIdOne(serviceOrderDto.getId());
                        visaOfficialDO.setCommissionAmount(remainingAmount / 1.1);
                        visaOfficialDO.setPredictCommission(visaOfficialDO.getCommissionAmount() * servicePackagePriceV2DTO.getRate() / 100);
                        for (VisaOfficialListDO visaOfficialListDO : list) {
                            visaOfficialDO.setCommissionAmount(visaOfficialDO.getCommissionAmount() - visaOfficialListDO.getCommissionAmount());
                            visaOfficialDO.setPredictCommission(visaOfficialDO.getPredictCommission() - visaOfficialListDO.getPredictCommission());
                            visaOfficialDO1.setPerAmount(visaOfficialListDO.getPerAmount());
                            visaOfficialDO1.setId(visaOfficialListDO.getId());
                            visaOfficialDO1.setCommissionState("YJY");
                            visaOfficialDO1.setInstallmentNum(1);
                            visaOfficialDO1.setInstallment(2);
                            visaOfficialDO1.setPredictCommissionAmount(visaOfficialListDO.getPredictCommissionAmount());
                            visaOfficialDO1.setExchangeRate(visaOfficialListDO.getExchangeRate());
                            serviceOrderApplyFailedNode.visaOfficialDao.updateHandlingDate(visaOfficialDO.getId(),visaOfficialDO.getHandlingDate());
                            serviceOrderApplyFailedNode.visaOfficialDao.updateVisaOfficial(visaOfficialDO1);
                        }
                        visaOfficialDO.setPredictCommissionAmount(visaOfficialDO.getCommissionAmount());
                        visaOfficialDO.setPredictCommissionCNY(visaOfficialDO.getPredictCommission() * visaOfficialDO.getExchangeRate());
                        visaOfficialDO.setInstallmentNum(2);
                        visaOfficialDO.setInstallment(2);
                        visaOfficialDO.setKjApprovalDate(null);
                        serviceOrderApplyFailedNode.visaOfficialDao.addVisa(visaOfficialDO);
                    }
                    return SUSPEND_NODE;
                }
            }

            return SUSPEND_NODE;
        } catch (ServiceException e) {
            context.putParameter("response", new Response<ServiceOrderDTO>(1, "服务订单执行异常:" + e.getMessage(), null));
            return null;
        }
    }
    // 文案地区判断
    public ServicePackagePriceV2DTO closeJugdNew(Integer officialId, ServicePackagePriceDO servicePackagePriceDO) {
        // 判断文案结算方式
        ServicePackagePriceV2DTO servicePackagePriceV2DTO = new ServicePackagePriceV2DTO();
        OfficialDO officialDO = serviceOrderApplyFailedNode.officialDAO.getOfficialById(officialId);
        String rulerV2 = servicePackagePriceDO.getRulerV2();
        List<ServicePackagePriceV2DTO> servicePackagePriceV2DTOS = JSONArray.parseArray(rulerV2, ServicePackagePriceV2DTO.class);
        for (ServicePackagePriceV2DTO packagePriceV2DTO : servicePackagePriceV2DTOS) {
            String officialGrades = packagePriceV2DTO.getOfficialGrades();
            if (StringUtil.isNotEmpty(officialGrades)) {
                String[] split = officialGrades.split(",");
                if (Arrays.asList(split).contains(String.valueOf(officialDO.getGradeId()))) {
                    servicePackagePriceV2DTO = packagePriceV2DTO;
                }
            }
        }
        return servicePackagePriceV2DTO;
    }

}
