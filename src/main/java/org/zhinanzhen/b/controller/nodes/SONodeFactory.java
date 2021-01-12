package org.zhinanzhen.b.controller.nodes;

import java.util.List;

import javax.annotation.Resource;

import com.ikasoa.core.utils.ListUtil;
import com.ikasoa.web.workflow.Node;
import com.ikasoa.web.workflow.NodeFactory;
import com.ikasoa.web.workflow.nodes.SuspendNode;

public class SONodeFactory {
	
	@Resource
	private ServiceOrderReviewNode serviceOrderReviewNode;
	
	
	public List<Node> nodeList = ListUtil.buildArrayList(
			new ServiceOrderPendingNode(),
			serviceOrderReviewNode, 
			new ServiceOrderWaitNode(),
			new ServiceOrderCloseNode(),
			new ServiceOrderFinishNode(),
			new ServiceOrderCompleteNode(),
			new ServiceOrderPaidNode(),
			new SuspendNode());

	private NodeFactory nodeFactory = new NodeFactory(nodeList);

	public Node getNode(String name) {
		return nodeFactory.getNode(name);
	}

}
