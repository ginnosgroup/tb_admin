package org.zhinanzhen.b.controller.nodes;

import org.springframework.stereotype.Component;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

import com.ikasoa.web.workflow.Context;

// 文案已提交申请
@Component
public class ServiceOrderApplyNode extends SODecisionNode {

	// 文案

	@Override
	public String getName() {
		return "APPLY";
	}

	@Override
	protected String decide(Context context) {
		isSingleStep = true;
		ServiceOrderDTO serviceOrderDto;
		try {
			serviceOrderDto = serviceOrderService.getServiceOrderById(getServiceOrderId(context));
			if (serviceOrderDto == null) {
				context.putParameter("response",
						new Response<ServiceOrderDTO>(1, "服务订单不存在:" + getServiceOrderId(context), null));
				return SUSPEND_NODE;
			}
//			if (!"WA".equalsIgnoreCase(getAp(context))) {
//				context.putParameter("response", new Response<ServiceOrderDTO>(1, "仅限文案操作!", null));
//				return SUSPEND_NODE;
//			}
		} catch (ServiceException e) {
			context.putParameter("response", new Response<ServiceOrderDTO>(1, "服务订单执行异常:" + e.getMessage(), null));
			return SUSPEND_NODE;
		}
		// to:apply,complete,close
		String state = getState(context, "APPLY", "COMPLETE", "CLOSE");
		if (state == null)
			return SUSPEND_NODE;
		return state;
	}

}
