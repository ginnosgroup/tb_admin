package org.zhinanzhen.b.controller.nodes;

import org.springframework.stereotype.Component;
import org.zhinanzhen.b.controller.SODecisionNode;

import com.ikasoa.web.workflow.Context;

//文案COE支付成功
@Component
public class ServiceOrderPaidNode extends SODecisionNode {

	@Override
	public String getName() {
		return "PAID";
	}

	@Override
	protected String decide(Context context) {
		isSingleStep = true;
		return null;
	}

}
