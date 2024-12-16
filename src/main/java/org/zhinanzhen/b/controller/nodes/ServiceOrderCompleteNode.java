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
            // 计算绑定订单金额
            boolean isBound = false;
            double costPrince = 0.00;
            List<Integer> integers = new ArrayList<>();
            List<RefundDO> refundDOs = new ArrayList<>();
            integers = serviceOrderCompleteNode.serviceOrderDAO.listBybindingOrder(serviceOrderDto.getId());
            isBound = !integers.isEmpty();
            if (serviceOrderDto.getApplicantParentId() > 0) {
                integers = serviceOrderCompleteNode.serviceOrderDAO.listBybindingOrder(serviceOrderDto.getApplicantParentId());
                isBound = !integers.isEmpty();
            }
            if (isBound) { // 被绑定订单金额确定
                for (Integer integer : integers) {
                    ServicePackagePriceDO byServiceId = serviceOrderCompleteNode.servicePackagePriceDAO.getByServiceId(integer);
                    costPrince += byServiceId.getCostPrince();
                }
            }
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
                        List<VisaDO> visaDOS = serviceOrderCompleteNode.visaDAO.listVisaByServiceOrderId(serviceOrderDto.getId());
                        double amount = visaDOS.stream().mapToDouble(VisaDO::getAmount).sum();
//                        if (list != null && list.size() == 1) {
//                            costPrince = costPrince * 0.6;
//                            visaOfficialDO.setCommissionAmount(visaOfficialDO.getCommissionAmount() / 0.4);
//                            visaOfficialDO.setPredictCommission(visaOfficialDO.getPredictCommission() / 0.4);
//                        }
//                        if (list != null && list.size() == 2) {
//                            costPrince = costPrince * 0.4;
//                            visaOfficialDO.setCommissionAmount(visaOfficialDO.getCommissionAmount() / 0.2);
//                            visaOfficialDO.setPredictCommission(visaOfficialDO.getPredictCommission() / 0.2);
//                        }
                        visaOfficialDO.setPerAmount(amount);
                        visaOfficialDO.setCommissionAmount(amount / 1.1);
                        visaOfficialDO.setPredictCommission(visaOfficialDO.getCommissionAmount() * servicePackagePriceV2DTO.getRate() / 100);
                        if (isBound) { // 被绑定订单金额确定
                            visaOfficialDO.setCommissionAmount(visaOfficialDO.getCommissionAmount() - costPrince);
                            visaOfficialDO.setPredictCommission((visaOfficialDO.getCommissionAmount()) * servicePackagePriceV2DTO.getRate() / 100);
                        }
//                        double expectAmount = visaOfficialDO.getExpectAmount();
                        for (VisaOfficialListDO visaOfficialListDO : list) {
                            visaOfficialDO.setPerAmount(visaOfficialDO.getPerAmount() - visaOfficialListDO.getPerAmount());
                            visaOfficialDO.setCommissionAmount(visaOfficialDO.getCommissionAmount() - visaOfficialListDO.getCommissionAmount());
                            visaOfficialDO.setPredictCommission(visaOfficialDO.getCommissionAmount() * servicePackagePriceV2DTO.getRate() / 100);
//                            expectAmount = expectAmount - visaOfficialListDO.getPerAmount();
//                            visaOfficialDO.setPerAmount(expectAmount);

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
                        visaOfficialDO.setPerAmount(visaOfficialDO.getPerAmount());
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

                        double remainingAmount = 0.00;
                        List<VisaDO> visaDOS = serviceOrderCompleteNode.visaDAO.listVisaByServiceOrderId(serviceOrderParent.getId());
                        ServicePackagePriceDO packagePriceDAOByServiceId = serviceOrderCompleteNode.servicePackagePriceDAO.getByServiceId(serviceOrderParent.getServiceId());
                        // 计算extra金额
                        double sumAmount = visaDOS.stream().mapToDouble(VisaDO::getAmount).sum();
                        double extraAmount = 0.00;
                        for (VisaDO aDo : visaDOS) {
                            RefundDO refundByVisaId = serviceOrderCompleteNode.refundDAO.getRefundByVisaId(aDo.getId());
                            if (ObjectUtil.isNotNull(refundByVisaId)) {
                                refundDOs.add(refundByVisaId);
                            }
                        }
                        if (ObjectUtil.isNotNull(packagePriceDAOByServiceId) && packagePriceDAOByServiceId.getMaxPrice() < (sumAmount - refundDOs.stream().mapToDouble(RefundDO::getAmount).sum())) {
                            extraAmount = sumAmount - refundDOs.stream().mapToDouble(RefundDO::getAmount).sum() - packagePriceDAOByServiceId.getMaxPrice();
                        }
                        if (extraAmount > 0) {
                            remainingAmount = sumAmount - refundDOs.stream().mapToDouble(RefundDO::getAmount).sum() - extraAmount;
                            remainingAmount = remainingAmount - costPrince;
                            extraAmount = extraAmount * 0.5;
                        } else {
                            refundDOs = new ArrayList<>();
                            if ((list != null) && (visaDOS != null && visaDOS.size() > 1)) {
                                VisaDO visaDO = visaDOS.get(0);
                                double visaNumOne = visaDOS.get(0).getAmount();
                                visaDOS.remove(0);
                                double visaNumTwo = visaDOS.stream().mapToDouble(VisaDO::getAmount).sum();
                                double predictCommissionAmount = list.get(0).getPredictCommissionAmount();
                                remainingAmount = findClosest(predictCommissionAmount, visaNumOne, visaNumTwo);
                                if (visaDO.getAmount() == remainingAmount){
                                    RefundDO refundByVisaId = serviceOrderCompleteNode.refundDAO.getRefundByVisaId(visaDO.getId());
                                    if (ObjectUtil.isNotNull(refundByVisaId)) {
                                        refundDOs.add(refundByVisaId);
                                    }
                                    if (!refundDOs.isEmpty()) {
                                        remainingAmount = remainingAmount - refundDOs.stream().mapToDouble(RefundDO::getAmount).sum();
                                    }
                                } else {
                                    for (VisaDO aDo : visaDOS) {
                                        RefundDO refundByVisaId = serviceOrderCompleteNode.refundDAO.getRefundByVisaId(aDo.getId());
                                        if (ObjectUtil.isNotNull(refundByVisaId)) {
                                            refundDOs.add(refundByVisaId);
                                        }
                                    }
                                    if (!refundDOs.isEmpty()) {
                                        remainingAmount = remainingAmount - refundDOs.stream().mapToDouble(RefundDO::getAmount).sum();
                                    }
                                }
                                costPrince = costPrince * (remainingAmount) / (visaNumOne + visaNumTwo);
                                remainingAmount = remainingAmount - costPrince;
                            } else {
                                remainingAmount = visaDOS.stream().mapToDouble(VisaDO::getAmount).sum();
                                remainingAmount = remainingAmount - costPrince;
                                RefundDO refundByVisaId = serviceOrderCompleteNode.refundDAO.getRefundByVisaId(visaDOS.get(0).getId());
                                if (ObjectUtil.isNotNull(refundByVisaId)) {
                                    refundDOs.add(refundByVisaId);
                                }
                                if (!refundDOs.isEmpty()) {
                                    remainingAmount = remainingAmount - refundDOs.stream().mapToDouble(RefundDO::getAmount).sum();
                                }
                                remainingAmount = remainingAmount * 0.5;
                            }
                        }
                        visaOfficialDO = serviceOrderCompleteNode.visaOfficialDao.getByServiceOrderIdOne(serviceOrderDto.getId());
                        if (extraAmount > 0) {
                            VisaOfficialDO visaOfficialDO2 = visaOfficialDO;
                            visaOfficialDO.setCommissionAmount(remainingAmount / 1.1 / 2 + extraAmount - visaOfficialDO.getCommissionAmount());
                            visaOfficialDO.setPredictCommission(remainingAmount / 2 / 1.1 * servicePackagePriceV2DTO.getRate() / 100 + extraAmount * 1.4 / 100 - visaOfficialDO.getPredictCommission());
                            visaOfficialDO.setPerAmount(visaOfficialDO.getPerAmount() * 0.5);
                            visaOfficialDO2.setPerAmount(visaOfficialDO.getPerAmount());
                            visaOfficialDO2.setId(visaOfficialDO2.getId());
                            visaOfficialDO2.setCommissionState("YJY");
                            visaOfficialDO2.setInstallmentNum(1);
                            visaOfficialDO2.setInstallment(2);
                            visaOfficialDO2.setPredictCommissionAmount(visaOfficialDO2.getPredictCommissionAmount());
                            visaOfficialDO2.setExchangeRate(visaOfficialDO2.getExchangeRate());
                            serviceOrderCompleteNode.visaOfficialDao.updateHandlingDate(visaOfficialDO.getId(),visaOfficialDO.getHandlingDate());
                            serviceOrderCompleteNode.visaOfficialDao.updateVisaOfficial(visaOfficialDO2);
                        } else {
                            for (VisaOfficialListDO visaOfficialListDO : list) {
                                visaOfficialDO.setCommissionAmount(remainingAmount / 1.1);
                                if (isBound) { // 被绑定订单金额确定
                                    visaOfficialDO.setCommissionAmount(visaOfficialDO.getCommissionAmount() - costPrince);
                                    visaOfficialDO.setPredictCommission((visaOfficialDO.getCommissionAmount()) * servicePackagePriceV2DTO.getRate() / 100);
                                }
                                visaOfficialDO.setCommissionAmount(visaOfficialDO.getCommissionAmount() - visaOfficialListDO.getCommissionAmount());
                                visaOfficialDO.setPredictCommission((visaOfficialDO.getCommissionAmount()) * servicePackagePriceV2DTO.getRate() / 100);
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


    // 判断以哪一个visa结算
    public static double findClosest(double num, double a, double b) {
        // 计算差的绝对值
        double diffA = Math.abs(num - a);
        double diffB = Math.abs(num - b);

        // 比较绝对值，如果相等则返回两个数值
        if (diffA == diffB) {
            return a;
        } else if (diffA < diffB) {
            return a;
        } else {
            return b;
        }
    }

    // 文案地区判断
    public ServicePackagePriceV2DTO closeJugdNew(Integer officialId, ServicePackagePriceDO servicePackagePriceDO) {
        // 判断文案结算方式
        ServicePackagePriceV2DTO servicePackagePriceV2DTO = new ServicePackagePriceV2DTO();
        OfficialDO officialDO = serviceOrderCompleteNode.officialDAO.getOfficialById(officialId);
        if (ObjectUtil.isNotNull(servicePackagePriceDO)) {
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
        }
        return servicePackagePriceV2DTO;
    }

}
