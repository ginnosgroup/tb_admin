package org.zhinanzhen.b.controller;

import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;
import org.zhinanzhen.b.service.ServiceOrderService;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

import com.ikasoa.web.workflow.Context;
import com.ikasoa.web.workflow.nodes.AbstractDecisionNode;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public abstract class SODecisionNode extends AbstractDecisionNode {

	protected final static String SUSPEND_NODE = "SuspendNode";

	@Resource
	ServiceOrderService serviceOrderService;

	@Override
	protected boolean saveNode(Context context) {
		try {
			ServiceOrderDTO serviceOrderDto = serviceOrderService.getServiceOrderById(getServiceOrderId(context));
			if (serviceOrderDto == null) {
				log.error("未找到服务订单:serviceOrderId=" + getServiceOrderId(context));
				return false;
			}
			String type = serviceOrderDto.getType();
			if ("VISA".equals(type)) {// 签证
				if ("PAID".equals(getName())) {
					log.error("签证服务订单没有COE支付流程!");
					return false;
				}
			} else if ("OVST".equals(type)) {// 留学
				if ("WAIT".equals(getName())) {
					log.error("留学服务订单没有MARA审核流程!");
					return false;
				}
			} else if ("SIV".equals(type)) { // 独立技术移民
			} else {
				log.error("服务类型错误:serviceOrderId=" + serviceOrderDto.getId() + ",type=" + type);
				return false;
			}
			serviceOrderDto.setState(getName());
			if (serviceOrderService.updateServiceOrder(serviceOrderDto) > 0)
				log.info("保存流程状态成功:serviceOrderId=" + serviceOrderDto.getId() + ",state=" + getName());
			else
				log.info("保存流程状态失败:serviceOrderId=" + serviceOrderDto.getId() + ",state=" + getName());
		} catch (ServiceException e) {
			log.error("流程节点更新失败:" + e.getMessage());
			return false;
		}
		return true;
	}

	protected String getAp(Context context) {
		Map<String, Object> parameters = context.getParameters();
		if (!parameters.containsKey("ap")) {
			log.error("ap不存在!");
			return null;
		}
		return (String) parameters.get("ap");
	}

	protected int getServiceOrderId(Context context) {
		Map<String, Object> parameters = context.getParameters();
		if (!parameters.containsKey("serviceOrderId")) {
			log.error("serviceOrderId不存在!");
			return 0;
		}
		return (Integer) parameters.get("serviceOrderId");
	}

	protected String getState(Context context, String... stateList) {
		Map<String, Object> parameters = context.getParameters();
		if (parameters.containsKey("state")) {
			String state = getState((String) parameters.get("state"), stateList);
			if (state != null)
				return state;
			else {
				context.putParameter("response", new Response<ServiceOrderDTO>(1, "无效的状态参数:" + state, null));
				return null;
			}
		} else {
			context.putParameter("response", new Response<ServiceOrderDTO>(1, "状态参数不存在!", null));
			return null;
		}
	}

	private String getState(String state, String... stateList) {
		for (String s : stateList) {
			if (s.equals(state))
				return state;
		}
		return null;
	}

}
