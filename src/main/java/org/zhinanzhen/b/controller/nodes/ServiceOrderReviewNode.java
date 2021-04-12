package org.zhinanzhen.b.controller.nodes;

import org.springframework.stereotype.Component;
import org.zhinanzhen.b.service.ServiceOrderService;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

import com.ikasoa.web.workflow.Context;

// 文案审核
@Component
public class ServiceOrderReviewNode extends SODecisionNode {

	// 顾问,文案
	
	public ServiceOrderReviewNode(ServiceOrderService serviceOrderService) {
		super.serviceOrderService = serviceOrderService;
	}

	@Override
	public String getName() {
		return "REVIEW";
	}

	@Override
	protected String decide(Context context) {
		isSingleStep = true;
		if (!"GW".equalsIgnoreCase(getAp(context))) {
			context.putParameter("response", new Response<ServiceOrderDTO>(1, "仅限顾问操作!文案审核请传'OREVIEW'.", null));
			return SUSPEND_NODE;
		}
		try {
			ServiceOrderDTO serviceOrderDto = serviceOrderService.getServiceOrderById(getServiceOrderId(context));
			if (serviceOrderDto == null) {
				context.putParameter("response",
						new Response<ServiceOrderDTO>(1, "服务订单不存在:" + getServiceOrderId(context), null));
				return SUSPEND_NODE;
			}
			if (serviceOrderDto.getParentId() == 0 && ("SIV".equalsIgnoreCase(serviceOrderDto.getType())
					|| "MT".equalsIgnoreCase(serviceOrderDto.getType()))) {
				context.putParameter("response", new Response<ServiceOrderDTO>(1, "该订单不支持审核.", serviceOrderDto));
				return SUSPEND_NODE;
			}
		} catch (ServiceException e) {
			context.putParameter("response", new Response<ServiceOrderDTO>(1, "服务订单执行异常:" + e.getMessage(), null));
			return SUSPEND_NODE;
		}
//		return SUSPEND_NODE;
		String state = getNextState(context);
		if ("WAIT".equalsIgnoreCase(state) && !"VISA".equalsIgnoreCase(getType(context))) {
			context.putParameter("response", new Response<ServiceOrderDTO>(1, "只有签证类才能进行mara审核流程.", null));
			return SUSPEND_NODE;
		}
		if (state == null && context.getParameter("state") == null) {
			context.putParameter("response", new Response<ServiceOrderDTO>(1, "状态值不能为空.", null));
			return SUSPEND_NODE;
		}
		return state;
	}
	
	@Override
	public String[] nextNodeNames() {
		return new String[]{"REVIEW","PENDING", "OREVIEW", "WAIT", "CLOSE", "FINISH"}; // "PENDING"是驳回
	}

}
