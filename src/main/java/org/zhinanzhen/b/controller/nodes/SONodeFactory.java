package org.zhinanzhen.b.controller;

import java.util.List;

import org.zhinanzhen.b.controller.nodes.ServiceOrderCloseNode;
import org.zhinanzhen.b.controller.nodes.ServiceOrderCompleteNode;
import org.zhinanzhen.b.controller.nodes.ServiceOrderFinishNode;
import org.zhinanzhen.b.controller.nodes.ServiceOrderPaidNode;
import org.zhinanzhen.b.controller.nodes.ServiceOrderPendingNode;
import org.zhinanzhen.b.controller.nodes.ServiceOrderReviewNode;
import org.zhinanzhen.b.controller.nodes.ServiceOrderWaitNode;

import com.ikasoa.core.utils.ListUtil;
import com.ikasoa.web.workflow.Node;
import com.ikasoa.web.workflow.NodeFactory;
import com.ikasoa.web.workflow.nodes.SuspendNode;

public class SONodeFactory {
	
	public static List<Node> nodeList = ListUtil.buildArrayList(
			new ServiceOrderPendingNode(),
			new ServiceOrderReviewNode(), 
			new ServiceOrderWaitNode(),
			new ServiceOrderCloseNode(),
			new ServiceOrderFinishNode(),
			new ServiceOrderCompleteNode(),
			new ServiceOrderPaidNode(),
			new SuspendNode());

	private static NodeFactory nodeFactory = new NodeFactory(nodeList);

	public static Node getNode(String name) {
		return nodeFactory.getNode(name);
	}

}
