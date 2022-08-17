package org.zhinanzhen.b.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.zhinanzhen.b.controller.nodes.RNodeFactory;
import org.zhinanzhen.b.service.RefundService;
import org.zhinanzhen.b.service.pojo.RefundDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.ListResponse;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

import com.ikasoa.core.utils.ObjectUtil;
import com.ikasoa.core.utils.StringUtil;
import com.ikasoa.web.workflow.Context;
import com.ikasoa.web.workflow.Node;
import com.ikasoa.web.workflow.Workflow;
import com.ikasoa.web.workflow.WorkflowStarter;
import com.ikasoa.web.workflow.impl.WorkflowStarterImpl;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/refund")
public class RefundController extends BaseController {

	private static final Logger LOG = LoggerFactory.getLogger(RefundController.class);

	private static WorkflowStarter workflowStarter = new WorkflowStarterImpl();

	@Resource
	RefundService refundService;

	@Resource
	RNodeFactory rNodeFactory;
	
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public enum RefundStateEnum {
		PENDING, REVIEW, COMPLETE, PAID, CLOSE;

		public static RefundStateEnum get(String name) {
			for (RefundStateEnum e : RefundStateEnum.values())
				if (e.toString().equals(name))
					return e;
			return null;
		}
	}

	@RequestMapping(value = "/upload_img", method = RequestMethod.POST)
	@ResponseBody
	public Response<String> uploadImage(@RequestParam MultipartFile file, HttpServletRequest request,
			HttpServletResponse response) throws IllegalStateException, IOException {
		super.setPostHeader(response);
		return super.upload2(file, request.getSession(), "/uploads/payment_voucher_image_url_r/");
	}

	@RequestMapping(value = "/upload_img2", method = RequestMethod.POST)
	@ResponseBody
	public Response<String> uploadImage2(@RequestParam MultipartFile file, HttpServletRequest request,
			HttpServletResponse response) throws IllegalStateException, IOException {
		super.setPostHeader(response);
		return super.upload2(file, request.getSession(), "/uploads/refund_voucher_image_url/");
	}

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> add(@RequestBody RefundDTO refundDto, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo == null || (!"SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())
					&& !"GW".equalsIgnoreCase(adminUserLoginInfo.getApList())))
				return new Response<Integer>(1, "仅限顾问和超级管理员能创建退款单.", 0);
			if (refundDto.getReceived() > 0 && refundDto.getAmount() > refundDto.getReceived())
				return new Response<Integer>(1, "退款金额不能大于实付金额.", 0);
			if (refundDto.getAdviserId() <= 0 && "GW".equalsIgnoreCase(adminUserLoginInfo.getApList()))
				refundDto.setAdviserId(getAdviserId(request));
			if (refundService.addRefund(refundDto) > 0) {
				return new Response<Integer>(0, refundDto.getId());
			} else {
				return new Response<Integer>(0, "创建失败.", 0);
			}
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public ListResponse<List<RefundDTO>> list(@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "state", required = false) String state, @RequestParam(value = "pageNum") int pageNum,
			@RequestParam(value = "pageSize") int pageSize, HttpServletRequest request, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			if (getKjId(request) != null) {
				if (state == null)
					state = "REVIEW";
				return new ListResponse<List<RefundDTO>>(true, pageSize,
						refundService.countRefund(type, state, null, null, null),
						refundService.listRefund(type, state, null, null, null, pageNum, pageSize), null);
			}
			if (getAdviserId(request) != null)
				return new ListResponse<List<RefundDTO>>(true, pageSize,
						refundService.countRefund(type, state, getAdviserId(request), null, null),
						refundService.listRefund(type, state, getAdviserId(request), null, null, pageNum, pageSize),
						null);
			return new ListResponse<List<RefundDTO>>(false, pageSize, 0, null, "仅顾问和会计才有权限查看退款单!");
		} catch (ServiceException e) {
			return new ListResponse<List<RefundDTO>>(false, pageSize, 0, null, e.getMessage());
		}
	}

	@RequestMapping(value = "/down", method = RequestMethod.GET)
	@ResponseBody
	public void down(@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "state", required = false) String state, HttpServletRequest request,
			HttpServletResponse response) {
		List<RefundDTO> list = null;
		try {
			if (getKjId(request) != null) {
				if (state == null)
					state = "REVIEW";
				list = refundService.listRefund(type, state, null, null, null, 0, 9999);
			}
			if (getAdviserId(request) != null)
				list = refundService.listRefund(type, state, getAdviserId(request), null, null, 0, 9999);
		} catch (ServiceException e) {
			e.printStackTrace();
			return;
		}
		if(list == null) {
			LOG.error("仅顾问和会计才有权限查看退款单!");
			return;
		}
		InputStream is = null;
		OutputStream os = null;
		jxl.Workbook wb = null;
		try {
			response.reset();
			String tableName = "refund_order_list";
			response.setHeader("Content-disposition",
					"attachment; filename=" + new String(tableName.getBytes("GB2312"), "8859_1") + ".xls");
			response.setContentType("application/msexcel");
			os = response.getOutputStream();
			try {
				is = this.getClass().getResourceAsStream("/RefundOrderList.xls");
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
				LOG.debug("wbe is null !os=" + os + ",wb" + wb);
			} else {
				LOG.debug("wbe not null !os=" + os + ",wb" + wb);
			}
			WritableSheet sheet = wbe.getSheet(0);
			WritableCellFormat cellFormat = new WritableCellFormat();
			int i = 1;
			for (RefundDTO refundDto : list) {
				sheet.addCell(new Label(0, i, refundDto.getId() + "", cellFormat));
				if (refundDto.getGmtCreate() != null)
					sheet.addCell(new Label(1, i, sdf.format(refundDto.getGmtCreate()), cellFormat));
				if ("VISA".equalsIgnoreCase(refundDto.getType()))
					sheet.addCell(new Label(2, i, refundDto.getVisaId() + "", cellFormat));
				else if ("OVST".equalsIgnoreCase(refundDto.getType()))
					sheet.addCell(new Label(2, i, refundDto.getCommissionOrderId() + "", cellFormat));
				sheet.addCell(new Label(3, i, refundDto.getUserName(), cellFormat));
				if ("VISA".equalsIgnoreCase(refundDto.getType()))
					sheet.addCell(new Label(4, i, "签证", cellFormat));
				else if ("OVST".equalsIgnoreCase(refundDto.getType()))
					sheet.addCell(new Label(4, i, "留学", cellFormat));
				sheet.addCell(new Label(5, i, refundDto.getSchoolName(), cellFormat));
				sheet.addCell(new Label(6, i, refundDto.getInstitutionName(), cellFormat));
				sheet.addCell(new Label(7, i, refundDto.getCourseName(), cellFormat));
				sheet.addCell(new Label(8, i, refundDto.getReceived() + "", cellFormat));
				sheet.addCell(new Label(9, i, refundDto.getOfficialName(), cellFormat));
				sheet.addCell(new Label(10, i, refundDto.getAdviserName(), cellFormat));
				sheet.addCell(new Label(11, i, refundDto.getAmount() + "", cellFormat));
				sheet.addCell(new Label(12, i, refundDto.getAccountName(), cellFormat));
				sheet.addCell(new Label(13, i, refundDto.getBsb(), cellFormat));
				sheet.addCell(new Label(14, i, refundDto.getBankName(), cellFormat));
				sheet.addCell(new Label(15, i, refundDto.getRmbRemarks(), cellFormat));
				sheet.addCell(new Label(16, i, refundDto.getRefundDetail(), cellFormat));
				if (refundDto.getReceiveDate() != null)
					sheet.addCell(new Label(17, i, sdf.format(refundDto.getReceiveDate()), cellFormat));
				if ("PENDING".equalsIgnoreCase(refundDto.getState()))
					if (StringUtil.isEmpty(refundDto.getReason()))
						sheet.addCell(new Label(18, i, "已驳回-" + refundDto.getReason(), cellFormat));
					else
						sheet.addCell(new Label(18, i, "待提交", cellFormat));
				else if ("REVIEW".equalsIgnoreCase(refundDto.getState()))
					sheet.addCell(new Label(18, i, "审核中", cellFormat));
				else if ("COMPLETE".equalsIgnoreCase(refundDto.getState()))
					sheet.addCell(new Label(18, i, "已通过", cellFormat));
				else if ("PAID".equalsIgnoreCase(refundDto.getState()))
					sheet.addCell(new Label(18, i, "退款完成", cellFormat));
				else if ("CLOSE".equalsIgnoreCase(refundDto.getState()))
					sheet.addCell(new Label(18, i, "已关闭", cellFormat));
				else
					sheet.addCell(new Label(18, i, "未知状态", cellFormat));
				sheet.addCell(new Label(19, i, refundDto.getRemarks(), cellFormat));
				i++;
			}
			wbe.write();
			wbe.close();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		} finally {
			try {
				if (is != null)
					is.close();
				LOG.debug("is is close");
			} catch (IOException e) {
				LOG.error("is is close 出现 异常:");
				e.printStackTrace();
			}
			try {
				if (os != null)
					os.close();
				LOG.debug("os is close");
			} catch (IOException e) {
				LOG.error("os is close 出现 异常:");
				e.printStackTrace();
			}
			if (wb != null)
				wb.close();
			LOG.debug("wb is close");
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

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> update(@RequestBody RefundDTO refundDto, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo == null || (!"SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())
					&& !"KJ".equalsIgnoreCase(adminUserLoginInfo.getApList())))
				return new Response<Integer>(1, "仅限财务和超级管理员能修改退款单.", 0);
			if (refundDto.getId() <= 0)
				return new Response<Integer>(1, "id不正确.", 0);
			RefundDTO _refundDto = refundService.getRefundById(refundDto.getId());
			if (refundDto.getState() != null)
				_refundDto.setState(refundDto.getState());
			if (refundDto.getPaymentVoucherImageUrl() != null)
				_refundDto.setPaymentVoucherImageUrl(refundDto.getPaymentVoucherImageUrl());
			if (refundDto.getRefundVoucherImageUrl() != null)
				_refundDto.setRefundVoucherImageUrl(refundDto.getRefundVoucherImageUrl());
			if (refundService.updateRefund(_refundDto) > 0) {
				return new Response<Integer>(0, _refundDto.getId());
			} else {
				return new Response<Integer>(1, "修改失败.", 0);
			}
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/update1", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> update1(@RequestBody RefundDTO refundDto, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo == null || (!"SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())
					&& !"GW".equalsIgnoreCase(adminUserLoginInfo.getApList())))
				return new Response<Integer>(1, "仅限顾问和超级管理员能修改退款单.", 0);
			if (refundDto.getId() <= 0)
				return new Response<Integer>(1, "id不正确.", 0);
			RefundDTO _refundDto = refundService.getRefundById(refundDto.getId());
			if (_refundDto.getAdviserId() != super.getAdviserId(request))
				return new Response<Integer>(1, "不属于该退款单顾问不能修改.", 0);
			refundDto.setState(null);
			if (refundDto.getReceived() > 0 && refundDto.getAmount() > refundDto.getReceived())
				return new Response<Integer>(1, "退款金额不能大于实付金额.", 0);
			if (refundService.updateRefund(refundDto) > 0) {
				return new Response<Integer>(0, "修改成功.");
			} else {
				return new Response<Integer>(1, "修改失败.", 0);
			}
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/deleteRefundById", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> deleteRefundById(@RequestParam(value = "id") int id, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo == null || (!"SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList()) && !"KJ".equalsIgnoreCase(adminUserLoginInfo.getApList())))
				return new Response<Integer>(1, "仅限超级管理员和会计能删除退款单.", 0);
			return new Response<Integer>(0, refundService.deleteRefundById(id));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/next_flow", method = RequestMethod.POST)
	@ResponseBody
	public Response<RefundDTO> nextFlow(@RequestBody RefundDTO refundDto, HttpServletRequest request,
			HttpServletResponse response) {
		AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
		if (adminUserLoginInfo == null)
			return new Response<RefundDTO>(1, "请先登录.", null);
		if (ObjectUtil.isNull(refundDto))
			return new Response<RefundDTO>(1, "退款单不存在!", null);
		if (refundDto.getId() <= 0)
			return new Response<RefundDTO>(1, "id不正确:" + refundDto.getId(), null);
		Node node = rNodeFactory.getNode(refundDto.getState());

		Context context = new Context();
		context.putParameter("refundId", refundDto.getId());
		context.putParameter("type", refundDto.getType());
		context.putParameter("state", refundDto.getState());
		context.putParameter("reason", refundDto.getReason());
		context.putParameter("paymentVoucherImageUrl", refundDto.getPaymentVoucherImageUrl());
		context.putParameter("ap", adminUserLoginInfo.getApList());
		context.putParameter("adminUserId", adminUserLoginInfo.getId());

		LOG.info("Flow API Log : " + context.toString());

		String[] nextNodeNames = node.nextNodeNames();
		if (nextNodeNames != null)
			if (Arrays.asList(nextNodeNames).contains(refundDto.getState()))
				node = rNodeFactory.getNode(refundDto.getState());
			else
				return new Response<RefundDTO>(1,
						StringUtil.merge("状态:", refundDto.getState(), "不是合法状态. (", Arrays.toString(nextNodeNames), ")"),
						null);

		Workflow workflow = new Workflow("Refund Work Flow", node, rNodeFactory);

		context = workflowStarter.process(workflow, context);

		return context.getParameter("response") != null ? (Response<RefundDTO>) context.getParameter("response")
				: new Response<RefundDTO>(0, refundDto.getId() + "", null);
	}

}
