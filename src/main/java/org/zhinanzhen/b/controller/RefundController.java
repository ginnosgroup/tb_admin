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
import org.zhinanzhen.b.service.RefundService;
import org.zhinanzhen.b.service.pojo.RefundDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/refund")
public class RefundController extends BaseController {

	@Resource
	RefundService refundService;
	
	public enum RefundStateEnum {
		PENDING, REVIEW, APPLY, CLOSE;

		public static RefundStateEnum get(String name) {
			for (RefundStateEnum e : RefundStateEnum.values())
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
			RefundDTO refundDto = new RefundDTO();
			refundDto.setState(state);
			refundDto.setType(type);
			refundDto.setUserId(userId);
			refundDto.setAdviserId(adviserId);
			refundDto.setMaraId(maraId);
			refundDto.setOfficialId(officialId);
			if (schoolId != null && schoolId > 0)
				refundDto.setSchoolId(schoolId);
			if (courseId != null && courseId > 0)
				refundDto.setCourseId(courseId);
			refundDto.setReceiveDate(new Date(Long.parseLong(receiveDate)));
			refundDto.setReceived(Double.parseDouble(received));
			if (paymentVoucherImageUrl != null)
				refundDto.setPaymentVoucherImageUrl(paymentVoucherImageUrl);
			refundDto.setRefundDetailId(refundDetailId);
			refundDto.setRefundDetail(refundDetail);
			refundDto.setCurrencyType(currencyType);
			refundDto.setAmountName(amountName);
			refundDto.setBankName(bankName);
			refundDto.setBsb(bsb);
			refundDto.setRemarks(remarks);
			if (refundService.addRefund(refundDto) > 0) {
				return new Response<Integer>(0, refundDto.getId());
			} else {
				return new Response<Integer>(0, "创建失败.", 0);
			}
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<RefundDTO>> list(@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "state", required = false) String state, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<List<RefundDTO>>(0, refundService.listRefund(type, state));
		} catch (ServiceException e) {
			return new Response<List<RefundDTO>>(1, e.getMessage(), null);
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
			RefundDTO refundDto = refundService.getRefundById(id);
			if (state != null)
				refundDto.setState(state);
			if (userId != null && userId > 0)
				refundDto.setUserId(userId);
			if (adviserId != null && adviserId > 0)
				refundDto.setAdviserId(adviserId);
			if (maraId != null && maraId > 0)
				refundDto.setMaraId(maraId);
			if (officialId != null && officialId > 0)
				refundDto.setOfficialId(officialId);
			if (schoolId != null && schoolId > 0)
				refundDto.setSchoolId(schoolId);
			if (courseId != null && courseId > 0)
				refundDto.setCourseId(courseId);
			if (receiveDate != null)
				refundDto.setReceiveDate(new Date(Long.parseLong(receiveDate)));
			if (received != null)
				refundDto.setReceived(Double.parseDouble(received));
			if (paymentVoucherImageUrl != null)
				refundDto.setPaymentVoucherImageUrl(paymentVoucherImageUrl);
			if (refundVoucherImageUrl != null)
				refundDto.setRefundVoucherImageUrl(refundVoucherImageUrl);
			if (refundDetailId != null && refundDetailId > 0)
				refundDto.setRefundDetailId(refundDetailId);
			if (refundDetail != null)
				refundDto.setRefundDetail(refundDetail);
			if (currencyType != null)
				refundDto.setCurrencyType(currencyType);
			if (amountName != null)
				refundDto.setAmountName(amountName);
			if (bankName != null)
				refundDto.setBankName(bankName);
			if (bsb != null)
				refundDto.setBsb(bsb);
			if (remarks != null)
				refundDto.setRemarks(remarks);
			if (refundService.updateRefund(refundDto) > 0) {
				return new Response<Integer>(0, refundDto.getId());
			} else {
				return new Response<Integer>(0, "修改失败.", 0);
			}
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/deleteRefundById", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> deleteRefundById(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, refundService.deleteRefundById(id));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), 0);
		}
	}

}
