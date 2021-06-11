package org.zhinanzhen.b.controller;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.zhinanzhen.b.service.CommissionOrderService;
import org.zhinanzhen.b.service.DashboardService;
import org.zhinanzhen.b.service.ServiceOrderService;
import org.zhinanzhen.b.service.pojo.CommissionOrderListDTO;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/dashboard")
public class DashboardController extends BaseController {

	@Resource
	DashboardService dashboardService;

	@Resource
	CommissionOrderService commissionOrderService;

	@Resource
	private ServiceOrderService serviceOrderService;

	@RequestMapping(value = "/getMonthExpectAmount", method = RequestMethod.GET)
	@ResponseBody
	public Response<Double> getMonthExpectAmount(HttpServletRequest request, HttpServletResponse response) {
		try {
			if (getAdminUserLoginInfo(request) == null)
				return new Response<Double>(1, "请先登录!", null);
			return new Response<Double>(0, dashboardService.getThisMonthExpectAmount(getAdviserId(request)));
		} catch (ServiceException e) {
			return new Response<Double>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/listThisMonthCommissionOrder", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<CommissionOrderListDTO>> listThisMonthCommissionOrder(HttpServletRequest request,
			HttpServletResponse response) {
		try {
			if (getAdminUserLoginInfo(request) == null)
				return new Response<List<CommissionOrderListDTO>>(1, "请先登录!", null);
			return new Response<List<CommissionOrderListDTO>>(0,
					commissionOrderService.listThisMonthCommissionOrder(getAdviserId(request), getOfficialId(request)));
		} catch (ServiceException e) {
			return new Response<List<CommissionOrderListDTO>>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/NotReviewedServiceOrder",method = RequestMethod.GET)
	@ResponseBody
	public  Response<List<ServiceOrderDTO>> NotReviewedServiceOrder(
			@RequestParam(value = "thisMonth",required = false)boolean thisMonth,
			@RequestParam(value = "officialId",required = false)Integer officialId,
			HttpServletRequest request){
		if (getAdminUserLoginInfo(request) == null)
			return  new Response(1,"先登录！");
		Integer _OfficialId = getOfficialId(request);
		if (_OfficialId != null){
			if (getOfficialAdminId(request) == null) //不是文案管理员返回null
				officialId = _OfficialId; //不是文案管理员则显示自己的服务订单
		}
		return  new Response(0 , serviceOrderService.NotReviewedServiceOrder(officialId,thisMonth));
	}

	@GetMapping(value = "/caseCount")
	@ResponseBody
	public Response<Integer> caseCount(
			@RequestParam(value = "officialId")Integer officialId,
			@RequestParam(value = "days",required = false)String Days,
			@RequestParam(value = "state",required = false)String state,
			HttpServletRequest request){
		if (getAdminUserLoginInfo(request) == null)
			return  new Response(1,"先登录！");
		Integer _officialId = getOfficialId(request);
		if (_officialId != null)
			officialId = _officialId;
		return  new Response<>(0,serviceOrderService.caseCount(officialId,Days,state));
	}


}
