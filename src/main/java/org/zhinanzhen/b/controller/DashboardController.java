package org.zhinanzhen.b.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zhinanzhen.b.service.DashboardService;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/dashboard")
public class DashboardController {

	@Resource
	DashboardService dashboardService;

	@RequestMapping(value = "/getMonthExpectAmount", method = RequestMethod.GET)
	@ResponseBody
	public Response<Double> getMonthExpectAmount(HttpServletRequest request, HttpServletResponse response) {
		try {
			return new Response<Double>(0, dashboardService.getThisMonthExpectAmount());
		} catch (ServiceException e) {
			return new Response<Double>(e.getCode(), e.getMessage(), null);
		}
	}

}
