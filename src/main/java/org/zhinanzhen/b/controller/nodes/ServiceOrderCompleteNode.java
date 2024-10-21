package org.zhinanzhen.b.controller.nodes;

import com.ikasoa.core.utils.ObjectUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zhinanzhen.b.dao.*;
import org.zhinanzhen.b.dao.pojo.*;
import org.zhinanzhen.b.service.ServiceOrderService;
import org.zhinanzhen.b.service.pojo.CommissionAmountDTO;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.dao.RegionDAO;
import org.zhinanzhen.tb.service.ServiceException;

import com.ikasoa.web.workflow.Context;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
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

    public static ServiceOrderCompleteNode serviceOrderCompleteNode;

    @PostConstruct
    public void init(){
        serviceOrderCompleteNode=this;
        serviceOrderCompleteNode.visaOfficialDao=this.visaOfficialDao;
        serviceOrderCompleteNode.serviceDAO=this.serviceDAO;
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
            if (!"ZX".equals(type) && !serviceOrderDto.isSettle() && !"WA".equalsIgnoreCase(getAp(context))) { // 咨询不用判断文案权限,扣拥留学不用判断
                context.putParameter("response", new Response<ServiceOrderDTO>(1, "仅限文案操作!", null));
                return null;
            }
            if ("VISA".equals(type) && serviceOrderDto.getParentId() == 0) // 签证
            {
                List<String> arrayList = serviceOrderCompleteNode.serviceDAO.listLongTimeVisa();
                String code = serviceOrderDto.getService().getCode();
                String serviceType = code.replaceAll("[^\\p{L}\\p{N}\\p{Script=Han}]+", "");
                if (arrayList.contains(serviceType)) {
                    VisaOfficialDO visaOfficialDO = new VisaOfficialDO();
                    VisaOfficialDO visaOfficialDO1 = new VisaOfficialDO();
                    visaOfficialDO = serviceOrderCompleteNode.visaOfficialDao.getByServiceOrderIdOne(serviceOrderDto.getId());
                    visaOfficialDO.setInstallmentNum(2);
                    visaOfficialDO.setInstallment(2);
                    visaOfficialDO.setKjApprovalDate(null);
                    visaOfficialDO1.setId(visaOfficialDO.getId());
                    visaOfficialDO1.setCommissionState("YJY");
                    visaOfficialDO1.setInstallmentNum(1);
                    visaOfficialDO1.setInstallment(2);
                    visaOfficialDO1.setPredictCommissionAmount(visaOfficialDO.getPredictCommissionAmount());
                    visaOfficialDO1.setExchangeRate(visaOfficialDO.getExchangeRate());
                    if (ObjectUtil.isNotNull(officialById) && 1000034 == officialById.getGradeId()) {
                        visaOfficialDO.setPerAmount(visaOfficialDO.getPerAmount() * 0.4);
                        visaOfficialDO1.setPerAmount(visaOfficialDO.getPerAmount());
                    } else {
                        visaOfficialDO.setPerAmount(visaOfficialDO.getPerAmount() * 0.5);
                        visaOfficialDO1.setPerAmount(visaOfficialDO.getPerAmount());
                    }
                    serviceOrderCompleteNode.visaOfficialDao.addVisa(visaOfficialDO);
                    serviceOrderCompleteNode.visaOfficialDao.updateHandlingDate(visaOfficialDO.getId(),visaOfficialDO.getHandlingDate());
                    serviceOrderCompleteNode.visaOfficialDao.updateVisaOfficial(visaOfficialDO1);
                }
                return SUSPEND_NODE;
            }

            if (serviceOrderDto.getApplicantParentId() > 0) {
                ServiceOrderDTO serviceOrderParent = serviceOrderCompleteNode.serviceOrderService.getServiceOrderById(serviceOrderDto.getApplicantParentId());
                isSiv = "SIV".equalsIgnoreCase(serviceOrderParent.getType());
                if (isSiv) {
                    ServicePackageDO servicePackageDO = serviceOrderCompleteNode.servicePackageDAO.getById(serviceOrderDto.getServicePackageId());
                    if (ObjectUtil.isNotNull(servicePackageDO) && "VA".equalsIgnoreCase(servicePackageDO.getType())) {
                            VisaOfficialDO visaOfficialDO = new VisaOfficialDO();
                            VisaOfficialDO visaOfficialDO1 = new VisaOfficialDO();
                            visaOfficialDO = serviceOrderCompleteNode.visaOfficialDao.getByServiceOrderIdOne(serviceOrderDto.getId());
                            visaOfficialDO.setInstallmentNum(2);
                            visaOfficialDO.setInstallment(2);
                            visaOfficialDO.setKjApprovalDate(null);
                            visaOfficialDO1.setId(visaOfficialDO.getId());
                            visaOfficialDO1.setCommissionState("YJY");
                            visaOfficialDO1.setInstallmentNum(1);
                            visaOfficialDO1.setInstallment(2);
                            visaOfficialDO1.setPredictCommissionAmount(visaOfficialDO.getPredictCommissionAmount());
                            visaOfficialDO1.setExchangeRate(visaOfficialDO.getExchangeRate());
                        if (ObjectUtil.isNotNull(officialById) && 1000034 == officialById.getGradeId()) {
                            visaOfficialDO.setPerAmount(visaOfficialDO.getPerAmount() * 0.5);
                            visaOfficialDO1.setPerAmount(visaOfficialDO.getPerAmount());
                        } else {
                            visaOfficialDO.setPerAmount(visaOfficialDO.getPerAmount() * 0);
                            visaOfficialDO1.setPerAmount(visaOfficialDO.getPerAmount());
                        }
                        serviceOrderCompleteNode.visaOfficialDao.addVisa(visaOfficialDO);
                        serviceOrderCompleteNode.visaOfficialDao.updateHandlingDate(visaOfficialDO.getId(),visaOfficialDO.getHandlingDate());
                        serviceOrderCompleteNode.visaOfficialDao.updateVisaOfficial(visaOfficialDO1);
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

}
