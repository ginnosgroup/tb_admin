package org.zhinanzhen.b.controller.nodes;

import org.springframework.stereotype.Component;
import org.zhinanzhen.b.service.RefundService;
import org.zhinanzhen.b.service.pojo.RefundDTO;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

import com.ikasoa.web.workflow.Context;

//申请成功
@Component
public class RefundCompleteNode extends RDecisionNode {

	// 财务

	public RefundCompleteNode(RefundService refundService) {
		super.refundService = refundService;
	}

	@Override
	public String getName() {
		return "COMPLETE";
	}

	@Override
	protected String decide(Context context) {
		try {
			isSingleStep = true;
			RefundDTO refundDto = refundService.getRefundById(getRefundId(context));
			if (refundDto == null) {
//				context.putParameter("response",
//						new Response<ServiceOrderDTO>(1, "退款单不存在:" + getRefundId(context), null));
				return null;
			}
			if (!"KJ".equalsIgnoreCase(getAp(context))) {
				context.putParameter("response", new Response<ServiceOrderDTO>(1, "仅限财务操作!", null));
				return null;
			}
			return getNextState(context);
		} catch (ServiceException e) {
			context.putParameter("response", new Response<ServiceOrderDTO>(1, "退款单执行异常:" + e.getMessage(), null));
			return null;
		}
	}

	@Override
	public String[] nextNodeNames() {
		return new String[] { "COMPLETE", "PAID", "CLOSE" };
	}

}
