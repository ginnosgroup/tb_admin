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
import org.zhinanzhen.b.service.BrokerageService;
import org.zhinanzhen.b.service.pojo.BrokerageDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

import com.ikasoa.core.utils.StringUtil;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/brokerage")
public class BrokerageController extends BaseController {

	@Resource
	BrokerageService brokerageService;

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	public Response<BrokerageDTO> addBrokerage(@RequestParam(value = "userId", required = false) String userId,
			@RequestParam(value = "handlingDate") String handlingDate,
			@RequestParam(value = "receiveTypeId") String receiveTypeId,
			@RequestParam(value = "receiveDate") String receiveDate,
			@RequestParam(value = "serviceId") String serviceId, @RequestParam(value = "receivable") String receivable,
			@RequestParam(value = "received", required = false) String received,
			@RequestParam(value = "amount") String amount, @RequestParam(value = "adviserId") String adviserId,
			@RequestParam(value = "officialId") String officialId, HttpServletRequest request,
			HttpServletResponse response) {

		// 更改当前顾问编号
		Integer newAdviserId = getAdviserId(request);
		if (newAdviserId != null)
			adviserId = newAdviserId + "";

		try {
			super.setPostHeader(response);
			BrokerageDTO brokerageDto = new BrokerageDTO();
			if (StringUtil.isNotEmpty(userId)) {
				brokerageDto.setUserId(Integer.parseInt(userId));
				List<BrokerageDTO> brokerageList = brokerageService.listBrokerage(null, null, null, null, null, null,
						Integer.parseInt(userId), 0, 1);
				if (brokerageList.size() > 0) {
					BrokerageDTO nowBrokerageDto = brokerageList.get(0);
					return new Response<BrokerageDTO>(1, "客户" + nowBrokerageDto.getUserName() + "(ID:"
							+ nowBrokerageDto.getUserId() + ")已存在!顾问是" + nowBrokerageDto.getAdviserName() + ",请联系管理员.",
							null);
				}
			}
			if (StringUtil.isNotEmpty(handlingDate)) {
				brokerageDto.setHandlingDate(new Date(Long.parseLong(handlingDate)));
			}
			if (StringUtil.isNotEmpty(receiveTypeId)) {
				brokerageDto.setReceiveTypeId(Integer.parseInt(receiveTypeId));
			}
			if (StringUtil.isNotEmpty(receiveDate)) {
				brokerageDto.setReceiveDate(new Date(Long.parseLong(receiveDate)));
			}
			if (StringUtil.isNotEmpty(serviceId)) {
				brokerageDto.setServiceId(Integer.parseInt(serviceId));
			}
			if (StringUtil.isNotEmpty(receivable)) {
				brokerageDto.setReceivable(Double.parseDouble(receivable));
			}
			if (StringUtil.isNotEmpty(received)) {
				brokerageDto.setReceived(Double.parseDouble(received));
			}
			if (StringUtil.isNotEmpty(amount)) {
				brokerageDto.setAmount(Double.parseDouble(amount));
			}
			if (StringUtil.isNotEmpty(adviserId)) {
				brokerageDto.setAdviserId(StringUtil.toInt(adviserId));
			}
			if (StringUtil.isNotEmpty(officialId)) {
				brokerageDto.setOfficialId(StringUtil.toInt(officialId));
			}
			double commission = brokerageDto.getAmount();
			brokerageDto.setGst(commission / 11);
			brokerageDto.setDeductGst(commission - brokerageDto.getGst());
			brokerageDto.setBonus(brokerageDto.getDeductGst() * 0.1);
			if (brokerageService.addBrokerage(brokerageDto) > 0) {
				return new Response<BrokerageDTO>(0, brokerageDto);
			} else {
				return new Response<BrokerageDTO>(1, "创建失败.", null);
			}
		} catch (ServiceException e) {
			return new Response<BrokerageDTO>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	@ResponseBody
	public Response<BrokerageDTO> updateBrokerage(@RequestParam(value = "id") int id,
			@RequestParam(value = "userId", required = false) String userId,
			@RequestParam(value = "handlingDate", required = false) String handlingDate,
			@RequestParam(value = "receiveTypeId", required = false) String receiveTypeId,
			@RequestParam(value = "receiveDate", required = false) String receiveDate,
			@RequestParam(value = "serviceId", required = false) String serviceId,
			@RequestParam(value = "receivable", required = false) String receivable,
			@RequestParam(value = "received", required = false) String received,
			@RequestParam(value = "amount", required = false) String amount,
			@RequestParam(value = "adviserId", required = false) String adviserId,
			@RequestParam(value = "officialId", required = false) String officialId, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			BrokerageDTO brokerageDto = new BrokerageDTO();
			brokerageDto.setId(id);
			if (StringUtil.isNotEmpty(handlingDate)) {
				brokerageDto.setHandlingDate(new Date(Long.parseLong(handlingDate)));
			}
			if (StringUtil.isNotEmpty(userId)) {
				brokerageDto.setUserId(Integer.parseInt(userId));
			}
			if (StringUtil.isNotEmpty(receiveTypeId)) {
				brokerageDto.setReceiveTypeId(Integer.parseInt(receiveTypeId));
			}
			if (StringUtil.isNotEmpty(receiveDate)) {
				brokerageDto.setReceiveDate(new Date(Long.parseLong(receiveDate)));
			}
			if (StringUtil.isNotEmpty(serviceId)) {
				brokerageDto.setServiceId(Integer.parseInt(serviceId));
			}
			if (StringUtil.isNotEmpty(receivable)) {
				brokerageDto.setReceivable(Double.parseDouble(receivable));
			}
			if (StringUtil.isNotEmpty(received)) {
				brokerageDto.setReceived(Double.parseDouble(received));
			}
			if (StringUtil.isNotEmpty(amount)) {
				brokerageDto.setAmount(Double.parseDouble(amount));
			}
			if (StringUtil.isNotEmpty(adviserId)) {
				brokerageDto.setAdviserId(StringUtil.toInt(adviserId));
			}
			if (StringUtil.isNotEmpty(officialId)) {
				brokerageDto.setOfficialId(StringUtil.toInt(officialId));
			}
			double commission = brokerageDto.getAmount();
			brokerageDto.setGst(commission / 11);
			brokerageDto.setDeductGst(commission - brokerageDto.getGst());
			brokerageDto.setBonus(brokerageDto.getDeductGst() * 0.1);
			if (brokerageService.updateBrokerage(brokerageDto) > 0) {
				return new Response<BrokerageDTO>(0, brokerageDto);
			} else {
				return new Response<BrokerageDTO>(1, "修改失败.", null);
			}
		} catch (ServiceException e) {
			return new Response<BrokerageDTO>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/count", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> countBrokerage(@RequestParam(value = "keyword", required = false) String keyword,
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
			return new Response<Integer>(0, brokerageService.countBrokerage(keyword, startHandlingDate, endHandlingDate,
					startDate, endDate, adviserId, userId));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<BrokerageDTO>> listBrokerage(@RequestParam(value = "keyword", required = false) String keyword,
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
			return new Response<List<BrokerageDTO>>(0, brokerageService.listBrokerage(keyword, startHandlingDate,
					endHandlingDate, startDate, endDate, adviserId, userId, pageNum, pageSize));
		} catch (ServiceException e) {
			return new Response<List<BrokerageDTO>>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/get", method = RequestMethod.GET)
	@ResponseBody
	public Response<BrokerageDTO> getBrokerage(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<BrokerageDTO>(0, brokerageService.getBrokerageById(id));
		} catch (ServiceException e) {
			return new Response<BrokerageDTO>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/close", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> closeBrokerage(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			BrokerageDTO brokerageDto = brokerageService.getBrokerageById(id);
			brokerageDto.setClose(true);
			return new Response<Integer>(0, brokerageService.updateBrokerage(brokerageDto));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/reopen", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> reopenBrokerage(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			BrokerageDTO brokerageDto = brokerageService.getBrokerageById(id);
			brokerageDto.setClose(false);
			return new Response<Integer>(0, brokerageService.updateBrokerage(brokerageDto));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/delete", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> deleteBrokerage(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, brokerageService.deleteBrokerageById(id));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), 0);
		}
	}

}
