package org.zhinanzhen.b.controller;

import java.io.FileInputStream;
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

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.zhinanzhen.b.dao.pojo.CommissionInfoDO;
import org.zhinanzhen.b.service.*;
import org.zhinanzhen.b.service.pojo.*;
import org.zhinanzhen.b.service.pojo.ant.Sorter;
import org.zhinanzhen.tb.controller.ListResponse;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.RegionService;
import org.zhinanzhen.tb.service.ServiceException;

import com.alibaba.fastjson.JSON;
import com.ikasoa.core.utils.ListUtil;
import com.ikasoa.core.utils.StringUtil;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import org.zhinanzhen.tb.service.UserService;
import org.zhinanzhen.tb.service.pojo.RegionDTO;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/commissionOrder")
public class CommissionOrderController extends BaseCommissionOrderController {

	@Resource
	CommissionOrderService commissionOrderService;

	@Resource
	SubagencyService subagencyService;

	@Resource
	SchoolService schoolService;

	@Resource
	UserService userService;

	@Resource
	ServiceOrderService serviceOrderService;
	
	@Resource
	ApplicantService applicantService;

	@Resource
	RegionService regionService;

	@Resource
	SchoolInstitutionService schoolInstitutionService;

	@Resource
	MailRemindService mailRemindService;

	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@RequestMapping(value = "/upload_img", method = RequestMethod.POST)
	@ResponseBody
	public Response<String> uploadImage(@RequestParam MultipartFile file, HttpServletRequest request,
			HttpServletResponse response) throws IllegalStateException, IOException {
		super.setPostHeader(response);
		return super.upload2(file, request.getSession(), "/uploads/payment_voucher_image_url_c/");
	}

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	public Response<List<CommissionOrderDTO>> add(@RequestParam(value = "serviceOrderId") Integer serviceOrderId,
			@RequestParam(value = "state", required = false) String state,
			@RequestParam(value = "isSettle") Boolean isSettle,
			@RequestParam(value = "isDepositUser") Boolean isDepositUser,
			@RequestParam(value = "schoolId", required = false) Integer schoolId,
			@RequestParam(value = "courseId", required = false) Integer courseId,
			@RequestParam(value = "schoolInstitutionLocationId", required = false)Integer schoolInstitutionLocationId,
			@RequestParam(value = "studentCode") String studentCode,
			@RequestParam(value = "userId") Integer userId,
			@RequestParam(value = "applicantBirthday", required = false) String applicantBirthday,
			@RequestParam(value = "adviserId") Integer adviserId,
			@RequestParam(value = "officialId") Integer officialId,
			@RequestParam(value = "isStudying") Boolean isStudying,
			@RequestParam(value = "installment") Integer installment,
			@RequestParam(value = "installmentDueDate1") String installmentDueDate1,
			@RequestParam(value = "installmentDueDate2", required = false) String installmentDueDate2,
			@RequestParam(value = "installmentDueDate3", required = false) String installmentDueDate3,
			@RequestParam(value = "installmentDueDate4", required = false) String installmentDueDate4,
			@RequestParam(value = "installmentDueDate5", required = false) String installmentDueDate5,
			@RequestParam(value = "installmentDueDate6", required = false) String installmentDueDate6,
			@RequestParam(value = "installmentDueDate7", required = false) String installmentDueDate7,
			@RequestParam(value = "installmentDueDate8", required = false) String installmentDueDate8,
			@RequestParam(value = "installmentDueDate9", required = false) String installmentDueDate9,
			@RequestParam(value = "installmentDueDate10", required = false) String installmentDueDate10,
			@RequestParam(value = "installmentDueDate11", required = false) String installmentDueDate11,
			@RequestParam(value = "installmentDueDate12", required = false) String installmentDueDate12,
			@RequestParam(value = "paymentVoucherImageUrl1", required = false) String paymentVoucherImageUrl1,
			@RequestParam(value = "paymentVoucherImageUrl2", required = false) String paymentVoucherImageUrl2,
			@RequestParam(value = "paymentVoucherImageUrl3", required = false) String paymentVoucherImageUrl3,
			@RequestParam(value = "paymentVoucherImageUrl4", required = false) String paymentVoucherImageUrl4,
			@RequestParam(value = "paymentVoucherImageUrl5", required = false) String paymentVoucherImageUrl5,
			@RequestParam(value = "invoiceVoucherImageUrl1", required = false) String invoiceVoucherImageUrl1,
			@RequestParam(value = "invoiceVoucherImageUrl2", required = false) String invoiceVoucherImageUrl2,
			@RequestParam(value = "invoiceVoucherImageUrl3", required = false) String invoiceVoucherImageUrl3,
			@RequestParam(value = "invoiceVoucherImageUrl4", required = false) String invoiceVoucherImageUrl4,
			@RequestParam(value = "invoiceVoucherImageUrl5", required = false) String invoiceVoucherImageUrl5,
			@RequestParam(value = "dob") String dob, @RequestParam(value = "startDate") String startDate,
			@RequestParam(value = "endDate") String endDate, @RequestParam(value = "tuitionFee") String tuitionFee,
			@RequestParam(value = "perTermTuitionFee") String perTermTuitionFee,
			@RequestParam(value = "receiveTypeId") Integer receiveTypeId,
			@RequestParam(value = "receiveDate") String receiveDate,
			@RequestParam(value = "perAmount") String perAmount, @RequestParam(value = "amount") String amount,
			@RequestParam(value = "currency", required = false) String currency,
			@RequestParam(value = "exchangeRate", required = false) String exchangeRate,
			@RequestParam(value = "bonusDate", required = false) String bonusDate,
			@RequestParam(value = "zyDate", required = false) String zyDate,
			@RequestParam(value = "remarks", required = false) String remarks,
			@RequestParam(value = "verifyCode", required = false) String verifyCode, HttpServletRequest request,
			HttpServletResponse response) {

		try {
			super.setPostHeader(response);
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo == null || (!"SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())
					&& !"GW".equalsIgnoreCase(adminUserLoginInfo.getApList())))
				if (!"WA".equalsIgnoreCase(adminUserLoginInfo.getApList()) || !isSettle )
					return new Response<List<CommissionOrderDTO>>(1, "仅限顾问和超级管理员能创建佣金订单.", null);
			List<CommissionOrderDTO> commissionOrderDtoList = new ArrayList<>();
			ServiceOrderDTO serviceOrderDto = serviceOrderService.getServiceOrderById(serviceOrderId);
			if (serviceOrderDto == null)
				return new Response<List<CommissionOrderDTO>>(1, "服务订单(ID:" + serviceOrderId + ")不存在!", null);
			
			if (serviceOrderDto.getSubagencyId() <= 0)
				return new Response<List<CommissionOrderDTO>>(1,
						"SubagencyId(" + serviceOrderDto.getSubagencyId() + ")不存在!", null);
			CommissionOrderDTO commissionOrderDto = new CommissionOrderDTO();
			commissionOrderDto.setCode(UUID.randomUUID().toString());
			commissionOrderDto.setServiceOrderId(serviceOrderId);
			if (StringUtil.isNotEmpty(state))
				commissionOrderDto.setState(state);
			// else
			// commissionOrderDto.setState(ReviewKjStateEnum.PENDING.toString());
			if (isSettle)
				commissionOrderDto.setCommissionState(CommissionStateEnum.DJY.toString());
			else
				commissionOrderDto.setCommissionState(CommissionStateEnum.DZY.toString());
			commissionOrderDto.setSettle(isSettle);
			commissionOrderDto.setDepositUser(isDepositUser);
			commissionOrderDto.setSchoolId(schoolId == null ? 0 : schoolId);
			commissionOrderDto.setCourseId(courseId == null ? 0 : courseId);
			commissionOrderDto.setSchoolInstitutionLocationId(schoolInstitutionLocationId == null ? 0 : schoolInstitutionLocationId);
			commissionOrderDto.setStudentCode(studentCode);
			commissionOrderDto.setUserId(userId);
			commissionOrderDto.setAdviserId(adviserId);
			commissionOrderDto.setOfficialId(officialId);
			commissionOrderDto.setStudying(isStudying);
			commissionOrderDto.setInstallment(installment);
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl1))
				commissionOrderDto.setPaymentVoucherImageUrl1(paymentVoucherImageUrl1);
			else
				commissionOrderDto.setPaymentVoucherImageUrl1(serviceOrderDto.getPaymentVoucherImageUrl1()); // 许十一(2021-06-17)说非提前不需要引用服务订单的支付凭证!
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl2))
				commissionOrderDto.setPaymentVoucherImageUrl2(paymentVoucherImageUrl2);
			else
				commissionOrderDto.setPaymentVoucherImageUrl2(serviceOrderDto.getPaymentVoucherImageUrl2());
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl3))
				commissionOrderDto.setPaymentVoucherImageUrl3(paymentVoucherImageUrl3);
			else
				commissionOrderDto.setPaymentVoucherImageUrl3(serviceOrderDto.getPaymentVoucherImageUrl3());
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl4))
				commissionOrderDto.setPaymentVoucherImageUrl4(paymentVoucherImageUrl4);
			else
				commissionOrderDto.setPaymentVoucherImageUrl4(serviceOrderDto.getPaymentVoucherImageUrl4());
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl5))
				commissionOrderDto.setPaymentVoucherImageUrl5(paymentVoucherImageUrl5);
			else
				commissionOrderDto.setPaymentVoucherImageUrl5(serviceOrderDto.getPaymentVoucherImageUrl5());
			commissionOrderDto.setDob(new Date(Long.parseLong(dob)));
			commissionOrderDto.setStartDate(new Date(Long.parseLong(startDate)));
			commissionOrderDto.setEndDate(new Date(Long.parseLong(endDate)));
			commissionOrderDto.setTuitionFee(Double.parseDouble(tuitionFee));
			commissionOrderDto.setPerTermTuitionFee(Double.parseDouble(perTermTuitionFee));
			commissionOrderDto.setReceiveTypeId(receiveTypeId);
			commissionOrderDto.setReceiveDate(new Date(Long.parseLong(receiveDate)));
			commissionOrderDto.setPerAmount(Double.parseDouble(perAmount));
			commissionOrderDto.setAmount(Double.parseDouble(amount));
			if (commissionOrderDto.getPerAmount() < commissionOrderDto.getAmount())
				return new Response<List<CommissionOrderDTO>>(1, "本次应收款(" + commissionOrderDto.getPerAmount()
						+ ")不能小于本次已收款(" + commissionOrderDto.getAmount() + ")!", null);
			if (StringUtil.isNotEmpty(currency))
				commissionOrderDto.setCurrency(currency);
			if (StringUtil.isNotEmpty(exchangeRate))
				commissionOrderDto.setExchangeRate(Double.parseDouble(exchangeRate));
			commissionOrderDto.setDiscount(commissionOrderDto.getPerAmount() - commissionOrderDto.getAmount());
			if (StringUtil.isNotEmpty(bonusDate))
				commissionOrderDto.setBonusDate(new Date(Long.parseLong(bonusDate)));
			if (StringUtil.isNotEmpty(zyDate))
				commissionOrderDto.setZyDate(new Date(Long.parseLong(zyDate)));
			if (StringUtil.isNotEmpty(remarks))
				commissionOrderDto.setRemarks(remarks);

			//if (serviceOrderDto.isSettle() == true && (
			//		StringUtil.isNotEmpty(invoiceVoucherImageUrl1) || StringUtil.isNotEmpty(invoiceVoucherImageUrl2)
			//		|| StringUtil.isNotEmpty(invoiceVoucherImageUrl3) || StringUtil.isNotEmpty(invoiceVoucherImageUrl4)
			//		|| StringUtil.isNotEmpty(invoiceVoucherImageUrl5)) ){
				/*
				杜大哥（2021-10-18）说：【提前扣拥】在上传【invoice凭证】之后，申请月奖直接将服务订单状态改成【RECEIVED】(已收款凭证已提交)
				 */
			//	serviceOrderDto.setState("RECEIVED");
			//}
			//if (StringUtil.isNotEmpty(invoiceVoucherImageUrl1))
			//	serviceOrderDto.setInvoiceVoucherImageUrl1(invoiceVoucherImageUrl1);
			//if (StringUtil.isNotEmpty(invoiceVoucherImageUrl2))
			//	serviceOrderDto.setInvoiceVoucherImageUrl2(invoiceVoucherImageUrl2);
			//if (StringUtil.isNotEmpty(invoiceVoucherImageUrl3))
			//	serviceOrderDto.setInvoiceVoucherImageUrl3(invoiceVoucherImageUrl3);
			//if (StringUtil.isNotEmpty(invoiceVoucherImageUrl4))
			//	serviceOrderDto.setInvoiceVoucherImageUrl4(invoiceVoucherImageUrl4);
			//if (StringUtil.isNotEmpty(invoiceVoucherImageUrl5))
			//	serviceOrderDto.setInvoiceVoucherImageUrl5(invoiceVoucherImageUrl5);


			
			// SubagencyDTO subagencyDto =
			// subagencyService.getSubagencyById(serviceOrderDto.getSubagencyId());
			// if (subagencyDto == null)
			// return new Response<List<CommissionOrderDTO>>(1,
			// "Subagency(" + serviceOrderDto.getSubagencyId() + ")不存在!", null);
			// // 佣金
			// commissionOrderDto.setCommission(commissionOrderDto.getAmount());
			// // 预收业绩
			// Double expectAmount = commissionOrderDto.getAmount() *
			// subagencyDto.getCommissionRate() * 1.1;
			// commissionOrderDto.setExpectAmount(expectAmount);
			// // GST
			// commissionOrderDto
			// .setGst(new BigDecimal(expectAmount / 11).setScale(2,
			// BigDecimal.ROUND_HALF_UP).doubleValue());
			// // Deduct GST
			// commissionOrderDto.setDeductGst(new BigDecimal(expectAmount -
			// commissionOrderDto.getGst())
			// .setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
			// // Bonus
			// commissionOrderDto.setBonus(new
			// BigDecimal(commissionOrderDto.getDeductGst() * 0.1)
			// .setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());

			String msg = "";
			for (int installmentNum = 1; installmentNum <= installment; installmentNum++) {
				commissionOrderDto.setInstallmentNum(installmentNum);
				if (installmentNum == 1 && installmentDueDate1 != null) {
					commissionOrderDto.setInstallmentDueDate(new Date(Long.parseLong(installmentDueDate1)));
					commissionOrderDto.setState(ReviewKjStateEnum.REVIEW.toString()); // 第一笔单子直接进入财务审核状态
					if (StringUtil.isNotEmpty(verifyCode))
						commissionOrderDto.setVerifyCode(verifyCode.replace("$", "").replace("#", "").replace(" ", ""));
					commissionOrderDto.setKjApprovalDate(new Date());
					//  提前扣拥会将创建的invoice发票凭证的invoiceNo暂存到佣金临时表中，创建佣金的时候写入第一笔单子
					if (isSettle){
						CommissionOrderTempDTO comTemp = commissionOrderService.getCommissionOrderTempByServiceOrderId(serviceOrderId);
						if (comTemp != null){
							commissionOrderDto.setInvoiceNumber(comTemp.getInvoiceNumber());
						}
					}
				} else {
					if (installmentNum == 2 && installmentDueDate2 != null) {
						commissionOrderDto.setInstallmentDueDate(new Date(Long.parseLong(installmentDueDate2)));
					} else if (installmentNum == 3 && installmentDueDate3 != null) {
						commissionOrderDto.setInstallmentDueDate(new Date(Long.parseLong(installmentDueDate3)));
					} else if (installmentNum == 4 && installmentDueDate4 != null) {
						commissionOrderDto.setInstallmentDueDate(new Date(Long.parseLong(installmentDueDate4)));
					} else if (installmentNum == 5 && installmentDueDate5 != null) {
						commissionOrderDto.setInstallmentDueDate(new Date(Long.parseLong(installmentDueDate5)));
					} else if (installmentNum == 6 && installmentDueDate6 != null) {
						commissionOrderDto.setInstallmentDueDate(new Date(Long.parseLong(installmentDueDate6)));
					} else if (installmentNum == 7 && installmentDueDate7 != null) {
						commissionOrderDto.setInstallmentDueDate(new Date(Long.parseLong(installmentDueDate7)));
					} else if (installmentNum == 8 && installmentDueDate8 != null) {
						commissionOrderDto.setInstallmentDueDate(new Date(Long.parseLong(installmentDueDate8)));
					} else if (installmentNum == 9 && installmentDueDate9 != null) {
						commissionOrderDto.setInstallmentDueDate(new Date(Long.parseLong(installmentDueDate9)));
					} else if (installmentNum == 10 && installmentDueDate10 != null) {
						commissionOrderDto.setInstallmentDueDate(new Date(Long.parseLong(installmentDueDate10)));
					} else if (installmentNum == 11 && installmentDueDate11 != null) {
						commissionOrderDto.setInstallmentDueDate(new Date(Long.parseLong(installmentDueDate11)));
					} else if (installmentNum == 12 && installmentDueDate12 != null) {
						commissionOrderDto.setInstallmentDueDate(new Date(Long.parseLong(installmentDueDate12)));
					} else
						break;
					commissionOrderDto.setState(ReviewKjStateEnum.PENDING.toString());
					commissionOrderDto.setVerifyCode(null);
					commissionOrderDto.setKjApprovalDate(null);
					commissionOrderDto.setReceiveDate(null);
					commissionOrderDto.setPaymentVoucherImageUrl1(null);
					commissionOrderDto.setPaymentVoucherImageUrl2(null);
					commissionOrderDto.setPaymentVoucherImageUrl3(null);
					commissionOrderDto.setPaymentVoucherImageUrl4(null);
					commissionOrderDto.setPaymentVoucherImageUrl5(null);
					commissionOrderDto.setInvoiceNumber(null);
				}
				int id = commissionOrderService.addCommissionOrder(commissionOrderDto);
				if (id > 0) {
					serviceOrderDto.setSubmitted(true);
					userService.updateDOB(new Date(Long.parseLong(dob)), userId);
					serviceOrderService.updateServiceOrder(serviceOrderDto); // 同时更改服务订单状态
					ApplicantDTO applicantDto = serviceOrderDto.getApplicant();
					if (applicantDto != null && applicantBirthday != null) {
						applicantDto.setBirthday(new Date(Long.parseLong(applicantBirthday)));
						if (applicantService.update(applicantDto) <= 0)
							msg += "申请人生日修改失败! (serviceOrderId:" + serviceOrderDto.getId() + ", applicantId:"
									+ applicantDto.getId() + ", applicantBirthday:" + applicantDto.getBirthday() + ");";
						else
							msg += "申请人生日修改成功. (serviceOrderId:" + serviceOrderDto.getId() + ", applicantId:"
									+ applicantDto.getId() + ", applicantBirthday:" + applicantDto.getBirthday() + ");";
					}
					commissionOrderDtoList.add(commissionOrderDto);
					CommissionOrderListDTO commissionOrderListDto = commissionOrderService.getCommissionOrderById(id);
					if (isSettle){

					}
					int i = 0;
					if (commissionOrderListDto.getSchoolId() > 0)
						i = schoolService.updateSchoolSetting(commissionOrderListDto); // 根据学校设置更新佣金值
					else
						i = schoolInstitutionService.updateSchoolSetting(commissionOrderListDto);
					if (i > 0) {
					} else if (i == -1)
						msg += id + "计算失败. (佣金记录不存在);";
					else if (i == -2)
						msg += id + "计算失败. (学校佣金设置不存在或不正确);";
					else if (i == -3)
						msg += id + "计算失败. (佣金办理时间不在设置合同时间范围内);";
					else
						msg += id + "计算失败. ;";
				} else
					msg += "佣金订单创建失败. (" + commissionOrderDto.toString() + ");";
			}
			return new Response<List<CommissionOrderDTO>>(0, msg, commissionOrderDtoList);
		} catch (ServiceException e) {
			return new Response<List<CommissionOrderDTO>>(e.getCode(), e.getMessage(), null);
		}
	}

	// 顾问不能修改,财务能修改
	@RequestMapping(value = "/update", method = RequestMethod.POST)
	@ResponseBody
	public Response<CommissionOrderDTO> update(@RequestParam(value = "id") int id,
			@RequestParam(value = "state", required = false) String state,
			@RequestParam(value = "commissionState", required = false) String commissionState,
			@RequestParam(value = "isSettle", required = false) Boolean isSettle,
			@RequestParam(value = "isDepositUser", required = false) Boolean isDepositUser,
			@RequestParam(value = "schoolId", required = false) Integer schoolId,
			@RequestParam(value = "studentCode", required = false) String studentCode,
			@RequestParam(value = "adviserId", required = false) Integer adviserId,
			@RequestParam(value = "officialId", required = false) Integer officialId,
			@RequestParam(value = "isStudying", required = false) Boolean isStudying,
			@RequestParam(value = "installmentDueDate", required = false) String installmentDueDate,
			@RequestParam(value = "paymentVoucherImageUrl1", required = false) String paymentVoucherImageUrl1,
			@RequestParam(value = "paymentVoucherImageUrl2", required = false) String paymentVoucherImageUrl2,
			@RequestParam(value = "paymentVoucherImageUrl3", required = false) String paymentVoucherImageUrl3,
			@RequestParam(value = "paymentVoucherImageUrl4", required = false) String paymentVoucherImageUrl4,
			@RequestParam(value = "paymentVoucherImageUrl5", required = false) String paymentVoucherImageUrl5,
			@RequestParam(value = "dob", required = false) String dob,
			@RequestParam(value = "startDate", required = false) String startDate,
			@RequestParam(value = "endDate", required = false) String endDate,
			@RequestParam(value = "tuitionFee", required = false) String tuitionFee,
			@RequestParam(value = "perTermTuitionFee", required = false) String perTermTuitionFee,
			@RequestParam(value = "receiveTypeId", required = false) Integer receiveTypeId,
			@RequestParam(value = "receiveDate", required = false) String receiveDate,
			@RequestParam(value = "perAmount", required = false) String perAmount,
			@RequestParam(value = "amount", required = false) String amount,
			@RequestParam(value = "sureExpectAmount", required = false) Double sureExpectAmount,
			@RequestParam(value = "currency", required = false) String currency,
			@RequestParam(value = "exchangeRate", required = false) String exchangeRate,
			@RequestParam(value = "invoiceNumber", required = false) String invoiceNumber,
			@RequestParam(value = "zyDate", required = false) String zyDate,
			@RequestParam(value = "bankCheck", required = false) String bankCheck,
			@RequestParam(value = "isChecked", required = false) String isChecked,
			@RequestParam(value = "remarks", required = false) String remarks,
			@RequestParam(value = "verifyCode", required = false) String verifyCode,
			@RequestParam(value = "applicantBirthday", required = false) String applicantBirthday,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			CommissionOrderListDTO commissionOrderListDto = commissionOrderService.getCommissionOrderById(id);
			if (commissionOrderListDto == null)
				return new Response<CommissionOrderDTO>(1, "留学佣金订单订单(ID:" + id + ")不存在!", null);
			ServiceOrderDTO serviceOrderDto = serviceOrderService
					.getServiceOrderById(commissionOrderListDto.getServiceOrderId());
			if (serviceOrderDto == null)
				return new Response<CommissionOrderDTO>(1,
						"服务订单(ID:" + commissionOrderListDto.getServiceOrderId() + ")不存在!", null);
			if (serviceOrderDto.getSubagencyId() <= 0)
				return new Response<CommissionOrderDTO>(1, "SubagencyId(" + serviceOrderDto.getSubagencyId() + ")不存在!",
						null);
			CommissionOrderDTO commissionOrderDto = commissionOrderService.getCommissionOrderById(id);
			if (commissionOrderDto == null)
				return new Response<CommissionOrderDTO>(1, "佣金订单不存在,修改失败.", null);
			if (adminUserLoginInfo != null && "KJ".equalsIgnoreCase(adminUserLoginInfo.getApList())
					&& commissionState != null) { // 只有会计能修改佣金状态
				commissionOrderDto
						.setCommissionState(CommissionStateEnum.get(commissionState.toUpperCase()).toString());
				if (CommissionStateEnum.YJY.toString().equalsIgnoreCase(commissionState))
					state = ReviewKjStateEnum.COMPLETE.toString(); // 如果会计修改状态为已结佣，那么佣金订单状态需改成COMPLETE
			}
			if (StringUtil.isNotEmpty(state))
				commissionOrderDto.setState(state);
			else
				commissionOrderDto.setState(ReviewKjStateEnum.REVIEW.toString()); // 修改后重新审核
			if (isSettle != null)
				commissionOrderDto.setSettle(isSettle);
			if (isDepositUser != null)
				commissionOrderDto.setDepositUser(isDepositUser);
			if (schoolId != null)
				commissionOrderDto.setSchoolId(schoolId);
			if (StringUtil.isNotEmpty(studentCode))
				commissionOrderDto.setStudentCode(studentCode);
			if (adviserId != null)
				commissionOrderDto.setAdviserId(adviserId);
			if (officialId != null)
				commissionOrderDto.setOfficialId(officialId);
			if (isStudying != null)
				commissionOrderDto.setStudying(isStudying);
			if (installmentDueDate != null)
				commissionOrderDto.setInstallmentDueDate(new Date(Long.parseLong(installmentDueDate)));
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl1)) {
				commissionOrderDto.setPaymentVoucherImageUrl1(paymentVoucherImageUrl1);
				serviceOrderDto.setPaymentVoucherImageUrl1(paymentVoucherImageUrl1);
			}
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl2)) {
				commissionOrderDto.setPaymentVoucherImageUrl2(paymentVoucherImageUrl2);
				serviceOrderDto.setPaymentVoucherImageUrl2(paymentVoucherImageUrl2);
			}
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl3)) {
				commissionOrderDto.setPaymentVoucherImageUrl3(paymentVoucherImageUrl3);
				serviceOrderDto.setPaymentVoucherImageUrl3(paymentVoucherImageUrl3);
			}
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl4)) {
				commissionOrderDto.setPaymentVoucherImageUrl4(paymentVoucherImageUrl4);
				serviceOrderDto.setPaymentVoucherImageUrl4(paymentVoucherImageUrl4);
			}
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl5)) {
				commissionOrderDto.setPaymentVoucherImageUrl5(paymentVoucherImageUrl5);
				serviceOrderDto.setPaymentVoucherImageUrl5(paymentVoucherImageUrl5);
			}
			if (dob != null)
				commissionOrderDto.setDob(new Date(Long.parseLong(dob)));
			if (startDate != null)
				commissionOrderDto.setStartDate(new Date(Long.parseLong(startDate)));
			if (endDate != null)
				commissionOrderDto.setEndDate(new Date(Long.parseLong(endDate)));
			if (StringUtil.isNotEmpty(tuitionFee))
				commissionOrderDto.setTuitionFee(Double.parseDouble(tuitionFee));
			if (StringUtil.isNotEmpty(perTermTuitionFee))
				commissionOrderDto.setPerTermTuitionFee(Double.parseDouble(perTermTuitionFee));
			if (receiveTypeId != null) {
				commissionOrderDto.setReceiveTypeId(receiveTypeId);
				serviceOrderDto.setReceiveTypeId(receiveTypeId);
			}
			if (StringUtil.isNotEmpty(receiveDate)) {
				commissionOrderDto.setReceiveDate(new Date(Long.parseLong(receiveDate)));
				serviceOrderDto.setReceiveDate(new Date(Long.parseLong(receiveDate)));
			}
			if (StringUtil.isNotEmpty(perAmount)) {
				commissionOrderDto.setPerAmount(Double.parseDouble(perAmount));
				serviceOrderDto.setPerAmount(Double.parseDouble(perAmount));
			}
			if (StringUtil.isNotEmpty(amount)) {
				commissionOrderDto.setAmount(Double.parseDouble(amount));
				serviceOrderDto.setAmount(Double.parseDouble(amount));
			}
			double _perAmount = commissionOrderListDto.getPerAmount();
			if (commissionOrderDto.getPerAmount() > 0)
				_perAmount = commissionOrderDto.getPerAmount();
			if (_perAmount < commissionOrderDto.getAmount())
				return new Response<CommissionOrderDTO>(1,
						"本次应收款(" + _perAmount + ")不能小于本次已收款(" + commissionOrderDto.getAmount() + ")!", null);
			commissionOrderDto.setDiscount(_perAmount - commissionOrderDto.getAmount());
			if (sureExpectAmount != null)
				commissionOrderDto.setSureExpectAmount(sureExpectAmount);
			if (StringUtil.isNotEmpty(currency))
				commissionOrderDto.setCurrency(currency);
			if (StringUtil.isNotEmpty(exchangeRate))
				commissionOrderDto.setExchangeRate(Double.parseDouble(exchangeRate));
			Double rate = getRate();
			if (rate != null && rate > 0)
				commissionOrderDto.setExchangeRate(rate);
			if (StringUtil.isNotEmpty(invoiceNumber))
				commissionOrderDto.setInvoiceNumber(invoiceNumber);
			if (StringUtil.isNotEmpty(zyDate))
				commissionOrderDto.setZyDate(new Date(Long.parseLong(zyDate)));
			if (StringUtil.isNotEmpty(bankCheck))
				commissionOrderDto.setBankCheck(bankCheck);
			commissionOrderDto.setChecked(isChecked != null && "true".equalsIgnoreCase(isChecked));
			if (StringUtil.isNotEmpty(remarks))
				commissionOrderDto.setRemarks(remarks);
			if (StringUtil.isNotEmpty(verifyCode))
				commissionOrderDto.setVerifyCode(verifyCode.replace("$", "").replace("#", "").replace(" ", ""));
			if (commissionOrderDto.getKjApprovalDate() == null || commissionOrderDto.getKjApprovalDate().getTime() == 0)
				commissionOrderDto.setKjApprovalDate(new Date());

			// SubagencyDTO subagencyDto =
			// subagencyService.getSubagencyById(serviceOrderDto.getSubagencyId());
			// if (subagencyDto == null)
			// return new Response<CommissionOrderDTO>(1, "Subagency(" +
			// serviceOrderDto.getSubagencyId() + ")不存在!",
			// null);
			// // 佣金
			// commissionOrderDto.setCommission(commissionOrderDto.getAmount());
			// // 预收业绩
			// Double expectAmount = commissionOrderDto.getAmount() *
			// subagencyDto.getCommissionRate() * 1.1;
			// commissionOrderDto.setExpectAmount(expectAmount);
			// // GST
			// commissionOrderDto
			// .setGst(new BigDecimal(expectAmount / 11).setScale(2,
			// BigDecimal.ROUND_HALF_UP).doubleValue());
			// // Deduct GST
			// commissionOrderDto.setDeductGst(new BigDecimal(expectAmount -
			// commissionOrderDto.getGst())
			// .setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
			// Bonus
			// commissionOrderDto.setBonus(new
			// BigDecimal(commissionOrderDto.getDeductGst() * 0.1)
			// .setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());

			String msg = "";
			if (commissionOrderService.updateCommissionOrder(commissionOrderDto) > 0) {
				CommissionOrderListDTO _commissionOrderListDto = commissionOrderService
						.getCommissionOrderById(commissionOrderDto.getId());
				serviceOrderDto.setReceivable(_commissionOrderListDto.getTotalPerAmount());
				serviceOrderDto.setReceived(_commissionOrderListDto.getTotalAmount());
				serviceOrderService.updateServiceOrder(serviceOrderDto); // 同步修改服务订单
				ApplicantDTO applicantDto = serviceOrderDto.getApplicant();
				if (StringUtil.isEmpty(applicantBirthday))
					applicantBirthday = dob;
				if (applicantDto != null && applicantBirthday != null) {
					applicantDto.setBirthday(new Date(Long.parseLong(applicantBirthday)));
					if (applicantService.update(applicantDto) <= 0)
						msg += "申请人生日修改失败! (serviceOrderId:" + serviceOrderDto.getId() + ", applicantId:"
								+ applicantDto.getId() + ", applicantBirthday:" + applicantDto.getBirthday() + ");";
					else
						msg += "申请人生日修改成功. (serviceOrderId:" + serviceOrderDto.getId() + ", applicantId:"
								+ applicantDto.getId() + ", applicantBirthday:" + applicantDto.getBirthday() + ");";
				}
				userService.updateDOB(new Date(Long.parseLong(dob)), commissionOrderListDto.getUserId());
				int i = 0;
				if (commissionOrderListDto.getCourseId() == 0)
					i = schoolService.updateSchoolSetting(commissionOrderListDto); // 根据学校设置更新佣金值
				else
					i = schoolInstitutionService.updateSchoolSetting(_commissionOrderListDto);
				if (i > 0) {
				} else if (i == -1)
					msg += id + "计算失败. (佣金记录不存在);";
				else if (i == -2)
					msg += id + "计算失败. (学校佣金设置不存在或不正确);";
				else if (i == -3)
					msg += id + "计算失败. (佣金办理时间不在设置合同时间范围内);";
				else
					msg += id + "计算失败. ;";
				return new Response<CommissionOrderDTO>(0, msg, commissionOrderDto);
			} else
				return new Response<CommissionOrderDTO>(1, "修改失败.", null);
		} catch (ServiceException e) {
			return new Response<CommissionOrderDTO>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/kjUpdate", method = RequestMethod.POST)
	@ResponseBody
	public Response<CommissionOrderDTO> kjUpdate(@RequestParam(value = "id") int id,
			@RequestParam(value = "schoolPaymentAmount", required = false) Double schoolPaymentAmount,
			@RequestParam(value = "schoolPaymentDate", required = false) String schoolPaymentDate,
			@RequestParam(value = "invoiceNumber", required = false) String invoiceNumber,
			@RequestParam(value = "zyDate", required = false) String zyDate,
			@RequestParam(value = "sureExpectAmount", required = false) Double sureExpectAmount,
			@RequestParam(value = "bonus", required = false) Double bonus,
			@RequestParam(value = "bonusDate", required = false) String bonusDate, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo != null) {
				if (adminUserLoginInfo == null || (!"SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())
						&& !"KJ".equalsIgnoreCase(adminUserLoginInfo.getApList())))
					return new Response<CommissionOrderDTO>(1, "仅限会计修改.", null);
				return updateOne(id, schoolPaymentAmount, schoolPaymentDate, invoiceNumber, zyDate, sureExpectAmount,
						bonus, bonusDate, true);
			} else
				return new Response<CommissionOrderDTO>(1, "修改失败.", null);
		} catch (ServiceException e) {
			return new Response<CommissionOrderDTO>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/updateKjApprovalDate", method = RequestMethod.POST)
	@ResponseBody
	public Response<CommissionOrderDTO> updateKjApprovalDate(@RequestParam(value = "id") int id,
			@RequestParam(value = "kjApprovalDate") String kjApprovalDate, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			CommissionOrderDTO commissionOrderDto = commissionOrderService.getCommissionOrderById(id);
			if (commissionOrderDto == null)
				return new Response<CommissionOrderDTO>(1, "佣金订单不存在,修改失败.", null);
			if (adminUserLoginInfo != null && ("KJ".equalsIgnoreCase(adminUserLoginInfo.getApList())
					|| "SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())))
				commissionOrderDto.setKjApprovalDate(new Date(Long.parseLong(kjApprovalDate)));
			else
				return new Response<CommissionOrderDTO>(1, "只有会计和超级管理员能修改会计审核时间.", null);
			if (commissionOrderService.updateCommissionOrder(commissionOrderDto) > 0)
				return new Response<CommissionOrderDTO>(0, "", commissionOrderDto);
			else
				return new Response<CommissionOrderDTO>(1, "修改失败.", null);
		} catch (ServiceException e) {
			return new Response<CommissionOrderDTO>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/update", method = RequestMethod.PUT)
	@ResponseBody
	public Response<Integer> update(@RequestBody List<BatchUpdateCommissionOrder> batchUpdateList,
			HttpServletRequest request, HttpServletResponse response) {
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
	public Response<Integer> kjUpdate(@RequestBody List<BatchUpdateCommissionOrder> batchUpdateList,
			HttpServletRequest request, HttpServletResponse response) {
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

	private Response<Integer> batchUpdate(List<BatchUpdateCommissionOrder> batchUpdateList, boolean isChangeState)
			throws ServiceException {
		int x = 0;
		String msg = "";
		for (BatchUpdateCommissionOrder batchUpdate : batchUpdateList) {
			Response<CommissionOrderDTO> _response = updateOne(batchUpdate.getId(),
					batchUpdate.getSchoolPaymentAmount(), batchUpdate.getSchoolPaymentDate(),
					batchUpdate.getInvoiceNumber(), batchUpdate.getZyDate(), batchUpdate.getSureExpectAmount(),
					batchUpdate.getBonus(), batchUpdate.getBonusDate(), isChangeState);
			if (_response.getCode() == 0)
				x++;
			else
				msg += _response.getMessage();
		}
		return new Response<Integer>(0, msg, x);
	}

	private Response<CommissionOrderDTO> updateOne(int id, Double schoolPaymentAmount, String schoolPaymentDate,
			String invoiceNumber, String zyDate, Double sureExpectAmount, Double bonus, String bonusDate,
			boolean isChangeState) throws ServiceException {
		CommissionOrderDTO commissionOrderDto = commissionOrderService.getCommissionOrderById(id);
		if (commissionOrderDto == null)
			return new Response<CommissionOrderDTO>(1, "留学佣金订单订单(ID:" + id + ")不存在!", null);
		if (schoolPaymentAmount != null)
			commissionOrderDto.setSchoolPaymentAmount(schoolPaymentAmount);
		if (schoolPaymentDate != null)
			commissionOrderDto.setSchoolPaymentDate(new Date(Long.parseLong(schoolPaymentDate)));
		if (StringUtil.isNotEmpty(invoiceNumber))
			commissionOrderDto.setInvoiceNumber(invoiceNumber);
		if (StringUtil.isNotEmpty(zyDate))
			commissionOrderDto.setZyDate(new Date(Long.parseLong(zyDate)));
		if (sureExpectAmount != null)
			commissionOrderDto.setSureExpectAmount(sureExpectAmount);
		if (bonus != null)
			commissionOrderDto.setBonus(bonus);
		if (bonusDate != null)
			commissionOrderDto.setBonusDate(new Date(Long.parseLong(bonusDate)));
		if (isChangeState && schoolPaymentDate != null && schoolPaymentAmount != null
				&& StringUtil.isNotEmpty(invoiceNumber))
			if (bonus != null || bonusDate != null) {
				commissionOrderDto.setState(ReviewKjStateEnum.COMPLETE.toString());
				commissionOrderDto.setCommissionState(CommissionStateEnum.YJY.toString());
			} else {
				commissionOrderDto.setState(ReviewKjStateEnum.REVIEW.toString());
				commissionOrderDto.setCommissionState(CommissionStateEnum.YZY.toString());
			}
//		return commissionOrderService.updateCommissionOrder(commissionOrderDto) > 0
//				? new Response<CommissionOrderDTO>(0, commissionOrderDto)
//				: new Response<CommissionOrderDTO>(1, "修改失败.", null);

		String msg = "";
		if (commissionOrderService.updateCommissionOrder(commissionOrderDto) > 0) {
			if (sureExpectAmount != null && sureExpectAmount > 0) {
				CommissionOrderListDTO _commissionOrderListDto = commissionOrderService
						.getCommissionOrderById(commissionOrderDto.getId());
				int i = schoolService.updateSchoolSetting(_commissionOrderListDto); // 根据学校设置更新佣金值
				if (i > 0) {
				} else if (i == -1)
					msg += id + "计算失败. (佣金记录不存在);";
				else if (i == -2)
					msg += id + "计算失败. (学校佣金设置不存在或不正确);";
				else if (i == -3)
					msg += id + "计算失败. (佣金办理时间不在设置合同时间范围内);";
				else
					msg += id + "计算失败. ;";
			}
			return new Response<CommissionOrderDTO>(0, msg, commissionOrderDto);
		} else
			return new Response<CommissionOrderDTO>(1, "修改失败.", null);
	}

	@RequestMapping(value = "/updateCommission", method = RequestMethod.POST)
	@ResponseBody
	public Response<CommissionOrderDTO> updateCommission(@RequestParam(value = "id") int id, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			CommissionOrderListDTO commissionOrderListDto = commissionOrderService.getCommissionOrderById(id);
			int i = schoolInstitutionService.updateSchoolSetting(commissionOrderListDto); // 根据学校设置更新佣金值
			if (i > 0)
				return new Response<CommissionOrderDTO>(0, "计算成功.", commissionOrderListDto);
			else if (i == -1)
				return new Response<CommissionOrderDTO>(1, "计算失败. (佣金记录不存在)", null);
			else if (i == -2)
				return new Response<CommissionOrderDTO>(2, "计算失败. (学校佣金设置不存在或不正确)", null);
			else if (i == -3)
				return new Response<CommissionOrderDTO>(3, "计算失败. (佣金办理时间不在设置合同时间范围内)", null);
			else
				return new Response<CommissionOrderDTO>(4, "计算失败.", null);
		} catch (ServiceException e) {
			return new Response<CommissionOrderDTO>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/close", method = RequestMethod.POST)
	@ResponseBody
	public Response<CommissionOrderDTO> close(@RequestParam(value = "id") int id,
			@RequestParam(value = "isStudying", required = false) Boolean isStudying,
			@RequestParam(value = "remarks", required = false) String remarks, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			CommissionOrderDTO commissionOrderDto = commissionOrderService.getCommissionOrderById(id);
			if (commissionOrderDto == null)
				return new Response<CommissionOrderDTO>(1, "佣金订单不存在,修改失败.", null);
			commissionOrderDto.setState(ReviewKjStateEnum.CLOSE.toString());
			commissionOrderDto.setClose(true);
			commissionOrderDto.setInvoiceNumber(null); // close后把invoice清空
			if (isStudying != null)
				commissionOrderDto.setStudying(isStudying);
			if (StringUtil.isNotEmpty(remarks))
				commissionOrderDto.setRemarks(remarks);
			return commissionOrderService.updateCommissionOrder(commissionOrderDto) > 0
					? new Response<CommissionOrderDTO>(0, commissionOrderDto)
					: new Response<CommissionOrderDTO>(1, "修改失败.", null);
		} catch (ServiceException e) {
			return new Response<CommissionOrderDTO>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/count", method = RequestMethod.GET)
	@ResponseBody
	@Deprecated
	public Response<Integer> count(@RequestParam(value = "id", required = false) Integer id,
			@RequestParam(value = "regionId", required = false) Integer regionId,
			@RequestParam(value = "maraId", required = false) Integer maraId,
			@RequestParam(value = "adviserId", required = false) Integer adviserId,
			@RequestParam(value = "officialId", required = false) Integer officialId,
			@RequestParam(value = "userId", required = false) Integer userId,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "applicantName", required = false) String applicantName,
			@RequestParam(value = "phone", required = false) String phone,
			@RequestParam(value = "wechatUsername", required = false) String wechatUsername,
			@RequestParam(value = "schoolId", required = false) Integer schoolId,
			@RequestParam(value = "isSettle", required = false) Boolean isSettle,
			@RequestParam(value = "state", required = false) String state,
			@RequestParam(value = "applyState", required = false) String applyState,
			@RequestParam(value = "commissionState", required = false) String commissionState,
			@RequestParam(value = "startKjApprovalDate", required = false) String startKjApprovalDate,
			@RequestParam(value = "endKjApprovalDate", required = false) String endKjApprovalDate,
			@RequestParam(value = "startDate", required = false) String startDate,
			@RequestParam(value = "endDate", required = false) String endDate,
			@RequestParam(value = "startInvoiceCreate", required = false) String startInvoiceCreate,
			@RequestParam(value = "endInvoiceCreate", required = false) String endInvoiceCreate,
			HttpServletRequest request, HttpServletResponse response) {

		Integer newMaraId = getMaraId(request);
		if (newMaraId != null)
			maraId = newMaraId;
		Integer newOfficialId = getOfficialId(request);
		if (newOfficialId != null)
			officialId = newOfficialId;

		List<String> commissionStateList = null;
		if (StringUtil.isNotEmpty(commissionState))
			commissionStateList = Arrays.asList(commissionState.split(","));

		List<Integer> regionIdList = null;
		if (regionId != null && regionId > 0)
			regionIdList = ListUtil.buildArrayList(regionId);

		// 会计角色过滤状态
		Boolean isYzyAndYjy = false;
		List<String> stateList = new ArrayList<>();
		if (state == null && getKjId(request) != null) {
			stateList.add(ReviewKjStateEnum.REVIEW.toString());
			stateList.add(ReviewKjStateEnum.FINISH.toString());
			stateList.add(ReviewKjStateEnum.COMPLETE.toString());
			stateList.add(ReviewKjStateEnum.CLOSE.toString());
			if (CommissionStateEnum.YZY.toString().equalsIgnoreCase(commissionState)) {
				commissionStateList = null;
				isYzyAndYjy = true;
			}
		} else if (state == null)
			stateList = null;
		else
			stateList.add(state);

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
				Integer newAdviserId = getAdviserId(request);
				if (newAdviserId != null)
					adviserId = newAdviserId;
			}

			return new Response<Integer>(0,
					commissionOrderService.countCommissionOrder(id, regionIdList, maraId, adviserId, officialId, userId,
							name, applicantName, phone, wechatUsername, schoolId, isSettle, stateList, commissionStateList,
							startKjApprovalDate, endKjApprovalDate,startDate,endDate, startInvoiceCreate, endInvoiceCreate, isYzyAndYjy,
							applyState));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public ListResponse<List<CommissionOrderListDTO>> list(@RequestParam(value = "id", required = false) Integer id,
			@RequestParam(value = "regionId", required = false) Integer regionId,
			@RequestParam(value = "maraId", required = false) Integer maraId,
			@RequestParam(value = "adviserId", required = false) Integer adviserId,
			@RequestParam(value = "officialId", required = false) Integer officialId,
			@RequestParam(value = "userId", required = false) Integer userId,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "userName", required = false) String userName,
			@RequestParam(value = "applicantName", required = false) String applicantName,
			@RequestParam(value = "phone", required = false) String phone,
			@RequestParam(value = "wechatUsername", required = false) String wechatUsername,
			@RequestParam(value = "schoolId", required = false) Integer schoolId,
			@RequestParam(value = "isSettle", required = false) Boolean isSettle,
			@RequestParam(value = "state", required = false) String state,
			@RequestParam(value = "applyState", required = false) String applyState,
			@RequestParam(value = "commissionState", required = false) String commissionState,
			@RequestParam(value = "startKjApprovalDate", required = false) String startKjApprovalDate,
			@RequestParam(value = "endKjApprovalDate", required = false) String endKjApprovalDate,
			@RequestParam(value = "startDate", required = false) String startDate,
			@RequestParam(value = "endDate", required = false) String endDate,
			@RequestParam(value = "startInvoiceCreate", required = false) String startInvoiceCreate,
			@RequestParam(value = "endInvoiceCreate", required = false) String endInvoiceCreate,
			@RequestParam(value = "pageNum") int pageNum, @RequestParam(value = "pageSize") int pageSize,
			@RequestParam(value = "sorter", required = false) String sorter, HttpServletRequest request,
			HttpServletResponse response) {

		Integer newMaraId = getMaraId(request);
		if (newMaraId != null)
			maraId = newMaraId;
		Integer newOfficialId = getOfficialId(request);
		if (newOfficialId != null)
			officialId = newOfficialId;

		List<String> commissionStateList = null;
		if (StringUtil.isNotEmpty(commissionState))
			commissionStateList = Arrays.asList(commissionState.split(","));

		// 会计角色过滤状态
		Boolean isYzyAndYjy = false;
		List<String> stateList = new ArrayList<>();
		if (state == null && getKjId(request) != null) {
			stateList.add(ReviewKjStateEnum.REVIEW.toString());
			stateList.add(ReviewKjStateEnum.FINISH.toString());
			stateList.add(ReviewKjStateEnum.COMPLETE.toString());
			stateList.add(ReviewKjStateEnum.CLOSE.toString());
			if (CommissionStateEnum.YZY.toString().equalsIgnoreCase(commissionState)) {
				commissionStateList = null;
				isYzyAndYjy = true;
			}
		} else if (state == null)
			stateList = null;
		else
			stateList.add(state);

		List<Integer> regionIdList = null;
		if (regionId != null && regionId > 0)
			regionIdList = ListUtil.buildArrayList(regionId);

		Sorter _sorter = null;
		if (sorter != null)
			_sorter = JSON.parseObject(sorter.replace("adviser,name", "adviserName"), Sorter.class);

		if (StringUtil.isNotEmpty(userName))
			name = userName;

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
				Integer newAdviserId = getAdviserId(request);
				if (newAdviserId != null)
					adviserId = newAdviserId;
				if (adminUserLoginInfo == null)
					return new ListResponse<List<CommissionOrderListDTO>>(false, pageSize, 0, null, "No permission !");
				if ("GW".equalsIgnoreCase(adminUserLoginInfo.getApList()) && adviserId == null)
					return new ListResponse<List<CommissionOrderListDTO>>(false, pageSize, 0, null,
							"无法获取顾问编号，请退出重新登录后再尝试．");
			}

			int total = commissionOrderService.countCommissionOrder(id, regionIdList, maraId, adviserId, officialId,
					userId, name, applicantName, phone, wechatUsername, schoolId, isSettle, stateList,
					commissionStateList, startKjApprovalDate, endKjApprovalDate, startDate, endDate, startInvoiceCreate,
					endInvoiceCreate, isYzyAndYjy, applyState);
			List<CommissionOrderListDTO> list = commissionOrderService.listCommissionOrder(id, regionIdList, maraId,
					adviserId, officialId, userId, name, applicantName, phone, wechatUsername, schoolId, isSettle,
					stateList, commissionStateList, startKjApprovalDate, endKjApprovalDate, startDate, endDate,
					startInvoiceCreate, endInvoiceCreate, isYzyAndYjy, applyState, pageNum, pageSize, _sorter);
			list.forEach(co -> {
				if (co.getServiceOrderId() > 0)
					try {
						ServiceOrderDTO serviceOrderDto = serviceOrderService
								.getServiceOrderById(co.getServiceOrderId());
						if (serviceOrderDto != null) {
							ApplicantDTO applicantDto = co.getApplicant();
							if (applicantDto != null) {
								if (StringUtil.isEmpty(applicantDto.getUrl()))
									applicantDto.setUrl(serviceOrderDto.getNutCloud());
								if (StringUtil.isEmpty(applicantDto.getContent()))
									applicantDto.setContent(serviceOrderDto.getInformation());
								co.setApplicant(applicantDto);
							}
						}
					} catch (ServiceException e) {
					}
				try {
					List<MailRemindDTO> mailRemindDTOS = mailRemindService.list(getAdviserId(request),newOfficialId,getKjId(request),
							null,null,co.getId(),null,false,true);
					co.setMailRemindDTOS(mailRemindDTOS);
				} catch (ServiceException se) {
					se.printStackTrace();
				}
			});
			return new ListResponse<List<CommissionOrderListDTO>>(true, pageSize, total, list, "");
		} catch (ServiceException e) {
			return new ListResponse<List<CommissionOrderListDTO>>(false, pageSize, 0, null, e.getMessage());
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
			Workbook wb = Workbook.getWorkbook(is);
			Sheet sheet = wb.getSheet(0);
			for (int i = 1; i < sheet.getRows(); i++) {
				Cell[] cells = sheet.getRow(i);
				if (cells == null || cells.length == 0)
					continue;
				String _id = cells[0].getContents();
				String _schoolPaymentAmount = cells[27].getContents();
				String _schoolPaymentDate = cells[28].getContents();
				String _invoiceNumber = cells[29].getContents();
				String _zyDate = cells[30].getContents();
				String _sureExpectAmount = cells[24].getContents();
				String _bonus = cells[32].getContents();
				String _bonusDate = cells[33].getContents();
				try {
					CommissionOrderListDTO commissionOrderListDto = commissionOrderService
							.getCommissionOrderById(Integer.parseInt(_id));
					if (commissionOrderListDto == null) {
						message += "[" + _id + "]佣金订单不存在;";
						continue;
					}
					if (!CommissionStateEnum.DJY.toString()
							.equalsIgnoreCase(commissionOrderListDto.getCommissionState())
							&& !CommissionStateEnum.DZY.toString()
									.equalsIgnoreCase(commissionOrderListDto.getCommissionState())
							&& !CommissionStateEnum.YZY.toString()
									.equalsIgnoreCase(commissionOrderListDto.getCommissionState())) {
						message += "[" + _id + "]只有状态为待结佣,待追佣,已追佣允许更新佣金订单;";
						continue;
					}
					Response<CommissionOrderDTO> _r = updateOne(Integer.parseInt(_id),
							StringUtil.isEmpty(_schoolPaymentAmount) ? null
									: Double.parseDouble(_schoolPaymentAmount.trim()),
							StringUtil.isEmpty(_schoolPaymentDate) ? null
									: simpleDateFormat.parse(_schoolPaymentDate.trim()).getTime() + "",
							_invoiceNumber,
							StringUtil.isEmpty(_zyDate) ? null : simpleDateFormat.parse(_zyDate.trim()).getTime() + "",
							StringUtil.isEmpty(_sureExpectAmount) ? null : Double.parseDouble(_sureExpectAmount.trim()),
							StringUtil.isEmpty(_bonus) ? null : Double.parseDouble(_bonus.trim()),
							StringUtil.isEmpty(_bonusDate) ? null
									: simpleDateFormat.parse(_bonusDate.trim()).getTime() + "",
							true);
					if (_r.getCode() > 0)
						message += "[" + _id + "]" + _r.getMessage() + ";";
					else
						n++;
				} catch (NumberFormatException | ServiceException | ParseException e) {
					message += "[" + _id + "]" + e.getMessage() + ";";
				}
			}
		} catch (BiffException | IOException e) {
			return new Response<Integer>(1, "上传失败:" + e.getMessage(), 0);
		}
		return new Response<Integer>(0, message, n);
	}

	@RequestMapping(value = "/down", method = RequestMethod.GET)
	@ResponseBody
	public void down(@RequestParam(value = "id", required = false) Integer id,
			@RequestParam(value = "regionId", required = false) Integer regionId,
			@RequestParam(value = "maraId", required = false) Integer maraId,
			@RequestParam(value = "adviserId", required = false) Integer adviserId,
			@RequestParam(value = "officialId", required = false) Integer officialId,
			@RequestParam(value = "userId", required = false) Integer userId,
			@RequestParam(value = "userName", required = false) String name,
			@RequestParam(value = "applicantName", required = false) String applicantName,
			@RequestParam(value = "phone", required = false) String phone,
			@RequestParam(value = "wechatUsername", required = false) String wechatUsername,
			@RequestParam(value = "schoolId", required = false) Integer schoolId,
			@RequestParam(value = "isSettle", required = false) Boolean isSettle,
			@RequestParam(value = "state", required = false) String state,
			@RequestParam(value = "commissionState", required = false) String commissionState,
			@RequestParam(value = "startKjApprovalDate", required = false) String startKjApprovalDate,
			@RequestParam(value = "endKjApprovalDate", required = false) String endKjApprovalDate,
			@RequestParam(value = "startDate", required = false) String startDate,
			@RequestParam(value = "endDate", required = false) String endDate,
			@RequestParam(value = "startInvoiceCreate", required = false) String startInvoiceCreate,
			@RequestParam(value = "endInvoiceCreate", required = false) String endInvoiceCreate,
			HttpServletRequest request, HttpServletResponse response) {

		Integer newMaraId = getMaraId(request);
		if (newMaraId != null)
			maraId = newMaraId;
		Integer newAdviserId = getAdviserId(request);
		if (newAdviserId != null)
			adviserId = newAdviserId;
		Integer newOfficialId = getOfficialId(request);
		if (newOfficialId != null)
			officialId = newOfficialId;

		List<String> commissionStateList = null;
		if (StringUtil.isNotEmpty(commissionState))
			commissionStateList = Arrays.asList(commissionState.split(","));

		// 会计角色过滤状态
		Boolean isYzyAndYjy = false;
		List<String> stateList = new ArrayList<>();
		if (state == null && getKjId(request) != null) {
			stateList.add(ReviewKjStateEnum.REVIEW.toString());
			stateList.add(ReviewKjStateEnum.FINISH.toString());
			stateList.add(ReviewKjStateEnum.COMPLETE.toString());
			stateList.add(ReviewKjStateEnum.CLOSE.toString());
			if (CommissionStateEnum.YZY.toString().equalsIgnoreCase(commissionState)) {
				commissionStateList = null;
				isYzyAndYjy = true;
			}
		} else if (state == null)
			stateList = null;
		else
			stateList.add(state);

		List<Integer> regionIdList = null;
		if (regionId != null && regionId > 0)
			regionIdList = ListUtil.buildArrayList(regionId);

		InputStream is = null;
		OutputStream os = null;
		Workbook wb = null;
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
			String tableName = "commission_order_information";
			response.setHeader("Content-disposition",
					"attachment; filename=" + new String(tableName.getBytes("GB2312"), "8859_1") + ".xls");
			response.setContentType("application/msexcel");

//			Date _startKjApprovalDate = null;
//			if (startKjApprovalDate != null)
//				_startKjApprovalDate = new Date(Long.parseLong(startKjApprovalDate));
//			Date _endKjApprovalDate = null;
//			if (endKjApprovalDate != null)
//				_endKjApprovalDate = new Date(Long.parseLong(endKjApprovalDate));

			List<CommissionOrderListDTO> commissionOrderList = commissionOrderService.listCommissionOrder(id,
					regionIdList, maraId, adviserId, officialId, userId, name, applicantName, phone, wechatUsername, schoolId,
					isSettle, stateList, commissionStateList, startKjApprovalDate, endKjApprovalDate,startDate,endDate,
					startInvoiceCreate, endInvoiceCreate, isYzyAndYjy, state, 0, 9999, null);

			if (isCN(regionId)) {
				
				os = response.getOutputStream();
				try {
					is = this.getClass().getResourceAsStream("/CommissionOrderTemplateCNY.xls");
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
				for (CommissionOrderListDTO commissionOrderListDto : commissionOrderList) {
					sheet.addCell(new Label(0, i, "CS" + commissionOrderListDto.getId(), cellFormat));
					sheet.addCell(new Label(1, i, sdf.format(commissionOrderListDto.getGmtCreate()), cellFormat));
					if (commissionOrderListDto.getReceiveDate() != null)
						sheet.addCell(new Label(2, i, sdf.format(commissionOrderListDto.getReceiveDate()), cellFormat));
					if (commissionOrderListDto.getUser() != null)
						sheet.addCell(new Label(3, i, commissionOrderListDto.getUser().getName(), cellFormat));
					sheet.addCell(new Label(4, i, commissionOrderListDto.getStudentCode(), cellFormat));
					if (commissionOrderListDto.getBirthday() != null)
						sheet.addCell(new Label(5, i, sdf.format(commissionOrderListDto.getBirthday()), cellFormat));
					if (commissionOrderListDto.getReceiveType() != null)
						sheet.addCell(new Label(6, i, commissionOrderListDto.getReceiveType().getName() + "", cellFormat));
					if (commissionOrderListDto.getService() != null)
						sheet.addCell(new Label(7, i, commissionOrderListDto.getService().getName(), cellFormat));
					sheet.addCell(new Label(8, i, commissionOrderListDto.isSettle() + "", cellFormat));
					if (commissionOrderListDto.getSchool() != null) {
						sheet.addCell(new Label(9, i, commissionOrderListDto.getSchool().getName() + "", cellFormat));
						sheet.addCell(new Label(13, i, commissionOrderListDto.getSchool().getSubject() + "", cellFormat));
					}
					if (commissionOrderListDto.getSchoolInstitutionListDTO() != null){
						sheet.addCell(new Label(9, i, commissionOrderListDto.getSchoolInstitutionListDTO().getInstitutionTradingName() , cellFormat));
						//if (commissionOrderListDto.getSchoolInstitutionListDTO().getSchoolInstitutionLocationDO() != null)
						sheet.addCell(new Label(10, i, commissionOrderListDto.getSchoolInstitutionListDTO().getInstitutionName(), cellFormat));
						if (commissionOrderListDto.getSchoolInstitutionListDTO().getSchoolInstitutionLocationDO() != null){
							sheet.addCell(new Label(11, i, commissionOrderListDto.getSchoolInstitutionListDTO().getSchoolInstitutionLocationDO().getName(), cellFormat));
							sheet.addCell(new Label(12, i, commissionOrderListDto.getSchoolInstitutionListDTO().getSchoolInstitutionLocationDO().getState(), cellFormat));
						}
						if (commissionOrderListDto.getSchoolInstitutionListDTO().getSchoolCourseDO() != null)
							sheet.addCell(new Label(13, i, commissionOrderListDto.getSchoolInstitutionListDTO().getSchoolCourseDO().getCourseName(), cellFormat));
					}
					if (commissionOrderListDto.getStartDate() != null)
						sheet.addCell(new Label(14, i, sdf.format(commissionOrderListDto.getStartDate()), cellFormat));
					if (commissionOrderListDto.getEndDate() != null)
						sheet.addCell(new Label(15, i, sdf.format(commissionOrderListDto.getEndDate()), cellFormat));
					if (commissionOrderListDto.getInstallmentDueDate() != null)
						sheet.addCell(
								new Label(16, i, sdf.format(commissionOrderListDto.getInstallmentDueDate()), cellFormat));
					if (commissionOrderListDto.getReceiveType() != null)
						sheet.addCell(new Label(17, i, commissionOrderListDto.getReceiveType().getName() + "", cellFormat));
					sheet.addCell(new Label(18, i, commissionOrderListDto.getTuitionFee() + "", cellFormat));
					sheet.addCell(new Label(19, i, commissionOrderListDto.getPerAmount() + "", cellFormat)); // .getPerTermTuitionFee()
					sheet.addCell(new Label(20, i, commissionOrderListDto.getTotalPerAmountAUD() + "", cellFormat));
					sheet.addCell(new Label(21, i, commissionOrderListDto.getTotalAmountAUD() + "", cellFormat));
					sheet.addCell(new Label(22, i, commissionOrderListDto.getCurrency(), cellFormat));
					sheet.addCell(new Label(23, i, commissionOrderListDto.getExchangeRate() + "", cellFormat));
					sheet.addCell(new Label(24, i, commissionOrderListDto.getAmountAUD() + "", cellFormat));
					sheet.addCell(new Label(25, i, commissionOrderListDto.getExpectAmount() + "", cellFormat));
					if (commissionOrderListDto.isSettle())
						sheet.addCell(new Label(26, i, commissionOrderListDto.getExpectAmount() + "", cellFormat));
					else
						sheet.addCell(new Label(26, i, commissionOrderListDto.getSureExpectAmount() + "", cellFormat));
					sheet.addCell(new Label(27, i, commissionOrderListDto.getSchoolPaymentAmount() + "", cellFormat));
					//28待确定
					if (commissionOrderListDto.getSchoolPaymentDate() != null) 
						sheet.addCell(
								new Label(29, i, sdf.format(commissionOrderListDto.getSchoolPaymentDate()), cellFormat));
					sheet.addCell(new Label(30, i, commissionOrderListDto.getInvoiceNumber(), cellFormat));
					if (commissionOrderListDto.getZyDate() != null)
						sheet.addCell(new Label(31, i, sdf.format(commissionOrderListDto.getZyDate()), cellFormat));
					if (commissionOrderListDto.getSubagency() != null)
						sheet.addCell(new Label(32, i, commissionOrderListDto.getSubagency().getName(), cellFormat));
					sheet.addCell(new Label(33, i, commissionOrderListDto.getBonus() + "", cellFormat));
					if (commissionOrderListDto.getBonusDate() != null)
						sheet.addCell(new Label(34, i, sdf.format(commissionOrderListDto.getBonusDate()), cellFormat));
					sheet.addCell(new Label(35, i, commissionOrderListDto.getBankCheck(), cellFormat));
					sheet.addCell(new Label(36, i, commissionOrderListDto.isChecked() + "", cellFormat));
					if (commissionOrderListDto.getAdviser() != null)
						sheet.addCell(new Label(37, i, commissionOrderListDto.getAdviser().getName(), cellFormat));
					if (commissionOrderListDto.getState() != null)
						sheet.addCell(new Label(38, i, getStateStr(commissionOrderListDto.getState()), cellFormat));
					if (commissionOrderListDto.getKjApprovalDate() != null)
						sheet.addCell(new Label(39, i, sdf.format(commissionOrderListDto.getKjApprovalDate()), cellFormat));
					sheet.addCell(new Label(40, i, commissionOrderListDto.getRemarks(), cellFormat));
					ServiceOrderDTO serviceOrderDTO = serviceOrderService
							.getServiceOrderById(commissionOrderListDto.getServiceOrderId());
					sheet.addCell(new Label(41, i,
							serviceOrderDTO != null && serviceOrderDTO.getRemarks() != null ? serviceOrderDTO.getRemarks()
									: "",
							cellFormat));
					i++;
				}
				wbe.write();
				wbe.close();
				
			} else {
				
				os = response.getOutputStream();
				try {
					is = this.getClass().getResourceAsStream("/CommissionOrderTemplate.xls");
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
				for (CommissionOrderListDTO commissionOrderListDto : commissionOrderList) {
					sheet.addCell(new Label(0, i, "CS" + commissionOrderListDto.getId(), cellFormat));
					sheet.addCell(new Label(1, i, sdf.format(commissionOrderListDto.getGmtCreate()), cellFormat));
					if (commissionOrderListDto.getReceiveDate() != null)
						sheet.addCell(new Label(2, i, sdf.format(commissionOrderListDto.getReceiveDate()), cellFormat));
					if (commissionOrderListDto.getUser() != null)
						sheet.addCell(new Label(3, i, commissionOrderListDto.getUser().getName(), cellFormat));
					sheet.addCell(new Label(4, i, commissionOrderListDto.getStudentCode(), cellFormat));
					if (commissionOrderListDto.getBirthday() != null)
						sheet.addCell(new Label(5, i, sdf.format(commissionOrderListDto.getBirthday()), cellFormat));
					if (commissionOrderListDto.getReceiveType() != null)
						sheet.addCell(new Label(6, i, commissionOrderListDto.getReceiveType().getName() + "", cellFormat));
					if (commissionOrderListDto.getService() != null)
						sheet.addCell(new Label(7, i, commissionOrderListDto.getService().getName(), cellFormat));
					sheet.addCell(new Label(8, i, commissionOrderListDto.isSettle() + "", cellFormat));
					if (commissionOrderListDto.getSchool() != null) {
						sheet.addCell(new Label(9, i, commissionOrderListDto.getSchool().getName() + "", cellFormat));
						sheet.addCell(new Label(13, i, commissionOrderListDto.getSchool().getSubject() + "", cellFormat));
					}
					if (commissionOrderListDto.getSchoolInstitutionListDTO() != null){
						sheet.addCell(new Label(9, i, commissionOrderListDto.getSchoolInstitutionListDTO().getInstitutionTradingName() , cellFormat));
						//if (commissionOrderListDto.getSchoolInstitutionListDTO().getSchoolInstitutionLocationDO() != null)
						sheet.addCell(new Label(10, i, commissionOrderListDto.getSchoolInstitutionListDTO().getInstitutionName(), cellFormat));
						if (commissionOrderListDto.getSchoolInstitutionListDTO().getSchoolInstitutionLocationDO() != null){
							sheet.addCell(new Label(11, i, commissionOrderListDto.getSchoolInstitutionListDTO().getSchoolInstitutionLocationDO().getName(), cellFormat));
							sheet.addCell(new Label(12, i, commissionOrderListDto.getSchoolInstitutionListDTO().getSchoolInstitutionLocationDO().getState(), cellFormat));
						}
						if (commissionOrderListDto.getSchoolInstitutionListDTO().getSchoolCourseDO() != null)
							sheet.addCell(new Label(13, i, commissionOrderListDto.getSchoolInstitutionListDTO().getSchoolCourseDO().getCourseName(), cellFormat));
					}
					if (commissionOrderListDto.getStartDate() != null)
						sheet.addCell(new Label(14, i, sdf.format(commissionOrderListDto.getStartDate()), cellFormat));
					if (commissionOrderListDto.getEndDate() != null)
						sheet.addCell(new Label(15, i, sdf.format(commissionOrderListDto.getEndDate()), cellFormat));
					if (commissionOrderListDto.getInstallmentDueDate() != null)
						sheet.addCell(
								new Label(16, i, sdf.format(commissionOrderListDto.getInstallmentDueDate()), cellFormat));
					if (commissionOrderListDto.getReceiveType() != null)
						sheet.addCell(new Label(17, i, commissionOrderListDto.getReceiveType().getName() + "", cellFormat));
					sheet.addCell(new Label(18, i, commissionOrderListDto.getTuitionFee() + "", cellFormat));
					sheet.addCell(new Label(19, i, commissionOrderListDto.getPerAmount() + "", cellFormat)); // .getPerTermTuitionFee()
					sheet.addCell(new Label(20, i, commissionOrderListDto.getTotalPerAmountAUD() + "", cellFormat));
					sheet.addCell(new Label(21, i, commissionOrderListDto.getTotalAmountAUD() + "", cellFormat));
					sheet.addCell(new Label(22, i, commissionOrderListDto.getCurrency(), cellFormat));
					sheet.addCell(new Label(23, i, commissionOrderListDto.getExchangeRate() + "", cellFormat));
					sheet.addCell(new Label(24, i, commissionOrderListDto.getAmountAUD() + "", cellFormat));
					sheet.addCell(new Label(25, i, commissionOrderListDto.getExpectAmount() + "", cellFormat));
					if (commissionOrderListDto.isSettle())
						sheet.addCell(new Label(26, i, commissionOrderListDto.getExpectAmount() + "", cellFormat));
					else
						sheet.addCell(new Label(26, i, commissionOrderListDto.getSureExpectAmount() + "", cellFormat));
					sheet.addCell(new Label(27, i, commissionOrderListDto.getGst() + "", cellFormat));
					sheet.addCell(new Label(28, i, commissionOrderListDto.getDeductGst() + "", cellFormat));
					sheet.addCell(new Label(29, i, commissionOrderListDto.getSchoolPaymentAmount() + "", cellFormat));
					if (commissionOrderListDto.getSchoolPaymentDate() != null)
						sheet.addCell(
								new Label(30, i, sdf.format(commissionOrderListDto.getSchoolPaymentDate()), cellFormat));
					sheet.addCell(new Label(31, i, commissionOrderListDto.getInvoiceNumber(), cellFormat));
					if (commissionOrderListDto.getZyDate() != null)
						sheet.addCell(new Label(32, i, sdf.format(commissionOrderListDto.getZyDate()), cellFormat));
					if (commissionOrderListDto.getSubagency() != null)
						sheet.addCell(new Label(33, i, commissionOrderListDto.getSubagency().getName(), cellFormat));
					sheet.addCell(new Label(34, i, commissionOrderListDto.getBonus() + "", cellFormat));
					if (commissionOrderListDto.getBonusDate() != null)
						sheet.addCell(new Label(35, i, sdf.format(commissionOrderListDto.getBonusDate()), cellFormat));
					sheet.addCell(new Label(36, i, commissionOrderListDto.getBankCheck(), cellFormat));
					sheet.addCell(new Label(37, i, commissionOrderListDto.isChecked() + "", cellFormat));
					if (commissionOrderListDto.getAdviser() != null)
						sheet.addCell(new Label(38, i, commissionOrderListDto.getAdviser().getName(), cellFormat));
					if (commissionOrderListDto.getState() != null)
						sheet.addCell(new Label(39, i, getStateStr(commissionOrderListDto.getState()), cellFormat));
					if (commissionOrderListDto.getKjApprovalDate() != null)
						sheet.addCell(new Label(40, i, sdf.format(commissionOrderListDto.getKjApprovalDate()), cellFormat));
					sheet.addCell(new Label(41, i, commissionOrderListDto.getRemarks(), cellFormat));
					ServiceOrderDTO serviceOrderDTO = serviceOrderService
							.getServiceOrderById(commissionOrderListDto.getServiceOrderId());
					sheet.addCell(new Label(42, i,
							serviceOrderDTO != null && serviceOrderDTO.getRemarks() != null ? serviceOrderDTO.getRemarks()
									: "",
							cellFormat));
					i++;
				}
				wbe.write();
				wbe.close();
				
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}finally{
			try {
				if (is != null)
					is.close();
				System.out.println("is is close");
			} catch (IOException e) {
				System.out.println("is is close 出现 异常:");
				e.printStackTrace();
			}
			try {
				if (os != null)
					os.close();
				System.out.println("os is close");
			} catch (IOException e) {
				System.out.println("os is close 出现 异常:");
				e.printStackTrace();
			}
			if (wb != null)
				wb.close();
			System.out.println("wb is close");
		}

	}

	@RequestMapping(value = "/get", method = RequestMethod.GET)
	@ResponseBody
	public Response<CommissionOrderListDTO> get(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<CommissionOrderListDTO>(0, commissionOrderService.getCommissionOrderById(id));
		} catch (ServiceException e) {
			return new Response<CommissionOrderListDTO>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/get2", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<CommissionOrderListDTO>> get2(@RequestParam(value = "idList") String idList [], HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			List<CommissionOrderListDTO> commissionOrderListDTOList = new ArrayList<>();
			for (String id : idList ){
				CommissionOrderListDTO commissionOrderListDTO = commissionOrderService.getCommissionOrderById(StringUtil.toInt(id));
				if (commissionOrderListDTO == null)
					return new Response<List<CommissionOrderListDTO>>(1,"佣金订单不存在 id : " + id, null);
				commissionOrderListDTOList.add(commissionOrderListDTO);
			}
			return new Response<List<CommissionOrderListDTO>>(0, commissionOrderListDTOList );
		} catch (ServiceException e) {
			return new Response<List<CommissionOrderListDTO>>(1, e.getMessage(), null);
		}
	}
	@RequestMapping(value = "/getInfo", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<CommissionInfoDTO>> getInfo(@RequestParam(value = "id") int id,HttpServletRequest request, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			Integer adviserId = getAdviserId(request);
			return new Response<List<CommissionInfoDTO>>(0, commissionOrderService.getCommissionInfoById(id,adviserId));
		}
		catch (ServiceException e) {
			return new Response<List<CommissionInfoDTO>>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/approval", method = RequestMethod.POST)
	@ResponseBody
	public Response<CommissionOrderListDTO> approval(@RequestParam(value = "id") int id,
			@RequestParam(value = "state") String state, HttpServletRequest request, HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			if (ReviewKjStateEnum.CLOSE.toString().equalsIgnoreCase(state))
				return new Response<CommissionOrderListDTO>(1, "关闭操作请调用'refuse'接口.", null);
			// 审核
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo != null) {
				if (adminUserLoginInfo == null || (!"SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())
						&& !"KJ".equalsIgnoreCase(adminUserLoginInfo.getApList())))
					return new Response<CommissionOrderListDTO>(1, "仅限会计审核佣金订单.", null);
				if ("SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())
						|| "KJ".equalsIgnoreCase(adminUserLoginInfo.getApList())) {
					if (ReviewKjStateEnum.get(state) != null) {
						CommissionOrderListDTO commissionOrderListDto = commissionOrderService
								.getCommissionOrderById(id);
						if (commissionOrderListDto == null)
							return new Response<CommissionOrderListDTO>(1, "佣金订单不存在!", null);
						serviceOrderService.approval(id, adminUserLoginInfo.getId(), null, null, null,
								state.toUpperCase());
						commissionOrderListDto.setState(state);
						if (commissionOrderService.updateCommissionOrder(commissionOrderListDto) > 0)
							return new Response<CommissionOrderListDTO>(0, commissionOrderListDto);
						else
							return new Response<CommissionOrderListDTO>(1, "修改操作异常!", null);
					} else
						return new Response<CommissionOrderListDTO>(1, "state错误!(" + state + ")", null);
				} else
					return new Response<CommissionOrderListDTO>(1, "该用户无审核权限!", null);
			} else
				return new Response<CommissionOrderListDTO>(1, "请登录!", null);
		} catch (ServiceException e) {
			return new Response<CommissionOrderListDTO>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/refuse", method = RequestMethod.POST)
	@ResponseBody
	public Response<CommissionOrderListDTO> refuse(@RequestParam(value = "id") int id,
			@RequestParam(value = "state") String state,
			@RequestParam(value = "refuseReason", required = false) String refuseReason, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			if (ReviewKjStateEnum.COMPLETE.toString().equalsIgnoreCase(state)
					|| ReviewKjStateEnum.FINISH.toString().equalsIgnoreCase(state))
				return new Response<CommissionOrderListDTO>(1, "完成操作请调用'approval'接口.", null);
			// 审核
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo != null) {
				if (adminUserLoginInfo == null || (!"SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())
						&& !"KJ".equalsIgnoreCase(adminUserLoginInfo.getApList())))
					return new Response<CommissionOrderListDTO>(1, "仅限会计审核佣金订单.", null);
				if ("SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())
						|| "KJ".equalsIgnoreCase(adminUserLoginInfo.getApList())) {
					if (ReviewKjStateEnum.get(state) != null) {

						CommissionOrderListDTO commissionOrderListDto = commissionOrderService
								.getCommissionOrderById(id);
						if (commissionOrderListDto == null)
							return new Response<CommissionOrderListDTO>(1, "佣金订单不存在!", null);
						// 更新驳回原因
						if (StringUtil.isNotEmpty(refuseReason))
							commissionOrderListDto.setRefuseReason(refuseReason);
						commissionOrderListDto.setState(state);
						if (commissionOrderService.updateCommissionOrder(commissionOrderListDto) > 0) {
							commissionOrderService.sendRefuseEmail(id);
							return new Response<CommissionOrderListDTO>(0, commissionOrderListDto);
						} else
							return new Response<CommissionOrderListDTO>(1, "修改操作异常!", null);
					} else
						return new Response<CommissionOrderListDTO>(1, "state错误!(" + state + ")", null);
				} else
					return new Response<CommissionOrderListDTO>(1, "该用户无审核权限!", null);
			} else
				return new Response<CommissionOrderListDTO>(1, "请登录!", null);
		} catch (ServiceException e) {
			return new Response<CommissionOrderListDTO>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/addComment", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> addComment(@RequestParam(value = "adminUserId", required = false) Integer adminUserId,
			@RequestParam(value = "commissionOrderId", required = false) Integer commissionOrderId,
			@RequestParam(value = "content") String content, HttpServletRequest request, HttpServletResponse response) {
		try {
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			super.setPostHeader(response);
			CommissionOrderCommentDTO commissionOrderCommentDto = new CommissionOrderCommentDTO();
			commissionOrderCommentDto
					.setAdminUserId(adminUserLoginInfo != null ? adminUserLoginInfo.getId() : adminUserId);
			commissionOrderCommentDto.setCommissionOrderId(commissionOrderId);
			commissionOrderCommentDto.setContent(content);
			if (commissionOrderService.addComment(commissionOrderCommentDto) > 0)
				return new Response<Integer>(0, commissionOrderCommentDto.getId());
			else
				return new Response<Integer>(1, "创建失败.", 0);
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/updateInfo",method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> updateInfo (@RequestParam(value = "serviceorderid",required = false)Integer serviceorderid,
									     @RequestParam(value = "installment",required = false)Integer installment,
										 @RequestParam(value = "installmentDueDate1",required = false) String installmentDueDate1,
										 @RequestParam(value = "installmentDueDate2", required = false) String installmentDueDate2,
										 @RequestParam(value = "installmentDueDate3", required = false) String installmentDueDate3,
										 @RequestParam(value = "installmentDueDate4", required = false) String installmentDueDate4,
										 @RequestParam(value = "installmentDueDate5", required = false) String installmentDueDate5,
										 @RequestParam(value = "installmentDueDate6", required = false) String installmentDueDate6,
										 @RequestParam(value = "installmentDueDate7", required = false) String installmentDueDate7,
										 @RequestParam(value = "installmentDueDate8", required = false) String installmentDueDate8,
										 @RequestParam(value = "installmentDueDate9", required = false) String installmentDueDate9,
										 @RequestParam(value = "installmentDueDate10", required = false) String installmentDueDate10,
										 @RequestParam(value = "installmentDueDate11", required = false) String installmentDueDate11,
										 @RequestParam(value = "installmentDueDate12", required = false) String installmentDueDate12,
									  HttpServletRequest request, HttpServletResponse response)
	{
		try {
			super.setPostHeader(response);
			Integer adviserId = getAdviserId(request);
			List<CommissionInfoDTO> info = commissionOrderService.getCommissionInfoById(serviceorderid,adviserId);
			if (info.size()>installment){
				for (CommissionInfoDTO commissionInfoDTO : info) {
					if (commissionInfoDTO.getInstallmentNum()>installment && commissionInfoDTO.getState().equals("PENDING")){
						commissionOrderService.deleteCommissionOrderInfoById(serviceorderid,commissionInfoDTO.getInstallmentNum());
					}
				}
				commissionOrderService.setinstallmentById(serviceorderid,installment);
				if (installmentDueDate1!=null){
					commissionOrderService.setinstallmentDueDateById(serviceorderid,1,new Date(Long.parseLong(installmentDueDate1)));
				}
				if (installmentDueDate2!=null){
					commissionOrderService.setinstallmentDueDateById(serviceorderid,2,new Date(Long.parseLong(installmentDueDate2)));
				}
				if (installmentDueDate3!=null){
					commissionOrderService.setinstallmentDueDateById(serviceorderid,3,new Date(Long.parseLong(installmentDueDate3)));
				}
				if (installmentDueDate4!=null){
					commissionOrderService.setinstallmentDueDateById(serviceorderid,4,new Date(Long.parseLong(installmentDueDate4)));
				}
				if (installmentDueDate5!=null){
					commissionOrderService.setinstallmentDueDateById(serviceorderid,5,new Date(Long.parseLong(installmentDueDate5)));
				}
				if (installmentDueDate6!=null){
					commissionOrderService.setinstallmentDueDateById(serviceorderid,6,new Date(Long.parseLong(installmentDueDate6)));
				}
				if (installmentDueDate7!=null){
					commissionOrderService.setinstallmentDueDateById(serviceorderid,7,new Date(Long.parseLong(installmentDueDate7)));
				}
				if (installmentDueDate8!=null){
					commissionOrderService.setinstallmentDueDateById(serviceorderid,8,new Date(Long.parseLong(installmentDueDate8)));
				}
				if (installmentDueDate9!=null){
					commissionOrderService.setinstallmentDueDateById(serviceorderid,9,new Date(Long.parseLong(installmentDueDate9)));
				}
				if (installmentDueDate10!=null){
					commissionOrderService.setinstallmentDueDateById(serviceorderid,10,new Date(Long.parseLong(installmentDueDate10)));
				}
				if (installmentDueDate11!=null){
					commissionOrderService.setinstallmentDueDateById(serviceorderid,11,new Date(Long.parseLong(installmentDueDate11)));
				}
				if (installmentDueDate12!=null){
					commissionOrderService.setinstallmentDueDateById(serviceorderid,12,new Date(Long.parseLong(installmentDueDate12)));
				}
			}
			if (info.size()<installment){
				int num = installment-info.size();
				for (int i = 0; i < num; i++) {
					commissionOrderService.addCommissionInfoById(serviceorderid);
				}
				commissionOrderService.setinstallmentById(serviceorderid,installment);
				if (installmentDueDate1!=null){
					commissionOrderService.setinstallmentDueDateById(serviceorderid,1,new Date(Long.parseLong(installmentDueDate1)));
				}
				if (installmentDueDate2!=null){
					commissionOrderService.setinstallmentDueDateById(serviceorderid,2,new Date(Long.parseLong(installmentDueDate2)));
				}
				if (installmentDueDate3!=null){
					commissionOrderService.setinstallmentDueDateById(serviceorderid,3,new Date(Long.parseLong(installmentDueDate3)));
				}
				if (installmentDueDate4!=null){
					commissionOrderService.setinstallmentDueDateById(serviceorderid,4,new Date(Long.parseLong(installmentDueDate4)));
				}
				if (installmentDueDate5!=null){
					commissionOrderService.setinstallmentDueDateById(serviceorderid,5,new Date(Long.parseLong(installmentDueDate5)));
				}
				if (installmentDueDate6!=null){
					commissionOrderService.setinstallmentDueDateById(serviceorderid,6,new Date(Long.parseLong(installmentDueDate6)));
				}
				if (installmentDueDate7!=null){
					commissionOrderService.setinstallmentDueDateById(serviceorderid,7,new Date(Long.parseLong(installmentDueDate7)));
				}
				if (installmentDueDate8!=null){
					commissionOrderService.setinstallmentDueDateById(serviceorderid,8,new Date(Long.parseLong(installmentDueDate8)));
				}
				if (installmentDueDate9!=null){
					commissionOrderService.setinstallmentDueDateById(serviceorderid,9,new Date(Long.parseLong(installmentDueDate9)));
				}
				if (installmentDueDate10!=null){
					commissionOrderService.setinstallmentDueDateById(serviceorderid,10,new Date(Long.parseLong(installmentDueDate10)));
				}
				if (installmentDueDate11!=null){
					commissionOrderService.setinstallmentDueDateById(serviceorderid,11,new Date(Long.parseLong(installmentDueDate11)));
				}
				if (installmentDueDate12!=null){
					commissionOrderService.setinstallmentDueDateById(serviceorderid,12,new Date(Long.parseLong(installmentDueDate12)));
				}

			}
			if (info.size()==installment){
				if (installmentDueDate1!=null){
					commissionOrderService.setinstallmentDueDateById(serviceorderid,1,new Date(Long.parseLong(installmentDueDate1)));
				}
				if (installmentDueDate2!=null){
					commissionOrderService.setinstallmentDueDateById(serviceorderid,2,new Date(Long.parseLong(installmentDueDate2)));
				}
				if (installmentDueDate3!=null){
					commissionOrderService.setinstallmentDueDateById(serviceorderid,3,new Date(Long.parseLong(installmentDueDate3)));
				}
				if (installmentDueDate4!=null){
					commissionOrderService.setinstallmentDueDateById(serviceorderid,4,new Date(Long.parseLong(installmentDueDate4)));
				}
				if (installmentDueDate5!=null){
					commissionOrderService.setinstallmentDueDateById(serviceorderid,5,new Date(Long.parseLong(installmentDueDate5)));
				}
				if (installmentDueDate6!=null){
					commissionOrderService.setinstallmentDueDateById(serviceorderid,6,new Date(Long.parseLong(installmentDueDate6)));
				}
				if (installmentDueDate7!=null){
					commissionOrderService.setinstallmentDueDateById(serviceorderid,7,new Date(Long.parseLong(installmentDueDate7)));
				}
				if (installmentDueDate8!=null){
					commissionOrderService.setinstallmentDueDateById(serviceorderid,8,new Date(Long.parseLong(installmentDueDate8)));
				}
				if (installmentDueDate9!=null){
					commissionOrderService.setinstallmentDueDateById(serviceorderid,9,new Date(Long.parseLong(installmentDueDate9)));
				}
				if (installmentDueDate10!=null){
					commissionOrderService.setinstallmentDueDateById(serviceorderid,10,new Date(Long.parseLong(installmentDueDate10)));
				}
				if (installmentDueDate11!=null){
					commissionOrderService.setinstallmentDueDateById(serviceorderid,11,new Date(Long.parseLong(installmentDueDate11)));
				}
				if (installmentDueDate12!=null){
					commissionOrderService.setinstallmentDueDateById(serviceorderid,12,new Date(Long.parseLong(installmentDueDate12)));
				}
			}
			return new Response<Integer>(0,"修改成功",0);
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}

	}

	@RequestMapping(value = "/countComment", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> countComment(@RequestParam(value = "commissionOrderId") Integer commissionOrderId,
			HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, commissionOrderService.listComment(commissionOrderId).size());
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/listComment", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<CommissionOrderCommentDTO>> listComment(
			@RequestParam(value = "commissionOrderId") Integer commissionOrderId, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<List<CommissionOrderCommentDTO>>(0,
					commissionOrderService.listComment(commissionOrderId));
		} catch (ServiceException e) {
			return new Response<List<CommissionOrderCommentDTO>>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/deleteComment", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> deleteComment(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, commissionOrderService.deleteComment(id));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/deleteCommissionOrder", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> deleteCommissionOrder(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, commissionOrderService.deleteCommissionOrder(id));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), 0);
		}
	}

	/*
	*财务驳回状态为REFERED，顾问修改佣金信息之后再提交申请月奖
	 */
	@Deprecated
	@RequestMapping(value = "/updateSubmitted", method = RequestMethod.POST)
	@ResponseBody
	public Response<List<CommissionOrderDTO>> updateSubmitted(@RequestParam(value = "serviceOrderId") Integer serviceOrderId,
					//@RequestParam(value = "state", required = false) String state,
					@RequestParam(value = "isSettle") Boolean isSettle,
					@RequestParam(value = "isDepositUser") Boolean isDepositUser,
					@RequestParam(value = "schoolId") Integer schoolId, @RequestParam(value = "studentCode") String studentCode,
					@RequestParam(value = "userId") Integer userId, @RequestParam(value = "adviserId") Integer adviserId,
					@RequestParam(value = "officialId") Integer officialId,
					@RequestParam(value = "isStudying") Boolean isStudying,
					@RequestParam(value = "installment") Integer installment,
					@RequestParam(value = "installmentDueDate1") String installmentDueDate1,
					@RequestParam(value = "installmentDueDate2", required = false) String installmentDueDate2,
					@RequestParam(value = "installmentDueDate3", required = false) String installmentDueDate3,
					@RequestParam(value = "installmentDueDate4", required = false) String installmentDueDate4,
					@RequestParam(value = "installmentDueDate5", required = false) String installmentDueDate5,
					@RequestParam(value = "installmentDueDate6", required = false) String installmentDueDate6,
					@RequestParam(value = "installmentDueDate7", required = false) String installmentDueDate7,
					@RequestParam(value = "installmentDueDate8", required = false) String installmentDueDate8,
					@RequestParam(value = "installmentDueDate9", required = false) String installmentDueDate9,
					@RequestParam(value = "installmentDueDate10", required = false) String installmentDueDate10,
					@RequestParam(value = "installmentDueDate11", required = false) String installmentDueDate11,
					@RequestParam(value = "installmentDueDate12", required = false) String installmentDueDate12,
					@RequestParam(value = "paymentVoucherImageUrl1", required = false) String paymentVoucherImageUrl1,
					@RequestParam(value = "paymentVoucherImageUrl2", required = false) String paymentVoucherImageUrl2,
					@RequestParam(value = "paymentVoucherImageUrl3", required = false) String paymentVoucherImageUrl3,
					@RequestParam(value = "paymentVoucherImageUrl4", required = false) String paymentVoucherImageUrl4,
					@RequestParam(value = "paymentVoucherImageUrl5", required = false) String paymentVoucherImageUrl5,
					@RequestParam(value = "dob") String dob, @RequestParam(value = "startDate") String startDate,
					@RequestParam(value = "endDate") String endDate, @RequestParam(value = "tuitionFee") String tuitionFee,
					@RequestParam(value = "perTermTuitionFee") String perTermTuitionFee,
					@RequestParam(value = "receiveTypeId") Integer receiveTypeId,
					@RequestParam(value = "receiveDate") String receiveDate,
					@RequestParam(value = "perAmount") String perAmount, @RequestParam(value = "amount") String amount,
					@RequestParam(value = "bonusDate", required = false) String bonusDate,
					@RequestParam(value = "zyDate", required = false) String zyDate,
					@RequestParam(value = "remarks", required = false) String remarks,
					@RequestParam(value = "verifyCode", required = false) String verifyCode,
					HttpServletRequest request, HttpServletResponse response){

		try {
			super.setPostHeader(response);
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo == null || (!"SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())
					&& !"GW".equalsIgnoreCase(adminUserLoginInfo.getApList())))
				return new Response<List<CommissionOrderDTO>>(1, "仅限顾问和超级管理员能修改佣金订单.", null);
			ServiceOrderDTO serviceOrderDTO = serviceOrderService.getServiceOrderById(serviceOrderId);
			if (serviceOrderDTO == null)
				return new Response<List<CommissionOrderDTO>>(1, "服务订单(ID:" + serviceOrderId + ")不存在!", null);
			if (serviceOrderDTO.getSubagencyId() <= 0)
				return new Response<List<CommissionOrderDTO>>
						(1, "SubagencyId(" + serviceOrderDTO.getSubagencyId() + ")不存在!", null);
			//List<CommissionOrderListDTO> commissionOrderListDTOS= commissionOrderService.CommissionOrderListByServiceOrderId(serviceOrderId);
			//if (commissionOrderListDTOS.size() == 0)
			CommissionOrderListDTO commissionOrderListDTO = commissionOrderService.getFirstCommissionOrderByServiceOrderId(serviceOrderId);
			if (commissionOrderListDTO == null)
				return new Response<List<CommissionOrderDTO>>(1, "还未创建服务订单为:" + serviceOrderId + "的佣金订单", null);
			//CommissionOrderDTO commissionOrderDto = new CommissionOrderDTO();
			if (isSettle)
				commissionOrderListDTO.setCommissionState(CommissionStateEnum.DJY.toString());
			else
				commissionOrderListDTO.setCommissionState(CommissionStateEnum.DZY.toString());
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl1))
				commissionOrderListDTO.setPaymentVoucherImageUrl1(paymentVoucherImageUrl1);
			else
				commissionOrderListDTO.setPaymentVoucherImageUrl1(serviceOrderDTO.getPaymentVoucherImageUrl1());
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl2))
				commissionOrderListDTO.setPaymentVoucherImageUrl2(paymentVoucherImageUrl2);
			else
				commissionOrderListDTO.setPaymentVoucherImageUrl2(serviceOrderDTO.getPaymentVoucherImageUrl2());
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl3))
				commissionOrderListDTO.setPaymentVoucherImageUrl3(paymentVoucherImageUrl3);
			else
				commissionOrderListDTO.setPaymentVoucherImageUrl3(serviceOrderDTO.getPaymentVoucherImageUrl3());
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl4))
				commissionOrderListDTO.setPaymentVoucherImageUrl4(paymentVoucherImageUrl4);
			else
				commissionOrderListDTO.setPaymentVoucherImageUrl4(serviceOrderDTO.getPaymentVoucherImageUrl4());
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl5))
				commissionOrderListDTO.setPaymentVoucherImageUrl5(paymentVoucherImageUrl5);
			else
				commissionOrderListDTO.setPaymentVoucherImageUrl5(serviceOrderDTO.getPaymentVoucherImageUrl5());
			commissionOrderListDTO.setStudentCode(studentCode);
			commissionOrderListDTO.setDob(new Date(Long.parseLong(dob)));
			commissionOrderListDTO.setStartDate(new Date(Long.parseLong(startDate)));
			commissionOrderListDTO.setEndDate(new Date(Long.parseLong(endDate)));
			commissionOrderListDTO.setTuitionFee(Double.parseDouble(tuitionFee));
			commissionOrderListDTO.setPerTermTuitionFee(Double.parseDouble(perTermTuitionFee));
			commissionOrderListDTO.setReceiveTypeId(receiveTypeId);
			commissionOrderListDTO.setReceiveDate(new Date(Long.parseLong(receiveDate)));
			commissionOrderListDTO.setPerAmount(Double.parseDouble(perAmount));
			commissionOrderListDTO.setAmount(Double.parseDouble(amount));
			if (installment == null || installment != commissionOrderListDTO.getInstallment())
				installment = commissionOrderListDTO.getInstallment();
			if (commissionOrderListDTO.getPerAmount() < commissionOrderListDTO.getAmount())
				return new Response<List<CommissionOrderDTO>>(1, "本次应收款(" + commissionOrderListDTO.getPerAmount()
						+ ")不能小于本次已收款(" + commissionOrderListDTO.getAmount() + ")!", null);
			commissionOrderListDTO.setDiscount(commissionOrderListDTO.getPerAmount() - commissionOrderListDTO.getAmount());
			if (StringUtil.isNotEmpty(bonusDate))
				commissionOrderListDTO.setBonusDate(new Date(Long.parseLong(bonusDate)));
			if (StringUtil.isNotEmpty(zyDate))
				commissionOrderListDTO.setZyDate(new Date(Long.parseLong(zyDate)));
			if (StringUtil.isNotEmpty(remarks))
				commissionOrderListDTO.setRemarks(remarks);

			String msg = "";
			for (int installmentNum = 1; installmentNum <= installment; installmentNum++) {
				commissionOrderListDTO.setInstallmentNum(installmentNum);
				if (installmentNum == 1 && installmentDueDate1 != null) {
					commissionOrderListDTO.setInstallmentDueDate(new Date(Long.parseLong(installmentDueDate1)));
					//commissionOrderListDTO.setState(ReviewKjStateEnum.REVIEW.toString()); // 第一笔单子直接进入财务审核状态
					if (StringUtil.isNotEmpty(verifyCode))
						commissionOrderListDTO.setVerifyCode(verifyCode.replace("$", "").replace("#", "").replace(" ", ""));
					//commissionOrderListDTO.setKjApprovalDate(new Date());
				} else {
					if (installmentNum == 2 && installmentDueDate2 != null) {
						commissionOrderListDTO.setInstallmentDueDate(new Date(Long.parseLong(installmentDueDate2)));
					} else if (installmentNum == 3 && installmentDueDate3 != null) {
						commissionOrderListDTO.setInstallmentDueDate(new Date(Long.parseLong(installmentDueDate3)));
					} else if (installmentNum == 4 && installmentDueDate4 != null) {
						commissionOrderListDTO.setInstallmentDueDate(new Date(Long.parseLong(installmentDueDate4)));
					} else if (installmentNum == 5 && installmentDueDate5 != null) {
						commissionOrderListDTO.setInstallmentDueDate(new Date(Long.parseLong(installmentDueDate5)));
					} else if (installmentNum == 6 && installmentDueDate6 != null) {
						commissionOrderListDTO.setInstallmentDueDate(new Date(Long.parseLong(installmentDueDate6)));
					} else if (installmentNum == 7 && installmentDueDate7 != null) {
						commissionOrderListDTO.setInstallmentDueDate(new Date(Long.parseLong(installmentDueDate7)));
					} else if (installmentNum == 8 && installmentDueDate8 != null) {
						commissionOrderListDTO.setInstallmentDueDate(new Date(Long.parseLong(installmentDueDate8)));
					} else if (installmentNum == 9 && installmentDueDate9 != null) {
						commissionOrderListDTO.setInstallmentDueDate(new Date(Long.parseLong(installmentDueDate9)));
					} else if (installmentNum == 10 && installmentDueDate10 != null) {
						commissionOrderListDTO.setInstallmentDueDate(new Date(Long.parseLong(installmentDueDate10)));
					} else if (installmentNum == 11 && installmentDueDate11 != null) {
						commissionOrderListDTO.setInstallmentDueDate(new Date(Long.parseLong(installmentDueDate11)));
					} else if (installmentNum == 12 && installmentDueDate12 != null) {
						commissionOrderListDTO.setInstallmentDueDate(new Date(Long.parseLong(installmentDueDate12)));
					} else
						break;
					//commissionOrderListDTO.setState(ReviewKjStateEnum.PENDING.toString());
					commissionOrderListDTO.setVerifyCode(null);
					commissionOrderListDTO.setKjApprovalDate(null);
					commissionOrderListDTO.setReceiveDate(null);
					commissionOrderListDTO.setPaymentVoucherImageUrl1(null);
					commissionOrderListDTO.setPaymentVoucherImageUrl2(null);
					commissionOrderListDTO.setPaymentVoucherImageUrl3(null);
					commissionOrderListDTO.setPaymentVoucherImageUrl4(null);
					commissionOrderListDTO.setPaymentVoucherImageUrl5(null);
				}
				int id = commissionOrderService.updateCommissionOrderByServiceOrderId(commissionOrderListDTO);
				if (id > 0) {
					//serviceOrderDto.setSubmitted(true);
					userService.updateDOB(new Date(Long.parseLong(dob)), userId);
					//serviceOrderService.updateServiceOrder(serviceOrderDTO); // 同时更改服务订单状态
					//commissionOrderDtoList.add(commissionOrderDto);
					CommissionOrderListDTO commissionOrderListDto = commissionOrderService.getCommissionOrderById(id);
					int i = schoolService.updateSchoolSetting(commissionOrderListDto); // 根据学校设置更新佣金值
					if (i > 0) {
					} else if (i == -1)
						msg += id + "计算失败. (佣金记录不存在);";
					else if (i == -2)
						msg += id + "计算失败. (学校佣金设置不存在或不正确);";
					else if (i == -3)
						msg += id + "计算失败. (佣金办理时间不在设置合同时间范围内);";
					else
						msg += id + "计算失败. ;";
				} else
					msg += "佣金订单创建失败. (" + commissionOrderListDTO.toString() + ");";
			}
			return new Response<List<CommissionOrderDTO>>(0, msg, null);

		} catch (ServiceException e) {
			return new Response<List<CommissionOrderDTO>>(e.getCode(), e.getMessage(), null);
		}
	}

	/**
	 * 提前扣拥留学：顾问填写申请月奖的数据，暂存到b_commission_order_temp表中，等财务通过之后，获取
	 * 数据再创建佣金订单
	 * @return
	 */
	@PostMapping(value = "/addCommissionOrderTemp")
	@ResponseBody
	public Response addCommissionOrderTemp(@RequestParam(value = "installment") Integer installment,
		   @RequestParam(value = "installmentDueDate1") String installmentDueDate1,
		   @RequestParam(value = "installmentDueDate2", required = false) String installmentDueDate2,
		   @RequestParam(value = "installmentDueDate3", required = false) String installmentDueDate3,
		   @RequestParam(value = "installmentDueDate4", required = false) String installmentDueDate4,
		   @RequestParam(value = "installmentDueDate5", required = false) String installmentDueDate5,
		   @RequestParam(value = "installmentDueDate6", required = false) String installmentDueDate6,
		   @RequestParam(value = "installmentDueDate7", required = false) String installmentDueDate7,
		   @RequestParam(value = "installmentDueDate8", required = false) String installmentDueDate8,
		   @RequestParam(value = "installmentDueDate9", required = false) String installmentDueDate9,
		   @RequestParam(value = "installmentDueDate10", required = false) String installmentDueDate10,
		   @RequestParam(value = "installmentDueDate11", required = false) String installmentDueDate11,
		   @RequestParam(value = "installmentDueDate12", required = false) String installmentDueDate12,
		   @RequestParam(value = "paymentVoucherImageUrl1", required = false) String paymentVoucherImageUrl1,
		   @RequestParam(value = "paymentVoucherImageUrl2", required = false) String paymentVoucherImageUrl2,
		   @RequestParam(value = "paymentVoucherImageUrl3", required = false) String paymentVoucherImageUrl3,
		   @RequestParam(value = "paymentVoucherImageUrl4", required = false) String paymentVoucherImageUrl4,
		   @RequestParam(value = "paymentVoucherImageUrl5", required = false) String paymentVoucherImageUrl5,
		   @RequestParam(value = "invoiceVoucherImageUrl1", required = false) String invoiceVoucherImageUrl1,
		   @RequestParam(value = "invoiceVoucherImageUrl2", required = false) String invoiceVoucherImageUrl2,
		   @RequestParam(value = "invoiceVoucherImageUrl3", required = false) String invoiceVoucherImageUrl3,
		   @RequestParam(value = "invoiceVoucherImageUrl4", required = false) String invoiceVoucherImageUrl4,
		   @RequestParam(value = "invoiceVoucherImageUrl5", required = false) String invoiceVoucherImageUrl5,
		   @RequestParam(value = "dob") String dob, @RequestParam(value = "startDate") String startDate,
		   @RequestParam(value = "endDate") String endDate, @RequestParam(value = "tuitionFee") String tuitionFee,
		   @RequestParam(value = "perTermTuitionFee") String perTermTuitionFee,
		   @RequestParam(value = "receiveTypeId") Integer receiveTypeId,
		   @RequestParam(value = "receiveDate") String receiveDate,
		   @RequestParam(value = "perAmount") String perAmount, @RequestParam(value = "amount") String amount,
		   @RequestParam(value = "remarks", required = false) String remarks,
		   @RequestParam(value = "studentCode") String studentCode,
		   @RequestParam(value = "serviceOrderId")int serviceOrderId,
		   @RequestParam(value = "expectAmount")String expectAmount,
			@RequestParam(value = "currency", required = false) String currency,
			@RequestParam(value = "exchangeRate", required = false) String exchangeRate,
		   @RequestParam(value = "verifyCode", required = false) String verifyCode,
			HttpServletRequest request, HttpServletResponse response
										   ){
		try {
			super.setPostHeader(response);
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo == null || (!"SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())
					&& !"GW".equalsIgnoreCase(adminUserLoginInfo.getApList())))
				return new Response(1, "仅限顾问和超级管理员能创建.", null);
			ServiceOrderDTO serviceOrderDto = serviceOrderService.getServiceOrderById(serviceOrderId);
			if (serviceOrderDto == null)
				return new Response(1, "服务订单(ID:" + serviceOrderId + ")不存在!", null);

			if (serviceOrderDto.getSubagencyId() <= 0)
				return new Response(1,
						"SubagencyId(" + serviceOrderDto.getSubagencyId() + ")不存在!", null);
			CommissionOrderTempDTO tempDTO = commissionOrderService.getCommissionOrderTempByServiceOrderId(serviceOrderId);
			if (tempDTO != null) {
				return updateCommissionOrderTemp(installment,installmentDueDate1,installmentDueDate2,installmentDueDate3,installmentDueDate4,
						installmentDueDate5,installmentDueDate6,installmentDueDate7,installmentDueDate8,installmentDueDate9,installmentDueDate10,
						installmentDueDate11,installmentDueDate12,paymentVoucherImageUrl1,paymentVoucherImageUrl2,paymentVoucherImageUrl3,
						paymentVoucherImageUrl4,paymentVoucherImageUrl5,invoiceVoucherImageUrl1,invoiceVoucherImageUrl2,invoiceVoucherImageUrl3,
						invoiceVoucherImageUrl4,invoiceVoucherImageUrl5,dob, startDate,endDate, tuitionFee,perTermTuitionFee,receiveTypeId,
						receiveDate,perAmount, amount,remarks,studentCode,serviceOrderId,expectAmount, currency, exchangeRate,verifyCode,tempDTO,serviceOrderDto);
			}
			tempDTO = new CommissionOrderTempDTO();
			tempDTO.setServiceOrderId(serviceOrderId);
			tempDTO.setStudentCode(studentCode);
			tempDTO.setDob(new Date(Long.parseLong(dob)));
			tempDTO.setStartDate(new Date(Long.parseLong(startDate)));
			tempDTO.setEndDate(new Date(Long.parseLong(endDate)));
			tempDTO.setTuitionFee(Double.parseDouble(tuitionFee));
			tempDTO.setPerTermTuitionFee(Double.parseDouble(perTermTuitionFee));
			tempDTO.setReceiveTypeId(receiveTypeId);
			tempDTO.setReceiveDate(new Date(Long.parseLong(receiveDate)));
			tempDTO.setPerAmount(Double.parseDouble(perAmount));
			tempDTO.setAmount(Double.parseDouble(amount));
			tempDTO.setExpectAmount(Double.parseDouble(expectAmount));
			if (StringUtil.isNotEmpty(currency))
				tempDTO.setCurrency(currency);
			if (StringUtil.isNotEmpty(exchangeRate))
				tempDTO.setExchangeRate(Double.parseDouble(exchangeRate));
			if (tempDTO.getPerAmount() < tempDTO.getAmount())
				return new Response(1, "本次应收款(" + tempDTO.getPerAmount()
						+ ")不能小于本次已收款(" + tempDTO.getAmount() + ")!", null);
			tempDTO.setDiscount(tempDTO.getPerAmount() - tempDTO.getAmount());
			if (StringUtil.isNotEmpty(verifyCode))
				tempDTO.setVerifyCode(verifyCode);
			if (StringUtil.isNotEmpty(remarks))
				tempDTO.setRemarks(remarks);
			if (installment == null || installment <= 0 || installment > 12)
				return new Response(1,"installment = " + installment);
			tempDTO.setInstallment(installment);
			if (StringUtil.isNotEmpty(installmentDueDate1))
				tempDTO.setInstallmentDueDate1(new Date(Long.parseLong(installmentDueDate1)));
			if (StringUtil.isNotEmpty(installmentDueDate2))
				tempDTO.setInstallmentDueDate2(new Date(Long.parseLong(installmentDueDate2)));
			if (StringUtil.isNotEmpty(installmentDueDate3))
				tempDTO.setInstallmentDueDate3(new Date(Long.parseLong(installmentDueDate3)));
			if (StringUtil.isNotEmpty(installmentDueDate4))
				tempDTO.setInstallmentDueDate4(new Date(Long.parseLong(installmentDueDate4)));
			if (StringUtil.isNotEmpty(installmentDueDate5))
				tempDTO.setInstallmentDueDate5(new Date(Long.parseLong(installmentDueDate5)));
			if (StringUtil.isNotEmpty(installmentDueDate6))
				tempDTO.setInstallmentDueDate6(new Date(Long.parseLong(installmentDueDate6)));
			if (StringUtil.isNotEmpty(installmentDueDate7))
				tempDTO.setInstallmentDueDate7(new Date(Long.parseLong(installmentDueDate7)));
			if (StringUtil.isNotEmpty(installmentDueDate8))
				tempDTO.setInstallmentDueDate8(new Date(Long.parseLong(installmentDueDate8)));
			if (StringUtil.isNotEmpty(installmentDueDate9))
				tempDTO.setInstallmentDueDate9(new Date(Long.parseLong(installmentDueDate9)));
			if (StringUtil.isNotEmpty(installmentDueDate10))
				tempDTO.setInstallmentDueDate10(new Date(Long.parseLong(installmentDueDate10)));
			if (StringUtil.isNotEmpty(installmentDueDate11))
				tempDTO.setInstallmentDueDate11(new Date(Long.parseLong(installmentDueDate11)));
			if (StringUtil.isNotEmpty(installmentDueDate12))
				tempDTO.setInstallmentDueDate12(new Date(Long.parseLong(installmentDueDate12)));
			
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl1)){
				tempDTO.setPaymentVoucherImageUrl1(paymentVoucherImageUrl1);
				serviceOrderDto.setPaymentVoucherImageUrl1(paymentVoucherImageUrl1);
			}
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl2)){
				tempDTO.setPaymentVoucherImageUrl2(paymentVoucherImageUrl2);
				serviceOrderDto.setPaymentVoucherImageUrl2(paymentVoucherImageUrl2);
			}
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl3)){
				tempDTO.setPaymentVoucherImageUrl3(paymentVoucherImageUrl3);
				serviceOrderDto.setPaymentVoucherImageUrl3(paymentVoucherImageUrl3);
			}
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl4)){
				tempDTO.setPaymentVoucherImageUrl4(paymentVoucherImageUrl4);
				serviceOrderDto.setPaymentVoucherImageUrl4(paymentVoucherImageUrl4);
			}
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl5)){
				tempDTO.setPaymentVoucherImageUrl5(paymentVoucherImageUrl5);
				serviceOrderDto.setPaymentVoucherImageUrl5(paymentVoucherImageUrl5);
			}


			if (serviceOrderDto.isSettle()){
				if (StringUtil.isNotEmpty(paymentVoucherImageUrl1) || StringUtil.isNotEmpty(paymentVoucherImageUrl2)
						|| StringUtil.isNotEmpty(paymentVoucherImageUrl3) || StringUtil.isNotEmpty(paymentVoucherImageUrl4)
						|| StringUtil.isNotEmpty(paymentVoucherImageUrl5)) {
				/*
				杜大哥（2021-10-18）说：【提前扣拥】在上传【invoice凭证】之后，申请月奖直接将服务订单状态改成【RECEIVED】(已收款凭证已提交)
				 */
					serviceOrderDto.setState("RECEIVED");
				} else
					return new Response(1, "请上传支付凭证");
			}else
				return new Response(1, "服务订单不是提前扣拥:" + serviceOrderDto.getId());

			//顾问在留学提前扣拥状态为COMPLETE时，不用上传发票凭证
			//if (StringUtil.isNotEmpty(invoiceVoucherImageUrl1))
			//	serviceOrderDto.setInvoiceVoucherImageUrl1(invoiceVoucherImageUrl1);
			//if (StringUtil.isNotEmpty(invoiceVoucherImageUrl2))
			//	serviceOrderDto.setInvoiceVoucherImageUrl2(invoiceVoucherImageUrl2);
			//if (StringUtil.isNotEmpty(invoiceVoucherImageUrl3))
			//	serviceOrderDto.setInvoiceVoucherImageUrl3(invoiceVoucherImageUrl3);
			//if (StringUtil.isNotEmpty(invoiceVoucherImageUrl4))
			//	serviceOrderDto.setInvoiceVoucherImageUrl4(invoiceVoucherImageUrl4);
			//if (StringUtil.isNotEmpty(invoiceVoucherImageUrl5))
			//	serviceOrderDto.setInvoiceVoucherImageUrl5(invoiceVoucherImageUrl5);
			
			if(commissionOrderService.addCommissionOrderTemp(tempDTO) > 0){
				if (serviceOrderService.updateServiceOrder(serviceOrderDto) > 0) // 同时更改服务订单状态
					return new Response(0, "success", tempDTO);
			}
			return new Response(1, "失败");
		} catch (ServiceException e) {
			return new Response(e.getCode(), e.getMessage(), null);
		}
	}

	@GetMapping(value = "/getCommissionOrderTempByServiceOrderId")
	@ResponseBody
	public Response getCommissionOrderTempByServiceOrderId(int id){
		try {
			return  new Response(0,"", commissionOrderService.getCommissionOrderTempByServiceOrderId(id));
		}catch (ServiceException e){
			return new Response(e.getCode(), e.getMessage(), null);
		}
	}


	public Response updateCommissionOrderTemp(
			/*
			//@RequestParam(value = "id")int id,
			@RequestParam(value = "installment") Integer installment,
			@RequestParam(value = "installmentDueDate1") String installmentDueDate1,
			@RequestParam(value = "installmentDueDate2", required = false) String installmentDueDate2,
			@RequestParam(value = "installmentDueDate3", required = false) String installmentDueDate3,
			@RequestParam(value = "installmentDueDate4", required = false) String installmentDueDate4,
			@RequestParam(value = "installmentDueDate5", required = false) String installmentDueDate5,
			@RequestParam(value = "installmentDueDate6", required = false) String installmentDueDate6,
			@RequestParam(value = "installmentDueDate7", required = false) String installmentDueDate7,
			@RequestParam(value = "installmentDueDate8", required = false) String installmentDueDate8,
			@RequestParam(value = "installmentDueDate9", required = false) String installmentDueDate9,
			@RequestParam(value = "installmentDueDate10", required = false) String installmentDueDate10,
			@RequestParam(value = "installmentDueDate11", required = false) String installmentDueDate11,
			@RequestParam(value = "installmentDueDate12", required = false) String installmentDueDate12,
			@RequestParam(value = "paymentVoucherImageUrl1", required = false) String paymentVoucherImageUrl1,
			@RequestParam(value = "paymentVoucherImageUrl2", required = false) String paymentVoucherImageUrl2,
			@RequestParam(value = "paymentVoucherImageUrl3", required = false) String paymentVoucherImageUrl3,
			@RequestParam(value = "paymentVoucherImageUrl4", required = false) String paymentVoucherImageUrl4,
			@RequestParam(value = "paymentVoucherImageUrl5", required = false) String paymentVoucherImageUrl5,
			@RequestParam(value = "invoiceVoucherImageUrl1", required = false) String invoiceVoucherImageUrl1,
			@RequestParam(value = "invoiceVoucherImageUrl2", required = false) String invoiceVoucherImageUrl2,
			@RequestParam(value = "invoiceVoucherImageUrl3", required = false) String invoiceVoucherImageUrl3,
			@RequestParam(value = "invoiceVoucherImageUrl4", required = false) String invoiceVoucherImageUrl4,
			@RequestParam(value = "invoiceVoucherImageUrl5", required = false) String invoiceVoucherImageUrl5,
			@RequestParam(value = "dob",required = false) String dob, @RequestParam(value = "startDate",required = false) String startDate,
			@RequestParam(value = "endDate",required = false) String endDate, @RequestParam(value = "tuitionFee",required = false) String tuitionFee,
			@RequestParam(value = "perTermTuitionFee",required = false) String perTermTuitionFee,
			@RequestParam(value = "receiveTypeId",required = false) Integer receiveTypeId,
			@RequestParam(value = "receiveDate",required = false) String receiveDate,
			@RequestParam(value = "perAmount",required = false) String perAmount, @RequestParam(value = "amount",required = false) String amount,
			@RequestParam(value = "remarks", required = false) String remarks,
			@RequestParam(value = "studentCode",required = false) String studentCode,
			@RequestParam(value = "serviceOrderId")int serviceOrderId,
			@RequestParam(value = "expectAmount",required = false)String expectAmount,
			@RequestParam(value = "verifyCode", required = false) String verifyCode,
			HttpServletRequest request, HttpServletResponse response
			 */
			Integer installment, String installmentDueDate1, String installmentDueDate2, String installmentDueDate3,
			String installmentDueDate4, String installmentDueDate5, String installmentDueDate6, String installmentDueDate7,
			String installmentDueDate8, String installmentDueDate9, String installmentDueDate10, String installmentDueDate11,
			String installmentDueDate12, String paymentVoucherImageUrl1, String paymentVoucherImageUrl2,
			String paymentVoucherImageUrl3, String paymentVoucherImageUrl4, String paymentVoucherImageUrl5, String invoiceVoucherImageUrl1,
			String invoiceVoucherImageUrl2, String invoiceVoucherImageUrl3, String invoiceVoucherImageUrl4, String invoiceVoucherImageUrl5,
			String dob, String startDate, String endDate, String tuitionFee, String perTermTuitionFee, Integer receiveTypeId,
			String receiveDate, String perAmount, String amount, String remarks, String studentCode, int serviceOrderId,
			String expectAmount, String currency, String exchangeRate, String verifyCode,CommissionOrderTempDTO tempDTO,ServiceOrderDTO serviceOrderDto
	){
		try {
			/*
			super.setPostHeader(response);
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo == null || (!"SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())
					&& !"GW".equalsIgnoreCase(adminUserLoginInfo.getApList())))
				return new Response(1, "仅限顾问和超级管理员能修改.", null);
			ServiceOrderDTO serviceOrderDto = serviceOrderService.getServiceOrderById(serviceOrderId);
			if (serviceOrderDto == null)
				return new Response(1, "服务订单(ID:" + serviceOrderId + ")不存在!", null);

			CommissionOrderTempDTO tempDTO = commissionOrderService.getCommissionOrderTempByServiceOrderId(serviceOrderId);
			if (tempDTO == null)
				return new Response(1,"此条记录不存在，修改失败");

			if (serviceOrderDto.getSubagencyId() <= 0)
				return new Response(1,
						"SubagencyId(" + serviceOrderDto.getSubagencyId() + ")不存在!", null);
			 */

			if (StringUtil.isNotEmpty(studentCode))
				tempDTO.setStudentCode(studentCode);
			if (StringUtil.isNotEmpty(dob))
				tempDTO.setDob(new Date(Long.parseLong(dob)));
			if (StringUtil.isNotEmpty(startDate))
				tempDTO.setStartDate(new Date(Long.parseLong(startDate)));
			if (StringUtil.isNotEmpty(endDate))
				tempDTO.setEndDate(new Date(Long.parseLong(endDate)));
			if (StringUtil.isNotEmpty(tuitionFee))
				tempDTO.setTuitionFee(Double.parseDouble(tuitionFee));
			if (StringUtil.isNotEmpty(perTermTuitionFee))
				tempDTO.setPerTermTuitionFee(Double.parseDouble(perTermTuitionFee));
			if (receiveTypeId != null)
				tempDTO.setReceiveTypeId(receiveTypeId);
			if (StringUtil.isNotEmpty(receiveDate))
				tempDTO.setReceiveDate(new Date(Long.parseLong(receiveDate)));
			if (StringUtil.isNotEmpty(perAmount))
				tempDTO.setPerAmount(Double.parseDouble(perAmount));
			if (StringUtil.isNotEmpty(amount))
				tempDTO.setAmount(Double.parseDouble(amount));
			if (StringUtil.isNotEmpty(expectAmount))
				tempDTO.setExpectAmount(Double.parseDouble(expectAmount));
			if (tempDTO.getPerAmount() < tempDTO.getAmount())
				return new Response(1, "本次应收款(" + tempDTO.getPerAmount()
						+ ")不能小于本次已收款(" + tempDTO.getAmount() + ")!", null);
			tempDTO.setDiscount(tempDTO.getPerAmount() - tempDTO.getAmount());
			if (StringUtil.isNotEmpty(verifyCode))
				tempDTO.setVerifyCode(verifyCode);
			if (StringUtil.isNotEmpty(remarks))
				tempDTO.setRemarks(remarks);

			if (installment != null && installment > 0)
				tempDTO.setInstallment(installment);

			if (StringUtil.isNotEmpty(installmentDueDate1))
				tempDTO.setInstallmentDueDate1(new Date(Long.parseLong(installmentDueDate1)));
			if (StringUtil.isNotEmpty(installmentDueDate2))
				tempDTO.setInstallmentDueDate2(new Date(Long.parseLong(installmentDueDate2)));
			else
				tempDTO.setInstallmentDueDate2(null);
			if (StringUtil.isNotEmpty(installmentDueDate3))
				tempDTO.setInstallmentDueDate3(new Date(Long.parseLong(installmentDueDate3)));
			else
				tempDTO.setInstallmentDueDate3(null);
			if (StringUtil.isNotEmpty(installmentDueDate4))
				tempDTO.setInstallmentDueDate4(new Date(Long.parseLong(installmentDueDate4)));
			else
				tempDTO.setInstallmentDueDate4(null);
			if (StringUtil.isNotEmpty(installmentDueDate5))
				tempDTO.setInstallmentDueDate5(new Date(Long.parseLong(installmentDueDate5)));
			else
				tempDTO.setInstallmentDueDate5(null);
			if (StringUtil.isNotEmpty(installmentDueDate6))
				tempDTO.setInstallmentDueDate6(new Date(Long.parseLong(installmentDueDate6)));
			else
				tempDTO.setInstallmentDueDate6(null);
			if (StringUtil.isNotEmpty(installmentDueDate7))
				tempDTO.setInstallmentDueDate7(new Date(Long.parseLong(installmentDueDate7)));
			else
				tempDTO.setInstallmentDueDate7(null);
			if (StringUtil.isNotEmpty(installmentDueDate8))
				tempDTO.setInstallmentDueDate8(new Date(Long.parseLong(installmentDueDate8)));
			else
				tempDTO.setInstallmentDueDate8(null);
			if (StringUtil.isNotEmpty(installmentDueDate9))
				tempDTO.setInstallmentDueDate9(new Date(Long.parseLong(installmentDueDate9)));
			else
				tempDTO.setInstallmentDueDate9(null);
			if (StringUtil.isNotEmpty(installmentDueDate10))
				tempDTO.setInstallmentDueDate10(new Date(Long.parseLong(installmentDueDate10)));
			else
				tempDTO.setInstallmentDueDate10(null);
			if (StringUtil.isNotEmpty(installmentDueDate11))
				tempDTO.setInstallmentDueDate11(new Date(Long.parseLong(installmentDueDate11)));
			else
				tempDTO.setInstallmentDueDate11(null);
			if (StringUtil.isNotEmpty(installmentDueDate12))
				tempDTO.setInstallmentDueDate12(new Date(Long.parseLong(installmentDueDate12)));
			else
				tempDTO.setInstallmentDueDate12(null);
			
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl1)){
				tempDTO.setPaymentVoucherImageUrl1(paymentVoucherImageUrl1);
				serviceOrderDto.setPaymentVoucherImageUrl1(paymentVoucherImageUrl1);
			}
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl2)){
				tempDTO.setPaymentVoucherImageUrl2(paymentVoucherImageUrl2);
				serviceOrderDto.setPaymentVoucherImageUrl2(paymentVoucherImageUrl2);
			}
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl3)){
				tempDTO.setPaymentVoucherImageUrl3(paymentVoucherImageUrl3);
				serviceOrderDto.setPaymentVoucherImageUrl3(paymentVoucherImageUrl3);
			}
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl4)){
				tempDTO.setPaymentVoucherImageUrl4(paymentVoucherImageUrl4);
				serviceOrderDto.setPaymentVoucherImageUrl4(paymentVoucherImageUrl4);
			}
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl5)){
				tempDTO.setPaymentVoucherImageUrl5(paymentVoucherImageUrl5);
				serviceOrderDto.setPaymentVoucherImageUrl5(paymentVoucherImageUrl5);
			}

			if (serviceOrderDto.isSettle()){
				if (StringUtil.isNotEmpty(paymentVoucherImageUrl1) || StringUtil.isNotEmpty(paymentVoucherImageUrl2)
						|| StringUtil.isNotEmpty(paymentVoucherImageUrl3) || StringUtil.isNotEmpty(paymentVoucherImageUrl4)
						|| StringUtil.isNotEmpty(paymentVoucherImageUrl5)) {
				/*
				杜大哥（2021-10-18）说：【提前扣拥】在上传【invoice凭证】之后，申请月奖直接将服务订单状态改成【RECEIVED】(已收款凭证已提交)
				 */
					serviceOrderDto.setState("RECEIVED");
				} else
					return new Response(1, "请上传支付凭证");
			}else
				return new Response(1, "服务订单不是提前扣拥:" + serviceOrderDto.getId());

			//顾问在留学提前扣拥状态为COMPLETE时，不用上传发票凭证
			//if (StringUtil.isNotEmpty(invoiceVoucherImageUrl1))
			//	serviceOrderDto.setInvoiceVoucherImageUrl1(invoiceVoucherImageUrl1);
			//if (StringUtil.isNotEmpty(invoiceVoucherImageUrl2))
			//	serviceOrderDto.setInvoiceVoucherImageUrl2(invoiceVoucherImageUrl2);
			//if (StringUtil.isNotEmpty(invoiceVoucherImageUrl3))
			//	serviceOrderDto.setInvoiceVoucherImageUrl3(invoiceVoucherImageUrl3);
			//if (StringUtil.isNotEmpty(invoiceVoucherImageUrl4))
			//	serviceOrderDto.setInvoiceVoucherImageUrl4(invoiceVoucherImageUrl4);
			//if (StringUtil.isNotEmpty(invoiceVoucherImageUrl5))
			//	serviceOrderDto.setInvoiceVoucherImageUrl5(invoiceVoucherImageUrl5);
			
			if (commissionOrderService.updateCommissionOrderTemp(tempDTO) > 0){
				if (serviceOrderService.updateServiceOrder(serviceOrderDto) > 0 )
					return new Response(0,"success",tempDTO);
			}
			return new Response(1,"更新失败");
		}catch (ServiceException e) {
			return new Response(e.getCode(), e.getMessage(), null);
		}

	}
}
