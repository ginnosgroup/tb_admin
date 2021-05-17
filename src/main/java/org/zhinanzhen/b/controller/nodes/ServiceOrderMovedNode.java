package org.zhinanzhen.b.controller.nodes;

import com.ikasoa.web.workflow.Context;
import org.springframework.stereotype.Component;
import org.zhinanzhen.b.service.ServiceOrderService;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

/**
 * Created with IntelliJ IDEA.
 * Date: 2021/05/10 18:28
 * Description:财务转账完成状态:    MOVED
 * 文案操作
 * 提前扣拥类型：MOVEING ---->REFERED
 * Version: V1.0
 */
@Component
public class ServiceOrderMovedNode extends  SODecisionNode{

    //文案

    public ServiceOrderMovedNode(ServiceOrderService serviceOrderService){
        super.serviceOrderService = serviceOrderService;
    }

    @Override
    protected String decide(Context context) {
        isSingleStep = true;
        if (!"KJ".equalsIgnoreCase(getAp(context))) {
            context.putParameter("response", new Response<ServiceOrderDTO>(1, "仅限会计操作!", null));
            return null;
        }
        return "PAID";
    }

    @Override
    public String getName() {
        return "MOVED";
    }

    @Override
    public String[] nextNodeNames() {
        return new String[]{"PAID","CLOSE"};
    }
}
