package org.zhinanzhen.b.controller.nodes;

import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;
import org.zhinanzhen.b.service.ServiceOrderService;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.tb.service.ServiceException;

import com.ikasoa.core.utils.StringUtil;
import com.ikasoa.web.workflow.Context;
import com.ikasoa.web.workflow.nodes.AbstractNode;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public abstract class SONode extends AbstractNode {

	@Resource
	ServiceOrderService serviceOrderService;

	@Override
	protected boolean saveNode(Context context) {
		try {
			ServiceOrderDTO serviceOrderDto = serviceOrderService.getServiceOrderById(getServiceOrderId(context));
			if (serviceOrderDto == null) {
				log.error("未找到服务订单:" + getServiceOrderId(context));
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
			serviceOrderService.updateServiceOrder(serviceOrderDto);
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

	protected int getServiceOrderId(Context context) {
		Map<String, Object> parameters = context.getParameters();
		if (!parameters.containsKey("serviceOrder")) {
			log.error("serviceOrder不存在!");
			return 0;
		}
		return StringUtil.toInt((String) parameters.get("serviceOrderId"));
	}

	protected String getClosedReason(Context context) {
		Map<String, Object> parameters = context.getParameters();
		if (!parameters.containsKey("closedReason")) {
			log.warn("closedReason不存在!");
			return null;
		}
		return (String) parameters.get("closedReason");
	}

}
