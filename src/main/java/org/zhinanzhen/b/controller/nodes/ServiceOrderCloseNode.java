package org.zhinanzhen.b.controller.nodes;

import org.springframework.stereotype.Component;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

import com.ikasoa.core.utils.StringUtil;
import com.ikasoa.web.workflow.Context;
import com.ikasoa.web.workflow.Node;
import com.ikasoa.web.workflow.nodes.SuspendNode;

// 关闭
@Component
public class ServiceOrderCloseNode extends SONode {

	// 文案

	@Override
	public String getName() {
		return "CLOSE";
	}

	@Override
	public Node getNextNode(Context context) {
		return new SuspendNode();
	}

	@Override
	public Context processNode(Context context) {
		ServiceOrderDTO serviceOrderDto;
		try {
			serviceOrderDto = serviceOrderService.getServiceOrderById(getServiceOrderId(context));
			if (serviceOrderDto == null) {
				context.putParameter("response",
						new Response<ServiceOrderDTO>(1, "服务订单不存在:" + getServiceOrderId(context), null));
				exce(context);
			}
			String closedReason = getClosedReason(context);
			if (StringUtil.isNotEmpty(closedReason)) {
				serviceOrderDto.setClosedReason(closedReason);
				serviceOrderService.updateServiceOrder(serviceOrderDto);
			}
		} catch (ServiceException e) {
			context.putParameter("response", new Response<ServiceOrderDTO>(1, "服务订单执行异常:" + e.getMessage(), null));
		}
		return context;

	}

}
