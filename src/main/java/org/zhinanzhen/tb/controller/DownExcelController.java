package org.zhinanzhen.tb.controller;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zhinanzhen.b.controller.BaseCommissionOrderController.ReviewKjStateEnum;
import org.zhinanzhen.b.service.BrokerageSaService;
import org.zhinanzhen.b.service.BrokerageService;
import org.zhinanzhen.b.service.CommissionOrderService;
import org.zhinanzhen.b.service.RefundService;
import org.zhinanzhen.b.service.SchoolBrokerageSaService;
import org.zhinanzhen.b.service.VisaService;
import org.zhinanzhen.b.service.pojo.BrokerageDTO;
import org.zhinanzhen.b.service.pojo.BrokerageSaDTO;
import org.zhinanzhen.b.service.pojo.CommissionOrderReportDTO;
import org.zhinanzhen.b.service.pojo.RefundDTO;
import org.zhinanzhen.b.service.pojo.SchoolBrokerageSaDTO;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.b.service.pojo.VisaDTO;
import org.zhinanzhen.b.service.pojo.VisaReportDTO;
import org.zhinanzhen.tb.service.AdviserService;
import org.zhinanzhen.tb.service.OrderPayTypeEnum;
import org.zhinanzhen.tb.service.OrderService;
import org.zhinanzhen.tb.service.OrderStateEnum;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.UserAuthTypeEnum;
import org.zhinanzhen.tb.service.UserService;
import org.zhinanzhen.tb.service.pojo.AdviserDTO;
import org.zhinanzhen.tb.service.pojo.OrderDTO;
import org.zhinanzhen.tb.service.pojo.UserDTO;
import org.zhinanzhen.tb.utils.ConfigService;

import com.ikasoa.core.utils.StringUtil;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Colour;
import jxl.format.UnderlineStyle;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import lombok.AllArgsConstructor;
import lombok.Data;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/down")
public class DownExcelController extends BaseController {
	@Resource
	private ConfigService configService;
	@Resource
	private UserService userService;
	@Resource
	private OrderService orderService;

	@Resource
	VisaService visaService;

	@Resource
	CommissionOrderService commissionOrderService;

	@Resource
	BrokerageService brokerageService;

	@Resource
	BrokerageSaService brokerageSaService;

	@Resource
	SchoolBrokerageSaService schoolBrokerageSaService;

	@Resource
	RefundService refundService;

	@Resource
	AdviserService adviserService;

	@RequestMapping("/user")
	public void userExport(String name, String authType, String authNickname, String phone,
			HttpServletResponse response) throws Exception {
		OutputStream os = response.getOutputStream();// 取得输出流
		response.reset();// 清空输出流
		String tableName = "user_information";
		response.setHeader("Content-disposition",
				"attachment; filename=" + new String(tableName.getBytes("GB2312"), "8859_1") + ".xls");// 设定输出文件头
		response.setContentType("application/msexcel");
		String inpath = "/UserTemplate2.xls";
		UserAuthTypeEnum authTypeEnum = null;
		if (StringUtil.isNotEmpty(authType)) {
			authTypeEnum = UserAuthTypeEnum.get(authType);
		}
		List<UserDTO> userDtoList = userService.listUser(name, authTypeEnum, authNickname, phone, null, 0, 0, 10000);
		downUserUtil(os, inpath, userDtoList);
	}

	@RequestMapping("/userByRecommendUserId")
	public void userExport(int userId, HttpServletResponse response) throws Exception {
		OutputStream os = response.getOutputStream();// 取得输出流
		response.reset();// 清空输出流
		String tableName = "user_information";
		response.setHeader("Content-disposition",
				"attachment; filename=" + new String(tableName.getBytes("GB2312"), "8859_1") + ".xls");// 设定输出文件头
		response.setContentType("application/msexcel");
		String inpath = "/UserTemplate2.xls";
		UserDTO userDto = userService.getUserById(userId);
		String AuthOpenId = userDto.getAuthOpenid();
		List<UserDTO> userDtoList = userService.listUserByRecommendOpenId(AuthOpenId);
		downUserUtil(os, inpath, userDtoList);
	}

	@RequestMapping("/order")
	public void orderExport(Integer orderId, String name, Integer regionId, String state, String userName,
			String userPhone, HttpServletResponse response) throws Exception {
		if (orderId != null && orderId == 0) {
			orderId = null;
		}
		if (regionId != null && regionId == 0) {
			regionId = null;
		}
		OutputStream os = response.getOutputStream();// 取得输出流
		response.reset();// 清空输出流
		String tableName = "order_information";
		response.setHeader("Content-disposition",
				"attachment; filename=" + new String(tableName.getBytes("GB2312"), "8859_1") + ".xls");// 设定输出文件头
		response.setContentType("application/msexcel");
		String inpath = "/OrderTemplate2.xls";
		OrderStateEnum stateEnum = null;
		if (StringUtil.isNotEmpty(state)) {
			stateEnum = OrderStateEnum.get(state);
			if (stateEnum == null) {
				throw new Exception("状态参数错误 state = " + state);
			}
		}
		List<OrderDTO> orderDtoList = orderService.listOrder(orderId, name, regionId, stateEnum, userName, userPhone, 0,
				10000);
		List<OrderDTO> orderDtoList2 = new ArrayList<OrderDTO>();
		for (OrderDTO orderDto : orderDtoList) {
			if ("虚拟订单".equals(orderDto.getName())) {
				continue;
			}
			orderDtoList2.add(orderDto);
		}
		downOrderUtil(os, inpath, orderDtoList2);
	}

	@RequestMapping("/visa")
	public void visaExport(@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "startHandlingDate", required = false) String startHandlingDate,
			@RequestParam(value = "endHandlingDate", required = false) String endHandlingDate,
			@RequestParam(value = "startDate", required = false) String startDate,
			@RequestParam(value = "endDate", required = false) String endDate,
			@RequestParam(value = "adviserId", required = false) Integer adviserId, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		// 更改当前顾问编号
		Integer newAdviserId = getAdviserId(request);
		if (newAdviserId != null)
			adviserId = newAdviserId;

		response.reset();// 清空输出流
		String tableName = "visa_information";
		response.setHeader("Content-disposition",
				"attachment; filename=" + new String(tableName.getBytes("GB2312"), "8859_1") + ".xls");
		response.setContentType("application/msexcel");

		try {
			super.setGetHeader(response);
			List<VisaDTO> visaDtoList = visaService.listVisa(keyword, startHandlingDate, endHandlingDate, null, null,
					null, null, startDate, endDate, null, adviserId, null, 0, 9999);

			OutputStream os = response.getOutputStream();
			jxl.Workbook wb;
			InputStream is;
			try {
				is = this.getClass().getResourceAsStream("/VisaTemplate2.xls");
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
			WritableSheet sheet = wbe.getSheet(0);
			WritableCellFormat cellFormat = new WritableCellFormat();

			int i = 1;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			for (VisaDTO visaDto : visaDtoList) {
				sheet.addCell(new Label(0, i, visaDto.getId() + "", cellFormat));
				sheet.addCell(new Label(1, i, sdf.format(visaDto.getHandlingDate()), cellFormat));
				sheet.addCell(new Label(2, i, visaDto.getUserName(), cellFormat));
				sheet.addCell(new Label(3, i, visaDto.getBirthday() + "", cellFormat));
				sheet.addCell(new Label(4, i, visaDto.getPhone(), cellFormat));
				sheet.addCell(new Label(5, i, sdf.format(visaDto.getReceiveDate()), cellFormat));
				sheet.addCell(new Label(6, i, visaDto.getReceiveTypeName(), cellFormat));
				sheet.addCell(new Label(7, i, visaDto.getServiceCode(), cellFormat));
				sheet.addCell(new Label(8, i, visaDto.getServiceOrderId() + "", cellFormat));
				sheet.addCell(
						new Label(9, i, visaDto.getInstallmentNum() + "/" + visaDto.getInstallment(), cellFormat));
				sheet.addCell(new Label(10, i, visaDto.getReceivable() + "", cellFormat));
				sheet.addCell(new Label(11, i, visaDto.getReceived() + "", cellFormat));
				sheet.addCell(new Label(12, i, visaDto.getPerAmount() + "", cellFormat));
				sheet.addCell(new Label(13, i, visaDto.getAmount() + "", cellFormat));
				sheet.addCell(new Label(14, i, visaDto.getExpectAmount() + "", cellFormat));
				sheet.addCell(new Label(15, i, visaDto.getGst() + "", cellFormat));
				sheet.addCell(new Label(16, i, visaDto.getDeductGst() + "", cellFormat));
				sheet.addCell(new Label(17, i, visaDto.getBonus() + "", cellFormat));
				sheet.addCell(new Label(18, i, visaDto.getAdviserName(), cellFormat));
				sheet.addCell(new Label(19, i, visaDto.getOfficialName(), cellFormat));
				sheet.addCell(new Label(20, i, visaDto.getRemarks(), cellFormat));
				i++;
			}
			wbe.write();
			wbe.close();
		} catch (ServiceException e) {
			return;
		}

	}

	@RequestMapping("/brokerage")
	public void brokerageExport(@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "startHandlingDate", required = false) String startHandlingDate,
			@RequestParam(value = "endHandlingDate", required = false) String endHandlingDate,
			@RequestParam(value = "startDate", required = false) String startDate,
			@RequestParam(value = "endDate", required = false) String endDate,
			@RequestParam(value = "adviserId", required = false) Integer adviserId, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		// 更改当前顾问编号
		Integer newAdviserId = getAdviserId(request);
		if (newAdviserId != null)
			adviserId = newAdviserId;

		response.reset();// 清空输出流
		String tableName = "brokerage_information";
		response.setHeader("Content-disposition",
				"attachment; filename=" + new String(tableName.getBytes("GB2312"), "8859_1") + ".xls");
		response.setContentType("application/msexcel");

		try {
			super.setGetHeader(response);
			List<BrokerageDTO> brokerageDtoList = brokerageService.listBrokerage(keyword, startHandlingDate,
					endHandlingDate, startDate, endDate, adviserId, null, 0, 9999);

			OutputStream os = response.getOutputStream();
			jxl.Workbook wb;
			InputStream is;
			try {
				is = this.getClass().getResourceAsStream("/BrokerageTemplate.xls");
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
			WritableSheet sheet = wbe.getSheet(0);
			WritableCellFormat cellFormat = new WritableCellFormat();

			int i = 1;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			for (BrokerageDTO brokerageDto : brokerageDtoList) {
				sheet.addCell(new Label(0, i, brokerageDto.getId() + "", cellFormat));
				sheet.addCell(new Label(1, i, sdf.format(brokerageDto.getHandlingDate()), cellFormat));
				sheet.addCell(new Label(2, i, brokerageDto.getUserName(), cellFormat));
				sheet.addCell(new Label(3, i, brokerageDto.getBirthday() + "", cellFormat));
				sheet.addCell(new Label(4, i, brokerageDto.getPhone() + "", cellFormat));
				sheet.addCell(new Label(5, i, brokerageDto.getServiceCode(), cellFormat));
				sheet.addCell(new Label(6, i, brokerageDto.getAmount() + "", cellFormat));
				sheet.addCell(new Label(7, i, brokerageDto.getGst() + "", cellFormat));
				sheet.addCell(new Label(8, i, brokerageDto.getDeductGst() + "", cellFormat));
				sheet.addCell(new Label(9, i, brokerageDto.getBonus() + "", cellFormat));
				sheet.addCell(new Label(10, i, brokerageDto.getAdviserName(), cellFormat));
				i++;
			}
			wbe.write();
			wbe.close();
		} catch (ServiceException e) {
			return;
		}

	}

	@RequestMapping("/brokerage_sa")
	public void brokerageSaExport(@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "startHandlingDate", required = false) String startHandlingDate,
			@RequestParam(value = "endHandlingDate", required = false) String endHandlingDate,
			@RequestParam(value = "startDate", required = false) String startDate,
			@RequestParam(value = "endDate", required = false) String endDate,
			@RequestParam(value = "adviserId", required = false) Integer adviserId,
			@RequestParam(value = "schoolId", required = false) Integer schoolId, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		// 更改当前顾问编号
		Integer newAdviserId = getAdviserId(request);
		if (newAdviserId != null)
			adviserId = newAdviserId;

		response.reset();// 清空输出流
		String tableName = "brokerage_sa_information";
		response.setHeader("Content-disposition",
				"attachment; filename=" + new String(tableName.getBytes("GB2312"), "8859_1") + ".xls");
		response.setContentType("application/msexcel");

		try {
			super.setGetHeader(response);
			List<BrokerageSaDTO> brokerageSaDtoList = brokerageSaService.listBrokerageSa(keyword, startHandlingDate,
					endHandlingDate, startDate, endDate, adviserId, schoolId, null, 0, 9999);

			OutputStream os = response.getOutputStream();
			jxl.Workbook wb;
			InputStream is;
			try {
				is = this.getClass().getResourceAsStream("/BrokerageSaTemplate.xls");
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
			WritableSheet sheet = wbe.getSheet(0);
			WritableCellFormat cellFormat = new WritableCellFormat();

			int i = 1;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			for (BrokerageSaDTO brokerageSaDto : brokerageSaDtoList) {
				sheet.addCell(new Label(0, i, brokerageSaDto.getId() + "", cellFormat));
				sheet.addCell(new Label(1, i, sdf.format(brokerageSaDto.getHandlingDate()), cellFormat));
				sheet.addCell(new Label(2, i, brokerageSaDto.getUserName(), cellFormat));
				sheet.addCell(new Label(3, i, brokerageSaDto.getBirthday() + "", cellFormat));
				sheet.addCell(new Label(4, i, brokerageSaDto.getPhone(), cellFormat));
				sheet.addCell(new Label(5, i, brokerageSaDto.getSchoolName(), cellFormat));
				sheet.addCell(new Label(6, i, brokerageSaDto.getSchoolSubject(), cellFormat));
				sheet.addCell(new Label(7, i, sdf.format(brokerageSaDto.getStartDate()), cellFormat));
				sheet.addCell(new Label(8, i, sdf.format(brokerageSaDto.getEndDate()), cellFormat));
				sheet.addCell(new Label(9, i, brokerageSaDto.getReceiveTypeName(), cellFormat));
				sheet.addCell(new Label(10, i, brokerageSaDto.getTuitionFee() + "", cellFormat));
				sheet.addCell(new Label(11, i, brokerageSaDto.getCommission() + "", cellFormat));
				sheet.addCell(new Label(12, i, brokerageSaDto.getGst() + "", cellFormat));
				sheet.addCell(new Label(13, i, brokerageSaDto.getDeductGst() + "", cellFormat));
				sheet.addCell(new Label(14, i, brokerageSaDto.getBonus() + "", cellFormat));
				sheet.addCell(new Label(15, i, brokerageSaDto.getAdviserName(), cellFormat));
				i++;
			}
			wbe.write();
			wbe.close();
		} catch (ServiceException e) {
			return;
		}

	}

	@RequestMapping("/school_brokerage_sa")
	public void schoolBrokerageSaExport(@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "startHandlingDate", required = false) String startHandlingDate,
			@RequestParam(value = "endHandlingDate", required = false) String endHandlingDate,
			@RequestParam(value = "startDate", required = false) String startDate,
			@RequestParam(value = "endDate", required = false) String endDate,
			@RequestParam(value = "adviserId", required = false) Integer adviserId,
			@RequestParam(value = "schoolId", required = false) Integer schoolId,
			@RequestParam(value = "subagencyId", required = false) Integer subagencyId,
			@RequestParam(value = "isSettleAccounts", required = false) Boolean isSettleAccounts,
			HttpServletRequest request, HttpServletResponse response) throws Exception {

		// 更改当前顾问编号
		Integer newAdviserId = getAdviserId(request);
		if (newAdviserId != null)
			adviserId = newAdviserId;

		response.reset();// 清空输出流
		String tableName = "school_brokerage_sa_information";
		response.setHeader("Content-disposition",
				"attachment; filename=" + new String(tableName.getBytes("GB2312"), "8859_1") + ".xls");
		response.setContentType("application/msexcel");

		try {
			super.setGetHeader(response);
			List<SchoolBrokerageSaDTO> schoolBrokerageSaDtoList = schoolBrokerageSaService.listSchoolBrokerageSa(
					keyword, startHandlingDate, endHandlingDate, startDate, endDate, newAdviserId, schoolId,
					subagencyId, null, isSettleAccounts, 0, 9999);

			OutputStream os = response.getOutputStream();
			jxl.Workbook wb;
			InputStream is;
			try {
				is = this.getClass().getResourceAsStream("/SchoolBrokerageSaTemplate.xls");
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
			WritableSheet sheet = wbe.getSheet(0);
			WritableCellFormat cellFormat = new WritableCellFormat();

			int i = 1;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			for (SchoolBrokerageSaDTO schoolBrokerageSaDto : schoolBrokerageSaDtoList) {
				sheet.addCell(new Label(0, i, schoolBrokerageSaDto.getId() + "", cellFormat));
				sheet.addCell(new Label(1, i, sdf.format(schoolBrokerageSaDto.getHandlingDate()), cellFormat));
				sheet.addCell(new Label(2, i, schoolBrokerageSaDto.getUserName(), cellFormat));
				sheet.addCell(new Label(3, i, schoolBrokerageSaDto.getBirthday() + "", cellFormat));
				sheet.addCell(new Label(4, i, schoolBrokerageSaDto.getPhone(), cellFormat));
				sheet.addCell(new Label(5, i, schoolBrokerageSaDto.getSchoolName(), cellFormat));
				sheet.addCell(new Label(6, i, schoolBrokerageSaDto.getStudentCode(), cellFormat));
				sheet.addCell(new Label(7, i, schoolBrokerageSaDto.getSchoolSubject(), cellFormat));
				sheet.addCell(new Label(8, i, sdf.format(schoolBrokerageSaDto.getStartDate()), cellFormat));
				sheet.addCell(new Label(9, i, sdf.format(schoolBrokerageSaDto.getEndDate()), cellFormat));
				sheet.addCell(new Label(10, i, schoolBrokerageSaDto.getTuitionFee() + "", cellFormat));
				sheet.addCell(new Label(11, i, schoolBrokerageSaDto.getFirstTermTuitionFee() + "", cellFormat));
				sheet.addCell(new Label(12, i, schoolBrokerageSaDto.getCommission() + "", cellFormat));
				sheet.addCell(new Label(13, i, schoolBrokerageSaDto.getSubagencyName(), cellFormat));
				sheet.addCell(new Label(14, i, schoolBrokerageSaDto.getAdviserName(), cellFormat));
				i++;
			}
			wbe.write();
			wbe.close();
		} catch (ServiceException e) {
			return;
		}

	}

	@RequestMapping("/refund")
	public void refund(@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "startHandlingDate", required = false) String startHandlingDate,
			@RequestParam(value = "endHandlingDate", required = false) String endHandlingDate,
			@RequestParam(value = "startDate", required = false) String startDate,
			@RequestParam(value = "endDate", required = false) String endDate,
			@RequestParam(value = "adviserId", required = false) Integer adviserId,
			@RequestParam(value = "officialId", required = false) Integer officialId, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		// 更改当前顾问编号
		Integer newAdviserId = getAdviserId(request);
		if (newAdviserId != null)
			adviserId = newAdviserId;

		response.reset();// 清空输出流
		String tableName = "refund_information";
		response.setHeader("Content-disposition",
				"attachment; filename=" + new String(tableName.getBytes("GB2312"), "8859_1") + ".xls");
		response.setContentType("application/msexcel");

		try {
			super.setGetHeader(response);
			List<RefundDTO> refundDtoList = refundService.listRefund(keyword, startHandlingDate, endHandlingDate,
					startDate, endDate, newAdviserId, officialId, null, 0, 9999);

			OutputStream os = response.getOutputStream();
			jxl.Workbook wb;
			InputStream is;
			try {
				is = this.getClass().getResourceAsStream("/RefundTemplate.xls");
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
			WritableSheet sheet = wbe.getSheet(0);
			WritableCellFormat cellFormat = new WritableCellFormat();

			int i = 1;
			for (RefundDTO refundDto : refundDtoList) {
				sheet.addCell(new Label(0, i, refundDto.getId() + "", cellFormat));
				sheet.addCell(new Label(1, i, refundDto.getUserName(), cellFormat));
				sheet.addCell(new Label(2, i, refundDto.getName(), cellFormat));
				sheet.addCell(new Label(3, i, refundDto.getAmount() + "", cellFormat));
				sheet.addCell(new Label(4, i, refundDto.getBankName(), cellFormat));
				sheet.addCell(new Label(5, i, refundDto.getBankAccount(), cellFormat));
				sheet.addCell(new Label(6, i, refundDto.getBsb(), cellFormat));
				sheet.addCell(new Label(7, i, refundDto.getAdviserName(), cellFormat));
				i++;
			}
			wbe.write();
			wbe.close();
		} catch (ServiceException e) {
			return;
		}

	}

	@RequestMapping("/merge")
	public void mergeExport(@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "startHandlingDate", required = false) String startHandlingDate,
			@RequestParam(value = "endHandlingDate", required = false) String endHandlingDate,
			@RequestParam(value = "startDate", required = false) String startDate,
			@RequestParam(value = "endDate", required = false) String endDate,
			@RequestParam(value = "adviserId") Integer adviserId,
			@RequestParam(value = "schoolId", required = false) Integer schoolId,
			@RequestParam(value = "subagencyId", required = false) Integer subagencyId,
			@RequestParam(value = "officialId", required = false) Integer officialId,
			@RequestParam(value = "isSettleAccounts", required = false) Boolean isSettleAccounts,
			HttpServletRequest request, HttpServletResponse response) throws Exception {

		// 更改当前顾问编号
		Integer newAdviserId = getAdviserId(request);
		if (newAdviserId != null)
			adviserId = newAdviserId;

		response.reset();// 清空输出流
		String tableName = "merge_information";
		response.setHeader("Content-disposition",
				"attachment; filename=" + new String(tableName.getBytes("GB2312"), "8859_1") + ".xls");
		response.setContentType("application/msexcel");

		try {
			super.setGetHeader(response);

			AdviserDTO adviserDto = adviserService.getAdviserById(adviserId);

			// 签证类
			List<VisaDTO> visaDtoList = visaService.listVisa(keyword, startHandlingDate, endHandlingDate, null, null,
					null, null, startDate, endDate, null, adviserId, null, 0, 15);

			// ?
			// List<BrokerageDTO> brokerageDtoList =
			// brokerageService.listBrokerage(keyword, startHandlingDate,
			// endHandlingDate, startDate, endDate, adviserId, null, 0, 15);

			// 提前扣除学校佣金的学生表
			List<BrokerageSaDTO> brokerageSaDtoList = brokerageSaService.listBrokerageSa(keyword, startHandlingDate,
					endHandlingDate, startDate, endDate, adviserId, schoolId, null, 0, 10);

			// 需问学校追要佣金的学生表
			List<SchoolBrokerageSaDTO> schoolBrokerageSaDtoList = schoolBrokerageSaService.listSchoolBrokerageSa(
					keyword, startHandlingDate, endHandlingDate, startDate, endDate, adviserId, schoolId, subagencyId,
					null, isSettleAccounts, 0, 10);

			// 申请退款
			List<RefundDTO> refundDtoList = refundService.listRefund(keyword, startHandlingDate, endHandlingDate,
					startDate, endDate, adviserId, officialId, null, 0, 10);

			OutputStream os = response.getOutputStream();
			jxl.Workbook wb;
			InputStream is;
			try {
				is = this.getClass().getResourceAsStream("/MergeTemplate.xls");
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
			WritableSheet sheet = wbe.getSheet(0);
			WritableCellFormat cellFormat = new WritableCellFormat();
			cellFormat.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			if (adviserDto != null) {
				WritableCellFormat _cellFormat = new WritableCellFormat();
				_cellFormat.setFont(new WritableFont(WritableFont.ARIAL, 14, WritableFont.BOLD, false,
						UnderlineStyle.NO_UNDERLINE, Colour.BLACK));
				sheet.addCell(new Label(4, 0, "顾问名：" + adviserDto.getName(), _cellFormat));
			}

			int i = 4;
			for (VisaDTO visaDto : visaDtoList) {
				sheet.addCell(new Label(0, i, sdf.format(visaDto.getHandlingDate()), cellFormat));
				sheet.addCell(new Label(1, i, visaDto.getUserName(), cellFormat));
				sheet.addCell(new Label(2, i, visaDto.getBirthday() + "", cellFormat));
				sheet.addCell(new Label(3, i, visaDto.getPhone(), cellFormat));
				sheet.addCell(new Label(4, i, sdf.format(visaDto.getReceiveDate()), cellFormat));
				sheet.addCell(new Label(5, i, visaDto.getReceiveTypeName(), cellFormat));
				sheet.addCell(new Label(6, i, visaDto.getServiceCode(), cellFormat));
				sheet.addCell(new jxl.write.Number(7, i, visaDto.getReceivable(), cellFormat));
				sheet.addCell(new jxl.write.Number(8, i, visaDto.getReceived(), cellFormat));
				sheet.addCell(new jxl.write.Number(9, i, visaDto.getAmount(), cellFormat));
				sheet.addCell(new jxl.write.Number(10, i, visaDto.getGst(), cellFormat));
				sheet.addCell(new jxl.write.Number(11, i, visaDto.getDeductGst(), cellFormat));
				sheet.addCell(new jxl.write.Number(12, i, visaDto.getBonus(), cellFormat));
				sheet.addCell(new Label(13, i, visaDto.getOfficialName(), cellFormat));
				sheet.addCell(new Label(14, i, visaDto.getRemarks(), cellFormat));
				i++;
			}

			i = 24;
			for (BrokerageSaDTO brokerageSaDto : brokerageSaDtoList) {
				sheet.addCell(new Label(0, i, sdf.format(brokerageSaDto.getHandlingDate()), cellFormat));
				sheet.addCell(new Label(1, i, brokerageSaDto.getUserName(), cellFormat));
				sheet.addCell(new Label(2, i, brokerageSaDto.getBirthday() + "", cellFormat));
				sheet.addCell(new Label(3, i, brokerageSaDto.getPhone(), cellFormat));
				sheet.addCell(new Label(4, i, brokerageSaDto.getSchoolName(), cellFormat));
				sheet.addCell(new Label(5, i, brokerageSaDto.getSchoolSubject(), cellFormat));
				sheet.addCell(new Label(6, i, sdf.format(brokerageSaDto.getStartDate()), cellFormat));
				sheet.addCell(new Label(7, i, sdf.format(brokerageSaDto.getEndDate()), cellFormat));
				sheet.addCell(new Label(8, i, brokerageSaDto.getReceiveTypeName(), cellFormat));
				sheet.addCell(new jxl.write.Number(9, i, brokerageSaDto.getTuitionFee(), cellFormat));
				sheet.addCell(new jxl.write.Number(10, i, brokerageSaDto.getCommission(), cellFormat));
				sheet.addCell(new jxl.write.Number(11, i, brokerageSaDto.getGst(), cellFormat));
				sheet.addCell(new jxl.write.Number(12, i, brokerageSaDto.getDeductGst(), cellFormat));
				sheet.addCell(new jxl.write.Number(13, i, brokerageSaDto.getBonus(), cellFormat));
				sheet.addCell(new Label(14, i, brokerageSaDto.getAdviserName(), cellFormat));
				sheet.addCell(new Label(15, i, brokerageSaDto.getRemarks(), cellFormat));
				i++;
			}

			i = 45;
			for (SchoolBrokerageSaDTO schoolBrokerageSaDto : schoolBrokerageSaDtoList) {
				sheet.addCell(new Label(0, i, sdf.format(schoolBrokerageSaDto.getHandlingDate()), cellFormat));
				sheet.addCell(new Label(1, i, schoolBrokerageSaDto.getUserName(), cellFormat));
				sheet.addCell(new Label(2, i, schoolBrokerageSaDto.getBirthday() + "", cellFormat));
				sheet.addCell(new Label(3, i, schoolBrokerageSaDto.getPhone(), cellFormat));
				sheet.addCell(new Label(4, i, schoolBrokerageSaDto.getSchoolName(), cellFormat));
				sheet.addCell(new Label(5, i, schoolBrokerageSaDto.getStudentCode(), cellFormat));
				sheet.addCell(new Label(6, i, schoolBrokerageSaDto.getSchoolSubject(), cellFormat));
				sheet.addCell(new Label(7, i, sdf.format(schoolBrokerageSaDto.getStartDate()), cellFormat));
				sheet.addCell(new Label(8, i, sdf.format(schoolBrokerageSaDto.getEndDate()), cellFormat));
				sheet.addCell(new jxl.write.Number(9, i, schoolBrokerageSaDto.getTuitionFee(), cellFormat));
				sheet.addCell(new jxl.write.Number(10, i, schoolBrokerageSaDto.getFirstTermTuitionFee(), cellFormat));
				sheet.addCell(new jxl.write.Number(11, i, schoolBrokerageSaDto.getCommission(), cellFormat));
				sheet.addCell(new Label(12, i, schoolBrokerageSaDto.getSubagencyName(), cellFormat));
				sheet.addCell(new Label(13, i, schoolBrokerageSaDto.getAdviserName(), cellFormat));
				sheet.addCell(new Label(14, i, schoolBrokerageSaDto.getRemarks(), cellFormat));
				i++;
			}

			i = 73;
			for (RefundDTO refundDto : refundDtoList) {
				sheet.addCell(new Label(0, i, refundDto.getId() + "", cellFormat));
				sheet.addCell(new Label(1, i, refundDto.getUserName(), cellFormat));
				sheet.addCell(new Label(2, i, refundDto.getName(), cellFormat));
				sheet.addCell(new jxl.write.Number(3, i, refundDto.getAmount(), cellFormat));
				sheet.addCell(new Label(4, i, refundDto.getBankName(), cellFormat));
				sheet.addCell(new Label(5, i, refundDto.getBankAccount(), cellFormat));
				sheet.addCell(new Label(6, i, refundDto.getBsb(), cellFormat));
				sheet.addCell(new Label(7, i, refundDto.getAdviserName(), cellFormat));
				sheet.addCell(new Label(7, i, refundDto.getRemarks(), cellFormat));
				i++;
			}

			wbe.write();
			wbe.close();
		} catch (ServiceException e) {
			return;
		}

	}

	public void downUserUtil(OutputStream os, String inpath, List<UserDTO> userDtoList) throws Exception {
		jxl.Workbook wb;
		InputStream is;
		try {
			is = this.getClass().getResourceAsStream(inpath);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("模版不存在");
		}
		try {
			wb = Workbook.getWorkbook(is);
		} catch (Exception e) {
			e.printStackTrace();
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
		WritableSheet sheet = wbe.getSheet(0);
		WritableCellFormat cellFormat = new WritableCellFormat();
		// cellFormat.setBorder(jxl.format.Border.ALL,
		// jxl.format.BorderLineStyle.THIN);

		int i = 1;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		for (UserDTO userDto : userDtoList) {
			String date = sdf.format(userDto.getGmtCreate());
			sheet.addCell(new Label(0, i, date, cellFormat));
			sheet.addCell(new Label(1, i, userDto.getId() + "", cellFormat));
			sheet.addCell(new Label(2, i, userDto.getName(), cellFormat));
			sheet.addCell(new Label(3, i, userDto.getBirthday() + "", cellFormat));
			sheet.addCell(new Label(4, i, userDto.getAuthNickname(), cellFormat));
			sheet.addCell(new Label(5, i, userDto.getPhone(), cellFormat));
			sheet.addCell(new Label(6, i, userDto.getAuthUsername(), cellFormat));
			sheet.addCell(new Label(7, i, userDto.getEmail(), cellFormat));
			sheet.addCell(new Label(8, i, userDto.getBalance() + "", cellFormat));
			if (userDto.getAdviserDto() != null) {
				sheet.addCell(new Label(8, i, userDto.getAdviserDto().getName(), cellFormat));
			} else {
				sheet.addCell(new Label(8, i, "", cellFormat));
			}
			i++;
		}
		wbe.write();
		wbe.close();

	}

	public void downOrderUtil(OutputStream os, String inpath, List<OrderDTO> orderDtoList) throws Exception {
		jxl.Workbook wb;
		InputStream is;
		try {
			is = this.getClass().getResourceAsStream(inpath);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("模版不存在");
		}
		try {
			wb = Workbook.getWorkbook(is);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("模版格式不支持");
		}
		WorkbookSettings settings = new WorkbookSettings();
		settings.setWriteAccess(null);
		jxl.write.WritableWorkbook wbe = Workbook.createWorkbook(os, wb, settings);
		WritableSheet sheet = wbe.getSheet(0);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		int i = 1;
		WritableCellFormat cellFormat = new WritableCellFormat();
		cellFormat.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
		for (OrderDTO orderDto : orderDtoList) {
			String date = sdf.format(orderDto.getGmtCreate());
			sheet.addCell(new Label(0, i, date, cellFormat));
			sheet.addCell(new Label(1, i, orderDto.getId() + "", cellFormat));
			if (orderDto.getSubjectDto() != null) {
				sheet.addCell(new Label(2, i, orderDto.getSubjectDto().getName(), cellFormat));
			}
			if (orderDto.getUserDo() != null) {
				sheet.addCell(new Label(3, i, orderDto.getUserDo().getName(), cellFormat));
			}
			if (orderDto.getRegionDo() != null) {
				sheet.addCell(new Label(4, i, orderDto.getRegionDo().getName(), cellFormat));
			}
			if (orderDto.getUserDo() != null) {
				sheet.addCell(new Label(5, i, orderDto.getUserDo().getPhone(), cellFormat));
			}
			if (orderDto.getSubjectDto() != null) {
				sheet.addCell(new Label(6, i, orderDto.getSubjectDto().getPreAmount() + "", cellFormat));
			}
			sheet.addCell(new Label(7, i, orderDto.getAmount() + "", cellFormat));
			if (orderDto.getPayDate() != null) {
				String payDate = sdf.format(orderDto.getPayDate());
				sheet.addCell(new Label(8, i, payDate, cellFormat));
			}
			sheet.addCell(new Label(9, i, orderDto.getFinishPrice() + "", cellFormat));
			sheet.addCell(new Label(10, i, orderDto.getNum() + "", cellFormat));
			sheet.addCell(new Label(11, i, orderDto.getPayAmount() + "", cellFormat));
			sheet.addCell(new Label(12, i, orderDto.getFinalPayAmount() + "", cellFormat));
			double waitFinishAmountPay = 0;
			if (orderDto.getFinalPayAmount() > 0) {
				waitFinishAmountPay = new BigDecimal(
						orderDto.getFinalPayAmount() - orderDto.getRemainPayBalance() - orderDto.getRemainPayAmount())
								.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
			}
			sheet.addCell(new Label(13, i, waitFinishAmountPay + "", cellFormat));
			sheet.addCell(new Label(14, i, orderDto.getRemainPayBalance() + "", cellFormat));
			sheet.addCell(new Label(15, i, orderDto.getRemainPayAmount() + "", cellFormat));
			String state = "";
			if (orderDto.getState().equals(OrderStateEnum.NEW)) {
				state = "待付款";
			} else if (orderDto.getState().equals(OrderStateEnum.WAIT)) {
				state = "待成团";
			} else if (orderDto.getState().equals(OrderStateEnum.SUCCESS)) {
				state = "已成团";
			} else if (orderDto.getState().equals(OrderStateEnum.END)) {
				state = "未成团";
			}
			sheet.addCell(new Label(16, i, state + "", cellFormat));
			if (orderDto.getAdviserDo() != null) {
				sheet.addCell(new Label(17, i, orderDto.getAdviserDo().getName(), cellFormat));
			}
			if (orderDto.getRemainPayDate() != null) {
				String remainPayDate = sdf.format(orderDto.getRemainPayDate());
				sheet.addCell(new Label(18, i, remainPayDate, cellFormat));
			}
			String payType = orderDto.getPayType().toString();
			if (OrderPayTypeEnum.OTHER.toString().equals(payType)) {
				payType = "未支付";
			}
			if (OrderPayTypeEnum.BALANCE.toString().equals(payType)) {
				payType = "余额支付";
			}
			sheet.addCell(new Label(19, i, payType, cellFormat));
			i++;
		}
		wbe.write();
		wbe.close();
	}

	@RequestMapping(value = "/report", method = RequestMethod.GET)
	@ResponseBody
	public void down(@RequestParam(value = "startDate", required = false) String startDate,
			@RequestParam(value = "endDate", required = false) String endDate,
			@RequestParam(value = "dateType", required = false) String dateType,
			@RequestParam(value = "dateMethod", required = false) String dateMethod,
			@RequestParam(value = "regionId", required = false) Integer regionId,
			@RequestParam(value = "adviserId", required = false) Integer adviserId, HttpServletRequest request,
			HttpServletResponse response) {

		try {
			response.reset();// 清空输出流
			String tableName = "commission_report_information";
			response.setHeader("Content-disposition",
					"attachment; filename=" + new String(tableName.getBytes("GB2312"), "8859_1") + ".xls");
			response.setContentType("application/msexcel");

			Map<String, CommissionReport> crMap = new HashMap<>();
			List<VisaReportDTO> visaReportList = visaService.listVisaReport(startDate, endDate, dateType, dateMethod,
					regionId, adviserId);
			if (visaReportList != null)
				visaReportList.forEach(v -> {
					if (v.getDate() != null)
						crMap.put(v.getDate() + "-" + v.getRegionId() + "-" + v.getAdviserId(),
								new CommissionReport(v.getDate(), v.getRegionId(), v.getArea(), v.getAdviserId(),
										v.getConsultant(), v.getCommission(), v.getServiceFee(), 0, 0, 0));
				});
			List<CommissionOrderReportDTO> commissionOrderReportList = commissionOrderService
					.listCommissionOrderReport(startDate, endDate, dateType, dateMethod, regionId, adviserId);
			if (commissionOrderReportList != null)
				commissionOrderReportList.forEach(c -> {
					if (c.getDate() != null) {
						CommissionReport cr = crMap.get(c.getDate() + "-" + c.getRegionId() + "-" + c.getAdviserId());
						if (cr != null) {
							cr.setDeductionCommission(c.getDeductionCommission());
							cr.setClaimCommission(c.getClaimCommission());
							cr.setClaimedCommission(c.getClaimedCommission());
							crMap.put(c.getDate() + "-" + c.getRegionId() + "-" + c.getAdviserId(), cr);
						} else
							crMap.put(c.getDate() + "-" + c.getRegionId() + "-" + c.getAdviserId(),
									new CommissionReport(c.getDate(), c.getRegionId(), c.getArea(), c.getAdviserId(),
											c.getConsultant(), 0, 0, c.getDeductionCommission(), c.getClaimCommission(),
											c.getClaimedCommission()));
					}
				});

			OutputStream os = response.getOutputStream();
			jxl.Workbook wb;
			InputStream is;
			try {
				is = this.getClass().getResourceAsStream("/CommissionReportTemplate.xls");
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
			WritableSheet sheet = wbe.getSheet(0);
			WritableCellFormat cellFormat = new WritableCellFormat();

			int i = 1;
			for (Map.Entry<String, CommissionReport> entry : crMap.entrySet()) {
				CommissionReport commissionReport = entry.getValue();
				sheet.addCell(new Label(0, i, commissionReport.getDate(), cellFormat));
				sheet.addCell(new Label(1, i, commissionReport.getArea(), cellFormat));
				sheet.addCell(new Label(2, i, commissionReport.getConsultant(), cellFormat));
				sheet.addCell(new Label(3, i, commissionReport.getCommission() + "", cellFormat));
				sheet.addCell(new Label(4, i, commissionReport.getServiceFee() + "", cellFormat));
				sheet.addCell(new Label(5, i, commissionReport.getDeductionCommission() + "", cellFormat));
				sheet.addCell(new Label(6, i, commissionReport.getClaimCommission() + "", cellFormat));
				sheet.addCell(new Label(7, i, commissionReport.getClaimedCommission() + "", cellFormat));
				i++;
			}
			wbe.write();
			wbe.close();

		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	@AllArgsConstructor
	@Data
	class CommissionReport {

		private String date;

		private int regionId;

		private String area;

		private int adviserId;

		private String consultant;

		private double commission;

		private double serviceFee;

		private double deductionCommission;

		private double claimCommission;

		private double claimedCommission;
	}

}
