package org.zhinanzhen.b.controller.nodes;

import org.springframework.stereotype.Component;
import org.zhinanzhen.b.service.ServiceOrderManageService;
import org.zhinanzhen.b.service.ServiceOrderService;

// Mara驳回
@Component
public class ServiceOrderRejectNode extends ServiceOrderReviewNode {

	public ServiceOrderRejectNode(ServiceOrderService serviceOrderService, ServiceOrderManageService serviceOrderManageService) {
		super(serviceOrderService, serviceOrderManageService);
	}

	@Override
	public String getName() {
		return "REJECT";
	}
	
	@Override
	public String[] nextNodeNames() {
		return null;
	}

}
