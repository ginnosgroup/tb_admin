package org.zhinanzhen.b.controller.nodes;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.zhinanzhen.b.controller.OfficialController.OfficialWorkStateEnum;
import org.zhinanzhen.b.service.ExchangeRateService;
import org.zhinanzhen.b.service.ServiceOrderService;
import org.zhinanzhen.b.service.pojo.ExchangeRateDTO;
import org.zhinanzhen.b.service.pojo.OfficialDTO;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.RegionService;
import org.zhinanzhen.tb.service.ServiceException;

import com.ikasoa.core.utils.MapUtil;
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

	// 文案审核黑名单
	private static Map<Integer, List<String>> bOfficialReviewPermissions = buildPermissions(
			"7:1000003,1000005;9:1000003,1000005;10:1000003,1000005;11:1000003,1000005;14:1000003,1000005;19:1000003,1000005;1000106:1000003,1000005;1000125:1000003,1000005;1000127:1000003,1000005");

	// 文案审核白名单
	private static Map<Integer, List<String>> wOfficialReviewPermissions = buildPermissions(
			"22:1000003,1000005;26:1000003,1000005;1000020:1000003,1000005;1000026:1000003;1000039:1000003,1000005;1000040:1000003,1000005;1000041:1000003,1000005;1000068:1000003,1000005;1000104:1000003,1000005;19:1000034,1000040,1000041,1000002");

	private static Map<Integer, List<String>> buildPermissions(String value) {
		Map<Integer, List<String>> map = MapUtil.newHashMap();
		String[] _s1 = value.split(";");
		for (String s1 : _s1) {
			String[] _s2 = s1.split(":");
			if (_s2.length != 2)
				continue;
			map.put(Integer.parseInt(_s2[0]), Arrays.asList(_s2[1].split(",")));
		}
		return map;
	}
	
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
			OfficialDTO officialDto = serviceOrderDto.getOfficial();
			// 判断文案状态
			if (ObjectUtil.isNotNull(officialDto)
					&& OfficialWorkStateEnum.BUSY.name().equalsIgnoreCase(officialDto.getWorkState())) {
				context.putParameter("response",
						new Response<ServiceOrderDTO>(1, "你选择的文案已经设置为忙碌状态,请重新选择.", serviceOrderDto));
				return null;
			}
			// 判断文案服务项目匹配
			if (ObjectUtil.isNotNull(officialDto)) {
				int serviceId = serviceOrderDto.getServiceId();
				String officialIdStr = officialDto.getId() + "";
				List<String> blackList = bOfficialReviewPermissions.get(serviceId);
				if (ObjectUtil.isNotNull(blackList) && blackList.contains(officialIdStr)) {
					context.putParameter("response",
							new Response<ServiceOrderDTO>(1,
									StringUtil.merge("您选择的文案[", officialDto.getName(), "]暂时不能为该项目提供支持,请更换文案."),
									serviceOrderDto));
					return null;
				}
				List<String> whiteList = wOfficialReviewPermissions.get(serviceId);
				if (ObjectUtil.isNotNull(whiteList) && !whiteList.contains(officialIdStr)) {
					context.putParameter("response",
							new Response<ServiceOrderDTO>(1,
									StringUtil.merge("您选择的文案[", officialDto.getName(), "]暂时不能为该项目提供支持,请更换文案.."),
									serviceOrderDto));
					return null;
				}
			}
			// 提交审核时更新汇率
			if (exchangeRateService != null) {
				if (regionService.isCNByAdviserId(serviceOrderDto.getAdviserId())) { // 如果是中国地区则使用季度固定汇率
					double qRate = exchangeRateService.getQuarterExchangeRate();
					log.info(StringUtil.merge("为服务订单(", serviceOrderDto.getId(), ")设置季度固定汇率:", qRate));
					serviceOrderDto.setExchangeRate(qRate);
				} else {
					ExchangeRateDTO rate = exchangeRateService.getExchangeRate();
					if (ObjectUtil.isNotNull(rate) && rate.getRate() > 0)
						serviceOrderDto.setExchangeRate(rate.getRate());
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
