package org.zhinanzhen.b.controller;

import java.io.IOException;
import java.math.BigDecimal;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.zhinanzhen.b.service.CommissionOrderService;
import org.zhinanzhen.b.service.SchoolService;
import org.zhinanzhen.b.service.SubagencyService;
import org.zhinanzhen.b.service.pojo.CommissionOrderDTO;
import org.zhinanzhen.b.service.pojo.CommissionOrderListDTO;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.b.service.pojo.SubagencyDTO;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

import com.ikasoa.core.utils.StringUtil;

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

	@RequestMapping(value = "/upload_img", method = RequestMethod.POST)
	@ResponseBody
	public Response<String> uploadImage(@RequestParam MultipartFile file, HttpServletRequest request,
			HttpServletResponse response) throws IllegalStateException, IOException {
		super.setPostHeader(response);
		return super.upload(file, request.getSession(), "/uploads/payment_voucher_image_url/");
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
			@RequestParam(value = "paymentVoucherImageUrl1", required = false) String paymentVoucherImageUrl1,
			@RequestParam(value = "paymentVoucherImageUrl2", required = false) String paymentVoucherImageUrl2,
			@RequestParam(value = "startDate") String startDate, @RequestParam(value = "endDate") String endDate,
			@RequestParam(value = "tuitionFee") String tuitionFee,
			@RequestParam(value = "perTermTuitionFee") String perTermTuitionFee,
			@RequestParam(value = "receiveTypeId") Integer receiveTypeId,
			@RequestParam(value = "receiveDate") String receiveDate,
			@RequestParam(value = "perAmount") String perAmount, @RequestParam(value = "amount") String amount,
			@RequestParam(value = "bonusDate", required = false) String bonusDate,
			@RequestParam(value = "remarks", required = false) String remarks, HttpServletRequest request,
			HttpServletResponse response) {

		try {
			super.setPostHeader(response);
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo == null || (StringUtil.isNotEmpty(adminUserLoginInfo.getApList())
					&& !"GW".equalsIgnoreCase(adminUserLoginInfo.getApList())))
				return new Response<List<CommissionOrderDTO>>(1, "仅顾问和超级管理员能创建佣金订单.", null);
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
			else
				commissionOrderDto.setState(ReviewKjStateEnum.PENDING.toString());
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
			if (StringUtil.isNotEmpty(remarks))
				commissionOrderDto.setRemarks(remarks);

			SubagencyDTO subagencyDto = subagencyService.getSubagencyById(serviceOrderDto.getSubagencyId());
			if (subagencyDto == null)
				return new Response<List<CommissionOrderDTO>>(1,
						"Subagency(" + serviceOrderDto.getSubagencyId() + ")不存在!", null);
			// 佣金
			commissionOrderDto.setCommission(commissionOrderDto.getAmount());
			// 预收业绩
			Double expectAmount = commissionOrderDto.getAmount() * subagencyDto.getCommissionRate() * 1.1;
			commissionOrderDto.setExpectAmount(expectAmount);
			// GST
			commissionOrderDto
					.setGst(new BigDecimal(expectAmount / 11).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
			// Deduct GST
			commissionOrderDto.setDeductGst(new BigDecimal(expectAmount - commissionOrderDto.getGst())
					.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
			// Bonus
			commissionOrderDto.setBonus(new BigDecimal(commissionOrderDto.getDeductGst() * 0.1)
					.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());

			for (int installmentNum = 1; installmentNum <= installment; installmentNum++) {
				commissionOrderDto.setInstallmentNum(installmentNum);
				if (installmentNum == 1 && installmentDueDate1 != null)
					commissionOrderDto.setInstallmentDueDate(new Date(Long.parseLong(installmentDueDate1)));
				else if (installmentNum == 2 && installmentDueDate2 != null)
					commissionOrderDto.setInstallmentDueDate(new Date(Long.parseLong(installmentDueDate2)));
				else if (installmentNum == 3 && installmentDueDate3 != null)
					commissionOrderDto.setInstallmentDueDate(new Date(Long.parseLong(installmentDueDate3)));
				else
					break;
				if (commissionOrderService.addCommissionOrder(commissionOrderDto) > 0)
					commissionOrderDtoList.add(commissionOrderDto);
			}
			serviceOrderDto.setSubmitted(true);
			serviceOrderService.updateServiceOrder(serviceOrderDto); // 同时更改服务订单状态
			return new Response<List<CommissionOrderDTO>>(0, commissionOrderDtoList);
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
			@RequestParam(value = "startDate", required = false) String startDate,
			@RequestParam(value = "endDate", required = false) String endDate,
			@RequestParam(value = "tuitionFee", required = false) String tuitionFee,
			@RequestParam(value = "perTermTuitionFee", required = false) String perTermTuitionFee,
			@RequestParam(value = "receiveTypeId", required = false) Integer receiveTypeId,
			@RequestParam(value = "receiveDate", required = false) String receiveDate,
			@RequestParam(value = "perAmount", required = false) String perAmount,
			@RequestParam(value = "amount", required = false) String amount,
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
			CommissionOrderDTO commissionOrderDto = new CommissionOrderDTO();
			commissionOrderDto.setId(id);
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
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl1))
				commissionOrderDto.setPaymentVoucherImageUrl1(paymentVoucherImageUrl1);
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl2))
				commissionOrderDto.setPaymentVoucherImageUrl2(paymentVoucherImageUrl2);
			if (startDate != null)
				commissionOrderDto.setStartDate(new Date(Long.parseLong(startDate)));
			if (endDate != null)
				commissionOrderDto.setEndDate(new Date(Long.parseLong(endDate)));
			if (StringUtil.isNotEmpty(tuitionFee))
				commissionOrderDto.setTuitionFee(Double.parseDouble(tuitionFee));
			if (StringUtil.isNotEmpty(perTermTuitionFee))
				commissionOrderDto.setPerTermTuitionFee(Double.parseDouble(perTermTuitionFee));
			if (receiveTypeId != null)
				commissionOrderDto.setReceiveTypeId(receiveTypeId);
			if (StringUtil.isNotEmpty(receiveDate))
				commissionOrderDto.setReceiveDate(new Date(Long.parseLong(receiveDate)));
			if (StringUtil.isNotEmpty(perAmount))
				commissionOrderDto.setPerAmount(Double.parseDouble(perAmount));
			if (StringUtil.isNotEmpty(amount))
				commissionOrderDto.setAmount(Double.parseDouble(amount));
			double _perAmount = commissionOrderListDto.getPerAmount();
			if (commissionOrderDto.getPerAmount() > 0)
				_perAmount = commissionOrderDto.getPerAmount();
			if (_perAmount < commissionOrderDto.getAmount())
				return new Response<CommissionOrderDTO>(1,
						"本次应收款(" + _perAmount + ")不能小于本次已收款(" + commissionOrderDto.getAmount() + ")!", null);
			commissionOrderDto.setDiscount(_perAmount - commissionOrderDto.getAmount());
			if (StringUtil.isNotEmpty(remarks))
				commissionOrderDto.setRemarks(remarks);

			SubagencyDTO subagencyDto = subagencyService.getSubagencyById(serviceOrderDto.getSubagencyId());
			if (subagencyDto == null)
				return new Response<CommissionOrderDTO>(1, "Subagency(" + serviceOrderDto.getSubagencyId() + ")不存在!",
						null);
			// 佣金
			commissionOrderDto.setCommission(commissionOrderDto.getAmount());
			// 预收业绩
			Double expectAmount = commissionOrderDto.getAmount() * subagencyDto.getCommissionRate() * 1.1;
			commissionOrderDto.setExpectAmount(expectAmount);
			// GST
			commissionOrderDto
					.setGst(new BigDecimal(expectAmount / 11).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
			// Deduct GST
			commissionOrderDto.setDeductGst(new BigDecimal(expectAmount - commissionOrderDto.getGst())
					.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
			// Bonus
			commissionOrderDto.setBonus(new BigDecimal(commissionOrderDto.getDeductGst() * 0.1)
					.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());

			return commissionOrderService.updateCommissionOrder(commissionOrderDto) > 0
					? new Response<CommissionOrderDTO>(0, commissionOrderDto)
					: new Response<CommissionOrderDTO>(1, "修改失败.", null);
		} catch (ServiceException e) {
			return new Response<CommissionOrderDTO>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/kjUpdate", method = RequestMethod.POST)
	@ResponseBody
	public Response<CommissionOrderDTO> kjUpdate(@RequestParam(value = "id") int id,
			@RequestParam(value = "schoolPaymentAmount", required = false) String schoolPaymentAmount,
			@RequestParam(value = "schoolPaymentDate", required = false) String schoolPaymentDate,
			@RequestParam(value = "invoiceNumber", required = false) String invoiceNumber,
			@RequestParam(value = "bonus", required = false) String bonus,
			@RequestParam(value = "bonusDate", required = false) String bonusDate, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo != null)
				if (adminUserLoginInfo == null || (StringUtil.isNotEmpty(adminUserLoginInfo.getApList())
						&& !"KJ".equalsIgnoreCase(adminUserLoginInfo.getApList())))
					return new Response<CommissionOrderDTO>(1, "仅限会计修改.", null);
			CommissionOrderDTO commissionOrderDto = commissionOrderService.getCommissionOrderById(id);
			if (commissionOrderDto == null)
				return new Response<CommissionOrderDTO>(1, "留学佣金订单订单(ID:" + id + ")不存在!", null);
			if (StringUtil.isNotEmpty(schoolPaymentAmount))
				commissionOrderDto.setSchoolPaymentAmount(Double.parseDouble(schoolPaymentAmount));
			if (schoolPaymentDate != null)
				commissionOrderDto.setSchoolPaymentDate(new Date(Long.parseLong(schoolPaymentDate)));
			if (StringUtil.isNotEmpty(invoiceNumber))
				commissionOrderDto.setInvoiceNumber(invoiceNumber);
			if (bonus != null)
				commissionOrderDto.setBonus(Double.parseDouble(bonus));
			if (bonusDate != null)
				commissionOrderDto.setBonusDate(new Date(Long.parseLong(bonusDate)));
			if (bonus != null || bonusDate != null) {
				commissionOrderDto.setState(ReviewKjStateEnum.COMPLETE.toString());
				commissionOrderDto.setCommissionState(CommissionStateEnum.YJY.toString());
			} else {
				commissionOrderDto.setState(ReviewKjStateEnum.REVIEW.toString());
				commissionOrderDto.setCommissionState(CommissionStateEnum.YZY.toString());
			}
			return commissionOrderService.updateCommissionOrder(commissionOrderDto) > 0
					? new Response<CommissionOrderDTO>(0, commissionOrderDto)
					: new Response<CommissionOrderDTO>(1, "修改失败.", null);
		} catch (ServiceException e) {
			return new Response<CommissionOrderDTO>(e.getCode(), e.getMessage(), null);
		}
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
				return new Response<CommissionOrderDTO>(0, "修改成功.", commissionOrderListDto);
			else if (i == -1)
				return new Response<CommissionOrderDTO>(1, "修改失败. (佣金记录不存在)", null);
			else if (i == -2)
				return new Response<CommissionOrderDTO>(2, "修改失败. (学校佣金设置不存在或不正确)", null);
			else if (i == -3)
				return new Response<CommissionOrderDTO>(3, "修改失败. (佣金办理时间不在设置合同时间范围内)", null);
			else
				return new Response<CommissionOrderDTO>(4, "修改失败.", null);
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
			CommissionOrderDTO commissionOrderDto = new CommissionOrderDTO();
			commissionOrderDto.setId(id);
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
	public Response<Integer> count(@RequestParam(value = "maraId", required = false) Integer maraId,
			@RequestParam(value = "adviserId", required = false) Integer adviserId,
			@RequestParam(value = "officialId", required = false) Integer officialId,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "phone", required = false) String phone,
			@RequestParam(value = "wechatUsername", required = false) String wechatUsername,
			@RequestParam(value = "schoolId", required = false) Integer schoolId,
			@RequestParam(value = "isSettle", required = false) Boolean isSettle,
			@RequestParam(value = "state", required = false) String state,
			@RequestParam(value = "commissionState", required = false) String commissionState,
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

		// 会计角色过滤状态
		List<String> stateList = new ArrayList<>();
		if (state == null && getKjId(request) != null) {
			stateList.add(ReviewKjStateEnum.REVIEW.toString());
			stateList.add(ReviewKjStateEnum.FINISH.toString());
			stateList.add(ReviewKjStateEnum.COMPLETE.toString());
			stateList.add(ReviewKjStateEnum.CLOSE.toString());
		} else if (state == null)
			stateList = null;
		else
			stateList.add(state);

		List<String> commissionStateList = null;
		if (StringUtil.isNotEmpty(commissionState))
			commissionStateList = Arrays.asList(commissionState.split(","));

		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, commissionOrderService.countCommissionOrder(maraId, adviserId, officialId,
					name, phone, wechatUsername, schoolId, isSettle, stateList, commissionStateList));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<CommissionOrderListDTO>> list(@RequestParam(value = "maraId", required = false) Integer maraId,
			@RequestParam(value = "adviserId", required = false) Integer adviserId,
			@RequestParam(value = "officialId", required = false) Integer officialId,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "phone", required = false) String phone,
			@RequestParam(value = "wechatUsername", required = false) String wechatUsername,
			@RequestParam(value = "schoolId", required = false) Integer schoolId,
			@RequestParam(value = "isSettle", required = false) Boolean isSettle,
			@RequestParam(value = "state", required = false) String state,
			@RequestParam(value = "commissionState", required = false) String commissionState,
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

		// 会计角色过滤状态
		List<String> stateList = new ArrayList<>();
		if (state == null && getKjId(request) != null) {
			stateList.add(ReviewKjStateEnum.REVIEW.toString());
			stateList.add(ReviewKjStateEnum.FINISH.toString());
			stateList.add(ReviewKjStateEnum.COMPLETE.toString());
			stateList.add(ReviewKjStateEnum.CLOSE.toString());
		} else if (state == null)
			stateList = null;
		else
			stateList.add(state);

		List<String> commissionStateList = null;
		if (StringUtil.isNotEmpty(commissionState))
			commissionStateList = Arrays.asList(commissionState.split(","));

		try {
			super.setGetHeader(response);
			return new Response<List<CommissionOrderListDTO>>(0,
					commissionOrderService.listCommissionOrder(maraId, adviserId, officialId, name, phone,
							wechatUsername, schoolId, isSettle, stateList, commissionStateList, pageNum, pageSize));
		} catch (ServiceException e) {
			return new Response<List<CommissionOrderListDTO>>(1, e.getMessage(), null);
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

}
