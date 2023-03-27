package org.zhinanzhen.b.controller.nodes;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;
import org.zhinanzhen.b.controller.OfficialController.OfficialWorkStateEnum;
import org.zhinanzhen.b.service.ExchangeRateService;
import org.zhinanzhen.b.service.ServiceOrderService;
import org.zhinanzhen.b.service.pojo.ExchangeRateDTO;
import org.zhinanzhen.b.service.pojo.OfficialDTO;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

import com.ikasoa.core.utils.ObjectUtil;
import com.ikasoa.core.utils.StringUtil;
import com.ikasoa.web.workflow.Context;

// 文案审核
@Component
public class ServiceOrderReviewNode extends SODecisionNode {
	
	@Resource
	ExchangeRateService exchangeRateService;

	// 顾问,文案
	
	public ServiceOrderReviewNode(ServiceOrderService serviceOrderService) {
		super.serviceOrderService = serviceOrderService;
	}

	@Override
	public String getName() {
		return "REVIEW";
	}

	@Override
	protected String decide(Context context) {
		isSingleStep = true;
//		if (!"GW".equalsIgnoreCase(getAp(context)) ) {
//			context.putParameter("response", new Response<ServiceOrderDTO>(1, "仅限顾问操作!文案审核请传'OREVIEW'.", null));
//			return SUSPEND_NODE;
//		}
		try {
			ServiceOrderDTO serviceOrderDto = serviceOrderService.getServiceOrderById(getServiceOrderId(context));
			if (serviceOrderDto == null) {
				context.putParameter("response",
						new Response<ServiceOrderDTO>(1, "服务订单不存在:" + getServiceOrderId(context), null));
				return null;
			}
			if (serviceOrderDto.getParentId() == 0 && ("SIV".equalsIgnoreCase(serviceOrderDto.getType())
					|| "NSV".equalsIgnoreCase(serviceOrderDto.getType())
					|| "MT".equalsIgnoreCase(serviceOrderDto.getType()))) {
				context.putParameter("response", new Response<ServiceOrderDTO>(1, "该订单不支持审核.", serviceOrderDto));
				return null;
			}
			// 判断文案状态
			OfficialDTO officialDto = serviceOrderDto.getOfficial();
			if (ObjectUtil.isNotNull(officialDto)
					&& OfficialWorkStateEnum.BUSY.name().equalsIgnoreCase(officialDto.getWorkState())) {
				context.putParameter("response",
						new Response<ServiceOrderDTO>(1, "你选择的文案已经设置为忙碌状态,请重新勋章.", serviceOrderDto));
				return null;
			}
			// 提交审核时更新汇率
			if (exchangeRateService != null) {
				ExchangeRateDTO rate = exchangeRateService.getExchangeRate();
				if (ObjectUtil.isNotNull(rate) && rate.getRate() > 0)
					serviceOrderDto.setExchangeRate(rate.getRate());
			}
		} catch (ServiceException e) {
			context.putParameter("response", new Response<ServiceOrderDTO>(1, "服务订单执行异常:" + e.getMessage(), null));
			return null;
		}
//		return SUSPEND_NODE;
		String state = getNextState(context);
		if ("WAIT".equalsIgnoreCase(state) && !"VISA".equalsIgnoreCase(getType(context))) {
			context.putParameter("response", new Response<ServiceOrderDTO>(1, "只有签证类才能进行mara审核流程.", null));
			return null;
		}
		
		if (state == null && context.getParameter("state") == null) {
			context.putParameter("response", new Response<ServiceOrderDTO>(1, "状态值不能为空.", null));
			return null;
		}
		
		return state;
	}
	
	@Override
	public String[] nextNodeNames() {
		return new String[]{"REVIEW","PENDING", "OREVIEW", "WAIT", "COMPLETE", "PAID", "APPLY", "CLOSE", "FINISH", "APPLY_FAILED"}; // "PENDING"是驳回
	}

}
