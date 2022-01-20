package org.zhinanzhen.b.controller.nodes;

import org.springframework.stereotype.Component;
import org.zhinanzhen.b.service.RefundService;

import com.ikasoa.core.utils.ListUtil;
import com.ikasoa.web.workflow.Node;
import com.ikasoa.web.workflow.NodeFactory;
import com.ikasoa.web.workflow.nodes.SuspendNode;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Component
public class RNodeFactory implements NodeFactory {
	
	private RefundService refundService;

	public Node getNode(String name) {
		for (Node node : ListUtil.buildArrayList(
				new RefundPendingNode(refundService),
				new RefundReviewNode(refundService),
				new RefundCompleteNode(refundService),
				new RefundPaidNode(refundService),
				new RefundCloseNode(refundService),
				new SuspendNode()))
			if (name.equals(node.getName()))
				return node;
		return null;
	}

}
