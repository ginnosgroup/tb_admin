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
import org.zhinanzhen.b.service.RefundService;
import org.zhinanzhen.b.service.pojo.RefundDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

import com.ikasoa.core.utils.StringUtil;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/refund")
public class RefundController extends BaseController {

	@Resource
	RefundService refundService;

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	public Response<RefundDTO> addRefund(@RequestParam(value = "handlingDate") String handlingDate,
			@RequestParam(value = "userId") String userId, @RequestParam(value = "name") String name,
			@RequestParam(value = "adviserId") String adviserId, @RequestParam(value = "officialId") String officialId,
			@RequestParam(value = "receiveTypeId") String receiveTypeId, @RequestParam(value = "amount") String amount,
			@RequestParam(value = "preRefundAmount") String preRefundAmount,
			@RequestParam(value = "bankName") String bankName, @RequestParam(value = "bankAccount") String bankAccount,
			@RequestParam(value = "bsb") String bsb, @RequestParam(value = "refundDate") String refundDate,
			@RequestParam(value = "refundAmount") String refundAmount,
			@RequestParam(value = "remarks", required = false) String remarks, HttpServletRequest request,
			HttpServletResponse response) {

		// 更改当前顾问编号
		Integer newAdviserId = getAdviserId(request);
		if (newAdviserId != null)
			adviserId = newAdviserId + "";

		try {
			super.setPostHeader(response);
			RefundDTO refundDto = new RefundDTO();
			if (StringUtil.isNotEmpty(handlingDate)) {
				refundDto.setHandlingDate(new Date(Long.parseLong(handlingDate)));
			}
			if (StringUtil.isNotEmpty(userId)) {
				refundDto.setUserId(StringUtil.toInt(userId));
			}
			if (StringUtil.isNotEmpty(name)) {
				refundDto.setName(name);
			}
			if (StringUtil.isNotEmpty(adviserId)) {
				refundDto.setAdviserId(StringUtil.toInt(adviserId));
			}
			if (StringUtil.isNotEmpty(officialId)) {
				refundDto.setOfficialId(StringUtil.toInt(officialId));
			}
			if (StringUtil.isNotEmpty(receiveTypeId)) {
				refundDto.setReceiveTypeId(StringUtil.toInt(receiveTypeId));
			}
			if (StringUtil.isNotEmpty(amount)) {
				refundDto.setAmount(Double.parseDouble(amount));
			}
			if (StringUtil.isNotEmpty(preRefundAmount)) {
				refundDto.setPreRefundAmount(Double.parseDouble(preRefundAmount));
			}
			if (StringUtil.isNotEmpty(bankName)) {
				refundDto.setBankName(bankName);
			}
			if (StringUtil.isNotEmpty(bankAccount)) {
				refundDto.setBankAccount(bankAccount);
			}
			if (StringUtil.isNotEmpty(bsb)) {
				refundDto.setBsb(bsb);
			}
			if (StringUtil.isNotEmpty(refundDate)) {
				refundDto.setRefundDate(new Date(Long.parseLong(refundDate)));
			}
			if (StringUtil.isNotEmpty(refundAmount)) {
				refundDto.setRefundAmount(Double.parseDouble(refundAmount));
			}
			if (StringUtil.isNotEmpty(remarks))
				refundDto.setRemarks(remarks);
			refundDto.setGst(refundDto.getRefundAmount() / 11);
			refundDto.setDeductGst(refundDto.getRefundAmount() - refundDto.getGst());
			refundDto.setRefund(refundDto.getDeductGst() * 0.1);
			if (refundService.addRefund(refundDto) > 0) {
				return new Response<RefundDTO>(0, refundDto);
			} else {
				return new Response<RefundDTO>(1, "创建失败.", null);
			}
		} catch (ServiceException e) {
			return new Response<RefundDTO>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	@ResponseBody
	public Response<RefundDTO> updateRefund(@RequestParam(value = "id") int id,
			@RequestParam(value = "handlingDate", required = false) String handlingDate,
			@RequestParam(value = "userId", required = false) String userId,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "adviserId", required = false) String adviserId,
			@RequestParam(value = "officialId", required = false) String officialId,
			@RequestParam(value = "receiveTypeId", required = false) String receiveTypeId,
			@RequestParam(value = "amount", required = false) String amount,
			@RequestParam(value = "preRefundAmount", required = false) String preRefundAmount,
			@RequestParam(value = "bankName", required = false) String bankName,
			@RequestParam(value = "bankAccount", required = false) String bankAccount,
			@RequestParam(value = "bsb", required = false) String bsb,
			@RequestParam(value = "refundDate", required = false) String refundDate,
			@RequestParam(value = "refundAmount", required = false) String refundAmount,
			@RequestParam(value = "remarks", required = false) String remarks, HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			RefundDTO refundDto = new RefundDTO();
			refundDto.setId(id);
			if (StringUtil.isNotEmpty(handlingDate)) {
				refundDto.setHandlingDate(new Date(Long.parseLong(handlingDate)));
			}
			if (StringUtil.isNotEmpty(userId)) {
				refundDto.setUserId(StringUtil.toInt(userId));
			}
			if (StringUtil.isNotEmpty(name)) {
				refundDto.setName(name);
			}
			if (StringUtil.isNotEmpty(adviserId)) {
				refundDto.setAdviserId(StringUtil.toInt(adviserId));
			}
			if (StringUtil.isNotEmpty(officialId)) {
				refundDto.setOfficialId(StringUtil.toInt(officialId));
			}
			if (StringUtil.isNotEmpty(receiveTypeId)) {
				refundDto.setReceiveTypeId(StringUtil.toInt(receiveTypeId));
			}
			if (StringUtil.isNotEmpty(amount)) {
				refundDto.setAmount(Double.parseDouble(amount));
			}
			if (StringUtil.isNotEmpty(preRefundAmount)) {
				refundDto.setPreRefundAmount(Double.parseDouble(preRefundAmount));
			}
			if (StringUtil.isNotEmpty(bankName)) {
				refundDto.setBankName(bankName);
			}
			if (StringUtil.isNotEmpty(bankAccount)) {
				refundDto.setBankAccount(bankAccount);
			}
			if (StringUtil.isNotEmpty(bsb)) {
				refundDto.setBsb(bsb);
			}
			if (StringUtil.isNotEmpty(refundDate)) {
				refundDto.setRefundDate(new Date(Long.parseLong(refundDate)));
			}
			if (StringUtil.isNotEmpty(refundAmount)) {
				refundDto.setRefundAmount(Double.parseDouble(refundAmount));
			}
			if (StringUtil.isNotEmpty(remarks))
				refundDto.setRemarks(remarks);
			refundDto.setGst(refundDto.getRefundAmount() / 11);
			refundDto.setDeductGst(refundDto.getRefundAmount() - refundDto.getGst());
			refundDto.setRefund(refundDto.getDeductGst() * 0.1);
			if (refundService.updateRefund(refundDto) > 0) {
				return new Response<RefundDTO>(0, refundDto);
			} else {
				return new Response<RefundDTO>(1, "创建失败.", null);
			}
		} catch (ServiceException e) {
			return new Response<RefundDTO>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/count", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> countRefund(@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "startHandlingDate", required = false) String startHandlingDate,
			@RequestParam(value = "endHandlingDate", required = false) String endHandlingDate,
			@RequestParam(value = "startDate", required = false) String startDate,
			@RequestParam(value = "endDate", required = false) String endDate,
			@RequestParam(value = "adviserId", required = false) Integer adviserId,
			@RequestParam(value = "officialId", required = false) Integer officialId,
			@RequestParam(value = "userId", required = false) Integer userId, HttpServletRequest request,
			HttpServletResponse response) {

		// 更改当前顾问编号
		Integer newAdviserId = getAdviserId(request);
		if (newAdviserId != null)
			adviserId = newAdviserId;

		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, refundService.countRefund(keyword, startHandlingDate, endHandlingDate,
					startDate, endDate, adviserId, officialId, userId));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<RefundDTO>> listRefund(@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "startHandlingDate", required = false) String startHandlingDate,
			@RequestParam(value = "endHandlingDate", required = false) String endHandlingDate,
			@RequestParam(value = "startDate", required = false) String startDate,
			@RequestParam(value = "endDate", required = false) String endDate,
			@RequestParam(value = "adviserId", required = false) Integer adviserId,
			@RequestParam(value = "officialId", required = false) Integer officialId,
			@RequestParam(value = "userId", required = false) Integer userId,
			@RequestParam(value = "pageNum") int pageNum, @RequestParam(value = "pageSize") int pageSize,
			HttpServletRequest request, HttpServletResponse response) {

		// 更改当前顾问编号
		Integer newAdviserId = getAdviserId(request);
		if (newAdviserId != null)
			adviserId = newAdviserId;

		try {
			super.setGetHeader(response);
			return new Response<List<RefundDTO>>(0, refundService.listRefund(keyword, startHandlingDate,
					endHandlingDate, startDate, endDate, adviserId, officialId, userId, pageNum, pageSize));
		} catch (ServiceException e) {
			return new Response<List<RefundDTO>>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/get", method = RequestMethod.GET)
	@ResponseBody
	public Response<RefundDTO> getRefund(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<RefundDTO>(0, refundService.getRefundById(id));
		} catch (ServiceException e) {
			return new Response<RefundDTO>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/close", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> closeRefund(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			RefundDTO refundDto = refundService.getRefundById(id);
			refundDto.setClose(true);
			return new Response<Integer>(0, refundService.updateRefund(refundDto));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/reopen", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> reopenBrokerageSa(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			RefundDTO refundDto = refundService.getRefundById(id);
			refundDto.setClose(false);
			return new Response<Integer>(0, refundService.updateRefund(refundDto));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/delete", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> deleteRefund(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, refundService.deleteRefundById(id));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), 0);
		}
	}

}
