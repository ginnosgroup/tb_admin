package org.zhinanzhen.b.controller.nodes;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.ikasoa.web.workflow.Context;

// mara审核
@Component
public class ServiceOrderWaitNode extends SODecisionNode {
	
	// 文案,mara

	@Override
	public String getName() {
		return "WAIT";
	}

	@Override
	protected String decide(Context context) {
		isSingleStep = true;
		Map<String, Object> parameters = context.getParameters();
		return (String) parameters.get("state");
	}

}
