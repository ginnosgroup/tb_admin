package org.zhinanzhen.b.controller.nodes;

import org.springframework.stereotype.Component;

import com.ikasoa.web.workflow.Context;
import com.ikasoa.web.workflow.Node;

@Component
public class ServiceOrderPendingNode extends SONode {

	// 顾问

	@Override
	public String getName() {
		return "PENDING";
	}

	@Override
	public Node getNextNode() {
		return SONodeFactory.getNode("REVIEW");
	}

	@Override
	public Context processNode(Context context) {
		isSingleStep = true;
		return context;
	}

}
