package org.zhinanzhen.b.controller.nodes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.zhinanzhen.b.dao.OfficialReviewRuleDAO;
import org.zhinanzhen.b.dao.ServiceOrderDAO;
import org.zhinanzhen.b.dao.VisaDAO;
import org.zhinanzhen.b.service.ServiceOrderManageService;
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

	private final ServiceOrderManageService serviceOrderManageService;

	private final OfficialReviewRuleDAO officialReviewRuleDAO;

	private final ServiceOrderDAO serviceOrderDAO;

	private final VisaDAO visaDao;

	@Autowired
	public SONodeFactory(@Lazy ServiceOrderService serviceOrderService,
			ServiceOrderManageService serviceOrderManageService, OfficialReviewRuleDAO officialReviewRuleDAO,
			ServiceOrderDAO serviceOrderDAO, VisaDAO visaDao) {
		this.serviceOrderService = serviceOrderService;
		this.serviceOrderManageService = serviceOrderManageService;
		this.officialReviewRuleDAO = officialReviewRuleDAO;
		this.serviceOrderDAO = serviceOrderDAO;
		this.visaDao = visaDao;
	}


	public Node getNode(String name) {
		for (Node node : ListUtil.buildArrayList(
				new ServiceOrderPendingNode(serviceOrderService, visaDao),
				new ServiceOrderReviewNode(serviceOrderService, serviceOrderManageService, officialReviewRuleDAO,
						serviceOrderDAO),
				new ServiceOrderOfficialReviewNode(serviceOrderService), 
				new ServiceOrderWaitNode(serviceOrderService),
				new ServiceOrderRejectNode(serviceOrderService, serviceOrderManageService, officialReviewRuleDAO,
						serviceOrderDAO),
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
