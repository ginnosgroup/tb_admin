package org.zhinanzhen.b.controller.nodes;

import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;
import org.zhinanzhen.b.service.RefundService;
import org.zhinanzhen.b.service.pojo.RefundDTO;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

import com.ikasoa.core.utils.StringUtil;
import com.ikasoa.web.workflow.Context;
import com.ikasoa.web.workflow.nodes.AbstractDecisionNode;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public abstract class RDecisionNode extends AbstractDecisionNode {

	protected final static String SUSPEND_NODE = "SuspendNode";

	@Resource
	protected RefundService refundService;

	@Override
	protected boolean saveNode(Context context) {
		try {
			RefundDTO refundDto = refundService.getRefundById(getRefundId(context));
			if (refundDto == null) {
				log.error("未找到服务订单:refundId=" + getRefundId(context));
				return false;
			}
			log.info("context:" + context);
			refundDto.setState(getName());
			if (context.getParameter("reason") != null) {
				refundDto.setReason(context.getParameter("reason").toString());
				log.info("写入reason:" + refundDto.getReason());
			}
			if (context.getParameter("paymentVoucherImageUrl") != null) {
				refundDto.setPaymentVoucherImageUrl(context.getParameter("paymentVoucherImageUrl").toString());
				log.info("写入paymentVoucherImageUrl:" + refundDto.getPaymentVoucherImageUrl());
			}
			if (context.getParameter("paymentVoucherImageUrl2") != null) {
				refundDto.setPaymentVoucherImageUrl2(context.getParameter("paymentVoucherImageUrl2").toString());
				log.info("写入paymentVoucherImageUrl2:" + refundDto.getPaymentVoucherImageUrl2());
			}
			if (context.getParameter("paymentVoucherImageUrl3") != null) {
				refundDto.setPaymentVoucherImageUrl3(context.getParameter("paymentVoucherImageUrl3").toString());
				log.info("写入paymentVoucherImageUrl3:" + refundDto.getPaymentVoucherImageUrl3());
			}
			if (context.getParameter("paymentVoucherImageUrl4") != null) {
				refundDto.setPaymentVoucherImageUrl4(context.getParameter("paymentVoucherImageUrl4").toString());
				log.info("写入paymentVoucherImageUrl4:" + refundDto.getPaymentVoucherImageUrl4());
			}
			if (context.getParameter("paymentVoucherImageUrl5") != null) {
				refundDto.setPaymentVoucherImageUrl5(context.getParameter("paymentVoucherImageUrl5").toString());
				log.info("写入paymentVoucherImageUrl5:" + refundDto.getPaymentVoucherImageUrl5());
			}
			if (context.getParameter("note") != null) {
				refundDto.setNote(context.getParameter("note").toString());
				log.info("写入note:" + refundDto.getNote());
			}
			if (refundService.updateRefund(refundDto) > 0)
				log.info("保存流程状态成功:refundId=" + refundDto.getId() + ",state=" + getName());
			else
				log.info("保存流程状态失败:refundId=" + refundDto.getId() + ",state=" + getName());
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

	protected int getRefundId(Context context) {
		Map<String, Object> parameters = context.getParameters();
		if (!parameters.containsKey("refundId")) {
			log.error("refundId不存在!");
			return 0;
		}
		return (Integer) parameters.get("refundId");
	}

	protected String getNextState(Context context) {
		String[] stateList = nextNodeNames();
		if (stateList == null)
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
