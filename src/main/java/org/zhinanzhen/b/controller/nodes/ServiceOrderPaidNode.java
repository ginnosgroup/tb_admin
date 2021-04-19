package org.zhinanzhen.b.controller.nodes;

import org.springframework.stereotype.Component;
import org.zhinanzhen.b.service.ServiceOrderService;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.tb.controller.Response;

import com.ikasoa.web.workflow.Context;

//文案COE支付成功
@Component
public class ServiceOrderPaidNode extends SODecisionNode {

	public ServiceOrderPaidNode(ServiceOrderService serviceOrderService) {
		super.serviceOrderService = serviceOrderService;
	}
	
	@Override
	public String getName() {
		return "PAID";
	}

	@Override
	protected String decide(Context context) {
		isSingleStep = true;
		if (!"WA".equalsIgnoreCase(getAp(context))) {
			context.putParameter("response", new Response<ServiceOrderDTO>(1, "仅限文案操作!", null));
			return null;
		}
		return null;
	}
	
	@Override
	public String[] nextNodeNames() {
		return new String[]{"CLOSE"};
	}

}
