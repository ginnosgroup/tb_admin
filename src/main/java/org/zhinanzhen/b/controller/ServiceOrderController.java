package org.zhinanzhen.b.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.zhinanzhen.b.service.ServiceOrderService;
import org.zhinanzhen.b.service.ServicePackageService;
import org.zhinanzhen.b.service.pojo.ServiceOrderCommentDTO;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.b.service.pojo.ServiceOrderReviewDTO;
import org.zhinanzhen.b.service.pojo.ServicePackageDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.UserService;

import com.ikasoa.core.ErrorCodeEnum;
import com.ikasoa.core.utils.StringUtil;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/serviceOrder")
public class ServiceOrderController extends BaseController {

	private static final Logger LOG = LoggerFactory.getLogger(ServiceOrderController.class);

	@Resource
	ServiceOrderService serviceOrderService;

	@Resource
	UserService userService;

	@Resource
	ServicePackageService servicePackageService;

	public enum ReviewAdviserStateEnum {
		PENDING, REVIEW, APPLY, COMPLETE, PAID, CLOSE;
		public static ReviewAdviserStateEnum get(String name) {
			for (ReviewAdviserStateEnum e : ReviewAdviserStateEnum.values())
				if (e.toString().equals(name))
					return e;
			return null;
		}
	}

	public enum ReviewMaraStateEnum {
		WAIT, REVIEW, FINISH;
		public static ReviewMaraStateEnum get(String name) {
			for (ReviewMaraStateEnum e : ReviewMaraStateEnum.values())
				if (e.toString().equals(name))
					return e;
			return null;
		}
	}

	public enum ReviewOfficialStateEnum {
		PENDING, WAIT, REVIEW, FINISH, APPLY, COMPLETE, PAID, CLOSE;
		public static ReviewOfficialStateEnum get(String name) {
			for (ReviewOfficialStateEnum e : ReviewOfficialStateEnum.values())
				if (e.toString().equals(name))
					return e;
			return null;
		}
	}

	public enum ServiceOrderReviewStateEnum {
		ADVISER, OFFICIAL, MARA, KJ;
		public static ServiceOrderReviewStateEnum get(String name) {
			for (ServiceOrderReviewStateEnum e : ServiceOrderReviewStateEnum.values())
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
		return super.upload2(file, request.getSession(), "/uploads/payment_voucher_image_url/");
	}

	@RequestMapping(value = "/upload_visa_voucher_img", method = RequestMethod.POST)
	@ResponseBody
	public Response<String> uploadVisaVoucherImage(@RequestParam MultipartFile file, HttpServletRequest request,
			HttpServletResponse response) throws IllegalStateException, IOException {
		super.setPostHeader(response);
		return super.upload(file, request.getSession(), "/uploads/visa_voucher_image_url/");
	}

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> addServiceOrder(@RequestParam(value = "type") String type,
			@RequestParam(value = "peopleNumber", required = false) Integer peopleNumber,
			@RequestParam(value = "peopleType", required = false) String peopleType,
			@RequestParam(value = "peopleRemarks", required = false) String peopleRemarks,
			@RequestParam(value = "serviceId") String serviceId,
			@RequestParam(value = "schoolId", required = false) Integer schoolId,
			@RequestParam(value = "schoolId2", required = false) Integer schoolId2,
			@RequestParam(value = "servicePackageIds", required = false) String servicePackageIds,
			@RequestParam(value = "isSettle", required = false) String isSettle,
			@RequestParam(value = "isDepositUser", required = false) String isDepositUser,
			@RequestParam(value = "subagencyId", required = false) String subagencyId,
			@RequestParam(value = "isPay") String isPay,
			@RequestParam(value = "receiveTypeId", required = false) String receiveTypeId,
			@RequestParam(value = "receiveDate", required = false) String receiveDate,
			@RequestParam(value = "receivable", required = false) String receivable,
			@RequestParam(value = "discount", required = false) String discount,
			@RequestParam(value = "received", required = false) String received,
			@RequestParam(value = "installment", required = false) Integer installment,
			@RequestParam(value = "paymentVoucherImageUrl1", required = false) String paymentVoucherImageUrl1,
			@RequestParam(value = "paymentVoucherImageUrl2", required = false) String paymentVoucherImageUrl2,
			@RequestParam(value = "perAmount", required = false) String perAmount,
			@RequestParam(value = "amount", required = false) String amount,
			@RequestParam(value = "expectAmount", required = false) String expectAmount,
			@RequestParam(value = "gst", required = false) String gst,
			@RequestParam(value = "deductGst", required = false) String deductGst,
			@RequestParam(value = "bonus", required = false) String bonus,
			@RequestParam(value = "userId") String userId,
			@RequestParam(value = "maraId", required = false) String maraId,
			@RequestParam(value = "adviserId") String adviserId,
			@RequestParam(value = "officialId", required = false) String officialId,
			@RequestParam(value = "remarks", required = false) String remarks,
			@RequestParam(value = "closedReason", required = false) String closedReason, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo == null || (StringUtil.isNotEmpty(adminUserLoginInfo.getApList())
					&& !"GW".equalsIgnoreCase(adminUserLoginInfo.getApList())))
				return new Response<Integer>(1, "仅限顾问和超级管理员能创建服务订单.", 0);
			ServiceOrderDTO serviceOrderDto = new ServiceOrderDTO();
			serviceOrderDto.setCode(UUID.randomUUID().toString());
			if (StringUtil.isNotEmpty(type))
				serviceOrderDto.setType(type);
			serviceOrderDto.setPeopleNumber(peopleNumber != null && peopleNumber > 0 ? peopleNumber : 1);
			serviceOrderDto.setPeopleType(StringUtil.isNotEmpty(peopleType) ? peopleType : "1A");
			if (StringUtil.isNotEmpty(peopleRemarks))
				serviceOrderDto.setPeopleRemarks(peopleRemarks);
			if (StringUtil.isNotEmpty(serviceId))
				serviceOrderDto.setServiceId(StringUtil.toInt(serviceId));
			if ("OVST".equalsIgnoreCase(type) && (schoolId == null || schoolId <= 0))
				return new Response<Integer>(1, "创建留学服务订单必须选择一个学校.", 0);
			if (schoolId != null && schoolId > 0)
				serviceOrderDto.setSchoolId(schoolId);
			serviceOrderDto.setState(ReviewAdviserStateEnum.PENDING.toString());
			serviceOrderDto.setSettle(isSettle != null && "true".equalsIgnoreCase(isSettle));
			serviceOrderDto.setDepositUser(isDepositUser != null && "true".equalsIgnoreCase(isDepositUser));
			if (StringUtil.isNotEmpty(subagencyId))
				serviceOrderDto.setSubagencyId(StringUtil.toInt(subagencyId));
			serviceOrderDto.setPay("true".equalsIgnoreCase(isPay));
			if (StringUtil.isNotEmpty(receiveTypeId))
				serviceOrderDto.setReceiveTypeId(StringUtil.toInt(receiveTypeId));
			if (StringUtil.isNotEmpty(receiveDate))
				serviceOrderDto.setReceiveDate(new Date(Long.parseLong(receiveDate)));
			if (StringUtil.isNotEmpty(receivable))
				serviceOrderDto.setReceivable(Double.parseDouble(receivable));
			if (StringUtil.isNotEmpty(discount))
				serviceOrderDto.setDiscount(Double.parseDouble(discount));
			if (StringUtil.isNotEmpty(received))
				serviceOrderDto.setReceived(Double.parseDouble(received));
			if (installment != null && installment > 0)
				serviceOrderDto.setInstallment(installment);
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl1))
				serviceOrderDto.setPaymentVoucherImageUrl1(paymentVoucherImageUrl1);
			// else if (serviceOrderDto.isPay())
			// return new Response<Integer>(1, "必须上传支付凭证!", 0);
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl2))
				serviceOrderDto.setPaymentVoucherImageUrl2(paymentVoucherImageUrl2);
			if (StringUtil.isNotEmpty(perAmount))
				serviceOrderDto.setPerAmount(Double.parseDouble(perAmount));
			if (StringUtil.isNotEmpty(amount))
				serviceOrderDto.setAmount(Double.parseDouble(amount));
			if (StringUtil.isNotEmpty(expectAmount))
				serviceOrderDto.setExpectAmount(Double.parseDouble(expectAmount));
			if (StringUtil.isNotEmpty(gst))
				serviceOrderDto.setGst(Double.parseDouble(gst));
			if (StringUtil.isNotEmpty(deductGst))
				serviceOrderDto.setDeductGst(Double.parseDouble(deductGst));
			if (StringUtil.isNotEmpty(bonus))
				serviceOrderDto.setBonus(Double.parseDouble(bonus));
			if (StringUtil.isNotEmpty(userId))
				if (userService.getUserById(StringUtil.toInt(userId)) == null)
					return new Response<Integer>(1, "用户编号错误(" + userId + ")，创建失败.", 0);
				else
					serviceOrderDto.setUserId(StringUtil.toInt(userId));
			if (StringUtil.isNotEmpty(maraId))
				serviceOrderDto.setMaraId(StringUtil.toInt(maraId));
			if (StringUtil.isNotEmpty(adviserId))
				serviceOrderDto.setAdviserId(StringUtil.toInt(adviserId));
			if (StringUtil.isNotEmpty(officialId))
				serviceOrderDto.setOfficialId(StringUtil.toInt(officialId));
			if (StringUtil.isNotEmpty(remarks))
				serviceOrderDto.setRemarks(remarks);
			if (StringUtil.isNotEmpty(closedReason))
				serviceOrderDto.setClosedReason(closedReason);
			if (serviceOrderService.addServiceOrder(serviceOrderDto) > 0) {
				String msg = "";
				if (adminUserLoginInfo != null)
					serviceOrderService.approval(serviceOrderDto.getId(), adminUserLoginInfo.getId(),
							ReviewAdviserStateEnum.PENDING.toString(), null, null, null);
				// 创建子服务订单
				if (StringUtil.isNotEmpty(servicePackageIds)) {
					List<String> servicePackageIdList = Arrays.asList(servicePackageIds.split(","));
					serviceOrderDto.setParentId(serviceOrderDto.getId());
					serviceOrderDto.setId(0);
					for (String servicePackageId : servicePackageIdList) {
						int id = StringUtil.toInt(servicePackageId);
						if (servicePackageService.getById(id) == null) {
							msg += "服务包不存在(" + id + "),请检查参数. ";
							continue;
						}
						serviceOrderDto.setServicePackageId(id);
						ServicePackageDTO servicePackageDto = servicePackageService.getById(id);
						if (servicePackageDto == null)
							return new Response<Integer>(1, "服务包不存在.", 0);
						if (serviceOrderService.addServiceOrder(serviceOrderDto) > 0 && adminUserLoginInfo != null)
							serviceOrderService.approval(serviceOrderDto.getId(), adminUserLoginInfo.getId(),
									ReviewAdviserStateEnum.PENDING.toString(), null, null, null);
						else
							msg += "子服务订单创建失败(" + serviceOrderDto + "). ";
					}
				}
				if (schoolId2 != null && schoolId2 > 0 && "OVST".equalsIgnoreCase(type)) {
					serviceOrderDto.setId(0);
					serviceOrderDto.setSchoolId(schoolId2);
					if (serviceOrderService.addServiceOrder(serviceOrderDto) > 0)
						msg += "创建第二学校服务订单成功(第二服务订单编号:" + serviceOrderDto.getId() + "). ";
					else
						msg += "创建第二学校服务订单失败(第二学校编号:" + schoolId2 + "). ";
				}
				return new Response<Integer>(0, msg, serviceOrderDto.getId());
			} else
				return new Response<Integer>(1, "创建失败.", 0);
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> updateServiceOrder(@RequestParam(value = "id") int id,
			@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "peopleNumber", required = false) Integer peopleNumber,
			@RequestParam(value = "peopleType", required = false) String peopleType,
			@RequestParam(value = "peopleRemarks", required = false) String peopleRemarks,
			@RequestParam(value = "serviceId", required = false) String serviceId,
			@RequestParam(value = "schoolId", required = false) String schoolId,
			@RequestParam(value = "isSettle", required = false) String isSettle,
			@RequestParam(value = "isDepositUser", required = false) String isDepositUser,
			@RequestParam(value = "subagencyId", required = false) String subagencyId,
			@RequestParam(value = "isPay", required = false) String isPay,
			@RequestParam(value = "receiveTypeId", required = false) String receiveTypeId,
			@RequestParam(value = "receiveDate", required = false) String receiveDate,
			@RequestParam(value = "receivable", required = false) String receivable,
			@RequestParam(value = "discount", required = false) String discount,
			@RequestParam(value = "received", required = false) String received,
			@RequestParam(value = "installment", required = false) Integer installment,
			@RequestParam(value = "paymentVoucherImageUrl1", required = false) String paymentVoucherImageUrl1,
			@RequestParam(value = "paymentVoucherImageUrl2", required = false) String paymentVoucherImageUrl2,
			@RequestParam(value = "perAmount", required = false) String perAmount,
			@RequestParam(value = "amount", required = false) String amount,
			@RequestParam(value = "expectAmount", required = false) String expectAmount,
			@RequestParam(value = "gst", required = false) String gst,
			@RequestParam(value = "deductGst", required = false) String deductGst,
			@RequestParam(value = "bonus", required = false) String bonus,
			@RequestParam(value = "userId", required = false) String userId,
			@RequestParam(value = "maraId", required = false) String maraId,
			@RequestParam(value = "adviserId", required = false) String adviserId,
			@RequestParam(value = "officialId", required = false) String officialId,
			@RequestParam(value = "remarks", required = false) String remarks,
			@RequestParam(value = "closedReason", required = false) String closedReason, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			ServiceOrderDTO serviceOrderDto = serviceOrderService.getServiceOrderById(id);
			if (serviceOrderDto == null)
				return new Response<Integer>(1, "服务订单不存在,修改失败.", 0);
			if (StringUtil.isNotEmpty(type))
				serviceOrderDto.setType(type);
			if (peopleNumber != null && peopleNumber > 0)
				serviceOrderDto.setPeopleNumber(peopleNumber);
			if (StringUtil.isNotEmpty(peopleType))
				serviceOrderDto.setPeopleType(peopleType);
			if (StringUtil.isNotEmpty(peopleRemarks))
				serviceOrderDto.setPeopleRemarks(peopleRemarks);
			if (StringUtil.isNotEmpty(serviceId))
				serviceOrderDto.setServiceId(StringUtil.toInt(serviceId));
			if (StringUtil.isNotEmpty(schoolId))
				serviceOrderDto.setSchoolId(StringUtil.toInt(schoolId));
			if (isSettle != null)
				serviceOrderDto.setSettle("true".equalsIgnoreCase(isSettle));
			if (isDepositUser != null)
				serviceOrderDto.setDepositUser("true".equalsIgnoreCase(isDepositUser));
			if (StringUtil.isNotEmpty(subagencyId))
				serviceOrderDto.setSubagencyId(StringUtil.toInt(subagencyId));
			if (isPay != null)
				serviceOrderDto.setPay("true".equalsIgnoreCase(isPay));
			if (StringUtil.isNotEmpty(receiveTypeId))
				serviceOrderDto.setReceiveTypeId(StringUtil.toInt(receiveTypeId));
			if (StringUtil.isNotEmpty(receiveDate))
				serviceOrderDto.setReceiveDate(new Date(Long.parseLong(receiveDate)));
			if (StringUtil.isNotEmpty(receivable))
				serviceOrderDto.setReceivable(Double.parseDouble(receivable));
			if (StringUtil.isNotEmpty(discount))
				serviceOrderDto.setDiscount(Double.parseDouble(discount));
			if (StringUtil.isNotEmpty(received))
				serviceOrderDto.setReceived(Double.parseDouble(received));
			if (installment != null && installment > 0)
				serviceOrderDto.setInstallment(installment);
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl1))
				serviceOrderDto.setPaymentVoucherImageUrl1(paymentVoucherImageUrl1);
			// else if (serviceOrderDto.isPay())
			// return new Response<Integer>(1, "必须上传支付凭证!", 0);
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl2))
				serviceOrderDto.setPaymentVoucherImageUrl2(paymentVoucherImageUrl2);
			if (StringUtil.isNotEmpty(perAmount))
				serviceOrderDto.setPerAmount(Double.parseDouble(perAmount));
			if (StringUtil.isNotEmpty(amount))
				serviceOrderDto.setAmount(Double.parseDouble(amount));
			if (StringUtil.isNotEmpty(expectAmount))
				serviceOrderDto.setExpectAmount(Double.parseDouble(expectAmount));
			if (StringUtil.isNotEmpty(gst))
				serviceOrderDto.setGst(Double.parseDouble(gst));
			if (StringUtil.isNotEmpty(deductGst))
				serviceOrderDto.setDeductGst(Double.parseDouble(deductGst));
			if (StringUtil.isNotEmpty(bonus))
				serviceOrderDto.setBonus(Double.parseDouble(bonus));
			if (StringUtil.isNotEmpty(userId))
				serviceOrderDto.setUserId(StringUtil.toInt(userId));
			if (StringUtil.isNotEmpty(maraId))
				serviceOrderDto.setMaraId(StringUtil.toInt(maraId));
			if (StringUtil.isNotEmpty(adviserId))
				serviceOrderDto.setAdviserId(StringUtil.toInt(adviserId));
			if (StringUtil.isNotEmpty(officialId))
				serviceOrderDto.setOfficialId(StringUtil.toInt(officialId));
			if (StringUtil.isNotEmpty(remarks))
				serviceOrderDto.setRemarks(remarks);
			if (StringUtil.isNotEmpty(closedReason))
				serviceOrderDto.setClosedReason(closedReason);
			if (serviceOrderDto.getServicePackageId() > 0) {
				ServicePackageDTO servicePackageDto = servicePackageService
						.getById(serviceOrderDto.getServicePackageId());
				if (servicePackageDto == null)
					return new Response<Integer>(1, "服务包不存在:" + serviceOrderDto.getServicePackageId(), 0);
				// if (serviceOrderDto.getOfficialId() <= 0 &&
				// "SIV".equalsIgnoreCase(serviceOrderDto.getType()))
				if (StringUtil.isEmpty(officialId) && "SIV".equalsIgnoreCase(serviceOrderDto.getType()))
					return new Response<Integer>(1, "必须选择文案.", 0);
				// if (serviceOrderDto.getMaraId() <= 0 &&
				// "SIV".equalsIgnoreCase(serviceOrderDto.getType())
				if (StringUtil.isEmpty(maraId) && "SIV".equalsIgnoreCase(serviceOrderDto.getType())
						&& !"EOI".equalsIgnoreCase(servicePackageDto.getType()))
					return new Response<Integer>(1, "必须选择Mara.", 0);
			}
			int i = serviceOrderService.updateServiceOrder(serviceOrderDto);
			if (i > 0) {
				return new Response<Integer>(0, i);
			} else {
				return new Response<Integer>(1, "修改失败.", 0);
			}
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/updateVoucherImageUrl", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> updateVoucherImageUrl(@RequestParam(value = "id") int id,
			@RequestParam(value = "paymentVoucherImageUrl", required = false) String paymentVoucherImageUrl,
			@RequestParam(value = "visaVoucherImageUrl", required = false) String visaVoucherImageUrl,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo != null)
				if (adminUserLoginInfo == null || (StringUtil.isNotEmpty(adminUserLoginInfo.getApList())
						&& !"WA".equalsIgnoreCase(adminUserLoginInfo.getApList())))
					return new Response<Integer>(1, "仅限文案修改.", null);
			ServiceOrderDTO serviceOrderDto = serviceOrderService.getServiceOrderById(id);
			if (serviceOrderDto == null)
				return new Response<Integer>(1, "服务订单不存在,修改失败.", 0);
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl))
				serviceOrderDto.setPaymentVoucherImageUrl3(paymentVoucherImageUrl);
			if (StringUtil.isNotEmpty(visaVoucherImageUrl))
				serviceOrderDto.setVisaVoucherImageUrl(visaVoucherImageUrl);
			int i = serviceOrderService.updateServiceOrder(serviceOrderDto);
			if (i > 0) {
				return new Response<Integer>(0, i);
			} else {
				return new Response<Integer>(1, "修改失败.", 0);
			}
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/updateRemarks", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> updateRemarks(@RequestParam(value = "id") int id,
			@RequestParam(value = "remarks", required = false) String remarks, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo != null)
				if (adminUserLoginInfo == null || (StringUtil.isNotEmpty(adminUserLoginInfo.getApList())
						&& !"WA".equalsIgnoreCase(adminUserLoginInfo.getApList())))
					return new Response<Integer>(1, "仅限文案修改.", null);
			ServiceOrderDTO serviceOrderDto = serviceOrderService.getServiceOrderById(id);
			if (serviceOrderDto == null)
				return new Response<Integer>(1, "服务订单不存在,修改失败.", 0);
			if (StringUtil.isNotEmpty(remarks))
				serviceOrderDto.setRemarks(remarks);
			int i = serviceOrderService.updateServiceOrder(serviceOrderDto);
			return i > 0 ? new Response<Integer>(0, i) : new Response<Integer>(1, "修改失败.", 0);
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/count", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> countServiceOrder(@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "state", required = false) String state,
			@RequestParam(value = "userId", required = false) String userId,
			@RequestParam(value = "maraId", required = false) String maraId,
			@RequestParam(value = "adviserId", required = false) String adviserId,
			@RequestParam(value = "officialId", required = false) String officialId, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			String excludeState = null;
			List<String> stateList = null;
			if (state != null)
				stateList = new ArrayList<>(Arrays.asList(state.split(",")));
			List<String> reviewStateList = null;
			Integer newAdviserId = getAdviserId(request);
			if (newAdviserId != null)
				adviserId = newAdviserId + "";
			Integer newMaraId = getMaraId(request);
			if (newMaraId != null) {
				maraId = newMaraId + "";
				excludeState = ReviewAdviserStateEnum.PENDING.toString();
				reviewStateList = new ArrayList<>();
				reviewStateList.add(ServiceOrderReviewStateEnum.ADVISER.toString());
				reviewStateList.add(ServiceOrderReviewStateEnum.MARA.toString());
				reviewStateList.add(ServiceOrderReviewStateEnum.OFFICIAL.toString());
			}
			Integer newOfficialId = getOfficialId(request);
			if (newOfficialId != null) {
				officialId = newOfficialId + "";
				excludeState = ReviewAdviserStateEnum.PENDING.toString();
			}

			return new Response<Integer>(0,
					serviceOrderService.countServiceOrder(type, excludeState, stateList, reviewStateList,
							StringUtil.toInt(userId), StringUtil.toInt(maraId), StringUtil.toInt(adviserId),
							StringUtil.toInt(officialId), 0));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<ServiceOrderDTO>> listServiceOrder(@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "state", required = false) String state,
			@RequestParam(value = "userId", required = false) String userId,
			@RequestParam(value = "maraId", required = false) String maraId,
			@RequestParam(value = "adviserId", required = false) String adviserId,
			@RequestParam(value = "officialId", required = false) String officialId,
			@RequestParam(value = "pageNum") int pageNum, @RequestParam(value = "pageSize") int pageSize,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			String excludeState = null;
			List<String> stateList = null;
			if (state != null)
				stateList = new ArrayList<>(Arrays.asList(state.split(",")));
			List<String> reviewStateList = null;
			Integer newAdviserId = getAdviserId(request);
			if (newAdviserId != null)
				adviserId = newAdviserId + "";
			Integer newMaraId = getMaraId(request);
			if (newMaraId != null) {
				maraId = newMaraId + "";
				excludeState = ReviewAdviserStateEnum.PENDING.toString();
				reviewStateList = new ArrayList<>();
				reviewStateList.add(ServiceOrderReviewStateEnum.ADVISER.toString());
				reviewStateList.add(ServiceOrderReviewStateEnum.MARA.toString());
				reviewStateList.add(ServiceOrderReviewStateEnum.OFFICIAL.toString());
			}
			Integer newOfficialId = getOfficialId(request);
			if (newOfficialId != null) {
				officialId = newOfficialId + "";
				excludeState = ReviewAdviserStateEnum.PENDING.toString();
			}

			return new Response<List<ServiceOrderDTO>>(0,
					serviceOrderService.listServiceOrder(type, excludeState, stateList, reviewStateList,
							StringUtil.toInt(userId), StringUtil.toInt(maraId), StringUtil.toInt(adviserId),
							StringUtil.toInt(officialId), 0, pageNum, pageSize));
		} catch (ServiceException e) {
			return new Response<List<ServiceOrderDTO>>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/get", method = RequestMethod.GET)
	@ResponseBody
	public Response<ServiceOrderDTO> getServiceOrder(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<ServiceOrderDTO>(0, serviceOrderService.getServiceOrderById(id));
		} catch (ServiceException e) {
			return new Response<ServiceOrderDTO>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/delete", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> deleteServiceOrder(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, serviceOrderService.deleteServiceOrderById(id));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/finish", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> finish(@RequestParam(value = "id") int id, HttpServletRequest request,
			HttpServletResponse response) {
		super.setPostHeader(response);
		try {
			return serviceOrderService.getServiceOrderById(id) != null
					? new Response<Integer>(0, serviceOrderService.finish(id)) : null;
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), -1);
		}
	}

	@RequestMapping(value = "/approval", method = RequestMethod.POST)
	@ResponseBody
	public Response<ServiceOrderDTO> approval(@RequestParam(value = "id") int id,
			@RequestParam(value = "state") String state,
			@RequestParam(value = "subagencyId", required = false) String subagencyId,
			@RequestParam(value = "remarks", required = false) String remarks, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			if (ReviewAdviserStateEnum.CLOSE.toString().equalsIgnoreCase(state))
				return new Response<ServiceOrderDTO>(1, "关闭操作请调用'refuse'接口.", null);
			// 获取服务订单
			ServiceOrderDTO serviceOrderDto = null;
			try {
				serviceOrderDto = serviceOrderService.getServiceOrderById(id);
			} catch (ServiceException e) {
				return new Response<ServiceOrderDTO>(1, e.getMessage(), null);
			}
			if (serviceOrderDto == null) {
				ServiceException se = new ServiceException("服务订单不存在:" + id);
				se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
				throw se;
			}
			if (serviceOrderDto.getPaymentVoucherImageUrl1() == null
					&& serviceOrderDto.getPaymentVoucherImageUrl2() == null) {
				ServiceException se = new ServiceException("支付凭证不能为空!");
				se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
				throw se;
			}
			// 审核
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo != null)
				if (StringUtil.isEmpty(adminUserLoginInfo.getApList())
						|| "GW".equalsIgnoreCase(adminUserLoginInfo.getApList())) {
					if (ReviewAdviserStateEnum.get(state) != null)
						if (ReviewAdviserStateEnum.REVIEW.toString().equals(state.toUpperCase())) { // 顾问审核
							// 如果有子订单,就一起提交审核
							if (serviceOrderDto.getParentId() == 0
									&& "SIV".equalsIgnoreCase(serviceOrderDto.getType())) {
								List<ServiceOrderDTO> serviceOrderList = serviceOrderService.listServiceOrder(null,
										null, null, null, 0, 0, 0, 0, serviceOrderDto.getId(), 0, 10);
								for (ServiceOrderDTO so : serviceOrderList) {
									if (so.getServicePackage() == null)
										return new Response<ServiceOrderDTO>(1, "子订单没有服务包.", so);
									if (so.getOfficialId() <= 0 && "SIV".equalsIgnoreCase(so.getType()))
										return new Response<ServiceOrderDTO>(1, "子订单必须选择文案.", so);
									if (so.getMaraId() <= 0 && "SIV".equalsIgnoreCase(so.getType())
											&& !"EOI".equalsIgnoreCase(so.getServicePackage().getType()))
										return new Response<ServiceOrderDTO>(1, "子订单必须选择Mara.", so);
									serviceOrderService.approval(so.getId(), adminUserLoginInfo.getId(),
											state.toUpperCase(), null, null, null);
								}
							}
							return new Response<ServiceOrderDTO>(0, serviceOrderService.approval(id,
									adminUserLoginInfo.getId(), state.toUpperCase(), null, null, null));
						} else if (ReviewAdviserStateEnum.PAID.toString().equals(state.toUpperCase())) { // 顾问支付同时修改文案状态
							serviceOrderService.finish(id);
							return new Response<ServiceOrderDTO>(0,
									serviceOrderService.approval(id, adminUserLoginInfo.getId(), state.toUpperCase(),
											null, ReviewOfficialStateEnum.PAID.toString(), null));
						} else if (ReviewAdviserStateEnum.COMPLETE.toString().equals(state.toUpperCase())) { // 顾问完成同时修改文案和会计状态
							serviceOrderService.finish(id);
							return new Response<ServiceOrderDTO>(0,
									serviceOrderService.approval(id, adminUserLoginInfo.getId(), state.toUpperCase(),
											null, ReviewOfficialStateEnum.COMPLETE.toString(), null));
						} else
							return new Response<ServiceOrderDTO>(0, serviceOrderService.approval(id,
									adminUserLoginInfo.getId(), state.toUpperCase(), null, null, null));
					else
						return new Response<ServiceOrderDTO>(1, "state错误!(" + state + ")", null);
				} else if ("MA".equalsIgnoreCase(adminUserLoginInfo.getApList())) {
					if (!"VISA".equalsIgnoreCase(serviceOrderDto.getType()))
						return new Response<ServiceOrderDTO>(1, "Mara审核仅限签证服务订单!", null);
					if (ReviewMaraStateEnum.get(state) != null
							&& ReviewMaraStateEnum.FINISH.toString().equals(state.toUpperCase())) { // Mara审核通过同时修改状态
						serviceOrderService.updateServiceOrderRviewState(id,
								ServiceOrderReviewStateEnum.MARA.toString());
						return new Response<ServiceOrderDTO>(0,
								serviceOrderService.approval(id, adminUserLoginInfo.getId(), null, state.toUpperCase(),
										ReviewOfficialStateEnum.REVIEW.toString(), null));
					}
					if (ReviewMaraStateEnum.get(state) != null
							&& !ReviewMaraStateEnum.REVIEW.toString().equals(state.toUpperCase())) // mara调用approval方法不能驳回
						return new Response<ServiceOrderDTO>(0, serviceOrderService.approval(id,
								adminUserLoginInfo.getId(), null, state.toUpperCase(), null, null));
					else
						return new Response<ServiceOrderDTO>(1, "state错误!(" + state + ")", null);
				} else if ("WA".equalsIgnoreCase(adminUserLoginInfo.getApList())) {
					if (ReviewOfficialStateEnum.get(state) != null
							&& !ReviewOfficialStateEnum.CLOSE.toString().equals(state.toUpperCase())) { // 文案调用approval方法不能关闭
						if (ReviewOfficialStateEnum.FINISH.toString().equals(state.toUpperCase())) // 文案审核通过同时修改状态
							serviceOrderService.updateServiceOrderRviewState(id,
									ServiceOrderReviewStateEnum.OFFICIAL.toString());
						if (ReviewOfficialStateEnum.WAIT.toString().equals(state.toUpperCase())) { // 文案提交mara审核
							serviceOrderService.updateServiceOrderRviewState(id,
									ServiceOrderReviewStateEnum.ADVISER.toString());
							waUpdateSubagency(serviceOrderDto, subagencyId);
							waUpdateRemarks(serviceOrderDto, remarks);
							return new Response<ServiceOrderDTO>(0,
									serviceOrderService.approval(id, adminUserLoginInfo.getId(), null,
											ReviewMaraStateEnum.WAIT.toString(), state.toUpperCase(), null));
						} else if (ReviewOfficialStateEnum.APPLY.toString().equals(state.toUpperCase())) { // 文案申请同时修改顾问状态
							serviceOrderService.finish(id);
							waUpdateRemarks(serviceOrderDto, remarks);
							return new Response<ServiceOrderDTO>(0,
									serviceOrderService.approval(id, adminUserLoginInfo.getId(),
											ReviewAdviserStateEnum.APPLY.toString(), null, state.toUpperCase(), null));
						} else if (ReviewOfficialStateEnum.PAID.toString().equals(state.toUpperCase())) { // 文案支付同时修改顾问状态
							serviceOrderService.finish(id);
							return new Response<ServiceOrderDTO>(0,
									serviceOrderService.approval(id, adminUserLoginInfo.getId(),
											ReviewAdviserStateEnum.PAID.toString(), null, state.toUpperCase(), null));
						} else if (ReviewOfficialStateEnum.COMPLETE.toString().equals(state.toUpperCase())) { // 文案完成同时修改顾问和会计状态
							serviceOrderService.finish(id);
							waUpdateRemarks(serviceOrderDto, remarks);
							return new Response<ServiceOrderDTO>(0,
									serviceOrderService.approval(id, adminUserLoginInfo.getId(),
											ReviewAdviserStateEnum.COMPLETE.toString(), null, state.toUpperCase(),
											null));
						} else {
							waUpdateSubagency(serviceOrderDto, subagencyId);
							waUpdateRemarks(serviceOrderDto, remarks);
							return new Response<ServiceOrderDTO>(0, serviceOrderService.approval(id,
									adminUserLoginInfo.getId(), null, null, state.toUpperCase(), null));
						}
					} else
						return new Response<ServiceOrderDTO>(1, "state错误!(" + state + ")", null);
				} else
					return new Response<ServiceOrderDTO>(1, "该用户无审核权限!", null);
			else
				return new Response<ServiceOrderDTO>(1, "请登录!", null);
		} catch (ServiceException e) {
			return new Response<ServiceOrderDTO>(1, e.getMessage(), null);
		}
	}

	private void waUpdateSubagency(ServiceOrderDTO serviceOrderDto, String subagencyId) throws ServiceException {
		if (StringUtil.isNotEmpty(subagencyId)) {
			serviceOrderDto.setSubagencyId(StringUtil.toInt(subagencyId));
			if (serviceOrderService.updateServiceOrder(serviceOrderDto) <= 0)
				LOG.error("文案修改失败! (subagencyId:" + subagencyId + ")");
		}
	}

	private void waUpdateRemarks(ServiceOrderDTO serviceOrderDto, String remarks) throws ServiceException {
		if (StringUtil.isNotEmpty(remarks)) {
			serviceOrderDto.setRemarks(remarks);
			if (serviceOrderService.updateServiceOrder(serviceOrderDto) <= 0)
				LOG.error("文案修改失败! (remarks:" + remarks + ")");
		}
	}

	@RequestMapping(value = "/refuse", method = RequestMethod.POST)
	@ResponseBody
	public Response<ServiceOrderDTO> refuse(@RequestParam(value = "id") int id,
			@RequestParam(value = "state") String state,
			@RequestParam(value = "closedReason", required = false) String closedReason, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			if (ReviewAdviserStateEnum.COMPLETE.toString().equalsIgnoreCase(state)
					|| ReviewOfficialStateEnum.FINISH.toString().equalsIgnoreCase(state))
				return new Response<ServiceOrderDTO>(1, "完成操作请调用'approval'接口.", null);
			// 获取服务订单
			ServiceOrderDTO serviceOrderDto = null;
			try {
				serviceOrderDto = serviceOrderService.getServiceOrderById(id);
			} catch (ServiceException e) {
				return new Response<ServiceOrderDTO>(1, e.getMessage(), null);
			}
			// 审核
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo != null)
				if (StringUtil.isEmpty(adminUserLoginInfo.getApList())
						|| "GW".equalsIgnoreCase(adminUserLoginInfo.getApList())) {
					if (ReviewAdviserStateEnum.get(state) != null)
						if (ReviewAdviserStateEnum.PENDING.toString().equals(state.toUpperCase())) // 顾问撤回同时修改文案和mara状态
							return new Response<ServiceOrderDTO>(0,
									serviceOrderService.refuse(id, adminUserLoginInfo.getId(), state.toUpperCase(),
											null, ReviewOfficialStateEnum.PENDING.toString(),
											ReviewMaraStateEnum.WAIT.toString()));
						else if (ReviewOfficialStateEnum.REVIEW.toString().equals(state.toUpperCase())) // 顾问驳回同时修改mara状态
							return new Response<ServiceOrderDTO>(0,
									serviceOrderService.refuse(id, adminUserLoginInfo.getId(), state.toUpperCase(),
											ReviewMaraStateEnum.REVIEW.toString(), null, null));
						else if (ReviewOfficialStateEnum.CLOSE.toString().equals(state.toUpperCase())) { // 顾问关闭同时修改文案状态
							serviceOrderService.finish(id);
							// 更新关闭原因
							if (StringUtil.isNotEmpty(closedReason)) {
								serviceOrderDto.setClosedReason(closedReason);
								serviceOrderService.updateServiceOrder(serviceOrderDto);
							}
							return new Response<ServiceOrderDTO>(0,
									serviceOrderService.refuse(id, adminUserLoginInfo.getId(), state.toUpperCase(),
											null, ReviewOfficialStateEnum.CLOSE.toString(), null));
						} else
							return new Response<ServiceOrderDTO>(0, serviceOrderService.refuse(id,
									adminUserLoginInfo.getId(), state.toUpperCase(), null, null, null));
					else
						return new Response<ServiceOrderDTO>(1, "state错误!(" + state + ")", null);
				} else if ("MA".equalsIgnoreCase(adminUserLoginInfo.getApList())) {
					if (!"VISA".equalsIgnoreCase(serviceOrderDto.getType()))
						return new Response<ServiceOrderDTO>(1, "Mara审核仅限签证服务订单!", null);
					if (ReviewMaraStateEnum.get(state) != null) {
						if (ReviewMaraStateEnum.REVIEW.toString().equals(state.toUpperCase())) { // mara驳回同时修改顾问状态
							serviceOrderService.updateServiceOrderRviewState(id, null);
							return new Response<ServiceOrderDTO>(0,
									serviceOrderService.refuse(id, adminUserLoginInfo.getId(),
											ReviewAdviserStateEnum.REVIEW.toString(), state.toUpperCase(),
											ReviewOfficialStateEnum.REVIEW.toString(), null));
						} else
							return new Response<ServiceOrderDTO>(0, serviceOrderService.refuse(id,
									adminUserLoginInfo.getId(), null, state.toUpperCase(), null, null));
					} else
						return new Response<ServiceOrderDTO>(1, "state错误!(" + state + ")", null);
				} else if ("WA".equalsIgnoreCase(adminUserLoginInfo.getApList())) {
					if (ReviewOfficialStateEnum.get(state) != null)
						if (ReviewOfficialStateEnum.CLOSE.toString().equals(state.toUpperCase())) { // 文案关闭同时修改顾问状态
							serviceOrderService.finish(id);
							// 更新关闭原因
							if (StringUtil.isNotEmpty(closedReason)) {
								serviceOrderDto.setClosedReason(closedReason);
								serviceOrderService.updateServiceOrder(serviceOrderDto);
							}
							return new Response<ServiceOrderDTO>(0,
									serviceOrderService.refuse(id, adminUserLoginInfo.getId(),
											ReviewAdviserStateEnum.CLOSE.toString(), null, state.toUpperCase(), null));
						} else if (ReviewOfficialStateEnum.PENDING.toString().equals(state.toUpperCase())) // 文案驳回同时修改顾问状态
							return new Response<ServiceOrderDTO>(0,
									serviceOrderService.refuse(id, adminUserLoginInfo.getId(),
											ReviewAdviserStateEnum.PENDING.toString(), null, state.toUpperCase(),
											null));
						else
							return new Response<ServiceOrderDTO>(0, serviceOrderService.refuse(id,
									adminUserLoginInfo.getId(), null, null, state.toUpperCase(), null));
					else
						return new Response<ServiceOrderDTO>(1, "state错误!(" + state + ")", null);
				} else
					return new Response<ServiceOrderDTO>(1, "该用户无审核权限!", null);
			else
				return new Response<ServiceOrderDTO>(1, "请登录!", null);
		} catch (ServiceException e) {
			return new Response<ServiceOrderDTO>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/reviews", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<ServiceOrderReviewDTO>> reviews(@RequestParam(value = "serviceOrderId") String serviceOrderId,
			HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<List<ServiceOrderReviewDTO>>(0,
					serviceOrderService.reviews(StringUtil.toInt(serviceOrderId)));
		} catch (ServiceException e) {
			return new Response<List<ServiceOrderReviewDTO>>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/addComment", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> addComment(@RequestParam(value = "adminUserId", required = false) Integer adminUserId,
			@RequestParam(value = "serviceOrderId") Integer serviceOrderId,
			@RequestParam(value = "content") String content, HttpServletRequest request, HttpServletResponse response) {
		try {
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			super.setPostHeader(response);
			ServiceOrderCommentDTO serviceOrderCommentDto = new ServiceOrderCommentDTO();
			serviceOrderCommentDto
					.setAdminUserId(adminUserLoginInfo != null ? adminUserLoginInfo.getId() : adminUserId);
			serviceOrderCommentDto.setServiceOrderId(serviceOrderId);
			serviceOrderCommentDto.setContent(content);
			if (serviceOrderService.addComment(serviceOrderCommentDto) > 0)
				return new Response<Integer>(0, serviceOrderCommentDto.getId());
			else
				return new Response<Integer>(1, "创建失败.", 0);
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/countComment", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> countComment(@RequestParam(value = "serviceOrderId") Integer serviceOrderId,
			HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, serviceOrderService.listComment(serviceOrderId).size());
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/listComment", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<ServiceOrderCommentDTO>> listComment(
			@RequestParam(value = "serviceOrderId") Integer serviceOrderId, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<List<ServiceOrderCommentDTO>>(0, serviceOrderService.listComment(serviceOrderId));
		} catch (ServiceException e) {
			return new Response<List<ServiceOrderCommentDTO>>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/deleteComment", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> deleteComment(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, serviceOrderService.deleteComment(id));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), 0);
		}
	}

}
