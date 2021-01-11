package org.zhinanzhen.b.controller.nodes;

import org.springframework.stereotype.Component;
import org.zhinanzhen.b.controller.SODecisionNode;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

import com.ikasoa.web.workflow.Context;

//mara审核完成
@Component
public class ServiceOrderFinishNode extends SODecisionNode {

	// mara

	@Override
	public String getName() {
		return "FINISH";
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
//			if (!"MA".equalsIgnoreCase(getAp(context)) || !"VISA".equalsIgnoreCase(serviceOrderDto.getType())) {
//				context.putParameter("response", new Response<ServiceOrderDTO>(1, "Mara审核仅限签证服务订单!", null));
//				return SUSPEND_NODE;
//			}
		} catch (ServiceException e) {
			context.putParameter("response", new Response<ServiceOrderDTO>(1, "服务订单执行异常:" + e.getMessage(), null));
			return SUSPEND_NODE;
		}
		String state = getState(context, "APPLY", "COMPLETE", "CLOSE");
		if (state == null)
			return SUSPEND_NODE;
		return state;
	}

}
