package org.zhinanzhen.b.controller.nodes;

import org.springframework.stereotype.Component;
import org.zhinanzhen.b.service.ServiceOrderService;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.tb.controller.Response;

import com.ikasoa.web.workflow.Context;

// mara审核
@Component
public class ServiceOrderWaitNode extends SODecisionNode {

	// 文案,mara

	public ServiceOrderWaitNode(ServiceOrderService serviceOrderService) {
		super.serviceOrderService = serviceOrderService;
	}

	@Override
	public String getName() {
		return "WAIT";
	}

	@Override
	protected String decide(Context context) {
		isSingleStep = true;
		String state = getState(context, "REVIEW", "REJECT", "FINISH");
		if ("REJECT".equalsIgnoreCase(state) && !"VISA".equalsIgnoreCase(getType(context))) {
			context.putParameter("response", new Response<ServiceOrderDTO>(1, "只有签证类才能进行mara审核流程.", null));
			return SUSPEND_NODE;
		}
		return state;
	}

}
