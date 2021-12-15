package org.zhinanzhen.b.controller;

import java.io.IOException;
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
import org.springframework.web.multipart.MultipartFile;
import org.zhinanzhen.b.service.OrderRefundService;
import org.zhinanzhen.b.service.pojo.OrderRefundDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/order_refund")
public class OrderRefundController extends BaseController {

	@Resource
	OrderRefundService orderRefundService;
	
	public enum OrderRefundStateEnum {
		PENDING, REVIEW, APPLY, CLOSE;

		public static OrderRefundStateEnum get(String name) {
			for (OrderRefundStateEnum e : OrderRefundStateEnum.values())
				if (e.toString().equals(name))
					return e;
			return null;
		}
	}

	@RequestMapping(value = "/upload_img", method = RequestMethod.POST)
	@ResponseBody
	public Response<String> uploadImage(@RequestParam MultipartFile file, HttpServletRequest request,
			HttpServletResponse response) throws IllegalStateException, IOException {
		super.setPostHeader(response);
		return super.upload2(file, request.getSession(), "/uploads/payment_voucher_image_url_r/");
	}

	@RequestMapping(value = "/upload_img2", method = RequestMethod.POST)
	@ResponseBody
	public Response<String> uploadImage2(@RequestParam MultipartFile file, HttpServletRequest request,
			HttpServletResponse response) throws IllegalStateException, IOException {
		super.setPostHeader(response);
		return super.upload2(file, request.getSession(), "/uploads/refund_voucher_image_url/");
	}

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> add(@RequestParam(value = "state") String state, @RequestParam(value = "type") String type,
			@RequestParam(value = "visaId", required = false) Integer visaId,
			@RequestParam(value = "commissionOrderId", required = false) Integer commissionOrderId,
			@RequestParam(value = "receiveDate") String receiveDate, @RequestParam(value = "received") String received,
			@RequestParam(value = "paymentVoucherImageUrl", required = false) String paymentVoucherImageUrl,
			@RequestParam(value = "refundDetailId") Integer refundDetailId,
			@RequestParam(value = "refundDetail") String refundDetail,
			@RequestParam(value = "currencyType") String currencyType,
			@RequestParam(value = "amountName") String amountName, @RequestParam(value = "bankName") String bankName,
			@RequestParam(value = "bsb") String bsb, @RequestParam(value = "remarks") String remarks,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			OrderRefundDTO orderRefundDto = new OrderRefundDTO();
			orderRefundDto.setState(state);
			orderRefundDto.setType(type);
			if (visaId != null && visaId > 0)
				orderRefundDto.setVisaId(visaId);
			if (commissionOrderId != null && commissionOrderId > 0)
				orderRefundDto.setCommissionOrderId(commissionOrderId);
			orderRefundDto.setReceiveDate(new Date(Long.parseLong(receiveDate)));
			orderRefundDto.setReceived(Double.parseDouble(received));
			if (paymentVoucherImageUrl != null)
				orderRefundDto.setPaymentVoucherImageUrl(paymentVoucherImageUrl);
			orderRefundDto.setRefundDetailId(refundDetailId);
			orderRefundDto.setRefundDetail(refundDetail);
			orderRefundDto.setCurrencyType(currencyType);
			orderRefundDto.setAmountName(amountName);
			orderRefundDto.setBankName(bankName);
			orderRefundDto.setBsb(bsb);
			orderRefundDto.setRemarks(remarks);
			if (orderRefundService.addOrderRefund(orderRefundDto) > 0) {
				return new Response<Integer>(0, orderRefundDto.getId());
			} else {
				return new Response<Integer>(0, "创建失败.", 0);
			}
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<OrderRefundDTO>> list(@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "visaId", required = false) Integer visaId,
			@RequestParam(value = "commissionOrderId", required = false) Integer commissionOrderId,
			@RequestParam(value = "state", required = false) String state, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<List<OrderRefundDTO>>(0,
					orderRefundService.listOrderRefund(type, visaId, commissionOrderId, state));
		} catch (ServiceException e) {
			return new Response<List<OrderRefundDTO>>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> update(@RequestParam(value = "id") int id, @RequestParam(value = "state") String state,
			@RequestParam(value = "receiveDate", required = false) String receiveDate,
			@RequestParam(value = "received", required = false) String received,
			@RequestParam(value = "paymentVoucherImageUrl", required = false) String paymentVoucherImageUrl,
			@RequestParam(value = "refundVoucherImageUrl", required = false) String refundVoucherImageUrl,
			@RequestParam(value = "refundDetailId", required = false) Integer refundDetailId,
			@RequestParam(value = "refundDetail", required = false) String refundDetail,
			@RequestParam(value = "currencyType", required = false) String currencyType,
			@RequestParam(value = "amountName", required = false) String amountName,
			@RequestParam(value = "bankName", required = false) String bankName,
			@RequestParam(value = "bsb", required = false) String bsb,
			@RequestParam(value = "remarks", required = false) String remarks, HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			if (id <= 0)
				return new Response<Integer>(1, "id不正确.", 0);
			OrderRefundDTO orderRefundDto = orderRefundService.getOrderRefundById(id);
			if (state != null)
				orderRefundDto.setState(state);
			if (receiveDate != null)
				orderRefundDto.setReceiveDate(new Date(Long.parseLong(receiveDate)));
			if (received != null)
				orderRefundDto.setReceived(Double.parseDouble(received));
			if (paymentVoucherImageUrl != null)
				orderRefundDto.setPaymentVoucherImageUrl(paymentVoucherImageUrl);
			if (refundVoucherImageUrl != null)
				orderRefundDto.setRefundVoucherImageUrl(refundVoucherImageUrl);
			if (refundDetailId != null && refundDetailId > 0)
				orderRefundDto.setRefundDetailId(refundDetailId);
			if (refundDetail != null)
				orderRefundDto.setRefundDetail(refundDetail);
			if (currencyType != null)
				orderRefundDto.setCurrencyType(currencyType);
			if (amountName != null)
				orderRefundDto.setAmountName(amountName);
			if (bankName != null)
				orderRefundDto.setBankName(bankName);
			if (bsb != null)
				orderRefundDto.setBsb(bsb);
			if (remarks != null)
				orderRefundDto.setRemarks(remarks);
			if (orderRefundService.updateOrderRefund(orderRefundDto) > 0) {
				return new Response<Integer>(0, orderRefundDto.getId());
			} else {
				return new Response<Integer>(0, "修改失败.", 0);
			}
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/deleteOrderRefundById", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> deleteOrderRefundById(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, orderRefundService.deleteOrderRefundById(id));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), 0);
		}
	}

}
