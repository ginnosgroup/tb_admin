package org.zhinanzhen.b.controller.nodes;

import org.springframework.stereotype.Component;
import org.zhinanzhen.b.service.ServiceOrderService;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

import com.ikasoa.web.workflow.Context;

//申请成功
@Component
public class ServiceOrderCompleteNode extends SODecisionNode {

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
		if (!"WA".equalsIgnoreCase(getAp(context))) {
			context.putParameter("response", new Response<ServiceOrderDTO>(1, "仅限文案操作!", null));
			return null;
		}
		try {
			ServiceOrderDTO serviceOrderDto = serviceOrderService.getServiceOrderById(getServiceOrderId(context));
			String type = serviceOrderDto.getType();
			if (!"OVST".equals(type))
				return null;
			isSingleStep = true;
			return "PAID";
		} catch (ServiceException e) {
			context.putParameter("response", new Response<ServiceOrderDTO>(1, "服务订单执行异常:" + e.getMessage(), null));
			return null;
		}
	}
	
	@Override
	public String[] nextNodeNames() {
		return new String[]{"PAID", "CLOSE"};
	}

}
