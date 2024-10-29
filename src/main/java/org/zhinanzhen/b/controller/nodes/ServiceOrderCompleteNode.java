package org.zhinanzhen.b.controller.nodes;

import com.alibaba.fastjson.JSONArray;
import com.ikasoa.core.utils.ObjectUtil;
import com.ikasoa.core.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zhinanzhen.b.dao.*;
import org.zhinanzhen.b.dao.pojo.*;
import org.zhinanzhen.b.service.ServiceOrderService;
import org.zhinanzhen.b.service.pojo.CommissionAmountDTO;
import org.zhinanzhen.b.service.pojo.ServiceDTO;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.b.service.pojo.ServicePackagePriceV2DTO;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.dao.RegionDAO;
import org.zhinanzhen.tb.service.ServiceException;

import com.ikasoa.web.workflow.Context;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//申请成功
@Component
public class ServiceOrderCompleteNode extends SODecisionNode {
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
    private ServiceOrderDAO serviceOrderDAO;

    @Autowired
    private RefundDAO refundDAO;

    public static ServiceOrderCompleteNode serviceOrderCompleteNode;

    @PostConstruct
    public void init(){
        serviceOrderCompleteNode=this;
        serviceOrderCompleteNode.visaOfficialDao=this.visaOfficialDao;
        serviceOrderCompleteNode.serviceDAO=this.serviceDAO;
        serviceOrderCompleteNode.servicePackageDAO=this.servicePackageDAO;
        serviceOrderCompleteNode.officialDAO=this.officialDAO;
        serviceOrderCompleteNode.servicePackagePriceDAO=this.servicePackagePriceDAO;
        serviceOrderCompleteNode.visaDAO=this.visaDAO;
        serviceOrderCompleteNode.refundDAO=this.refundDAO;
        serviceOrderCompleteNode.serviceOrderDAO=this.serviceOrderDAO;
    }

    public ServiceOrderCompleteNode(ServiceOrderService serviceOrderService) {
        super.serviceOrderService = serviceOrderService;
    }


    @Override
    public String getName() {
        return "COMPLETE";
    }

    @Override
    protected String decide(Context context) {
        try {
            Boolean isSiv = Boolean.FALSE;
            ServiceOrderDTO serviceOrderDto = serviceOrderService.getServiceOrderById(getServiceOrderId(context));
            OfficialDO officialById = serviceOrderCompleteNode.officialDAO.getOfficialById(serviceOrderDto.getOfficialId());
            String type = serviceOrderDto.getType();
            ServicePackagePriceDO servicePackagePriceDO = serviceOrderCompleteNode.servicePackagePriceDAO.getByServiceId(serviceOrderDto.getServiceId());
            ServicePackagePriceV2DTO servicePackagePriceV2DTO = closeJugdNew(serviceOrderDto.getOfficialId(), servicePackagePriceDO);
            if (!"ZX".equals(type) && !serviceOrderDto.isSettle() && !"WA".equalsIgnoreCase(getAp(context))) { // 咨询不用判断文案权限,扣拥留学不用判断
                context.putParameter("response", new Response<ServiceOrderDTO>(1, "仅限文案操作!", null));
                return null;
            }
            if ("VISA".equals(type) && serviceOrderDto.getParentId() == 0) // 签证
            {
                ServiceDTO service = serviceOrderDto.getService();
                if (ObjectUtil.isNotNull(officialById) && 1000034 == officialById.getRegionId() && !service.getCode().contains("820") && !service.getCode().contains("309")) {
                    List<String> arrayList = serviceOrderCompleteNode.serviceDAO.listLongTimeVisa();
                    String code = service.getCode();
                    String serviceType = code.replaceAll("[^\\p{L}\\p{N}\\p{Script=Han}]+", "");
                    if (arrayList.contains(serviceType)) {
                        List<VisaOfficialListDO> list = serviceOrderCompleteNode.visaOfficialDao.list(null, null, null,
                                null, null, null,
                                null, null, null, null, null, null, null, 0,
                                10, null, null, serviceOrderDto.getId(), null);
                        VisaOfficialDO visaOfficialDO = new VisaOfficialDO();
                        VisaOfficialDO visaOfficialDO1 = new VisaOfficialDO();
                        visaOfficialDO = serviceOrderCompleteNode.visaOfficialDao.getByServiceOrderIdOne(serviceOrderDto.getId());
                        visaOfficialDO.setCommissionAmount(visaOfficialDO.getCommissionAmount() / 0.4);
                        visaOfficialDO.setPredictCommission(visaOfficialDO.getPredictCommission() / 0.4);
                        double expectAmount = visaOfficialDO.getExpectAmount();
                        for (VisaOfficialListDO visaOfficialListDO : list) {
                            visaOfficialDO.setCommissionAmount(visaOfficialDO.getCommissionAmount() - visaOfficialListDO.getCommissionAmount());
                            visaOfficialDO.setPredictCommission(visaOfficialDO.getPredictCommission() - visaOfficialListDO.getPredictCommission());
                            expectAmount = expectAmount - visaOfficialListDO.getPerAmount();
                            visaOfficialDO.setPerAmount(expectAmount);

                            visaOfficialDO1.setPerAmount(visaOfficialListDO.getPerAmount());
                            visaOfficialDO1.setId(visaOfficialListDO.getId());
                            visaOfficialDO1.setCommissionState("YJY");
                            visaOfficialDO1.setInstallmentNum(1);
                            visaOfficialDO1.setInstallment(2);
                            visaOfficialDO1.setPredictCommissionAmount(visaOfficialListDO.getPredictCommissionAmount());
                            visaOfficialDO1.setExchangeRate(visaOfficialListDO.getExchangeRate());
                            serviceOrderCompleteNode.visaOfficialDao.updateHandlingDate(visaOfficialDO.getId(),visaOfficialDO.getHandlingDate());
                            serviceOrderCompleteNode.visaOfficialDao.updateVisaOfficial(visaOfficialDO1);
                        }
                        visaOfficialDO.setPredictCommissionAmount(visaOfficialDO.getCommissionAmount());
                        visaOfficialDO.setPredictCommissionCNY(visaOfficialDO.getPredictCommission() * visaOfficialDO.getExchangeRate());
                        visaOfficialDO.setInstallmentNum(2);
                        visaOfficialDO.setInstallment(2);
                        visaOfficialDO.setKjApprovalDate(null);
                        serviceOrderCompleteNode.visaOfficialDao.addVisa(visaOfficialDO);
                    }
                    return SUSPEND_NODE;
                } else {
                    List<String> arrayList = serviceOrderCompleteNode.serviceDAO.listLongTimeVisa();
                    String code = service.getCode();
                    String serviceType = code.replaceAll("[^\\p{L}\\p{N}\\p{Script=Han}]+", "");
                    if (arrayList.contains(serviceType)) {
                        VisaOfficialDO visaOfficialDO = new VisaOfficialDO();
                        VisaOfficialDO visaOfficialDO1 = new VisaOfficialDO();
                        visaOfficialDO = serviceOrderCompleteNode.visaOfficialDao.getByServiceOrderIdOne(serviceOrderDto.getId());
                        visaOfficialDO.setInstallmentNum(2);
                        visaOfficialDO.setInstallment(2);
                        visaOfficialDO.setKjApprovalDate(null);
                        visaOfficialDO.setPerAmount(visaOfficialDO.getPerAmount() * 0.5);
                        visaOfficialDO1.setPerAmount(visaOfficialDO.getPerAmount());
                        visaOfficialDO1.setId(visaOfficialDO.getId());
                        visaOfficialDO1.setCommissionState("YJY");
                        visaOfficialDO1.setInstallmentNum(1);
                        visaOfficialDO1.setInstallment(2);
                        visaOfficialDO1.setPredictCommissionAmount(visaOfficialDO.getPredictCommissionAmount());
                        visaOfficialDO1.setExchangeRate(visaOfficialDO.getExchangeRate());
                        serviceOrderCompleteNode.visaOfficialDao.addVisa(visaOfficialDO);
                        serviceOrderCompleteNode.visaOfficialDao.updateHandlingDate(visaOfficialDO.getId(),visaOfficialDO.getHandlingDate());
                        serviceOrderCompleteNode.visaOfficialDao.updateVisaOfficial(visaOfficialDO1);
                    }
                    return SUSPEND_NODE;
                }
            }
            if (serviceOrderDto.getApplicantParentId() > 0 && (ObjectUtil.isNotNull(officialById) && 1000034 == officialById.getRegionId())) {
                boolean isBound = false;
                List<Integer> integers = new ArrayList<>();
                if (serviceOrderDto.getApplicantParentId() > 0) {
                    integers = serviceOrderCompleteNode.serviceOrderDAO.listBybindingOrder(serviceOrderDto.getApplicantParentId());
                    isBound = !integers.isEmpty();
                }
                ServiceOrderDTO serviceOrderParent = serviceOrderCompleteNode.serviceOrderService.getServiceOrderById(serviceOrderDto.getApplicantParentId());
                isSiv = "SIV".equalsIgnoreCase(serviceOrderParent.getType());
                if (isSiv) {
                    ServicePackageDO servicePackageDO = serviceOrderCompleteNode.servicePackageDAO.getById(serviceOrderDto.getServicePackageId());
                    if (ObjectUtil.isNotNull(servicePackageDO) && "VA".equalsIgnoreCase(servicePackageDO.getType())) {
                        List<VisaOfficialListDO> list = serviceOrderCompleteNode.visaOfficialDao.list(null, null, null,
                                null, null, null,
                                null, null, null, null, null, null, null, 0,
                                10, null, null, serviceOrderDto.getId(), null);
                        VisaOfficialDO visaOfficialDO = new VisaOfficialDO();
                        VisaOfficialDO visaOfficialDO1 = new VisaOfficialDO();

                        VisaDO visaByServiceOrderIdFirst = serviceOrderCompleteNode.visaDAO.getFirstVisaByServiceOrderId(serviceOrderParent.getId());
                        VisaDO visaByServiceOrderIdSecond = serviceOrderCompleteNode.visaDAO.getSecondVisaByServiceOrderId(serviceOrderParent.getId());
                        double remainingAmount = visaByServiceOrderIdFirst.getAmount();
                        if (ObjectUtil.isNotNull(visaByServiceOrderIdSecond)) {
                            remainingAmount += visaByServiceOrderIdSecond.getAmount();
                            RefundDO secondRefundByVisaId = serviceOrderCompleteNode.refundDAO.getRefundByVisaId(visaByServiceOrderIdSecond.getId());
                            if (ObjectUtil.isNotNull(secondRefundByVisaId)) {
                                remainingAmount = remainingAmount - secondRefundByVisaId.getAmount();
                            }
                            remainingAmount = remainingAmount * 0.5;
                        } else {
                            remainingAmount = remainingAmount * 0.5;
                        }
                        RefundDO firstRefundByVisaId = serviceOrderCompleteNode.refundDAO.getRefundByVisaId(visaByServiceOrderIdFirst.getId());
                        if (ObjectUtil.isNotNull(firstRefundByVisaId)) {
                            remainingAmount = remainingAmount - firstRefundByVisaId.getAmount();
                        }
                        visaOfficialDO = serviceOrderCompleteNode.visaOfficialDao.getByServiceOrderIdOne(serviceOrderDto.getId());
                        visaOfficialDO.setCommissionAmount(remainingAmount / 1.1);

                        double costPrince = 0.00;
                        for (VisaOfficialListDO visaOfficialListDO : list) {
                            if (isBound) { // 被绑定订单金额确定
                                for (Integer integer : integers) {
                                    ServicePackagePriceDO byServiceId = serviceOrderCompleteNode.servicePackagePriceDAO.getByServiceId(integer);
                                    costPrince += byServiceId.getCostPrince();
                                }
                                costPrince = costPrince * 0.5;
                                visaOfficialDO.setCommissionAmount(visaOfficialDO.getCommissionAmount() - costPrince);
                                visaOfficialDO.setPredictCommission((visaOfficialDO.getCommissionAmount()) * servicePackagePriceV2DTO.getRate() / 100);
                            }
                            visaOfficialDO.setCommissionAmount(visaOfficialDO.getCommissionAmount() - visaOfficialListDO.getCommissionAmount());
                            visaOfficialDO.setPredictCommission(visaOfficialDO.getPredictCommission() - visaOfficialListDO.getPredictCommission());
                            visaOfficialDO.setPerAmount(visaOfficialDO.getPerAmount() * 0.5);
                            visaOfficialDO1.setPerAmount(visaOfficialDO.getPerAmount());
                            visaOfficialDO1.setId(visaOfficialListDO.getId());
                            visaOfficialDO1.setCommissionState("YJY");
                            visaOfficialDO1.setInstallmentNum(1);
                            visaOfficialDO1.setInstallment(2);
                            visaOfficialDO1.setPredictCommissionAmount(visaOfficialListDO.getPredictCommissionAmount());
                            visaOfficialDO1.setExchangeRate(visaOfficialListDO.getExchangeRate());
                            serviceOrderCompleteNode.visaOfficialDao.updateHandlingDate(visaOfficialDO.getId(),visaOfficialDO.getHandlingDate());
                            serviceOrderCompleteNode.visaOfficialDao.updateVisaOfficial(visaOfficialDO1);
                        }
                        visaOfficialDO.setPredictCommissionAmount(visaOfficialDO.getCommissionAmount());
                        visaOfficialDO.setPredictCommissionCNY(visaOfficialDO.getPredictCommission() * visaOfficialDO.getExchangeRate());
                        visaOfficialDO.setInstallmentNum(2);
                        visaOfficialDO.setInstallment(2);
                        visaOfficialDO.setKjApprovalDate(null);
                        serviceOrderCompleteNode.visaOfficialDao.addVisa(visaOfficialDO);
                    }
                    return SUSPEND_NODE;
                }
            }
            if ("ZX".equals(type)) {//咨询
                isSingleStep = true;
                return SUSPEND_NODE;
            }
            isSingleStep = true;
            return "PAID";
        } catch (ServiceException e) {
            context.putParameter("response", new Response<ServiceOrderDTO>(1, "服务订单执行异常:" + e.getMessage(), null));
            return null;
        }
    }

    @Override
    public String[] nextNodeNames() {
        return new String[]{"PAID", "CLOSE", "WAIT_FD"};
    }

    // 文案地区判断
    public ServicePackagePriceV2DTO closeJugdNew(Integer officialId, ServicePackagePriceDO servicePackagePriceDO) {
        // 判断文案结算方式
        ServicePackagePriceV2DTO servicePackagePriceV2DTO = new ServicePackagePriceV2DTO();
        OfficialDO officialDO = serviceOrderCompleteNode.officialDAO.getOfficialById(officialId);
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
