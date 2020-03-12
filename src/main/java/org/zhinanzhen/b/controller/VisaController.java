package org.zhinanzhen.b.controller;

import java.math.BigDecimal;
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
import org.zhinanzhen.b.service.ServiceOrderService;
import org.zhinanzhen.b.service.VisaService;
import org.zhinanzhen.b.service.pojo.VisaDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

import com.ikasoa.core.utils.StringUtil;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/visa")
public class VisaController extends BaseController {

	@Resource
	ServiceOrderService serviceOrderService;

	@Resource
	VisaService visaService;

	public enum ReviewKjStateEnum {
		PENDING, WAIT, REVIEW, FINISH, COMPLETE, CLOSE;
		public static ReviewKjStateEnum get(String name) {
			for (ReviewKjStateEnum e : ReviewKjStateEnum.values())
				if (e.toString().equals(name))
					return e;
			return null;
		}
	}

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	public Response<List<VisaDTO>> addVisa(@RequestParam(value = "userId", required = false) String userId,
			@RequestParam(value = "handlingDate") String handlingDate,
			@RequestParam(value = "receiveTypeId") String receiveTypeId,
			@RequestParam(value = "receiveDate") String receiveDate,
			@RequestParam(value = "serviceId") String serviceId,
			@RequestParam(value = "serviceOrderId") String serviceOrderId,
			@RequestParam(value = "installment") Integer installment,
			@RequestParam(value = "receivable") String receivable,
			@RequestParam(value = "received", required = false) String received,
			@RequestParam(value = "perAmount") String perAmount, @RequestParam(value = "amount") String amount,
			@RequestParam(value = "adviserId") String adviserId, @RequestParam(value = "maraId") String maraId,
			@RequestParam(value = "officialId") String officialId,
			@RequestParam(value = "remarks", required = false) String remarks, HttpServletRequest request,
			HttpServletResponse response) {

		// 更改当前顾问编号
		Integer newAdviserId = getAdviserId(request);
		if (newAdviserId != null)
			adviserId = newAdviserId + "";

		try {
			super.setPostHeader(response);
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo == null || (StringUtil.isNotEmpty(adminUserLoginInfo.getApList())
					&& !"GW".equalsIgnoreCase(adminUserLoginInfo.getApList())))
				return new Response<List<VisaDTO>>(1, "仅顾问和超级管理员能创建佣金订单.", null);
			List<VisaDTO> visaDtoList = new ArrayList<>();
			VisaDTO visaDto = new VisaDTO();
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
			if (StringUtil.isNotEmpty(serviceOrderId))
				visaDto.setServiceOrderId(Integer.parseInt(serviceOrderId));
			if (installment != null)
				visaDto.setInstallment(installment);
			if (StringUtil.isNotEmpty(receivable))
				visaDto.setReceivable(Double.parseDouble(receivable));
			if (StringUtil.isNotEmpty(received))
				visaDto.setReceived(Double.parseDouble(received));
			if (StringUtil.isNotEmpty(perAmount))
				visaDto.setPerAmount(Double.parseDouble(perAmount));
			if (StringUtil.isNotEmpty(amount))
				visaDto.setAmount(Double.parseDouble(amount));
			if (visaDto.getPerAmount() < visaDto.getAmount())
				return new Response<List<VisaDTO>>(1,
						"本次应收款(" + visaDto.getPerAmount() + ")不能小于本次已收款(" + visaDto.getAmount() + ")!", null);
			visaDto.setDiscount(visaDto.getPerAmount() - visaDto.getAmount());
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
			visaDto.setGst(commission / 11);
			visaDto.setDeductGst(commission - visaDto.getGst());
			visaDto.setBonus(visaDto.getDeductGst() * 0.1);
			visaDto.setExpectAmount(
					new BigDecimal(commission * 1.1).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());

			for (int installmentNum = 1; installmentNum <= installment; installmentNum++) {
				visaDto.setInstallmentNum(installmentNum);
				if (visaService.addVisa(visaDto) > 0)
					visaDtoList.add(visaDto);
			}
			return new Response<List<VisaDTO>>(0, visaDtoList);
		} catch (ServiceException e) {
			return new Response<List<VisaDTO>>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	@ResponseBody
	public Response<VisaDTO> updateVisa(@RequestParam(value = "id") int id,
			@RequestParam(value = "userId", required = false) String userId,
			@RequestParam(value = "handlingDate", required = false) String handlingDate,
			@RequestParam(value = "receiveTypeId", required = false) String receiveTypeId,
			@RequestParam(value = "receiveDate", required = false) String receiveDate,
			@RequestParam(value = "serviceId", required = false) String serviceId,
			@RequestParam(value = "serviceOrderId", required = false) String serviceOrderId,
			@RequestParam(value = "receivable", required = false) String receivable,
			@RequestParam(value = "received", required = false) String received,
			@RequestParam(value = "perAmount", required = false) String perAmount,
			@RequestParam(value = "amount", required = false) String amount,
			@RequestParam(value = "adviserId", required = false) String adviserId,
			@RequestParam(value = "maraId", required = false) String maraId,
			@RequestParam(value = "officialId", required = false) String officialId,
			@RequestParam(value = "remarks", required = false) String remarks, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			VisaDTO _visaDto = visaService.getVisaById(id);
			VisaDTO visaDto = new VisaDTO();
			visaDto.setId(id);
			if (StringUtil.isNotEmpty(handlingDate)) {
				visaDto.setHandlingDate(new Date(Long.parseLong(handlingDate)));
			}
			if (StringUtil.isNotEmpty(userId)) {
				visaDto.setUserId(Integer.parseInt(userId));
			}
			if (StringUtil.isNotEmpty(receiveTypeId)) {
				visaDto.setReceiveTypeId(Integer.parseInt(receiveTypeId));
			}
			if (StringUtil.isNotEmpty(receiveDate)) {
				visaDto.setReceiveDate(new Date(Long.parseLong(receiveDate)));
			}
			if (StringUtil.isNotEmpty(serviceId)) {
				visaDto.setServiceId(Integer.parseInt(serviceId));
			}
			if (StringUtil.isNotEmpty(serviceOrderId))
				visaDto.setServiceOrderId(Integer.parseInt(serviceOrderId));
			if (StringUtil.isNotEmpty(receivable)) {
				visaDto.setReceivable(Double.parseDouble(receivable));
			}
			if (StringUtil.isNotEmpty(received)) {
				visaDto.setReceived(Double.parseDouble(received));
			}
			if (StringUtil.isNotEmpty(perAmount))
				visaDto.setPerAmount(Double.parseDouble(perAmount));
			if (StringUtil.isNotEmpty(amount)) {
				visaDto.setAmount(Double.parseDouble(amount));
			}
			double _perAmount = _visaDto.getPerAmount();
			if (visaDto.getPerAmount() > 0)
				_perAmount = visaDto.getPerAmount();
			if (_perAmount < visaDto.getAmount())
				return new Response<VisaDTO>(1, "本次应收款(" + _perAmount + ")不能小于本次已收款(" + visaDto.getAmount() + ")!",
						null);
			visaDto.setDiscount(_perAmount - visaDto.getAmount());
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
			visaDto.setGst(commission / 11);
			visaDto.setDeductGst(commission - visaDto.getGst());
			visaDto.setBonus(visaDto.getDeductGst() * 0.1);
			visaDto.setExpectAmount(
					new BigDecimal(commission * 1.1).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
			if (visaService.updateVisa(visaDto) > 0) {
				return new Response<VisaDTO>(0, visaDto);
			} else {
				return new Response<VisaDTO>(1, "修改失败.", null);
			}
		} catch (ServiceException e) {
			return new Response<VisaDTO>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/count", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> countVisa(@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "startHandlingDate", required = false) String startHandlingDate,
			@RequestParam(value = "endHandlingDate", required = false) String endHandlingDate,
			@RequestParam(value = "startDate", required = false) String startDate,
			@RequestParam(value = "endDate", required = false) String endDate,
			@RequestParam(value = "adviserId", required = false) Integer adviserId,
			@RequestParam(value = "userId", required = false) Integer userId, HttpServletRequest request,
			HttpServletResponse response) {

		// 更改当前顾问编号
		Integer newAdviserId = getAdviserId(request);
		if (newAdviserId != null)
			adviserId = newAdviserId;

		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, visaService.countVisa(keyword, startHandlingDate, endHandlingDate,
					startDate, endDate, adviserId, userId));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<VisaDTO>> listVisa(@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "startHandlingDate", required = false) String startHandlingDate,
			@RequestParam(value = "endHandlingDate", required = false) String endHandlingDate,
			@RequestParam(value = "startDate", required = false) String startDate,
			@RequestParam(value = "endDate", required = false) String endDate,
			@RequestParam(value = "adviserId", required = false) Integer adviserId,
			@RequestParam(value = "userId", required = false) Integer userId,
			@RequestParam(value = "pageNum") int pageNum, @RequestParam(value = "pageSize") int pageSize,
			HttpServletRequest request, HttpServletResponse response) {

		// 更改当前顾问编号
		Integer newAdviserId = getAdviserId(request);
		if (newAdviserId != null)
			adviserId = newAdviserId;

		try {
			super.setGetHeader(response);
			return new Response<List<VisaDTO>>(0, visaService.listVisa(keyword, startHandlingDate, endHandlingDate,
					startDate, endDate, adviserId, userId, pageNum, pageSize));
		} catch (ServiceException e) {
			return new Response<List<VisaDTO>>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/get", method = RequestMethod.GET)
	@ResponseBody
	public Response<VisaDTO> getVisa(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<VisaDTO>(0, visaService.getVisaById(id));
		} catch (ServiceException e) {
			return new Response<VisaDTO>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/close", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> closeVisa(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			VisaDTO visaDto = visaService.getVisaById(id);
			visaDto.setClose(true);
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
				if (adminUserLoginInfo == null || (StringUtil.isNotEmpty(adminUserLoginInfo.getApList())
						&& !"KJ".equalsIgnoreCase(adminUserLoginInfo.getApList())))
					return new Response<VisaDTO>(1, "仅限会计审核佣金订单.", null);
				if (StringUtil.isEmpty(adminUserLoginInfo.getApList())
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
			@RequestParam(value = "closedReason", required = false) String closedReason, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			if (ReviewKjStateEnum.COMPLETE.toString().equalsIgnoreCase(state)
					|| ReviewKjStateEnum.FINISH.toString().equalsIgnoreCase(state))
				return new Response<VisaDTO>(1, "完成操作请调用'approval'接口.", null);
			// 审核
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo != null) {
				if (adminUserLoginInfo == null || (StringUtil.isNotEmpty(adminUserLoginInfo.getApList())
						&& !"KJ".equalsIgnoreCase(adminUserLoginInfo.getApList())))
					return new Response<VisaDTO>(1, "仅限会计审核佣金订单.", null);
				if (StringUtil.isEmpty(adminUserLoginInfo.getApList())
						|| "KJ".equalsIgnoreCase(adminUserLoginInfo.getApList())) {
					if (ReviewKjStateEnum.get(state) != null) {
						VisaDTO visaDto = visaService.getVisaById(id);
						if (visaDto == null)
							return new Response<VisaDTO>(1, "佣金订单不存在!", null);
						serviceOrderService.refuse(id, adminUserLoginInfo.getId(), null, null, null,
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

}
