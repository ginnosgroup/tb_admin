package org.zhinanzhen.b.controller.nodes;

import javax.annotation.Resource;

import com.ikasoa.core.utils.ListUtil;
import com.ikasoa.web.workflow.Node;
import com.ikasoa.web.workflow.NodeFactory;
import com.ikasoa.web.workflow.nodes.SuspendNode;

public class SONodeFactory {
	
	@Resource
	private static ServiceOrderPendingNode serviceOrderPendingNode;
	
	@Resource
	private static ServiceOrderReviewNode serviceOrderReviewNode;

	public static NodeFactory nodeFactory = new NodeFactory(ListUtil.buildArrayList(
			serviceOrderPendingNode,
			serviceOrderReviewNode, 
			new ServiceOrderWaitNode(),
			new ServiceOrderCloseNode(),
			new ServiceOrderFinishNode(),
			new ServiceOrderCompleteNode(),
			new ServiceOrderPaidNode(),
			new SuspendNode()));

	public static Node getNode(String name) {
		return nodeFactory.getNode(name);
	}

}
