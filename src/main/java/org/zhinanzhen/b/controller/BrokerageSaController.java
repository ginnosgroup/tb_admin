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
import org.zhinanzhen.b.service.BrokerageSaService;
import org.zhinanzhen.b.service.pojo.BrokerageSaDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/brokerage_sa")
public class BrokerageSaController extends BaseController {

	@Resource
	BrokerageSaService brokerageSaService;

	@RequestMapping(value = "/count", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> countBrokerage(@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "startCreateDate", required = false) String startCreateDate,
			@RequestParam(value = "endCreateDate", required = false) String endCreateDate,
			@RequestParam(value = "startHandlingDate", required = false) String startHandlingDate,
			@RequestParam(value = "endHandlingDate", required = false) String endHandlingDate,
			@RequestParam(value = "adviserId", required = false) Integer adviserId,
			@RequestParam(value = "schoolId", required = false) Integer schoolId, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, brokerageSaService.countBrokerageSa(keyword, startCreateDate, endCreateDate,
					startHandlingDate, endHandlingDate, adviserId, schoolId));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<BrokerageSaDTO>> listBrokerage(
			@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "startCreateDate", required = false) String startCreateDate,
			@RequestParam(value = "endCreateDate", required = false) String endCreateDate,
			@RequestParam(value = "startHandlingDate", required = false) String startHandlingDate,
			@RequestParam(value = "endHandlingDate", required = false) String endHandlingDate,
			@RequestParam(value = "adviserId", required = false) Integer adviserId,
			@RequestParam(value = "schoolId", required = false) Integer schoolId,
			@RequestParam(value = "pageNum") int pageNum, @RequestParam(value = "pageSize") int pageSize,
			HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<List<BrokerageSaDTO>>(0, brokerageSaService.listBrokerageSa(keyword, startCreateDate,
					endCreateDate, startHandlingDate, endHandlingDate, adviserId, schoolId, pageNum, pageSize));
		} catch (ServiceException e) {
			return new Response<List<BrokerageSaDTO>>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/get", method = RequestMethod.GET)
	@ResponseBody
	public Response<BrokerageSaDTO> getBrokerageSa(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<BrokerageSaDTO>(0, brokerageSaService.getBrokerageSaById(id));
		} catch (ServiceException e) {
			return new Response<BrokerageSaDTO>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/close", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> closeBrokerageSa(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			BrokerageSaDTO brokerageSaDto = brokerageSaService.getBrokerageSaById(id);
			brokerageSaDto.setClose(true);
			return new Response<Integer>(0, brokerageSaService.updateBrokerageSa(brokerageSaDto));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/reopen", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> reopenBrokerageSa(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			BrokerageSaDTO brokerageSaDto = brokerageSaService.getBrokerageSaById(id);
			brokerageSaDto.setClose(false);
			return new Response<Integer>(0, brokerageSaService.updateBrokerageSa(brokerageSaDto));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/delete", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> deleteBrokerageSa(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, brokerageSaService.deleteBrokerageSaById(id));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), 0);
		}
	}

}
