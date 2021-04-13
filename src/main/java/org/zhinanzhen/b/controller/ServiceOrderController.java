package org.zhinanzhen.b.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.zhinanzhen.b.controller.nodes.SONodeFactory;
import org.zhinanzhen.b.dao.pojo.ServiceOrderReadcommittedDateDO;
import org.zhinanzhen.b.service.*;
import org.zhinanzhen.b.service.pojo.*;
import org.zhinanzhen.b.service.pojo.ant.Sorter;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.ListResponse;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.RegionService;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.UserService;
import org.zhinanzhen.tb.service.pojo.AdviserDTO;
import org.zhinanzhen.tb.service.pojo.RegionDTO;
import org.zhinanzhen.tb.utils.SendEmailUtil;

import com.alibaba.fastjson.JSON;
import com.ikasoa.core.ErrorCodeEnum;
import com.ikasoa.core.utils.ListUtil;
import com.ikasoa.core.utils.ObjectUtil;
import com.ikasoa.core.utils.StringUtil;
import com.ikasoa.web.workflow.Context;
import com.ikasoa.web.workflow.Node;
import com.ikasoa.web.workflow.Workflow;
import com.ikasoa.web.workflow.WorkflowStarter;
import com.ikasoa.web.workflow.impl.WorkflowStarterImpl;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/serviceOrder")
public class ServiceOrderController extends BaseController {

	private static final Logger LOG = LoggerFactory.getLogger(ServiceOrderController.class);

	private static WorkflowStarter workflowStarter = new WorkflowStarterImpl();

	@Resource
	ServiceOrderService serviceOrderService;

	@Resource
	UserService userService;

	@Resource
	ServicePackageService servicePackageService;

	@Resource
	RegionService regionService;

	@Resource
	ServiceAssessService serviceAssessService;

	@Resource
	CommissionOrderService commissionOrderService;

	@Resource
	ServiceOrderReadcommittedDateService serviceOrderReadcommittedDateService;
	
	@Resource
	SONodeFactory soNodeFactory;

	@Resource
	WXWorkService wxWorkService;

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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
		return super.upload2(file, request.getSession(), "/uploads/payment_voucher_image_url_s/");
	}

	@RequestMapping(value = "/upload_visa_voucher_img", method = RequestMethod.POST)
	@ResponseBody
	public Response<String> uploadVisaVoucherImage(@RequestParam MultipartFile file, HttpServletRequest request,
			HttpServletResponse response) throws IllegalStateException, IOException {
		super.setPostHeader(response);
		return super.upload2(file, request.getSession(), "/uploads/visa_voucher_image_url/");
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
			@RequestParam(value = "schoolId3", required = false) Integer schoolId3,
			@RequestParam(value = "schoolId4", required = false) Integer schoolId4,
			@RequestParam(value = "schoolId5", required = false) Integer schoolId5,
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
			@RequestParam(value = "paymentVoucherImageUrl3", required = false) String paymentVoucherImageUrl3,
			@RequestParam(value = "paymentVoucherImageUrl4", required = false) String paymentVoucherImageUrl4,
			@RequestParam(value = "paymentVoucherImageUrl5", required = false) String paymentVoucherImageUrl5,
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
			@RequestParam(value = "closedReason", required = false) String closedReason,
			@RequestParam(value = "information", required = false) String information,
			@RequestParam(value = "isHistory", required = false) String isHistory,
			@RequestParam(value = "nutCloud") String nutCloud,
			@RequestParam(value = "serviceAssessId", required = false) String serviceAssessId,
			@RequestParam(value = "verifyCode", required = false) String verifyCode, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo == null || (!"SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())
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
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl3))
				serviceOrderDto.setPaymentVoucherImageUrl3(paymentVoucherImageUrl3);
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl4))
				serviceOrderDto.setPaymentVoucherImageUrl4(paymentVoucherImageUrl4);
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl5))
				serviceOrderDto.setPaymentVoucherImageUrl5(paymentVoucherImageUrl5);
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
			if (StringUtil.isNotEmpty(maraId) && !"SIV".equalsIgnoreCase(serviceOrderDto.getType())
					&& !"MT".equalsIgnoreCase(serviceOrderDto.getType())) // SIV主订单和MT主订单不需要mara
				serviceOrderDto.setMaraId(StringUtil.toInt(maraId));
			if (StringUtil.isNotEmpty(adviserId))
				serviceOrderDto.setAdviserId(StringUtil.toInt(adviserId));
			if (StringUtil.isNotEmpty(officialId) && !"SIV".equalsIgnoreCase(serviceOrderDto.getType())
					&& !"MT".equalsIgnoreCase(serviceOrderDto.getType())) // SIV主订单和MT主订单不需要文案
				serviceOrderDto.setOfficialId(StringUtil.toInt(officialId));
			if (StringUtil.isNotEmpty(remarks))
				serviceOrderDto.setRemarks(remarks);
			if (StringUtil.isNotEmpty(closedReason))
				serviceOrderDto.setClosedReason(closedReason);
			if (StringUtil.isNotEmpty(information))
				serviceOrderDto.setInformation(information);
			serviceOrderDto.setHistory(isHistory != null && "true".equalsIgnoreCase(isHistory));
			if (StringUtil.isNotEmpty(nutCloud))
				serviceOrderDto.setNutCloud(nutCloud);
			if (StringUtil.isNotEmpty(serviceAssessId)) {
				if ( !type.equalsIgnoreCase("SIV") && serviceAssessService.seleteAssessByServiceId(serviceId).size() == 0 )
					return new Response(1, "当前服务编号不是评估(" + serviceId + ")，创建失败.", 0);
				serviceOrderDto.setServiceAssessId(serviceAssessId);
			}
			if (isHistory != null && "true".equalsIgnoreCase(isHistory))
				serviceOrderDto.setRealPeopleNumber(0);
			else
				serviceOrderDto.setRealPeopleNumber(peopleNumber != null && peopleNumber > 0 ? peopleNumber : 1);
			if (StringUtil.isNotEmpty(verifyCode))
				serviceOrderDto.setVerifyCode(verifyCode.replace("$","").replace("#","").replace(" ",""));
			if (serviceOrderService.addServiceOrder(serviceOrderDto) > 0) {
				String msg = "";
				if (adminUserLoginInfo != null)
					serviceOrderService.approval(serviceOrderDto.getId(), adminUserLoginInfo.getId(),
							serviceOrderDto.getState(), null, null, null);
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
						serviceOrderDto.setServiceAssessId("CA".equalsIgnoreCase(servicePackageDto.getType()) ? serviceAssessId : null);
						serviceOrderDto.setType("VISA"); // 独立技术移民子订单为VISA
						serviceOrderDto.setPay(false); // 独立技术移民子订单都未支付
						if (StringUtil.isNotEmpty(maraId))
							serviceOrderDto.setMaraId(StringUtil.toInt(maraId)); // 独立技术移民子订单需要mara
						if (StringUtil.isNotEmpty(officialId))
							serviceOrderDto.setOfficialId(StringUtil.toInt(officialId)); // 独立技术移民子订单需要文案
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
					if (serviceOrderService.addServiceOrder(serviceOrderDto) > 0) {
						if (adminUserLoginInfo != null)
							serviceOrderService.approval(serviceOrderDto.getId(), adminUserLoginInfo.getId(),
									serviceOrderDto.getState(), null, null, null);
						msg += "创建第二学校服务订单成功(第二服务订单编号:" + serviceOrderDto.getId() + "). ";
					}

					else
						msg += "创建第二学校服务订单失败(第二学校编号:" + schoolId2 + "). ";
				}
				if (schoolId3 != null && schoolId3 > 0 && "OVST".equalsIgnoreCase(type)) {
					serviceOrderDto.setId(0);
					serviceOrderDto.setSchoolId(schoolId3);
					if (serviceOrderService.addServiceOrder(serviceOrderDto) > 0) {
						if (adminUserLoginInfo != null)
							serviceOrderService.approval(serviceOrderDto.getId(), adminUserLoginInfo.getId(),
									serviceOrderDto.getState(), null, null, null);
						msg += "创建第三学校服务订单成功(第三服务订单编号:" + serviceOrderDto.getId() + "). ";
					} else
						msg += "创建第三学校服务订单失败(第三学校编号:" + schoolId3 + "). ";
				}
				if (schoolId4 != null && schoolId4 > 0 && "OVST".equalsIgnoreCase(type)) {
					serviceOrderDto.setId(0);
					serviceOrderDto.setSchoolId(schoolId4);
					if (serviceOrderService.addServiceOrder(serviceOrderDto) > 0) {
						if (adminUserLoginInfo != null)
							serviceOrderService.approval(serviceOrderDto.getId(), adminUserLoginInfo.getId(),
									serviceOrderDto.getState(), null, null, null);
						msg += "创建第四学校服务订单成功(第四服务订单编号:" + serviceOrderDto.getId() + "). ";
					} else
						msg += "创建第四学校服务订单失败(第四学校编号:" + schoolId4 + "). ";
				}
				if (schoolId5 != null && schoolId5 > 0 && "OVST".equalsIgnoreCase(type)) {
					serviceOrderDto.setId(0);
					serviceOrderDto.setSchoolId(schoolId5);
					if (serviceOrderService.addServiceOrder(serviceOrderDto) > 0) {
						if (adminUserLoginInfo != null)
							serviceOrderService.approval(serviceOrderDto.getId(), adminUserLoginInfo.getId(),
									serviceOrderDto.getState(), null, null, null);
						msg += "创建第五学校服务订单成功(第五服务订单编号:" + serviceOrderDto.getId() + "). ";
					} else
						msg += "创建第五学校服务订单失败(第五学校编号:" + schoolId5 + "). ";
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
			@RequestParam(value = "paymentVoucherImageUrl3", required = false) String paymentVoucherImageUrl3,
			@RequestParam(value = "paymentVoucherImageUrl4", required = false) String paymentVoucherImageUrl4,
			@RequestParam(value = "paymentVoucherImageUrl5", required = false) String paymentVoucherImageUrl5,
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
			@RequestParam(value = "information", required = false) String information,
			@RequestParam(value = "isHistory", required = false) String isHistory,
			@RequestParam(value = "nutCloud", required = false) String nutCloud,
			@RequestParam(value = "serviceAssessId", required = false) String serviceAssessId,
			@RequestParam(value = "verifyCode", required = false) String verifyCode, HttpServletResponse response) {
//		if (getOfficialAdminId(request) != null)
//			return new Response<Integer>(1, "文案管理员不可操作服务订单.", 0);
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
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl3))
				serviceOrderDto.setPaymentVoucherImageUrl3(paymentVoucherImageUrl3);
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl4))
				serviceOrderDto.setPaymentVoucherImageUrl4(paymentVoucherImageUrl4);
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl5))
				serviceOrderDto.setPaymentVoucherImageUrl5(paymentVoucherImageUrl5);
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
				if (StringUtil.isEmpty(officialId)
						&& ("SIV".equalsIgnoreCase(serviceOrderDto.getType()) || serviceOrderDto.getParentId() > 0))
					return new Response<Integer>(1, "必须选择文案.", 0);
				// if (serviceOrderDto.getMaraId() <= 0 &&
				// "SIV".equalsIgnoreCase(serviceOrderDto.getType())
				if (StringUtil.isEmpty(maraId)
						&& ("SIV".equalsIgnoreCase(serviceOrderDto.getType()) || serviceOrderDto.getParentId() > 0)
						&& !"EOI".equalsIgnoreCase(servicePackageDto.getType()))
					return new Response<Integer>(1, "必须选择Mara.", 0);
			}
			if (StringUtil.isNotEmpty(information))
				serviceOrderDto.setInformation(information);
			serviceOrderDto.setHistory(isHistory != null && "true".equalsIgnoreCase(isHistory));
			if (StringUtil.isNotEmpty(nutCloud))
				serviceOrderDto.setNutCloud(nutCloud);
			if (StringUtil.isNotEmpty(serviceAssessId)) {
				if (serviceAssessService.seleteAssessByServiceId(serviceId).size() == 0)
					return new Response(1, "当前服务编号不是评估(" + serviceId + ") .", 0);
				serviceOrderDto.setServiceAssessId(serviceAssessId);
			} else
				serviceOrderDto.setServiceAssessId(null);
			if (StringUtil.isNotEmpty(verifyCode))
				serviceOrderDto.setVerifyCode(verifyCode.replace("$","").replace("#","").replace(" ",""));
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
			@RequestParam(value = "coePaymentVoucherImageUrl1", required = false) String coePaymentVoucherImageUrl1,
			@RequestParam(value = "coePaymentVoucherImageUrl2", required = false) String coePaymentVoucherImageUrl2,
			@RequestParam(value = "coePaymentVoucherImageUrl3", required = false) String coePaymentVoucherImageUrl3,
			@RequestParam(value = "coePaymentVoucherImageUrl4", required = false) String coePaymentVoucherImageUrl4,
			@RequestParam(value = "coePaymentVoucherImageUrl5", required = false) String coePaymentVoucherImageUrl5,
			@RequestParam(value = "visaVoucherImageUrl", required = false) String visaVoucherImageUrl,
			HttpServletRequest request, HttpServletResponse response) {
//		if (getOfficialAdminId(request) != null)
//			return new Response<Integer>(1, "文案管理员不可操作服务订单.", 0);
		try {
			super.setPostHeader(response);
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo != null)
				if (adminUserLoginInfo == null || (!"SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())
						&& !"WA".equalsIgnoreCase(adminUserLoginInfo.getApList())))
					return new Response<Integer>(1, "仅限文案修改.", null);
			ServiceOrderDTO serviceOrderDto = serviceOrderService.getServiceOrderById(id);
			if (serviceOrderDto == null)
				return new Response<Integer>(1, "服务订单不存在,修改失败.", 0);
			if (getOfficialAdminId(request) != null && serviceOrderDto.getOfficialId() != getOfficialId(request))
				return new Response<Integer>(1, "(文案管理员" + getOfficialId(request) + ")只能操作自己的服务订单,不可操作(文案"
						+ serviceOrderDto.getOfficialId() + ")服务订单.", 0);
			if (StringUtil.isNotEmpty(coePaymentVoucherImageUrl1))
				serviceOrderDto.setCoePaymentVoucherImageUrl1(coePaymentVoucherImageUrl1);
			if (StringUtil.isNotEmpty(coePaymentVoucherImageUrl2))
				serviceOrderDto.setCoePaymentVoucherImageUrl2(coePaymentVoucherImageUrl2);
			if (StringUtil.isNotEmpty(coePaymentVoucherImageUrl3))
				serviceOrderDto.setCoePaymentVoucherImageUrl3(coePaymentVoucherImageUrl3);
			if (StringUtil.isNotEmpty(coePaymentVoucherImageUrl4))
				serviceOrderDto.setCoePaymentVoucherImageUrl4(coePaymentVoucherImageUrl4);
			if (StringUtil.isNotEmpty(coePaymentVoucherImageUrl5))
				serviceOrderDto.setCoePaymentVoucherImageUrl5(coePaymentVoucherImageUrl5);
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
//		if (getOfficialAdminId(request) != null)
//			return new Response<Integer>(1, "文案管理员不可操作服务订单.", 0);
		try {
			super.setPostHeader(response);
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo != null)
				if (adminUserLoginInfo == null || (!"SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())
						&& !"WA".equalsIgnoreCase(adminUserLoginInfo.getApList())))
					return new Response<Integer>(1, "仅限文案修改.", null);
			ServiceOrderDTO serviceOrderDto = serviceOrderService.getServiceOrderById(id);
			if (serviceOrderDto == null)
				return new Response<Integer>(1, "服务订单不存在,修改失败.", 0);
			if (getOfficialAdminId(request) != null && serviceOrderDto.getOfficialId() != getOfficialId(request))
				return new Response<Integer>(1, "(文案管理员" + getOfficialId(request) + ")只能操作自己的服务订单,不可操作(文案"
						+ serviceOrderDto.getOfficialId() + ")服务订单.", 0);
			if (StringUtil.isNotEmpty(remarks))
				serviceOrderDto.setRemarks(remarks);
			int i = serviceOrderService.updateServiceOrder(serviceOrderDto);
			return i > 0 ? new Response<Integer>(0, i) : new Response<Integer>(1, "修改失败.", 0);
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/updateRealPeopleNumber", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> updateRealPeopleNumber(@RequestParam(value = "id") int id,
			@RequestParam(value = "realPeopleNumber", required = false) Integer realPeopleNumber,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo != null)
				if (adminUserLoginInfo == null || (!"SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())
						&& !"WA".equalsIgnoreCase(adminUserLoginInfo.getApList())))
					return new Response<Integer>(1, "仅限文案修改.", null);
			ServiceOrderDTO serviceOrderDto = serviceOrderService.getServiceOrderById(id);
			if (serviceOrderDto == null)
				return new Response<Integer>(1, "服务订单不存在,修改失败.", 0);
			serviceOrderDto
					.setRealPeopleNumber(realPeopleNumber != null && realPeopleNumber > 0 ? realPeopleNumber : 1);
			int i = serviceOrderService.updateServiceOrder(serviceOrderDto);
			return i > 0 ? new Response<Integer>(0, i) : new Response<Integer>(1, "修改失败.", 0);
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/updateOfficial", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> updateOfficial(@RequestParam(value = "id") int id,
			@RequestParam(value = "officialId") Integer officialId, HttpServletRequest request,
			HttpServletResponse response) {
		if (getOfficialAdminId(request) == null)
			return new Response<Integer>(1, "仅限文案管理员操作.", 0);
		try {
			super.setPostHeader(response);
			ServiceOrderDTO serviceOrderDto = serviceOrderService.getServiceOrderById(id);
			if (serviceOrderDto == null)
				return new Response<Integer>(1, "服务订单不存在,修改失败.", 0);
			serviceOrderDto.setOfficialId(officialId);
			int i = serviceOrderService.updateServiceOrder(serviceOrderDto);
			return i > 0 ? new Response<Integer>(0, i) : new Response<Integer>(1, "修改失败.", 0);
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/updateReadcommittedDate", method = RequestMethod.POST)
	@ResponseBody
	@Transactional
	public Response<Integer> updateReadcommittedDate(@RequestParam(value = "id") int id,
			@RequestParam(value = "readcommittedDate") String readcommittedDate, HttpServletRequest request,
			HttpServletResponse response) {
		if (getOfficialAdminId(request) == null)
			return new Response<Integer>(1, "仅限文案管理员操作.", 0);
		try {
			super.setPostHeader(response);
			ServiceOrderDTO serviceOrderDto = serviceOrderService.getServiceOrderById(id);
			if (serviceOrderDto == null)
				return new Response<Integer>(1, "服务订单不存在,修改失败.", 0);
			ServiceOrderReadcommittedDateDO serviceOrderReadcommittedDateDO = new ServiceOrderReadcommittedDateDO();
			serviceOrderReadcommittedDateDO.setServiceOrderId(serviceOrderDto.getId());
			serviceOrderReadcommittedDateDO.setHistoryDate(serviceOrderDto.getReadcommittedDate());
			serviceOrderDto.setReadcommittedDate(sdf.parse(readcommittedDate));
			int i = serviceOrderService.updateServiceOrder(serviceOrderDto);
			if (i > 0) {
				serviceOrderReadcommittedDateService.add(serviceOrderReadcommittedDateDO);
				return new Response<Integer>(0, i);
			}
			return new Response<Integer>(1, "修改失败.", 0);
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), null);
		} catch (ParseException e) {
			e.printStackTrace();
			return new Response<Integer>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/count", method = RequestMethod.GET)
	@ResponseBody
	@Deprecated
	public Response<Integer> countServiceOrder(@RequestParam(value = "id", required = false) Integer id,
			@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "state", required = false) String state,
			@RequestParam(value = "auditingState", required = false) String auditingState,
			@RequestParam(value = "reviewState", required = false) String reviewState,
			@RequestParam(value = "startMaraApprovalDate", required = false) String startMaraApprovalDate,
			@RequestParam(value = "endMaraApprovalDate", required = false) String endMaraApprovalDate,
			@RequestParam(value = "startOfficialApprovalDate", required = false) String startOfficialApprovalDate,
			@RequestParam(value = "endOfficialApprovalDate", required = false) String endOfficialApprovalDate,
			@RequestParam(value = "startReadcommittedDate", required = false) String startReadcommittedDate,
			@RequestParam(value = "endReadcommittedDate", required = false) String endReadcommittedDate,
			@RequestParam(value = "regionId", required = false) Integer regionId,
			@RequestParam(value = "userId", required = false) Integer userId,
			@RequestParam(value = "maraId", required = false) Integer maraId,
			@RequestParam(value = "adviserId", required = false) Integer adviserId,
			@RequestParam(value = "officialId", required = false) Integer officialId,
			@RequestParam(value = "officialTagId", required = false) Integer officialTagId,
			@RequestParam(value = "isNotApproved", required = false) Boolean isNotApproved,
			@RequestParam(value = "serviceId", required = false) Integer serviceId,
			@RequestParam(value = "schoolId", required = false) Integer schoolId, HttpServletRequest request,
			HttpServletResponse response) {

		String excludeState = null;
		List<String> stateList = null;
		if (state != null && !"".equals(state))
			stateList = new ArrayList<>(Arrays.asList(state.split(",")));
		List<String> reviewStateList = null;
		if (reviewState != null && !"".equals(reviewState))
			reviewStateList = new ArrayList<>(Arrays.asList(reviewState.split(",")));
		Integer newMaraId = getMaraId(request);
		if (newMaraId != null) {
			maraId = newMaraId;
			excludeState = ReviewAdviserStateEnum.PENDING.toString();
			reviewStateList = new ArrayList<>();
			reviewStateList.add(ServiceOrderReviewStateEnum.ADVISER.toString());
			reviewStateList.add(ServiceOrderReviewStateEnum.MARA.toString());
			reviewStateList.add(ServiceOrderReviewStateEnum.OFFICIAL.toString());
		}
		Integer newOfficialId = getOfficialId(request);
		if (newOfficialId != null) {
			if (getOfficialAdminId(request) == null)
				officialId = newOfficialId; // 非文案管理员就只显示自己的单子
			excludeState = ReviewAdviserStateEnum.PENDING.toString();
		}

		List<Integer> regionIdList = null;
		if (regionId != null && regionId > 0)
			regionIdList = ListUtil.buildArrayList(regionId);

		try {
			super.setGetHeader(response);

			// 处理顾问管理员
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo != null && "GW".equalsIgnoreCase(adminUserLoginInfo.getApList())
					&& adminUserLoginInfo.getRegionId() != null && adminUserLoginInfo.getRegionId() > 0) {
				List<RegionDTO> regionList = regionService.listRegion(adminUserLoginInfo.getRegionId());
				regionIdList = ListUtil.buildArrayList(adminUserLoginInfo.getRegionId());
				for (RegionDTO region : regionList)
					regionIdList.add(region.getId());
			} else {
				Integer newAdviserId = getAdviserId(request);
				if (newAdviserId != null)
					adviserId = newAdviserId;
				if (adminUserLoginInfo == null)
					return new Response<Integer>(0, "No permission !", 0);
			}

			if (id != null && id > 0) {
				if (serviceOrderService.getServiceOrderById(id) != null)
					return new Response<Integer>(0, 1);
				else
					return new Response<Integer>(0, 0);
			}

			return new Response<Integer>(0,
					serviceOrderService.countServiceOrder(type, excludeState, stateList, auditingState, reviewStateList,
							startMaraApprovalDate, endMaraApprovalDate, startOfficialApprovalDate,
							endOfficialApprovalDate, startReadcommittedDate, endReadcommittedDate, regionIdList, userId,
							maraId, adviserId, officialId, officialTagId, 0,
							isNotApproved != null ? isNotApproved : false, serviceId, schoolId));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public ListResponse<List<ServiceOrderDTO>> listServiceOrder(@RequestParam(value = "id", required = false) Integer id,
			@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "state", required = false) String state,
			@RequestParam(value = "auditingState", required = false) String auditingState,
			@RequestParam(value = "reviewState", required = false) String reviewState,
			@RequestParam(value = "startMaraApprovalDate", required = false) String startMaraApprovalDate,
			@RequestParam(value = "endMaraApprovalDate", required = false) String endMaraApprovalDate,
			@RequestParam(value = "startOfficialApprovalDate", required = false) String startOfficialApprovalDate,
			@RequestParam(value = "endOfficialApprovalDate", required = false) String endOfficialApprovalDate,
			@RequestParam(value = "startReadcommittedDate", required = false) String startReadcommittedDate,
			@RequestParam(value = "endReadcommittedDate", required = false) String endReadcommittedDate,
			@RequestParam(value = "regionId", required = false) Integer regionId,
			@RequestParam(value = "userId", required = false) Integer userId,
			@RequestParam(value = "maraId", required = false) Integer maraId,
			@RequestParam(value = "adviserId", required = false) Integer adviserId,
			@RequestParam(value = "officialId", required = false) Integer officialId,
			@RequestParam(value = "officialTagId", required = false) Integer officialTagId,
			@RequestParam(value = "isNotApproved", required = false) Boolean isNotApproved,
			@RequestParam(value = "serviceId", required = false) Integer serviceId,
			@RequestParam(value = "schoolId", required = false) Integer schoolId,
			@RequestParam(value = "pageNum") int pageNum, @RequestParam(value = "pageSize") int pageSize,
			@RequestParam(value = "sorter", required = false) String sorter, HttpServletRequest request,
			HttpServletResponse response) {
		String excludeState = null;
		List<String> stateList = null;
		if (state != null && !"".equals(state))
			stateList = new ArrayList<>(Arrays.asList(state.split(",")));
		List<String> reviewStateList = null;
		if (reviewState != null && !"".equals(reviewState))
			reviewStateList = new ArrayList<>(Arrays.asList(reviewState.split(",")));
		Integer newMaraId = getMaraId(request);
		if (newMaraId != null) {
			maraId = newMaraId;
			excludeState = ReviewAdviserStateEnum.PENDING.toString();
			reviewStateList = new ArrayList<>();
			reviewStateList.add(ServiceOrderReviewStateEnum.ADVISER.toString());
			reviewStateList.add(ServiceOrderReviewStateEnum.MARA.toString());
			reviewStateList.add(ServiceOrderReviewStateEnum.OFFICIAL.toString());
		}
		Integer newOfficialId = getOfficialId(request);
		if (newOfficialId != null) {
			if (getOfficialAdminId(request) == null)
				officialId = newOfficialId; // 非文案管理员就只显示自己的单子
			excludeState = ReviewAdviserStateEnum.PENDING.toString();
		}

		List<Integer> regionIdList = null;
		if (regionId != null && regionId > 0)
			regionIdList = ListUtil.buildArrayList(regionId);
		
		Sorter _sorter = null;
		if (sorter != null)
			_sorter = JSON.parseObject(sorter, Sorter.class);

		try {
			super.setGetHeader(response);
			// 处理顾问管理员
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo != null && "GW".equalsIgnoreCase(adminUserLoginInfo.getApList())
					&& adminUserLoginInfo.getRegionId() != null && adminUserLoginInfo.getRegionId() > 0) {
				List<RegionDTO> regionList = regionService.listRegion(adminUserLoginInfo.getRegionId());
				regionIdList = ListUtil.buildArrayList(adminUserLoginInfo.getRegionId());
				for (RegionDTO region : regionList)
					regionIdList.add(region.getId());
			} else {
				Integer newAdviserId = getAdviserId(request);
				if (newAdviserId != null)
					adviserId = newAdviserId;
				if (adminUserLoginInfo == null)
					return new ListResponse<List<ServiceOrderDTO>>(false, pageSize, 0, null, "No permission !");
			}

			if (id != null && id > 0) {
				List<ServiceOrderDTO> list = new ArrayList<ServiceOrderDTO>();
				ServiceOrderDTO serviceOrder = serviceOrderService.getServiceOrderById(id);
				if (serviceOrder != null)
					list.add(serviceOrder);
				return new ListResponse<List<ServiceOrderDTO>>(false, pageSize, 0, list, "");
			}
			int total = serviceOrderService.countServiceOrder(type, excludeState, stateList, auditingState,
					reviewStateList, startMaraApprovalDate, endMaraApprovalDate, startOfficialApprovalDate,
					endOfficialApprovalDate, startReadcommittedDate, endReadcommittedDate, regionIdList, userId, maraId,
					adviserId, officialId, officialTagId, 0, isNotApproved != null ? isNotApproved : false, serviceId,
					schoolId);
			List<ServiceOrderDTO> serviceOrderList = serviceOrderService.listServiceOrder(type, excludeState, stateList,
					auditingState, reviewStateList, startMaraApprovalDate, endMaraApprovalDate,
					startOfficialApprovalDate, endOfficialApprovalDate, startReadcommittedDate, endReadcommittedDate,
					regionIdList, userId, maraId, adviserId, officialId, officialTagId, 0,
					isNotApproved != null ? isNotApproved : false, pageNum, pageSize, _sorter, serviceId, schoolId);

			if (newOfficialId != null)
				for (ServiceOrderDTO so : serviceOrderList)
					so.setOfficialNotes(serviceOrderService.listOfficialRemarks(so.getId(), newOfficialId)); // 写入note
			return new ListResponse<List<ServiceOrderDTO>>(true, pageSize, total, serviceOrderList, "");
		} catch (ServiceException e) {
			return new ListResponse<List<ServiceOrderDTO>>(false, pageSize, 0, null, e.getMessage());
		}
	}

	@RequestMapping(value = "/get", method = RequestMethod.GET)
	@ResponseBody
	public Response<ServiceOrderDTO> getServiceOrder(@RequestParam(value = "id") int id, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			ServiceOrderDTO serviceOrderDto = serviceOrderService.getServiceOrderById(id);
			if (getAdminUserLoginInfo(request) != null && getOfficialId(request) != null)
				serviceOrderDto.setOfficialNotes(serviceOrderService.listOfficialRemarks(id, getOfficialId(request))); // 写入文案note
			return new Response<ServiceOrderDTO>(0, serviceOrderDto);
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
					? new Response<Integer>(0, serviceOrderService.finish(id))
					: null;
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
			if (!"OVST".equals(serviceOrderDto.getType()) && serviceOrderDto.isPay()
					&& serviceOrderDto.getPaymentVoucherImageUrl1() == null
					&& serviceOrderDto.getPaymentVoucherImageUrl2() == null) {
				ServiceException se = new ServiceException("支付凭证不能为空!");
				se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
				throw se;
			}
			// 审核
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo != null)
				if ("SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())
						|| "GW".equalsIgnoreCase(adminUserLoginInfo.getApList())) {
					if (ReviewAdviserStateEnum.get(state) != null)
						if (ReviewAdviserStateEnum.REVIEW.toString().equals(state.toUpperCase())) { // 顾问审核
							if (serviceOrderDto.getParentId() == 0 && ("SIV".equalsIgnoreCase(serviceOrderDto.getType())
									|| "MT".equalsIgnoreCase(serviceOrderDto.getType()))) {
								return new Response<ServiceOrderDTO>(1, "该订单不支持审核.", serviceOrderDto);
								// List<ServiceOrderDTO> serviceOrderList =
								// serviceOrderService.listServiceOrder(null,
								// null, null, null, 0, 0, 0, 0,
								// serviceOrderDto.getId(), 0, 10);
								// for (ServiceOrderDTO so : serviceOrderList) {
								// if (so.getServicePackage() == null)
								// return new Response<ServiceOrderDTO>(1,
								// "子订单没有服务包.", so);
								// if (so.getOfficialId() <= 0)
								// return new Response<ServiceOrderDTO>(1,
								// "子订单必须选择文案.", so);
								// if (so.getMaraId() <= 0
								// &&
								// !"EOI".equalsIgnoreCase(so.getServicePackage().getType()))
								// return new Response<ServiceOrderDTO>(1,
								// "子订单必须选择Mara.", so);
								// serviceOrderService.approval(so.getId(),
								// adminUserLoginInfo.getId(),
								// state.toUpperCase(), null, null, null);
								// }
							}
							ServiceOrderDTO serviceOrderDTO = serviceOrderService.approval(id, adminUserLoginInfo.getId(), state.toUpperCase(), null, null, null);
							wxWorkService.sendMsg(serviceOrderDto.getId());
							return new Response<ServiceOrderDTO>(0, serviceOrderDTO);
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
							// serviceOrderService.finish(id);
							serviceOrderService.Readcommitted(id);
							waUpdateRemarks(serviceOrderDto, remarks);
							return new Response<ServiceOrderDTO>(0,
									serviceOrderService.approval(id, adminUserLoginInfo.getId(),
											ReviewAdviserStateEnum.APPLY.toString(), null, state.toUpperCase(), null));
						} else if (ReviewOfficialStateEnum.PAID.toString().equals(state.toUpperCase())) { // 文案支付同时修改顾问状态
							serviceOrderService.Readcommitted(id);
							serviceOrderService.finish(id);
							return new Response<ServiceOrderDTO>(0,
									serviceOrderService.approval(id, adminUserLoginInfo.getId(),
											ReviewAdviserStateEnum.PAID.toString(), null, state.toUpperCase(), null));
						} else if (ReviewOfficialStateEnum.COMPLETE.toString().equals(state.toUpperCase())) { // 文案完成同时修改顾问和会计状态
							// serviceOrderService.finish(id);
							serviceOrderService.Readcommitted(id);
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
			@RequestParam(value = "refuseReason", required = false) String refuseReason,
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
				if ("SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())
						|| "GW".equalsIgnoreCase(adminUserLoginInfo.getApList())) {
					if (ReviewAdviserStateEnum.get(state) != null)
						if (ReviewAdviserStateEnum.PENDING.toString().equals(state.toUpperCase())) // 顾问撤回同时修改文案和mara状态
							return new Response<ServiceOrderDTO>(0,
									serviceOrderService.refuse(id, adminUserLoginInfo.getId(), state.toUpperCase(),
											ReviewOfficialStateEnum.PENDING.toString(),
											ReviewMaraStateEnum.WAIT.toString(), null));
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
						// 更新驳回原因
						if (StringUtil.isNotEmpty(refuseReason)) {
							serviceOrderDto.setRefuseReason(refuseReason);
							serviceOrderService.updateServiceOrder(serviceOrderDto);
						}
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
					if (ReviewOfficialStateEnum.get(state) != null) {
						// 更新驳回原因
						if (StringUtil.isNotEmpty(refuseReason)) {
							serviceOrderDto.setRefuseReason(refuseReason);
							serviceOrderService.updateServiceOrder(serviceOrderDto);
						}
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
			ServiceOrderDTO serviceOrder = serviceOrderService.getServiceOrderById(serviceOrderId);
			if (serviceOrder == null)
				return new Response<Integer>(1, "服务订单为空.", 0);
			ServiceOrderCommentDTO serviceOrderCommentDto = new ServiceOrderCommentDTO();
			serviceOrderCommentDto
					.setAdminUserId(adminUserLoginInfo != null ? adminUserLoginInfo.getId() : adminUserId);
			serviceOrderCommentDto.setServiceOrderId(serviceOrderId);
			serviceOrderCommentDto.setContent(content);
			if (serviceOrderService.addComment(serviceOrderCommentDto) > 0) {
				// 发送邮件
				String serviceType = "?";
				if ("VISA".equalsIgnoreCase(serviceOrder.getType()))
					serviceType = "签证";
				else if ("OVST".equalsIgnoreCase(serviceOrder.getType()))
					serviceType = "留学";
				else if ("SIV".equalsIgnoreCase(serviceOrder.getType()))
					serviceType = "独立技术移民";
				else if ("MT".equalsIgnoreCase(serviceOrder.getType()))
					serviceType = "曼拓";
				String title = "您的" + serviceType + "订单" + serviceOrder.getId() + "有最新评论";
				String message = "您的服务订单有一条新的评论，请及时查看．<br/>服务订单类型:" + serviceType + "<br/>客户:"
						+ (serviceOrder.getUser() != null ? serviceOrder.getUser().getName() : "") + "<br/>订单ID:"
						+ serviceOrder.getId() + "<br/>评论内容:" + serviceOrderCommentDto.getContent() + "<br/>评论时间:"
						+ new Date()
						+ "<br/><br/><a href='https://yongjinbiao.zhinanzhen.org/webroot/serviceorder-detail.html?id="
						+ serviceOrder.getId() + "'>服务订单详情</a>";
				String email = "";
				if (adminUserLoginInfo != null && "GW".equalsIgnoreCase(adminUserLoginInfo.getApList())) {
					OfficialDTO official = serviceOrder.getOfficial();
					if (official != null)
						email = official.getEmail();
					MaraDTO mara = serviceOrder.getMara();
					if (mara != null)
						if ("".equals(email))
							email = mara.getEmail();
						else
							email = email + "," + mara.getEmail();
				} else if (adminUserLoginInfo != null && "WA".equalsIgnoreCase(adminUserLoginInfo.getApList())) {
					AdviserDTO adviser = serviceOrder.getAdviser();
					if (adviser != null)
						email = adviser.getEmail();
					MaraDTO mara = serviceOrder.getMara();
					if (mara != null)
						if ("".equals(email))
							email = mara.getEmail();
						else
							email = email + "," + mara.getEmail();
				} else if (adminUserLoginInfo != null && "MA".equalsIgnoreCase(adminUserLoginInfo.getApList())) {
					MaraDTO mara = serviceOrder.getMara();
					if (mara != null)
						if ("".equals(email))
							email = mara.getEmail();
						else
							email = email + "," + mara.getEmail();
				} else if (adminUserLoginInfo != null && "M".equalsIgnoreCase(adminUserLoginInfo.getApList())) {
					OfficialDTO official = serviceOrder.getOfficial();
					if (official != null)
						email = official.getEmail();
					AdviserDTO adviser = serviceOrder.getAdviser();
					if (adviser != null)
						if ("".equals(email))
							email = adviser.getEmail();
						else
							email = email + "," + adviser.getEmail();
				}
				if (!"".equals(email))
					SendEmailUtil.send(email, title, message);
				return new Response<Integer>(0, serviceOrderCommentDto.getId());
			} else
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

	//

	@RequestMapping(value = "/addOfficialRemarks", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> addOfficialRemarks(@RequestParam(value = "serviceOrderId") Integer serviceOrderId,
			@RequestParam(value = "content") String content, HttpServletRequest request, HttpServletResponse response) {
		AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
		if (adminUserLoginInfo != null)
			if (adminUserLoginInfo == null || !"WA".equalsIgnoreCase(adminUserLoginInfo.getApList())
					|| adminUserLoginInfo.getOfficialId() == null)
				return new Response<Integer>(1, "仅限文案操作.", null);
		try {
			super.setPostHeader(response);
			ServiceOrderDTO serviceOrder = serviceOrderService.getServiceOrderById(serviceOrderId);
			if (serviceOrder == null)
				return new Response<Integer>(1, "服务订单为空.", 0);
			ServiceOrderOfficialRemarksDTO serviceOrderOfficialRemarksDto = new ServiceOrderOfficialRemarksDTO();
			serviceOrderOfficialRemarksDto.setOfficialId(adminUserLoginInfo.getOfficialId());
			serviceOrderOfficialRemarksDto.setServiceOrderId(serviceOrderId);
			serviceOrderOfficialRemarksDto.setContent(content);
			if (serviceOrderService.addOfficialRemarks(serviceOrderOfficialRemarksDto) > 0)
				return new Response<Integer>(0, serviceOrderOfficialRemarksDto.getId());
			else
				return new Response<Integer>(1, "创建失败.", 0);
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/updateOfficialRemarks", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> updateOfficialRemarks(@RequestParam(value = "id") Integer id,
			@RequestParam(value = "content") String content, HttpServletRequest request, HttpServletResponse response) {
		AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
		if (adminUserLoginInfo != null)
			if (adminUserLoginInfo == null || !"WA".equalsIgnoreCase(adminUserLoginInfo.getApList())
					|| adminUserLoginInfo.getOfficialId() == null)
				return new Response<Integer>(1, "仅限文案操作.", null);
		try {
			super.setPostHeader(response);
			ServiceOrderOfficialRemarksDTO serviceOrderOfficialRemarksDto = new ServiceOrderOfficialRemarksDTO();
			serviceOrderOfficialRemarksDto.setId(id);
			serviceOrderOfficialRemarksDto.setOfficialId(adminUserLoginInfo.getOfficialId());
			serviceOrderOfficialRemarksDto.setContent(content);
			if (serviceOrderService.updateOfficialRemarks(serviceOrderOfficialRemarksDto) > 0)
				return new Response<Integer>(0, serviceOrderOfficialRemarksDto.getId());
			else
				return new Response<Integer>(1, "修改失败.", 0);
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/listOfficialRemarks", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<ServiceOrderOfficialRemarksDTO>> listOfficialRemarks(
			@RequestParam(value = "serviceOrderId") Integer serviceOrderId, HttpServletRequest request,
			HttpServletResponse response) {
		AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
		if (adminUserLoginInfo != null)
			if (adminUserLoginInfo == null || !"WA".equalsIgnoreCase(adminUserLoginInfo.getApList())
					|| adminUserLoginInfo.getOfficialId() == null)
				return new Response<List<ServiceOrderOfficialRemarksDTO>>(1, "仅限文案操作.", null);
		try {
			super.setGetHeader(response);
			return new Response<List<ServiceOrderOfficialRemarksDTO>>(0,
					serviceOrderService.listOfficialRemarks(serviceOrderId, adminUserLoginInfo.getOfficialId()));
		} catch (ServiceException e) {
			return new Response<List<ServiceOrderOfficialRemarksDTO>>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/deleteOfficialRemarks", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> deleteOfficialRemarks(@RequestParam(value = "id") int id, HttpServletRequest request,
			HttpServletResponse response) {
		AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
		if (adminUserLoginInfo != null)
			if (adminUserLoginInfo == null || !"WA".equalsIgnoreCase(adminUserLoginInfo.getApList())
					|| adminUserLoginInfo.getOfficialId() == null)
				return new Response<Integer>(1, "仅限文案操作.", null);
		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, serviceOrderService.deleteServiceOrderOfficialRemarksDTO(id));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/down", method = RequestMethod.GET)
	@ResponseBody
	public void down(@RequestParam(value = "id", required = false) Integer id,
			@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "state", required = false) String state,
			@RequestParam(value = "auditingState", required = false) String auditingState,
			@RequestParam(value = "reviewState", required = false) String reviewState,
			@RequestParam(value = "startMaraApprovalDate", required = false) String startMaraApprovalDate,
			@RequestParam(value = "endMaraApprovalDate", required = false) String endMaraApprovalDate,
			@RequestParam(value = "startOfficialApprovalDate", required = false) String startOfficialApprovalDate,
			@RequestParam(value = "endOfficialApprovalDate", required = false) String endOfficialApprovalDate,
			@RequestParam(value = "startReadcommittedDate", required = false) String startReadcommittedDate,
			@RequestParam(value = "endReadcommittedDate", required = false) String endReadcommittedDate,
			@RequestParam(value = "regionId", required = false) Integer regionId,
			@RequestParam(value = "userId", required = false) Integer userId,
			@RequestParam(value = "maraId", required = false) Integer maraId,
			@RequestParam(value = "adviserId", required = false) Integer adviserId,
			@RequestParam(value = "officialId", required = false) Integer officialId,
			@RequestParam(value = "officialTagId", required = false) Integer officialTagId,
			@RequestParam(value = "isNotApproved", required = false) Boolean isNotApproved,
			@RequestParam(value = "serviceId", required = false) Integer serviceId,
			@RequestParam(value = "schoolId", required = false) Integer schoolId, HttpServletRequest request,
			HttpServletResponse response) {

		String excludeState = null;
		List<String> stateList = null;
		if (state != null && !"".equals(state))
			stateList = new ArrayList<>(Arrays.asList(state.split(",")));
		List<String> reviewStateList = null;
		if (reviewState != null && !"".equals(reviewState))
			reviewStateList = new ArrayList<>(Arrays.asList(reviewState.split(",")));
		Integer newMaraId = getMaraId(request);
		if (newMaraId != null) {
			maraId = newMaraId;
			excludeState = ReviewAdviserStateEnum.PENDING.toString();
			reviewStateList = new ArrayList<>();
			reviewStateList.add(ServiceOrderReviewStateEnum.ADVISER.toString());
			reviewStateList.add(ServiceOrderReviewStateEnum.MARA.toString());
			reviewStateList.add(ServiceOrderReviewStateEnum.OFFICIAL.toString());
		}
		Integer newOfficialId = getOfficialId(request);
		if (newOfficialId != null) {
			if (getOfficialAdminId(request) == null)
				officialId = newOfficialId; // 非文案管理员就只显示自己的单子
			excludeState = ReviewAdviserStateEnum.PENDING.toString();
		}

		List<Integer> regionIdList = null;
		if (regionId != null && regionId > 0)
			regionIdList = ListUtil.buildArrayList(regionId);

		try {
			super.setGetHeader(response);
			// 处理顾问管理员
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo != null && "GW".equalsIgnoreCase(adminUserLoginInfo.getApList())
					&& adminUserLoginInfo.getRegionId() != null && adminUserLoginInfo.getRegionId() > 0) {
				List<RegionDTO> regionList = regionService.listRegion(adminUserLoginInfo.getRegionId());
				regionIdList = ListUtil.buildArrayList(adminUserLoginInfo.getRegionId());
				for (RegionDTO region : regionList)
					regionIdList.add(region.getId());
			} else {
				Integer newAdviserId = getAdviserId(request);
				if (newAdviserId != null)
					adviserId = newAdviserId;
			}

			List<ServiceOrderDTO> serviceOrderList = null;
			if (id != null && id > 0) {
				serviceOrderList = new ArrayList<ServiceOrderDTO>();
				ServiceOrderDTO serviceOrder = serviceOrderService.getServiceOrderById(id);
				if (serviceOrder != null)
					serviceOrderList.add(serviceOrder);
			}
			if (id == null) {
				serviceOrderList = serviceOrderService.listServiceOrder(type, excludeState, stateList, auditingState,
						reviewStateList, startMaraApprovalDate, endMaraApprovalDate, startOfficialApprovalDate,
						endOfficialApprovalDate, startReadcommittedDate, endReadcommittedDate, regionIdList, userId,
						maraId, adviserId, officialId, officialTagId, 0, isNotApproved != null ? isNotApproved : false,
						0, 9999, null, serviceId, schoolId);

				if (newOfficialId != null)
					for (ServiceOrderDTO so : serviceOrderList)
						so.setOfficialNotes(serviceOrderService.listOfficialRemarks(so.getId(), newOfficialId)); // 写入note
			}

			response.reset();// 清空输出流
			String tableName = "ServiceOrderInformation";
			response.setHeader("Content-disposition",
					"attachment; filename=" + new String(tableName.getBytes("GB2312"), "8859_1") + ".xls");
			response.setContentType("application/msexcel");

			OutputStream os = response.getOutputStream();
			jxl.Workbook wb;
			InputStream is;
			try {
				is = this.getClass().getResourceAsStream("/ServiceOrderTemplate.xls");
			} catch (Exception e) {
				throw new Exception("模版不存在");
			}
			try {
				wb = Workbook.getWorkbook(is);
			} catch (Exception e) {
				throw new Exception("模版格式不支持");
			}
			WorkbookSettings settings = new WorkbookSettings();
			settings.setWriteAccess(null);
			jxl.write.WritableWorkbook wbe = Workbook.createWorkbook(os, wb, settings);

			if (wbe == null) {
				System.out.println("wbe is null !os=" + os + ",wb" + wb);
			} else {
				System.out.println("wbe not null !os=" + os + ",wb" + wb);
			}
			WritableSheet sheet = wbe.getSheet(0);
			WritableCellFormat cellFormat = new WritableCellFormat();
			int i = 1;
			for (ServiceOrderDTO so : serviceOrderList) {
				sheet.addCell(new Label(0, i, so.getId() + "", cellFormat));
				if (so.getGmtCreate() != null)
					sheet.addCell(new Label(1, i, sdf.format(so.getGmtCreate()), cellFormat));
				if (so.getOfficialApprovalDate() != null)
					sheet.addCell(new Label(2, i, sdf.format(so.getOfficialApprovalDate()), cellFormat));
				if (so.getFinishDate() != null)
					sheet.addCell(new Label(3, i, sdf.format(so.getFinishDate()), cellFormat));
				sheet.addCell(new Label(4, i, so.getUserId() + "", cellFormat));
				if (so.getUser() != null) {
					sheet.addCell(new Label(5, i, so.getUser().getName() + "", cellFormat));
					sheet.addCell(new Label(6, i, sdf.format(so.getUser().getBirthday()), cellFormat));
					sheet.addCell(new Label(7, i, so.getUser().getPhone(), cellFormat));
				}
				if (so.getAdviser() != null)
					sheet.addCell(new Label(8, i, so.getAdviser().getName(), cellFormat));
				if (so.getMara() != null)
					sheet.addCell(new Label(9, i, so.getMara().getName(), cellFormat));
				if (so.getOfficial() != null)
					sheet.addCell(new Label(10, i, so.getOfficial().getName(), cellFormat));

				if (so.getService() != null) {
					sheet.addCell(new Label(11, i, so.getService().getName(), cellFormat));
					sheet.addCell(new Label(12, i, so.getService().getCode(), cellFormat));
					if (so.getServiceAssessDO() != null)
						sheet.addCell(new Label(12, i,
								so.getService().getCode() + " - " + so.getServiceAssessDO().getName(), cellFormat));
				}
				if (so.getSchool() != null) {
					sheet.addCell(new Label(11, i, " 留学 ", cellFormat));
					sheet.addCell(new Label(12, i, so.getSchool().getName(), cellFormat));
				}

				if (so.getReview() != null) {
					if (so.getState().equalsIgnoreCase("PENDING"))
						sheet.addCell(new Label(13, i, "待提交审核", cellFormat));
					else {
						if (so.getReview().getType().equalsIgnoreCase("APPROVAL")) {
							if (StringUtil.isEmpty(so.getReview().getOfficialState()))
								sheet.addCell(new Label(13, i, "资料待审核", cellFormat));
							if (StringUtil.isNotEmpty(so.getReview().getOfficialState())) {
								if (so.getReview().getOfficialState().equalsIgnoreCase("REVIEW")) {
									sheet.addCell(new Label(13, i, "资料审核中", cellFormat));
									if (StringUtil.isNotBlank(so.getReview().getMaraState())
											&& so.getReview().getMaraState().equalsIgnoreCase("FINISH"))
										sheet.addCell(new Label(13, i, "资料审核完成", cellFormat));
								}

								if (so.getReview().getOfficialState().equalsIgnoreCase("WAIT")) {
									if (StringUtil.isNotBlank(so.getReview().getMaraState())
											&& so.getReview().getMaraState().equalsIgnoreCase("WAIT"))
										sheet.addCell(new Label(13, i, "已提交Mara审核", cellFormat));
								}
								if (so.getReview().getOfficialState().equalsIgnoreCase("APPLY"))
									sheet.addCell(new Label(13, i, "服务申请中", cellFormat));
								if (so.getReview().getOfficialState().equalsIgnoreCase("COMPLETE"))
									sheet.addCell(new Label(13, i, "申请成功", cellFormat));
								if (so.getReview().getOfficialState().equalsIgnoreCase("PAID")) {
									sheet.addCell(new Label(13, i, "支付成功", cellFormat));
									if (so.getType().equalsIgnoreCase("OVST"))
										if (so.isSubmitted())
											sheet.addCell(new Label(13, i, "支付成功,月奖已申请", cellFormat));
										else
											sheet.addCell(new Label(13, i, "支付成功,月奖未申请", cellFormat));
								}
								if (so.getReview().getOfficialState().equalsIgnoreCase("CLOSE"))
									sheet.addCell(new Label(13, i, "已关闭", cellFormat));
							}
						}
						if (so.getReview().getType().equalsIgnoreCase("REFUSE")
								& StringUtil.isNotBlank(so.getReview().getOfficialState())) {
							if (so.getReview().getOfficialState().equalsIgnoreCase("PENDING"))
								sheet.addCell(new Label(13, i, "待提交审核,文案已驳回", cellFormat));
							if (so.getReview().getOfficialState().equalsIgnoreCase("REVIEW"))
								sheet.addCell(new Label(13, i, "资料审核中,已驳回", cellFormat));
							if (so.getReview().getOfficialState().equalsIgnoreCase("CLOSE"))
								sheet.addCell(new Label(13, i, "已关闭", cellFormat));
						}
					}

				}
				sheet.addCell(new Label(14, i, so.getRealPeopleNumber() + "", cellFormat));
				sheet.addCell(new Label(15, i, so.getRemarks(), cellFormat));
				i++;
			}
			wbe.write();
			wbe.close();

		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	@RequestMapping(value = "/downExcel", method = RequestMethod.GET)
	@ResponseBody
	public void downExcel(@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "startOfficialApprovalDate", required = false) String startOfficialApprovalDate,
			@RequestParam(value = "endOfficialApprovalDate", required = false) String endOfficialApprovalDate,
			HttpServletRequest request, HttpServletResponse response) {

		try {
			super.setGetHeader(response);
			List<EachRegionNumberDTO> eachRegionNumberDTOS = serviceOrderService.listServiceOrderGroupByForRegion(type,
					startOfficialApprovalDate, endOfficialApprovalDate);

			response.reset();// 清空输出流
			String tableName = "Information";
			response.setHeader("Content-disposition",
					"attachment; filename=" + new String(tableName.getBytes("GB2312"), "8859_1") + ".xls");
			response.setContentType("application/msexcel");

			OutputStream os = response.getOutputStream();
			jxl.Workbook wb;
			InputStream is;
			try {
				is = this.getClass().getResourceAsStream("/data.xls");
			} catch (Exception e) {
				throw new Exception("模版不存在");
			}
			try {
				wb = Workbook.getWorkbook(is);
			} catch (Exception e) {
				throw new Exception("模版格式不支持");
			}
			WorkbookSettings settings = new WorkbookSettings();
			settings.setWriteAccess(null);
			jxl.write.WritableWorkbook wbe = Workbook.createWorkbook(os, wb, settings);

			if (wbe == null) {
				System.out.println("wbe is null !os=" + os + ",wb" + wb);
			} else {
				System.out.println("wbe not null !os=" + os + ",wb" + wb);
			}
			WritableSheet sheet = wbe.getSheet(0);
			WritableCellFormat cellFormat = new WritableCellFormat();
			int i = 0;
			for (EachRegionNumberDTO eo : eachRegionNumberDTOS) {
				if (i == 0 && type.equalsIgnoreCase("VISA")) {
					sheet.addCell(new Label(1, i, "签证项目", cellFormat));
					i++;
				}
				if (i == 0 && type.equalsIgnoreCase("OVST")) {
					sheet.addCell(new Label(1, i, "留学学校", cellFormat));
					i++;
				}
				sheet.addCell(new Label(0, i, i + "", cellFormat));
				sheet.addCell(new Label(1, i, eo.getName(), cellFormat));
				sheet.addCell(new Label(2, i, eo.getTotal() + "", cellFormat));
				sheet.addCell(new Label(3, i, eo.getSydney() + "", cellFormat));
				sheet.addCell(new Label(4, i, eo.getMelbourne() + "", cellFormat));
				sheet.addCell(new Label(5, i, eo.getBrisbane() + "", cellFormat));
				sheet.addCell(new Label(6, i, eo.getAdelaide() + "", cellFormat));
				sheet.addCell(new Label(7, i, eo.getHobart() + "", cellFormat));
				sheet.addCell(new Label(8, i, eo.getCanberra() + "", cellFormat));
				sheet.addCell(new Label(9, i, eo.getSydney2() + "", cellFormat));
				sheet.addCell(new Label(10, i, eo.getOther() + "", cellFormat));
				i++;
			}
			wbe.write();
			wbe.close();

		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	@RequestMapping(value = "/next_flow", method = RequestMethod.POST)
	@ResponseBody
	public Response<ServiceOrderDTO> approval(@RequestParam(value = "id") int id,
			@RequestParam(value = "state") String state,
			@RequestParam(value = "subagencyId", required = false) String subagencyId,
			@RequestParam(value = "closedReason", required = false) String closedReason,
			@RequestParam(value = "refuseReason", required = false) String refuseReason,
			@RequestParam(value = "remarks", required = false) String remarks,
			@RequestParam(value = "stateMark", required = false) String stateMark, HttpServletRequest request,
			HttpServletResponse response) {
		AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
		if (adminUserLoginInfo == null)
			return new Response<ServiceOrderDTO>(1, "请先登录.", null);
		if (id <= 0)
			return new Response<ServiceOrderDTO>(1, "id不正确:" + id, null);
		ServiceOrderDTO serviceOrderDto;
		try {
			serviceOrderDto = serviceOrderService.getServiceOrderById(id);
			if (ObjectUtil.isNull(serviceOrderDto))
				return new Response<ServiceOrderDTO>(1, "服务订单不存在:" + id, null);
			Node node = soNodeFactory.getNode(serviceOrderDto.getState());

			Context context = new Context();
			context.putParameter("serviceOrderId", id);
			context.putParameter("type", serviceOrderDto.getType());
			context.putParameter("state", state);
			context.putParameter("subagencyId", subagencyId);
			context.putParameter("closedReason", closedReason);
			context.putParameter("refuseReason", refuseReason);
			context.putParameter("remarks", remarks);
			context.putParameter("stateMark", stateMark);
			context.putParameter("ap", adminUserLoginInfo.getApList());
			context.putParameter("adminUserId", adminUserLoginInfo.getId());
			
			String[] nextNodeNames = node.nextNodeNames();
			if (nextNodeNames != null)
				if (Arrays.asList(nextNodeNames).contains(state))
					node = soNodeFactory.getNode(state);
				else
					return new Response<ServiceOrderDTO>(1,
							StringUtil.merge("状态:", state, "不是合法状态. (", Arrays.toString(nextNodeNames), ")"), null);

			Workflow workflow = new Workflow("Service Order Work Flow", node, soNodeFactory);
			
			context = workflowStarter.process(workflow, context);
			return context.getParameter("response") != null
					? (Response<ServiceOrderDTO>) context.getParameter("response")
					: new Response<ServiceOrderDTO>(0, id + "", null);
		} catch (ServiceException e) {
			return new Response<ServiceOrderDTO>(1, "异常:" + e.getMessage(), null);
		}
	}
}
