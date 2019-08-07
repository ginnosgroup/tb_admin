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
import org.zhinanzhen.b.service.BrokerageSaService;
import org.zhinanzhen.b.service.pojo.BrokerageSaDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

import com.ikasoa.core.utils.StringUtil;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/brokerage_sa")
public class BrokerageSaController extends BaseController {

	@Resource
	BrokerageSaService brokerageSaService;

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	public Response<BrokerageSaDTO> addBrokerageSa(@RequestParam(value = "handlingDate") String handlingDate,
			@RequestParam(value = "userId") String userId, @RequestParam(value = "schoolId") String schoolId,
			@RequestParam(value = "startDate") String startDate, @RequestParam(value = "endDate") String endDate,
			@RequestParam(value = "receiveTypeId") String receiveTypeId,
			@RequestParam(value = "tuitionFee") String tuitionFee,
			@RequestParam(value = "discount", required = false) String discount,
			@RequestParam(value = "commission") String commission, @RequestParam(value = "adviserId") String adviserId,
			@RequestParam(value = "officialId") String officialId,
			@RequestParam(value = "remarks", required = false) String remarks, HttpServletRequest request,
			HttpServletResponse response) {

		// 更改当前顾问编号
		Integer newAdviserId = getAdviserId(request);
		if (newAdviserId != null)
			adviserId = newAdviserId + "";

		try {
			super.setPostHeader(response);
			BrokerageSaDTO brokerageSaDto = new BrokerageSaDTO();
			if (StringUtil.isNotEmpty(handlingDate)) {
				brokerageSaDto.setHandlingDate(new Date(Long.parseLong(handlingDate)));
			}
			if (StringUtil.isNotEmpty(userId)) {
				brokerageSaDto.setUserId(StringUtil.toInt(userId));
			}
			if (StringUtil.isNotEmpty(startDate)) {
				brokerageSaDto.setStartDate(new Date(Long.parseLong(startDate)));
			}
			if (StringUtil.isNotEmpty(endDate)) {
				brokerageSaDto.setEndDate(new Date(Long.parseLong(endDate)));
			}
			if (StringUtil.isNotEmpty(schoolId)) {
				brokerageSaDto.setSchoolId(StringUtil.toInt(schoolId));
			}
			if (StringUtil.isNotEmpty(receiveTypeId)) {
				brokerageSaDto.setReceiveTypeId(StringUtil.toInt(receiveTypeId));
			}
			if (StringUtil.isNotEmpty(tuitionFee)) {
				brokerageSaDto.setTuitionFee(Double.parseDouble(tuitionFee));
			}
			if (StringUtil.isNotEmpty(discount)) {
				brokerageSaDto.setDiscount(Double.parseDouble(discount));
			}
			if (StringUtil.isNotEmpty(commission)) {
				brokerageSaDto.setCommission(Double.parseDouble(commission));
			}
			if (StringUtil.isNotEmpty(adviserId)) {
				brokerageSaDto.setAdviserId(StringUtil.toInt(adviserId));
			}
			if (StringUtil.isNotEmpty(officialId)) {
				brokerageSaDto.setOfficialId(StringUtil.toInt(officialId));
			}
			if (StringUtil.isNotEmpty(remarks))
				brokerageSaDto.setRemarks(remarks);
			brokerageSaDto.setGst(brokerageSaDto.getCommission() / 11);
			brokerageSaDto.setDeductGst(brokerageSaDto.getCommission() - brokerageSaDto.getGst());
			brokerageSaDto.setBonus(brokerageSaDto.getDeductGst() * 0.1);
			if (brokerageSaService.addBrokerageSa(brokerageSaDto) > 0) {
				return new Response<BrokerageSaDTO>(0, brokerageSaDto);
			} else {
				return new Response<BrokerageSaDTO>(1, "创建失败.", null);
			}
		} catch (ServiceException e) {
			return new Response<BrokerageSaDTO>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	@ResponseBody
	public Response<BrokerageSaDTO> updateBrokerage(@RequestParam(value = "id") int id,
			@RequestParam(value = "handlingDate", required = false) String handlingDate,
			@RequestParam(value = "userId", required = false) String userId,
			@RequestParam(value = "schoolId", required = false) String schoolId,
			@RequestParam(value = "startDate", required = false) String startDate,
			@RequestParam(value = "endDate", required = false) String endDate,
			@RequestParam(value = "receiveTypeId", required = false) String receiveTypeId,
			@RequestParam(value = "tuitionFee", required = false) String tuitionFee,
			@RequestParam(value = "discount", required = false) String discount,
			@RequestParam(value = "commission", required = false) String commission,
			@RequestParam(value = "adviserId", required = false) String adviserId,
			@RequestParam(value = "officialId", required = false) String officialId,
			@RequestParam(value = "remarks", required = false) String remarks, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			BrokerageSaDTO brokerageSaDto = new BrokerageSaDTO();
			brokerageSaDto.setId(id);
			if (StringUtil.isNotEmpty(handlingDate)) {
				brokerageSaDto.setHandlingDate(new Date(Long.parseLong(handlingDate)));
			}
			if (StringUtil.isNotEmpty(userId)) {
				brokerageSaDto.setUserId(Integer.parseInt(userId));
			}
			if (StringUtil.isNotEmpty(schoolId)) {
				brokerageSaDto.setSchoolId(StringUtil.toInt(schoolId));
			}
			if (StringUtil.isNotEmpty(receiveTypeId)) {
				brokerageSaDto.setReceiveTypeId(StringUtil.toInt(receiveTypeId));
			}
			if (StringUtil.isNotEmpty(tuitionFee)) {
				brokerageSaDto.setTuitionFee(Double.parseDouble(tuitionFee));
			}
			if (StringUtil.isNotEmpty(discount)) {
				brokerageSaDto.setDiscount(Double.parseDouble(discount));
			}
			if (StringUtil.isNotEmpty(commission)) {
				brokerageSaDto.setCommission(Double.parseDouble(commission));
			}
			if (StringUtil.isNotEmpty(adviserId)) {
				brokerageSaDto.setAdviserId(StringUtil.toInt(adviserId));
			}
			if (StringUtil.isNotEmpty(officialId)) {
				brokerageSaDto.setOfficialId(StringUtil.toInt(officialId));
			}
			if (StringUtil.isNotEmpty(remarks))
				brokerageSaDto.setRemarks(remarks);
			brokerageSaDto.setGst(brokerageSaDto.getCommission() / 11);
			brokerageSaDto.setDeductGst(brokerageSaDto.getCommission() - brokerageSaDto.getGst());
			brokerageSaDto.setBonus(brokerageSaDto.getDeductGst() * 0.1);
			if (brokerageSaService.updateBrokerageSa(brokerageSaDto) > 0) {
				return new Response<BrokerageSaDTO>(0, brokerageSaDto);
			} else {
				return new Response<BrokerageSaDTO>(1, "修改失败.", null);
			}
		} catch (ServiceException e) {
			return new Response<BrokerageSaDTO>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/count", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> countBrokerage(@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "startHandlingDate", required = false) String startHandlingDate,
			@RequestParam(value = "endHandlingDate", required = false) String endHandlingDate,
			@RequestParam(value = "startDate", required = false) String startDate,
			@RequestParam(value = "endDate", required = false) String endDate,
			@RequestParam(value = "adviserId", required = false) Integer adviserId,
			@RequestParam(value = "schoolId", required = false) Integer schoolId,
			@RequestParam(value = "userId", required = false) Integer userId, HttpServletRequest request,
			HttpServletResponse response) {

		// 更改当前顾问编号
		Integer newAdviserId = getAdviserId(request);
		if (newAdviserId != null)
			adviserId = newAdviserId;

		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, brokerageSaService.countBrokerageSa(keyword, startHandlingDate,
					endHandlingDate, startDate, endDate, adviserId, schoolId, userId));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<BrokerageSaDTO>> listBrokerage(
			@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "startHandlingDate", required = false) String startHandlingDate,
			@RequestParam(value = "endHandlingDate", required = false) String endHandlingDate,
			@RequestParam(value = "startDate", required = false) String startDate,
			@RequestParam(value = "endDate", required = false) String endDate,
			@RequestParam(value = "adviserId", required = false) Integer adviserId,
			@RequestParam(value = "schoolId", required = false) Integer schoolId,
			@RequestParam(value = "userId", required = false) Integer userId,
			@RequestParam(value = "pageNum") int pageNum, @RequestParam(value = "pageSize") int pageSize,
			HttpServletRequest request, HttpServletResponse response) {

		// 更改当前顾问编号
		Integer newAdviserId = getAdviserId(request);
		if (newAdviserId != null)
			adviserId = newAdviserId;

		try {
			super.setGetHeader(response);
			return new Response<List<BrokerageSaDTO>>(0, brokerageSaService.listBrokerageSa(keyword, startHandlingDate,
					endHandlingDate, startDate, endDate, adviserId, schoolId, userId, pageNum, pageSize));
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
