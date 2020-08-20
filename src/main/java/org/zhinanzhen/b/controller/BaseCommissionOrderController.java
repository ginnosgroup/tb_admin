package org.zhinanzhen.b.controller;

import javax.annotation.Resource;

import org.zhinanzhen.b.service.ServiceOrderService;
import org.zhinanzhen.tb.controller.BaseController;

public class BaseCommissionOrderController extends BaseController {

	@Resource
	protected ServiceOrderService serviceOrderService;

	public enum ReviewKjStateEnum {
		PENDING, WAIT, REVIEW, FINISH, COMPLETE, CLOSE;

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

}
