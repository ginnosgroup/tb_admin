package org.zhinanzhen.b.controller;

import java.util.ArrayList;
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
import org.zhinanzhen.b.controller.ServiceOrderController.ReviewKjStateEnum;
import org.zhinanzhen.b.service.CommissionOrderService;
import org.zhinanzhen.b.service.ServiceOrderService;
import org.zhinanzhen.b.service.SubagencyService;
import org.zhinanzhen.b.service.pojo.CommissionOrderDTO;
import org.zhinanzhen.b.service.pojo.CommissionOrderListDTO;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.b.service.pojo.SubagencyDTO;
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

	@Resource
	SubagencyService subagencyService;

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	public Response<List<CommissionOrderDTO>> add(@RequestParam(value = "serviceOrderId") Integer serviceOrderId,
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
			commissionOrderDto.setSettle(isSettle);
			commissionOrderDto.setDepositUser(isDepositUser);
			commissionOrderDto.setSchoolId(schoolId);
			commissionOrderDto.setStudentCode(studentCode);
			commissionOrderDto.setUserId(userId);
			commissionOrderDto.setAdviserId(adviserId);
			commissionOrderDto.setOfficialId(officialId);
			commissionOrderDto.setStudying(isStudying);
			commissionOrderDto.setInstallment(installment);
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
			// 佣金
			SubagencyDTO subagencyDto = subagencyService.getSubagencyById(serviceOrderDto.getSubagencyId());
			if (subagencyDto == null)
				return new Response<List<CommissionOrderDTO>>(1,
						"Subagency(" + serviceOrderDto.getSubagencyId() + ")不存在!", null);
			Double commission = commissionOrderDto.getAmount() * subagencyDto.getCommissionRate();
			// GST
			commissionOrderDto.setGst(commission / 11);
			// Deduct GST
			commissionOrderDto.setDeductGst(commission - commissionOrderDto.getGst());
			// Bonus
			commissionOrderDto.setBonus(commissionOrderDto.getDeductGst() * 0.1);
			// 预收业绩
			commissionOrderDto.setExpectAmount(commission * 1.1);

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
			return new Response<List<CommissionOrderDTO>>(0, commissionOrderDtoList);
		} catch (ServiceException e) {
			return new Response<List<CommissionOrderDTO>>(e.getCode(), e.getMessage(), null);
		}
	}

	// 顾问不能修改,财务能修改
	@RequestMapping(value = "/update", method = RequestMethod.POST)
	@ResponseBody
	public Response<CommissionOrderDTO> update(@RequestParam(value = "id") int id,
			@RequestParam(value = "isSettle", required = false) Boolean isSettle,
			@RequestParam(value = "isDepositUser", required = false) Boolean isDepositUser,
			@RequestParam(value = "schoolId", required = false) Integer schoolId,
			@RequestParam(value = "studentCode", required = false) String studentCode,
			@RequestParam(value = "adviserId", required = false) Integer adviserId,
			@RequestParam(value = "officialId", required = false) Integer officialId,
			@RequestParam(value = "isStudying", required = false) Boolean isStudying,
			@RequestParam(value = "installmentDueDate", required = false) String installmentDueDate,
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
			if (commissionOrderDto.getPerAmount() < commissionOrderDto.getAmount())
				return new Response<CommissionOrderDTO>(1, "本次应收款(" + commissionOrderDto.getPerAmount() + ")不能小于本次已收款("
						+ commissionOrderDto.getAmount() + ")!", null);
			commissionOrderDto.setDiscount(commissionOrderDto.getPerAmount() - commissionOrderDto.getAmount());
			if (StringUtil.isNotEmpty(remarks))
				commissionOrderDto.setRemarks(remarks);
			// 佣金
			SubagencyDTO subagencyDto = subagencyService.getSubagencyById(serviceOrderDto.getSubagencyId());
			if (subagencyDto == null)
				return new Response<CommissionOrderDTO>(1, "Subagency(" + serviceOrderDto.getSubagencyId() + ")不存在!",
						null);
			Double commission = commissionOrderDto.getAmount() * subagencyDto.getCommissionRate();
			// GST
			commissionOrderDto.setGst(commission / 11);
			// Deduct GST
			commissionOrderDto.setDeductGst(commission - commissionOrderDto.getGst());
			// Bonus
			commissionOrderDto.setBonus(commissionOrderDto.getDeductGst() * 0.1);
			// 预收业绩
			commissionOrderDto.setExpectAmount(commission * 1.1);
			return commissionOrderService.updateCommissionOrder(commissionOrderDto) > 0
					? new Response<CommissionOrderDTO>(0, commissionOrderDto)
					: new Response<CommissionOrderDTO>(1, "修改失败.", null);
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
			@RequestParam(value = "state", required = false) String state, HttpServletRequest request,
			HttpServletResponse response) {

		Integer newMaraId = getMaraId(request);
		if (newMaraId != null)
			maraId = newMaraId;
		Integer newAdviserId = getAdviserId(request);
		if (newAdviserId != null)
			adviserId = newAdviserId;
		Integer newOfficialId = getOfficialId(request);
		if (newOfficialId != null)
			officialId = newOfficialId;

		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, commissionOrderService.countCommissionOrder(maraId, adviserId, officialId,
					name, phone, wechatUsername, schoolId, isSettle, state));
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
			@RequestParam(value = "state", required = false) String state, @RequestParam(value = "pageNum") int pageNum,
			@RequestParam(value = "pageSize") int pageSize, HttpServletRequest request, HttpServletResponse response) {

		Integer newMaraId = getMaraId(request);
		if (newMaraId != null)
			maraId = newMaraId;
		Integer newAdviserId = getAdviserId(request);
		if (newAdviserId != null)
			adviserId = newAdviserId;
		Integer newOfficialId = getOfficialId(request);
		if (newOfficialId != null)
			officialId = newOfficialId;

		try {
			super.setGetHeader(response);
			return new Response<List<CommissionOrderListDTO>>(0, commissionOrderService.listCommissionOrder(maraId,
					adviserId, officialId, name, phone, wechatUsername, schoolId, isSettle, state, pageNum, pageSize));
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

}
