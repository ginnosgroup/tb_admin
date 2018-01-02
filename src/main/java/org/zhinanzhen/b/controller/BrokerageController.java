package org.zhinanzhen.b.controller;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zhinanzhen.b.service.BrokerageService;
import org.zhinanzhen.b.service.pojo.BrokerageDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/brokerage")
public class BrokerageController extends BaseController {

	@Resource
	BrokerageService brokerageService;

	@RequestMapping(value = "/count", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> countBrokerage(@RequestParam(value = "stardDate", required = false) String stardDate,
			@RequestParam(value = "endDate", required = false) String endDate,
			@RequestParam(value = "adviserId", required = false) Integer adviserId, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, brokerageService.countBrokerage(stardDate, endDate, adviserId));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<BrokerageDTO>> listBrokerage(
			@RequestParam(value = "stardDate", required = false) String stardDate,
			@RequestParam(value = "endDate", required = false) String endDate,
			@RequestParam(value = "adviserId", required = false) Integer adviserId,
			@RequestParam(value = "pageNum") int pageNum, @RequestParam(value = "pageSize") int pageSize,
			HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<List<BrokerageDTO>>(0,
					brokerageService.listBrokerage(stardDate, endDate, adviserId, pageNum, pageSize));
		} catch (ServiceException e) {
			return new Response<List<BrokerageDTO>>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/get", method = RequestMethod.GET)
	@ResponseBody
	public Response<BrokerageDTO> getBrokerage(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<BrokerageDTO>(0, brokerageService.getBrokerageById(id));
		} catch (ServiceException e) {
			return new Response<BrokerageDTO>(1, e.getMessage(), null);
		}
	}

}
