package org.zhinanzhen.b.controller.nodes;

import com.ikasoa.core.utils.ObjectUtil;
import com.ikasoa.web.workflow.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zhinanzhen.b.dao.OfficialDAO;
import org.zhinanzhen.b.dao.ServiceDAO;
import org.zhinanzhen.b.dao.ServicePackageDAO;
import org.zhinanzhen.b.dao.VisaOfficialDao;
import org.zhinanzhen.b.dao.pojo.OfficialDO;
import org.zhinanzhen.b.dao.pojo.ServicePackageDO;
import org.zhinanzhen.b.dao.pojo.VisaOfficialDO;
import org.zhinanzhen.b.service.ServiceOrderService;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

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

    public static ServiceOrderApplyFailedNode serviceOrderApplyFailedNode;

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
                        OfficialDO officialById = serviceOrderApplyFailedNode.officialDAO.getOfficialById(serviceOrderDto.getOfficialId());

                        VisaOfficialDO visaOfficialDO = new VisaOfficialDO();
                        VisaOfficialDO visaOfficialDO1 = new VisaOfficialDO();
                        visaOfficialDO = serviceOrderApplyFailedNode.visaOfficialDao.getByServiceOrderIdOne(serviceOrderDto.getId());
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
                        serviceOrderApplyFailedNode.visaOfficialDao.addVisa(visaOfficialDO);
                        serviceOrderApplyFailedNode.visaOfficialDao.updateHandlingDate(visaOfficialDO.getId(),visaOfficialDO.getHandlingDate());
                        serviceOrderApplyFailedNode.visaOfficialDao.updateVisaOfficial(visaOfficialDO1);
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


}
