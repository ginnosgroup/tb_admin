package org.zhinanzhen.b.controller.nodes;

import com.ikasoa.web.workflow.Context;
import org.springframework.stereotype.Component;
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
            isSingleStep = true;
            ServiceOrderDTO serviceOrderDto = serviceOrderService.getServiceOrderById(getServiceOrderId(context));
            String type = serviceOrderDto.getType();
            if (!"WA".equalsIgnoreCase(getAp(context))){
                context.putParameter("response", new Response<ServiceOrderDTO>(1, "仅限文案操作!", null));
                return null;
            }
            //if ( "VISA".equals(type) && serviceOrderDto.getParentId() == 0 ) // 签证
            //   return SUSPEND_NODE;
            //if ("ZX".equals(type)){//咨询
            //   isSingleStep = true;
            //   return SUSPEND_NODE;
            //}
            return SUSPEND_NODE;
        } catch (ServiceException e) {
            context.putParameter("response", new Response<ServiceOrderDTO>(1, "服务订单执行异常:" + e.getMessage(), null));
            return null;
        }
    }


}
