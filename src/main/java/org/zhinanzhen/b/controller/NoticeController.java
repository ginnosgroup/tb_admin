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
import org.zhinanzhen.tb.service.AdviserService;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.UserService;
import org.zhinanzhen.tb.service.pojo.AdviserDTO;
import org.zhinanzhen.tb.service.pojo.UserDTO;
import org.zhinanzhen.tb.utils.SendEmailUtil;

import lombok.extern.slf4j.Slf4j;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/notice")
@Slf4j
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

	@RequestMapping(value = "/sendEmails", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> sendEmails(HttpServletResponse response) {
		super.setGetHeader(response);
		try {
			String title = "提醒邮件";

			List<ServiceOrderDTO> allServiceOrderList = serviceOrderService.listServiceOrder(null, null, null, null, 0,
					0, 0, 0, 0, false, 0, 1000);
			for (ServiceOrderDTO serviceOrder : allServiceOrderList) {
				int days = getDateDays(serviceOrder.getGmtModify(), new Date());
				try {
					AdviserDTO adviserDto1 = adviserService.getAdviserById(serviceOrder.getAdviserId());
					AdviserDTO adviserDto2 = adviserService.getAdviserById(serviceOrder.getAdviserId2());
					if (days > 2 && "REVIEW".equalsIgnoreCase(serviceOrder.getState())) {
						SendEmailUtil.send(adviserDto1.getEmail(), title, "亲爱的" + adviserDto1.getName() + ":<br/>您的服务订单"
								+ serviceOrder.getId() + " 超过48小时未提交审核，请及时处理，如已操作请忽略。");
						SendEmailUtil.send(adviserDto2.getEmail(), title, "亲爱的" + adviserDto2.getName() + ":<br/>您的服务订单"
								+ serviceOrder.getId() + " 超过48小时未提交审核，请及时处理，如已操作请忽略。");
					}
				} catch (Exception e) {
					log.error("提醒邮件发送异常:" + e.getMessage());
					continue;
				}
			}

			List<VisaDTO> allVisaList = visaService.listVisa(null, null, null, null, null, null, null, null, null, 0, 0,
					0, 1000);
			for (VisaDTO visa : allVisaList) {
				try {
					ServiceOrderDTO serviceOrderDto = serviceOrderService.getServiceOrderById(visa.getServiceOrderId());
					if (serviceOrderDto == null || !"REVIEW".equalsIgnoreCase(serviceOrderDto.getState()))
						continue;
					AdviserDTO adviserDto = adviserService.getAdviserById(visa.getAdviserId());
					UserDTO userDto = userService.getUserById(visa.getUserId());
					Date visaExpirationDate = userDto.getVisaExpirationDate();
					int days = getDateDays(visaExpirationDate, new Date());
					if (days == 0 || days == 1 || days == 2 || days == 3 || days == 7 || days == 15 || days == 30)
						SendEmailUtil.send(adviserDto.getEmail(), title,
								"亲爱的" + adviserDto.getName() + ":<br/>您客户" + userDto.getName() + "，签证日期还有" + days
										+ "天到期，请尽快联系客户，如已重新申请签证为保证下次提醒请更新签证时间。<br/>客户ID:" + visa.getUserId() + "/签证日期:"
										+ visaExpirationDate);
				} catch (Exception e) {
					log.error("提醒邮件发送异常:" + e.getMessage());
					continue;
				}
			}

			List<CommissionOrderListDTO> allCommissionOrderList = commissionOrderService.listCommissionOrder(0, 0, 0, 0,
					null, null, null, null, null, null, null, null, null, null, 0, 1000);
			for (CommissionOrderListDTO commissionOrderListDto : allCommissionOrderList) {
				try {
					ServiceOrderDTO serviceOrderDto = serviceOrderService
							.getServiceOrderById(commissionOrderListDto.getServiceOrderId());
					if (serviceOrderDto == null || !"REVIEW".equalsIgnoreCase(serviceOrderDto.getState()))
						continue;
					AdviserDTO adviserDto = adviserService.getAdviserById(commissionOrderListDto.getAdviserId());
					Date installmentDueDate = commissionOrderListDto.getInstallmentDueDate();
					int days = getDateDays(installmentDueDate, new Date());
					if (days == 0 || days == 1 || days == 2 || days == 3 || days == 7 || days == 15 || days == 30)
						SendEmailUtil.send(adviserDto.getEmail(), title,
								"亲爱的" + adviserDto.getName() + ":<br/>您佣金服务订单"
										+ commissionOrderListDto.getServiceOrderId() + "，installment date 距今还有" + days
										+ "天，请尽快联系学生是否以及提交学费，如已提交请尽快提交月奖申请。");
				} catch (Exception e) {
					log.error("提醒邮件发送异常:" + e.getMessage());
					continue;
				}
			}
			return new Response<Integer>(0, "发送成功.", 1);
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	private int getDateDays(Date date1, Date date2) {
		return (int) ((date2.getTime() - date1.getTime() + 1000000) / (3600 * 24 * 1000));
	}

}
