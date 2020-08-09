package org.zhinanzhen.b.controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.zhinanzhen.b.service.CommissionOrderService;
import org.zhinanzhen.b.service.SchoolService;
import org.zhinanzhen.b.service.ServiceOrderService;
import org.zhinanzhen.b.service.SubagencyService;
import org.zhinanzhen.b.service.pojo.CommissionOrderCommentDTO;
import org.zhinanzhen.b.service.pojo.CommissionOrderDTO;
import org.zhinanzhen.b.service.pojo.CommissionOrderListDTO;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

import com.ikasoa.core.utils.StringUtil;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WriteException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
	ServiceOrderService serviceOrderService;
	
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@RequestMapping(value = "/upload_img", method = RequestMethod.POST)
	@ResponseBody
	public Response<String> uploadImage(@RequestParam MultipartFile file, HttpServletRequest request,
			HttpServletResponse response) throws IllegalStateException, IOException {
		super.setPostHeader(response);
		return super.upload2(file, request.getSession(), "/uploads/payment_voucher_image_url/");
	}

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	public Response<List<CommissionOrderDTO>> add(@RequestParam(value = "serviceOrderId") Integer serviceOrderId,
			@RequestParam(value = "state", required = false) String state,
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
			@RequestParam(value = "paymentVoucherImageUrl1", required = false) String paymentVoucherImageUrl1,
			@RequestParam(value = "paymentVoucherImageUrl2", required = false) String paymentVoucherImageUrl2,
			@RequestParam(value = "paymentVoucherImageUrl3", required = false) String paymentVoucherImageUrl3,
			@RequestParam(value = "paymentVoucherImageUrl4", required = false) String paymentVoucherImageUrl4,
			@RequestParam(value = "paymentVoucherImageUrl5", required = false) String paymentVoucherImageUrl5,
			@RequestParam(value = "startDate") String startDate, @RequestParam(value = "endDate") String endDate,
			@RequestParam(value = "tuitionFee") String tuitionFee,
			@RequestParam(value = "perTermTuitionFee") String perTermTuitionFee,
			@RequestParam(value = "receiveTypeId") Integer receiveTypeId,
			@RequestParam(value = "receiveDate") String receiveDate,
			@RequestParam(value = "perAmount") String perAmount, @RequestParam(value = "amount") String amount,
			@RequestParam(value = "bonusDate", required = false) String bonusDate,
			@RequestParam(value = "zyDate", required = false) String zyDate,
			@RequestParam(value = "remarks", required = false) String remarks, HttpServletRequest request,
			HttpServletResponse response) {

		try {
			super.setPostHeader(response);
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo == null || (StringUtil.isNotEmpty(adminUserLoginInfo.getApList())
					&& !"GW".equalsIgnoreCase(adminUserLoginInfo.getApList())))
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
			commissionOrderDto.setSchoolId(schoolId);
			commissionOrderDto.setStudentCode(studentCode);
			commissionOrderDto.setUserId(userId);
			commissionOrderDto.setAdviserId(adviserId);
			commissionOrderDto.setOfficialId(officialId);
			commissionOrderDto.setStudying(isStudying);
			commissionOrderDto.setInstallment(installment);
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl1))
				commissionOrderDto.setPaymentVoucherImageUrl1(paymentVoucherImageUrl1);
			else
				commissionOrderDto.setPaymentVoucherImageUrl1(serviceOrderDto.getPaymentVoucherImageUrl1());
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
			commissionOrderDto.setDiscount(commissionOrderDto.getPerAmount() - commissionOrderDto.getAmount());
			if (StringUtil.isNotEmpty(bonusDate))
				commissionOrderDto.setBonusDate(new Date(Long.parseLong(bonusDate)));
			if (StringUtil.isNotEmpty(zyDate))
				commissionOrderDto.setZyDate(new Date(Long.parseLong(zyDate)));
			if (StringUtil.isNotEmpty(remarks))
				commissionOrderDto.setRemarks(remarks);

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
					commissionOrderDto.setKjApprovalDate(new Date());
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
					} else
						break;
					commissionOrderDto.setState(ReviewKjStateEnum.PENDING.toString());
					commissionOrderDto.setPaymentVoucherImageUrl1(null);
					commissionOrderDto.setPaymentVoucherImageUrl2(null);
					commissionOrderDto.setPaymentVoucherImageUrl3(null);
					commissionOrderDto.setPaymentVoucherImageUrl4(null);
					commissionOrderDto.setPaymentVoucherImageUrl5(null);
				}
				int id = commissionOrderService.addCommissionOrder(commissionOrderDto);
				if (id > 0) {
					serviceOrderDto.setSubmitted(true);
					serviceOrderService.updateServiceOrder(serviceOrderDto); // 同时更改服务订单状态
					commissionOrderDtoList.add(commissionOrderDto);
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
			@RequestParam(value = "startDate", required = false) String startDate,
			@RequestParam(value = "endDate", required = false) String endDate,
			@RequestParam(value = "tuitionFee", required = false) String tuitionFee,
			@RequestParam(value = "perTermTuitionFee", required = false) String perTermTuitionFee,
			@RequestParam(value = "receiveTypeId", required = false) Integer receiveTypeId,
			@RequestParam(value = "receiveDate", required = false) String receiveDate,
			@RequestParam(value = "perAmount", required = false) String perAmount,
			@RequestParam(value = "amount", required = false) String amount,
			@RequestParam(value = "sureExpectAmount", required = false) Double sureExpectAmount,
			@RequestParam(value = "invoiceNumber", required = false) String invoiceNumber,
			@RequestParam(value = "zyDate", required = false) String zyDate,
			@RequestParam(value = "bankCheck", required = false) String bankCheck,
			@RequestParam(value = "isChecked", required = false) String isChecked,
			@RequestParam(value = "remarks", required = false) String remarks, HttpServletRequest request,
			HttpServletResponse response) {
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
			if (StringUtil.isNotEmpty(invoiceNumber))
				commissionOrderDto.setInvoiceNumber(invoiceNumber);
			if (StringUtil.isNotEmpty(zyDate))
				commissionOrderDto.setZyDate(new Date(Long.parseLong(zyDate)));
			if (StringUtil.isNotEmpty(bankCheck))
				commissionOrderDto.setBankCheck(bankCheck);
			commissionOrderDto.setChecked(isChecked != null && "true".equalsIgnoreCase(isChecked));
			if (StringUtil.isNotEmpty(remarks))
				commissionOrderDto.setRemarks(remarks);

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
				if (adminUserLoginInfo == null || (StringUtil.isNotEmpty(adminUserLoginInfo.getApList())
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

	@RequestMapping(value = "/update", method = RequestMethod.PUT)
	@ResponseBody
	public Response<Integer> update(@RequestBody List<BatchUpdateCommissionOrder> batchUpdateList,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo != null)
				if (adminUserLoginInfo == null || (StringUtil.isNotEmpty(adminUserLoginInfo.getApList())
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
				if (adminUserLoginInfo == null || (StringUtil.isNotEmpty(adminUserLoginInfo.getApList())
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
		if (isChangeState)
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
			int i = schoolService.updateSchoolSetting(commissionOrderListDto); // 根据学校设置更新佣金值
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
	public Response<Integer> count(@RequestParam(value = "regionId", required = false) Integer regionId,
			@RequestParam(value = "maraId", required = false) Integer maraId,
			@RequestParam(value = "adviserId", required = false) Integer adviserId,
			@RequestParam(value = "officialId", required = false) Integer officialId,
			@RequestParam(value = "userId", required = false) Integer userId,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "phone", required = false) String phone,
			@RequestParam(value = "wechatUsername", required = false) String wechatUsername,
			@RequestParam(value = "schoolId", required = false) Integer schoolId,
			@RequestParam(value = "isSettle", required = false) Boolean isSettle,
			@RequestParam(value = "state", required = false) String state,
			@RequestParam(value = "commissionState", required = false) String commissionState,
			@RequestParam(value = "startKjApprovalDate", required = false) String startKjApprovalDate,
			@RequestParam(value = "endKjApprovalDate", required = false) String endKjApprovalDate,
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

//		Date _startKjApprovalDate = null;
//		if (startKjApprovalDate != null)
//			_startKjApprovalDate = new Date(Long.parseLong(startKjApprovalDate));
//		Date _endKjApprovalDate = null;
//		if (endKjApprovalDate != null)
//			_endKjApprovalDate = new Date(Long.parseLong(endKjApprovalDate));

		try {
			super.setGetHeader(response);
			return new Response<Integer>(0,
					commissionOrderService.countCommissionOrder(regionId, maraId, adviserId, officialId, userId, name, phone,
							wechatUsername, schoolId, isSettle, stateList, commissionStateList, startKjApprovalDate,
							endKjApprovalDate, isYzyAndYjy));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<CommissionOrderListDTO>> list(
			@RequestParam(value = "regionId", required = false) Integer regionId,
			@RequestParam(value = "maraId", required = false) Integer maraId,
			@RequestParam(value = "adviserId", required = false) Integer adviserId,
			@RequestParam(value = "officialId", required = false) Integer officialId,
			@RequestParam(value = "userId", required = false) Integer userId,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "phone", required = false) String phone,
			@RequestParam(value = "wechatUsername", required = false) String wechatUsername,
			@RequestParam(value = "schoolId", required = false) Integer schoolId,
			@RequestParam(value = "isSettle", required = false) Boolean isSettle,
			@RequestParam(value = "state", required = false) String state,
			@RequestParam(value = "commissionState", required = false) String commissionState,
			@RequestParam(value = "startKjApprovalDate", required = false) String startKjApprovalDate,
			@RequestParam(value = "endKjApprovalDate", required = false) String endKjApprovalDate,
			@RequestParam(value = "pageNum") int pageNum, @RequestParam(value = "pageSize") int pageSize,
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

//		Date _startKjApprovalDate = null;
//		if (startKjApprovalDate != null)
//			_startKjApprovalDate = new Date(Long.parseLong(startKjApprovalDate));
//		Date _endKjApprovalDate = null;
//		if (endKjApprovalDate != null)
//			_endKjApprovalDate = new Date(Long.parseLong(endKjApprovalDate));

		try {
			super.setGetHeader(response);
			return new Response<List<CommissionOrderListDTO>>(0,
					commissionOrderService.listCommissionOrder(regionId, maraId, adviserId, officialId, userId, name,
							phone, wechatUsername, schoolId, isSettle, stateList, commissionStateList,
							startKjApprovalDate, endKjApprovalDate, isYzyAndYjy, pageNum, pageSize));
		} catch (ServiceException e) {
			return new Response<List<CommissionOrderListDTO>>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/upload", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> upload(@RequestParam MultipartFile file, HttpServletRequest request,
			HttpServletResponse response) throws IllegalStateException, IOException {
		super.setPostHeader(response);
		String message = "";
		int n = 0;
		Response<String> r = super.upload2(file, request.getSession(), "/tmp/");
		try (InputStream is = new FileInputStream("/data" + r.getData())) {
			jxl.Workbook wb = jxl.Workbook.getWorkbook(is);
			Sheet sheet = wb.getSheet(0);
			for (int i = 1; i < sheet.getRows(); i++) {
				Cell[] cells = sheet.getRow(i);
				String _id = cells[0].getContents();
				String _schoolPaymentAmount = cells[21].getContents();
				String _schoolPaymentDate = cells[23].getContents();
				String _invoiceNumber = cells[24].getContents();
				String _zyDate = cells[25].getContents();
				String _sureExpectAmount = cells[19].getContents();
				String _bonus = cells[27].getContents();
				String _bonusDate = cells[28].getContents();
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
									.equalsIgnoreCase(commissionOrderListDto.getCommissionState())) {
						message += "[" + _id + "]佣金订单状态不是待结佣或待追佣;";
						continue;
					}
					Response<CommissionOrderDTO> _r = updateOne(Integer.parseInt(_id),
							StringUtil.isEmpty(_schoolPaymentAmount) ? null
									: Double.parseDouble(_schoolPaymentAmount.trim()),
							StringUtil.isEmpty(_schoolPaymentDate) ? null : _schoolPaymentDate.trim(),
							_invoiceNumber, StringUtil.isEmpty(_zyDate) ? null : _zyDate.trim(),
							StringUtil.isEmpty(_sureExpectAmount) ? null : Double.parseDouble(_sureExpectAmount.trim()),
							StringUtil.isEmpty(_bonus) ? null : Double.parseDouble(_bonus.trim()),
							StringUtil.isEmpty(_bonusDate) ? null : _bonusDate.trim(), true);
					if (_r.getCode() > 0)
						message += "[" + _id + "]" + _r.getMessage() + ";";
					else
						n++;
				} catch (NumberFormatException | ServiceException e) {
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
	public void down(@RequestParam(value = "regionId", required = false) Integer regionId,
			@RequestParam(value = "maraId", required = false) Integer maraId,
			@RequestParam(value = "adviserId", required = false) Integer adviserId,
			@RequestParam(value = "officialId", required = false) Integer officialId,
			@RequestParam(value = "userId", required = false) Integer userId,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "phone", required = false) String phone,
			@RequestParam(value = "wechatUsername", required = false) String wechatUsername,
			@RequestParam(value = "schoolId", required = false) Integer schoolId,
			@RequestParam(value = "isSettle", required = false) Boolean isSettle,
			@RequestParam(value = "state", required = false) String state,
			@RequestParam(value = "commissionState", required = false) String commissionState,
			@RequestParam(value = "startKjApprovalDate", required = false) String startKjApprovalDate,
			@RequestParam(value = "endKjApprovalDate", required = false) String endKjApprovalDate,
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

		try {
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

			List<CommissionOrderListDTO> commissionOrderList = commissionOrderService.listCommissionOrder(regionId,
					maraId, adviserId, officialId, userId, name, phone, wechatUsername, schoolId, isSettle, stateList,
					commissionStateList, startKjApprovalDate, endKjApprovalDate, isYzyAndYjy, 0, 9999);

			OutputStream os = response.getOutputStream();
			jxl.Workbook wb;
			InputStream is;
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
				sheet.addCell(new Label(0, i, commissionOrderListDto.getId() + "", cellFormat));
				sheet.addCell(new Label(1, i, sdf.format(commissionOrderListDto.getGmtCreate()), cellFormat));
				if (commissionOrderListDto.getReceiveDate() != null)
					sheet.addCell(new Label(2, i, sdf.format(commissionOrderListDto.getReceiveDate()), cellFormat));
				if (commissionOrderListDto.getUser() != null)
					sheet.addCell(new Label(3, i, commissionOrderListDto.getUser().getName(), cellFormat));
				sheet.addCell(new Label(4, i, commissionOrderListDto.getStudentCode(), cellFormat));
				sheet.addCell(new Label(5, i, sdf.format(commissionOrderListDto.getBirthday()), cellFormat));
				if (commissionOrderListDto.getReceiveType() != null)
					sheet.addCell(new Label(6, i, commissionOrderListDto.getReceiveType().getName() + "", cellFormat));
				if (commissionOrderListDto.getService() != null)
					sheet.addCell(new Label(7, i, commissionOrderListDto.getService().getName(), cellFormat));
				sheet.addCell(new Label(8, i, commissionOrderListDto.isSettle() + "", cellFormat));
				if (commissionOrderListDto.getSchool() != null) {
					sheet.addCell(new Label(9, i, commissionOrderListDto.getSchool().getName() + "", cellFormat));
					sheet.addCell(new Label(10, i, commissionOrderListDto.getSchool().getSubject() + "", cellFormat));
				}
				if (commissionOrderListDto.getInstallmentDueDate() != null)
					sheet.addCell(
							new Label(11, i, sdf.format(commissionOrderListDto.getInstallmentDueDate()), cellFormat));
				if (commissionOrderListDto.getReceiveType() != null)
					sheet.addCell(new Label(12, i, commissionOrderListDto.getReceiveType().getName() + "", cellFormat));
				sheet.addCell(new Label(13, i, commissionOrderListDto.getTuitionFee() + "", cellFormat));
				sheet.addCell(new Label(14, i, commissionOrderListDto.getPerAmount() + "", cellFormat)); // .getPerTermTuitionFee()
				sheet.addCell(new Label(15, i, commissionOrderListDto.getTotalPerAmount() + "", cellFormat));
				sheet.addCell(new Label(16, i, commissionOrderListDto.getTotalAmount() + "", cellFormat));
				sheet.addCell(new Label(17, i, commissionOrderListDto.getAmount() + "", cellFormat));
				sheet.addCell(new Label(18, i, commissionOrderListDto.getExpectAmount() + "", cellFormat));
				if (commissionOrderListDto.isSettle())
					sheet.addCell(new Label(19, i, commissionOrderListDto.getExpectAmount() + "", cellFormat));
				else
					sheet.addCell(new Label(19, i, commissionOrderListDto.getSureExpectAmount() + "", cellFormat));
				sheet.addCell(new Label(20, i, commissionOrderListDto.getGst() + "", cellFormat));
				sheet.addCell(new Label(21, i, commissionOrderListDto.getDeductGst() + "", cellFormat));
				sheet.addCell(new Label(22, i, commissionOrderListDto.getSchoolPaymentAmount() + "", cellFormat));
				if (commissionOrderListDto.getSchoolPaymentDate() != null)
					sheet.addCell(
							new Label(23, i, sdf.format(commissionOrderListDto.getSchoolPaymentDate()), cellFormat));
				sheet.addCell(new Label(24, i, commissionOrderListDto.getInvoiceNumber(), cellFormat));
				if (commissionOrderListDto.getZyDate() != null)
					sheet.addCell(new Label(25, i, sdf.format(commissionOrderListDto.getZyDate()), cellFormat));
				if (commissionOrderListDto.getSubagency() != null)
					sheet.addCell(new Label(26, i, commissionOrderListDto.getSubagency().getName(), cellFormat));
				sheet.addCell(new Label(27, i, commissionOrderListDto.getBonus() + "", cellFormat));
				if (commissionOrderListDto.getBonusDate() != null)
					sheet.addCell(new Label(28, i, sdf.format(commissionOrderListDto.getBonusDate()), cellFormat));
				sheet.addCell(new Label(29, i, commissionOrderListDto.getBankCheck(), cellFormat));
				sheet.addCell(new Label(30, i, commissionOrderListDto.isChecked() + "", cellFormat));
				if (commissionOrderListDto.getAdviser() != null)
					sheet.addCell(new Label(31, i, commissionOrderListDto.getAdviser().getName(), cellFormat));
				if (commissionOrderListDto.getCommissionState() != null)
					sheet.addCell(
							new Label(32, i, getKjStateStr(commissionOrderListDto.getCommissionState()), cellFormat));
				if (commissionOrderListDto.getKjApprovalDate() != null)
					sheet.addCell(new Label(33, i, sdf.format(commissionOrderListDto.getKjApprovalDate()), cellFormat));
				sheet.addCell(new Label(34, i, commissionOrderListDto.getRemarks(), cellFormat));
				i++;
			}
			wbe.write();
			wbe.close();
		} catch (Exception e) {
			e.printStackTrace();
			return;
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
				if (adminUserLoginInfo == null || (StringUtil.isNotEmpty(adminUserLoginInfo.getApList())
						&& !"KJ".equalsIgnoreCase(adminUserLoginInfo.getApList())))
					return new Response<CommissionOrderListDTO>(1, "仅限会计审核佣金订单.", null);
				if (StringUtil.isEmpty(adminUserLoginInfo.getApList())
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
			@RequestParam(value = "state") String state, HttpServletRequest request, HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			if (ReviewKjStateEnum.COMPLETE.toString().equalsIgnoreCase(state)
					|| ReviewKjStateEnum.FINISH.toString().equalsIgnoreCase(state))
				return new Response<CommissionOrderListDTO>(1, "完成操作请调用'approval'接口.", null);
			// 审核
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo != null) {
				if (adminUserLoginInfo == null || (StringUtil.isNotEmpty(adminUserLoginInfo.getApList())
						&& !"KJ".equalsIgnoreCase(adminUserLoginInfo.getApList())))
					return new Response<CommissionOrderListDTO>(1, "仅限会计审核佣金订单.", null);
				if (StringUtil.isEmpty(adminUserLoginInfo.getApList())
						|| "KJ".equalsIgnoreCase(adminUserLoginInfo.getApList())) {
					if (ReviewKjStateEnum.get(state) != null) {
						CommissionOrderListDTO commissionOrderListDto = commissionOrderService
								.getCommissionOrderById(id);
						if (commissionOrderListDto == null)
							return new Response<CommissionOrderListDTO>(1, "佣金订单不存在!", null);
						serviceOrderService.refuse(id, adminUserLoginInfo.getId(), null, null, null,
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

	@RequestMapping(value = "/addComment", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> addComment(@RequestParam(value = "adminUserId", required = false) Integer adminUserId,
			@RequestParam(value = "commissionOrderId") Integer commissionOrderId,
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

}
