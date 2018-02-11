package org.zhinanzhen.b.controller;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zhinanzhen.b.service.SubagencyService;
import org.zhinanzhen.b.service.pojo.SubagencyDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

import com.ikasoa.core.utils.StringUtil;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/subagency")
public class SubagencyController extends BaseController {

	@Resource
	SubagencyService subagencyService;

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> addSubagency(@RequestParam(value = "name") String name,
			@RequestParam(value = "commissionRate") String commissionRate, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			SubagencyDTO subagencyDto = new SubagencyDTO();
			subagencyDto.setName(name);
			subagencyDto.setCommissionRate(Double.parseDouble(commissionRate.trim()));
			if (subagencyService.addSubagency(subagencyDto) > 0) {
				return new Response<Integer>(0, subagencyDto.getId());
			} else {
				return new Response<Integer>(1, "创建失败.", 0);
			}
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> updateSubagency(@RequestParam(value = "id") int id,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "commissionRate", required = false) String commissionRate, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			Double _commissionRate = null;
			if (StringUtil.isNotEmpty(commissionRate)) {
				_commissionRate = Double.parseDouble(commissionRate.trim());
			}
			int i = subagencyService.updateSubagency(name, _commissionRate);
			if (i > 0) {
				return new Response<Integer>(0, i);
			} else {
				return new Response<Integer>(1, "修改失败.", 0);
			}
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<SubagencyDTO>> listSubagency(@RequestParam(value = "keyword", required = false) String keyword,
			HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<List<SubagencyDTO>>(0, subagencyService.listSubagency(keyword));
		} catch (ServiceException e) {
			return new Response<List<SubagencyDTO>>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/get", method = RequestMethod.GET)
	@ResponseBody
	public Response<SubagencyDTO> getSubagency(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<SubagencyDTO>(0, subagencyService.getSubagencyById(id));
		} catch (ServiceException e) {
			return new Response<SubagencyDTO>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/delete", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> deleteSubagency(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, subagencyService.deleteSubagencyById(id));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), 0);
		}
	}

}
