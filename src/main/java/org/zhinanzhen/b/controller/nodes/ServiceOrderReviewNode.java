package org.zhinanzhen.b.controller.nodes;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;
import org.zhinanzhen.b.controller.OfficialController.OfficialWorkStateEnum;
import org.zhinanzhen.b.dao.OfficialReviewRuleDAO;
import org.zhinanzhen.b.dao.pojo.OfficialReviewRuleDO;
import org.zhinanzhen.b.service.ExchangeRateService;
import org.zhinanzhen.b.service.ServiceOrderManageService;
import org.zhinanzhen.b.service.ServiceOrderService;
import org.zhinanzhen.b.service.pojo.ExchangeRateDTO;
import org.zhinanzhen.b.service.pojo.OfficialDTO;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.RegionService;
import org.zhinanzhen.tb.service.ServiceException;

import com.ikasoa.core.utils.ObjectUtil;
import com.ikasoa.core.utils.StringUtil;
import com.ikasoa.web.workflow.Context;

import lombok.extern.slf4j.Slf4j;

// 文案审核
@Component
@Slf4j
public class ServiceOrderReviewNode extends SODecisionNode {
	
	@Resource
	ExchangeRateService exchangeRateService;
	
	@Resource
	RegionService regionService;

	private final OfficialReviewRuleDAO officialReviewRuleDAO;

	public ServiceOrderReviewNode(ServiceOrderService serviceOrderService,
			ServiceOrderManageService serviceOrderManageService, OfficialReviewRuleDAO officialReviewRuleDAO) {
		super.serviceOrderService = serviceOrderService;
		super.serviceOrderManageService = serviceOrderManageService;
		this.officialReviewRuleDAO = officialReviewRuleDAO;
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
			ServiceOrderDTO serviceOrderDtoT = new ServiceOrderDTO();
			long l = System.currentTimeMillis();
			ServiceOrderDTO serviceOrderDto = serviceOrderService.getServiceOrderById(getServiceOrderId(context));
			ServiceOrderDTO serviceorderManageDto = serviceOrderManageService.getServiceOrderById(getServiceOrderId(context));
			log.info("两个获取服务订单耗时----------------" + (System.currentTimeMillis() - l));
			if (serviceOrderDto == null && serviceorderManageDto == null) {
				context.putParameter("response",
						new Response<ServiceOrderDTO>(1, "服务订单不存在:" + getServiceOrderId(context), null));
				return null;
			}
			serviceOrderDtoT = serviceOrderDto != null ? serviceOrderDto : serviceorderManageDto;
			if (serviceOrderDtoT.getParentId() == 0 && ("SIV".equalsIgnoreCase(serviceOrderDtoT.getType())
					|| "NSV".equalsIgnoreCase(serviceOrderDtoT.getType())
					|| "MT".equalsIgnoreCase(serviceOrderDtoT.getType()))) {
				context.putParameter("response", new Response<ServiceOrderDTO>(1, "该订单不支持审核.", serviceOrderDto));
				return null;
			}
			OfficialDTO officialDto = serviceOrderDtoT.getOfficial();
			// 判断文案状态
			if (ObjectUtil.isNotNull(officialDto)
					&& OfficialWorkStateEnum.BUSY.name().equalsIgnoreCase(officialDto.getWorkState())) {
				context.putParameter("response",
						new Response<ServiceOrderDTO>(1, "你选择的文案已经设置为忙碌状态,请重新选择.", serviceOrderDtoT));
				return null;
			}
			// 判断文案服务项目匹配
			if (ObjectUtil.isNotNull(officialDto)) {
				int serviceId = serviceOrderDtoT.getServiceId();
				OfficialReviewRuleDO reviewRule = officialReviewRuleDAO
						.getByOfficialIdAndServiceId(officialDto.getId(), serviceId);
				if (reviewRule != null && Integer.valueOf(0).equals(reviewRule.getCanAccept())) {
					context.putParameter("response",
							new Response<ServiceOrderDTO>(1,
									StringUtil.merge("您选择的文案[", officialDto.getName(), "]暂时不能为该项目提供支持,请更换文案."),
									serviceOrderDtoT));
					return null;
				}
				if (reviewRule != null && reviewRule.getMinReceivable() != null
						&& serviceOrderDtoT.getReceivable() <= reviewRule.getMinReceivable()) {
					context.putParameter("response",
							new Response<ServiceOrderDTO>(1,
									StringUtil.merge("您选择的文案[", officialDto.getName(), "]暂时不能为该项目提供支持,请更换文案."),
									serviceOrderDtoT));
					return null;
				}
			}
			// 提交审核时更新汇率
			if (exchangeRateService != null) {
				if (regionService.isCNByAdviserId(serviceOrderDtoT.getAdviserId())) { // 如果是中国地区则使用季度固定汇率
					double qRate = exchangeRateService.getQuarterExchangeRate();
					log.info(StringUtil.merge("为服务订单(", serviceOrderDtoT.getId(), ")设置季度固定汇率:", qRate));
					serviceOrderDtoT.setExchangeRate(qRate);
				} else {
					ExchangeRateDTO rate = exchangeRateService.getExchangeRate();
					if (ObjectUtil.isNotNull(rate) && rate.getRate() > 0)
						serviceOrderDtoT.setExchangeRate(rate.getRate());
				}
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
