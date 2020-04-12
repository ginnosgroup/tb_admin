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

}
