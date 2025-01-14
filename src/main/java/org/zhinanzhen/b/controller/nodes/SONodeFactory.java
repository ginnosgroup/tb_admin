package org.zhinanzhen.b.controller.nodes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.zhinanzhen.b.service.ServiceOrderService;

import com.ikasoa.core.utils.ListUtil;
import com.ikasoa.web.workflow.Node;
import com.ikasoa.web.workflow.NodeFactory;
import com.ikasoa.web.workflow.nodes.SuspendNode;

import lombok.AllArgsConstructor;

//@AllArgsConstructor
@Component
public class SONodeFactory implements NodeFactory {

	private final ServiceOrderService serviceOrderService;

	@Autowired
	public SONodeFactory(@Lazy ServiceOrderService serviceOrderService) {
		this.serviceOrderService = serviceOrderService;
	}

	public Node getNode(String name) {
		for (Node node : ListUtil.buildArrayList(
				new ServiceOrderPendingNode(serviceOrderService),
				new ServiceOrderReviewNode(serviceOrderService),
				new ServiceOrderOfficialReviewNode(serviceOrderService), 
				new ServiceOrderWaitNode(serviceOrderService),
				new ServiceOrderRejectNode(serviceOrderService), 
				new ServiceOrderCloseNode(serviceOrderService),
				new ServiceOrderFinishNode(serviceOrderService), 
				new ServiceOrderCompleteNode(serviceOrderService),
				new ServiceOrderPaidNode(serviceOrderService), 
				new ServiceOrderWaitFdNode(serviceOrderService),
				new ServiceOrderCompleteFdNode(serviceOrderService), 
				new ServiceOrderReceivedNode(serviceOrderService),
				new ServiceOrderApplyFailedNode(serviceOrderService),
				new ServiceOrderApplyNode(serviceOrderService),
				new ServiceOrderTransferNode(serviceOrderService),
				new SuspendNode())) {
			if (name.equals(node.getName()))
				return node;
		}
		return null;
	}

}
