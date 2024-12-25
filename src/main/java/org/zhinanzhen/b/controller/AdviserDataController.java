package org.zhinanzhen.b.controller;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zhinanzhen.b.dao.pojo.ServiceAssessDO;
import org.zhinanzhen.tb.service.AdviserService;
import org.zhinanzhen.b.service.AdviserDataService;
import org.zhinanzhen.b.service.ServiceAssessService;
import org.zhinanzhen.b.service.ServicePackageService;
import org.zhinanzhen.b.service.SchoolInstitutionService;
import org.zhinanzhen.b.service.SchoolService;
import org.zhinanzhen.b.service.SchoolCourseService;
import org.zhinanzhen.b.service.pojo.AdviserCommissionOrderDTO;
import org.zhinanzhen.b.service.pojo.AdviserServiceOrderDTO;
import org.zhinanzhen.b.service.pojo.AdviserUserDTO;
import org.zhinanzhen.b.service.pojo.AdviserVisaDTO;
import org.zhinanzhen.b.service.pojo.SchoolCourseDTO;
import org.zhinanzhen.b.service.pojo.SchoolDTO;
import org.zhinanzhen.b.service.pojo.SchoolInstitutionDTO;
import org.zhinanzhen.b.service.pojo.ServicePackageDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

import com.ikasoa.core.utils.ListUtil;
import com.ikasoa.core.utils.MapUtil;
import com.ikasoa.core.utils.ObjectUtil;
import com.ikasoa.core.utils.StringUtil;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import org.zhinanzhen.tb.utils.Base64Util;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/adviserData")
public class AdviserDataController extends BaseController {
	
	private static final Logger LOG = LoggerFactory.getLogger(AdviserDataController.class);
	
	@Resource
	AdviserService adviserService;

	@Resource
	AdviserDataService adviserDataService;
	
	@Resource
	ServiceAssessService serviceAssessService;
	
	@Resource
	ServicePackageService servicePackageService;
	
	@Resource
	SchoolInstitutionService schoolInstitutionService;
	
	@Resource
	SchoolCourseService schoolCourseService;
	
	@Resource
	SchoolService schoolService;

	@RequestMapping(value = "/down", method = RequestMethod.GET)
	@ResponseBody
	public void down(@RequestParam(value = "adviserId", required = false) Integer adviserId, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			List<AdviserServiceOrderDTO> serviceOrderList = adviserDataService.listServiceOrder(adviserId);
			List<AdviserVisaDTO> visaList = adviserDataService.listVisa(adviserId);
			List<AdviserCommissionOrderDTO> commissionOrderList = adviserDataService.listCommissionOrder(adviserId);
			List<AdviserUserDTO> userList = adviserDataService.listUser(adviserId);

			response.reset();// 清空输出流
			String tableName = "AdviserData-" + adviserId;
			response.setHeader("Content-disposition",
					"attachment; filename=" + new String(tableName.getBytes("GB2312"), "8859_1") + ".xls");
			response.setContentType("application/msexcel");

			OutputStream os = response.getOutputStream();
			jxl.Workbook wb;
			InputStream is;
			try {
				is = this.getClass().getResourceAsStream("/AdviserDataTemplate.xls");
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
			WritableCellFormat cellFormat = new WritableCellFormat();
			WritableSheet sheet0 = wbe.getSheet(0);
			int i = 1;
			for (AdviserServiceOrderDTO so : serviceOrderList) {
				sheet0.addCell(new Label(0, i, so.getId() + "", cellFormat));
				sheet0.addCell(new Label(1, i, so.getGmtCreate(), cellFormat));
				if (StringUtil.equals("签证", so.getType()) && StringUtil.isNotEmpty(so.getServiceName()))
					sheet0.addCell(new Label(2, i, so.getServiceName(), cellFormat));
				else
					sheet0.addCell(new Label(2, i, so.getType(), cellFormat));
				sheet0.addCell(new Label(3, i, so.getUserName(), cellFormat));
				sheet0.addCell(new Label(4, i, so.getApplicantName(), cellFormat));
				sheet0.addCell(new Label(5, i, so.getMaraName(), cellFormat));
				sheet0.addCell(new Label(6, i, so.getOfficialName(), cellFormat));
				sheet0.addCell(new Label(7, i, so.getCurrency(), cellFormat));
				sheet0.addCell(new Label(8, i, so.getExchangeRate() + "", cellFormat));
				sheet0.addCell(new Label(9, i, so.getReceiveTypeName(), cellFormat));
				sheet0.addCell(new Label(10, i, so.getServiceOrderReceiveDate(), cellFormat));
				if (so.getServicePackageId() > 0) {
					ServicePackageDTO servicePackageDto = servicePackageService.getById(so.getServicePackageId());
					String str = " - ";
					if ("CA".equals(servicePackageDto.getType()))
						str += "职业评估";
					if ("EOI".equals(servicePackageDto.getType()))
						str += "EOI";
					if ("SA".equals(servicePackageDto.getType()))
						str += "学校申请";
					if ("VA".equals(servicePackageDto.getType()))
						str += "签证申请";
					if ("ZD".equals(servicePackageDto.getType()))
						str += "州担";
					if ("MAT".equals(servicePackageDto.getType()))
						str += "Matrix";
					if ("ROI".equals(servicePackageDto.getType()))
						str += "ROI";
					if ("SBO".equals(servicePackageDto.getType()))
						str += "SBO";
					if ("TM".equals(servicePackageDto.getType()))
						str += "提名";
					if ("DB".equals(servicePackageDto.getType()))
						str += "担保";
					sheet0.addCell(new Label(11, i, so.getServiceCode() + str, cellFormat));
				} else
					sheet0.addCell(new Label(11, i, so.getServiceCode(), cellFormat));
				if (StringUtil.isNotEmpty(so.getServiceAssessId())) {
					ServiceAssessDO serviceAssessDo = serviceAssessService.seleteAssessById(so.getServiceAssessId());
					if (ObjectUtil.isNotNull(serviceAssessDo))
						sheet0.addCell(new Label(11, i,
								StringUtil.merge(so.getServiceCode(), " - ", serviceAssessDo.getName()), cellFormat));
				}
				if (StringUtil.isNotEmpty(so.getInstitutionTradingName()))
					sheet0.addCell(new Label(12, i, so.getInstitutionTradingName(), cellFormat));
				else if (so.getCourseId() > 0) { // 新学校库
					SchoolCourseDTO schoolCourseDto = schoolCourseService.schoolCourseById(so.getCourseId());
					if (ObjectUtil.isNotNull(schoolCourseDto)) {
						SchoolInstitutionDTO schoolInstitutionDto = schoolInstitutionService
								.getSchoolInstitutionById(schoolCourseDto.getProviderId());
						if (ObjectUtil.isNotNull(schoolInstitutionDto))
							sheet0.addCell(new Label(12, i, schoolInstitutionDto.getInstitutionName(), cellFormat));
					}
				} else if (so.getSchoolId() > 0) { // 旧学校库
					SchoolDTO schoolDto = schoolService.getSchoolById(so.getSchoolId());
					if (ObjectUtil.isNotNull(schoolDto))
						sheet0.addCell(new Label(12, i, schoolDto.getName(), cellFormat));
				}
				if (StringUtil.equals("独立技术移民", so.getType()) && so.isSubmitted())
					sheet0.addCell(new Label(13, i, "已提交佣金表", cellFormat));
				else
					sheet0.addCell(new Label(13, i, so.getServiceOrderState(), cellFormat));
				sheet0.addCell(new Label(14, i, so.getIsSettle(), cellFormat));
				sheet0.addCell(new Label(15, i, so.getPerAmount(), cellFormat));
				sheet0.addCell(new Label(16, i, so.getAmount(), cellFormat));
				sheet0.addCell(new Label(17, i, so.getExpectAmount(), cellFormat));
				sheet0.addCell(new Label(18, i, so.getRemarks(), cellFormat));
				i++;
			}
			WritableSheet sheet1 = wbe.getSheet(1);
			int j = 1;
			for (AdviserVisaDTO v : visaList) {
				sheet1.addCell(new Label(0, j, v.getId() + "", cellFormat));
				sheet1.addCell(new Label(1, j, v.getGmtCreate(), cellFormat));
				sheet1.addCell(new Label(2, j, v.getServiceOrderId(), cellFormat));
				sheet1.addCell(new Label(3, j, v.getUserName(), cellFormat));
				sheet1.addCell(new Label(4, j, v.getApplicantName(), cellFormat));
				sheet1.addCell(new Label(5, j, v.getMaraName(), cellFormat));
				sheet1.addCell(new Label(6, j, v.getOfficialName(), cellFormat));
				sheet1.addCell(new Label(7, i, v.getCurrency(), cellFormat));
				sheet1.addCell(new Label(8, i, v.getExchangeRate() + "", cellFormat));
//				sheet1.addCell(new Label(9, j, v.getKjApprovalDate(), cellFormat));
				sheet1.addCell(new Label(9, j, v.getReceiveTypeName(), cellFormat));
				sheet1.addCell(new Label(10, j, v.getReceiveDate(), cellFormat));
				sheet1.addCell(new Label(11, j, v.getServiceCode(), cellFormat));
				sheet1.addCell(new Label(12, j, v.getGst(), cellFormat));
				sheet1.addCell(new Label(13, j, v.getBonus(), cellFormat));
				sheet1.addCell(new Label(14, j, v.getBonusDate(), cellFormat));
				sheet1.addCell(new Label(15, j, v.getCommissionState(), cellFormat));
				sheet1.addCell(new Label(16, j, v.getRemarks(), cellFormat));
				j++;
			}
			WritableSheet sheet2 = wbe.getSheet(2);
			int k = 1;
			for (AdviserCommissionOrderDTO c : commissionOrderList) {
				sheet2.addCell(new Label(0, k, c.getId() + "", cellFormat));
				sheet2.addCell(new Label(1, k, c.getGmtCreate(), cellFormat));
				sheet2.addCell(new Label(2, k, c.getServiceOrderId(), cellFormat));
				sheet2.addCell(new Label(3, k, c.getUserName(), cellFormat));
				sheet2.addCell(new Label(4, k, c.getApplicantName(), cellFormat));
				sheet2.addCell(new Label(5, k, c.getOfficialName(), cellFormat));
				sheet2.addCell(new Label(6, i, c.getCurrency(), cellFormat));
				sheet2.addCell(new Label(7, i, c.getExchangeRate() + "", cellFormat));
				sheet2.addCell(new Label(8, k, c.getKjApprovalDate(), cellFormat));
				sheet2.addCell(new Label(9, k, c.getStudentCode(), cellFormat));
				sheet2.addCell(new Label(10, k, c.getSchoolPaymentAmount(), cellFormat));
				sheet2.addCell(new Label(11, k, c.getSchoolPaymentDate(), cellFormat));
				sheet2.addCell(new Label(12, k, c.getTuitionFee(), cellFormat));
				sheet2.addCell(new Label(13, k, c.getPerTermTuitionFee(), cellFormat));
				sheet2.addCell(new Label(14, k, c.getReceiveTypeName(), cellFormat));
				sheet2.addCell(new Label(15, k, c.getReceiveDate(), cellFormat));
				sheet2.addCell(new Label(16, k, c.getPerAmount(), cellFormat));
				sheet2.addCell(new Label(17, k, c.getAmount(), cellFormat));
				sheet2.addCell(new Label(18, k, c.getExpectAmount(), cellFormat));
				sheet2.addCell(new Label(19, k, c.getSureExpectAmount(), cellFormat));
				sheet2.addCell(new Label(20, k, c.getGst(), cellFormat));
				sheet2.addCell(new Label(21, k, c.getBonus(), cellFormat));
				sheet2.addCell(new Label(21, k, c.getBonusDate(), cellFormat));
				sheet2.addCell(new Label(23, k, c.getCommissionState(), cellFormat));
				sheet2.addCell(new Label(24, k, c.getRemarks(), cellFormat));
				k++;
			}
			WritableSheet sheet3 = wbe.getSheet(3);
			int l = 1;
			for (AdviserUserDTO u : userList) {
				sheet3.addCell(new Label(0, l, u.getId() + "", cellFormat));
				sheet3.addCell(new Label(1, l, u.getName(), cellFormat));
				String nickname = u.getNickname();
				if (StringUtil.isNotEmpty(nickname) && nickname.contains("�")) {
					sheet3.addCell(new Label(2, l, u.getName(), cellFormat));
				} else {
					byte[] bytes = Base64Util.decodeBase64(nickname);
					if (bytes != null) {
						nickname = new String(bytes, "utf-8");
						nickname = nickname.replaceAll("[\\p{C}]", "");
					}
					if (StringUtil.isNotEmpty(nickname) && nickname.contains("�")) {
						sheet3.addCell(new Label(2, l, u.getName(), cellFormat));
					} else {
						sheet3.addCell(new Label(2, l, nickname, cellFormat));
					}
//					boolean matches = nickname.matches(".*[^\\x20-\\x7E]+.*");
//					if (matches) {
//						sheet3.addCell(new Label(2, l, u.getName(), cellFormat));
//					} else {
//						sheet3.addCell(new Label(2, l, nickname, cellFormat));
//					}
				}
				sheet3.addCell(new Label(3, l, u.getWeichatUsername(), cellFormat));
				sheet3.addCell(new Label(4, l, u.getApplicantId() + "", cellFormat));
				sheet3.addCell(new Label(5, l, u.getApplicantName(), cellFormat));
				sheet3.addCell(new Label(6, l, u.getBirthday(), cellFormat));
				sheet3.addCell(new Label(7, l, u.getPhone(), cellFormat));
				sheet3.addCell(new Label(8, l, u.getEmail(), cellFormat));
				sheet3.addCell(new Label(9, l, u.getIsCreater(), cellFormat));
				l++;
			}
			wbe.write();
			wbe.close();

		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}
	
	@RequestMapping(value = "/adviserDataMigration", method = RequestMethod.POST)
	@ResponseBody
	public Response<Map<String, Integer>> adviserDataMigration(@RequestParam(value = "newAdviserId", required = true) Integer newAdviserId, @RequestParam(value = "adviserId", required = true) Integer adviserId, @RequestParam(value = "userIds", required = false) String userIds, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo == null || (!"SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())))
				return new Response(1, "仅限超级管理员操作.", null);
			if (adviserService.getAdviserById(adviserId) == null)
				return new Response<Map<String, Integer>>(1, StringUtil.merge("原顾问ID错误或不存在:", adviserId),
						MapUtil.newHashMap());
			if (adviserService.getAdviserById(newAdviserId) == null)
				return new Response<Map<String, Integer>>(1, StringUtil.merge("新顾问ID错误或不存在:", newAdviserId),
						MapUtil.newHashMap());
			List<Integer> userIdList = ListUtil.newArrayList();
			if (StringUtil.isNotEmpty(userIds)) {
				String[] userIdStrs = userIds.split(",");
				for (String userIdStr : userIdStrs) {
					if (!"".equals(userIdStr))
						userIdList.add(Integer.parseInt(userIdStr.trim()));
				}
			} else
				userIdList = null;
			LOG.info(StringUtil.merge("顾问数据迁移:adviserId=", adviserId, "newAdviserId=", newAdviserId, "userIds=",
					userIds));
			return new Response<Map<String, Integer>>(0,
					adviserDataService.adviserDataMigration(newAdviserId, adviserId, userIdList, adminUserLoginInfo.getId(), adminUserLoginInfo.getApList()));
		} catch (ServiceException e) {
			return new Response<Map<String, Integer>>(1, StringUtil.merge("迁移失败:", e.getMessage()),
					MapUtil.newHashMap());
		}
	}
	
	@RequestMapping(value = "/checkAdviserDataMigration", method = RequestMethod.GET)
	@ResponseBody
	public Response<Map<String, Integer>> checkAdviserDataMigration(@RequestParam(value = "newAdviserId", required = true) Integer newAdviserId, @RequestParam(value = "adviserId", required = true) Integer adviserId, @RequestParam(value = "userIds", required = false) String userIds, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			if (adviserService.getAdviserById(adviserId) == null)
				return new Response<Map<String, Integer>>(1, StringUtil.merge("原顾问ID错误或不存在:", adviserId),
						MapUtil.newHashMap());
			if (adviserService.getAdviserById(newAdviserId) == null)
				return new Response<Map<String, Integer>>(1, StringUtil.merge("新顾问ID错误或不存在:", newAdviserId),
						MapUtil.newHashMap());
			List<Integer> userIdList = ListUtil.newArrayList();
			if (StringUtil.isNotEmpty(userIds)) {
				String[] userIdStrs = userIds.split(",");
				for (String userIdStr : userIdStrs) {
					if (!"".equals(userIdStr))
						userIdList.add(Integer.parseInt(userIdStr.trim()));
				}
			} else
				userIdList = null;
			LOG.info(StringUtil.merge("顾问数据统计:adviserId=", adviserId, "newAdviserId=", newAdviserId, "userIds=",
					userIds));
			return new Response<Map<String, Integer>>(0,
					adviserDataService.checkAdviserDataMigration(newAdviserId, adviserId, userIdList));
		} catch (ServiceException e) {
			return new Response<Map<String, Integer>>(1, StringUtil.merge("迁移失败:", e.getMessage()),
					MapUtil.newHashMap());
		}
	}

}
