package org.zhinanzhen.b.controller.nodes;

import org.springframework.stereotype.Component;

import com.ikasoa.web.workflow.Context;
import com.ikasoa.web.workflow.Node;
import com.ikasoa.web.workflow.NodeFactory;

@Component
public class ServiceOrderPendingNode extends SONode {
	
	private NodeFactory nodeFactory;

	// 顾问

	@Override
	public String getName() {
		return "PENDING";
	}

	@Override
	public Node getNextNode() {
		if(nodeFactory == null)
			return null;
		return nodeFactory.getNode("REVIEW");
	}

	@Override
	public Context processNode(Context context) {
		isSingleStep = true;
		nodeFactory = context.getNodeFactory();
		return context;
	}

}
