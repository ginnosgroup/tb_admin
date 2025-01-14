package org.zhinanzhen.tb.controller;


import com.ikasoa.core.utils.StringUtil;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.zhinanzhen.b.controller.RefundController;
import org.zhinanzhen.b.service.CommissionOrderService;
import org.zhinanzhen.b.service.RefundService;
import org.zhinanzhen.b.service.ServiceOrderService;
import org.zhinanzhen.b.service.VisaService;
import org.zhinanzhen.b.service.pojo.*;
import org.zhinanzhen.tb.service.*;
import org.zhinanzhen.tb.service.pojo.OrderDTO;
import org.zhinanzhen.tb.service.pojo.UserDTO;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/down")
public class DownExcelController extends BaseController {
	private static final Logger LOG = LoggerFactory.getLogger(DownExcelController.class);
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
	
	@Resource
	RegionService regionService;

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
		List<UserDTO> userDtoList = userService.listUser(name, authTypeEnum, authNickname, phone, null, null, null, 0, null, null,
				0, 10000);
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
					null, null, startDate, endDate, null, null, null, adviserId, null, userName, null, state,0, 9999, null);

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
				sheet.addCell(new Label(12, i, visaDto.getPerAmountAUD() + "", cellFormat));
				sheet.addCell(new Label(13, i, visaDto.getAmountAUD() + "", cellFormat));
				sheet.addCell(new Label(14, i, visaDto.getExpectAmountAUD() + "", cellFormat));
				sheet.addCell(new Label(15, i, visaDto.getGstAUD() + "", cellFormat));
				sheet.addCell(new Label(16, i, visaDto.getDeductGstAUD() + "", cellFormat));
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
        ServletOutputStream outputStream = null;
        ZipOutputStream zipos = null;

		try {
			List<String> adviserIdList = null;
			if (StringUtil.isNotEmpty(adviserIds))
				adviserIdList = Arrays.asList(adviserIds.split(","));
			response.reset();// 清空输出流

			//返回客户端浏览器的版本号、类型
			String agent = request.getHeader("USER-AGENT");
			String tableName = "压缩包";
			if (agent.contains("MSIE") || agent.contains("Trident")) {
				tableName = java.net.URLEncoder.encode(tableName, "UTF-8");
			} else {
				tableName = new String(tableName.getBytes("UTF-8"), "ISO-8859-1");
			}
			response.setHeader("Content-disposition",
					"attachment; filename=" + tableName + ".zip");
			response.setContentType("application/x-zip-compressed;charset=UTF-8");
			response.addHeader("Pargam", "no-cache");
			response.addHeader("Cache-Control", "no-cache");
			response.addHeader("Access-Contro1-A11ow-0rigin", "*");

			// AUD
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

			List<CommissionOrderReportDTO> commissionOrderReportAUDList = commissionOrderService
					.listCommissionOrderReport(startDate, endDate, dateType, dateMethod, regionId, adviserId, adviserIdList);
			if (commissionOrderReportAUDList != null)
				commissionOrderReportAUDList.forEach(c -> {
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
			 outputStream = response.getOutputStream();
			 zipos = new ZipOutputStream(outputStream);
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

			//获取地区汇总表格
			List<CommissionReport> totalList = new ArrayList<>();

			if (regionId != null && crListMap.size() > 0 && StringUtil.isNotEmpty(String.valueOf(adviserId))) {
				Stream<List<CommissionReport>> crListMaStream = crListMap.values().stream();
				crListMaStream.forEach(totalList::addAll);
				crListMap.put(totalList.get(0).getArea() + "_total", totalList);
			}
			/*ExecutorService executor = Executors.newFixedThreadPool(crListMap.size()/3>3?crListMap.size()/3:3);
			Map<String,HSSFWorkbook> hm = new HashMap<>(crListMap.size());
			CountDownLatch latch = new CountDownLatch(crListMap.size());
			for (Map.Entry<String, List<CommissionReport>> entry : crListMap.entrySet())
				Thread thread = new Thread(() -> {
					try {
						System.out.println("线程"+Thread.currentThread().getName()+"开始执行");
						 HSSFWorkbook sheets = exportDataToExcel(entry, regionId, startDate, endDate);

						 hm.put(entry.getKey() + ".xls",sheets);
						 latch.countDown();
						System.out.println("线程"+Thread.currentThread().getName()+"执行完成");
					} catch (IOException e) {
						e.printStackTrace();
					} catch (ServiceException e) {
						e.printStackTrace();
					}
				});
				executor.execute(thread);
				HSSFWorkbook sheets =  exportDataToExcel(entry, regionId, startDate, endDate);
				zipos.putNextEntry( new ZipEntry(entry.getKey() + ".xls"));
				sheets.write(zipos);
				sheets.close();
			}
			latch.await();
			executor.shutdown();
			for (Map.Entry<String, HSSFWorkbook> entry : hm.entrySet()) {
				zipos.putNextEntry( new ZipEntry(entry.getKey() + ".xls"));
				HSSFWorkbook sheets = entry.getValue();
				sheets.write(zipos);
				sheets.close();
			}*/
			for (Map.Entry<String, List<CommissionReport>> entry : crListMap.entrySet()) {
				 HSSFWorkbook sheets = exportDataToExcel(entry, regionId, startDate, endDate);
				zipos.putNextEntry( new ZipEntry(entry.getKey() + ".xls"));
				 sheets.write(zipos);
				 sheets.close();
			}
			zipos.flush();
			zipos.close();
			outputStream.close();

		} catch (Exception e) {
			e.printStackTrace();
			return;
		}finally {
		    if(zipos!=null){
                try {
                    zipos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
		    if(outputStream!=null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
	}

	public HSSFWorkbook  exportDataToExcel(Map.Entry<String, List<CommissionReport>> entry, Integer regionId,  String startDate, String endDate) throws IOException, ServiceException, InterruptedException {
		boolean isCn = regionService.isCN(regionId);

			int i = 1;
			List<CommissionReport> commissionReportList = entry.getValue();

			String url = "/CommissionReportTemplate.xls";
			if (isCn) {
				url = "/CommissionReportTemplateCNY.xls";
			}
			InputStream is = this.getClass().getResourceAsStream(url);
			HSSFWorkbook wb = new HSSFWorkbook(is);
			HSSFSheet sheet = wb.getSheetAt(0);
			for (CommissionReport commissionReport : commissionReportList) {
				HSSFRow row = sheet.createRow(i);
				double performanceIndex = commissionReport.getServiceFee() + commissionReport.getDeductionCommission()
						+ commissionReport.getClaimCommission() + commissionReport.getAdjustments() + commissionReport.getRefunding();
				row.createCell(0).setCellValue(commissionReport.getDate());
				row.createCell(1).setCellValue(commissionReport.getArea());
				row.createCell(2).setCellValue(commissionReport.getConsultant());
				row.createCell(3).setCellValue(new BigDecimal(performanceIndex).setScale(2, BigDecimal.ROUND_HALF_UP)
						.doubleValue() + "");
				row.createCell(4).setCellValue(commissionReport.getCommission() + "");
				row.createCell(5).setCellValue(commissionReport.getServiceFee() + "");
				row.createCell(6).setCellValue(commissionReport.getDeductionCommission() + "");
				row.createCell(7).setCellValue(commissionReport.getClaimCommission() + "");
				row.createCell(8).setCellValue(commissionReport.getClaimedCommission() + "");
				row.createCell(9).setCellValue(commissionReport.getAdjustments() + "");
				row.createCell(10).setCellValue(commissionReport.getRefunded() + "");
				i++;
			}
			HSSFSheet sheet2 = wb.getSheetAt(1);
			i = 1;
			List<VisaDTO> list = visaService.listVisa(null, null, null, null, null,
					null, startDate, endDate, null, null, null, null, null,
					commissionReportList.get(0).getAdviserId(), null, null, null, null, 0, 9999, null);
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
			BigDecimal amountTotal = new BigDecimal("0.00");
			BigDecimal gstTotal = new BigDecimal("0.00");
			BigDecimal deductGstTotal = new BigDecimal("0.00");
			BigDecimal expectAmountTotal = new BigDecimal("0.00");
			BigDecimal bonusTotal = new BigDecimal("0.00");
			int j;
			for (VisaDTO visaDto : list) {
				HSSFRow row = sheet2.createRow(i);
				bonusTotal = bonusTotal.add(new BigDecimal(visaDto.getBonusAUD()));
				amountTotal = amountTotal.add(new BigDecimal(visaDto.getAmountAUD()));
				gstTotal = gstTotal.add(new BigDecimal(visaDto.getGstAUD()));
				deductGstTotal = deductGstTotal.add(new BigDecimal(visaDto.getDeductGstAUD()));
				expectAmountTotal = expectAmountTotal.add(new BigDecimal(visaDto.getExpectAmountAUD()));
				row.createCell(0).setCellValue("CV" + visaDto.getId());
				row.createCell(1).setCellValue(sdf.format(visaDto.getGmtCreate()));
				if (visaDto.getReceiveDate() != null)
					row.createCell(2).setCellValue(sdf.format(visaDto.getReceiveDate()));
				row.createCell(3).setCellValue(visaDto.getUserName());
				row.createCell(4).setCellValue(visaDto.getReceiveTypeName());
				row.createCell(5).setCellValue(visaDto.getServiceCode());

				if (isCn) {
					row.createCell(6).setCellValue(visaDto.getTotalPerAmountCNY() + "");
					row.createCell(7).setCellValue(visaDto.getTotalAmountCNY() + "");
					row.createCell(8).setCellValue(visaDto.getCurrency());
					row.createCell(9).setCellValue(visaDto.getExchangeRate() + "");
					row.createCell(10).setCellValue(visaDto.getAmountCNY() + "");
					j = 10;
				} else {
					row.createCell(6).setCellValue(visaDto.getTotalPerAmountAUD() + "");
					row.createCell(7).setCellValue(visaDto.getTotalAmountAUD() + "");
					row.createCell(8).setCellValue(visaDto.getCurrency());
					row.createCell(9).setCellValue(visaDto.getExchangeRate() + "");
					row.createCell(10).setCellValue(visaDto.getAmountAUD() + "");
					row.createCell(11).setCellValue(visaDto.getGstAUD() + "");
					row.createCell(12).setCellValue(visaDto.getDeductGstAUD() + "");
					j = 12;
				}
				row.createCell(j + 1).setCellValue(visaDto.getExpectAmountAUD() + "");
				row.createCell(j + 2).setCellValue(visaDto.getExpectAmountAUD() + "");
				row.createCell(j + 3).setCellValue(visaDto.getBonus() + "");
				if (visaDto.getBonusDate() != null)
					row.createCell(j + 4).setCellValue(sdf.format(visaDto.getBonusDate()));
				row.createCell(j + 5).setCellValue(visaDto.getBankCheck());
				row.createCell(j + 6).setCellValue(visaDto.isChecked() + "");
				row.createCell(j + 7).setCellValue(visaDto.getAdviserName());
				if (visaDto.getState() != null)
					row.createCell(j + 8).setCellValue(getStateStr(visaDto.getState()));
				if (visaDto.getKjApprovalDate() != null)
					row.createCell(j + 9).setCellValue(sdf.format(visaDto.getKjApprovalDate()));
				row.createCell(j + 10).setCellValue(visaDto.getRemarks());
				i++;
			}
			HSSFRow row = sheet2.createRow(i);
			HSSFCellStyle style = wb.createCellStyle();
			style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
			style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			row.createCell(9).setCellValue("total");
			HSSFCell cell1 = row.createCell(9);
			cell1.setCellValue("total");
			cell1.setCellStyle(style);
			HSSFCell cell2 = row.createCell(10);
			cell2.setCellValue(amountTotal.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
			cell2.setCellStyle(style);
			HSSFCell cell3 = row.createCell(11);
			cell3.setCellValue(gstTotal.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
			cell3.setCellStyle(style);
			HSSFCell cell4 = row.createCell(12);
			cell4.setCellValue(deductGstTotal.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
			cell4.setCellStyle(style);
			List<CommissionOrderListDTO> commissionOrderList = commissionOrderService.listCommissionOrder(null,
					null, null, commissionReportList.get(0).getAdviserId(), null, null, null, null, null, null,
					null, null, null, null, startDate, endDate, null, null, null, null, null, null, 0, 9999, null);
			i += 3;
		String title = "订单ID,佣金订单创建日期,客户支付日期,Student Name,Student ID,生日,收款方式,服务项目,是否提前扣佣,Institute/Institution Trading Name,"
				+
				"Institution Name,Location Name,State,Course Name," +
				"Course Start Date,Course End Date,Installment Due Date,收款方式,Total Tuition Fee,Per Tuition Fee per Installment," +
				"总计应收澳币,总计收款澳币,创建订单时汇率,本次支付币种,本次收款澳币," +
				"Commission,确认预收业绩,GST,Deduct GST,学校支付金额,学校支付时间,Invoice NO.,追佣时间,Subagency,月奖," +
				"月奖支付时间,银行对账字段,是否自动对账,顾问,状态,财务审核时间,佣金备注,服务备注";
		if(isCn){
			title = "订单ID,佣金订单创建日期,客户支付日期,Student Name,Student ID,生日,收款方式,服务项目,是否提前扣佣,Institute/Institution Trading Name," +
					"Institution Name,Location Name,State,Course Name," +
					"Course Start Date,Course End Date,Installment Due Date,收款方式,Total Tuition Fee,Per Tuition Fee per Installment," +
					"总计应收人民币,总计收款人民币,创建订单时汇率,本次支付币种,本次收款人民币," +
					"Commission,确认预收业绩,GST,Deduct GST,学校支付金额,学校支付时间,Invoice NO.,追佣时间,Subagency,月奖," +
					"月奖支付时间,银行对账字段,是否自动对账,顾问,状态,财务审核时间,佣金备注,服务备注";
		}
		String[] titleArr = title.split(",");
		HSSFRow row2 = sheet2.createRow(i);
		HSSFCellStyle style2 = wb.createCellStyle();
		style2.setFillForegroundColor(IndexedColors.GREEN.getIndex());
		style2.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		HSSFCellStyle style3 = wb.createCellStyle();
		style3.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
		style3.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		for (int i1 = 0; i1 < titleArr.length; i1++) {
			 HSSFCell cell = row2.createCell(i1);
				if (!isCn && ("Commission".equals(titleArr[i1]) || "确认预收业绩".equals(titleArr[i1])
						|| "GST".equals(titleArr[i1]) || "Deduct GST".equals(titleArr[i1])))
					cell.setCellStyle(style3);
				else
					cell.setCellStyle(style2);
			 cell.setCellValue(titleArr[i1]);
		}
		i++;
		BigDecimal sureExpectAmountTotal = new BigDecimal("0.00");
		BigDecimal commissionTotal = new BigDecimal("0.00");
		for (CommissionOrderListDTO commissionOrderListDto : commissionOrderList) {
			HSSFRow row3 = sheet2.createRow(i);
			commissionTotal = commissionTotal.add(new BigDecimal(commissionOrderListDto.getExpectAmount()));
			row3.createCell(0).setCellValue( "CS" + commissionOrderListDto.getId());
			row3.createCell(1).setCellValue( sdf.format(commissionOrderListDto.getGmtCreate()));
			if (commissionOrderListDto.getReceiveDate() != null)
				row3.createCell(2).setCellValue( sdf.format(commissionOrderListDto.getReceiveDate()));
			if (commissionOrderListDto.getUser() != null)
				row3.createCell(3).setCellValue( commissionOrderListDto.getUser().getName());
			row3.createCell(4).setCellValue( commissionOrderListDto.getStudentCode());
			if (commissionOrderListDto.getBirthday() != null)
				row3.createCell(5).setCellValue( sdf.format(commissionOrderListDto.getBirthday()));
			if (commissionOrderListDto.getReceiveType() != null)
				row3.createCell(6).setCellValue( commissionOrderListDto.getReceiveType().getName() + "");
			if (commissionOrderListDto.getService() != null)
				row3.createCell(7).setCellValue( commissionOrderListDto.getService().getName());
			row3.createCell(8).setCellValue( commissionOrderListDto.isSettle() + "");
			if (commissionOrderListDto.getSchool() != null) {
				row3.createCell(9).setCellValue( commissionOrderListDto.getSchool().getName() + "");
				row3.createCell(13).setCellValue( commissionOrderListDto.getSchool().getSubject() + "");
			}
			if (commissionOrderListDto.getSchoolInstitutionListDTO() != null) {
				row3.createCell(9).setCellValue( commissionOrderListDto.getSchoolInstitutionListDTO().getInstitutionTradingName());
				row3.createCell(10).setCellValue( commissionOrderListDto.getSchoolInstitutionListDTO().getInstitutionName());
				if (commissionOrderListDto.getSchoolInstitutionListDTO().getSchoolInstitutionLocationDO() != null) {
					row3.createCell(11).setCellValue( commissionOrderListDto.getSchoolInstitutionListDTO().getSchoolInstitutionLocationDO().getName());
					row3.createCell(12).setCellValue( commissionOrderListDto.getSchoolInstitutionListDTO().getSchoolInstitutionLocationDO().getState());
				}
				if (commissionOrderListDto.getSchoolInstitutionListDTO().getSchoolCourseDO() != null)
					row3.createCell(13).setCellValue( commissionOrderListDto.getSchoolInstitutionListDTO().getSchoolCourseDO().getCourseName());
			}
			if (commissionOrderListDto.getStartDate() != null)
				row3.createCell(14).setCellValue( sdf.format(commissionOrderListDto.getStartDate()));
			if (commissionOrderListDto.getEndDate() != null)
				row3.createCell(15).setCellValue( sdf.format(commissionOrderListDto.getEndDate()));
			if (commissionOrderListDto.getInstallmentDueDate() != null)
				row3.createCell(
						16).setCellValue( sdf.format(commissionOrderListDto.getInstallmentDueDate()));
			if (commissionOrderListDto.getReceiveType() != null)
				row3.createCell(17).setCellValue( commissionOrderListDto.getReceiveType().getName() + "");
			row3.createCell(18).setCellValue( commissionOrderListDto.getTuitionFee() + "");
			row3.createCell(19).setCellValue( commissionOrderListDto.getPerAmount() + ""); // .getPerTermTuitionFee()

			if (isCn) {
				row3.createCell(20).setCellValue( commissionOrderListDto.getTotalPerAmountCNY() + "");
				row3.createCell(21).setCellValue( commissionOrderListDto.getTotalAmountCNY() + "");
				row3.createCell(22).setCellValue( commissionOrderListDto.getExchangeRate() + "");
				row3.createCell(23).setCellValue( commissionOrderListDto.getCurrency() + "");
				row3.createCell(24).setCellValue( commissionOrderListDto.getAmountCNY() + "");
			} else {
				row3.createCell(20).setCellValue( commissionOrderListDto.getTotalPerAmountAUD() + "");
				row3.createCell(21).setCellValue( commissionOrderListDto.getTotalAmountAUD() + "");
				row3.createCell(22).setCellValue( commissionOrderListDto.getExchangeRate() + "");
				row3.createCell(23).setCellValue( commissionOrderListDto.getCurrency() + "");
				row3.createCell(24).setCellValue( commissionOrderListDto.getAmountAUD() + "");
			}

			row3.createCell(25).setCellValue( commissionOrderListDto.getExpectAmountAUD() + "");
			if (commissionOrderListDto.isSettle()) {
				row3.createCell(26).setCellValue( commissionOrderListDto.getExpectAmountAUD() + "");
				sureExpectAmountTotal = sureExpectAmountTotal.add(new BigDecimal(commissionOrderListDto.getExpectAmountAUD()));
			} else {
				row3.createCell(26).setCellValue( commissionOrderListDto.getSureExpectAmount() + "");
				sureExpectAmountTotal = sureExpectAmountTotal.add(new BigDecimal(commissionOrderListDto.getSureExpectAmountAUD()));
			}
			row3.createCell(27).setCellValue( commissionOrderListDto.getGst() + "");
			row3.createCell(28).setCellValue( commissionOrderListDto.getDeductGst() + "");
			row3.createCell(29).setCellValue( commissionOrderListDto.getSchoolPaymentAmount() + "");
			if (commissionOrderListDto.getSchoolPaymentDate() != null)
				row3.createCell(
						30).setCellValue( sdf.format(commissionOrderListDto.getSchoolPaymentDate()));
			row3.createCell(31).setCellValue( commissionOrderListDto.getInvoiceNumber());
			if (commissionOrderListDto.getZyDate() != null)
				row3.createCell(32).setCellValue( sdf.format(commissionOrderListDto.getZyDate()));
			if (commissionOrderListDto.getSubagency() != null)
				row3.createCell(33).setCellValue( commissionOrderListDto.getSubagency().getName());
			row3.createCell(34).setCellValue( commissionOrderListDto.getBonus() + "");
			if (commissionOrderListDto.getBonusDate() != null)
				row3.createCell(35).setCellValue( sdf.format(commissionOrderListDto.getBonusDate()));
			row3.createCell(36).setCellValue( commissionOrderListDto.getBankCheck());
			row3.createCell(37).setCellValue( commissionOrderListDto.isChecked() + "");
			if (commissionOrderListDto.getAdviser() != null)
				row3.createCell(38).setCellValue( commissionOrderListDto.getAdviser().getName());
			if (commissionOrderListDto.getState() != null)
				row3.createCell(39).setCellValue( getStateStr(commissionOrderListDto.getState()));
			if (commissionOrderListDto.getKjApprovalDate() != null)
				row3.createCell(40).setCellValue( sdf.format(commissionOrderListDto.getKjApprovalDate()));
			row3.createCell(41).setCellValue( commissionOrderListDto.getRemarks());
			ServiceOrderDTO serviceOrderDTO = serviceOrderService
					.getServiceOrderById(commissionOrderListDto.getServiceOrderId());
			row3.createCell(42).setCellValue(
					serviceOrderDTO != null && serviceOrderDTO.getRemarks() != null ? serviceOrderDTO.getRemarks()
							: "" );
			i++;
		}
		i = 1;
		if (!isCn) {
			List<RefundDTO> ovstRefundedList = refundService.listRefund("OVST",
					RefundController.RefundStateEnum.PAID.toString(), null, null, null,
					commissionReportList.get(0).getAdviserId(), null, startDate, endDate, null, null, null, null, 0,
					9999);
			List<RefundDTO> visaRefundedList = refundService.listRefund("VISA",
					RefundController.RefundStateEnum.PAID.toString(), null, null, null,
					commissionReportList.get(0).getAdviserId(), null, startDate, endDate, null, null, null, null, 0,
					9999);
			HSSFSheet sheet3 = wb.getSheetAt(2);
			for (RefundDTO refundDTO : visaRefundedList) {
				HSSFRow row1 = sheet3.createRow(i);
				row1.createCell(0).setCellValue( sdf.format(refundDTO.getGmtCreate()));
				row1.createCell(1).setCellValue( sdf.format(refundDTO.getCompletedDate()));
				row1.createCell(2).setCellValue( refundDTO.getId() + "");
				row1.createCell(3).setCellValue( refundDTO.getVisaId() + "");
				row1.createCell(4).setCellValue( refundDTO.getUserName());
				row1.createCell(5).setCellValue( refundDTO.getUserId() + "");
				row1.createCell(6).setCellValue( refundDTO.getAdviserRegionName() + "");
				row1.createCell(7).setCellValue( "签证");
				row1.createCell(8).setCellValue( refundDTO.getServiceName());
				row1.createCell(9).setCellValue( refundDTO.getReceived() + "");
				row1.createCell(10).setCellValue( refundDTO.getOfficialName());
				row1.createCell(11).setCellValue( refundDTO.getMaraName());
				row1.createCell(12).setCellValue( refundDTO.getAdviserName());
				row1.createCell(13).setCellValue( refundDTO.getAmount() + "");
				row1.createCell(14).setCellValue( refundDTO.getBankName());
				row1.createCell(15).setCellValue( refundDTO.getAccountName());
				row1.createCell(16).setCellValue( refundDTO.getBsb());
				row1.createCell(17).setCellValue( refundDTO.getKjApprovalDate() == null ? "" : sdf.format(refundDTO.getKjApprovalDate()));
				row1.createCell(18).setCellValue( refundDTO.getRefundDetail());
				row1.createCell(19).setCellValue( refundDTO.getRemarks());
				row1.createCell(20).setCellValue( refundDTO.getNote());
				row1.createCell(21).setCellValue( refundDTO.getState());
				i++;
			}
			i +=3;
			String title2 = "退款申请时间,已完成退款时间,ID,原佣金订单ID,Client Name 客户姓名,客户ID,Branch,项目类型,学校名称 Institution Trading Name," +
					"Institution Name,Course Name,实付金额,文案,顾问,Amount 申请退款金额," +
					"Bank Name,Bank account,BSB,提交佣金审核时间(佣金月份),Refund Details 退款原因,备注,财务note,状态";
			String[] titleArr2 = title2.split(",");
			HSSFRow row3 = sheet3.createRow(i);
			for (int i1 = 0; i1 < titleArr2.length; i1++) {
				HSSFCell cell = row3.createCell(i1);
				if ("顾问".equals(titleArr2[i1]) || "Amount 申请退款金额".equals(titleArr2[i1]))
					cell.setCellStyle(style3);
				else
					cell.setCellStyle(style2);
				cell.setCellValue(titleArr2[i1]);
			}
			i++;
			for (RefundDTO refundDTO : ovstRefundedList) {
				HSSFRow row4 = sheet3.createRow(i);
				row4.createCell(0).setCellValue( sdf.format(refundDTO.getGmtCreate()));
				row4.createCell(1).setCellValue( sdf.format(refundDTO.getCompletedDate()));
				row4.createCell(2).setCellValue( refundDTO.getId() + "");
				row4.createCell(3).setCellValue( refundDTO.getCommissionOrderId() + "");
				row4.createCell(4).setCellValue( refundDTO.getUserName());
				row4.createCell(5).setCellValue( refundDTO.getUserId() + "");
				row4.createCell(6).setCellValue( refundDTO.getAdviserRegionName() + "");
				row4.createCell(7).setCellValue( "留学");
				row4.createCell(8).setCellValue( refundDTO.getSchoolName());
				row4.createCell(9).setCellValue( refundDTO.getInstitutionName() + "");
				row4.createCell(10).setCellValue( refundDTO.getCourseName());
				row4.createCell(11).setCellValue( refundDTO.getReceived() + "");
				row4.createCell(12).setCellValue( refundDTO.getOfficialName());
				row4.createCell(13).setCellValue( refundDTO.getAdviserName());
				row4.createCell(14).setCellValue( refundDTO.getAmount() + "");
				row4.createCell(15).setCellValue( refundDTO.getBankName());
				row4.createCell(16).setCellValue( refundDTO.getAccountName());
				row4.createCell(17).setCellValue( refundDTO.getBsb());
				row4.createCell(18).setCellValue( refundDTO.getKjApprovalDate() == null ? "" : sdf.format(refundDTO.getKjApprovalDate()));
				row4.createCell(19).setCellValue( getRefundDetail(refundDTO));
				row4.createCell(10).setCellValue( refundDTO.getRemarks());
				row4.createCell(21).setCellValue( refundDTO.getNote());
				row4.createCell(22).setCellValue( refundDTO.getState());
				i++;
			}
		}
		return wb;
	}


	public int outPutCvToSheet(WritableSheet sheet, WritableCellFormat cellFormat, WritableCellFormat
			cellGreen, WritableCellFormat cellYellow, int i, List<VisaDTO> list, String currency) throws Exception {
		BigDecimal amountTotal = new BigDecimal("0.00");
		BigDecimal gstTotal = new BigDecimal("0.00");
		BigDecimal deductGstTotal = new BigDecimal("0.00");
		BigDecimal expectAmountTotal = new BigDecimal("0.00");
		BigDecimal bonusTotal = new BigDecimal("0.00");
		int j;
		for (VisaDTO visaDto : list) {
			bonusTotal = bonusTotal.add(new BigDecimal(visaDto.getBonusAUD()));
			amountTotal = amountTotal.add(new BigDecimal(visaDto.getAmountAUD()));
			gstTotal = amountTotal.add(new BigDecimal(visaDto.getGstAUD()));
			deductGstTotal = amountTotal.add(new BigDecimal(visaDto.getDeductGstAUD()));
			expectAmountTotal = expectAmountTotal.add(new BigDecimal(visaDto.getExpectAmountAUD()));
			sheet.addCell(new Label(0, i, "CV" + visaDto.getId(), cellFormat));
			sheet.addCell(new Label(1, i, sdf.format(visaDto.getGmtCreate()), cellFormat));
			if (visaDto.getReceiveDate() != null)
				sheet.addCell(new Label(2, i, sdf.format(visaDto.getReceiveDate()), cellFormat));
			sheet.addCell(new Label(3, i, visaDto.getUserName(), cellFormat));
			sheet.addCell(new Label(4, i, visaDto.getReceiveTypeName(), cellFormat));
			sheet.addCell(new Label(5, i, visaDto.getServiceCode(), cellFormat));

			if ("CNY".equalsIgnoreCase(currency)) {
				sheet.addCell(new Label(6, i, visaDto.getTotalPerAmountCNY() + "", cellFormat));
				sheet.addCell(new Label(7, i, visaDto.getTotalAmountCNY() + "", cellFormat));
				sheet.addCell(new Label(8, i, visaDto.getCurrency(), cellFormat));
				sheet.addCell(new Label(9, i, visaDto.getExchangeRate() + "", cellFormat));
				sheet.addCell(new Label(10, i, visaDto.getAmountCNY() + "", cellFormat));
				j = 10;
			} else {
				sheet.addCell(new Label(6, i, visaDto.getTotalPerAmountAUD() + "", cellFormat));
				sheet.addCell(new Label(7, i, visaDto.getTotalAmountAUD() + "", cellFormat));
				sheet.addCell(new Label(8, i, visaDto.getCurrency(), cellFormat));
				sheet.addCell(new Label(9, i, visaDto.getExchangeRate() + "", cellFormat));
				sheet.addCell(new Label(10, i, visaDto.getAmountAUD() + "", cellFormat));
				sheet.addCell(new Label(11, i, visaDto.getGstAUD() + "", cellFormat));
				sheet.addCell(new Label(12, i, visaDto.getDeductGstAUD() + "", cellFormat));
				j = 12;
			}

			sheet.addCell(new Label(j + 1, i, visaDto.getExpectAmountAUD() + "", cellFormat));
			sheet.addCell(new Label(j + 2, i, visaDto.getExpectAmountAUD() + "", cellFormat));
			sheet.addCell(new Label(j + 3, i, visaDto.getBonus() + "", cellFormat));
			if (visaDto.getBonusDate() != null)
				sheet.addCell(new Label(j + 4, i, sdf.format(visaDto.getBonusDate()), cellFormat));
			sheet.addCell(new Label(j + 5, i, visaDto.getBankCheck(), cellFormat));
			sheet.addCell(new Label(j + 6, i, visaDto.isChecked() + "", cellFormat));
			sheet.addCell(new Label(j + 7, i, visaDto.getAdviserName(), cellFormat));
			if (visaDto.getState() != null)
				sheet.addCell(new Label(j + 8, i, getStateStr(visaDto.getState()), cellFormat));
			if (visaDto.getKjApprovalDate() != null)
				sheet.addCell(new Label(j + 9, i, sdf.format(visaDto.getKjApprovalDate()), cellFormat));
			sheet.addCell(new Label(j + 10, i, visaDto.getRemarks(), cellFormat));
			i++;
		}
		sheet.addCell(new Label(9, i, "total", cellYellow));
		sheet.addCell(new Label(10, i, amountTotal.setScale(2, BigDecimal.ROUND_HALF_UP).toString(), cellYellow));
		sheet.addCell(new Label(11, i, gstTotal.setScale(2, BigDecimal.ROUND_HALF_UP).toString(), cellYellow));
		sheet.addCell(new Label(12, i, deductGstTotal.setScale(2, BigDecimal.ROUND_HALF_UP).toString(), cellYellow));
//		sheet.addCell(new Label(j+1, i,  expectAmountTotal.setScale(2,BigDecimal.ROUND_HALF_UP).toString(), cellYellow));
//		sheet.addCell(new Label(j+2, i,  expectAmountTotal.setScale(2,BigDecimal.ROUND_HALF_UP).toString(), cellYellow));
//		sheet.addCell(new Label(j+3, i,  bonusTotal.setScale(2,BigDecimal.ROUND_HALF_UP).toString(), cellYellow));
		return i++;
	}

	public void outPutCsToSheet(WritableSheet sheet, WritableCellFormat cellFormat, WritableCellFormat cellGreen,
			WritableCellFormat cellYellow, int i, List<CommissionOrderListDTO> commissionOrderList, String currency)
			throws Exception {
		String title = "订单ID,佣金订单创建日期,客户支付日期,Student Name,Student ID,生日,收款方式,服务项目,是否提前扣佣,Institute/Institution Trading Name,"
				+
				"Institution Name,Location Name,State,Course Name," +
				"Course Start Date,Course End Date,Installment Due Date,收款方式,Total Tuition Fee,Per Tuition Fee per Installment," +
				"总计应收澳币,总计收款澳币,创建订单时汇率,本次支付币种,本次收款澳币," +
				"Commission,确认预收业绩,GST,Deduct GST,学校支付金额,学校支付时间,Invoice NO.,追佣时间,Subagency,月奖," +
				"月奖支付时间,银行对账字段,是否自动对账,顾问,状态,财务审核时间,佣金备注,服务备注";
		if("CNY".equalsIgnoreCase(currency))
			title = "订单ID,佣金订单创建日期,客户支付日期,Student Name,Student ID,生日,收款方式,服务项目,是否提前扣佣,Institute/Institution Trading Name," +
					"Institution Name,Location Name,State,Course Name," +
					"Course Start Date,Course End Date,Installment Due Date,收款方式,Total Tuition Fee,Per Tuition Fee per Installment," +
					"总计应收人民币,总计收款人民币,创建订单时汇率,本次支付币种,本次收款人民币," +
					"Commission,确认预收业绩,GST,Deduct GST,学校支付金额,学校支付时间,Invoice NO.,追佣时间,Subagency,月奖," +
					"月奖支付时间,银行对账字段,是否自动对账,顾问,状态,财务审核时间,佣金备注,服务备注";
		String [] titleArr = title.split(",");
		for (int ti = 0 ; ti < titleArr.length ; ti ++){
			sheet.addCell(new Label(ti, i, titleArr[ti] , cellGreen));
		}
		i++;

		BigDecimal sureExpectAmountTotal = new BigDecimal("0.00");
		BigDecimal commissionTotal = new BigDecimal("0.00");
		for (CommissionOrderListDTO commissionOrderListDto : commissionOrderList) {
			commissionTotal = commissionTotal.add(new BigDecimal(commissionOrderListDto.getExpectAmountAUD()));
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

			if("CNY".equalsIgnoreCase(currency)) {
				sheet.addCell(new Label(20, i, commissionOrderListDto.getTotalPerAmountCNY() + "", cellFormat));
				sheet.addCell(new Label(21, i, commissionOrderListDto.getTotalAmountCNY() + "", cellFormat));
				sheet.addCell(new Label(22, i, commissionOrderListDto.getExchangeRate() + "", cellFormat));
				sheet.addCell(new Label(23, i, commissionOrderListDto.getCurrency() + "", cellFormat));
				sheet.addCell(new Label(24, i, commissionOrderListDto.getAmountCNY() + "", cellFormat));
			} else {
				sheet.addCell(new Label(20, i, commissionOrderListDto.getTotalPerAmountAUD() + "", cellFormat));
				sheet.addCell(new Label(21, i, commissionOrderListDto.getTotalAmountAUD() + "", cellFormat));
				sheet.addCell(new Label(22, i, commissionOrderListDto.getExchangeRate() + "", cellFormat));
				sheet.addCell(new Label(23, i, commissionOrderListDto.getCurrency() + "", cellFormat));
				sheet.addCell(new Label(24, i, commissionOrderListDto.getAmountAUD() + "", cellFormat));
			}

			sheet.addCell(new Label(25, i, commissionOrderListDto.getExpectAmountAUD() + "", cellFormat));
			if (commissionOrderListDto.isSettle()){
				sheet.addCell(new Label(26, i, commissionOrderListDto.getExpectAmountAUD() + "", cellFormat));
				sureExpectAmountTotal = sureExpectAmountTotal.add(new BigDecimal(commissionOrderListDto.getExpectAmountAUD()));
			}
			else{
				sheet.addCell(new Label(26, i, commissionOrderListDto.getSureExpectAmountAUD() + "", cellFormat));
				sureExpectAmountTotal = sureExpectAmountTotal.add(new BigDecimal(commissionOrderListDto.getSureExpectAmount()));
			}
			sheet.addCell(new Label(27, i, commissionOrderListDto.getGst() + "", cellFormat));
			sheet.addCell(new Label(28, i, commissionOrderListDto.getDeductGst() + "", cellFormat));
			sheet.addCell(new Label(29, i, commissionOrderListDto.getSchoolPaymentAmount() + "", cellFormat));
			if (commissionOrderListDto.getSchoolPaymentDate() != null)
				sheet.addCell(
						new Label(30, i, sdf.format(commissionOrderListDto.getSchoolPaymentDate()), cellFormat));
			sheet.addCell(new Label(31, i, commissionOrderListDto.getInvoiceNumber(), cellFormat));
			if (commissionOrderListDto.getZyDate() != null)
				sheet.addCell(new Label(32, i, sdf.format(commissionOrderListDto.getZyDate()), cellFormat));
			if (commissionOrderListDto.getSubagency() != null)
				sheet.addCell(new Label(33, i, commissionOrderListDto.getSubagency().getName(), cellFormat));
			sheet.addCell(new Label(34, i, commissionOrderListDto.getBonus() + "", cellFormat));
			if (commissionOrderListDto.getBonusDate() != null)
				sheet.addCell(new Label(35, i, sdf.format(commissionOrderListDto.getBonusDate()), cellFormat));
			sheet.addCell(new Label(36, i, commissionOrderListDto.getBankCheck(), cellFormat));
			sheet.addCell(new Label(37, i, commissionOrderListDto.isChecked() + "", cellFormat));
			if (commissionOrderListDto.getAdviser() != null)
				sheet.addCell(new Label(38, i, commissionOrderListDto.getAdviser().getName(), cellFormat));
			if (commissionOrderListDto.getState() != null)
				sheet.addCell(new Label(39, i, getStateStr(commissionOrderListDto.getState()), cellFormat));
			if (commissionOrderListDto.getKjApprovalDate() != null)
				sheet.addCell(new Label(40, i, sdf.format(commissionOrderListDto.getKjApprovalDate()), cellFormat));
			sheet.addCell(new Label(41, i, commissionOrderListDto.getRemarks(), cellFormat));
			ServiceOrderDTO serviceOrderDTO = serviceOrderService
					.getServiceOrderById(commissionOrderListDto.getServiceOrderId());
			sheet.addCell(new Label(42, i,
					serviceOrderDTO != null && serviceOrderDTO.getRemarks() != null ? serviceOrderDTO.getRemarks()
							: "",
					cellFormat));
			i++;
		}
//		sheet.addCell(new Label(27, i,  "total", cellYellow));
//		sheet.addCell(new Label(28, i,  commissionTotal.setScale(2,BigDecimal.ROUND_HALF_UP).toString(), cellYellow));
//		sheet.addCell(new Label(29, i,  sureExpectAmountTotal.setScale(2,BigDecimal.ROUND_HALF_UP).toString(), cellYellow));
	}

	public int outPutVisaToRefundSheet(WritableSheet sheet, WritableCellFormat cellFormat, int i, List<RefundDTO> list) throws Exception {
		for (RefundDTO refundDTO : list) {
			sheet.addCell(new Label(0, i, sdf.format(refundDTO.getGmtCreate()), cellFormat));
			sheet.addCell(new Label(1, i, refundDTO.getId() + "", cellFormat));
			sheet.addCell(new Label(2, i, refundDTO.getVisaId() + "", cellFormat));
			sheet.addCell(new Label(3, i, refundDTO.getUserName(), cellFormat));
			sheet.addCell(new Label(4, i, refundDTO.getUserId() + "", cellFormat));
			sheet.addCell(new Label(5, i, refundDTO.getAdviserRegionName() + "", cellFormat));
			sheet.addCell(new Label(6, i, "签证", cellFormat));
			sheet.addCell(new Label(7, i, refundDTO.getServiceName(), cellFormat));
			sheet.addCell(new Label(8, i, refundDTO.getReceived() + "", cellFormat));
			sheet.addCell(new Label(9, i, refundDTO.getOfficialName(), cellFormat));
			sheet.addCell(new Label(10, i, refundDTO.getMaraName(), cellFormat));
			sheet.addCell(new Label(11, i, refundDTO.getAdviserName(), cellFormat));
			sheet.addCell(new Label(12, i, refundDTO.getAmount() + "", cellFormat));
			sheet.addCell(new Label(13, i, refundDTO.getBankName(), cellFormat));
			sheet.addCell(new Label(14, i, refundDTO.getBsb(), cellFormat));
			sheet.addCell(new Label(15, i, refundDTO.getKjApprovalDate() == null ? "" : sdf.format(refundDTO.getKjApprovalDate()), cellFormat));
			sheet.addCell(new Label(16, i, refundDTO.getRefundDetail(), cellFormat));
			sheet.addCell(new Label(17, i, refundDTO.getRemarks(), cellFormat));
			sheet.addCell(new Label(18, i, refundDTO.getState(), cellFormat));
			i++;
		}
		return i;
	}

	public void outPutOvstToRefundSheet(WritableSheet sheet, WritableCellFormat cellFormat, WritableCellFormat cellGreen,
										int i, List<RefundDTO> list) throws Exception {

		String title = "退款申请时间,ID,原佣金订单ID,Client Name 客户姓名,客户ID,Branch,项目类型,学校名称 Institution Trading Name," +
				"Institution Name,Course Name,实付金额,文案,顾问,Amount 申请退款金额," +
				"Bank Name,BSB,提交佣金审核时间(佣金月份),Refund Details 退款原因,备注,状态";
		String[] titleArr = title.split(",");
		for (int ti = 0; ti < titleArr.length; ti++) {
			sheet.addCell(new Label(ti, i, titleArr[ti], cellGreen));
		}
		i++;
		for (RefundDTO refundDTO : list) {
			sheet.addCell(new Label(0, i, sdf.format(refundDTO.getGmtCreate()), cellFormat));
			sheet.addCell(new Label(1, i, refundDTO.getId() + "", cellFormat));
			sheet.addCell(new Label(2, i, refundDTO.getCommissionOrderId() + "", cellFormat));
			sheet.addCell(new Label(3, i, refundDTO.getUserName(), cellFormat));
			sheet.addCell(new Label(4, i, refundDTO.getUserId() + "", cellFormat));
			sheet.addCell(new Label(5, i, refundDTO.getAdviserRegionName() + "", cellFormat));
			sheet.addCell(new Label(6, i, "留学", cellFormat));
			sheet.addCell(new Label(7, i, refundDTO.getSchoolName(), cellFormat));
			sheet.addCell(new Label(8, i, refundDTO.getInstitutionName() + "", cellFormat));
			sheet.addCell(new Label(9, i, refundDTO.getCourseName(), cellFormat));
			sheet.addCell(new Label(10, i, refundDTO.getReceived() + "", cellFormat));
			sheet.addCell(new Label(11, i, refundDTO.getOfficialName(), cellFormat));
			sheet.addCell(new Label(12, i, refundDTO.getAdviserName(), cellFormat));
			sheet.addCell(new Label(13, i, refundDTO.getAmount() + "", cellFormat));
			sheet.addCell(new Label(14, i, refundDTO.getBankName(), cellFormat));
			sheet.addCell(new Label(15, i, refundDTO.getBsb(), cellFormat));
			sheet.addCell(new Label(16, i, refundDTO.getKjApprovalDate() == null ? "" : sdf.format(refundDTO.getKjApprovalDate()), cellFormat));
			sheet.addCell(new Label(17, i, getRefundDetail(refundDTO), cellFormat));
			sheet.addCell(new Label(18, i, refundDTO.getRemarks(), cellFormat));
			sheet.addCell(new Label(19, i, refundDTO.getState(), cellFormat));
			i++;
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

	private String getRefundDetail(RefundDTO refundDTO) {
		if (refundDTO.getRefundDetailId() == 1)
			return "业务未成功办理，客户要求退款";
		else if (refundDTO.getRefundDetailId() == 2)
			return "客户取消业务";
		else if (refundDTO.getRefundDetailId() == 3)
			return "押金退款";
		else if (refundDTO.getRefundDetailId() == 4)
			return "referfee";
		else if (refundDTO.getRefundDetailId() == 5)
			return "Subagent结算";
		else if (refundDTO.getRefundDetailId() == 6)
			return "返佣";
		else if (refundDTO.getRefundDetailId() == 7)
			return "客户转错钱,全款退还";
		else if (refundDTO.getRefundDetailId() == 8)
			return "赔偿";
		else if (refundDTO.getRefundDetailId() == 99)
			return "其它:" + refundDTO.getRefundDetail();
		return null;
	}

}