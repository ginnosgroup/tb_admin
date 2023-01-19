package org.zhinanzhen.b.controller.nodes;

import org.springframework.stereotype.Component;
import org.zhinanzhen.b.service.ServiceOrderService;

import com.ikasoa.web.workflow.Context;
import com.ikasoa.web.workflow.Node;

@Component
public class ServiceOrderPendingNode extends SONode {

	// 顾问
	
	public ServiceOrderPendingNode(ServiceOrderService serviceOrderService) {
		super.serviceOrderService = serviceOrderService;
	}

	@Override
	public String getName() {
		return "PENDING";
	}

	@Override
	public Node getNextNode(Context context) {
		if (context.getNodeFactory() == null)
			return null;
		return context.getNodeFactory().getNode("REVIEW");
	}

	@Override
	public Context processNode(Context context) {
		isSingleStep = true;
		context.putParameter("stateMark2", "Retracted");
		return context;
	}
	
	@Override
	public String[] nextNodeNames() {
		return new String[]{"PENDING", "REVIEW", "COMPLETE"};
	}

}
