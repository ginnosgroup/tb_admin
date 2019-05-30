package org.zhinanzhen.tb.controller;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zhinanzhen.b.service.AbleStateEnum;
import org.zhinanzhen.tb.service.ConsultationService;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.pojo.ConsultationDTO;

import com.ikasoa.core.utils.StringUtil;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/controller")
public class ConsultationController extends BaseController {

	@Resource
	ConsultationService consultationService;

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> addConsultation(@RequestParam(value = "userId") String userId,
			@RequestParam(value = "contents") String contents,
			@RequestParam(value = "state", required = false) String state,
			@RequestParam(value = "remindDate", required = false) String remindDate,
			@RequestParam(value = "remindContents", required = false) String remindContents, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			ConsultationDTO consultationDto = new ConsultationDTO();
			consultationDto.setUserId(StringUtil.toInt(userId));
			consultationDto.setContents(contents);
			if (StringUtils.isNotEmpty(state))
				consultationDto.setState(AbleStateEnum.get(state));
			if (StringUtils.isNotEmpty(remindDate))
				consultationDto.setRemindDate(new Date(Long.parseLong(remindDate)));
			if (StringUtils.isNotEmpty(remindContents))
				consultationDto.setRemindContents(remindContents);
			if (consultationService.addConsultation(consultationDto) > 0) {
				return new Response<Integer>(0, consultationDto.getId());
			} else {
				return new Response<Integer>(1, "创建失败.", 0);
			}
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	@ResponseBody
	public Response<ConsultationDTO> updateConsultation(@RequestParam(value = "id") int id,
			@RequestParam(value = "state", required = false) String state,
			@RequestParam(value = "remindDate", required = false) String remindDate,
			@RequestParam(value = "remindContents", required = false) String remindContents, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			ConsultationDTO consultationDto = new ConsultationDTO();
			consultationDto.setId(id);
			if (StringUtils.isNotEmpty(state))
				consultationDto.setState(AbleStateEnum.get(state));
			if (StringUtil.isNotEmpty(remindDate)) {
				consultationDto.setRemindDate(new Date(Long.parseLong(remindDate)));
			}
			if (StringUtil.isNotEmpty(remindContents)) {
				consultationDto.setRemindContents(remindContents);
			}
			if (consultationService.updateConsultation(consultationDto) > 0) {
				return new Response<ConsultationDTO>(0, consultationDto);
			} else {
				return new Response<ConsultationDTO>(0, "修改失败.", null);
			}
		} catch (ServiceException e) {
			return new Response<ConsultationDTO>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<ConsultationDTO>> listConsultation(
			@RequestParam(value = "userId", required = false) String userId, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<List<ConsultationDTO>>(0,
					consultationService.listConsultationByUserId(Integer.parseInt(userId)));
		} catch (ServiceException e) {
			return new Response<List<ConsultationDTO>>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/listByDate", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<ConsultationDTO>> listConsultationByDate(
			@RequestParam(value = "date", required = false) String date,
			@RequestParam(value = "adviserId", required = false) String adviserId, HttpServletRequest request,
			HttpServletResponse response) {
		// 更改当前顾问编号
		Integer newAdviserId = getAdviserId(request);
		if (newAdviserId != null)
			adviserId = newAdviserId + "";
		try {
			super.setGetHeader(response);
			return new Response<List<ConsultationDTO>>(0,
					consultationService.listRemindByRemindDate(
							StringUtil.isEmpty(date) ? new Date() : new Date(Long.parseLong(date)),
							StringUtil.toInt(adviserId)));
		} catch (ServiceException e) {
			return new Response<List<ConsultationDTO>>(1, e.getMessage(), null);
		}
	}

}
