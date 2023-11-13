package org.zhinanzhen.b.controller;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zhinanzhen.b.service.ExchangeRateService;
import org.zhinanzhen.b.service.ServiceOrderService;
import org.zhinanzhen.b.service.pojo.ExchangeRateDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.service.ServiceException;

import com.ikasoa.core.utils.ObjectUtil;
import com.ikasoa.core.utils.StringUtil;

public class BaseCommissionOrderController extends BaseController {
	
	private static final Logger LOG = LoggerFactory.getLogger(BaseCommissionOrderController.class);

	@Resource
	protected ServiceOrderService serviceOrderService;
	
	@Resource
	ExchangeRateService exchangeRateService;

	public enum ReviewKjStateEnum {
		PENDING, WAIT, REVIEW, FINISH, COMPLETE, CLOSE;
		@Resource
		ExchangeRateService exchangeRateService;
		public static ReviewKjStateEnum get(String name) {
			for (ReviewKjStateEnum e : ReviewKjStateEnum.values())
				if (e.toString().equals(name))
					return e;
			return null;
		}
	}

	public enum CommissionStateEnum {
		DJY, YJY, DZY, YZY;

		public static CommissionStateEnum get(String name) {
			for (CommissionStateEnum e : CommissionStateEnum.values())
				if (e.toString().equals(name))
					return e;
			return null;
		}
	}

	protected String getKjStateStr(String state) {
		if ("DJY".equalsIgnoreCase(state))
			return "待结佣";
		if ("YJY".equalsIgnoreCase(state))
			return "已结佣";
		if ("DZY".equalsIgnoreCase(state))
			return "待追佣";
		if ("YZY".equalsIgnoreCase(state))
			return "已追佣";
		return "";
	}
	
	protected String getStateStr(String state) {
		if ("REVIEW".equalsIgnoreCase(state))
			return "待结佣";
		if ("WAIT".equalsIgnoreCase(state))
			return "已驳回";
		if ("FINISH".equalsIgnoreCase(state))
			return "已审核";
		if ("COMPLETE".equalsIgnoreCase(state))
			return "已结佣";
		if ("CLOSE".equalsIgnoreCase(state))
			return "已关闭";
		return "";
	}
	
	protected Double getRate(String name, int id, boolean isCN) throws ServiceException {
		// 提交审核时更新汇率
		if (isCN) {
			double qRate = exchangeRateService.getQuarterExchangeRate();
			LOG.info(StringUtil.merge("为", name, "佣金订单(", id, ")设置季度固定汇率:", qRate));
			return qRate;
		} else {
			if (exchangeRateService != null) {
				ExchangeRateDTO rate = exchangeRateService.getExchangeRate();
				if (ObjectUtil.isNotNull(rate) && rate.getRate() > 0)
					return rate.getRate();
			}
			return null;
		}
	}

}
