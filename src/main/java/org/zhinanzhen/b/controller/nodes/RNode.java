package org.zhinanzhen.b.controller.nodes;

import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;
import org.zhinanzhen.b.service.RefundService;
import org.zhinanzhen.b.service.pojo.RefundDTO;
import org.zhinanzhen.tb.service.ServiceException;

import com.ikasoa.core.utils.StringUtil;
import com.ikasoa.web.workflow.Context;
import com.ikasoa.web.workflow.nodes.AbstractNode;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public abstract class RNode extends AbstractNode {

	@Resource
	RefundService refundService;

	@Override
	protected boolean saveNode(Context context) {
		try {
			RefundDTO refundDto = refundService.getRefundById(getRefundId(context));
			if (refundDto == null) {
				log.error("未找到退款单:" + getRefundId(context));
				return false;
			}
			refundDto.setState(getName());
			if (context.getParameter("reason") != null) {
				refundDto.setReason(context.getParameter("reason").toString());
				log.info("写入reason:" + refundDto.getReason());
			}
			if (context.getParameter("reason") != null) {
				refundDto.setReason(context.getParameter("reason").toString());
				log.info("写入reason:" + refundDto.getReason());
			}
			if (context.getParameter("paymentVoucherImageUrl") != null) {
				refundDto.setPaymentVoucherImageUrl(context.getParameter("paymentVoucherImageUrl").toString());
				log.info("写入paymentVoucherImageUrl:" + refundDto.getPaymentVoucherImageUrl());
			}
			if (refundService.updateRefund(refundDto) > 0)
				log.info("保存流程状态成功:serviceOrderId=" + refundDto.getId() + ",state=" + getName());
			else
				log.info("保存流程状态失败:serviceOrderId=" + refundDto.getId() + ",state=" + getName());
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

	protected int getRefundId(Context context) {
		Map<String, Object> parameters = context.getParameters();
		if (!parameters.containsKey("refundId")) {
			log.error("refundId不存在!");
			return 0;
		}
		return StringUtil.toInt(parameters.get("refundId").toString());
	}

}
