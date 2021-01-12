package org.zhinanzhen.b.controller.nodes;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.ikasoa.core.utils.ListUtil;
import com.ikasoa.web.workflow.Node;
import com.ikasoa.web.workflow.NodeFactory;
import com.ikasoa.web.workflow.nodes.SuspendNode;

@Component
public class SONodeFactory implements NodeFactory {
	
	@Resource
	private ServiceOrderPendingNode serviceOrderPendingNode;
	
	@Resource
	private ServiceOrderReviewNode serviceOrderReviewNode;

	public List<Node> nodeList = ListUtil.buildArrayList(
			serviceOrderPendingNode,
			serviceOrderReviewNode, 
			new ServiceOrderWaitNode(),
			new ServiceOrderCloseNode(),
			new ServiceOrderFinishNode(),
			new ServiceOrderCompleteNode(),
			new ServiceOrderPaidNode(),
			new SuspendNode());

	public Node getNode(String name) {
		for (Node node : nodeList) {
System.out.println("----------name:"+name);
System.out.println("----------node.getName():"+node.getName());
			if (name.equals(node.getName()))
				return node;
		}
		return null;
	}

}
