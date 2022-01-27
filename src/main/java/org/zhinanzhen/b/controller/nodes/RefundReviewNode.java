package org.zhinanzhen.b.controller.nodes;

import org.springframework.stereotype.Component;
import org.zhinanzhen.b.service.RefundService;
import org.zhinanzhen.b.service.pojo.RefundDTO;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

import com.ikasoa.web.workflow.Context;

//提交财务审核
@Component
public class RefundReviewNode extends RDecisionNode {

	// 财务

	public RefundReviewNode(RefundService refundService) {
		super.refundService = refundService;
	}

	@Override
	public String getName() {
		return "REVIEW";
	}

	@Override
	protected String decide(Context context) {
		isSingleStep = true;
		try {
			RefundDTO refundDto = refundService.getRefundById(getRefundId(context));
			if (refundDto == null) {
				context.putParameter("response",
						new Response<ServiceOrderDTO>(1, "退款单不存在:" + getRefundId(context), null));
				return null;
			}
			if (!"GW".equalsIgnoreCase(getAp(context))) {
				context.putParameter("response", new Response<ServiceOrderDTO>(1, "仅限顾问操作!", null));
				return null;
			}
		} catch (ServiceException e) {
			context.putParameter("response", new Response<ServiceOrderDTO>(1, "退款单执行异常:" + e.getMessage(), null));
			return null;
		}
		return getNextState(context);
	}

	@Override
	public String[] nextNodeNames() {
		return new String[] { "REVIEW", "PENDING", "COMPLETE", "PAID", "CLOSE" }; // "PENDING"是驳回
	}

}