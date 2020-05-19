package org.zhinanzhen.b.controller;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zhinanzhen.b.service.CommissionOrderService;
import org.zhinanzhen.b.service.ServiceOrderService;
import org.zhinanzhen.b.service.VisaService;
import org.zhinanzhen.b.service.pojo.CommissionOrderListDTO;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.b.service.pojo.VisaDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.dao.pojo.AdviserDO;
import org.zhinanzhen.tb.service.AdviserService;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.UserService;
import org.zhinanzhen.tb.service.pojo.AdviserDTO;
import org.zhinanzhen.tb.service.pojo.UserDTO;

import com.ikasoa.web.utils.SimpleSendEmailTool;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/notice")
public class NoticeController extends BaseController {

	@Resource
	ServiceOrderService serviceOrderService;

	@Resource
	VisaService visaService;

	@Resource
	CommissionOrderService commissionOrderService;

	@Resource
	UserService userService;

	@Resource
	AdviserService adviserService;

	private SimpleSendEmailTool simpleSendEmailTool = new SimpleSendEmailTool("notice@zhinanzhen.org", "Znz@2020",
			SimpleSendEmailTool.SmtpServerEnum.EXMAIL_QQ);

	@RequestMapping(value = "/sendEmails", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> sendEmails(HttpServletResponse response) {
		super.setGetHeader(response);
		try {
			String title = "提醒邮件";

			List<ServiceOrderDTO> allServiceOrderList = serviceOrderService.listServiceOrder(null, null, null, null, 0,
					0, 0, 0, 0, 1000);
			for (ServiceOrderDTO serviceOrder : allServiceOrderList) {
			}

			List<VisaDTO> allVisaList = visaService.listVisa(null, null, null, null, null, null, null, 0, 0, 0, 1000);
			for (VisaDTO visa : allVisaList) {
				AdviserDTO adviserDto = adviserService.getAdviserById(visa.getAdviserId());
				UserDTO userDto = userService.getUserById(visa.getUserId());
				Date visaExpirationDate = userDto.getVisaExpirationDate();
				// TODO: sulei
				simpleSendEmailTool.send(adviserDto.getEmail(), title,
						"亲爱的" + adviserDto.getName() + ":<br/>您客户" + userDto.getName() + "，签证日期还有" + 30
								+ "天到期，请尽快联系客户，如已重新申请签证为保证下次提醒请更新签证时间。<br/>客户ID:" + visa.getUserId() + "/签证日期:"
								+ visaExpirationDate);
			}

			List<CommissionOrderListDTO> allCommissionOrderList = commissionOrderService.listCommissionOrder(0, 0, 0,
					null, null, null, null, null, null, null, null, 0, 1000);
			for (CommissionOrderListDTO commissionOrderListDto : allCommissionOrderList) {
				AdviserDTO adviserDto = adviserService.getAdviserById(commissionOrderListDto.getAdviserId());
				Date installmentDueDate = commissionOrderListDto.getInstallmentDueDate();
				// TODO: sulei
				simpleSendEmailTool.send(adviserDto.getEmail(), title,
						"亲爱的" + adviserDto.getName() + ":<br/>您佣金服务订单" + commissionOrderListDto.getServiceOrderId()
								+ "，installment date 距今还有" + 30 + "天，请尽快联系学生是否以及提交学费，如已提交请尽快提交月奖申请。");
			}

			return new Response<Integer>(0, "发送成功.", 1);
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

}
