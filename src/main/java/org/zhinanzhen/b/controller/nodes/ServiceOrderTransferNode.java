package org.zhinanzhen.b.controller.nodes;

import com.ikasoa.web.workflow.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zhinanzhen.b.dao.ServiceDAO;
import org.zhinanzhen.b.dao.VisaOfficialDao;
import org.zhinanzhen.b.dao.pojo.VisaOfficialDO;
import org.zhinanzhen.b.service.ServiceOrderService;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

import java.util.List;

// 长期签证中转
@Component
public class ServiceOrderTransferNode extends SODecisionNode{

    @Autowired
    private VisaOfficialDao visaOfficialDao;

    @Autowired
    private ServiceDAO serviceDAO;

    public static ServiceOrderTransferNode serviceOrderTransferNode;

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
            if ("VISA".equals(type) && serviceOrderDto.getParentId() == 0) // 签证
            {
                List<String> arrayList = serviceOrderTransferNode.serviceDAO.listLongTimeVisa();
                String code = serviceOrderDto.getService().getCode();
                String serviceType = code.replaceAll("[^\\p{L}\\p{N}\\p{Script=Han}]+", "");
                if (arrayList.contains(serviceType)) {
                    VisaOfficialDO visaOfficialDO = new VisaOfficialDO();
                    VisaOfficialDO visaOfficialDO1 = new VisaOfficialDO();
                    visaOfficialDO = serviceOrderTransferNode.visaOfficialDao.getByServiceOrderIdOne(serviceOrderDto.getId());
                    visaOfficialDO.setInstallmentNum(2);
                    visaOfficialDO.setInstallment(2);
                    visaOfficialDO.setKjApprovalDate(null);
                    visaOfficialDO.setPerAmount(visaOfficialDO.getPerAmount() * 0.2);
                    visaOfficialDO1.setPerAmount(visaOfficialDO.getPerAmount());
                    visaOfficialDO1.setId(visaOfficialDO.getId());
                    visaOfficialDO1.setCommissionState("YJY");
                    visaOfficialDO1.setInstallmentNum(1);
                    visaOfficialDO1.setInstallment(2);
                    visaOfficialDO1.setPredictCommissionAmount(visaOfficialDO.getPredictCommissionAmount());
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
}
