package org.zhinanzhen.b.controller;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.zhinanzhen.b.service.ServiceOrderService;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.b.service.pojo.ServiceOrderReviewDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

import com.ikasoa.core.utils.StringUtil;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/serviceOrder")
public class ServiceOrderController extends BaseController {

	@Resource
	ServiceOrderService serviceOrderService;

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

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> addServiceOrder(@RequestParam(value = "type") String type,
			@RequestParam(value = "serviceId") String serviceId,
			@RequestParam(value = "schoolId", required = false) String schoolId,
			@RequestParam(value = "isSettle", required = false) String isSettle,
			@RequestParam(value = "isDepositUser", required = false) String isDepositUser,
			@RequestParam(value = "subagencyId", required = false) String subagencyId,
			@RequestParam(value = "isPay") String isPay,
			@RequestParam(value = "receiveTypeId", required = false) String receiveTypeId,
			@RequestParam(value = "receiveDate", required = false) String receiveDate,
			@RequestParam(value = "receivable", required = false) String receivable,
			@RequestParam(value = "discount", required = false) String discount,
			@RequestParam(value = "received", required = false) String received,
			@RequestParam(value = "paymentTimes", required = false) String paymentTimes,
			@RequestParam(value = "perAmount", required = false) String perAmount,
			@RequestParam(value = "amount", required = false) String amount,
			@RequestParam(value = "expectAmount", required = false) String expectAmount,
			@RequestParam(value = "gst", required = false) String gst,
			@RequestParam(value = "deductGst", required = false) String deductGst,
			@RequestParam(value = "bonus", required = false) String bonus,
			@RequestParam(value = "userId") String userId,
			@RequestParam(value = "maraId", required = false) String maraId,
			@RequestParam(value = "adviserId", required = false) String adviserId,
			@RequestParam(value = "officialId", required = false) String officialId,
			@RequestParam(value = "remarks", required = false) String remarks,
			@RequestParam(value = "closedReason", required = false) String closedReason, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo == null || (StringUtil.isNotEmpty(adminUserLoginInfo.getApList())
					&& !"GW".equalsIgnoreCase(adminUserLoginInfo.getApList())))
				return new Response<Integer>(1, "仅顾问和超级管理员能创建服务订单.", 0);
			ServiceOrderDTO serviceOrderDto = new ServiceOrderDTO();
			if (StringUtil.isNotEmpty(type))
				serviceOrderDto.setType(type);
			if (StringUtil.isNotEmpty(serviceId))
				serviceOrderDto.setServiceId(StringUtil.toInt(serviceId));
			if (StringUtil.isNotEmpty(schoolId))
				serviceOrderDto.setSchoolId(StringUtil.toInt(schoolId));
			serviceOrderDto.setState(ReviewAdviserStateEnum.PENDING.toString());
			serviceOrderDto.setSettle(isSettle != null && "true".equalsIgnoreCase(isSettle));
			serviceOrderDto.setDepositUser(isDepositUser != null && "true".equalsIgnoreCase(isDepositUser));
			if (StringUtil.isNotEmpty(subagencyId))
				serviceOrderDto.setSubagencyId(StringUtil.toInt(subagencyId));
			serviceOrderDto.setPay(isPay != null && "true".equalsIgnoreCase(isPay));
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
			if (StringUtil.isNotEmpty(paymentTimes))
				serviceOrderDto.setPaymentTimes(StringUtil.toInt(paymentTimes));
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
			if (serviceOrderService.addServiceOrder(serviceOrderDto) > 0) {
				if (adminUserLoginInfo != null)
					serviceOrderService.approval(serviceOrderDto.getId(), adminUserLoginInfo.getId(),
							ReviewAdviserStateEnum.PENDING.toString(), null, null, null);
				return new Response<Integer>(0, serviceOrderDto.getId());
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
			@RequestParam(value = "paymentTimes", required = false) String paymentTimes,
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
			ServiceOrderDTO serviceOrderDto = new ServiceOrderDTO();
			serviceOrderDto.setId(id);
			if (StringUtil.isNotEmpty(type))
				serviceOrderDto.setType(type);
			if (StringUtil.isNotEmpty(serviceId))
				serviceOrderDto.setServiceId(StringUtil.toInt(serviceId));
			if (StringUtil.isNotEmpty(schoolId))
				serviceOrderDto.setSchoolId(StringUtil.toInt(schoolId));
			serviceOrderDto.setSettle(isSettle != null && "true".equalsIgnoreCase(isSettle));
			serviceOrderDto.setDepositUser(isDepositUser != null && "true".equalsIgnoreCase(isDepositUser));
			if (StringUtil.isNotEmpty(subagencyId))
				serviceOrderDto.setSubagencyId(StringUtil.toInt(subagencyId));
			serviceOrderDto.setPay(isPay != null && "true".equalsIgnoreCase(isPay));
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
			if (StringUtil.isNotEmpty(paymentTimes))
				serviceOrderDto.setPaymentTimes(StringUtil.toInt(paymentTimes));
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
							StringUtil.toInt(officialId)));
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
							StringUtil.toInt(officialId), pageNum, pageSize));
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
			@RequestParam(value = "state") String state, HttpServletRequest request, HttpServletResponse response) {
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
			// 审核
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo != null)
				if (StringUtil.isEmpty(adminUserLoginInfo.getApList())
						|| "GW".equalsIgnoreCase(adminUserLoginInfo.getApList())) {
					if (ReviewAdviserStateEnum.get(state) != null)
						if (ReviewAdviserStateEnum.REVIEW.toString().equals(state.toUpperCase())) // 顾问审核
							return new Response<ServiceOrderDTO>(0, serviceOrderService.approval(id,
									adminUserLoginInfo.getId(), state.toUpperCase(), null, null, null));
						else if (ReviewAdviserStateEnum.PAID.toString().equals(state.toUpperCase())) { // 顾问支付同时修改文案状态
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
							return new Response<ServiceOrderDTO>(0,
									serviceOrderService.approval(id, adminUserLoginInfo.getId(), null,
											ReviewMaraStateEnum.WAIT.toString(), state.toUpperCase(), null));
						} else if (ReviewOfficialStateEnum.APPLY.toString().equals(state.toUpperCase())) { // 文案申请同时修改顾问状态
							serviceOrderService.finish(id);
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
							return new Response<ServiceOrderDTO>(0,
									serviceOrderService.approval(id, adminUserLoginInfo.getId(),
											ReviewAdviserStateEnum.COMPLETE.toString(), null, state.toUpperCase(),
											null));
						} else
							return new Response<ServiceOrderDTO>(0, serviceOrderService.approval(id,
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

}
