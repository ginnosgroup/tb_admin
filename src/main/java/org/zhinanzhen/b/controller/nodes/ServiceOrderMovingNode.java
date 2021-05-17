package org.zhinanzhen.b.controller.nodes;

import com.ikasoa.web.workflow.Context;
import org.springframework.stereotype.Component;
import org.zhinanzhen.b.service.ServiceOrderService;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

/**
 * Created with IntelliJ IDEA.
 * Date: 2021/05/10 16:56
 * Description:等待财务转账状态 MOVING
 * 提前扣拥类型：COMPLETE---->MOVING
 * Version: V1.0
 */

@Component
public class ServiceOrderMovingNode extends  SODecisionNode{

    //文案

    public ServiceOrderMovingNode(ServiceOrderService serviceOrderService){
        super.serviceOrderService = serviceOrderService;
    }

    @Override
    protected String decide(Context context) {
        if (!"WA".equalsIgnoreCase(getAp(context)) && !"KJ".equalsIgnoreCase(getAp(context))) {
            context.putParameter("response", new Response<ServiceOrderDTO>(1, "仅限文案或者会计操作!", null));
            return null;
        }
        try {
            ServiceOrderDTO serviceOrderDto = serviceOrderService.getServiceOrderById(getServiceOrderId(context));
            String type = serviceOrderDto.getType();
            if ("VISA".equals(type) && serviceOrderDto.getParentId() == 0) // 签证
                return null;
            isSingleStep = true;
            return "REFERED";
        } catch (ServiceException e) {
            context.putParameter("response", new Response<ServiceOrderDTO>(1, "服务订单执行异常:" + e.getMessage(), null));
            return null;
        }
    }

    @Override
    public String getName() {
        return "MOVING";
    }

    @Override
    public String[] nextNodeNames() {
        return new String[]{"REFERED", "CLOSE"};
    }
}
