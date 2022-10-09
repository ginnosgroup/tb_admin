package org.zhinanzhen.b.controller.nodes;

import org.springframework.stereotype.Component;
import org.zhinanzhen.b.dao.*;
import org.zhinanzhen.b.dao.pojo.*;
import org.zhinanzhen.b.service.ServiceOrderService;
import org.zhinanzhen.b.service.pojo.CommissionAmountDTO;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

import com.ikasoa.web.workflow.Context;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

//申请成功
@Component
public class ServiceOrderCompleteNode extends SODecisionNode {

    @Resource
    private ServicePackagePriceDAO servicePackagePriceDAO;

    @Resource
    private RefundDAO refundDAO;

    @Resource
    private OfficialDAO officialDAO;

    @Resource
    private OfficialGradeDao officialGradeDao;


    @Resource
    private ServiceOrderDAO serviceOrderDao;

    @Resource
    private VisaDAO visaDAO;

    @Resource
    private ServiceOrderOfficialRemarksDAO serviceOrderOfficialRemarksDAO;

    @Resource
    private ServiceDAO serviceDAO;

    @Resource
    private VisaOfficialDao visaOfficialDao;


    // 文案

    public ServiceOrderCompleteNode(ServiceOrderService serviceOrderService) {
        super.serviceOrderService = serviceOrderService;
    }

    @Override
    public String getName() {
        return "COMPLETE";
    }

    @Override
    protected String decide(Context context) {
        //if (!"WA".equalsIgnoreCase(getAp(context))) {
        //	context.putParameter("response", new Response<ServiceOrderDTO>(1, "仅限文案操作!", null));
        //	return null;
        //}
        try {
            ServiceOrderDTO serviceOrderDto = serviceOrderService.getServiceOrderById(getServiceOrderId(context));
            String type = serviceOrderDto.getType();
            if (!"ZX".equals(type) && !serviceOrderDto.isSettle() && !"WA".equalsIgnoreCase(getAp(context))) { //咨询不用判断文案权限,扣拥留学不用判断
                context.putParameter("response", new Response<ServiceOrderDTO>(1, "仅限文案操作!", null));
                return null;
            }
            if ("VISA".equals(type) && serviceOrderDto.getParentId() == 0) // 签证
                return SUSPEND_NODE;
            if ("ZX".equals(type)) {//咨询
                isSingleStep = true;
                return SUSPEND_NODE;
            }
            List<String> arrayList = new ArrayList<String>() {
                {
                    this.add("103");
                    this.add("143");
                    this.add("173");
                    this.add("864");
                    this.add("835");
                    this.add("838");
                }
            };
            if (arrayList.contains(serviceDAO.getServiceById(serviceOrderDto.getServiceId()).getCode())) {

                VisaOfficialDO visaOfficialDO = visaOfficialDao.listByServiceOrderId(serviceOrderDto.getId());
                visaOfficialDO.setInstallmentNum(2);
                visaOfficialDao.addVisa(visaOfficialDO);

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
