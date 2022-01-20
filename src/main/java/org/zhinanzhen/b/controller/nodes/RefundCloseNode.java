package org.zhinanzhen.b.controller.nodes;

import org.springframework.stereotype.Component;
import org.zhinanzhen.b.service.RefundService;
import org.zhinanzhen.b.service.pojo.RefundDTO;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

import com.ikasoa.web.workflow.Context;
import com.ikasoa.web.workflow.Node;
import com.ikasoa.web.workflow.nodes.SuspendNode;

// 关闭
@Component
public class RefundCloseNode extends RNode {
	
	// 顾问

	public RefundCloseNode(RefundService refundService) {
		super.refundService = refundService;
	}

	@Override
	public String getName() {
		return "CLOSE";
	}

	@Override
	public Node getNextNode(Context context) {
		return new SuspendNode();
	}

	@Override
	public Context processNode(Context context) {
		RefundDTO refundDto;
		try {
			refundDto = refundService.getRefundById(getRefundId(context));
			if (refundDto == null) {
				context.putParameter("response",
						new Response<ServiceOrderDTO>(1, "退款单不存在:" + getRefundId(context), null));
				return null;
			}
		} catch (ServiceException e) {
			context.putParameter("response", new Response<ServiceOrderDTO>(1, "退款单执行异常:" + e.getMessage(), null));
		}
		return context;
	}

}
