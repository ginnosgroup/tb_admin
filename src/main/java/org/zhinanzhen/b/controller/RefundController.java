package org.zhinanzhen.b.controller;

import java.io.IOException;
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
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

import com.ikasoa.core.utils.ObjectUtil;
import com.ikasoa.core.utils.StringUtil;
import com.ikasoa.web.workflow.Context;
import com.ikasoa.web.workflow.Node;
import com.ikasoa.web.workflow.Workflow;
import com.ikasoa.web.workflow.WorkflowStarter;
import com.ikasoa.web.workflow.impl.WorkflowStarterImpl;

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
	
	public enum RefundStateEnum {
		PENDING, REVIEW, APPLY, CLOSE;

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
	public Response<List<RefundDTO>> list(@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "state", required = false) String state, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<List<RefundDTO>>(0, refundService.listRefund(type, state));
		} catch (ServiceException e) {
			return new Response<List<RefundDTO>>(1, e.getMessage(), null);
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
	public Response<Integer> update(@RequestParam(value = "id") int id,
			@RequestParam(value = "state", required = false) String state, HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			if (id <= 0)
				return new Response<Integer>(1, "id不正确.", 0);
			RefundDTO refundDto = refundService.getRefundById(id);
			if (state != null)
				refundDto.setState(state);
			if (refundService.updateRefund(refundDto) > 0) {
				return new Response<Integer>(0, refundDto.getId());
			} else {
				return new Response<Integer>(0, "修改失败.", 0);
			}
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/deleteRefundById", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> deleteRefundById(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, refundService.deleteRefundById(id));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), 0);
		}
	}
	
	@RequestMapping(value = "/next_flow", method = RequestMethod.POST)
	@ResponseBody
	public Response<RefundDTO> nextFlow(@RequestParam(value = "id") int id,
			@RequestParam(value = "state") String state,
			@RequestParam(value = "reason", required = false) String reason,
			@RequestParam(value = "paymentVoucherImageUrl", required = false) String paymentVoucherImageUrl,
			HttpServletRequest request, HttpServletResponse response) {
		AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
		if (adminUserLoginInfo == null)
			return new Response<RefundDTO>(1, "请先登录.", null);
		if (id <= 0)
			return new Response<RefundDTO>(1, "id不正确:" + id, null);
		RefundDTO refundDto;
		try {
			refundDto = refundService.getRefundById(id);
			if (ObjectUtil.isNull(refundDto))
				return new Response<RefundDTO>(1, "退款单不存在:" + id, null);
			Node node = rNodeFactory.getNode(refundDto.getState());

			Context context = new Context();
			context.putParameter("serviceOrderId", id);
			context.putParameter("type", refundDto.getType());
			context.putParameter("state", state);
			context.putParameter("reason", reason);
			context.putParameter("paymentVoucherImageUrl", paymentVoucherImageUrl);
			context.putParameter("ap", adminUserLoginInfo.getApList());
			context.putParameter("adminUserId", adminUserLoginInfo.getId());

			LOG.info("Flow API Log : " + context.toString());

			String[] nextNodeNames = node.nextNodeNames();
			if (nextNodeNames != null)
				if (Arrays.asList(nextNodeNames).contains(state))
					node = rNodeFactory.getNode(state);
				else
					return new Response<RefundDTO>(1,
							StringUtil.merge("状态:", state, "不是合法状态. (", Arrays.toString(nextNodeNames), ")"), null);

			Workflow workflow = new Workflow("Refund Work Flow", node, rNodeFactory);

			context = workflowStarter.process(workflow, context);

			return context.getParameter("response") != null ? (Response<RefundDTO>) context.getParameter("response")
					: new Response<RefundDTO>(0, id + "", null);

		} catch (ServiceException e) {
			return new Response<RefundDTO>(1, "异常:" + e.getMessage(), null);
		}
	}

}
