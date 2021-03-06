package org.zhinanzhen.b.controller.nodes;

import java.util.List;

import org.springframework.stereotype.Component;
import org.zhinanzhen.b.service.ServiceOrderService;

import com.ikasoa.core.utils.ListUtil;
import com.ikasoa.web.workflow.Node;
import com.ikasoa.web.workflow.NodeFactory;
import com.ikasoa.web.workflow.nodes.SuspendNode;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Component
public class SONodeFactory implements NodeFactory {
	
	private ServiceOrderService serviceOrderService;

	public List<Node> nodeList = ListUtil.buildArrayList(
			new ServiceOrderPendingNode(serviceOrderService),
			new ServiceOrderReviewNode(serviceOrderService), 
			new ServiceOrderOfficialReviewNode(serviceOrderService), 
			new ServiceOrderWaitNode(serviceOrderService),
			new ServiceOrderRejectNode(serviceOrderService),
			new ServiceOrderCloseNode(serviceOrderService),
			new ServiceOrderFinishNode(serviceOrderService),
			new ServiceOrderCompleteNode(serviceOrderService),
			new ServiceOrderPaidNode(serviceOrderService),
			new SuspendNode());

	public Node getNode(String name) {
		for (Node node : nodeList) {
			if (name.equals(node.getName()))
				return node;
		}
		return null;
	}

}
