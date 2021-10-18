package org.zhinanzhen.b.controller.nodes;

import com.ikasoa.web.workflow.Context;
import org.springframework.stereotype.Component;
import org.zhinanzhen.b.service.ServiceOrderService;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.tb.controller.Response;

/**
 * Created with IntelliJ IDEA.
 * Date: 2021/05/11 12:00
 * Description:已收款凭证已提交:  RECEIVED
 * 顾问操作
 * 提前扣拥类型：WAITFD ---->RECEIVED
 * Version: V1.0
 */
@Component
public class ServiceOrderReceivedNode extends SODecisionNode{

    //顾问

    public ServiceOrderReceivedNode(ServiceOrderService serviceOrderService){
        super.serviceOrderService = serviceOrderService;
    }

    @Override
    protected String decide(Context context) {
        isSingleStep = true;
        String state = getNextState(context);
        if (!"GW".equalsIgnoreCase(getAp(context))) {
            context.putParameter("response", new Response<ServiceOrderDTO>(1, "仅限顾问操作!", null));
            return null;
        }
        return state;
    }

    @Override
    public String getName() {
        return "RECEIVED";
    }

    @Override
    public String[] nextNodeNames() {
        return new String[]{"COMPLETE_FD","WAIT_FD","CLOSE"};//WAIT_FD 表示会计驳回。  WAIT_FD->RECEIVED
    }
}
