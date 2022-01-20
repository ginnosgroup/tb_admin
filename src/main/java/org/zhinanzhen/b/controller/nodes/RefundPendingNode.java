package org.zhinanzhen.b.controller.nodes;

import org.springframework.stereotype.Component;
import org.zhinanzhen.b.service.RefundService;

import com.ikasoa.web.workflow.Context;
import com.ikasoa.web.workflow.Node;

//待提交审核/驳回
@Component
public class RefundPendingNode extends RNode {

	// 顾问
	
	public RefundPendingNode(RefundService refundService) {
		super.refundService = refundService;
	}

	@Override
	public String getName() {
		return "PENDING";
	}

	@Override
	public Node getNextNode(Context context) {
		if (context.getNodeFactory() == null)
			return null;
		return context.getNodeFactory().getNode("REVIEW");
	}

	@Override
	public Context processNode(Context context) {
		isSingleStep = true;
		return context;
	}
	
	@Override
	public String[] nextNodeNames() {
		return new String[]{"PENDING", "REVIEW", "CLOSE"};
	}

}