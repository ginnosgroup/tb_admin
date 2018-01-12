package org.zhinanzhen.tb.controller;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zhinanzhen.tb.service.OrderPayTypeEnum;
import org.zhinanzhen.tb.service.OrderService;
import org.zhinanzhen.tb.service.OrderStateEnum;
import org.zhinanzhen.tb.service.UserService;
import org.zhinanzhen.tb.service.pojo.OrderDTO;
import org.zhinanzhen.tb.service.pojo.UserDTO;
import org.zhinanzhen.tb.utils.ConfigService;

import com.ikasoa.core.utils.StringUtil;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;

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

	@RequestMapping("/user")
	public void userExport(String name, String authNickname, String phone, HttpServletResponse response)
			throws Exception {
		OutputStream os = response.getOutputStream();// 取得输出流
		response.reset();// 清空输出流
		String tableName = "user_information";
		response.setHeader("Content-disposition",
				"attachment; filename=" + new String(tableName.getBytes("GB2312"), "8859_1") + ".xls");// 设定输出文件头
		response.setContentType("application/msexcel");
		String inpath = "/UserTemplate2.xls";
		List<UserDTO> userDtoList = userService.listUser(name, null, authNickname, phone, 0, 10000); // TODO:
																										// sulei
																										// 记得把类型过滤加上
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
}
