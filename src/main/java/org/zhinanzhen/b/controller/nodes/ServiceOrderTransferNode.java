package org.zhinanzhen.b.controller.nodes;

import com.alibaba.fastjson.JSONArray;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// 长期签证中转
@Component
public class ServiceOrderTransferNode extends SODecisionNode{

    @Autowired
    private VisaOfficialDao visaOfficialDao;

    @Autowired
    private ServiceDAO serviceDAO;

    @Autowired
    private OfficialDAO officialDAO;

    @Autowired
    private ServicePackagePriceDAO servicePackagePriceDAO;

    @Autowired
    private ServiceOrderDAO serviceOrderDAO;

    @Autowired
    private VisaDAO visaDAO;

    public static ServiceOrderTransferNode serviceOrderTransferNode;

    @PostConstruct
    public void init(){
        serviceOrderTransferNode=this;
        serviceOrderTransferNode.visaOfficialDao=this.visaOfficialDao;
        serviceOrderTransferNode.serviceDAO=this.serviceDAO;
        serviceOrderTransferNode.officialDAO=this.officialDAO;
        serviceOrderTransferNode.serviceOrderDAO=this.serviceOrderDAO;
        serviceOrderTransferNode.visaDAO=this.visaDAO;
        serviceOrderTransferNode.servicePackagePriceDAO=this.servicePackagePriceDAO;
    }

    public ServiceOrderTransferNode(ServiceOrderService serviceOrderService) {
        super.serviceOrderService = serviceOrderService;
    }

    @Override
    protected String decide(Context context) {
        try {
            ServiceOrderDTO serviceOrderDto = serviceOrderService.getServiceOrderById(getServiceOrderId(context));
            String type = serviceOrderDto.getType();
            if (!"ZX".equals(type) && !serviceOrderDto.isSettle() && !"WA".equalsIgnoreCase(getAp(context))) { // 咨询不用判断文案权限,扣拥留学不用判断
                context.putParameter("response", new Response<ServiceOrderDTO>(1, "仅限文案操作!", null));
                return null;
            }
            // 计算绑定订单金额
            boolean isBound = false;
            double costPrince = 0.00;
            List<Integer> integers = new ArrayList<>();
            integers = serviceOrderTransferNode.serviceOrderDAO.listBybindingOrder(serviceOrderDto.getId());
            isBound = !integers.isEmpty();
            if (isBound) { // 被绑定订单金额确定
                for (Integer integer : integers) {
                    ServicePackagePriceDO byServiceId = serviceOrderTransferNode.servicePackagePriceDAO.getByServiceId(integer);
                    costPrince += byServiceId.getCostPrince();
                }
                costPrince = costPrince * 0.2;
            }
            if ("VISA".equals(type) && serviceOrderDto.getParentId() == 0) // 签证
            {
                List<String> arrayList = serviceOrderTransferNode.serviceDAO.listLongTimeVisa();
                String code = serviceOrderDto.getService().getCode();
                String serviceType = code.replaceAll("[^\\p{L}\\p{N}\\p{Script=Han}]+", "");
                List<VisaDO> visaDOS = serviceOrderTransferNode.visaDAO.listVisaByServiceOrderId(serviceOrderDto.getId());
                double amount = visaDOS.stream().mapToDouble(VisaDO::getAmount).sum();
                if (arrayList.contains(serviceType)) {
                    ServicePackagePriceDO servicePackagePriceDO = serviceOrderTransferNode.servicePackagePriceDAO.getByServiceId(serviceOrderDto.getServiceId());
                    ServicePackagePriceV2DTO servicePackagePriceV2DTO = closeJugdNew(serviceOrderDto.getOfficialId(), servicePackagePriceDO);
                    VisaOfficialDO visaOfficialDO = new VisaOfficialDO();
                    VisaOfficialDO visaOfficialDO1 = new VisaOfficialDO();
                    visaOfficialDO = serviceOrderTransferNode.visaOfficialDao.getByServiceOrderIdOne(serviceOrderDto.getId());
                    visaOfficialDO.setInstallmentNum(2);
                    visaOfficialDO.setInstallment(2);
                    visaOfficialDO.setKjApprovalDate(null);

                    visaOfficialDO.setCommissionAmount(amount - costPrince);
                    visaOfficialDO.setPredictCommission((visaOfficialDO.getCommissionAmount()) * servicePackagePriceV2DTO.getRate() / 100);
                    if (isBound) { // 被绑定订单金额确定
                        visaOfficialDO.setCommissionAmount(visaOfficialDO.getCommissionAmount() - costPrince);
                        visaOfficialDO.setPredictCommission((visaOfficialDO.getCommissionAmount()) * servicePackagePriceV2DTO.getRate() / 100);
                    }
                    visaOfficialDO.setPerAmount(visaOfficialDO.getPerAmount() * 0.2);
                    visaOfficialDO1.setPerAmount(visaOfficialDO.getPerAmount());

                    visaOfficialDO1.setPredictCommissionAmount(visaOfficialDO.getPredictCommissionAmount());
                    visaOfficialDO.setCommissionAmount(visaOfficialDO.getCommissionAmount() * 0.2);
                    visaOfficialDO.setPredictCommissionAmount(visaOfficialDO.getCommissionAmount());
                    visaOfficialDO.setPredictCommission(visaOfficialDO.getPredictCommission() * 0.2);
                    visaOfficialDO.setPredictCommissionCNY(visaOfficialDO.getPredictCommission() * visaOfficialDO.getExchangeRate());
                    visaOfficialDO1.setId(visaOfficialDO.getId());
                    visaOfficialDO1.setCommissionState("YJY");
                    visaOfficialDO1.setInstallmentNum(1);
                    visaOfficialDO1.setInstallment(2);
                    visaOfficialDO1.setExchangeRate(visaOfficialDO.getExchangeRate());
                    serviceOrderTransferNode.visaOfficialDao.addVisa(visaOfficialDO);
                    serviceOrderTransferNode.visaOfficialDao.updateHandlingDate(visaOfficialDO.getId(), visaOfficialDO.getHandlingDate());
                    serviceOrderTransferNode.visaOfficialDao.updateVisaOfficial(visaOfficialDO1);
                }
            }
                return SUSPEND_NODE;
        } catch (ServiceException e) {
            context.putParameter("response", new Response<ServiceOrderDTO>(1, "服务订单执行异常:" + e.getMessage(), null));
            return null;
        }
    }

    @Override
    public String getName() {
        return "TRANSFER";
    }

    @Override
    public String[] nextNodeNames() {
        return new String[]{"COMPLETE", "PAID", "CLOSE", "APPLY_FAILED"};
    }

    // 文案地区判断
    public ServicePackagePriceV2DTO closeJugdNew(Integer officialId, ServicePackagePriceDO servicePackagePriceDO) {
        // 判断文案结算方式
        ServicePackagePriceV2DTO servicePackagePriceV2DTO = new ServicePackagePriceV2DTO();
        OfficialDO officialDO = serviceOrderTransferNode.officialDAO.getOfficialById(officialId);
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
