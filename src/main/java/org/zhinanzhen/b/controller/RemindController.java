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
import org.zhinanzhen.b.service.RemindService;
import org.zhinanzhen.b.service.pojo.RemindDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

import com.ikasoa.core.utils.StringUtil;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/remind")
public class RemindController extends BaseController {

	@Resource
	RemindService remindService;

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> addRemind(@RequestParam(value = "remindDate") String remindDate,
			@RequestParam(value = "schoolBrokerageSaId") String schoolBrokerageSaId, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			RemindDTO remindDto = new RemindDTO();
			remindDto.setRemindDate(new Date(Long.parseLong(remindDate)));
			remindDto.setSchoolBrokerageSaId(StringUtil.toInt(schoolBrokerageSaId));
			if (remindService.addRemind(remindDto) > 0) {
				return new Response<Integer>(0, remindDto.getId());
			} else {
				return new Response<Integer>(1, "创建失败.", 0);
			}
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<RemindDTO>> listRemind(
			@RequestParam(value = "schoolBrokerageSaId", required = false) String schoolBrokerageSaId,
			HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<List<RemindDTO>>(0,
					remindService.listRemindBySchoolBrokerageSaId(Integer.parseInt(schoolBrokerageSaId)));
		} catch (ServiceException e) {
			return new Response<List<RemindDTO>>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/listByDate", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<RemindDTO>> listRemindByDate(@RequestParam(value = "date", required = false) String date,
			HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<List<RemindDTO>>(0,
					remindService.listRemindByRemindDate(new Date(Long.parseLong(date))));
		} catch (ServiceException e) {
			return new Response<List<RemindDTO>>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/delete", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> deleteRemind(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, remindService.deleteRemindById(id));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/deleteRemindBySchoolBrokerageSaId", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> deleteRemindBySchoolBrokerageSaId(@RequestParam(value = "schoolBrokerageSaId") int schoolBrokerageSaId,
			HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, remindService.deleteRemindBySchoolBrokerageSaId(schoolBrokerageSaId));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), 0);
		}
	}

}
