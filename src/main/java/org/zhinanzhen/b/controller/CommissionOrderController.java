package org.zhinanzhen.b.controller;

import java.util.Date;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zhinanzhen.b.service.CommissionOrderService;
import org.zhinanzhen.b.service.ServiceOrderService;
import org.zhinanzhen.b.service.pojo.CommissionOrderDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

import com.ikasoa.core.utils.StringUtil;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/commissionOrder")
public class CommissionOrderController extends BaseController {

	@Resource
	ServiceOrderService serviceOrderService;

	@Resource
	CommissionOrderService commissionOrderService;

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	public Response<CommissionOrderDTO> add(@RequestParam(value = "serviceOrderId") String serviceOrderId,
			@RequestParam(value = "installment") String installment,
			@RequestParam(value = "startDate") String startDate, @RequestParam(value = "endDate") String endDate,
			@RequestParam(value = "tuitionFee") String tuitionFee,
			@RequestParam(value = "perTermTuitionFee") String perTermTuitionFee,
			@RequestParam(value = "remarks", required = false) String remarks, HttpServletRequest request,
			HttpServletResponse response) {

		try {
			super.setPostHeader(response);
			CommissionOrderDTO commissionOrderDto = new CommissionOrderDTO();
			if (serviceOrderService.getServiceOrderById(StringUtil.toInt(serviceOrderId)) == null)
				return new Response<CommissionOrderDTO>(1, "服务订单(ID:" + serviceOrderId + ")不存在!", null);
			commissionOrderDto.setInstallment(StringUtil.toInt(installment));
			commissionOrderDto.setStartDate(new Date(Long.parseLong(startDate)));
			commissionOrderDto.setEndDate(new Date(Long.parseLong(endDate)));
			commissionOrderDto.setTuitionFee(Double.parseDouble(tuitionFee));
			commissionOrderDto.setPerTermTuitionFee(Double.parseDouble(perTermTuitionFee));
			if (StringUtil.isNotEmpty(remarks))
				commissionOrderDto.setRemarks(remarks);
			return commissionOrderService.addCommissionOrder(commissionOrderDto) > 0
					? new Response<CommissionOrderDTO>(0, commissionOrderDto)
					: new Response<CommissionOrderDTO>(1, "创建失败.", null);
		} catch (ServiceException e) {
			return new Response<CommissionOrderDTO>(e.getCode(), e.getMessage(), null);
		}
	}

	// 顾问不能修改,财务能修改
	@RequestMapping(value = "/update", method = RequestMethod.POST)
	@ResponseBody
	public Response<CommissionOrderDTO> update(@RequestParam(value = "id") int id,
			@RequestParam(value = "serviceOrderId", required = false) String serviceOrderId,
			@RequestParam(value = "installment", required = false) String installment,
			@RequestParam(value = "startDate", required = false) String startDate,
			@RequestParam(value = "endDate", required = false) String endDate,
			@RequestParam(value = "tuitionFee", required = false) String tuitionFee,
			@RequestParam(value = "perTermTuitionFee", required = false) String perTermTuitionFee,
			@RequestParam(value = "remarks", required = false) String remarks, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			CommissionOrderDTO commissionOrderDto = new CommissionOrderDTO();
			commissionOrderDto.setId(id);
			if (StringUtil.isNotEmpty(serviceOrderId))
				commissionOrderDto.setServiceOrderId(Integer.parseInt(serviceOrderId));
			if (StringUtil.isNotEmpty(installment))
				commissionOrderDto.setInstallment(Integer.parseInt(installment));
			if (StringUtil.isNotEmpty(startDate))
				commissionOrderDto.setStartDate(new Date(Long.parseLong(startDate)));
			if (StringUtil.isNotEmpty(endDate))
				commissionOrderDto.setEndDate(new Date(Long.parseLong(endDate)));
			if (StringUtil.isNotEmpty(tuitionFee))
				commissionOrderDto.setTuitionFee(Double.parseDouble(tuitionFee));
			if (StringUtil.isNotEmpty(perTermTuitionFee))
				commissionOrderDto.setPerTermTuitionFee(Double.parseDouble(perTermTuitionFee));
			if (StringUtil.isNotEmpty(remarks))
				commissionOrderDto.setRemarks(remarks);
			return commissionOrderService.updateCommissionOrder(commissionOrderDto) > 0
					? new Response<CommissionOrderDTO>(0, commissionOrderDto)
					: new Response<CommissionOrderDTO>(1, "修改失败.", null);
		} catch (ServiceException e) {
			return new Response<CommissionOrderDTO>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/get", method = RequestMethod.GET)
	@ResponseBody
	public Response<CommissionOrderDTO> get(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<CommissionOrderDTO>(0, commissionOrderService.getCommissionOrderById(id));
		} catch (ServiceException e) {
			return new Response<CommissionOrderDTO>(1, e.getMessage(), null);
		}
	}

}
