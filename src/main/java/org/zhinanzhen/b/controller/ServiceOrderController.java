package org.zhinanzhen.b.controller;

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
		WAIT, REVIEW, APPLY, COMPLETE, PAID, CLOSE;
		public static ReviewAdviserStateEnum get(String name) {
			for (ReviewAdviserStateEnum e : ReviewAdviserStateEnum.values())
				if (e.toString().equals(name))
					return e;
			return null;
		}
	}

	public enum ReviewMaraStateEnum {
		WAIT, FINISH;
		public static ReviewMaraStateEnum get(String name) {
			for (ReviewMaraStateEnum e : ReviewMaraStateEnum.values())
				if (e.toString().equals(name))
					return e;
			return null;
		}
	}

	public enum ReviewOfficialStateEnum {
		REVIEW, FINISH, APPLY, COMPLETE, PAID;
		public static ReviewOfficialStateEnum get(String name) {
			for (ReviewOfficialStateEnum e : ReviewOfficialStateEnum.values())
				if (e.toString().equals(name))
					return e;
			return null;
		}
	}

	public enum ReviewKjStateEnum {
		WAIT, FINISH, COMPLETE;
		public static ReviewKjStateEnum get(String name) {
			for (ReviewKjStateEnum e : ReviewKjStateEnum.values())
				if (e.toString().equals(name))
					return e;
			return null;
		}
	}

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> addServiceOrder(@RequestParam(value = "type") String type,
			@RequestParam(value = "isPay") String isPay,
			@RequestParam(value = "receiveTypeId", required = false) String receiveTypeId,
			@RequestParam(value = "receiveDate", required = false) String receiveDate,
			@RequestParam(value = "receivable", required = false) String receivable,
			@RequestParam(value = "discount", required = false) String discount,
			@RequestParam(value = "received", required = false) String received,
			@RequestParam(value = "paymentTimes", required = false) String paymentTimes,
			@RequestParam(value = "amount", required = false) String amount,
			@RequestParam(value = "gst", required = false) String gst,
			@RequestParam(value = "deductGst", required = false) String deductGst,
			@RequestParam(value = "bonus", required = false) String bonus,
			@RequestParam(value = "userId") String userId,
			@RequestParam(value = "maraId", required = false) String maraId,
			@RequestParam(value = "adviserId") String adviserId, @RequestParam(value = "officialId") String officialId,
			@RequestParam(value = "remarks", required = false) String remarks, HttpServletRequest request,
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
			serviceOrderDto.setState(ReviewAdviserStateEnum.WAIT.toString());
			if (isPay != null && "true".equalsIgnoreCase(isPay))
				serviceOrderDto.setPay(true);
			else
				serviceOrderDto.setPay(false);
			if (StringUtil.isNotEmpty(receiveTypeId))
				serviceOrderDto.setReceiveTypeId(StringUtil.toInt(receiveTypeId));
			if (StringUtil.isNotEmpty(receiveDate))
				serviceOrderDto.setReceiveDate(new Date(Long.parseLong(receiveDate)));
			if (StringUtil.isNotEmpty(receivable))
				serviceOrderDto.setReceivable(Double.parseDouble(receivable));
			if (StringUtil.isNotEmpty(discount))
				serviceOrderDto.setAmount(Double.parseDouble(discount));
			if (StringUtil.isNotEmpty(received))
				serviceOrderDto.setReceived(Double.parseDouble(received));
			if (StringUtil.isNotEmpty(paymentTimes))
				serviceOrderDto.setPaymentTimes(StringUtil.toInt(paymentTimes));
			if (StringUtil.isNotEmpty(amount))
				serviceOrderDto.setAmount(Double.parseDouble(amount));
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
			if (serviceOrderService.addServiceOrder(serviceOrderDto) > 0) {
				if (adminUserLoginInfo != null)
					serviceOrderService.approval(serviceOrderDto.getId(), adminUserLoginInfo.getId(),
							ReviewAdviserStateEnum.WAIT.toString(), null, null, null);
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
			@RequestParam(value = "isPay", required = false) String isPay,
			@RequestParam(value = "receiveTypeId", required = false) String receiveTypeId,
			@RequestParam(value = "receiveDate", required = false) String receiveDate,
			@RequestParam(value = "receivable", required = false) String receivable,
			@RequestParam(value = "discount", required = false) String discount,
			@RequestParam(value = "received", required = false) String received,
			@RequestParam(value = "paymentTimes", required = false) String paymentTimes,
			@RequestParam(value = "amount", required = false) String amount,
			@RequestParam(value = "gst", required = false) String gst,
			@RequestParam(value = "deductGst", required = false) String deductGst,
			@RequestParam(value = "bonus", required = false) String bonus,
			@RequestParam(value = "userId", required = false) String userId,
			@RequestParam(value = "maraId", required = false) String maraId,
			@RequestParam(value = "adviserId", required = false) String adviserId,
			@RequestParam(value = "officialId", required = false) String officialId,
			@RequestParam(value = "remarks", required = false) String remarks, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			ServiceOrderDTO serviceOrderDto = new ServiceOrderDTO();
			serviceOrderDto.setId(id);
			if (StringUtil.isNotEmpty(type))
				serviceOrderDto.setType(type);
			if (isPay != null && "true".equalsIgnoreCase(isPay))
				serviceOrderDto.setPay(true);
			else
				serviceOrderDto.setPay(false);
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
			if (StringUtil.isNotEmpty(amount))
				serviceOrderDto.setAmount(Double.parseDouble(amount));
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
			@RequestParam(value = "officialId", required = false) String officialId, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, serviceOrderService.countServiceOrder(type, state, StringUtil.toInt(userId),
					StringUtil.toInt(maraId), StringUtil.toInt(adviserId), StringUtil.toInt(officialId)));
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
			HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<List<ServiceOrderDTO>>(0,
					serviceOrderService.listServiceOrder(type, state, StringUtil.toInt(userId),
							StringUtil.toInt(maraId), StringUtil.toInt(adviserId), StringUtil.toInt(officialId),
							pageNum, pageSize));
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

	@RequestMapping(value = "/approval", method = RequestMethod.POST)
	@ResponseBody
	public Response<ServiceOrderDTO> approval(@RequestParam(value = "id") int id,
			@RequestParam(value = "state") String state, HttpServletRequest request, HttpServletResponse response) {
		try {
			super.setPostHeader(response);
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
						return new Response<ServiceOrderDTO>(0, serviceOrderService.approval(id,
								adminUserLoginInfo.getId(), state.toUpperCase(), null, null, null));
					else
						return new Response<ServiceOrderDTO>(1, "state错误!(" + state + ")", null);
				} else if ("MA".equalsIgnoreCase(adminUserLoginInfo.getApList())) {
					if (!"VISA".equalsIgnoreCase(serviceOrderDto.getType()))
						return new Response<ServiceOrderDTO>(1, "Mara审核仅限签证服务订单!", null);
					if (ReviewMaraStateEnum.get(state) != null)
						return new Response<ServiceOrderDTO>(0, serviceOrderService.approval(id,
								adminUserLoginInfo.getId(), null, state.toUpperCase(), null, null));
					else
						return new Response<ServiceOrderDTO>(1, "state错误!(" + state + ")", null);
				} else if ("WA".equalsIgnoreCase(adminUserLoginInfo.getApList())) {
					if (ReviewOfficialStateEnum.get(state) != null)
						return new Response<ServiceOrderDTO>(0, serviceOrderService.approval(id,
								adminUserLoginInfo.getId(), null, null, state.toUpperCase(), null));
					else
						return new Response<ServiceOrderDTO>(1, "state错误!(" + state + ")", null);
				} else if ("KJ".equalsIgnoreCase(adminUserLoginInfo.getApList())) {
					if (ReviewKjStateEnum.get(state) != null)
						return new Response<ServiceOrderDTO>(0, serviceOrderService.approval(id,
								adminUserLoginInfo.getId(), null, null, null, state.toUpperCase()));
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

	@RequestMapping(value = "/refuse", method = RequestMethod.POST)
	@ResponseBody
	public Response<ServiceOrderDTO> refuse(@RequestParam(value = "id") int id,
			@RequestParam(value = "state") String state, HttpServletRequest request, HttpServletResponse response) {
		try {
			super.setPostHeader(response);
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
						return new Response<ServiceOrderDTO>(0, serviceOrderService.refuse(id,
								adminUserLoginInfo.getId(), state.toUpperCase(), null, null, null));
					else
						return new Response<ServiceOrderDTO>(1, "state错误!(" + state + ")", null);
				} else if ("MA".equalsIgnoreCase(adminUserLoginInfo.getApList())) {
					if (!"VISA".equalsIgnoreCase(serviceOrderDto.getType()))
						return new Response<ServiceOrderDTO>(1, "Mara审核仅限签证服务订单!", null);
					if (ReviewMaraStateEnum.get(state) != null)
						return new Response<ServiceOrderDTO>(0, serviceOrderService.refuse(id,
								adminUserLoginInfo.getId(), null, state.toUpperCase(), null, null));
					else
						return new Response<ServiceOrderDTO>(1, "state错误!(" + state + ")", null);
				} else if ("WA".equalsIgnoreCase(adminUserLoginInfo.getApList())) {
					if (ReviewOfficialStateEnum.get(state) != null)
						return new Response<ServiceOrderDTO>(0, serviceOrderService.refuse(id,
								adminUserLoginInfo.getId(), null, null, state.toUpperCase(), null));
					else
						return new Response<ServiceOrderDTO>(1, "state错误!(" + state + ")", null);
				} else if ("KJ".equalsIgnoreCase(adminUserLoginInfo.getApList())) {
					if (ReviewKjStateEnum.get(state) != null)
						return new Response<ServiceOrderDTO>(0, serviceOrderService.refuse(id,
								adminUserLoginInfo.getId(), null, null, null, state.toUpperCase()));
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

}
