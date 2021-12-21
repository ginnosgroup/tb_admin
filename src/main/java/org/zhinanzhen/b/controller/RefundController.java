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
import org.zhinanzhen.b.service.ManualRefundService;
import org.zhinanzhen.b.service.pojo.ManualRefundDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/manual_refund")
public class ManualRefundController extends BaseController {

	@Resource
	ManualRefundService manualRefundService;
	
	public enum ManualRefundStateEnum {
		PENDING, REVIEW, APPLY, CLOSE;

		public static ManualRefundStateEnum get(String name) {
			for (ManualRefundStateEnum e : ManualRefundStateEnum.values())
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
			@RequestParam(value = "userId") Integer userId, @RequestParam(value = "adviserId") Integer adviserId,
			@RequestParam(value = "maraId") Integer maraId, @RequestParam(value = "officialId") Integer officialId,
			@RequestParam(value = "schoolId", required = false) Integer schoolId,
			@RequestParam(value = "courseId", required = false) Integer courseId,
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
			ManualRefundDTO manualRefundDto = new ManualRefundDTO();
			manualRefundDto.setState(state);
			manualRefundDto.setType(type);
			manualRefundDto.setUserId(userId);
			manualRefundDto.setAdviserId(adviserId);
			manualRefundDto.setMaraId(maraId);
			manualRefundDto.setOfficialId(officialId);
			if (schoolId != null && schoolId > 0)
				manualRefundDto.setSchoolId(schoolId);
			if (courseId != null && courseId > 0)
				manualRefundDto.setCourseId(courseId);
			manualRefundDto.setReceiveDate(new Date(Long.parseLong(receiveDate)));
			manualRefundDto.setReceived(Double.parseDouble(received));
			if (paymentVoucherImageUrl != null)
				manualRefundDto.setPaymentVoucherImageUrl(paymentVoucherImageUrl);
			manualRefundDto.setRefundDetailId(refundDetailId);
			manualRefundDto.setRefundDetail(refundDetail);
			manualRefundDto.setCurrencyType(currencyType);
			manualRefundDto.setAmountName(amountName);
			manualRefundDto.setBankName(bankName);
			manualRefundDto.setBsb(bsb);
			manualRefundDto.setRemarks(remarks);
			if (manualRefundService.addManualRefund(manualRefundDto) > 0) {
				return new Response<Integer>(0, manualRefundDto.getId());
			} else {
				return new Response<Integer>(0, "创建失败.", 0);
			}
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<ManualRefundDTO>> list(@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "state", required = false) String state, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<List<ManualRefundDTO>>(0, manualRefundService.listManualRefund(type, state));
		} catch (ServiceException e) {
			return new Response<List<ManualRefundDTO>>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> update(@RequestParam(value = "id") int id,
			@RequestParam(value = "state", required = false) String state,
			@RequestParam(value = "userId", required = false) Integer userId,
			@RequestParam(value = "adviserId", required = false) Integer adviserId,
			@RequestParam(value = "maraId") Integer maraId,
			@RequestParam(value = "officialId", required = false) Integer officialId,
			@RequestParam(value = "schoolId", required = false) Integer schoolId,
			@RequestParam(value = "courseId", required = false) Integer courseId,
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
			ManualRefundDTO manualRefundDto = manualRefundService.getManualRefundById(id);
			if (state != null)
				manualRefundDto.setState(state);
			if (userId != null && userId > 0)
				manualRefundDto.setUserId(userId);
			if (adviserId != null && adviserId > 0)
				manualRefundDto.setAdviserId(adviserId);
			if (maraId != null && maraId > 0)
				manualRefundDto.setMaraId(maraId);
			if (officialId != null && officialId > 0)
				manualRefundDto.setOfficialId(officialId);
			if (schoolId != null && schoolId > 0)
				manualRefundDto.setSchoolId(schoolId);
			if (courseId != null && courseId > 0)
				manualRefundDto.setCourseId(courseId);
			if (receiveDate != null)
				manualRefundDto.setReceiveDate(new Date(Long.parseLong(receiveDate)));
			if (received != null)
				manualRefundDto.setReceived(Double.parseDouble(received));
			if (paymentVoucherImageUrl != null)
				manualRefundDto.setPaymentVoucherImageUrl(paymentVoucherImageUrl);
			if (refundVoucherImageUrl != null)
				manualRefundDto.setRefundVoucherImageUrl(refundVoucherImageUrl);
			if (refundDetailId != null && refundDetailId > 0)
				manualRefundDto.setRefundDetailId(refundDetailId);
			if (refundDetail != null)
				manualRefundDto.setRefundDetail(refundDetail);
			if (currencyType != null)
				manualRefundDto.setCurrencyType(currencyType);
			if (amountName != null)
				manualRefundDto.setAmountName(amountName);
			if (bankName != null)
				manualRefundDto.setBankName(bankName);
			if (bsb != null)
				manualRefundDto.setBsb(bsb);
			if (remarks != null)
				manualRefundDto.setRemarks(remarks);
			if (manualRefundService.updateManualRefund(manualRefundDto) > 0) {
				return new Response<Integer>(0, manualRefundDto.getId());
			} else {
				return new Response<Integer>(0, "修改失败.", 0);
			}
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/deleteManualRefundById", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> deleteManualRefundById(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, manualRefundService.deleteManualRefundById(id));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), 0);
		}
	}

}
