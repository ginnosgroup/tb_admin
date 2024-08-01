package org.zhinanzhen.b.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

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
import org.zhinanzhen.b.dao.pojo.ServiceOrderDO;
import org.zhinanzhen.b.service.*;
import org.zhinanzhen.b.service.pojo.*;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.ListResponse;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.RegionService;
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
import org.zhinanzhen.tb.utils.SendEmailUtil;


@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/refund")
public class RefundController extends BaseController {

	private static final Logger LOG = LoggerFactory.getLogger(RefundController.class);

	private static WorkflowStarter workflowStarter = new WorkflowStarterImpl();

	@Resource
	RefundService refundService;
	
	@Resource
	ExchangeRateService exchangeRateService;
	
	@Resource
	RegionService regionService;

	@Resource
	RNodeFactory rNodeFactory;

	@Resource
	private VisaService visaService;

	@Resource
	private VisaOfficialService visaOfficialService;

	@Resource
	private ServiceOrderService serviceOrderService;

	@Resource
	private OfficialService officialService;

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
			if (refundDto.getExchangeRate() == 0) {
				if (regionService.isCNByAdviserId(refundDto.getAdviserId())) { // 如果是中国地区则使用季度固定汇率
					double qRate = exchangeRateService.getQuarterExchangeRate();
					LOG.info(StringUtil.merge("为退款订单(", refundDto.getId(), ")设置季度固定汇率:", qRate));
					refundDto.setExchangeRate(qRate);
				} else {
					ExchangeRateDTO exchangeRateDto = exchangeRateService.getExchangeRate();
					if (ObjectUtil.isNotNull(exchangeRateDto))
						refundDto.setExchangeRate(exchangeRateDto.getRate());
				}
			}
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
			@RequestParam(value = "state", required = false) String state,
			@RequestParam(value = "visaId", required = false) Integer visaId,
			@RequestParam(value = "commissionOrderId", required = false) Integer commissionOrderId,
			@RequestParam(value = "adviserName", required = false) String adviserName,
			@RequestParam(value = "regionId", required = false) Integer regionId,
			@RequestParam(value = "startDate", required = false) String startDate,
			@RequestParam(value = "endDate", required = false) String endDate,
			@RequestParam(value = "startReviewedDate", required = false) String startReviewedDate,
			@RequestParam(value = "endReviewedDate", required = false) String endReviewedDate,
			@RequestParam(value = "startCompletedDate", required = false) String startCompletedDate,
			@RequestParam(value = "endCompletedDate", required = false) String endCompletedDate,
			@RequestParam(value = "pageNum") int pageNum, @RequestParam(value = "pageSize") int pageSize,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			if (getKjId(request) != null) {
				if (state == null)
					state = "REVIEW";
				return new ListResponse<List<RefundDTO>>(true, pageSize,
						refundService.countRefund(type, state, visaId, commissionOrderId, regionId, null, adviserName,
								startDate, endDate, startReviewedDate, endReviewedDate, startCompletedDate,
								endCompletedDate),
						refundService.listRefund(type, state, visaId, commissionOrderId, regionId, null, adviserName, startDate,
								endDate, startReviewedDate, endReviewedDate, startCompletedDate, endCompletedDate,
								pageNum, pageSize),
						null);
			}
			if (getAdviserId(request) != null)
				return new ListResponse<List<RefundDTO>>(true, pageSize,
						refundService.countRefund(type, state, visaId, commissionOrderId, regionId, getAdviserId(request), adviserName, startDate,
								endDate, startReviewedDate, endReviewedDate, startCompletedDate, endCompletedDate),
						refundService.listRefund(type, state, visaId, commissionOrderId, regionId, getAdviserId(request), adviserName, startDate,
								endDate, startReviewedDate, endReviewedDate, startCompletedDate, endCompletedDate,
								pageNum, pageSize),
						null);
			return new ListResponse<List<RefundDTO>>(false, pageSize, 0, null, "仅顾问和会计才有权限查看退款单!");
		} catch (ServiceException e) {
			return new ListResponse<List<RefundDTO>>(false, pageSize, 0, null, e.getMessage());
		}
	}

	@RequestMapping(value = "/down", method = RequestMethod.GET)
	@ResponseBody
	public void down(@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "state", required = false) String state,
			@RequestParam(value = "visaId", required = false) Integer visaId,
			@RequestParam(value = "commissionOrderId", required = false) Integer commissionOrderId,
			@RequestParam(value = "adviserName", required = false) String adviserName,
			@RequestParam(value = "regionId", required = false) Integer regionId,
			@RequestParam(value = "startDate", required = false) String startDate,
			@RequestParam(value = "endDate", required = false) String endDate,
			@RequestParam(value = "startReviewedDate", required = false) String startReviewedDate,
			@RequestParam(value = "endReviewedDate", required = false) String endReviewedDate,
			@RequestParam(value = "startCompletedDate", required = false) String startCompletedDate,
			@RequestParam(value = "endCompletedDate", required = false) String endCompletedDate, HttpServletRequest request,
			HttpServletResponse response) {
		List<RefundDTO> list = null;
		try {
			if (getKjId(request) != null) {
				if (state == null)
					state = "REVIEW";
				list = refundService.listRefund(type, state, visaId, commissionOrderId, regionId, null, adviserName, startDate,
						endDate, startReviewedDate, endReviewedDate, startCompletedDate, endCompletedDate, 0, 9999);
			}
			if (getAdviserId(request) != null)
				list = refundService.listRefund(type, state, visaId, commissionOrderId, regionId, getAdviserId(request),
						adviserName, startDate, endDate, startReviewedDate, endReviewedDate, startCompletedDate,
						endCompletedDate, 0, 9999);
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
				if (refundDto.getReceiveDate() != null)
					sheet.addCell(new Label(2, i, sdf.format(refundDto.getReceiveDate()), cellFormat));
				if (refundDto.getCompletedDate() != null)
					sheet.addCell(new Label(3, i, sdf.format(refundDto.getCompletedDate()), cellFormat));
				if ("VISA".equalsIgnoreCase(refundDto.getType()))
					sheet.addCell(new Label(4, i, refundDto.getVisaId() + "", cellFormat));
				else if ("OVST".equalsIgnoreCase(refundDto.getType()))
					sheet.addCell(new Label(4, i, refundDto.getCommissionOrderId() + "", cellFormat));
				sheet.addCell(new Label(5, i, refundDto.getUserName(), cellFormat));
				if ("VISA".equalsIgnoreCase(refundDto.getType()))
					sheet.addCell(new Label(6, i, "签证", cellFormat));
				else if ("OVST".equalsIgnoreCase(refundDto.getType()))
					sheet.addCell(new Label(6, i, "留学", cellFormat));
				sheet.addCell(new Label(7, i, refundDto.getSchoolName(), cellFormat));
				sheet.addCell(new Label(8, i, refundDto.getInstitutionName(), cellFormat));
				sheet.addCell(new Label(9, i, refundDto.getCourseName(), cellFormat));
				sheet.addCell(new Label(10, i, refundDto.getReceived() + "", cellFormat));
				sheet.addCell(new Label(11, i, refundDto.getOfficialName(), cellFormat));
				sheet.addCell(new Label(12, i, refundDto.getAdviserName(), cellFormat));
				sheet.addCell(new Label(13, i, refundDto.getAmount() + "", cellFormat));
				sheet.addCell(new Label(14, i, refundDto.getAccountName(), cellFormat));
				sheet.addCell(new Label(15, i, refundDto.getBsb(), cellFormat));
				sheet.addCell(new Label(16, i, refundDto.getBankName(), cellFormat));
				sheet.addCell(new Label(17, i, refundDto.getRmbRemarks(), cellFormat));
				sheet.addCell(new Label(18, i, refundDto.getRefundDetail(), cellFormat));
				if (refundDto.getReceiveDate() != null)
					sheet.addCell(new Label(19, i, sdf.format(refundDto.getReceiveDate()), cellFormat));
				if ("PENDING".equalsIgnoreCase(refundDto.getState()))
					if (StringUtil.isEmpty(refundDto.getReason()))
						sheet.addCell(new Label(20, i, "已驳回-" + refundDto.getReason(), cellFormat));
					else
						sheet.addCell(new Label(20, i, "待提交", cellFormat));
				else if ("REVIEW".equalsIgnoreCase(refundDto.getState()))
					sheet.addCell(new Label(20, i, "审核中", cellFormat));
				else if ("COMPLETE".equalsIgnoreCase(refundDto.getState()))
					sheet.addCell(new Label(20, i, "已通过", cellFormat));
				else if ("PAID".equalsIgnoreCase(refundDto.getState()))
					sheet.addCell(new Label(20, i, "退款完成", cellFormat));
				else if ("CLOSE".equalsIgnoreCase(refundDto.getState()))
					sheet.addCell(new Label(20, i, "已关闭", cellFormat));
				else
					sheet.addCell(new Label(20, i, "未知状态", cellFormat));
				sheet.addCell(new Label(21, i, refundDto.getRemarks(), cellFormat));
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
			if (refundDto.getCompletedDate() != null)
				_refundDto.setCompletedDate(refundDto.getCompletedDate());
			if (refundDto.getPaymentVoucherImageUrl() != null)
				_refundDto.setPaymentVoucherImageUrl(refundDto.getPaymentVoucherImageUrl());
			if (refundDto.getPaymentVoucherImageUrl2() != null)
				_refundDto.setPaymentVoucherImageUrl(refundDto.getPaymentVoucherImageUrl2());
			if (refundDto.getPaymentVoucherImageUrl3() != null)
				_refundDto.setPaymentVoucherImageUrl(refundDto.getPaymentVoucherImageUrl3());
			if (refundDto.getPaymentVoucherImageUrl4() != null)
				_refundDto.setPaymentVoucherImageUrl(refundDto.getPaymentVoucherImageUrl4());
			if (refundDto.getPaymentVoucherImageUrl5() != null)
				_refundDto.setPaymentVoucherImageUrl(refundDto.getPaymentVoucherImageUrl5());
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
			LOG.info(StringUtil.merge("删除退款订单:", refundService.getRefundById(id)));
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
		context.putParameter("paymentVoucherImageUrl2", refundDto.getPaymentVoucherImageUrl2());
		context.putParameter("paymentVoucherImageUrl3", refundDto.getPaymentVoucherImageUrl3());
		context.putParameter("paymentVoucherImageUrl4", refundDto.getPaymentVoucherImageUrl4());
		context.putParameter("paymentVoucherImageUrl5", refundDto.getPaymentVoucherImageUrl5());
		context.putParameter("note", refundDto.getNote());
		context.putParameter("ap", adminUserLoginInfo.getApList());
		context.putParameter("adminUserId", adminUserLoginInfo.getId());

		LOG.info("Flow API Log : " + context.toString());
		LOG.info("refundDto : " + refundDto);

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

		// 更新文案佣金订单金额以及发送邮件提醒退款信息
		try {
			if ("COMPLETE".equals(refundDto.getState())) {
				AtomicReference<String> visaOfficailId = new AtomicReference<>("");
				RefundDTO refundById = refundService.getRefundById(refundDto.getId());
				VisaDTO visaById = visaService.getVisaById(refundById.getVisaId());
				List<ServiceOrderDTO> list = serviceOrderService.getZiServiceOrderById(visaById.getServiceOrderId());
				// 父子订单重新结算
				if (list != null && !list.isEmpty()) {
					list.forEach(e -> {
                        try {
                            VisaOfficialDTO visaOfficialDTO = visaOfficialService.getByServiceOrderId(e.getId());
							if (ObjectUtil.isNotNull(visaOfficialDTO)) {
								visaOfficailId.set(visaOfficailId + "," + visaOfficialDTO.getId());
								visaOfficialDTO.setIsRefund(true);
								visaOfficialService.addVisa(visaOfficialDTO);
							}
                        } catch (ServiceException ex) {
                            throw new RuntimeException(ex);
                        }
                    });
				} else  { // 非父子订单重新结算
					List<VisaOfficialDTO> allvisaOfficialByServiceOrderId = visaOfficialService.getAllvisaOfficialByServiceOrderId(visaById.getServiceOrderId());
					if (allvisaOfficialByServiceOrderId != null) {
						allvisaOfficialByServiceOrderId.forEach(e -> {
							visaOfficailId.set(visaOfficailId + "," + e.getId());
							e.setIsRefund(true);
							try {
								visaOfficialService.addVisa(e);
							} catch (ServiceException ex) {
								throw new RuntimeException(ex);
							}
						});
					}
				}
				OfficialDTO officialById = officialService.getOfficialById(visaById.getOfficialId());
				String visaOfficailIds = visaOfficailId.get();
				String substring = visaOfficailIds.substring(1);
				String email = "jiaheng.xu@zhinanzhen.org";
				String title = "服务订单：" + visaById.getServiceOrderId()  + "退款信息：";
				String message = "<h1>订单退款信息</h1>"
						+ "<table border='1'>"
						+ "<tr><th>退款ID</th><th>佣金订单ID</th><th>服务订单ID</th><th>文案佣金订单ID</th><th>顾问名称</th></tr>"
						+ "<tr><td>" + refundDto.getId() + "</td><td>" + visaById.getId() + "</td><td>" + visaById.getServiceOrderId() + "</td><td>" + substring + "</td><td>" + officialById.getName() +"</td></tr>"
						+ "</table>";
				SendEmailUtil.send(email, title, message);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}


		return context.getParameter("response") != null ? (Response<RefundDTO>) context.getParameter("response")
				: new Response<RefundDTO>(0, refundDto.getId() + "", null);
	}

	@RequestMapping(value = "/next_flow2", method = RequestMethod.GET)
	@ResponseBody
	public Response<RefundDTO> nextFlow2(HttpServletRequest request,
										HttpServletResponse response) {
		String mail = "1286559059@qq.com";
		String title = "服务订单：" + 123123  + "退款信息：";
		String content = "<h1>This is a Heading</h1>"
				+ "<p>This is a paragraph.</p>"
				+ "<table border='1'>"
				+ "<tr><th>Header 1</th><th>Header 2</th></tr>"
				+ "<tr><td>Row 1, Cell 1</td><td>Row 1, Cell 2</td></tr>"
				+ "<tr><td>Row 2, Cell 1</td><td>Row 2, Cell 2</td></tr>"
				+ "</table>";
		if (StringUtil.isEmpty(mail) || StringUtil.isEmpty(title)) {
			LOG.error("参数错误!");
			return null;
		}
			SendEmailUtil.send(mail, title, content);
		return null;
	}

}
