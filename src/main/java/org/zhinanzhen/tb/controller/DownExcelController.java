package org.zhinanzhen.tb.controller;


import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zhinanzhen.b.service.CommissionOrderService;
import org.zhinanzhen.b.service.RefundService;
import org.zhinanzhen.b.service.ServiceOrderService;
import org.zhinanzhen.b.service.VisaService;
import org.zhinanzhen.b.service.pojo.*;
import org.zhinanzhen.tb.service.AdviserService;
import org.zhinanzhen.tb.service.OrderPayTypeEnum;
import org.zhinanzhen.tb.service.OrderService;
import org.zhinanzhen.tb.service.OrderStateEnum;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.UserAuthTypeEnum;
import org.zhinanzhen.tb.service.UserService;
import org.zhinanzhen.tb.service.pojo.OrderDTO;
import org.zhinanzhen.tb.service.pojo.UserDTO;
import com.ikasoa.core.utils.StringUtil;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import lombok.AllArgsConstructor;
import lombok.Data;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/down")
public class DownExcelController extends BaseController {
	@Resource
	private UserService userService;
	@Resource
	private OrderService orderService;

	@Resource
	VisaService visaService;

	@Resource
	CommissionOrderService commissionOrderService;

	@Resource
	AdviserService adviserService;

	@Resource
	ServiceOrderService serviceOrderService;

	@Resource
	RefundService refundService;

	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private static DecimalFormat df = new DecimalFormat("#,##0.00");

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
		List<UserDTO> userDtoList = userService.listUser(name, authTypeEnum, authNickname, phone, null, 0, null, 0,
				10000);
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
	public void visaExport(@RequestParam(value = "id", required = false) Integer id,
			@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "startHandlingDate", required = false) String startHandlingDate,
			@RequestParam(value = "endHandlingDate", required = false) String endHandlingDate,
			@RequestParam(value = "startDate", required = false) String startDate,
			@RequestParam(value = "endDate", required = false) String endDate,
			@RequestParam(value = "adviserId", required = false) Integer adviserId,
			@RequestParam(value = "userName", required = false) String userName,//tb_user.name   用户的名字
			@RequestParam(value = "state",required = false) String state,HttpServletRequest request,
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
			List<VisaDTO> visaDtoList = visaService.listVisa(id , keyword, startHandlingDate, endHandlingDate, null, null,
					null, null, startDate, endDate, null, null, null, adviserId, null, userName, state,0, 9999, null);

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
			@RequestParam(value = "adviserId", required = false) Integer adviserId,
			@RequestParam(value = "adviserIds", required = false) String adviserIds,HttpServletRequest request,
			HttpServletResponse response) {

		try {
			List<String> adviserIdList = null;
			if (StringUtil.isNotEmpty(adviserIds))
				adviserIdList = Arrays.asList(adviserIds.split(","));

			response.reset();// 清空输出流
			//String tableName = "commission_report_information";
			//response.setHeader("Content-disposition",
			//		"attachment; filename=" + new String(tableName.getBytes("GB2312"), "8859_1") + ".xls");
			//response.setContentType("application/msexcel");
			String tableName = "压缩包";
			response.setHeader("Content-disposition",
					"attachment; filename=" + new String(tableName.getBytes("GB2312"), "8859_1") + ".zip");
			response.setContentType("application/zip");

			Map<String, CommissionReport> crMap = new HashMap<>();
			List<VisaReportDTO> visaReportList = visaService.listVisaReport(startDate, endDate, dateType, dateMethod,
					regionId, adviserId, adviserIdList);
			if (visaReportList != null)
				visaReportList.forEach(v -> {
					if (v.getDate() != null)
						crMap.put(v.getDate() + "-" + v.getRegionId() + "-" + v.getConsultant(),
								new CommissionReport(v.getDate(), v.getRegionId(), v.getArea(), v.getAdviserId(),
										v.getConsultant(), v.getCommission(), v.getServiceFee(), 0, 0, 0
								, 0, 0, 0));
				});
			List<CommissionOrderReportDTO> commissionOrderReportList = commissionOrderService
					.listCommissionOrderReport(startDate, endDate, dateType, dateMethod, regionId, adviserId, adviserIdList);
			if (commissionOrderReportList != null)
				commissionOrderReportList.forEach(c -> {
					if (c.getDate() != null) {
						CommissionReport cr = crMap.get(c.getDate() + "-" + c.getRegionId() + "-" + c.getConsultant());
						if (cr != null) {
							cr.setDeductionCommission(c.getDeductionCommission());
							cr.setClaimCommission(c.getClaimCommission());
							cr.setClaimedCommission(c.getClaimedCommission());
							crMap.put(c.getDate() + "-" + c.getRegionId() + "-" + c.getConsultant(), cr);
						} else
							crMap.put(c.getDate() + "-" + c.getRegionId() + "-" + c.getConsultant(),
									new CommissionReport(c.getDate(), c.getRegionId(), c.getArea(), c.getAdviserId(),
											c.getConsultant(), 0, 0, c.getDeductionCommission(), c.getClaimCommission(),
											c.getClaimedCommission(), c.getAdjustments(), 0, 0));
					}
				});

			List<RefoundReportDTO> refoundReportList = refundService
					.listRefundReport(startDate, endDate, dateType, dateMethod, regionId, adviserId, adviserIdList);
			if (refoundReportList != null)
				refoundReportList.forEach(r ->{
					if (r.getDate() != null){
						CommissionReport cr = crMap.get(r.getDate() + "-" + r.getRegionId() + "-" + r.getConsultant());
						if (cr != null){
							cr.setRefunded(r.getRefunded());
							cr.setRefunding(r.getRefunding());
							crMap.put(r.getDate() + "-" + r.getRegionId() + "-" + r.getConsultant(), cr);
						}else
							crMap.put(r.getDate() + "-" + r.getRegionId() + "-" + r.getConsultant(),
									new CommissionReport(r.getDate(), r.getRegionId(), r.getArea(), r.getAdviserId(),
											r.getConsultant(), 0, 0, 0, 0,
											0, 0, r.getRefunded(), r.getRefunding()));
					}
				});


			//crMap 顾问分组
			Map<String,List<CommissionReport>> crListMap = new HashMap<>();
			for (Map.Entry<String, CommissionReport> entry : crMap.entrySet()){
				String entryKey = entry.getKey();
				boolean contains = false;
				String enkey = "";
				for (Map.Entry<String,List<CommissionReport>> en : crListMap.entrySet()){
					if (entryKey.contains(en.getKey())){
						contains = true;
						enkey = en.getKey();
					}
				}
				if (contains)
					crListMap.get(enkey).add(entry.getValue());
				else {
					List<CommissionReport> crList = new ArrayList<>();
					crList.add(entry.getValue());
					crListMap.put(entry.getValue().getConsultant(),crList);
				}
			}


			//OutputStream os = response.getOutputStream();
			ZipOutputStream zipos = new ZipOutputStream(response.getOutputStream());
			jxl.Workbook wb;
			InputStream is;

			WorkbookSettings settings = new WorkbookSettings();
			settings.setWriteAccess(null);
			//jxl.write.WritableWorkbook wbe = Workbook.createWorkbook(zipos, wb, settings);
			//jxl.write.WritableWorkbook wbe = null;

			//if (wbe == null) {
			//	System.out.println("wbe is null !os=" + zipos + ",wb" + wb);
			//} else {
			//	System.out.println("wbe not null !os=" + zipos + ",wb" + wb);
			//}
			//WritableSheet sheet = wbe.getSheet(0);



			for (Map.Entry<String, List<CommissionReport>> entry : crListMap.entrySet()) {
				int i = 1;
				List<CommissionReport> commissionReportList = entry.getValue();

				ZipEntry zipEntryXtv = new ZipEntry(entry.getKey() +".xls");
				zipos.putNextEntry(zipEntryXtv);
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
				jxl.write.WritableWorkbook wbe = Workbook.createWorkbook(zipos, wb, settings);

				WritableCellFormat cellFormat = new WritableCellFormat();
				WritableCellFormat cellGreen = new WritableCellFormat();
				cellGreen.setBackground(Colour.LIGHT_GREEN);
				cellGreen.setAlignment(Alignment.CENTRE); // 设置对齐方式
				cellGreen.setBorder(Border.ALL, BorderLineStyle.THIN);

				WritableCellFormat cellYellow = new WritableCellFormat();
				cellYellow.setBackground(Colour.YELLOW);

				WritableSheet _sheet = wbe.getSheet(0);

				for (CommissionReport commissionReport : commissionReportList){
					double performanceIndex = commissionReport.getServiceFee() + commissionReport.getDeductionCommission()
							+ commissionReport.getClaimCommission() + commissionReport.getAdjustments() + commissionReport.getRefunding();
					_sheet.addCell(new Label(0, i, commissionReport.getDate(), cellFormat));
					_sheet.addCell(new Label(1, i, commissionReport.getArea(), cellFormat));
					_sheet.addCell(new Label(2, i, commissionReport.getConsultant(), cellFormat));
					_sheet.addCell(new Label(3, i, new BigDecimal(performanceIndex).setScale(2,BigDecimal.ROUND_HALF_UP)
							.doubleValue() + "", cellFormat));
					_sheet.addCell(new Label(4, i, commissionReport.getCommission() + "", cellFormat));
					_sheet.addCell(new Label(5, i, commissionReport.getServiceFee() + "", cellFormat));
					_sheet.addCell(new Label(6, i, commissionReport.getDeductionCommission() + "", cellFormat));
					_sheet.addCell(new Label(7, i, commissionReport.getClaimCommission() + "", cellFormat));
					_sheet.addCell(new Label(8, i, commissionReport.getClaimedCommission() + "", cellFormat));
					_sheet.addCell(new Label(9, i, commissionReport.getRefunding() + "", cellFormat));
					_sheet.addCell(new Label(10, i, commissionReport.getRefunded() + "", cellFormat));
					i++;
				}

				WritableSheet sheet = wbe.getSheet(1);
				i = 1 ;
				List<VisaDTO> list = visaService.listVisa(null ,null, null, null, null,
						null, startDate, endDate, null, null, null, null, null,
						commissionReportList.get(0).getAdviserId(),null,null, null,0, 9999, null);
				list.forEach(v -> {
					if (v.getServiceOrderId() > 0)
						try {
							ServiceOrderDTO serviceOrderDto = serviceOrderService
									.getServiceOrderById(v.getServiceOrderId());
							if (serviceOrderDto != null)
								v.setServiceOrder(serviceOrderDto);
						} catch (ServiceException e) {
						}
				});

				i = outPutCvToSheet(sheet, cellFormat, cellGreen, cellYellow, i, list);

				List<CommissionOrderListDTO> commissionOrderList = commissionOrderService.listCommissionOrder(null,
						null, null, commissionReportList.get(0).getAdviserId(), null, null, null, null, null, null,
						null, null, null, startDate, endDate,null,null,
						null, null, null, null, 0, 9999, null);

				outPutCsToSheet(sheet, cellFormat,cellGreen, cellYellow, i += 3, commissionOrderList);

				wbe.write();
				//zipos.closeEntry();
				wbe.close();
			}

			zipos.flush();
			zipos.close();


		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	public int outPutCvToSheet(WritableSheet sheet, WritableCellFormat cellFormat,WritableCellFormat cellGreen,WritableCellFormat cellYellow , int i , List<VisaDTO> list) throws Exception {
		BigDecimal expectAmountTotal = new BigDecimal("0.00");
		BigDecimal bonusTotal = new BigDecimal("0.00");
		for (VisaDTO visaDto : list) {
			bonusTotal = bonusTotal.add(new BigDecimal(visaDto.getBonus()));
			expectAmountTotal = expectAmountTotal.add(new BigDecimal(visaDto.getExpectAmount()));
			sheet.addCell(new Label(0, i, "CV" + visaDto.getId() , cellFormat));
			sheet.addCell(new Label(1, i, sdf.format(visaDto.getGmtCreate()), cellFormat));
			if (visaDto.getReceiveDate() != null)
				sheet.addCell(new Label(2, i, sdf.format(visaDto.getReceiveDate()), cellFormat));
			sheet.addCell(new Label(3, i, visaDto.getUserName(), cellFormat));
			sheet.addCell(new Label(4, i, visaDto.getReceiveTypeName(), cellFormat));
			sheet.addCell(new Label(5, i, visaDto.getServiceCode(), cellFormat));
			sheet.addCell(new Label(6, i, visaDto.getTotalPerAmount() + "", cellFormat));
			sheet.addCell(new Label(7, i, visaDto.getTotalAmount() + "", cellFormat));
			sheet.addCell(new Label(8, i, visaDto.getAmount() + "", cellFormat));
			sheet.addCell(new Label(9, i, visaDto.getExpectAmount() + "", cellFormat));
			sheet.addCell(new Label(10, i, visaDto.getExpectAmount() + "", cellFormat));
			sheet.addCell(new Label(11, i, visaDto.getBonus() + "", cellFormat));
			if (visaDto.getBonusDate() != null)
				sheet.addCell(new Label(12, i, sdf.format(visaDto.getBonusDate()), cellFormat));
			sheet.addCell(new Label(13, i, visaDto.getBankCheck(), cellFormat));
			sheet.addCell(new Label(14, i, visaDto.isChecked() + "", cellFormat));
			sheet.addCell(new Label(15, i, visaDto.getAdviserName(), cellFormat));
			if (visaDto.getState() != null)
				sheet.addCell(new Label(16, i, getStateStr(visaDto.getState()), cellFormat));
			if (visaDto.getKjApprovalDate() != null)
				sheet.addCell(new Label(17, i, sdf.format(visaDto.getKjApprovalDate()), cellFormat));
			sheet.addCell(new Label(18, i, visaDto.getRemarks(), cellFormat));
			i++;
		}
		sheet.addCell(new Label(8, i,  "total", cellYellow));
		sheet.addCell(new Label(9, i,  expectAmountTotal.setScale(2,BigDecimal.ROUND_HALF_UP).toString(), cellYellow));
		sheet.addCell(new Label(10, i,  expectAmountTotal.setScale(2,BigDecimal.ROUND_HALF_UP).toString(), cellYellow));
		sheet.addCell(new Label(11, i,  bonusTotal.setScale(2,BigDecimal.ROUND_HALF_UP).toString(), cellYellow));
		return i++;
	}

	public void outPutCsToSheet(WritableSheet sheet, WritableCellFormat cellFormat, WritableCellFormat cellGreen,WritableCellFormat cellYellow , int i , List<CommissionOrderListDTO> commissionOrderList) throws Exception {
		String title = "订单ID,佣金订单创建日期,客户支付日期,Student Name,Student ID,生日,收款方式,服务项目,是否提前扣佣,Institute/Institution Trading Name," +
				"Institution Name,Location Name,State,Course Name," +
				"Course Start Date,Course End Date,Installment Due Date,收款方式,Total Tuition Fee,Per Tuition Fee per Installment," +
				"总计应收,总计已收,本次收款,Commission,确认预收业绩,GST,Deduct GST,学校支付金额,学校支付时间,Invoice NO.,追佣时间,Subagency,月奖," +
				"月奖支付时间,银行对账字段,是否自动对账,顾问,状态,财务审核时间,佣金备注,服务备注";
		String [] titleArr = title.split(",");
		for (int ti = 0 ; ti < titleArr.length ; ti ++){
			sheet.addCell(new Label(ti, i, titleArr[ti] , cellGreen));
		}
		i++;

		BigDecimal sureExpectAmountTotal = new BigDecimal("0.00");
		BigDecimal commissionTotal = new BigDecimal("0.00");
		for (CommissionOrderListDTO commissionOrderListDto : commissionOrderList) {
			commissionTotal = commissionTotal.add(new BigDecimal(commissionOrderListDto.getExpectAmount()));
			sheet.addCell(new Label(0, i, "CS" + commissionOrderListDto.getId(), cellFormat));
			sheet.addCell(new Label(1, i, sdf.format(commissionOrderListDto.getGmtCreate()), cellFormat));
			if (commissionOrderListDto.getReceiveDate() != null)
				sheet.addCell(new Label(2, i, sdf.format(commissionOrderListDto.getReceiveDate()), cellFormat));
			if (commissionOrderListDto.getUser() != null)
				sheet.addCell(new Label(3, i, commissionOrderListDto.getUser().getName(), cellFormat));
			sheet.addCell(new Label(4, i, commissionOrderListDto.getStudentCode(), cellFormat));
			if (commissionOrderListDto.getBirthday() != null)
				sheet.addCell(new Label(5, i, sdf.format(commissionOrderListDto.getBirthday()), cellFormat));
			if (commissionOrderListDto.getReceiveType() != null)
				sheet.addCell(new Label(6, i, commissionOrderListDto.getReceiveType().getName() + "", cellFormat));
			if (commissionOrderListDto.getService() != null)
				sheet.addCell(new Label(7, i, commissionOrderListDto.getService().getName(), cellFormat));
			sheet.addCell(new Label(8, i, commissionOrderListDto.isSettle() + "", cellFormat));
			if (commissionOrderListDto.getSchool() != null) {
				sheet.addCell(new Label(9, i, commissionOrderListDto.getSchool().getName() + "", cellFormat));
				sheet.addCell(new Label(13, i, commissionOrderListDto.getSchool().getSubject() + "", cellFormat));
			}
			if (commissionOrderListDto.getSchoolInstitutionListDTO() != null){
				sheet.addCell(new Label(9, i, commissionOrderListDto.getSchoolInstitutionListDTO().getInstitutionTradingName() , cellFormat));
				//if (commissionOrderListDto.getSchoolInstitutionListDTO().getSchoolInstitutionLocationDO() != null)
				sheet.addCell(new Label(10, i, commissionOrderListDto.getSchoolInstitutionListDTO().getInstitutionName(), cellFormat));
				if (commissionOrderListDto.getSchoolInstitutionListDTO().getSchoolInstitutionLocationDO() != null){
					sheet.addCell(new Label(11, i, commissionOrderListDto.getSchoolInstitutionListDTO().getSchoolInstitutionLocationDO().getName(), cellFormat));
					sheet.addCell(new Label(12, i, commissionOrderListDto.getSchoolInstitutionListDTO().getSchoolInstitutionLocationDO().getState(), cellFormat));
				}
				if (commissionOrderListDto.getSchoolInstitutionListDTO().getSchoolCourseDO() != null)
					sheet.addCell(new Label(13, i, commissionOrderListDto.getSchoolInstitutionListDTO().getSchoolCourseDO().getCourseName(), cellFormat));
			}
			if (commissionOrderListDto.getStartDate() != null)
				sheet.addCell(new Label(14, i, sdf.format(commissionOrderListDto.getStartDate()), cellFormat));
			if (commissionOrderListDto.getEndDate() != null)
				sheet.addCell(new Label(15, i, sdf.format(commissionOrderListDto.getEndDate()), cellFormat));
			if (commissionOrderListDto.getInstallmentDueDate() != null)
				sheet.addCell(
						new Label(16, i, sdf.format(commissionOrderListDto.getInstallmentDueDate()), cellFormat));
			if (commissionOrderListDto.getReceiveType() != null)
				sheet.addCell(new Label(17, i, commissionOrderListDto.getReceiveType().getName() + "", cellFormat));
			sheet.addCell(new Label(18, i, commissionOrderListDto.getTuitionFee() + "", cellFormat));
			sheet.addCell(new Label(19, i, commissionOrderListDto.getPerAmount() + "", cellFormat)); // .getPerTermTuitionFee()
			sheet.addCell(new Label(20, i, commissionOrderListDto.getTotalPerAmount() + "", cellFormat));
			sheet.addCell(new Label(21, i, commissionOrderListDto.getTotalAmount() + "", cellFormat));
			sheet.addCell(new Label(22, i, commissionOrderListDto.getAmount() + "", cellFormat));
			sheet.addCell(new Label(23, i, commissionOrderListDto.getExpectAmount() + "", cellFormat));
			if (commissionOrderListDto.isSettle()){
				sheet.addCell(new Label(24, i, commissionOrderListDto.getExpectAmount() + "", cellFormat));
				sureExpectAmountTotal = sureExpectAmountTotal.add(new BigDecimal(commissionOrderListDto.getExpectAmount()));
			}
			else{
				sheet.addCell(new Label(24, i, commissionOrderListDto.getSureExpectAmount() + "", cellFormat));
				sureExpectAmountTotal = sureExpectAmountTotal.add(new BigDecimal(commissionOrderListDto.getSureExpectAmount()));
			}
			sheet.addCell(new Label(25, i, commissionOrderListDto.getGst() + "", cellFormat));
			sheet.addCell(new Label(26, i, commissionOrderListDto.getDeductGst() + "", cellFormat));
			sheet.addCell(new Label(27, i, commissionOrderListDto.getSchoolPaymentAmount() + "", cellFormat));
			if (commissionOrderListDto.getSchoolPaymentDate() != null)
				sheet.addCell(
						new Label(28, i, sdf.format(commissionOrderListDto.getSchoolPaymentDate()), cellFormat));
			sheet.addCell(new Label(28, i, commissionOrderListDto.getInvoiceNumber(), cellFormat));
			if (commissionOrderListDto.getZyDate() != null)
				sheet.addCell(new Label(30, i, sdf.format(commissionOrderListDto.getZyDate()), cellFormat));
			if (commissionOrderListDto.getSubagency() != null)
				sheet.addCell(new Label(31, i, commissionOrderListDto.getSubagency().getName(), cellFormat));
			sheet.addCell(new Label(32, i, commissionOrderListDto.getBonus() + "", cellFormat));
			if (commissionOrderListDto.getBonusDate() != null)
				sheet.addCell(new Label(33, i, sdf.format(commissionOrderListDto.getBonusDate()), cellFormat));
			sheet.addCell(new Label(34, i, commissionOrderListDto.getBankCheck(), cellFormat));
			sheet.addCell(new Label(35, i, commissionOrderListDto.isChecked() + "", cellFormat));
			if (commissionOrderListDto.getAdviser() != null)
				sheet.addCell(new Label(36, i, commissionOrderListDto.getAdviser().getName(), cellFormat));
			if (commissionOrderListDto.getState() != null)
				sheet.addCell(new Label(37, i, getStateStr(commissionOrderListDto.getState()), cellFormat));
			if (commissionOrderListDto.getKjApprovalDate() != null)
				sheet.addCell(new Label(38, i, sdf.format(commissionOrderListDto.getKjApprovalDate()), cellFormat));
			sheet.addCell(new Label(39, i, commissionOrderListDto.getRemarks(), cellFormat));
			ServiceOrderDTO serviceOrderDTO = serviceOrderService
					.getServiceOrderById(commissionOrderListDto.getServiceOrderId());
			sheet.addCell(new Label(40, i,
					serviceOrderDTO != null && serviceOrderDTO.getRemarks() != null ? serviceOrderDTO.getRemarks()
							: "",
					cellFormat));
			i++;
		}
		sheet.addCell(new Label(19, i,  "total", cellYellow));
		sheet.addCell(new Label(20, i,  commissionTotal.setScale(2,BigDecimal.ROUND_HALF_UP).toString(), cellYellow));
		sheet.addCell(new Label(21, i,  sureExpectAmountTotal.setScale(2,BigDecimal.ROUND_HALF_UP).toString(), cellYellow));
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

		private double adjustments;

		private double refunded;

		private double refunding;
	}

	protected String getStateStr(String state) {
		if ("REVIEW".equalsIgnoreCase(state))
			return "待结佣";
		if ("WAIT".equalsIgnoreCase(state))
			return "已驳回";
		if ("FINISH".equalsIgnoreCase(state))
			return "已审核";
		if ("COMPLETE".equalsIgnoreCase(state))
			return "已结佣";
		if ("CLOSE".equalsIgnoreCase(state))
			return "已关闭";
		return "";
	}

}
