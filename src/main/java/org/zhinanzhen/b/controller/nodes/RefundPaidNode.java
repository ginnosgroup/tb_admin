package org.zhinanzhen.b.controller.nodes;

import java.util.Date;

import org.springframework.stereotype.Component;
import org.zhinanzhen.b.service.RefundService;
import org.zhinanzhen.b.service.pojo.RefundDTO;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

import com.ikasoa.web.workflow.Context;

//退款支付成功
@Component
public class RefundPaidNode extends RDecisionNode {

	public RefundPaidNode(RefundService refundService) {
		super.refundService = refundService;
	}

	@Override
	public String getName() {
		return "PAID";
	}

	@Override
	protected String decide(Context context) {
		isSingleStep = true;
		if (!"KJ".equalsIgnoreCase(getAp(context))) {
			context.putParameter("response", new Response<ServiceOrderDTO>(1, "仅限财务操作!", null));
			return null;
		}
		try {
			RefundDTO refundDto = refundService.getRefundById(getRefundId(context));
			if (refundDto == null) {
				context.putParameter("response",
						new Response<ServiceOrderDTO>(1, "退款单不存在:" + getRefundId(context), null));
				return null;
			}
			refundDto.setCompletedDate(new Date()); // 退款完成时间
			refundService.updateRefund(refundDto);
		} catch (ServiceException e) {
			context.putParameter("response", new Response<ServiceOrderDTO>(1, "退款单执行异常:" + e.getMessage(), null));
			return null;
		}
		return SUSPEND_NODE;
	}

	@Override
	public String[] nextNodeNames() {
		return new String[] { "PAID", "CLOSE" };
	}

}
