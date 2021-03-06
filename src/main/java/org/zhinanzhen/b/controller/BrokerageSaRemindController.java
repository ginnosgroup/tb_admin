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
import org.zhinanzhen.b.service.BrokerageSaRemindService;
import org.zhinanzhen.b.service.BrokerageSaService;
import org.zhinanzhen.b.service.pojo.BrokerageSaRemindDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

import com.ikasoa.core.utils.StringUtil;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/brokerageSaRemind")
public class BrokerageSaRemindController extends BaseController {

	@Resource
	BrokerageSaRemindService brokerageSaRemindService;

	@Resource
	BrokerageSaService brokerageSaService;

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> addRemind(@RequestParam(value = "remindDate") String remindDate,
			@RequestParam(value = "brokerageSaId") String brokerageSaId, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			BrokerageSaRemindDTO brokerageSaRemindDto = new BrokerageSaRemindDTO();
			brokerageSaRemindDto.setRemindDate(new Date(Long.parseLong(remindDate)));
			brokerageSaRemindDto.setBrokerageSaId(StringUtil.toInt(brokerageSaId));
			if (brokerageSaRemindService.addRemind(brokerageSaRemindDto) > 0) {
				return new Response<Integer>(0, brokerageSaRemindDto.getId());
			} else {
				return new Response<Integer>(1, "创建失败.", 0);
			}
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<BrokerageSaRemindDTO>> listRemind(
			@RequestParam(value = "brokerageSaId", required = false) String brokerageSaId,
			@RequestParam(value = "adviserId", required = false) String adviserId,
			@RequestParam(value = "state", required = false) String state, HttpServletRequest request,
			HttpServletResponse response) {
		// 更改当前顾问编号
		Integer newAdviserId = getAdviserId(request);
		if (newAdviserId != null)
			adviserId = newAdviserId + "";
		try {
			super.setGetHeader(response);
			List<BrokerageSaRemindDTO> BrokerageSaRemindList = brokerageSaRemindService.listRemindByBrokerageSaId(
					Integer.parseInt(brokerageSaId), StringUtil.isNotEmpty(adviserId) ? StringUtil.toInt(adviserId) : 0,
					AbleStateEnum.get(state));
			BrokerageSaRemindList.forEach(bsr -> {
				try {
					bsr.setBrokerageSa(brokerageSaService.getBrokerageSaById(bsr.getBrokerageSaId()));
				} catch (ServiceException e) {
					bsr.setBrokerageSa(null);
				}
			});
			return new Response<List<BrokerageSaRemindDTO>>(0, BrokerageSaRemindList);
		} catch (ServiceException e) {
			return new Response<List<BrokerageSaRemindDTO>>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/listByDate", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<BrokerageSaRemindDTO>> listRemindByDate(
			@RequestParam(value = "date", required = false) String date,
			@RequestParam(value = "adviserId", required = false) String adviserId, HttpServletRequest request,
			HttpServletResponse response) {
		// 更改当前顾问编号
		Integer newAdviserId = getAdviserId(request);
		if (newAdviserId != null)
			adviserId = newAdviserId + "";
		try {
			super.setGetHeader(response);
			List<BrokerageSaRemindDTO> BrokerageSaRemindList = brokerageSaRemindService.listRemindByRemindDate(
					new Date(Long.parseLong(date)), StringUtil.isNotEmpty(adviserId) ? StringUtil.toInt(adviserId) : 0);
			BrokerageSaRemindList.forEach(bsr -> {
				try {
					bsr.setBrokerageSa(brokerageSaService.getBrokerageSaById(bsr.getBrokerageSaId()));
				} catch (ServiceException e) {
					bsr.setBrokerageSa(null);
				}
			});
			return new Response<List<BrokerageSaRemindDTO>>(0, BrokerageSaRemindList);
		} catch (ServiceException e) {
			return new Response<List<BrokerageSaRemindDTO>>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/deleteRemindByBrokerageSaId", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> deleteRemindByBrokerageSaId(@RequestParam(value = "brokerageSaId") int brokerageSaId,
			HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, brokerageSaRemindService.deleteRemindByBrokerageSaId(brokerageSaId));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), 0);
		}
	}

}
