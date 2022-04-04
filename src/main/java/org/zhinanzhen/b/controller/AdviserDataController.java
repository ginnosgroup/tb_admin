package org.zhinanzhen.b.controller;

import java.io.InputStream;
import java.io.OutputStream;
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
import org.zhinanzhen.b.service.AdviserDataService;
import org.zhinanzhen.b.service.pojo.AdviserCommissionOrderDTO;
import org.zhinanzhen.b.service.pojo.AdviserServiceOrderDTO;
import org.zhinanzhen.b.service.pojo.AdviserUserDTO;
import org.zhinanzhen.b.service.pojo.AdviserVisaDTO;
import org.zhinanzhen.tb.controller.BaseController;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/adviserData")
public class AdviserDataController extends BaseController {

	@Resource
	AdviserDataService adviserDataService;

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
				sheet0.addCell(new Label(2, i, so.getType(), cellFormat));
				sheet0.addCell(new Label(3, i, so.getUserName(), cellFormat));
				sheet0.addCell(new Label(4, i, so.getMaraName(), cellFormat));
				sheet0.addCell(new Label(5, i, so.getOfficialName(), cellFormat));
				sheet0.addCell(new Label(6, i, so.getReceiveTypeName(), cellFormat));
				sheet0.addCell(new Label(7, i, so.getServiceOrderReceiveDate(), cellFormat));
				sheet0.addCell(new Label(8, i, so.getServiceName(), cellFormat));
				sheet0.addCell(new Label(9, i, so.getServiceOrderState(), cellFormat));
				sheet0.addCell(new Label(10, i, so.getIsSettle(), cellFormat));
				sheet0.addCell(new Label(11, i, so.getPerAmount(), cellFormat));
				sheet0.addCell(new Label(12, i, so.getAmount(), cellFormat));
				sheet0.addCell(new Label(13, i, so.getExpectAmount(), cellFormat));
				sheet0.addCell(new Label(14, i, so.getRemarks(), cellFormat));
				i++;
			}
			WritableSheet sheet1 = wbe.getSheet(1);
			int j = 1;
			for (AdviserVisaDTO v : visaList) {
				sheet1.addCell(new Label(0, j, v.getId() + "", cellFormat));
				sheet1.addCell(new Label(1, j, v.getGmtCreate(), cellFormat));
				sheet1.addCell(new Label(2, j, v.getServiceOrderId(), cellFormat));
				sheet1.addCell(new Label(3, j, v.getUserName(), cellFormat));
				sheet1.addCell(new Label(4, j, v.getMaraName(), cellFormat));
				sheet1.addCell(new Label(5, j, v.getOfficialName(), cellFormat));
				sheet1.addCell(new Label(6, j, v.getKjApprovalDate(), cellFormat));
				sheet1.addCell(new Label(7, j, v.getReceiveTypeName(), cellFormat));
				sheet1.addCell(new Label(8, j, v.getReceiveDate(), cellFormat));
				sheet1.addCell(new Label(9, j, v.getServiceName(), cellFormat));
				sheet1.addCell(new Label(10, j, v.getGst(), cellFormat));
				sheet1.addCell(new Label(11, j, v.getBonus(), cellFormat));
				sheet1.addCell(new Label(12, j, v.getBonusDate(), cellFormat));
				sheet1.addCell(new Label(13, j, v.getCommissionState(), cellFormat));
				sheet1.addCell(new Label(14, j, v.getRemarks(), cellFormat));
				j++;
			}
			WritableSheet sheet2 = wbe.getSheet(2);
			int k = 1;
			for (AdviserCommissionOrderDTO c : commissionOrderList) {
				sheet2.addCell(new Label(0, k, c.getId() + "", cellFormat));
				sheet2.addCell(new Label(1, k, c.getGmtCreate(), cellFormat));
				sheet2.addCell(new Label(2, k, c.getServiceOrderId(), cellFormat));
				sheet2.addCell(new Label(3, k, c.getUserName(), cellFormat));
				sheet2.addCell(new Label(4, k, c.getOfficialName(), cellFormat));
				sheet2.addCell(new Label(5, k, c.getKjApprovalDate(), cellFormat));
				sheet2.addCell(new Label(6, k, c.getStudentCode(), cellFormat));
				sheet2.addCell(new Label(7, k, c.getSchoolPaymentAmount(), cellFormat));
				sheet2.addCell(new Label(8, k, c.getSchoolPaymentDate(), cellFormat));
				sheet2.addCell(new Label(9, k, c.getTuitionFee(), cellFormat));
				sheet2.addCell(new Label(10, k, c.getPerTermTuitionFee(), cellFormat));
				sheet2.addCell(new Label(11, k, c.getReceiveTypeName(), cellFormat));
				sheet2.addCell(new Label(12, k, c.getReceiveDate(), cellFormat));
				sheet2.addCell(new Label(13, k, c.getServiceName(), cellFormat));
				sheet2.addCell(new Label(14, k, c.getPerAmount(), cellFormat));
				sheet2.addCell(new Label(15, k, c.getAmount(), cellFormat));
				sheet2.addCell(new Label(16, k, c.getExpectAmount(), cellFormat));
				sheet2.addCell(new Label(17, k, c.getSureExpectAmount(), cellFormat));
				sheet2.addCell(new Label(18, k, c.getGst(), cellFormat));
				sheet2.addCell(new Label(19, k, c.getBonus(), cellFormat));
				sheet2.addCell(new Label(20, k, c.getBonusDate(), cellFormat));
				sheet2.addCell(new Label(21, k, c.getCommissionState(), cellFormat));
				sheet2.addCell(new Label(22, k, c.getRemarks(), cellFormat));
				k++;
			}
			WritableSheet sheet3 = wbe.getSheet(3);
			int l = 1;
			for (AdviserUserDTO u : userList) {
				sheet3.addCell(new Label(0, l, u.getId() + "", cellFormat));
				sheet3.addCell(new Label(1, l, u.getName(), cellFormat));
				sheet3.addCell(new Label(2, l, u.getBirthday(), cellFormat));
				sheet3.addCell(new Label(3, l, u.getPhone(), cellFormat));
				sheet3.addCell(new Label(4, l, u.getEmail(), cellFormat));
				sheet3.addCell(new Label(5, l, u.getIsCreater(), cellFormat));
				l++;
			}
			wbe.write();
			wbe.close();

		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

}
