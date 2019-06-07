package org.zhinanzhen.b.controller;

import java.util.Date;
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
import org.zhinanzhen.b.service.AbleStateEnum;
import org.zhinanzhen.b.service.VisaRemindService;
import org.zhinanzhen.b.service.VisaService;
import org.zhinanzhen.b.service.pojo.VisaRemindDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

import com.ikasoa.core.utils.StringUtil;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/visaRemind")
public class VisaRemindController extends BaseController {

	@Resource
	VisaRemindService visaRemindService;

	@Resource
	VisaService visaService;

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> addRemind(@RequestParam(value = "remindDate") String remindDate,
			@RequestParam(value = "visaId") String visaId, HttpServletRequest request, HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			VisaRemindDTO visaRemindDto = new VisaRemindDTO();
			visaRemindDto.setRemindDate(new Date(Long.parseLong(remindDate)));
			visaRemindDto.setVisaId(StringUtil.toInt(visaId));
			if (visaRemindService.addRemind(visaRemindDto) > 0) {
				return new Response<Integer>(0, visaRemindDto.getId());
			} else {
				return new Response<Integer>(1, "创建失败.", 0);
			}
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<VisaRemindDTO>> listRemind(@RequestParam(value = "visaId") String visaId,
			@RequestParam(value = "adviserId", required = false) String adviserId,
			@RequestParam(value = "state", required = false) String state, HttpServletRequest request,
			HttpServletResponse response) {
		// 更改当前顾问编号
		Integer newAdviserId = getAdviserId(request);
		if (newAdviserId != null)
			adviserId = newAdviserId + "";
		try {
			super.setGetHeader(response);
			List<VisaRemindDTO> visaRemindList = visaRemindService.listRemindByVisaId(Integer.parseInt(visaId),
					StringUtil.isNotEmpty(adviserId) ? StringUtil.toInt(adviserId) : 0, AbleStateEnum.get(state));
			visaRemindList.forEach(vr -> {
				try {
					vr.setVisa(visaService.getVisaById(vr.getVisaId()));
				} catch (ServiceException e) {
					vr.setVisa(null);
				}
			});
			return new Response<List<VisaRemindDTO>>(0, visaRemindList);
		} catch (ServiceException e) {
			return new Response<List<VisaRemindDTO>>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/listByDate", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<VisaRemindDTO>> listRemindByDate(@RequestParam(value = "date", required = false) String date,
			@RequestParam(value = "adviserId", required = false) String adviserId, HttpServletRequest request,
			HttpServletResponse response) {
		// 更改当前顾问编号
		Integer newAdviserId = getAdviserId(request);
		if (newAdviserId != null)
			adviserId = newAdviserId + "";
		try {
			super.setGetHeader(response);
			List<VisaRemindDTO> visaRemindList = visaRemindService.listRemindByRemindDate(
					new Date(Long.parseLong(date)), StringUtil.isNotEmpty(adviserId) ? StringUtil.toInt(adviserId) : 0);
			visaRemindList.forEach(vr -> {
				try {
					vr.setVisa(visaService.getVisaById(vr.getVisaId()));
				} catch (ServiceException e) {
					vr.setVisa(null);
				}
			});
			return new Response<List<VisaRemindDTO>>(0, visaRemindList);
		} catch (ServiceException e) {
			return new Response<List<VisaRemindDTO>>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/updateStateByVisaId", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> updateStateByVisaId(@RequestParam(value = "visaId") int visaId,
			@RequestParam(value = "state", required = false) String state, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, visaRemindService.updateStateByVisaId(visaId, AbleStateEnum.get(state)));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/deleteRemindByVisaId", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> deleteRemindByVisaId(@RequestParam(value = "visaId") int visaId,
			HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, visaRemindService.deleteRemindByVisaId(visaId));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), 0);
		}
	}

}
