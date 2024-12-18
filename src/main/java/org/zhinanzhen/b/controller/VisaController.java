package org.zhinanzhen.b.controller;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.zhinanzhen.b.dao.pojo.ServicePackagePriceDO;
import org.zhinanzhen.b.dao.pojo.SetupExcelDO;
import org.zhinanzhen.b.service.*;
import org.zhinanzhen.b.service.pojo.*;
import org.zhinanzhen.b.service.pojo.ant.Sorter;
import org.zhinanzhen.tb.controller.ListResponse;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.AdviserService;
import org.zhinanzhen.tb.service.RegionService;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.UserService;
import org.zhinanzhen.tb.service.pojo.AdviserDTO;
import org.zhinanzhen.tb.service.pojo.RegionDTO;

import com.alibaba.fastjson.JSON;
import com.ikasoa.core.utils.ListUtil;
import com.ikasoa.core.utils.ObjectUtil;
import com.ikasoa.core.utils.StringUtil;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import org.zhinanzhen.tb.utils.WXWorkAPI;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/visa")
@Slf4j
public class VisaController extends BaseCommissionOrderController {
	
	private static final Logger LOG = LoggerFactory.getLogger(VisaController.class);
	
	@Resource
	MaraService maraService;
	@Resource
	VisaService visaService;
	@Resource
	VisaOfficialService visaOfficialService;

	@Resource
	ServiceOrderService serviceOrderService;

	@Resource
	ApplicantService applicantService;

	@Resource
	RegionService regionService;

	@Resource
	UserService userService;

	@Resource
	MailRemindService mailRemindService;

	@Resource
	ServicePackagePriceService servicePackagePriceService;
	
	@Resource
	private AdviserService adviserService;
	
	@Resource
	private KjService kjService;

	@Resource
	private ServiceService serviceService;

	@Resource
	WXWorkService wxWorkService;

	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@RequestMapping(value = "/upload_img", method = RequestMethod.POST)
	@ResponseBody
	public Response<String> uploadImage(@RequestParam MultipartFile file, HttpServletRequest request,
										HttpServletResponse response) throws IllegalStateException, IOException {
		super.setPostHeader(response);
		return super.upload2(file, request.getSession(), "/uploads/payment_voucher_image_url_v/");
	}

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	public Response<List<VisaDTO>> addVisa(@RequestParam(value = "userId", required = false) String userId,
										   @RequestParam(value = "applicantBirthday", required = false) String applicantBirthday,
										   @RequestParam(value = "handlingDate") String handlingDate,
										   @RequestParam(value = "receiveTypeId") String receiveTypeId,
										   @RequestParam(value = "receiveDate") String receiveDate,
										   @RequestParam(value = "serviceId") String serviceId,
										   @RequestParam(value = "serviceOrderId") Integer serviceOrderId,
										   @RequestParam(value = "installment") Integer installment,
										   @RequestParam(value = "paymentVoucherImageUrl1", required = false) String paymentVoucherImageUrl1,
										   @RequestParam(value = "paymentVoucherImageUrl2", required = false) String paymentVoucherImageUrl2,
										   @RequestParam(value = "paymentVoucherImageUrl3", required = false) String paymentVoucherImageUrl3,
										   @RequestParam(value = "paymentVoucherImageUrl4", required = false) String paymentVoucherImageUrl4,
										   @RequestParam(value = "paymentVoucherImageUrl5", required = false) String paymentVoucherImageUrl5,
										   @RequestParam(value = "visaVoucherImageUrl", required = false) String visaVoucherImageUrl,
										   @RequestParam(value = "receivable") String receivable,
										   @RequestParam(value = "received", required = false) String received,
										   @RequestParam(value = "perAmount") String perAmount, @RequestParam(value = "amount") String amount,
										   @RequestParam(value = "currency", required = false) String currency,
										   @RequestParam(value = "exchangeRate", required = false) String exchangeRate,
										   @RequestParam(value = "invoiceNumber", required = false) String invoiceNumber,
										   @RequestParam(value = "adviserId") String adviserId, @RequestParam(value = "maraId") String maraId,
										   @RequestParam(value = "officialId") String officialId,
										   @RequestParam(value = "remarks", required = false) String remarks,
										   @RequestParam(value = "verifyCode", required = false) String verifyCode, HttpServletRequest request,
										   HttpServletResponse response) {

		// 更改当前顾问编号
		Integer newAdviserId = getAdviserId(request);
		if (newAdviserId != null)
			adviserId = newAdviserId + "";

		try {
			super.setPostHeader(response);
			// AdminUserLoginInfo adminUserLoginInfo =
			// getAdminUserLoginInfo(request);
			// if (adminUserLoginInfo == null ||
			// (!"SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())
			// && !"GW".equalsIgnoreCase(adminUserLoginInfo.getApList())))
			// return new Response<List<VisaDTO>>(1, "仅顾问和超级管理员能创建佣金订单.", null);
			ServiceOrderDTO serviceOrderDto = serviceOrderService.getServiceOrderById(serviceOrderId);
			if (serviceOrderDto == null)
				return new Response<List<VisaDTO>>(1, "服务订单(ID:" + serviceOrderId + ")不存在!", null);
			List<VisaDTO> visaDtoList = new ArrayList<>();
			VisaDTO visaDto = new VisaDTO();
			double _receivable = 0.00;
			if (StringUtil.isNotEmpty(receivable))
				_receivable = Double.parseDouble(receivable);
			double _received = 0.00;
			if (StringUtil.isNotEmpty(received))
				_received = Double.parseDouble(received);
			visaDto.setState(ReviewKjStateEnum.PENDING.toString());
			if (StringUtil.isNotEmpty(userId))
				visaDto.setUserId(Integer.parseInt(userId));
			visaDto.setCode(UUID.randomUUID().toString());
			if (StringUtil.isNotEmpty(handlingDate))
				visaDto.setHandlingDate(new Date(Long.parseLong(handlingDate)));
			if (StringUtil.isNotEmpty(receiveTypeId))
				visaDto.setReceiveTypeId(Integer.parseInt(receiveTypeId));
			if (StringUtil.isNotEmpty(receiveDate))
				visaDto.setReceiveDate(new Date(Long.parseLong(receiveDate)));
			if (StringUtil.isNotEmpty(serviceId))
				visaDto.setServiceId(Integer.parseInt(serviceId));
			if (serviceOrderId != null && serviceOrderId > 0)
				visaDto.setServiceOrderId(serviceOrderId);
			if (installment != null)
				visaDto.setInstallment(installment);
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl1))
				visaDto.setPaymentVoucherImageUrl1(paymentVoucherImageUrl1);
			else
				visaDto.setPaymentVoucherImageUrl1(serviceOrderDto.getPaymentVoucherImageUrl1());
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl2))
				visaDto.setPaymentVoucherImageUrl2(paymentVoucherImageUrl2);
			else
				visaDto.setPaymentVoucherImageUrl2(serviceOrderDto.getPaymentVoucherImageUrl2());
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl3))
				visaDto.setPaymentVoucherImageUrl3(paymentVoucherImageUrl3);
			else
				visaDto.setPaymentVoucherImageUrl3(serviceOrderDto.getPaymentVoucherImageUrl3());
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl4))
				visaDto.setPaymentVoucherImageUrl4(paymentVoucherImageUrl4);
			else
				visaDto.setPaymentVoucherImageUrl4(serviceOrderDto.getPaymentVoucherImageUrl4());
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl5))
				visaDto.setPaymentVoucherImageUrl5(paymentVoucherImageUrl5);
			else
				visaDto.setPaymentVoucherImageUrl5(serviceOrderDto.getPaymentVoucherImageUrl5());
			if (StringUtil.isNotEmpty(visaVoucherImageUrl))
				visaDto.setVisaVoucherImageUrl(visaVoucherImageUrl);
			else
				visaDto.setVisaVoucherImageUrl(serviceOrderDto.getVisaVoucherImageUrl());
//				visaDto.setReceivable(Double.parseDouble(receivable));
//			if (StringUtil.isNotEmpty(received))
//				visaDto.setReceived(Double.parseDouble(received));
			if (StringUtil.isNotEmpty(perAmount))
				visaDto.setPerAmount(Double.parseDouble(perAmount));
			if (StringUtil.isNotEmpty(amount))
				visaDto.setAmount(Double.parseDouble(amount));
			if (visaDto.getPerAmount() < visaDto.getAmount())
				return new Response<List<VisaDTO>>(1,
						"本次应收款(" + visaDto.getPerAmount() + ")不能小于本次已收款(" + visaDto.getAmount() + ")!", null);
			if (StringUtil.isNotEmpty(currency))
				visaDto.setCurrency(currency);
			if (StringUtil.isNotEmpty(exchangeRate))
				visaDto.setExchangeRate(Double.parseDouble(exchangeRate));
			visaDto.setDiscount(visaDto.getPerAmount() - visaDto.getAmount());
			if (StringUtil.isNotEmpty(invoiceNumber))
				visaDto.setInvoiceNumber(invoiceNumber);
			if (StringUtil.isNotEmpty(adviserId)) {
				visaDto.setAdviserId(StringUtil.toInt(adviserId));
			}
			if (StringUtil.isNotEmpty(maraId))
				visaDto.setMaraId(StringUtil.toInt(maraId));
			if (StringUtil.isNotEmpty(officialId)) {
				visaDto.setOfficialId(StringUtil.toInt(officialId));
			}
			if (StringUtil.isNotEmpty(remarks))
				visaDto.setRemarks(remarks);
			double commission = visaDto.getAmount();
			if ("CNY".equals(currency)) {
				BigDecimal bigDecimal = BigDecimal.valueOf(commission);
				BigDecimal bigDecimalExc = new BigDecimal(exchangeRate);
				BigDecimal divide = bigDecimal.divide(bigDecimalExc, 4, RoundingMode.HALF_UP);
				commission = divide.doubleValue();
			}
			visaDto.setGst(commission / 11);
			visaDto.setDeductGst(commission - visaDto.getGst());
			visaDto.setBonus(visaDto.getDeductGst() * 0.1);
			visaDto.setExpectAmount(commission);

			double _perAmount = 0.00;
			double _amount = 0.00;
			for (int installmentNum = 1; installmentNum <= installment; installmentNum++) {
				visaDto.setInstallmentNum(installmentNum);
				if (installmentNum > 1) { // 只给第一个添加支付凭证
					visaDto.setPaymentVoucherImageUrl1(null);
					visaDto.setPaymentVoucherImageUrl2(null);
					visaDto.setPaymentVoucherImageUrl3(null);
					visaDto.setPaymentVoucherImageUrl4(null);
					visaDto.setPaymentVoucherImageUrl5(null);
					visaDto.setState(ReviewKjStateEnum.PENDING.toString());
					visaDto.setVerifyCode(null);// 只给第一笔对账verifyCode
					visaDto.setKjApprovalDate(null);
					visaDto.setReceiveDate(null);
					if (installment > 2) {
						visaDto.setPerAmount(_receivable > _perAmount ? (_receivable - _perAmount) / (installment - 1) : 0.00); // 第二笔单子修改本次应收款
					} else {
						visaDto.setPerAmount(_receivable > _perAmount ? _receivable - _perAmount : 0.00);
					}
					visaDto.setAmount(visaDto.getPerAmount());
					visaDto.setDiscount(0.00);
					commission = visaDto.getAmount();
					visaDto.setGst(commission / 11);
					visaDto.setDeductGst(commission - visaDto.getGst());
					visaDto.setBonus(visaDto.getDeductGst() * 0.1);
					visaDto.setExpectAmount(commission);
//					if (_received > 0.00)
//						visaDto.setAmount(_received > _amount ? _received - _amount : 0.00);
//					else


				} else {
					visaDto.setState(ReviewKjStateEnum.REVIEW.toString()); // 第一笔单子直接进入财务审核状态
					if (StringUtil.isNotEmpty(verifyCode))// 只给第一笔赋值verifyCode
						visaDto.setVerifyCode(verifyCode.replace("$", "").replace("#", "").replace(" ", ""));
					visaDto.setKjApprovalDate(new Date());
				}
				if (visaService.addVisa(visaDto) > 0)
					visaDtoList.add(visaDto);
				if (installmentNum == 1) {
					_perAmount += visaDto.getPerAmount();
					_amount += visaDto.getAmount();
				}
			}
			serviceOrderDto.setOfficialApprovalDate(new Date());
			serviceOrderDto.setSubmitted(true);
			serviceOrderService.updateServiceOrder(serviceOrderDto);
			if (serviceOrderDto.getParentId() > 0) {
				ServiceOrderDTO _serviceOrderDto = serviceOrderService
						.getServiceOrderById(serviceOrderDto.getParentId());
				if (_serviceOrderDto != null && !_serviceOrderDto.isSubmitted()) {
					_serviceOrderDto.setSubmitted(true);
					serviceOrderService.updateServiceOrder(_serviceOrderDto);
				}
			}
			ApplicantDTO applicantDto = serviceOrderDto.getApplicant();
			String msg = "";
			if (applicantDto != null && applicantBirthday != null) {
				applicantDto.setBirthday(new Date(Long.parseLong(applicantBirthday)));
				if (applicantService.update(applicantDto) <= 0)
					msg += "申请人生日修改失败! (serviceOrderId:" + serviceOrderDto.getId() + ", applicantId:"
							+ applicantDto.getId() + ", applicantBirthday:" + applicantDto.getBirthday() + ");";
				else
					msg += "申请人生日修改成功. (serviceOrderId:" + serviceOrderDto.getId() + ", applicantId:"
							+ applicantDto.getId() + ", applicantBirthday:" + applicantDto.getBirthday() + ");";
			}
			return new Response<List<VisaDTO>>(0, msg, visaDtoList);
		} catch (ServiceException e) {
			return new Response<List<VisaDTO>>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	@ResponseBody
	public Response<VisaDTO> update(@RequestParam(value = "id") int id,
									@RequestParam(value = "state", required = false) String state,
									@RequestParam(value = "userId", required = false) String userId,
									@RequestParam(value = "applicantBirthday", required = false) String applicantBirthday,
									@RequestParam(value = "commissionState", required = false) String commissionState,
									@RequestParam(value = "handlingDate", required = false) String handlingDate,
									@RequestParam(value = "receiveTypeId", required = false) String receiveTypeId,
									@RequestParam(value = "receiveDate", required = false) String receiveDate,
									@RequestParam(value = "serviceId", required = false) String serviceId,
									@RequestParam(value = "serviceOrderId", required = false) Integer serviceOrderId,
									@RequestParam(value = "paymentVoucherImageUrl1", required = false) String paymentVoucherImageUrl1,
									@RequestParam(value = "paymentVoucherImageUrl2", required = false) String paymentVoucherImageUrl2,
									@RequestParam(value = "paymentVoucherImageUrl3", required = false) String paymentVoucherImageUrl3,
									@RequestParam(value = "paymentVoucherImageUrl4", required = false) String paymentVoucherImageUrl4,
									@RequestParam(value = "paymentVoucherImageUrl5", required = false) String paymentVoucherImageUrl5,
									@RequestParam(value = "visaVoucherImageUrl", required = false) String visaVoucherImageUrl,
									@RequestParam(value = "receivable", required = false) String receivable,
									@RequestParam(value = "received", required = false) String received,
									@RequestParam(value = "perAmount", required = false) String perAmount,
									@RequestParam(value = "amount", required = false) String amount,
									@RequestParam(value = "sureExpectAmount", required = false) Double sureExpectAmount,
									@RequestParam(value = "currency", required = false) String currency,
									@RequestParam(value = "exchangeRate", required = false) String exchangeRate,
									@RequestParam(value = "invoiceNumber", required = false) String invoiceNumber,
									@RequestParam(value = "adviserId", required = false) String adviserId,
									@RequestParam(value = "maraId", required = false) String maraId,
									@RequestParam(value = "officialId", required = false) String officialId,
									@RequestParam(value = "bankCheck", required = false) String bankCheck,
									@RequestParam(value = "isChecked", required = false) String isChecked,
									@RequestParam(value = "remarks", required = false) String remarks,
									@RequestParam(value = "verifyCode", required = false) String verifyCode, HttpServletRequest request,
									HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			VisaDTO _visaDto = visaService.getVisaById(id);
			ServiceOrderDTO serviceOrderDto = serviceOrderService.getServiceOrderById(_visaDto.getServiceOrderId());
			VisaDTO visaDto = new VisaDTO();
			visaDto.setId(id);
			if (adminUserLoginInfo != null && "KJ".equalsIgnoreCase(adminUserLoginInfo.getApList())
					&& commissionState != null) // 只有会计能修改佣金状态
				visaDto.setCommissionState(CommissionStateEnum.get(commissionState.toUpperCase()).toString());
			if (StringUtil.isNotEmpty(state))
				visaDto.setState(state);
			else
				visaDto.setState(ReviewKjStateEnum.REVIEW.toString()); // 修改后重新审核
			if (StringUtil.isNotEmpty(handlingDate)) {
				visaDto.setHandlingDate(new Date(Long.parseLong(handlingDate)));
			}
			if (StringUtil.isNotEmpty(userId)) {
				visaDto.setUserId(Integer.parseInt(userId));
			}
			if (StringUtil.isNotEmpty(receiveTypeId)) {
				visaDto.setReceiveTypeId(Integer.parseInt(receiveTypeId));
				serviceOrderDto.setReceiveTypeId(Integer.parseInt(receiveTypeId));
			}
			if (StringUtil.isNotEmpty(receiveDate)) {
				visaDto.setReceiveDate(new Date(Long.parseLong(receiveDate)));
				serviceOrderDto.setReceiveDate(new Date(Long.parseLong(receiveDate)));
			}
			if (StringUtil.isNotEmpty(serviceId)) {
				visaDto.setServiceId(Integer.parseInt(serviceId));
			}
			if (serviceOrderId != null && serviceOrderId > 0)
				visaDto.setServiceOrderId(serviceOrderId);
//			if (StringUtil.isNotEmpty(receivable)) { // TODO: 准备废弃receivable
//				visaDto.setReceivable(Double.parseDouble(receivable));
//				serviceOrderDto.setReceivable(Double.parseDouble(receivable));
//			}
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl1)) {
				visaDto.setPaymentVoucherImageUrl1(paymentVoucherImageUrl1);
				serviceOrderDto.setPaymentVoucherImageUrl1(paymentVoucherImageUrl1);
			}
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl2)) {
				visaDto.setPaymentVoucherImageUrl2(paymentVoucherImageUrl2);
				serviceOrderDto.setPaymentVoucherImageUrl2(paymentVoucherImageUrl2);
			}
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl3)) {
				visaDto.setPaymentVoucherImageUrl3(paymentVoucherImageUrl3);
				serviceOrderDto.setPaymentVoucherImageUrl3(paymentVoucherImageUrl3);
			}
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl4)) {
				visaDto.setPaymentVoucherImageUrl4(paymentVoucherImageUrl4);
				serviceOrderDto.setPaymentVoucherImageUrl4(paymentVoucherImageUrl4);
			}
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl5)) {
				visaDto.setPaymentVoucherImageUrl5(paymentVoucherImageUrl5);
				serviceOrderDto.setPaymentVoucherImageUrl5(paymentVoucherImageUrl5);
			}
			if (StringUtil.isNotEmpty(visaVoucherImageUrl))
				visaDto.setVisaVoucherImageUrl(visaVoucherImageUrl);
//			if (StringUtil.isNotEmpty(received)) { // TODO: 准备废弃received
//				visaDto.setReceived(Double.parseDouble(received));
//				serviceOrderDto.setReceived(Double.parseDouble(received));
//			}
			if (StringUtil.isNotEmpty(perAmount)) {
				visaDto.setPerAmount(Double.parseDouble(perAmount));
				serviceOrderDto.setPerAmount(Double.parseDouble(perAmount));
			}
			if (StringUtil.isNotEmpty(amount)) {
				visaDto.setAmount(Double.parseDouble(amount));
				serviceOrderDto.setAmount(Double.parseDouble(amount));
			}
			if (sureExpectAmount != null) {
				if ("CNY".equalsIgnoreCase(_visaDto.getCurrency()))
					visaDto.setSureExpectAmount(new BigDecimal(sureExpectAmount * _visaDto.getExchangeRate())
							.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
				else
					visaDto.setSureExpectAmount(sureExpectAmount);
			}
			Double rate = getRate("签证", _visaDto.getId(), regionService.isCNByAdviserId(_visaDto.getAdviserId()));
			if (rate != null && rate > 0)
				visaDto.setExchangeRate(rate);
			if (StringUtil.isNotEmpty(currency))
				visaDto.setCurrency(currency);
			if (StringUtil.isNotEmpty(exchangeRate))
				visaDto.setExchangeRate(Double.parseDouble(exchangeRate));
			double _perAmount = _visaDto.getPerAmount();
			if (visaDto.getPerAmount() > 0)
				_perAmount = visaDto.getPerAmount();
			if (_perAmount < visaDto.getAmount())
				return new Response<VisaDTO>(1, "本次应收款(" + _perAmount + ")不能小于本次已收款(" + visaDto.getAmount() + ")!",
						null);
			visaDto.setDiscount(_perAmount - visaDto.getAmount());
			if (StringUtil.isNotEmpty(invoiceNumber))
				visaDto.setInvoiceNumber(invoiceNumber);
			if (StringUtil.isNotEmpty(adviserId)) {
				visaDto.setAdviserId(StringUtil.toInt(adviserId));
			}
			if (StringUtil.isNotEmpty(maraId))
				visaDto.setMaraId(StringUtil.toInt(maraId));
			if (StringUtil.isNotEmpty(officialId)) {
				visaDto.setOfficialId(StringUtil.toInt(officialId));
			}
			if (StringUtil.isNotEmpty(bankCheck))
				visaDto.setBankCheck(bankCheck);
			if (StringUtil.isNotEmpty(isChecked))
				visaDto.setChecked("true".equalsIgnoreCase(isChecked));
			else
				visaDto.setChecked(_visaDto.isChecked());
			if (StringUtil.isNotEmpty(remarks))
				visaDto.setRemarks(remarks);
			if (StringUtil.isNotEmpty(verifyCode))
				visaDto.setVerifyCode(verifyCode.replace("$", "").replace("#", "").replace(" ", ""));
			double commission = visaDto.getAmount();
			visaDto.setGst(commission / 11);
			visaDto.setDeductGst(commission - visaDto.getGst());
			visaDto.setBonus(visaDto.getDeductGst() * 0.1);
			visaDto.setExpectAmount(commission);
			if (_visaDto.getKjApprovalDate() == null || _visaDto.getKjApprovalDate().getTime() == 0)
				visaDto.setKjApprovalDate(new Date()); // debug
			if (visaService.updateVisa(visaDto) > 0) {
				VisaDTO _visaDTO = visaService.getVisaById(visaDto.getId());
				serviceOrderDto.setReceivable(_visaDTO.getTotalPerAmount());
				serviceOrderDto.setReceived(_visaDTO.getTotalAmount());
				serviceOrderService.updateServiceOrder(serviceOrderDto); // 同步修改服务订单
				ApplicantDTO applicantDto = serviceOrderDto.getApplicant();
				String msg = "";
				if (applicantDto != null && applicantBirthday != null) {
					applicantDto.setBirthday(new Date(Long.parseLong(applicantBirthday)));
					if (applicantService.update(applicantDto) <= 0)
						msg += "申请人生日修改失败! (serviceOrderId:" + serviceOrderDto.getId() + ", applicantId:"
								+ applicantDto.getId() + ", applicantBirthday:" + applicantDto.getBirthday() + ");";
					else
						msg += "申请人生日修改成功. (serviceOrderId:" + serviceOrderDto.getId() + ", applicantId:"
								+ applicantDto.getId() + ", applicantBirthday:" + applicantDto.getBirthday() + ");";
				}
				return new Response<VisaDTO>(0, msg, visaDto);
			} else {
				return new Response<VisaDTO>(1, "修改失败.", null);
			}
		} catch (ServiceException e) {
			return new Response<VisaDTO>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/kjUpdate", method = RequestMethod.POST)
	@ResponseBody
	public Response<VisaDTO> kjUpdate(@RequestParam(value = "id") int id,
									  @RequestParam(value = "sureExpectAmount", required = false) Double sureExpectAmount,
									  @RequestParam(value = "bonus", required = false) Double bonus,
									  @RequestParam(value = "bonusDate", required = false) String bonusDate, HttpServletRequest request,
									  HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo != null)
				if (adminUserLoginInfo == null || (!"SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())
						&& !"KJ".equalsIgnoreCase(adminUserLoginInfo.getApList())))
					return new Response<VisaDTO>(1, "仅限会计修改.", null);
			return updateOne(id, sureExpectAmount, bonus, bonusDate, true);
		} catch (ServiceException e) {
			return new Response<VisaDTO>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/updateKjApprovalDate", method = RequestMethod.POST)
	@ResponseBody
	public Response<VisaDTO> updateKjApprovalDate(@RequestParam(value = "id") int id,
												  @RequestParam(value = "kjApprovalDate") String kjApprovalDate, HttpServletRequest request,
												  HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			VisaDTO visaDto = visaService.getVisaById(id);
			if (visaDto == null)
				return new Response<VisaDTO>(1, "签证佣金订单不存在,修改失败.", null);
			if (adminUserLoginInfo != null && ("KJ".equalsIgnoreCase(adminUserLoginInfo.getApList())
					|| "SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())))
				visaDto.setKjApprovalDate(new Date(Long.parseLong(kjApprovalDate)));
			else
				return new Response<VisaDTO>(1, "只有会计和超级管理员能修改会计审核日期.", null);
			if (visaService.updateVisa(visaDto) > 0) {
				LOG.info("修改签证订单提交审核日期.(id=" + id + ",kjApprovalDate=" + kjApprovalDate);
				return new Response<VisaDTO>(0, visaDto);
			} else
				return new Response<VisaDTO>(1, "修改失败.", null);
		} catch (ServiceException e) {
			return new Response<VisaDTO>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/update", method = RequestMethod.PUT)
	@ResponseBody
	public Response<Integer> update(@RequestBody List<BatchUpdateVisa> batchUpdateList, HttpServletRequest request,
									HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo != null)
				if (adminUserLoginInfo == null || (!"SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())
						&& !"KJ".equalsIgnoreCase(adminUserLoginInfo.getApList())))
					return new Response<Integer>(1, "仅限会计修改.", 0);
			return batchUpdate(batchUpdateList, false);
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/kjUpdate", method = RequestMethod.PUT)
	@ResponseBody
	public Response<Integer> kjUpdate(@RequestBody List<BatchUpdateVisa> batchUpdateList, HttpServletRequest request,
									  HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo != null)
				if (adminUserLoginInfo == null || (!"SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())
						&& !"KJ".equalsIgnoreCase(adminUserLoginInfo.getApList())))
					return new Response<Integer>(1, "仅限会计修改.", 0);
			return batchUpdate(batchUpdateList, true);
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	private Response<Integer> batchUpdate(List<BatchUpdateVisa> batchUpdateList, boolean isChangeState)
			throws ServiceException {
		int i = 0;
		for (BatchUpdateVisa batchUpdateDto : batchUpdateList) {
			updateOne(batchUpdateDto.getId(), batchUpdateDto.getSureExpectAmount(), batchUpdateDto.getBonus(),
					batchUpdateDto.getBonusDate(), isChangeState);
			i++;
		}
		return new Response<Integer>(0, i);
	}

	private Response<VisaDTO> updateOne(int id, Double sureExpectAmount, Double bonus, String bonusDate,
										boolean isChangeState) throws ServiceException {
		VisaDTO visaDto = visaService.getVisaById(id);
		if (visaDto == null)
			return new Response<VisaDTO>(1, "签证佣金订单订单(ID:" + id + ")不存在!", null);
		if (sureExpectAmount != null) {
			if ("CNY".equalsIgnoreCase(visaDto.getCurrency()))
				visaDto.setSureExpectAmount(new BigDecimal(sureExpectAmount * visaDto.getExchangeRate())
						.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
			else
				visaDto.setSureExpectAmount(sureExpectAmount);
		}
		if (bonus != null)
			visaDto.setBonus(bonus);
		if (bonusDate != null)
			visaDto.setBonusDate(new Date(Long.parseLong(bonusDate)));
		if (isChangeState) {
			visaDto.setState(ReviewKjStateEnum.COMPLETE.toString());
			visaDto.setCommissionState(CommissionStateEnum.YJY.toString());
			// 修改文案佣金订单状态
			VisaOfficialDTO visaOfficialDto = visaOfficialService.getByServiceOrderId(visaDto.getServiceOrderId());
			if (visaOfficialDto != null && !visaOfficialDto.isMerged()) {
				LOG.info(StringUtil.merge("文案佣金订单(", visaOfficialDto.getId(), ")合账."));
				visaOfficialService.updateMerged(visaOfficialDto.getId(), Boolean.TRUE);
			}
		}
		return visaService.updateVisa(visaDto) > 0 ? new Response<VisaDTO>(0, visaDto)
				: new Response<VisaDTO>(1, "修改失败.", null);
	}

	@RequestMapping(value = "/count", method = RequestMethod.GET)
	@ResponseBody
	@Deprecated
	public Response<Integer> countVisa(@RequestParam(value = "id", required = false) Integer id,
									   @RequestParam(value = "keyword", required = false) String keyword,
									   @RequestParam(value = "startHandlingDate", required = false) String startHandlingDate,
									   @RequestParam(value = "endHandlingDate", required = false) String endHandlingDate,
									   @RequestParam(value = "commissionState", required = false) String commissionState,
									   @RequestParam(value = "startKjApprovalDate", required = false) String startKjApprovalDate,
									   @RequestParam(value = "endKjApprovalDate", required = false) String endKjApprovalDate,
									   @RequestParam(value = "startDate", required = false) String startDate,
									   @RequestParam(value = "endDate", required = false) String endDate,
									   @RequestParam(value = "startInvoiceCreate", required = false) String startInvoiceCreate,
									   @RequestParam(value = "endInvoiceCreate", required = false) String endInvoiceCreate,
									   @RequestParam(value = "regionId", required = false) Integer regionId,
									   @RequestParam(value = "adviserId", required = false) Integer adviserId,
									   @RequestParam(value = "userId", required = false) Integer userId,
									   @RequestParam(value = "userName", required = false) String userName,
									   @RequestParam(value = "applicantName", required = false) String applicantName,
									   @RequestParam(value = "state", required = false) String state, HttpServletRequest request,
									   HttpServletResponse response) {

		// 会计角色过滤状态
		List<String> stateList = null;
		if (getKjId(request) != null) {
			stateList = new ArrayList<>();
			stateList.add(ReviewKjStateEnum.REVIEW.toString());
			stateList.add(ReviewKjStateEnum.FINISH.toString());
			stateList.add(ReviewKjStateEnum.COMPLETE.toString());
			stateList.add(ReviewKjStateEnum.CLOSE.toString());
		}

		List<String> commissionStateList = null;
		if (StringUtil.isNotEmpty(commissionState))
			commissionStateList = Arrays.asList(commissionState.split(","));

		List<Integer> regionIdList = null;
		if (regionId != null && regionId > 0)
			regionIdList = ListUtil.buildArrayList(regionId);

//		Date _startKjApprovalDate = null;
//		if (startKjApprovalDate != null)
//			_startKjApprovalDate = new Date(Long.parseLong(startKjApprovalDate));
//		Date _endKjApprovalDate = null;
//		if (endKjApprovalDate != null)
//			_endKjApprovalDate = new Date(Long.parseLong(endKjApprovalDate));

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
				// 更改当前顾问编号
				Integer newAdviserId = getAdviserId(request);
				if (newAdviserId != null)
					adviserId = newAdviserId;
			}

			return new Response<Integer>(0, visaService.countVisa(id, keyword, startHandlingDate, endHandlingDate,
					stateList, commissionStateList, startKjApprovalDate, endKjApprovalDate, startDate, endDate,
					startInvoiceCreate, endInvoiceCreate, regionIdList, adviserId, userId, userName, applicantName, state));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public ListResponse<List<VisaDTO>> listVisa(@RequestParam(value = "id", required = false) Integer id,
												@RequestParam(value = "keyword", required = false) String keyword,
												@RequestParam(value = "startHandlingDate", required = false) String startHandlingDate,
												@RequestParam(value = "endHandlingDate", required = false) String endHandlingDate,
												@RequestParam(value = "commissionState", required = false) String commissionState,
												@RequestParam(value = "startKjApprovalDate", required = false) String startKjApprovalDate,
												@RequestParam(value = "endKjApprovalDate", required = false) String endKjApprovalDate,
												@RequestParam(value = "startDate", required = false) String startDate,
												@RequestParam(value = "endDate", required = false) String endDate,
												@RequestParam(value = "startInvoiceCreate", required = false) String startInvoiceCreate,
												@RequestParam(value = "endInvoiceCreate", required = false) String endInvoiceCreate,
												@RequestParam(value = "regionId", required = false) Integer regionId,
												@RequestParam(value = "adviserId", required = false) Integer adviserId,
												@RequestParam(value = "userId", required = false) Integer userId,
												@RequestParam(value = "userName", required = false) String userName,
												@RequestParam(value = "applicantName", required = false) String applicantName,
												@RequestParam(value = "state", required = false) String state, @RequestParam(value = "pageNum") int pageNum,
												@RequestParam(value = "pageSize") int pageSize,
												@RequestParam(value = "sorter", required = false) String sorter, HttpServletRequest request,
												HttpServletResponse response) {

		// 会计角色过滤状态
		List<String> stateList = null;
		if (state == null && getKjId(request) != null) {
			stateList = new ArrayList<>();
			stateList.add(ReviewKjStateEnum.REVIEW.toString());
			stateList.add(ReviewKjStateEnum.FINISH.toString());
			stateList.add(ReviewKjStateEnum.COMPLETE.toString());
//			stateList.add(ReviewKjStateEnum.CLOSE.toString());
		}

		List<String> commissionStateList = null;
		if (StringUtil.isNotEmpty(commissionState))
			commissionStateList = Arrays.asList(commissionState.split(","));

		List<Integer> regionIdList = null;
		if (regionId != null && regionId > 0)
			regionIdList = ListUtil.buildArrayList(regionId);

		Sorter _sorter = null;
		if (sorter != null)
			_sorter = JSON.parseObject(sorter, Sorter.class);

//		Date _startKjApprovalDate = null;
//		if (startKjApprovalDate != null)
//			_startKjApprovalDate = new Date(Long.parseLong(startKjApprovalDate));
//		Date _endKjApprovalDate = null;
//		if (endKjApprovalDate != null)
//			_endKjApprovalDate = new Date(Long.parseLong(endKjApprovalDate));

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
				// 更改当前顾问编号
				Integer newAdviserId = getAdviserId(request);
				if (newAdviserId != null)
					adviserId = newAdviserId;
				if (adminUserLoginInfo == null)
					return new ListResponse<List<VisaDTO>>(false, pageSize, 0, null, "No permission !");
				if ("GW".equalsIgnoreCase(adminUserLoginInfo.getApList()) && adviserId == null)
					return new ListResponse<List<VisaDTO>>(false, pageSize, 0, null, "无法获取顾问编号，请退出重新登录后再尝试．");
			}
			
			int total = visaService.countVisa(id, keyword, startHandlingDate, endHandlingDate, stateList,
					commissionStateList, startKjApprovalDate, endKjApprovalDate, startDate, endDate, startInvoiceCreate,
					endInvoiceCreate, regionIdList, adviserId, userId, userName,  applicantName, state);
			List<VisaDTO> list = visaService.listVisa(id, keyword, startHandlingDate, endHandlingDate, stateList,
					commissionStateList, startKjApprovalDate, endKjApprovalDate, startDate, endDate, startInvoiceCreate,
					endInvoiceCreate, regionIdList, adviserId, userId, userName, applicantName, state, pageNum,
					pageSize, _sorter);
			list.forEach(v -> {
				if (v.getServiceOrderId() > 0)
					try {
						ServiceOrderDTO serviceOrderDto = serviceOrderService
								.getServiceOrderById(v.getServiceOrderId());
						List<ApplicantDTO> applicantDTOS=new ArrayList<>();
						if (serviceOrderDto != null) {
							v.setServiceOrder(serviceOrderDto);
							List<ApplicantDTO> applicant = v.getApplicant();
								for (ApplicantDTO applicantDto : applicant) {
									if (applicantDto != null) {
										if (StringUtil.isEmpty(applicantDto.getUrl()))
											applicantDto.setUrl(serviceOrderDto.getNutCloud());
										if (StringUtil.isEmpty(applicantDto.getContent()))
											applicantDto.setContent(serviceOrderDto.getInformation());
										applicantDTOS.add(applicantDto);
									}
								}
							v.setApplicant(applicantDTOS);
						}

					} catch (ServiceException e) {
					}
				try {
					List<MailRemindDTO> mailRemindDTOS = mailRemindService.list(getAdviserId(request),
							getOfficialId(request), getKjId(request), null, v.getId(), null, null, false, true);
					v.setMailRemindDTOS(mailRemindDTOS);
				} catch (ServiceException serviceException) {
					serviceException.printStackTrace();
				}
				try{
					ServicePackagePriceDO servicePackagePriceDO = servicePackagePriceService.getServicePackagePriceByServiceId(v.getServiceId());
					if(servicePackagePriceDO!=null) {
						double thirdPrince =  servicePackagePriceDO.getThirdPrince();
						BigDecimal third_prince = BigDecimal.valueOf(thirdPrince);
						double expectAmountAUD = new BigDecimal(v.getAmountAUD()).subtract(third_prince).doubleValue();
						v.setExpectAmount(expectAmountAUD > 0.0 ? expectAmountAUD : 0.0);
					}
				}catch (ServiceException serviceException){
					serviceException.printStackTrace();
				}
			});
			if (list == null) {
				list = new ArrayList<>();
			}
			return new ListResponse<List<VisaDTO>>(true, pageSize, total, list, "");
		} catch (ServiceException e) {
			return new ListResponse<List<VisaDTO>>(false, pageSize, 0, null, e.getMessage());
		}
	}

	@RequestMapping(value = "/upload", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> upload(@RequestParam MultipartFile file, HttpServletRequest request,
									HttpServletResponse response) throws IllegalStateException, IOException {
		super.setPostHeader(response);
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String message = "";
		int n = 0;
		Response<String> r = super.upload2(file, request.getSession(), "/tmp/");
		try (InputStream is = new FileInputStream("/data" + r.getData())) {
			jxl.Workbook wb = jxl.Workbook.getWorkbook(is);
			Sheet sheet = wb.getSheet(0);
			for (int i = 1; i < sheet.getRows(); i++) {
				Cell[] cells = sheet.getRow(i);
                if(cells.length == 0)
                	continue;
				String _id = cells[0].getContents();
				String _bonus = cells[15].getContents();
				String _bonusDate = cells[16].getContents();
				try {
					VisaDTO visaDto = visaService.getVisaById(Integer.parseInt(_id));
					if (visaDto == null) {
						message += "[" + _id + "]佣金订单不存在;";
						continue;
					}
					if (!CommissionStateEnum.DJY.toString().equals(visaDto.getCommissionState())) {
						message += "[" + _id + "]佣金订单状态不是待结佣;";
						continue;
					}
					Response<VisaDTO> _r = updateOne(Integer.parseInt(_id), null,
							StringUtil.isEmpty(_bonus) ? null : Double.parseDouble(_bonus.trim()),
							StringUtil.isEmpty(_bonusDate) ? null
									: simpleDateFormat.parse(_bonusDate.trim()).getTime() + "",
							true);
					if (_r.getCode() > 0)
						message += "[" + _id + "]" + _r.getMessage() + ";";
					else
						n++;
				} catch (NumberFormatException | ServiceException e) {
					message += "[" + _id + "]" + e.getMessage() + ";";
				}
			}
		} catch (BiffException | IOException | ParseException e) {
			return new Response<Integer>(1, "上传失败:" + e.getMessage(), 0);
		}
		if (StringUtil.isNotEmpty(message))
			LOG.warn(message);
		return new Response<Integer>(0, message, n);
	}

	@RequestMapping(value = "/down", method = RequestMethod.GET)
	@ResponseBody
	public void down(@RequestParam(value = "id", required = false) Integer id,
					 @RequestParam(value = "keyword", required = false) String keyword,
					 @RequestParam(value = "startHandlingDate", required = false) String startHandlingDate,
					 @RequestParam(value = "endHandlingDate", required = false) String endHandlingDate,
					 @RequestParam(value = "commissionState", required = false) String commissionState,
					 @RequestParam(value = "startKjApprovalDate", required = false) String startKjApprovalDate,
					 @RequestParam(value = "endKjApprovalDate", required = false) String endKjApprovalDate,
					 @RequestParam(value = "startDate", required = false) String startDate,
					 @RequestParam(value = "endDate", required = false) String endDate,
					 @RequestParam(value = "startInvoiceCreate", required = false) String startInvoiceCreate,
					 @RequestParam(value = "endInvoiceCreate", required = false) String endInvoiceCreate,
					 @RequestParam(value = "regionId", required = false) Integer regionId,
					 @RequestParam(value = "adviserId", required = false) Integer adviserId,
					 @RequestParam(value = "userId", required = false) Integer userId,
					 @RequestParam(value = "userName", required = false) String userName,
					 @RequestParam(value = "applicantName", required = false) String applicantName,
					 @RequestParam(value = "state", required = false) String state, HttpServletRequest request,
					 HttpServletResponse response) {

		// 更改当前顾问编号
		Integer newAdviserId = getAdviserId(request);
		if (newAdviserId != null)
			adviserId = newAdviserId;

		// 会计角色过滤状态
		List<String> stateList = null;
		if (getKjId(request) != null) {
			stateList = new ArrayList<>();
			stateList.add(ReviewKjStateEnum.REVIEW.toString());
			stateList.add(ReviewKjStateEnum.FINISH.toString());
			stateList.add(ReviewKjStateEnum.COMPLETE.toString());
//			stateList.add(ReviewKjStateEnum.CLOSE.toString());
		}

		List<String> commissionStateList = null;
		if (StringUtil.isNotEmpty(commissionState))
			commissionStateList = Arrays.asList(commissionState.split(","));

		List<Integer> regionIdList = null;
		if (regionId != null && regionId > 0)
			regionIdList = ListUtil.buildArrayList(regionId);

		try {

			// 处理顾问管理员
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo != null && "GW".equalsIgnoreCase(adminUserLoginInfo.getApList())
					&& adminUserLoginInfo.getRegionId() != null && adminUserLoginInfo.getRegionId() > 0) {
				List<RegionDTO> regionList = regionService.listRegion(adminUserLoginInfo.getRegionId());
				regionIdList = ListUtil.buildArrayList(adminUserLoginInfo.getRegionId());
				for (RegionDTO region : regionList)
					regionIdList.add(region.getId());
				adviserId = null;
			}

			response.reset();// 清空输出流
			String tableName = "visa_information";
			response.setHeader("Content-disposition",
					"attachment; filename=" + new String(tableName.getBytes("GB2312"), "8859_1") + ".xls");
			response.setContentType("application/msexcel");

//			Date _startKjApprovalDate = null;
//			if (startKjApprovalDate != null)
//				_startKjApprovalDate = new Date(Long.parseLong(startKjApprovalDate));
//			Date _endKjApprovalDate = null;
//			if (endKjApprovalDate != null)
//				_endKjApprovalDate = new Date(Long.parseLong(endKjApprovalDate));

			List<VisaDTO> list = visaService.listVisa(id, keyword, startHandlingDate, endHandlingDate, stateList,
					commissionStateList, startKjApprovalDate, endKjApprovalDate, startDate, endDate, startInvoiceCreate,
					endInvoiceCreate, regionIdList, adviserId, userId, userName, applicantName, state, 0, 9999, null);

//			list.forEach(v -> {
//				if (v.getServiceOrderId() > 0)
//					try {
//						ServiceOrderDTO serviceOrderDto = serviceOrderService
//								.getServiceOrderById(v.getServiceOrderId());
//						if (serviceOrderDto != null)
//							v.setServiceOrder(serviceOrderDto);
//					} catch (ServiceException e) {
//
//					}
//			});
			int _regionId = 0;
			if (ObjectUtil.isNotNull(adviserId) && adviserId > 0)
				_regionId = adviserService.getAdviserById(adviserId).getRegionId();
			// 超级管理员导出佣金订单
			if ("SUPERAD".equals(adminUserLoginInfo.getApList())) {
				OutputStream os = response.getOutputStream();
				jxl.Workbook wb;
				InputStream is;
				try {
					is = this.getClass().getResourceAsStream("/visa_information.xls");
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
				List<AdviserDTO> adviserDTOS = adviserService.listAdviser(null, null, 0, 1000);
				Map<Integer, String> adviserMap = adviserDTOS.stream().collect(Collectors.toMap(AdviserDTO::getId, AdviserDTO::getName, (v1, v2) -> v2));
				List<ServiceDTO> serviceDTOS = serviceService.listAllService(null, 0, 999);
				Map<Integer, ServiceDTO> serviceMap = serviceDTOS.stream().collect(Collectors.toMap(ServiceDTO::getId, Function.identity()));
				int i = 1;
				for (VisaDTO visaDto : list) {
					sheet.addCell(new Label(0, i, String.valueOf(visaDto.getId()), cellFormat));
					sheet.addCell(new Label(1, i, sdf.format(visaDto.getGmtCreate()), cellFormat));
					if (visaDto.getReceiveDate() != null) {
						sheet.addCell(new Label(2, i, sdf.format(visaDto.getReceiveDate()), cellFormat));
					}
					sheet.addCell(new Label(3, i, visaDto.getUserName(), cellFormat));
					sheet.addCell(new Label(4, i, visaDto.getReceiveTypeName(), cellFormat));
					ServiceDTO serviceDTO = serviceMap.get(visaDto.getServiceId());
					if (ObjectUtil.isNotNull(serviceDTO)) {
						sheet.addCell(new Label(5, i, serviceDTO.getName() + "-" + serviceDTO.getCode(), cellFormat));
					}
					sheet.addCell(new Label(6, i, visaDto.getTotalAmountCNY() + "", cellFormat));
					sheet.addCell(new Label(7, i, visaDto.getTotalAmountAUD() + "", cellFormat));
					sheet.addCell(new Label(8, i, visaDto.getTotalAmountCNY() + "", cellFormat));
					sheet.addCell(new Label(9, i, visaDto.getTotalAmountAUD() + "", cellFormat));
					sheet.addCell(new Label(10, i, visaDto.getCurrency(), cellFormat));
					sheet.addCell(new Label(11, i, visaDto.getExchangeRate() + "", cellFormat));
					sheet.addCell(new Label(12, i, visaDto.getAmountCNY() + "", cellFormat));
					sheet.addCell(new Label(13, i, visaDto.getAmountAUD() + "", cellFormat));
					sheet.addCell(new Label(14, i, visaDto.getGst() + "", cellFormat));
					sheet.addCell(new Label(15, i, visaDto.getDeductGst() + "", cellFormat));
					sheet.addCell(new Label(16, i, visaDto.getExpectAmount() + "", cellFormat));
					sheet.addCell(new Label(17, i, visaDto.getSureExpectAmount() + "", cellFormat));
					sheet.addCell(new Label(18, i, visaDto.getBonus() + "", cellFormat));
					if (ObjectUtil.isNotNull(visaDto.getBonusDate())) {
						sheet.addCell(new Label(19, i, sdf.format(visaDto.getBonusDate()) + "", cellFormat));
					}
					if (StringUtil.isNotEmpty(visaDto.getVerifyCode())) {
						sheet.addCell(new Label(20, i, visaDto.getVerifyCode() + "", cellFormat));
					}
					String adviserName = adviserMap.get(visaDto.getAdviserId());
					if (StringUtil.isNotEmpty(adviserName)) {
						sheet.addCell(new Label(22, i, adviserName + "", cellFormat));
					}
					sheet.addCell(new Label(23, i, visaDto.getState() + "", cellFormat));
					sheet.addCell(new Label(24, i, visaDto.getRemarks() + "", cellFormat));
					i++;
				}
				wbe.write();
				wbe.close();
				if (is != null)
					is.close();
				if (os != null)
					os.close();
			}
			// 会计导出佣金订单
			if (getKjId(request) != null) {
				if (regionService.isCN(_regionId)) {
					OutputStream os = response.getOutputStream();
					jxl.Workbook wb;
					InputStream is;
					try {
						is = this.getClass().getResourceAsStream("/VisaTemplateCNY.xls");
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
					for (VisaDTO visaDto : list) {
						sheet.addCell(new Label(0, i, "CV" + visaDto.getId(), cellFormat));
						sheet.addCell(new Label(1, i, sdf.format(visaDto.getGmtCreate()), cellFormat));
						if (visaDto.getReceiveDate() != null)
							sheet.addCell(new Label(2, i, sdf.format(visaDto.getReceiveDate()), cellFormat));
						sheet.addCell(new Label(3, i, visaDto.getUserName(), cellFormat));
						sheet.addCell(new Label(4, i, visaDto.getReceiveTypeName(), cellFormat));
						sheet.addCell(new Label(5, i, visaDto.getServiceCode(), cellFormat));
						sheet.addCell(new Label(6, i, visaDto.getTotalAmountCNY() + "", cellFormat));
						sheet.addCell(new Label(7, i, visaDto.getTotalAmountAUD() + "", cellFormat));
						sheet.addCell(new Label(8, i, visaDto.getTotalAmountCNY() + "", cellFormat));
						sheet.addCell(new Label(9, i, visaDto.getTotalAmountAUD() + "", cellFormat));
						sheet.addCell(new Label(10, i, visaDto.getCurrency(), cellFormat));
						sheet.addCell(new Label(11, i, visaDto.getExchangeRate() + "", cellFormat));
						sheet.addCell(new Label(12, i, visaDto.getAmountCNY() + "", cellFormat));
						sheet.addCell(new Label(13, i, visaDto.getAmountAUD() + "", cellFormat));
						sheet.addCell(new Label(14, i, visaDto.getExpectAmountAUD() + "", cellFormat));
						sheet.addCell(new Label(15, i, visaDto.getExpectAmountAUD() + "", cellFormat));
						sheet.addCell(new Label(16, i, visaDto.getBonus() + "", cellFormat));
						if (visaDto.getBonusDate() != null)
							sheet.addCell(new Label(17, i, sdf.format(visaDto.getBonusDate()), cellFormat));
						sheet.addCell(new Label(18, i, visaDto.getBankCheck(), cellFormat));
						sheet.addCell(new Label(19, i, visaDto.isChecked() + "", cellFormat));
						sheet.addCell(new Label(20, i, visaDto.getAdviserName(), cellFormat));
						if (visaDto.getState() != null)
							sheet.addCell(new Label(21, i, getStateStr(visaDto.getState()), cellFormat));
						if (visaDto.getKjApprovalDate() != null)
							sheet.addCell(new Label(22, i, sdf.format(visaDto.getKjApprovalDate()), cellFormat));
						sheet.addCell(new Label(23, i, visaDto.getRemarks(), cellFormat));
						i++;
					}
					wbe.write();
					wbe.close();
					if (is != null)
						is.close();
					if (os != null)
						os.close();

				} else {

					//AUD

					OutputStream os = response.getOutputStream();
					jxl.Workbook wb;
					InputStream is;
					try {
						is = this.getClass().getResourceAsStream("/VisaTemplate.xls");
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
					for (VisaDTO visaDto : list) {
						sheet.addCell(new Label(0, i, "CV" + visaDto.getId(), cellFormat));
						sheet.addCell(new Label(1, i, sdf.format(visaDto.getGmtCreate()), cellFormat));
						if (visaDto.getReceiveDate() != null)
							sheet.addCell(new Label(2, i, sdf.format(visaDto.getReceiveDate()), cellFormat));
						sheet.addCell(new Label(3, i, visaDto.getUserName(), cellFormat));
						sheet.addCell(new Label(4, i, visaDto.getReceiveTypeName(), cellFormat));
						sheet.addCell(new Label(5, i, visaDto.getServiceCode(), cellFormat));
						sheet.addCell(new Label(6, i, visaDto.getTotalAmountCNY() + "", cellFormat));
						sheet.addCell(new Label(7, i, visaDto.getTotalAmountAUD() + "", cellFormat));
						sheet.addCell(new Label(8, i, visaDto.getTotalAmountCNY() + "", cellFormat));
						sheet.addCell(new Label(9, i, visaDto.getTotalAmountAUD() + "", cellFormat));
						sheet.addCell(new Label(10, i, visaDto.getCurrency(), cellFormat));
						sheet.addCell(new Label(11, i, visaDto.getExchangeRate() + "", cellFormat));
						sheet.addCell(new Label(12, i, visaDto.getAmountCNY() + "", cellFormat));
						sheet.addCell(new Label(13, i, visaDto.getAmountAUD() + "", cellFormat));
						sheet.addCell(new Label(14, i, visaDto.getGstAUD() + "", cellFormat));
						sheet.addCell(new Label(15, i, visaDto.getDeductGstAUD() + "", cellFormat));
						sheet.addCell(new Label(16, i, visaDto.getExpectAmountAUD() + "", cellFormat));
						sheet.addCell(new Label(17, i, visaDto.getExpectAmountAUD() + "", cellFormat));
						sheet.addCell(new Label(18, i, visaDto.getBonus() + "", cellFormat));
						if (visaDto.getBonusDate() != null)
							sheet.addCell(new Label(19, i, sdf.format(visaDto.getBonusDate()), cellFormat));
						sheet.addCell(new Label(20, i, visaDto.getBankCheck(), cellFormat));
						sheet.addCell(new Label(21, i, visaDto.isChecked() + "", cellFormat));
						sheet.addCell(new Label(22, i, visaDto.getAdviserName(), cellFormat));
						if (visaDto.getState() != null)
							sheet.addCell(new Label(23, i, getStateStr(visaDto.getState()), cellFormat));
						if (visaDto.getKjApprovalDate() != null)
							sheet.addCell(new Label(24, i, sdf.format(visaDto.getKjApprovalDate()), cellFormat));
						sheet.addCell(new Label(25, i, visaDto.getRemarks(), cellFormat));
						i++;
					}
					wbe.write();
					wbe.close();
					if (is != null)
						is.close();
					if (os != null)
						os.close();

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	@RequestMapping(value = "/down_V2", method = RequestMethod.GET)
	@ResponseBody
	public Response<String> down_V2(@RequestParam(value = "id", required = false) Integer id,
					 @RequestParam(value = "keyword", required = false) String keyword,
					 @RequestParam(value = "startHandlingDate", required = false) String startHandlingDate,
					 @RequestParam(value = "endHandlingDate", required = false) String endHandlingDate,
					 @RequestParam(value = "commissionState", required = false) String commissionState,
					 @RequestParam(value = "startKjApprovalDate", required = false) String startKjApprovalDate,
					 @RequestParam(value = "endKjApprovalDate", required = false) String endKjApprovalDate,
					 @RequestParam(value = "startDate", required = false) String startDate,
					 @RequestParam(value = "endDate", required = false) String endDate,
					 @RequestParam(value = "startInvoiceCreate", required = false) String startInvoiceCreate,
					 @RequestParam(value = "endInvoiceCreate", required = false) String endInvoiceCreate,
					 @RequestParam(value = "regionId", required = false) Integer regionId,
					 @RequestParam(value = "adviserId", required = false) Integer adviserId,
					 @RequestParam(value = "userId", required = false) Integer userId,
					 @RequestParam(value = "userName", required = false) String userName,
					 @RequestParam(value = "applicantName", required = false) String applicantName,
					 @RequestParam(value = "state", required = false) String state, HttpServletRequest request,
					 HttpServletResponse response) {

		// 更改当前顾问编号
		Integer newAdviserId = getAdviserId(request);
		if (newAdviserId != null)
			adviserId = newAdviserId;

		// 会计角色过滤状态
		List<String> stateList = null;
		if (getKjId(request) != null) {
			stateList = new ArrayList<>();
			stateList.add(ReviewKjStateEnum.REVIEW.toString());
			stateList.add(ReviewKjStateEnum.FINISH.toString());
			stateList.add(ReviewKjStateEnum.COMPLETE.toString());
//			stateList.add(ReviewKjStateEnum.CLOSE.toString());
		}

		List<String> commissionStateList = null;
		if (StringUtil.isNotEmpty(commissionState))
			commissionStateList = Arrays.asList(commissionState.split(","));

		List<Integer> regionIdList = null;
		if (regionId != null && regionId > 0)
			regionIdList = ListUtil.buildArrayList(regionId);

		try {

			// 处理顾问管理员
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo != null && "GW".equalsIgnoreCase(adminUserLoginInfo.getApList())
					&& adminUserLoginInfo.getRegionId() != null && adminUserLoginInfo.getRegionId() > 0) {
				List<RegionDTO> regionList = regionService.listRegion(adminUserLoginInfo.getRegionId());
				regionIdList = ListUtil.buildArrayList(adminUserLoginInfo.getRegionId());
				for (RegionDTO region : regionList)
					regionIdList.add(region.getId());
				adviserId = null;
			}

			response.reset();// 清空输出流
			String tableName = "visa_information";
			response.setHeader("Content-disposition",
					"attachment; filename=" + new String(tableName.getBytes("GB2312"), "8859_1") + ".xls");
			response.setContentType("application/msexcel");

			List<VisaDTO> list = visaService.listVisa(id, keyword, startHandlingDate, endHandlingDate, stateList,
					commissionStateList, startKjApprovalDate, endKjApprovalDate, startDate, endDate, startInvoiceCreate,
					endInvoiceCreate, regionIdList, adviserId, userId, userName, applicantName, state, 0, 9999, null);

			int _regionId;
			if (ObjectUtil.isNotNull(adviserId) && adviserId > 0)
				_regionId = adviserService.getAdviserById(adviserId).getRegionId();
            else {
                _regionId = 0;
            }
            // 超级管理员导出佣金订单
//			if ("SUPERAD".equals(adminUserLoginInfo.getApList())) {
				// 获取token
				Map<String, Object> tokenMap = wxWorkService.getToken(WXWorkAPI.SECRET_EXCEL);
				if ((int)tokenMap.get("errcode") != 0){
					throw  new RuntimeException( tokenMap.get("errmsg").toString());
				}
				String customerToken = (String) tokenMap.get("access_token");

				List<AdviserDTO> adviserDTOS = adviserService.listAdviser(null, null, 0, 1000);
				Map<Integer, String> adviserMap = adviserDTOS.stream().collect(Collectors.toMap(AdviserDTO::getId, AdviserDTO::getName, (v1, v2) -> v2));
				List<ServiceDTO> serviceDTOS = serviceService.listAllService(null, 0, 999);
				Map<Integer, ServiceDTO> serviceMap = serviceDTOS.stream().collect(Collectors.toMap(ServiceDTO::getId, Function.identity()));


			// 创建表格
			String setupExcelAccessToken = WXWorkAPI.SETUP_EXCEL.replace("ACCESS_TOKEN", customerToken);
			final JSONObject[] parm = {new JSONObject()};
			parm[0].put("doc_type", 10);
			parm[0].put("doc_name", "visa_information-" + sdf.format(new Date()));
			log.info("parm--------------------" + Arrays.toString(parm));
			log.info("setupExcelAccessToken-------------------" + setupExcelAccessToken);

			JSONObject setupExcelJsonObject = WXWorkAPI.sendPostBody_Map(setupExcelAccessToken, parm[0]);
			log.info("setupExcelJsonObject-------------" + setupExcelJsonObject.toString());
			String docid = setupExcelJsonObject.get("docid").toString();

			// 添加子表
			String accessTokenZiBiao = WXWorkAPI.CREATE_CHILE_TABLE.replace("ACCESS_TOKEN", customerToken);
			final JSONObject[] parmZiBiao = {new JSONObject()};
        	parmZiBiao[0].put("docid", docid);
			JSONObject jsonObjectProperties = new JSONObject();
			jsonObjectProperties.put("title", "佣金订单导出信息");
			jsonObjectProperties.put("index", 2);
			parmZiBiao[0].put("properties", jsonObjectProperties);
			JSONObject jsonObject1 = WXWorkAPI.sendPostBody_Map(accessTokenZiBiao, parmZiBiao[0]);
			log.info("setupExcelJsonObject-------------" + jsonObject1.toString());

			// 获得sheetId
			Object properties = jsonObject1.get("properties");
			JSONObject jsonObject4 = JSONObject.parseObject(properties.toString());
			String sheetId = jsonObject4.get("sheet_id").toString();
			log.info("sheet_id-------------------" + sheetId);

			// 查询默认字段id
			String accessTokenMoRen = WXWorkAPI.GET_DEFAULT_FIELD.replace("ACCESS_TOKEN", customerToken);
			final JSONObject[] parmMoRen = {new JSONObject()};
			parmMoRen[0].put("docid", docid);
			parmMoRen[0].put("sheet_id", sheetId);
			parmMoRen[0].put("offset", 0);
			parmMoRen[0].put("limit", 10);
			JSONObject jsonObject5 = WXWorkAPI.sendPostBody_Map(accessTokenMoRen, parmMoRen[0]);
			log.info("setupExcelJsonObject-------------" + jsonObject5.toString());

			// 获取默认字段id
			String fieldId = "";
			Object fields = jsonObject5.get("fields");
			JSONArray jsonArray = JSONArray.parseArray(fields.toString());
			Iterator<Object> iterator = jsonArray.iterator();
			while (iterator.hasNext()) {
				Object next = iterator.next();
				JSONObject jsonObject = JSONObject.parseObject(next.toString());
				fieldId = jsonObject.get("field_id").toString();
				log.info("字段id----------------------" + fieldId);
			}

			// 更新字段
			String accessToken2 = WXWorkAPI.UPDATE_FIELD.replace("ACCESS_TOKEN", customerToken);
			final JSONObject[] parm2 = {new JSONObject()};
			parm2[0].put("docid", docid);
			parm2[0].put("sheet_id", sheetId);
			// 添加字段标题title
			List<String> exlceTitles = buildExlceTitle(adminUserLoginInfo.getApList(), _regionId);
			log.info("字段标题-----------------------" + exlceTitles);
			List<JSONObject> fieldList = new ArrayList<>();
			List<String> exlceTitleNumberList = new ArrayList<>();
			exlceTitleNumberList.add("月奖");
			exlceTitleNumberList.add("确认预售业绩");
			exlceTitleNumberList.add("预收业绩");
			exlceTitleNumberList.add("Deduct GST");
			exlceTitleNumberList.add("GST");
			exlceTitleNumberList.add("本次收款澳币");
			exlceTitleNumberList.add("本次收款人民币");
			exlceTitleNumberList.add("创建订单时汇率");
			exlceTitleNumberList.add("总计收款澳币");
			exlceTitleNumberList.add("总计收款人民币");
			exlceTitleNumberList.add("总计应收澳币");
			exlceTitleNumberList.add("总计应收人民币");
			for (String exlceTitle : exlceTitles) {
				JSONObject jsonObjectField = new JSONObject();
				jsonObjectField.put("field_title", exlceTitle);
				if (exlceTitleNumberList.contains(exlceTitle)) {
					jsonObjectField.put("field_type", "FIELD_TYPE_NUMBER");
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("decimal_places", 2);
					jsonObject.put("use_separate", false);
					jsonObjectField.put("property_number", jsonObject);
				} else {
					jsonObjectField.put("field_type", "FIELD_TYPE_TEXT");
				}
				fieldList.add(jsonObjectField);
			}
			parm2[0].put("fields", fieldList);
			JSONObject jsonObject2 = WXWorkAPI.sendPostBody_Map(accessToken2, parm2[0]);
			log.info("jsonObject2-------------" + jsonObject2.toString());

			// 删除字段
			String accessTokenShanChu = WXWorkAPI.DELETE_FIELD.replace("ACCESS_TOKEN", customerToken);
			final JSONObject[] parmShanChu = {new JSONObject()};
			parmShanChu[0].put("docid", docid);
			parmShanChu[0].put("sheet_id", sheetId);
			List<String> fielIds = new ArrayList<>();
			fielIds.add(fieldId);
			parmShanChu[0].put("field_ids", fielIds);
			JSONObject jsonObjectShanChu = WXWorkAPI.sendPostBody_Map(accessTokenShanChu, parmShanChu[0]);
			log.info("setupExcelJsonObject-------------" + jsonObjectShanChu.toString());

			String url ="";
				if ("0".equals(jsonObject1.get("errcode").toString())) {
					url = setupExcelJsonObject.get("url").toString();
					String docId = setupExcelJsonObject.get("docid").toString();
					SetupExcelDO setupExcelDO = new SetupExcelDO();
					setupExcelDO.setUrl(url);
					setupExcelDO.setDocId(docId);
                    Thread thread1 = new Thread(() -> {
						try {
							// 线程1的任务
							if ("0".equals(jsonObject1.get("errcode").toString())) {
								// 添加行记录
								String accessTokenJiLu = WXWorkAPI.INSERT_ROW.replace("ACCESS_TOKEN", customerToken);
								final JSONObject[] parmJiLu = {new JSONObject()};
								parmJiLu[0].put("docid", docid);
								parmJiLu[0].put("sheet_id", sheetId);
								for (VisaDTO visaDTO : list) {
									JSONObject jsonObjectFILEDTITLE = buileExcelJsonObject(visaDTO, adviserMap, serviceMap, adminUserLoginInfo, _regionId);
									List<JSONObject> recordsList = new ArrayList<>();
									JSONObject jsonObjectValue = new JSONObject();
									jsonObjectValue.put("values", jsonObjectFILEDTITLE);
									recordsList.add(jsonObjectValue);
									parmJiLu[0].put("records", recordsList);
									log.info("请求体--------------------------" + JSONObject.toJSONString(parmJiLu[0]));
									JSONObject jsonObjectJiLu = WXWorkAPI.sendPostBody_Map(accessTokenJiLu, parmJiLu[0]);
									log.info(accessTokenJiLu);
									log.info("setupExcelJsonObject-------------" + jsonObjectJiLu.toString());
								}
							}
						} catch (Exception e) {
							// 处理异常，例如记录日志
							e.printStackTrace();
						}
					});
					thread1.start();
				}
				StringBuilder htmlBuilder = new StringBuilder();
				htmlBuilder.append("<a href=\"");
				htmlBuilder.append(url + "\""); // 插入链接的URL
				htmlBuilder.append(" target=\"_blank");
				htmlBuilder.append("\">");
				htmlBuilder.append("点击打开Excel链接"); // 插入链接的显示文本
				htmlBuilder.append("</a>");
				WXWorkAPI.sendShareLinkMsg(url, adminUserLoginInfo.getUsername(), "导出佣金订单信息");
				return new Response<>(0, "生成Excel成功， excel链接为：" + htmlBuilder);
//			}
//			// 会计导出佣金订单
//			if (getKjId(request) != null) {
//				if (regionService.isCN(_regionId)) {
//					OutputStream os = response.getOutputStream();
//					jxl.Workbook wb;
//					InputStream is;
//					try {
//						is = this.getClass().getResourceAsStream("/VisaTemplateCNY.xls");
//					} catch (Exception e) {
//						throw new Exception("模版不存在");
//					}
//					try {
//						wb = Workbook.getWorkbook(is);
//					} catch (Exception e) {
//						throw new Exception("模版格式不支持");
//					}
//					WorkbookSettings settings = new WorkbookSettings();
//					settings.setWriteAccess(null);
//					jxl.write.WritableWorkbook wbe = Workbook.createWorkbook(os, wb, settings);
//
//					if (wbe == null) {
//						System.out.println("wbe is null !os=" + os + ",wb" + wb);
//					} else {
//						System.out.println("wbe not null !os=" + os + ",wb" + wb);
//					}
//					WritableSheet sheet = wbe.getSheet(0);
//					WritableCellFormat cellFormat = new WritableCellFormat();
//
//					int i = 1;
//					for (VisaDTO visaDto : list) {
//						sheet.addCell(new Label(0, i, "CV" + visaDto.getId(), cellFormat));
//						sheet.addCell(new Label(1, i, sdf.format(visaDto.getGmtCreate()), cellFormat));
//						if (visaDto.getReceiveDate() != null)
//							sheet.addCell(new Label(2, i, sdf.format(visaDto.getReceiveDate()), cellFormat));
//						sheet.addCell(new Label(3, i, visaDto.getUserName(), cellFormat));
//						sheet.addCell(new Label(4, i, visaDto.getReceiveTypeName(), cellFormat));
//						sheet.addCell(new Label(5, i, visaDto.getServiceCode(), cellFormat));
//						sheet.addCell(new Label(6, i, visaDto.getTotalAmountCNY() + "", cellFormat));
//						sheet.addCell(new Label(7, i, visaDto.getTotalAmountAUD() + "", cellFormat));
//						sheet.addCell(new Label(8, i, visaDto.getTotalAmountCNY() + "", cellFormat));
//						sheet.addCell(new Label(9, i, visaDto.getTotalAmountAUD() + "", cellFormat));
//						sheet.addCell(new Label(10, i, visaDto.getCurrency(), cellFormat));
//						sheet.addCell(new Label(11, i, visaDto.getExchangeRate() + "", cellFormat));
//						sheet.addCell(new Label(12, i, visaDto.getAmountCNY() + "", cellFormat));
//						sheet.addCell(new Label(13, i, visaDto.getAmountAUD() + "", cellFormat));
//						sheet.addCell(new Label(14, i, visaDto.getExpectAmountAUD() + "", cellFormat));
//						sheet.addCell(new Label(15, i, visaDto.getExpectAmountAUD() + "", cellFormat));
//						sheet.addCell(new Label(16, i, visaDto.getBonus() + "", cellFormat));
//						if (visaDto.getBonusDate() != null)
//							sheet.addCell(new Label(17, i, sdf.format(visaDto.getBonusDate()), cellFormat));
//						sheet.addCell(new Label(18, i, visaDto.getBankCheck(), cellFormat));
//						sheet.addCell(new Label(19, i, visaDto.isChecked() + "", cellFormat));
//						sheet.addCell(new Label(20, i, visaDto.getAdviserName(), cellFormat));
//						if (visaDto.getState() != null)
//							sheet.addCell(new Label(21, i, getStateStr(visaDto.getState()), cellFormat));
//						if (visaDto.getKjApprovalDate() != null)
//							sheet.addCell(new Label(22, i, sdf.format(visaDto.getKjApprovalDate()), cellFormat));
//						sheet.addCell(new Label(23, i, visaDto.getRemarks(), cellFormat));
//						i++;
//					}
//					wbe.write();
//					wbe.close();
//					if (is != null)
//						is.close();
//					if (os != null)
//						os.close();
//
//				} else {
//					//AUD
//					OutputStream os = response.getOutputStream();
//					jxl.Workbook wb;
//					InputStream is;
//					try {
//						is = this.getClass().getResourceAsStream("/VisaTemplate.xls");
//					} catch (Exception e) {
//						throw new Exception("模版不存在");
//					}
//					try {
//						wb = Workbook.getWorkbook(is);
//					} catch (Exception e) {
//						throw new Exception("模版格式不支持");
//					}
//					WorkbookSettings settings = new WorkbookSettings();
//					settings.setWriteAccess(null);
//					jxl.write.WritableWorkbook wbe = Workbook.createWorkbook(os, wb, settings);
//
//					if (wbe == null) {
//						System.out.println("wbe is null !os=" + os + ",wb" + wb);
//					} else {
//						System.out.println("wbe not null !os=" + os + ",wb" + wb);
//					}
//					WritableSheet sheet = wbe.getSheet(0);
//					WritableCellFormat cellFormat = new WritableCellFormat();
//
//					int i = 1;
//					for (VisaDTO visaDto : list) {
//						sheet.addCell(new Label(0, i, "CV" + visaDto.getId(), cellFormat));
//						sheet.addCell(new Label(1, i, sdf.format(visaDto.getGmtCreate()), cellFormat));
//						if (visaDto.getReceiveDate() != null)
//							sheet.addCell(new Label(2, i, sdf.format(visaDto.getReceiveDate()), cellFormat));
//						sheet.addCell(new Label(3, i, visaDto.getUserName(), cellFormat));
//						sheet.addCell(new Label(4, i, visaDto.getReceiveTypeName(), cellFormat));
//						sheet.addCell(new Label(5, i, visaDto.getServiceCode(), cellFormat));
//						sheet.addCell(new Label(6, i, visaDto.getTotalAmountCNY() + "", cellFormat));
//						sheet.addCell(new Label(7, i, visaDto.getTotalAmountAUD() + "", cellFormat));
//						sheet.addCell(new Label(8, i, visaDto.getTotalAmountCNY() + "", cellFormat));
//						sheet.addCell(new Label(9, i, visaDto.getTotalAmountAUD() + "", cellFormat));
//						sheet.addCell(new Label(10, i, visaDto.getCurrency(), cellFormat));
//						sheet.addCell(new Label(11, i, visaDto.getExchangeRate() + "", cellFormat));
//						sheet.addCell(new Label(12, i, visaDto.getAmountCNY() + "", cellFormat));
//						sheet.addCell(new Label(13, i, visaDto.getAmountAUD() + "", cellFormat));
//						sheet.addCell(new Label(14, i, visaDto.getGstAUD() + "", cellFormat));
//						sheet.addCell(new Label(15, i, visaDto.getDeductGstAUD() + "", cellFormat));
//						sheet.addCell(new Label(16, i, visaDto.getExpectAmountAUD() + "", cellFormat));
//						sheet.addCell(new Label(17, i, visaDto.getExpectAmountAUD() + "", cellFormat));
//						sheet.addCell(new Label(18, i, visaDto.getBonus() + "", cellFormat));
//						if (visaDto.getBonusDate() != null)
//							sheet.addCell(new Label(19, i, sdf.format(visaDto.getBonusDate()), cellFormat));
//						sheet.addCell(new Label(20, i, visaDto.getBankCheck(), cellFormat));
//						sheet.addCell(new Label(21, i, visaDto.isChecked() + "", cellFormat));
//						sheet.addCell(new Label(22, i, visaDto.getAdviserName(), cellFormat));
//						if (visaDto.getState() != null)
//							sheet.addCell(new Label(23, i, getStateStr(visaDto.getState()), cellFormat));
//						if (visaDto.getKjApprovalDate() != null)
//							sheet.addCell(new Label(24, i, sdf.format(visaDto.getKjApprovalDate()), cellFormat));
//						sheet.addCell(new Label(25, i, visaDto.getRemarks(), cellFormat));
//						i++;
//					}
//					wbe.write();
//					wbe.close();
//					if (is != null)
//						is.close();
//					if (os != null)
//						os.close();
//				}
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new Response<>(0, "生成Excel成功， excel链接为：");
	}




//	@RequestMapping(value = "/down_V2", method = RequestMethod.GET)
//	@ResponseBody
//	public Response<String> down_V2(@RequestParam(value = "id", required = false) Integer id,
//									@RequestParam(value = "keyword", required = false) String keyword,
//									@RequestParam(value = "startHandlingDate", required = false) String startHandlingDate,
//									@RequestParam(value = "endHandlingDate", required = false) String endHandlingDate,
//									@RequestParam(value = "commissionState", required = false) String commissionState,
//									@RequestParam(value = "startKjApprovalDate", required = false) String startKjApprovalDate,
//									@RequestParam(value = "endKjApprovalDate", required = false) String endKjApprovalDate,
//									@RequestParam(value = "startDate", required = false) String startDate,
//									@RequestParam(value = "endDate", required = false) String endDate,
//									@RequestParam(value = "startInvoiceCreate", required = false) String startInvoiceCreate,
//									@RequestParam(value = "endInvoiceCreate", required = false) String endInvoiceCreate,
//									@RequestParam(value = "regionId", required = false) Integer regionId,
//									@RequestParam(value = "adviserId", required = false) Integer adviserId,
//									@RequestParam(value = "userId", required = false) Integer userId,
//									@RequestParam(value = "userName", required = false) String userName,
//									@RequestParam(value = "applicantName", required = false) String applicantName,
//									@RequestParam(value = "state", required = false) String state, HttpServletRequest request,
//									HttpServletResponse response) {
//
//		// 更改当前顾问编号
//		Integer newAdviserId = getAdviserId(request);
//		if (newAdviserId != null)
//			adviserId = newAdviserId;
//
//		// 会计角色过滤状态
//		List<String> stateList = null;
//		if (getKjId(request) != null) {
//			stateList = new ArrayList<>();
//			stateList.add(ReviewKjStateEnum.REVIEW.toString());
//			stateList.add(ReviewKjStateEnum.FINISH.toString());
//			stateList.add(ReviewKjStateEnum.COMPLETE.toString());
////			stateList.add(ReviewKjStateEnum.CLOSE.toString());
//		}
//
//		List<String> commissionStateList = null;
//		if (StringUtil.isNotEmpty(commissionState))
//			commissionStateList = Arrays.asList(commissionState.split(","));
//
//		List<Integer> regionIdList = null;
//		if (regionId != null && regionId > 0)
//			regionIdList = ListUtil.buildArrayList(regionId);
//
//		try {
//
//			// 处理顾问管理员
//			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
//			if (adminUserLoginInfo != null && "GW".equalsIgnoreCase(adminUserLoginInfo.getApList())
//					&& adminUserLoginInfo.getRegionId() != null && adminUserLoginInfo.getRegionId() > 0) {
//				List<RegionDTO> regionList = regionService.listRegion(adminUserLoginInfo.getRegionId());
//				regionIdList = ListUtil.buildArrayList(adminUserLoginInfo.getRegionId());
//				for (RegionDTO region : regionList)
//					regionIdList.add(region.getId());
//				adviserId = null;
//			}
//
//			response.reset();// 清空输出流
//			String tableName = "visa_information";
//			response.setHeader("Content-disposition",
//					"attachment; filename=" + new String(tableName.getBytes("GB2312"), "8859_1") + ".xls");
//			response.setContentType("application/msexcel");
//
//			List<VisaDTO> list = visaService.listVisa(id, keyword, startHandlingDate, endHandlingDate, stateList,
//					commissionStateList, startKjApprovalDate, endKjApprovalDate, startDate, endDate, startInvoiceCreate,
//					endInvoiceCreate, regionIdList, adviserId, userId, userName, applicantName, state, 0, 9999, null);
//
//			int _regionId;
//			if (ObjectUtil.isNotNull(adviserId) && adviserId > 0)
//				_regionId = adviserService.getAdviserById(adviserId).getRegionId();
//			else {
//				_regionId = 0;
//			}
//			// 超级管理员导出佣金订单
////			if ("SUPERAD".equals(adminUserLoginInfo.getApList())) {
//			// 获取token
//			Map<String, Object> tokenMap = wxWorkService.getToken(WXWorkAPI.SECRET_EXCEL);
//			if ((int)tokenMap.get("errcode") != 0){
//				throw  new RuntimeException( tokenMap.get("errmsg").toString());
//			}
//			String customerToken = (String) tokenMap.get("access_token");
//
//			List<AdviserDTO> adviserDTOS = adviserService.listAdviser(null, null, 0, 1000);
//			Map<Integer, String> adviserMap = adviserDTOS.stream().collect(Collectors.toMap(AdviserDTO::getId, AdviserDTO::getName, (v1, v2) -> v2));
//			List<ServiceDTO> serviceDTOS = serviceService.listAllService(null, 0, 999);
//			Map<Integer, ServiceDTO> serviceMap = serviceDTOS.stream().collect(Collectors.toMap(ServiceDTO::getId, Function.identity()));
//
//
//			// 创建表格
//			String setupExcelAccessToken = WXWorkAPI.SETUP_EXCEL.replace("ACCESS_TOKEN", customerToken);
//			final JSONObject[] parm = {new JSONObject()};
//			parm[0].put("doc_type", 4);
//			parm[0].put("doc_name", "ServiceOrderTemplate-" + sdf.format(new Date()));
//			String[] userIds = {"XuShiYi"};
//			parm[0].put("admin_users", userIds);
//			JSONObject setupExcelJsonObject = WXWorkAPI.sendPostBody_Map(setupExcelAccessToken, parm[0]);
//			String url = "";
//			if ("0".equals(setupExcelJsonObject.get("errcode").toString())) {
//				url = setupExcelJsonObject.get("url").toString();
//				String docId = setupExcelJsonObject.get("docid").toString();
//				SetupExcelDO setupExcelDO = new SetupExcelDO();
//				setupExcelDO.setUrl(url);
//				setupExcelDO.setDocId(docId);
//				String informationExcelAccessToken = WXWorkAPI.INFORMATION_EXCEL.replace("ACCESS_TOKEN", customerToken);
//				parm[0] = new JSONObject();
//				parm[0].put("docid", docId);
//				JSONObject informationExcelJsonObject = WXWorkAPI.sendPostBody_Map(informationExcelAccessToken, parm[0]);
//				List<VisaDTO> finalServiceOrderList = list;
//				Thread thread1 = new Thread(() -> {
//					try {
//						// 线程1的任务
//						if ("0".equals(informationExcelJsonObject.get("errcode").toString())) {
//							JSONArray propertiesObjects = JSONArray.parseArray(JSONObject.toJSONString(informationExcelJsonObject.get("properties")));
//							Iterator<Object> iterator = propertiesObjects.iterator();
//							String sheetId = JSONObject.parseObject(iterator.next().toString()).get("sheet_id").toString();
//							setupExcelDO.setSheetId(sheetId);
//							int i = wxWorkService.addExcel(setupExcelDO);
//							if (i > 0) {
//								String redactExcelAccessToken = WXWorkAPI.REDACT_EXCEL.replace("ACCESS_TOKEN", customerToken);
//								parm[0] = new JSONObject();
//								parm[0].put("docid", docId);
//
//								List<JSONObject> requests = new ArrayList<>();
//								JSONObject requestsJson = new JSONObject();
//								JSONObject updateRangeRequest = new JSONObject();
//								JSONObject gridData = new JSONObject();
//								int count = 0;
//
//								List<String> excelTitle = new ArrayList<>();
//								excelTitle.add("订单ID");
//								excelTitle.add("佣金订单创建日期");
//								excelTitle.add("客户支付日期");
//								excelTitle.add("客户名称");
//								excelTitle.add("收款方式");
//								excelTitle.add("服务项目");
//								excelTitle.add("总计应收人民币");
//								excelTitle.add("总计应收澳币");
//								excelTitle.add("总计收款人民币");
//								excelTitle.add("总计收款澳币");
//								excelTitle.add("本次支付币种");
//								excelTitle.add("创建订单时汇率");
//								excelTitle.add("本次收款人民币");
//								excelTitle.add("本次收款澳币");
//								if ("SUPERAD".equals(adminUserLoginInfo.getApList()) || ("KJ".equals(adminUserLoginInfo.getApList()) && !regionService.isCN(_regionId))) {
//									excelTitle.add("GST");
//									excelTitle.add("Deduct GST");
//								}
//								excelTitle.add("预收业绩");
//								excelTitle.add("确认预售业绩");
//								excelTitle.add("月奖");
//								excelTitle.add("月奖支付时间");
//								excelTitle.add("银行对账字段");
//								if ("KJ".equals(adminUserLoginInfo.getApList())) {
//									excelTitle.add("是否自动对账");
//								}
//								excelTitle.add("顾问");
//								excelTitle.add("状态");
//								if ("KJ".equals(adminUserLoginInfo.getApList())) {
//									excelTitle.add("财务审核时间");
//								}
//								excelTitle.add("备注");
//
//								for (VisaDTO serviceOrderDTO : finalServiceOrderList) {
//									if (count == 0) {
//										gridData.put("start_row", 0);
//										gridData.put("start_column", 0);
//										List<JSONObject> rows = new ArrayList<>();
//										for (String title : excelTitle) {
//											JSONObject jsonObject = new JSONObject();
//											JSONObject text = new JSONObject();
//											text.put("text", title);
//											jsonObject.put("cell_value", text);
//											rows.add(jsonObject);
//										}
//										List<JSONObject> objects = new ArrayList<>();
//										JSONObject rowsValue = new JSONObject();
//										rowsValue.put("values", rows);
//										objects.add(rowsValue);
//										gridData.put("rows", objects);
//										updateRangeRequest.put("sheet_id", sheetId);
//										updateRangeRequest.put("grid_data", gridData);
//										requestsJson.put("update_range_request", updateRangeRequest);
//										requests.add(requestsJson);
//										parm[0].put("requests", requests);
//										count++;
//										WXWorkAPI.sendPostBody_Map(redactExcelAccessToken, parm[0]);
//										parm[0] = new JSONObject();
//										requests.remove(0);
//									}
//									parm[0].put("docid", docId);
//									gridData.put("start_row", count);
//									gridData.put("start_column", 0);
//									List<JSONObject> rows = build(serviceOrderDTO, adviserMap, serviceMap, adminUserLoginInfo, _regionId);
//									List<JSONObject> objects = new ArrayList<>();
//									JSONObject rowsValue = new JSONObject();
//									rowsValue.put("values", rows);
//									objects.add(rowsValue);
//									gridData.put("rows", objects);
//									updateRangeRequest.put("sheet_id", sheetId);
//									updateRangeRequest.put("grid_data", gridData);
//									requestsJson.put("update_range_request", updateRangeRequest);
//									requests.add(requestsJson);
//									parm[0].put("requests", requests);
//									count++;
//									WXWorkAPI.sendPostBody_Map(redactExcelAccessToken, parm[0]);
//									parm[0] = new JSONObject();
//									requests.remove(0);
//								}
//							}
//						}
//					} catch (Exception e) {
//						// 处理异常，例如记录日志
//						e.printStackTrace();
//					}
//				});
//				thread1.start();
//			}
//			StringBuilder htmlBuilder = new StringBuilder();
//			htmlBuilder.append("<a href=\"");
//			htmlBuilder.append(url + "\""); // 插入链接的URL
//			htmlBuilder.append(" target=\"_blank");
//			htmlBuilder.append("\">");
//			htmlBuilder.append("点击打开Excel链接"); // 插入链接的显示文本
//			htmlBuilder.append("</a>");
//			WXWorkAPI.sendShareLinkMsg(url, adminUserLoginInfo.getUsername(), "导出佣金订单信息");
//			return new Response<>(0, "生成Excel成功， excel链接为：" + htmlBuilder);
////			}
////			// 会计导出佣金订单
////			if (getKjId(request) != null) {
////				if (regionService.isCN(_regionId)) {
////					OutputStream os = response.getOutputStream();
////					jxl.Workbook wb;
////					InputStream is;
////					try {
////						is = this.getClass().getResourceAsStream("/VisaTemplateCNY.xls");
////					} catch (Exception e) {
////						throw new Exception("模版不存在");
////					}
////					try {
////						wb = Workbook.getWorkbook(is);
////					} catch (Exception e) {
////						throw new Exception("模版格式不支持");
////					}
////					WorkbookSettings settings = new WorkbookSettings();
////					settings.setWriteAccess(null);
////					jxl.write.WritableWorkbook wbe = Workbook.createWorkbook(os, wb, settings);
////
////					if (wbe == null) {
////						System.out.println("wbe is null !os=" + os + ",wb" + wb);
////					} else {
////						System.out.println("wbe not null !os=" + os + ",wb" + wb);
////					}
////					WritableSheet sheet = wbe.getSheet(0);
////					WritableCellFormat cellFormat = new WritableCellFormat();
////
////					int i = 1;
////					for (VisaDTO visaDto : list) {
////						sheet.addCell(new Label(0, i, "CV" + visaDto.getId(), cellFormat));
////						sheet.addCell(new Label(1, i, sdf.format(visaDto.getGmtCreate()), cellFormat));
////						if (visaDto.getReceiveDate() != null)
////							sheet.addCell(new Label(2, i, sdf.format(visaDto.getReceiveDate()), cellFormat));
////						sheet.addCell(new Label(3, i, visaDto.getUserName(), cellFormat));
////						sheet.addCell(new Label(4, i, visaDto.getReceiveTypeName(), cellFormat));
////						sheet.addCell(new Label(5, i, visaDto.getServiceCode(), cellFormat));
////						sheet.addCell(new Label(6, i, visaDto.getTotalAmountCNY() + "", cellFormat));
////						sheet.addCell(new Label(7, i, visaDto.getTotalAmountAUD() + "", cellFormat));
////						sheet.addCell(new Label(8, i, visaDto.getTotalAmountCNY() + "", cellFormat));
////						sheet.addCell(new Label(9, i, visaDto.getTotalAmountAUD() + "", cellFormat));
////						sheet.addCell(new Label(10, i, visaDto.getCurrency(), cellFormat));
////						sheet.addCell(new Label(11, i, visaDto.getExchangeRate() + "", cellFormat));
////						sheet.addCell(new Label(12, i, visaDto.getAmountCNY() + "", cellFormat));
////						sheet.addCell(new Label(13, i, visaDto.getAmountAUD() + "", cellFormat));
////						sheet.addCell(new Label(14, i, visaDto.getExpectAmountAUD() + "", cellFormat));
////						sheet.addCell(new Label(15, i, visaDto.getExpectAmountAUD() + "", cellFormat));
////						sheet.addCell(new Label(16, i, visaDto.getBonus() + "", cellFormat));
////						if (visaDto.getBonusDate() != null)
////							sheet.addCell(new Label(17, i, sdf.format(visaDto.getBonusDate()), cellFormat));
////						sheet.addCell(new Label(18, i, visaDto.getBankCheck(), cellFormat));
////						sheet.addCell(new Label(19, i, visaDto.isChecked() + "", cellFormat));
////						sheet.addCell(new Label(20, i, visaDto.getAdviserName(), cellFormat));
////						if (visaDto.getState() != null)
////							sheet.addCell(new Label(21, i, getStateStr(visaDto.getState()), cellFormat));
////						if (visaDto.getKjApprovalDate() != null)
////							sheet.addCell(new Label(22, i, sdf.format(visaDto.getKjApprovalDate()), cellFormat));
////						sheet.addCell(new Label(23, i, visaDto.getRemarks(), cellFormat));
////						i++;
////					}
////					wbe.write();
////					wbe.close();
////					if (is != null)
////						is.close();
////					if (os != null)
////						os.close();
////
////				} else {
////					//AUD
////					OutputStream os = response.getOutputStream();
////					jxl.Workbook wb;
////					InputStream is;
////					try {
////						is = this.getClass().getResourceAsStream("/VisaTemplate.xls");
////					} catch (Exception e) {
////						throw new Exception("模版不存在");
////					}
////					try {
////						wb = Workbook.getWorkbook(is);
////					} catch (Exception e) {
////						throw new Exception("模版格式不支持");
////					}
////					WorkbookSettings settings = new WorkbookSettings();
////					settings.setWriteAccess(null);
////					jxl.write.WritableWorkbook wbe = Workbook.createWorkbook(os, wb, settings);
////
////					if (wbe == null) {
////						System.out.println("wbe is null !os=" + os + ",wb" + wb);
////					} else {
////						System.out.println("wbe not null !os=" + os + ",wb" + wb);
////					}
////					WritableSheet sheet = wbe.getSheet(0);
////					WritableCellFormat cellFormat = new WritableCellFormat();
////
////					int i = 1;
////					for (VisaDTO visaDto : list) {
////						sheet.addCell(new Label(0, i, "CV" + visaDto.getId(), cellFormat));
////						sheet.addCell(new Label(1, i, sdf.format(visaDto.getGmtCreate()), cellFormat));
////						if (visaDto.getReceiveDate() != null)
////							sheet.addCell(new Label(2, i, sdf.format(visaDto.getReceiveDate()), cellFormat));
////						sheet.addCell(new Label(3, i, visaDto.getUserName(), cellFormat));
////						sheet.addCell(new Label(4, i, visaDto.getReceiveTypeName(), cellFormat));
////						sheet.addCell(new Label(5, i, visaDto.getServiceCode(), cellFormat));
////						sheet.addCell(new Label(6, i, visaDto.getTotalAmountCNY() + "", cellFormat));
////						sheet.addCell(new Label(7, i, visaDto.getTotalAmountAUD() + "", cellFormat));
////						sheet.addCell(new Label(8, i, visaDto.getTotalAmountCNY() + "", cellFormat));
////						sheet.addCell(new Label(9, i, visaDto.getTotalAmountAUD() + "", cellFormat));
////						sheet.addCell(new Label(10, i, visaDto.getCurrency(), cellFormat));
////						sheet.addCell(new Label(11, i, visaDto.getExchangeRate() + "", cellFormat));
////						sheet.addCell(new Label(12, i, visaDto.getAmountCNY() + "", cellFormat));
////						sheet.addCell(new Label(13, i, visaDto.getAmountAUD() + "", cellFormat));
////						sheet.addCell(new Label(14, i, visaDto.getGstAUD() + "", cellFormat));
////						sheet.addCell(new Label(15, i, visaDto.getDeductGstAUD() + "", cellFormat));
////						sheet.addCell(new Label(16, i, visaDto.getExpectAmountAUD() + "", cellFormat));
////						sheet.addCell(new Label(17, i, visaDto.getExpectAmountAUD() + "", cellFormat));
////						sheet.addCell(new Label(18, i, visaDto.getBonus() + "", cellFormat));
////						if (visaDto.getBonusDate() != null)
////							sheet.addCell(new Label(19, i, sdf.format(visaDto.getBonusDate()), cellFormat));
////						sheet.addCell(new Label(20, i, visaDto.getBankCheck(), cellFormat));
////						sheet.addCell(new Label(21, i, visaDto.isChecked() + "", cellFormat));
////						sheet.addCell(new Label(22, i, visaDto.getAdviserName(), cellFormat));
////						if (visaDto.getState() != null)
////							sheet.addCell(new Label(23, i, getStateStr(visaDto.getState()), cellFormat));
////						if (visaDto.getKjApprovalDate() != null)
////							sheet.addCell(new Label(24, i, sdf.format(visaDto.getKjApprovalDate()), cellFormat));
////						sheet.addCell(new Label(25, i, visaDto.getRemarks(), cellFormat));
////						i++;
////					}
////					wbe.write();
////					wbe.close();
////					if (is != null)
////						is.close();
////					if (os != null)
////						os.close();
////				}
////			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return new Response<>(0, "生成Excel成功， excel链接为：");
//	}


	@RequestMapping(value = "/get", method = RequestMethod.GET)
	@ResponseBody
	public Response<VisaDTO> getVisa(@RequestParam(value = "id") int id, HttpServletResponse response,HttpServletRequest request) {
		try {
			super.setGetHeader(response);
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			VisaDTO visaById = new VisaDTO();
			visaById = visaService.getVisaById(id);
			if ("GW".equalsIgnoreCase(adminUserLoginInfo.getApList())) {
				if (visaById.getAdviserId() != adminUserLoginInfo.getAdviserId()) {
					visaById.setCurrentAdvisor(false);
				}
			}
			return new Response<VisaDTO>(0, visaById);
		} catch (ServiceException e) {
			return new Response<VisaDTO>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/get2", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<VisaDTO>> getVisa2(@RequestParam(value = "idList") String idList[],
											HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			List<VisaDTO> visaList = new ArrayList<>();
			for (String id : idList) {
				VisaDTO visaDTO = visaService.getVisaById(StringUtil.toInt(id));
				if (visaDTO == null)
					return new Response<List<VisaDTO>>(1, "佣金订单不存在 id : " + id, null);
				visaList.add(visaDTO);
			}
			return new Response<List<VisaDTO>>(0, visaList);
		} catch (ServiceException e) {
			return new Response<List<VisaDTO>>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/close", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> closeVisa(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			VisaDTO visaDto = visaService.getVisaById(id);
			visaDto.setClose(true);
			visaDto.setInvoiceNumber(null); // close后把invoice清空
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
			LOG.info(StringUtil.merge("删除签证佣金订单:", visaService.getVisaById(id)));
			return new Response<Integer>(0, visaService.deleteVisaById(id));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/approval", method = RequestMethod.POST)
	@ResponseBody
	public Response<VisaDTO> approval(@RequestParam(value = "id") int id, @RequestParam(value = "state") String state,
									  HttpServletRequest request, HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			if (ReviewKjStateEnum.CLOSE.toString().equalsIgnoreCase(state))
				return new Response<VisaDTO>(1, "关闭操作请调用'refuse'接口.", null);
			// 审核
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo != null) {
				if (adminUserLoginInfo == null || (!"SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())
						&& !"KJ".equalsIgnoreCase(adminUserLoginInfo.getApList())))
					return new Response<VisaDTO>(1, "仅限会计审核佣金订单.", null);
				if ("SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())
						|| "KJ".equalsIgnoreCase(adminUserLoginInfo.getApList())) {
					if (ReviewKjStateEnum.get(state) != null) {
						VisaDTO visaDto = visaService.getVisaById(id);
						if (visaDto == null)
							return new Response<VisaDTO>(1, "佣金订单不存在!", null);
						serviceOrderService.approval(id, adminUserLoginInfo.getId(), null, null, null,
								state.toUpperCase());
						visaDto.setState(state);
						if (visaService.updateVisa(visaDto) > 0)
							return new Response<VisaDTO>(0, visaDto);
						else
							return new Response<VisaDTO>(1, "修改操作异常!", null);
					} else
						return new Response<VisaDTO>(1, "state错误!(" + state + ")", null);
				} else
					return new Response<VisaDTO>(1, "该用户无审核权限!", null);
			} else
				return new Response<VisaDTO>(1, "请登录!", null);
		} catch (ServiceException e) {
			return new Response<VisaDTO>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/refuse", method = RequestMethod.POST)
	@ResponseBody
	public Response<VisaDTO> refuse(@RequestParam(value = "id") int id, @RequestParam(value = "state") String state,
									@RequestParam(value = "refuseReason", required = false) String refuseReason, HttpServletRequest request,
									HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			if (ReviewKjStateEnum.COMPLETE.toString().equalsIgnoreCase(state)
					|| ReviewKjStateEnum.FINISH.toString().equalsIgnoreCase(state))
				return new Response<VisaDTO>(1, "完成操作请调用'approval'接口.", null);
			// 审核
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo != null) {
				if (adminUserLoginInfo == null || (!"SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())
						&& !"KJ".equalsIgnoreCase(adminUserLoginInfo.getApList())))
					return new Response<VisaDTO>(1, "仅限会计审核佣金订单.", null);
				if ("SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())
						|| "KJ".equalsIgnoreCase(adminUserLoginInfo.getApList())) {
					if (ReviewKjStateEnum.get(state) != null) {
						VisaDTO visaDto = visaService.getVisaById(id);
						if (visaDto == null)
							return new Response<VisaDTO>(1, "佣金订单不存在!", null);
						// 更新驳回原因
						if (StringUtil.isNotEmpty(refuseReason))
							visaDto.setRefuseReason(refuseReason);
						visaDto.setState(state);
						if (visaService.updateVisa(visaDto) > 0) {
//							ServiceOrderDTO serviceOrderDto = serviceOrderService.getServiceOrderById(visaDto.getServiceOrderId());
							//TODO:sulei 需要更新成上一笔已提交佣金订单的金额．．．
//							if (visaDto.getPerAmount() > 0)
//								serviceOrderDto.setPerAmount(serviceOrderDto.getPerAmount() - visaDto.getPerAmount());
//							if (visaDto.getAmount() > 0)
//								serviceOrderDto.setAmount(serviceOrderDto.getAmount() - visaDto.getAmount());
//							serviceOrderService.updateServiceOrder(serviceOrderDto);
							visaService.sendRefuseEmail(visaDto);
							return new Response<VisaDTO>(0, visaDto);
						} else
							return new Response<VisaDTO>(1, "修改操作异常!", null);
					} else
						return new Response<VisaDTO>(1, "state错误!(" + state + ")", null);
				} else
					return new Response<VisaDTO>(1, "该用户无审核权限!", null);
			} else
				return new Response<VisaDTO>(1, "请登录!", null);
		} catch (ServiceException e) {
			return new Response<VisaDTO>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/addComment", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> addComment(@RequestParam(value = "adminUserId", required = false) Integer adminUserId,
										@RequestParam(value = "visaId", required = false) Integer visaId,
										@RequestParam(value = "content") String content, HttpServletRequest request, HttpServletResponse response) {
		try {
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			super.setPostHeader(response);
			VisaCommentDTO visaCommentDto = new VisaCommentDTO();
			visaCommentDto.setAdminUserId(adminUserLoginInfo != null ? adminUserLoginInfo.getId() : adminUserId);
			visaCommentDto.setVisaId(visaId);
			visaCommentDto.setContent(content);
			if (visaService.addComment(visaCommentDto) > 0)
				return new Response<Integer>(0, visaCommentDto.getId());
			else
				return new Response<Integer>(1, "创建失败.", 0);
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/countComment", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> countComment(@RequestParam(value = "visaId") Integer visaId,
										  HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, visaService.listComment(visaId).size());
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/listComment", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<VisaCommentDTO>> listComment(@RequestParam(value = "visaId") Integer visaId,
													  HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<List<VisaCommentDTO>>(0, visaService.listComment(visaId));
		} catch (ServiceException e) {
			return new Response<List<VisaCommentDTO>>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/deleteComment", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> deleteComment(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, visaService.deleteComment(id));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), 0);
		}
	}

	public List<JSONObject> build(VisaDTO so, Map<Integer, String> adviserMap, Map<Integer,ServiceDTO> serviceMap, AdminUserLoginInfo adminUserLoginInfo, int _regionId) throws ServiceException {
		List<JSONObject> rows = new ArrayList<>();
		// 订单ID
		JSONObject jsonObject = new JSONObject();
		JSONObject text = new JSONObject();
		text.put("text", String.valueOf(so.getId()));
		jsonObject.put("cell_value", text);
		rows.add(jsonObject);
		// 佣金订单创建日期
		JSONObject jsonObject1 = new JSONObject();
		JSONObject text1 = new JSONObject();
		text1.put("text", sdf.format(so.getGmtCreate()));
		jsonObject1.put("cell_value", text1);
		rows.add(jsonObject1);
		// 客户支付日期
		JSONObject jsonObject2 = new JSONObject();
		JSONObject text2 = new JSONObject();
		if (so.getReceiveDate() != null) {
			text2.put("text", sdf.format(so.getReceiveDate()));
		} else {
			text2.put("text", "");
		}
		jsonObject2.put("cell_value", text2);
		rows.add(jsonObject2);
		// 客户名称
		JSONObject jsonObject3 = new JSONObject();
		JSONObject text3 = new JSONObject();
		text3.put("text", so.getUserName());
		jsonObject3.put("cell_value", text3);
		rows.add(jsonObject3);
		// 收款方式
		JSONObject jsonObject4 = new JSONObject();
		JSONObject text4 = new JSONObject();
		text4.put("text", so.getReceiveTypeName());
		jsonObject4.put("cell_value", text4);
		rows.add(jsonObject4);
		// 服务项目
		ServiceDTO serviceDTO = serviceMap.get(so.getServiceId());
		if (ObjectUtil.isNotNull(serviceDTO)) {
			JSONObject jsonObject5 = new JSONObject();
			JSONObject text5 = new JSONObject();
			text5.put("text", serviceDTO.getName() + "-" + serviceDTO.getCode());
			jsonObject5.put("cell_value", text5);
			rows.add(jsonObject5);
		}
		// 总计应收人民币
		JSONObject jsonObject6 = new JSONObject();
		JSONObject text6 = new JSONObject();
		text6.put("text", String.valueOf(so.getTotalAmountCNY()));
		jsonObject6.put("cell_value", text6);
		rows.add(jsonObject6);
		// 总计应收澳币
		JSONObject jsonObject7 = new JSONObject();
		JSONObject text7 = new JSONObject();
		text7.put("text", String.valueOf(so.getTotalAmountAUD()));
		jsonObject7.put("cell_value", text7);
		rows.add(jsonObject7);
		// 总计收款人民币
		JSONObject jsonObject8 = new JSONObject();
		JSONObject text8 = new JSONObject();
		text8.put("text", String.valueOf(so.getTotalAmountCNY()));
		jsonObject8.put("cell_value", text8);
		rows.add(jsonObject8);
		// 总计收款澳币
		JSONObject jsonObject9 = new JSONObject();
		JSONObject text9 = new JSONObject();
		text9.put("text", String.valueOf(so.getTotalAmountAUD()));
		jsonObject9.put("cell_value", text9);
		rows.add(jsonObject9);
		// 本次支付币种
		JSONObject jsonObject10 = new JSONObject();
		JSONObject text10 = new JSONObject();
		text10.put("text", so.getCurrency());
		jsonObject10.put("cell_value", text10);
		rows.add(jsonObject10);
		// 创建订单时汇率
		JSONObject jsonObject11 = new JSONObject();
		JSONObject text11 = new JSONObject();
		text11.put("text", String.valueOf(so.getExchangeRate()));
		jsonObject11.put("cell_value", text11);
		rows.add(jsonObject11);
		// 本次收款人民币
		JSONObject jsonObject12 = new JSONObject();
		JSONObject text12 = new JSONObject();
		text12.put("text", String.valueOf(so.getAmountCNY()));
		jsonObject12.put("cell_value", text12);
		rows.add(jsonObject12);
		// 本次收款澳币
		JSONObject jsonObject13 = new JSONObject();
		JSONObject text13 = new JSONObject();
		text13.put("text", String.valueOf(so.getAmountAUD()));
		jsonObject13.put("cell_value", text13);
		rows.add(jsonObject13);
		// GST
		if ("SUPERAD".equals(adminUserLoginInfo.getApList()) || ("KJ".equals(adminUserLoginInfo.getApList()) && !regionService.isCN(_regionId))) {
			JSONObject jsonObject14 = new JSONObject();
			JSONObject text14 = new JSONObject();
			text14.put("text", String.valueOf(so.getGst()));
			jsonObject14.put("cell_value", text14);
			rows.add(jsonObject14);
			// Deduct GST
			JSONObject jsonObject15 = new JSONObject();
			JSONObject text15 = new JSONObject();
			text15.put("text", String.valueOf(so.getDeductGst()));
			jsonObject15.put("cell_value", text15);
			rows.add(jsonObject15);
		}
		// 预收业绩
		JSONObject jsonObject16 = new JSONObject();
		JSONObject text16 = new JSONObject();
		text16.put("text", String.valueOf(so.getExpectAmount()));
		jsonObject16.put("cell_value", text16);
		rows.add(jsonObject16);
		// 确认预售业绩
		JSONObject jsonObject17 = new JSONObject();
		JSONObject text17 = new JSONObject();
		text17.put("text", String.valueOf(so.getSureExpectAmount()));
		jsonObject17.put("cell_value", text17);
		rows.add(jsonObject17);
		// 月奖
		JSONObject jsonObject18 = new JSONObject();
		JSONObject text18 = new JSONObject();
		text18.put("text", String.valueOf(so.getBonus()));
		jsonObject18.put("cell_value", text18);
		rows.add(jsonObject18);
		// 月奖支付时间
		if (ObjectUtil.isNotNull(so.getBonusDate())) {
			JSONObject jsonObject19 = new JSONObject();
			JSONObject text19 = new JSONObject();
			text19.put("text", sdf.format(so.getBonusDate()));
			jsonObject18.put("cell_value", text19);
			rows.add(jsonObject19);
		} else {
			JSONObject jsonObject19 = new JSONObject();
			JSONObject text19 = new JSONObject();
			text19.put("text", "");
			jsonObject19.put("cell_value", text19);
			rows.add(jsonObject19);
		}
		// 银行对账字段
		if (StringUtil.isNotEmpty(so.getVerifyCode())) {
			JSONObject jsonObject20 = new JSONObject();
			JSONObject text20 = new JSONObject();
			text20.put("text", so.getVerifyCode());
			jsonObject20.put("cell_value", text20);
			rows.add(jsonObject20);
		} else {
			JSONObject jsonObject20 = new JSONObject();
			JSONObject text20 = new JSONObject();
			text20.put("text", "");
			jsonObject20.put("cell_value", text20);
			rows.add(jsonObject20);
		}
		if ("KJ".equals(adminUserLoginInfo.getApList())) {
			JSONObject jsonObject21 = new JSONObject();
			JSONObject text21 = new JSONObject();
			text21.put("text", String.valueOf(so.isChecked()));
			jsonObject21.put("cell_value", text21);
			rows.add(jsonObject21);
		}
		// 顾问
		String adviserName = adviserMap.get(so.getAdviserId());
		if (StringUtil.isNotEmpty(adviserName)) {
			JSONObject jsonObject21 = new JSONObject();
			JSONObject text21 = new JSONObject();
			text21.put("text", adviserName);
			jsonObject21.put("cell_value", text21);
			rows.add(jsonObject21);
		} else {
			JSONObject jsonObject21 = new JSONObject();
			JSONObject text21 = new JSONObject();
			text21.put("text", "");
			jsonObject21.put("cell_value", text21);
			rows.add(jsonObject21);
		}
		// 状态
		JSONObject jsonObject22 = new JSONObject();
		JSONObject text22 = new JSONObject();
		text22.put("text", so.getState());
		jsonObject22.put("cell_value", text22);
		rows.add(jsonObject22);
		// 财务审核时间
		if ("KJ".equals(adminUserLoginInfo.getApList())) {
			JSONObject jsonObject21 = new JSONObject();
			JSONObject text21 = new JSONObject();
			text21.put("text", String.valueOf(so.getKjApprovalDate()));
			jsonObject21.put("cell_value", text21);
			rows.add(jsonObject21);
		}
		// 备注
		JSONObject jsonObject23 = new JSONObject();
		JSONObject text23 = new JSONObject();
		text23.put("text", so.getRemarks());
		jsonObject23.put("cell_value", text23);
		rows.add(jsonObject23);

		return rows;
	}

	private List<String> buildExlceTitle(String apList, int regionId) throws ServiceException {
		List<String> excelTitle = new ArrayList<>();
		excelTitle.add("备注");
		if ("KJ".equals(apList)) {
			excelTitle.add("财务审核时间");
		}
		excelTitle.add("状态");
		excelTitle.add("顾问");
		if ("KJ".equals(apList)) {
			excelTitle.add("是否自动对账");
		}
		excelTitle.add("银行对账字段");
		excelTitle.add("月奖支付时间");
		excelTitle.add("月奖");
		excelTitle.add("确认预售业绩");
		excelTitle.add("预收业绩");
		if ("SUPERAD".equals(apList) || ("KJ".equals(apList) && !regionService.isCN(regionId))) {
			excelTitle.add("Deduct GST");
			excelTitle.add("GST");
		}
		excelTitle.add("本次收款澳币");
		excelTitle.add("本次收款人民币");
		excelTitle.add("创建订单时汇率");
		excelTitle.add("本次支付币种");
		excelTitle.add("总计收款澳币");
		excelTitle.add("总计收款人民币");
		excelTitle.add("总计应收澳币");
		excelTitle.add("总计应收人民币");
		excelTitle.add("服务项目");
		excelTitle.add("收款方式");
		excelTitle.add("客户名称");
		excelTitle.add("客户支付日期");
		excelTitle.add("佣金订单创建日期");
		excelTitle.add("订单ID");
		return excelTitle;
	}

	private JSONObject buileExcelJsonObject(VisaDTO so, Map<Integer, String> adviserMap, Map<Integer,ServiceDTO> serviceMap, AdminUserLoginInfo adminUserLoginInfo, int _regionId) throws ServiceException {
		List<JSONObject> jsonObjectFILEDTITLEList = new ArrayList<>();
		JSONObject jsonObjectFILEDTITLE = new JSONObject();
		// 订单ID
		JSONObject jsonObject = new JSONObject();
		buildJsonobjectRow(String.valueOf(so.getId()), "订单ID", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		// 佣金订单创建日期
		buildJsonobjectRow(sdf.format(so.getGmtCreate()), "佣金订单创建日期", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		// 客户支付日期
		if (so.getReceiveDate() != null) {
			buildJsonobjectRow(sdf.format(so.getGmtCreate()), "客户支付日期", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		} else {
			buildJsonobjectRow("", "客户支付日期", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		}
		// 客户名称
		buildJsonobjectRow(so.getUserName(), "客户名称", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		// 收款方式
		buildJsonobjectRow(so.getReceiveTypeName(), "收款方式", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);

		// 服务项目
		ServiceDTO serviceDTO = serviceMap.get(so.getServiceId());
		buildJsonobjectRow(serviceDTO.getName() + "-" + serviceDTO.getCode(), "服务项目", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);

		// 总计应收人民币
//		buildJsonobjectRow(String.valueOf(so.getTotalAmountCNY()), "总计应收人民币", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		buildJsonobjectRowNumber(so.getTotalAmountCNY(), "总计应收人民币", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);

		// 总计应收澳币
//		buildJsonobjectRow(String.valueOf(so.getTotalAmountAUD()), "总计应收澳币", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		buildJsonobjectRowNumber(so.getTotalAmountAUD(), "总计应收澳币", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);

		// 总计收款人民币
//		buildJsonobjectRow(String.valueOf(so.getTotalAmountCNY()), "总计收款人民币", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		buildJsonobjectRowNumber(so.getTotalAmountCNY(), "总计收款人民币", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);

		// 总计收款澳币
//		buildJsonobjectRow(String.valueOf(so.getTotalAmountAUD()), "总计收款澳币", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		buildJsonobjectRowNumber(so.getTotalAmountAUD(), "总计收款澳币", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		// 本次支付币种
		buildJsonobjectRow(so.getCurrency(), "本次支付币种", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		// 创建订单时汇率
//		buildJsonobjectRow(String.valueOf(so.getExchangeRate()), "创建订单时汇率", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		buildJsonobjectRowNumber(so.getExchangeRate(), "创建订单时汇率", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		// 本次收款人民币
//		buildJsonobjectRow(String.valueOf(so.getAmountCNY()), "本次收款人民币", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		buildJsonobjectRowNumber(so.getAmountCNY(), "本次收款人民币", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		// 本次收款澳币
//		buildJsonobjectRow(String.valueOf(so.getAmountAUD()), "本次收款澳币", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		buildJsonobjectRowNumber(so.getAmountAUD(), "本次收款澳币", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		// GST
		if ("SUPERAD".equals(adminUserLoginInfo.getApList()) || ("KJ".equals(adminUserLoginInfo.getApList()) && !regionService.isCN(_regionId))) {
//			buildJsonobjectRow(String.valueOf(so.getGst()), "GST", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
			buildJsonobjectRowNumber(so.getGst(), "GST", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
			// Deduct GST
//			buildJsonobjectRow(String.valueOf(so.getDeductGst()), "Deduct GST", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
			buildJsonobjectRowNumber(so.getDeductGst(), "Deduct GST", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		}
		// 预收业绩
//		buildJsonobjectRow(String.valueOf(so.getExpectAmount()), "预收业绩", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		buildJsonobjectRowNumber(so.getExpectAmount(), "预收业绩", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		// 确认预售业绩
//		buildJsonobjectRow(String.valueOf(so.getSureExpectAmount()), "确认预售业绩", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		buildJsonobjectRowNumber(so.getSureExpectAmount(), "确认预售业绩", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		// 月奖
//		buildJsonobjectRow(String.valueOf(so.getBonus()), "月奖", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		buildJsonobjectRowNumber(so.getBonus(), "月奖", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		// 月奖支付时间
		if (ObjectUtil.isNotNull(so.getBonusDate())) {
			buildJsonobjectRow(sdf.format(so.getBonusDate()), "月奖支付时间", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		} else {
			buildJsonobjectRow("", "月奖支付时间", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		}
		// 银行对账字段
		if (StringUtil.isNotEmpty(so.getVerifyCode())) {
			buildJsonobjectRow(so.getVerifyCode(), "银行对账字段", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		} else {
			buildJsonobjectRow("", "银行对账字段", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		}
		if ("KJ".equals(adminUserLoginInfo.getApList())) {
			buildJsonobjectRow(String.valueOf(so.isChecked()), "是否自动对账", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		}
		// 顾问
		String adviserName = adviserMap.get(so.getAdviserId());
		if (StringUtil.isNotEmpty(adviserName)) {
			buildJsonobjectRow(adviserName, "顾问", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		} else {
			buildJsonobjectRow("", "顾问", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		}
		// 状态
		buildJsonobjectRow(so.getState(), "状态", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		// 财务审核时间
		if ("KJ".equals(adminUserLoginInfo.getApList())) {
			buildJsonobjectRow(String.valueOf(so.getKjApprovalDate()), "财务审核时间", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		}
		// 备注
		if (so.getRemarks() != null) {
			buildJsonobjectRow(so.getRemarks(), "备注", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		} else {
			buildJsonobjectRow("", "备注", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		}

		return jsonObjectFILEDTITLE;
	}

	public void buildJsonobjectRow(String value, String title, JSONObject jsonObject, List<JSONObject> jsonObjectFILEDTITLEList, JSONObject jsonObjectFILEDTITLE) {
		jsonObject = new JSONObject();
		jsonObject.put("type", "text");
		jsonObjectFILEDTITLEList = new ArrayList<>();
		jsonObject.put("text", value);
		jsonObjectFILEDTITLEList.add(jsonObject);
		jsonObjectFILEDTITLE.put(title, jsonObjectFILEDTITLEList);
	}

	public void buildJsonobjectRowNumber(Double value, String title, JSONObject jsonObject, List<JSONObject> jsonObjectFILEDTITLEList, JSONObject jsonObjectFILEDTITLE) {
		jsonObjectFILEDTITLE.put(title, value);
	}

}
