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
import org.zhinanzhen.b.service.VisaService;
import org.zhinanzhen.b.service.pojo.VisaDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

import com.ikasoa.core.utils.StringUtil;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/visa")
public class VisaController extends BaseController {

	@Resource
	VisaService visaService;

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	public Response<VisaDTO> addVisa(@RequestParam(value = "userId", required = false) String userId,
			@RequestParam(value = "handlingDate") String handlingDate,
			@RequestParam(value = "receiveTypeId") String receiveTypeId,
			@RequestParam(value = "receiveDate") String receiveDate,
			@RequestParam(value = "serviceId") String serviceId, @RequestParam(value = "receivable") String receivable,
			@RequestParam(value = "received", required = false) String received,
			@RequestParam(value = "amount") String amount, @RequestParam(value = "adviserId") String adviserId,
			@RequestParam(value = "officialId") String officialId, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			VisaDTO visaDto = new VisaDTO();
			if (StringUtil.isNotEmpty(userId)) {
				visaDto.setUserId(Integer.parseInt(userId));
			}
			if (StringUtil.isNotEmpty(handlingDate)) {
				visaDto.setHandlingDate(new Date(Long.parseLong(handlingDate)));
			}
			if (StringUtil.isNotEmpty(receiveTypeId)) {
				visaDto.setReceiveTypeId(Integer.parseInt(receiveTypeId));
			}
			if (StringUtil.isNotEmpty(receiveDate)) {
				visaDto.setReceiveDate(new Date(Long.parseLong(receiveDate)));
			}
			if (StringUtil.isNotEmpty(serviceId)) {
				visaDto.setServiceId(Integer.parseInt(serviceId));
			}
			if (StringUtil.isNotEmpty(receivable)) {
				visaDto.setReceivable(Double.parseDouble(receivable));
			}
			if (StringUtil.isNotEmpty(received)) {
				visaDto.setReceived(Double.parseDouble(received));
			}
			if (StringUtil.isNotEmpty(amount)) {
				visaDto.setAmount(Double.parseDouble(amount));
			}
			if (StringUtil.isNotEmpty(adviserId)) {
				visaDto.setAdviserId(StringUtil.toInt(adviserId));
			}
			if (StringUtil.isNotEmpty(officialId)) {
				visaDto.setOfficialId(StringUtil.toInt(officialId));
			}
			double commission = visaDto.getAmount();
			visaDto.setGst(commission / 11);
			visaDto.setDeductGst(commission - visaDto.getGst());
			visaDto.setBonus(visaDto.getDeductGst() * 0.1);
			if (visaService.addVisa(visaDto) > 0) {
				return new Response<VisaDTO>(0, visaDto);
			} else {
				return new Response<VisaDTO>(1, "创建失败.", null);
			}
		} catch (ServiceException e) {
			return new Response<VisaDTO>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	@ResponseBody
	public Response<VisaDTO> updateVisa(@RequestParam(value = "id") int id,
			@RequestParam(value = "userId", required = false) String userId,
			@RequestParam(value = "handlingDate", required = false) String handlingDate,
			@RequestParam(value = "receiveTypeId", required = false) String receiveTypeId,
			@RequestParam(value = "receiveDate", required = false) String receiveDate,
			@RequestParam(value = "serviceId", required = false) String serviceId,
			@RequestParam(value = "receivable", required = false) String receivable,
			@RequestParam(value = "received", required = false) String received,
			@RequestParam(value = "amount", required = false) String amount,
			@RequestParam(value = "adviserId", required = false) String adviserId,
			@RequestParam(value = "officialId", required = false) String officialId, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			VisaDTO visaDto = new VisaDTO();
			visaDto.setId(id);
			if (StringUtil.isNotEmpty(handlingDate)) {
				visaDto.setHandlingDate(new Date(Long.parseLong(handlingDate)));
			}
			if (StringUtil.isNotEmpty(userId)) {
				visaDto.setUserId(Integer.parseInt(userId));
			}
			if (StringUtil.isNotEmpty(receiveTypeId)) {
				visaDto.setReceiveTypeId(Integer.parseInt(receiveTypeId));
			}
			if (StringUtil.isNotEmpty(receiveDate)) {
				visaDto.setReceiveDate(new Date(Long.parseLong(receiveDate)));
			}
			if (StringUtil.isNotEmpty(serviceId)) {
				visaDto.setServiceId(Integer.parseInt(serviceId));
			}
			if (StringUtil.isNotEmpty(receivable)) {
				visaDto.setReceivable(Double.parseDouble(receivable));
			}
			if (StringUtil.isNotEmpty(received)) {
				visaDto.setReceived(Double.parseDouble(received));
			}
			if (StringUtil.isNotEmpty(amount)) {
				visaDto.setAmount(Double.parseDouble(amount));
			}
			if (StringUtil.isNotEmpty(adviserId)) {
				visaDto.setAdviserId(StringUtil.toInt(adviserId));
			}
			if (StringUtil.isNotEmpty(officialId)) {
				visaDto.setOfficialId(StringUtil.toInt(officialId));
			}
			double commission = visaDto.getAmount();
			visaDto.setGst(commission / 11);
			visaDto.setDeductGst(commission - visaDto.getGst());
			visaDto.setBonus(visaDto.getDeductGst() * 0.1);
			if (visaService.updateVisa(visaDto) > 0) {
				return new Response<VisaDTO>(0, visaDto);
			} else {
				return new Response<VisaDTO>(1, "修改失败.", null);
			}
		} catch (ServiceException e) {
			return new Response<VisaDTO>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/count", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> countVisa(@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "startHandlingDate", required = false) String startHandlingDate,
			@RequestParam(value = "endHandlingDate", required = false) String endHandlingDate,
			@RequestParam(value = "startDate", required = false) String startDate,
			@RequestParam(value = "endDate", required = false) String endDate,
			@RequestParam(value = "adviserId", required = false) Integer adviserId, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<Integer>(0,
					visaService.countVisa(keyword, startHandlingDate, endHandlingDate, startDate, endDate, adviserId));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<VisaDTO>> listVisa(@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "startHandlingDate", required = false) String startHandlingDate,
			@RequestParam(value = "endHandlingDate", required = false) String endHandlingDate,
			@RequestParam(value = "startDate", required = false) String startDate,
			@RequestParam(value = "endDate", required = false) String endDate,
			@RequestParam(value = "adviserId", required = false) Integer adviserId,
			@RequestParam(value = "pageNum") int pageNum, @RequestParam(value = "pageSize") int pageSize,
			HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<List<VisaDTO>>(0, visaService.listVisa(keyword, startHandlingDate, endHandlingDate,
					startDate, endDate, adviserId, pageNum, pageSize));
		} catch (ServiceException e) {
			return new Response<List<VisaDTO>>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/get", method = RequestMethod.GET)
	@ResponseBody
	public Response<VisaDTO> getVisa(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<VisaDTO>(0, visaService.getVisaById(id));
		} catch (ServiceException e) {
			return new Response<VisaDTO>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/close", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> closeVisa(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			VisaDTO visaDto = visaService.getVisaById(id);
			visaDto.setClose(true);
			return new Response<Integer>(0, visaService.updateVisa(visaDto));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/reopen", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> reopenVisa(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			VisaDTO visaDto = visaService.getVisaById(id);
			visaDto.setClose(false);
			return new Response<Integer>(0, visaService.updateVisa(visaDto));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/delete", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> deleteVisa(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, visaService.deleteVisaById(id));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), 0);
		}
	}

}
