package org.zhinanzhen.b.controller;

import java.util.Date;

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
			@RequestParam(value = "subagencyId") String subagencyId, HttpServletRequest request,
			HttpServletResponse response) {
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
			@RequestParam(value = "subagencyId", required = false) String subagencyId, HttpServletRequest request,
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
