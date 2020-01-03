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
import org.zhinanzhen.b.service.ServiceService;
import org.zhinanzhen.b.service.pojo.ServiceDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

import com.ikasoa.core.utils.StringUtil;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/service")
public class ServiceController extends BaseController {

	@Resource
	ServiceService serviceService;

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> addService(@RequestParam(value = "type") String type,
			@RequestParam(value = "code") String code, @RequestParam(value = "isPay") String isPay,
			@RequestParam(value = "receiveTypeId") String receiveTypeId,
			@RequestParam(value = "receiveDate") String receiveDate,
			@RequestParam(value = "receivable") String receivable, @RequestParam(value = "discount") String discount,
			@RequestParam(value = "received") String received,
			@RequestParam(value = "paymentTimes") String paymentTimes, @RequestParam(value = "amount") String amount,
			@RequestParam(value = "gst") String gst, @RequestParam(value = "deductGst") String deductGst,
			@RequestParam(value = "bonus") String bonus, @RequestParam(value = "maraId") String maraId,
			@RequestParam(value = "adviserId") String adviserId, @RequestParam(value = "officialId") String officialId,
			@RequestParam(value = "remarks") String remarks, HttpServletRequest request, HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			ServiceDTO serviceDto = new ServiceDTO();
			if (StringUtil.isNotEmpty(type))
				serviceDto.setType(type);
			if (StringUtil.isNotEmpty(code))
				serviceDto.setCode(code);
			if (isPay != null && "true".equalsIgnoreCase(isPay))
				serviceDto.setPay(true);
			else
				serviceDto.setPay(false);
			if (StringUtil.isNotEmpty(receiveTypeId))
				serviceDto.setReceiveTypeId(StringUtil.toInt(receiveTypeId));
			if (StringUtil.isNotEmpty(receiveDate))
				serviceDto.setReceiveDate(new Date(Long.parseLong(receiveDate)));
			if (StringUtil.isNotEmpty(receivable))
				serviceDto.setReceivable(Double.parseDouble(receivable));
			if (StringUtil.isNotEmpty(discount))
				serviceDto.setAmount(Double.parseDouble(discount));
			if (StringUtil.isNotEmpty(received))
				serviceDto.setReceived(Double.parseDouble(received));
			if (StringUtil.isNotEmpty(paymentTimes))
				serviceDto.setPaymentTimes(StringUtil.toInt(paymentTimes));
			if (StringUtil.isNotEmpty(amount))
				serviceDto.setAmount(Double.parseDouble(amount));
			if (StringUtil.isNotEmpty(gst))
				serviceDto.setGst(Double.parseDouble(gst));
			if (StringUtil.isNotEmpty(deductGst))
				serviceDto.setDeductGst(Double.parseDouble(deductGst));
			if (StringUtil.isNotEmpty(bonus))
				serviceDto.setBonus(Double.parseDouble(bonus));
			if (StringUtil.isNotEmpty(maraId))
				serviceDto.setMaraId(StringUtil.toInt(maraId));
			if (StringUtil.isNotEmpty(adviserId))
				serviceDto.setAdviserId(StringUtil.toInt(adviserId));
			if (StringUtil.isNotEmpty(officialId))
				serviceDto.setOfficialId(StringUtil.toInt(officialId));
			if (StringUtil.isNotEmpty(remarks))
				serviceDto.setRemarks(remarks);
			if (serviceService.addService(serviceDto) > 0) {
				return new Response<Integer>(0, serviceDto.getId());
			} else {
				return new Response<Integer>(1, "创建失败.", 0);
			}
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> updateService(@RequestParam(value = "id") int id,
			@RequestParam(value = "code", required = false) String code, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			ServiceDTO serviceDto = new ServiceDTO();
			serviceDto.setId(id);
			serviceDto.setCode(code);
			int i = serviceService.updateService(serviceDto);
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
	public Response<List<ServiceDTO>> listService(HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<List<ServiceDTO>>(0, serviceService.listService());
		} catch (ServiceException e) {
			return new Response<List<ServiceDTO>>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/get", method = RequestMethod.GET)
	@ResponseBody
	public Response<ServiceDTO> getService(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<ServiceDTO>(0, serviceService.getServiceById(id));
		} catch (ServiceException e) {
			return new Response<ServiceDTO>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/delete", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> deleteService(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, serviceService.deleteServiceById(id));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), 0);
		}
	}

}
