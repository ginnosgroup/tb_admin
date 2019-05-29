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
import org.zhinanzhen.b.service.SchoolBrokerageSaService;
import org.zhinanzhen.b.service.pojo.SchoolBrokerageSaByDashboardListDTO;
import org.zhinanzhen.b.service.pojo.SchoolBrokerageSaDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

import com.ikasoa.core.utils.StringUtil;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/school_brokerage_sa")
public class SchoolBrokerageSaController extends BaseController {

	@Resource
	SchoolBrokerageSaService schoolBrokerageSaService;

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	public Response<SchoolBrokerageSaDTO> addSchoolBrokerageSa(
			@RequestParam(value = "handlingDate") String handlingDate, @RequestParam(value = "userId") String userId,
			@RequestParam(value = "schoolId") String schoolId, @RequestParam(value = "studentCode") String studentCode,
			@RequestParam(value = "startDate") String startDate, @RequestParam(value = "endDate") String endDate,
			@RequestParam(value = "tuitionFee") String tuitionFee,
			@RequestParam(value = "firstTermTuitionFee") String firstTermTuitionFee,
			@RequestParam(value = "commission") String commission, @RequestParam(value = "payDate") String payDate,
			@RequestParam(value = "invoiceCode") String invoiceCode,
			@RequestParam(value = "payAmount") String payAmount,
			@RequestParam(value = "subagencyId") String subagencyId,
			@RequestParam(value = "adviserId") String adviserId, @RequestParam(value = "officialId") String officialId,
			@RequestParam(value = "remarks", required = false) String remarks, HttpServletRequest request,
			HttpServletResponse response) {

		// 更改当前顾问编号
		Integer newAdviserId = getAdviserId(request);
		if (newAdviserId != null)
			adviserId = newAdviserId + "";

		try {
			super.setPostHeader(response);
			SchoolBrokerageSaDTO schoolBrokerageSaDto = new SchoolBrokerageSaDTO();
			if (StringUtil.isNotEmpty(handlingDate)) {
				schoolBrokerageSaDto.setHandlingDate(new Date(Long.parseLong(handlingDate)));
			}
			if (StringUtil.isNotEmpty(userId)) {
				schoolBrokerageSaDto.setUserId(StringUtil.toInt(userId));
			}
			if (StringUtil.isNotEmpty(schoolId)) {
				schoolBrokerageSaDto.setSchoolId(StringUtil.toInt(schoolId));
			}
			if (StringUtil.isNotEmpty(studentCode)) {
				schoolBrokerageSaDto.setStudentCode(studentCode);
			}
			if (StringUtil.isNotEmpty(startDate)) {
				schoolBrokerageSaDto.setStartDate(new Date(Long.parseLong(startDate)));
			}
			if (StringUtil.isNotEmpty(endDate)) {
				schoolBrokerageSaDto.setEndDate(new Date(Long.parseLong(endDate)));
			}
			if (StringUtil.isNotEmpty(tuitionFee)) {
				schoolBrokerageSaDto.setTuitionFee(Double.parseDouble(tuitionFee));
			}
			if (StringUtil.isNotEmpty(firstTermTuitionFee)) {
				schoolBrokerageSaDto.setFirstTermTuitionFee(Double.parseDouble(firstTermTuitionFee));
			}
			if (StringUtil.isNotEmpty(commission)) {
				schoolBrokerageSaDto.setCommission(Double.parseDouble(commission));
			}
			if (StringUtil.isNotEmpty(payDate)) {
				schoolBrokerageSaDto.setPayDate(new Date(Long.parseLong(payDate)));
			}
			if (StringUtil.isNotEmpty(invoiceCode)) {
				schoolBrokerageSaDto.setInvoiceCode(invoiceCode);
			}
			if (StringUtil.isNotEmpty(payAmount)) {
				schoolBrokerageSaDto.setPayAmount(Double.parseDouble(payAmount));
			}
			if (StringUtil.isNotEmpty(subagencyId)) {
				schoolBrokerageSaDto.setSubagencyId(Integer.parseInt(subagencyId));
			}
			if (StringUtil.isNotEmpty(adviserId)) {
				schoolBrokerageSaDto.setAdviserId(StringUtil.toInt(adviserId));
			}
			if (StringUtil.isNotEmpty(officialId)) {
				schoolBrokerageSaDto.setOfficialId(StringUtil.toInt(officialId));
			}
			if (StringUtil.isNotEmpty(remarks))
				schoolBrokerageSaDto.setRemarks(remarks);
			schoolBrokerageSaDto.setGst(schoolBrokerageSaDto.getCommission() / 11);
			schoolBrokerageSaDto.setDeductGst(schoolBrokerageSaDto.getCommission() - schoolBrokerageSaDto.getGst());
			schoolBrokerageSaDto.setBonus(schoolBrokerageSaDto.getDeductGst() * 0.1);
			if (schoolBrokerageSaService.addSchoolBrokerageSa(schoolBrokerageSaDto) > 0) {
				return new Response<SchoolBrokerageSaDTO>(0, schoolBrokerageSaDto);
			} else {
				return new Response<SchoolBrokerageSaDTO>(1, "创建失败.", null);
			}
		} catch (ServiceException e) {
			return new Response<SchoolBrokerageSaDTO>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	@ResponseBody
	public Response<SchoolBrokerageSaDTO> updateSchoolBrokerage(@RequestParam(value = "id") int id,
			@RequestParam(value = "handlingDate", required = false) String handlingDate,
			@RequestParam(value = "userId", required = false) String userId,
			@RequestParam(value = "schoolId", required = false) String schoolId,
			@RequestParam(value = "studentCode", required = false) String studentCode,
			@RequestParam(value = "startDate", required = false) String startDate,
			@RequestParam(value = "endDate", required = false) String endDate,
			@RequestParam(value = "tuitionFee", required = false) String tuitionFee,
			@RequestParam(value = "firstTermTuitionFee", required = false) String firstTermTuitionFee,
			@RequestParam(value = "commission", required = false) String commission,
			@RequestParam(value = "payDate", required = false) String payDate,
			@RequestParam(value = "invoiceCode", required = false) String invoiceCode,
			@RequestParam(value = "payAmount", required = false) String payAmount,
			@RequestParam(value = "subagencyId", required = false) String subagencyId,
			@RequestParam(value = "adviserId", required = false) String adviserId,
			@RequestParam(value = "officialId", required = false) String officialId,
			@RequestParam(value = "remarks", required = false) String remarks, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			SchoolBrokerageSaDTO schoolBrokerageSaDto = new SchoolBrokerageSaDTO();
			schoolBrokerageSaDto.setId(id);
			if (StringUtil.isNotEmpty(handlingDate)) {
				schoolBrokerageSaDto.setHandlingDate(new Date(Long.parseLong(handlingDate)));
			}
			if (StringUtil.isNotEmpty(userId)) {
				schoolBrokerageSaDto.setUserId(StringUtil.toInt(userId));
			}
			if (StringUtil.isNotEmpty(schoolId)) {
				schoolBrokerageSaDto.setSchoolId(StringUtil.toInt(schoolId));
			}
			if (StringUtil.isNotEmpty(studentCode)) {
				schoolBrokerageSaDto.setStudentCode(studentCode);
			}
			if (StringUtil.isNotEmpty(startDate)) {
				schoolBrokerageSaDto.setStartDate(new Date(Long.parseLong(startDate)));
			}
			if (StringUtil.isNotEmpty(endDate)) {
				schoolBrokerageSaDto.setEndDate(new Date(Long.parseLong(endDate)));
			}
			if (StringUtil.isNotEmpty(tuitionFee)) {
				schoolBrokerageSaDto.setTuitionFee(Double.parseDouble(tuitionFee));
			}
			if (StringUtil.isNotEmpty(firstTermTuitionFee)) {
				schoolBrokerageSaDto.setFirstTermTuitionFee(Double.parseDouble(firstTermTuitionFee));
			}
			if (StringUtil.isNotEmpty(commission)) {
				schoolBrokerageSaDto.setCommission(Double.parseDouble(commission));
			}
			if (StringUtil.isNotEmpty(payDate)) {
				schoolBrokerageSaDto.setPayDate(new Date(Long.parseLong(payDate)));
			}
			if (StringUtil.isNotEmpty(invoiceCode)) {
				schoolBrokerageSaDto.setInvoiceCode(invoiceCode);
			}
			if (StringUtil.isNotEmpty(payAmount)) {
				schoolBrokerageSaDto.setPayAmount(Double.parseDouble(payAmount));
			}
			if (StringUtil.isNotEmpty(subagencyId)) {
				schoolBrokerageSaDto.setSubagencyId(Integer.parseInt(subagencyId));
			}
			if (StringUtil.isNotEmpty(adviserId)) {
				schoolBrokerageSaDto.setAdviserId(StringUtil.toInt(adviserId));
			}
			if (StringUtil.isNotEmpty(officialId)) {
				schoolBrokerageSaDto.setOfficialId(StringUtil.toInt(officialId));
			}
			if (StringUtil.isNotEmpty(remarks))
				schoolBrokerageSaDto.setRemarks(remarks);
			schoolBrokerageSaDto.setGst(schoolBrokerageSaDto.getCommission() / 11);
			schoolBrokerageSaDto.setDeductGst(schoolBrokerageSaDto.getCommission() - schoolBrokerageSaDto.getGst());
			schoolBrokerageSaDto.setBonus(schoolBrokerageSaDto.getDeductGst() * 0.1);
			if (schoolBrokerageSaService.updateSchoolBrokerageSa(schoolBrokerageSaDto) > 0) {
				return new Response<SchoolBrokerageSaDTO>(0, schoolBrokerageSaDto);
			} else {
				return new Response<SchoolBrokerageSaDTO>(1, "修改失败.", null);
			}
		} catch (ServiceException e) {
			return new Response<SchoolBrokerageSaDTO>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/count", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> countSchoolBrokerage(@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "startHandlingDate", required = false) String startHandlingDate,
			@RequestParam(value = "endHandlingDate", required = false) String endHandlingDate,
			@RequestParam(value = "startDate", required = false) String startDate,
			@RequestParam(value = "endDate", required = false) String endDate,
			@RequestParam(value = "adviserId", required = false) Integer adviserId,
			@RequestParam(value = "schoolId", required = false) Integer schoolId,
			@RequestParam(value = "subagencyId", required = false) Integer subagencyId,
			@RequestParam(value = "userId", required = false) Integer userId,
			@RequestParam(value = "isSettleAccounts", required = false) Boolean isSettleAccounts,
			HttpServletRequest request, HttpServletResponse response) {

		// 更改当前顾问编号
		Integer newAdviserId = getAdviserId(request);
		if (newAdviserId != null)
			adviserId = newAdviserId;

		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, schoolBrokerageSaService.countSchoolBrokerageSa(keyword, startHandlingDate,
					endHandlingDate, startDate, endDate, adviserId, schoolId, subagencyId, userId, isSettleAccounts));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<SchoolBrokerageSaDTO>> listSchoolBrokerage(
			@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "startHandlingDate", required = false) String startHandlingDate,
			@RequestParam(value = "endHandlingDate", required = false) String endHandlingDate,
			@RequestParam(value = "startDate", required = false) String startDate,
			@RequestParam(value = "endDate", required = false) String endDate,
			@RequestParam(value = "adviserId", required = false) Integer adviserId,
			@RequestParam(value = "schoolId", required = false) Integer schoolId,
			@RequestParam(value = "subagencyId", required = false) Integer subagencyId,
			@RequestParam(value = "userId", required = false) Integer userId,
			@RequestParam(value = "isSettleAccounts", required = false) Boolean isSettleAccounts,
			@RequestParam(value = "pageNum") int pageNum, @RequestParam(value = "pageSize") int pageSize,
			HttpServletRequest request, HttpServletResponse response) {

		// 更改当前顾问编号
		Integer newAdviserId = getAdviserId(request);
		if (newAdviserId != null)
			adviserId = newAdviserId;

		try {
			super.setGetHeader(response);
			return new Response<List<SchoolBrokerageSaDTO>>(0,
					schoolBrokerageSaService.listSchoolBrokerageSa(keyword, startHandlingDate, endHandlingDate,
							startDate, endDate, adviserId, schoolId, subagencyId, userId, isSettleAccounts, pageNum,
							pageSize));
		} catch (ServiceException e) {
			return new Response<List<SchoolBrokerageSaDTO>>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/listByDashboard", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<SchoolBrokerageSaByDashboardListDTO>> listByDashboard(
			@RequestParam(value = "adviserId", required = false) String adviserId,
			@RequestParam(value = "pageNum") int pageNum, @RequestParam(value = "pageSize") int pageSize,
			HttpServletRequest request, HttpServletResponse response) {
		// 更改当前顾问编号
		Integer newAdviserId = getAdviserId(request);
		if (newAdviserId != null)
			adviserId = newAdviserId + "";
		try {
			super.setGetHeader(response);
			return new Response<List<SchoolBrokerageSaByDashboardListDTO>>(0,
					schoolBrokerageSaService.listSchoolBrokerageSaByDashboard(
							StringUtil.isNotEmpty(adviserId) ? StringUtil.toInt(adviserId) : 0, pageNum, pageSize));
		} catch (ServiceException e) {
			return new Response<List<SchoolBrokerageSaByDashboardListDTO>>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/get", method = RequestMethod.GET)
	@ResponseBody
	public Response<SchoolBrokerageSaDTO> getSchoolBrokerageSa(@RequestParam(value = "id") int id,
			HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<SchoolBrokerageSaDTO>(0, schoolBrokerageSaService.getSchoolBrokerageSaById(id));
		} catch (ServiceException e) {
			return new Response<SchoolBrokerageSaDTO>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/close", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> closeSchoolBrokerageSa(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			SchoolBrokerageSaDTO schoolBrokerageSaDto = schoolBrokerageSaService.getSchoolBrokerageSaById(id);
			if (schoolBrokerageSaDto != null) {
				return new Response<Integer>(0, schoolBrokerageSaService.updateClose(id, true));
			} else {
				return new Response<Integer>(2, "数据" + id + "不存在.", 0);
			}
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/reopen", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> reopenSchoolBrokerageSa(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			SchoolBrokerageSaDTO schoolBrokerageSaDto = schoolBrokerageSaService.getSchoolBrokerageSaById(id);
			if (schoolBrokerageSaDto != null) {
				return new Response<Integer>(0, schoolBrokerageSaService.updateClose(id, false));
			} else {
				return new Response<Integer>(2, "数据" + id + "不存在.", 0);
			}
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/delete", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> deleteSchoolBrokerageSa(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, schoolBrokerageSaService.deleteSchoolBrokerageSaById(id));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), 0);
		}
	}

}
