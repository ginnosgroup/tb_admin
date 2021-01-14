package org.zhinanzhen.b.controller.nodes;

import org.springframework.stereotype.Component;
import org.zhinanzhen.b.service.ServiceOrderService;

// Mara驳回
@Component
public class ServiceOrderRejectNode extends ServiceOrderReviewNode {

	public ServiceOrderRejectNode(ServiceOrderService serviceOrderService) {
		super(serviceOrderService);
	}

	@Override
	public String getName() {
		return "REJECT";
	}

}
