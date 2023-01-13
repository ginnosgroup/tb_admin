package org.zhinanzhen.b.controller.nodes;

import java.util.Date;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;
import org.zhinanzhen.b.service.ServiceOrderService;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

import com.ikasoa.core.utils.StringUtil;
import com.ikasoa.web.workflow.Context;
import com.ikasoa.web.workflow.nodes.AbstractDecisionNode;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public abstract class SODecisionNode extends AbstractDecisionNode {

	protected final static String SUSPEND_NODE = "SuspendNode";

	@Resource
	protected ServiceOrderService serviceOrderService;

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
			} else if ("NSV".equals(type)) {
			} else if ("ZX".equals(type)) { // 咨询服务
				if (!"REVIEW,COMPLETE".contains(getName())){
					log.error("咨询服务订单没有此文案审核流程!");
					return false;
				}
			} else {
				log.error("服务类型错误:serviceOrderId=" + serviceOrderDto.getId() + ",type=" + type);
				return false;
			}
			log.info("context:" + context);
			serviceOrderDto.setState(getName());
			if (context.getParameter("refuseReason") != null) {
				serviceOrderDto.setRefuseReason(context.getParameter("refuseReason").toString());
				log.info("写入refuseReason:" + serviceOrderDto.getRefuseReason());
			}
			if (context.getParameter("closedReason") != null) {
				serviceOrderDto.setClosedReason(context.getParameter("closedReason").toString());
				log.info("写入closedReason:" + serviceOrderDto.getClosedReason());
			}
			if (context.getParameter("remarks") != null) {
				serviceOrderDto.setRemarks(context.getParameter("remarks").toString());
				log.info("写入remarks:" + serviceOrderDto.getRemarks());
			}
			if (context.getParameter("stateMark") != null) {
				serviceOrderDto.setStateMark(context.getParameter("stateMark").toString());
				log.info("写入stateMark:" + serviceOrderDto.getStateMark());
			}
			if (context.getParameter("stateMark2") != null) {
				serviceOrderDto.setStateMark2(context.getParameter("stateMark2").toString());
				log.info("写入stateMark2:" + serviceOrderDto.getStateMark2());
			}
			if ("APPLY,COMPLETE".contains(getName())){
				if (serviceOrderDto.getReadcommittedDate() == null)
					serviceOrderDto.setReadcommittedDate(new Date());
			}
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
	
	@Override
	public String[] nextNodeNames() {
		return null;
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

	protected String getNextState(Context context) {
		String[] stateList = nextNodeNames();
		if(stateList == null)
			return null;
		Map<String, Object> parameters = context.getParameters();
		if (parameters.containsKey("state")) {
			String state = getState((String) parameters.get("state"), stateList);
			if (state != null)
				return state;
			else {
				String states = "";
				for (String s : stateList)
					states += StringUtil.merge(s, "/");
				context.putParameter("response", new Response<ServiceOrderDTO>(1,
						StringUtil.merge("无效的状态参数!(可选状态为:" + states + ",但状态参数为:" + parameters.get("state")), null));
				return null;
			}
		} else {
			context.putParameter("response", new Response<ServiceOrderDTO>(1, "状态参数不存在!", null));
			return null;
		}
	}

	protected String getType(Context context) {
		Map<String, Object> parameters = context.getParameters();
		if (parameters.containsKey("type"))
			return (String) parameters.get("type");
		else {
			context.putParameter("response", new Response<ServiceOrderDTO>(1, "类型参数不存在!", null));
			return null;
		}
	}

	private String getState(String state, String... stateList) {
		for (String s : stateList) {
			if (s.equals(state))
				return state;
		}
		log.warn("状态未注册:" + state);
		return null;
	}

}
