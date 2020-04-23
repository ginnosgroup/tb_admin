package org.zhinanzhen.b.controller;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zhinanzhen.b.service.DashboardService;
import org.zhinanzhen.b.service.pojo.CommissionOrderDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/dashboard")
public class DashboardController extends BaseController {

	@Resource
	DashboardService dashboardService;

	@RequestMapping(value = "/getMonthExpectAmount", method = RequestMethod.GET)
	@ResponseBody
	public Response<Double> getMonthExpectAmount(HttpServletRequest request, HttpServletResponse response) {
		try {
			return new Response<Double>(0, dashboardService.getThisMonthExpectAmount(getAdviserId(request)));
		} catch (ServiceException e) {
			return new Response<Double>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/listThisMonthCommissionOrder", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<CommissionOrderDTO>> listThisMonthCommissionOrder(HttpServletRequest request,
			HttpServletResponse response) {
		try {
			return new Response<List<CommissionOrderDTO>>(0, dashboardService.listThisMonthCommissionOrder());
		} catch (ServiceException e) {
			return new Response<List<CommissionOrderDTO>>(e.getCode(), e.getMessage(), null);
		}
	}

}
