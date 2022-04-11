package org.zhinanzhen.b.controller;

import com.ikasoa.core.utils.StringUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.zhinanzhen.b.service.*;
import org.zhinanzhen.b.service.pojo.*;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.AdviserService;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.UserService;
import org.zhinanzhen.tb.service.pojo.AdviserDTO;
import org.zhinanzhen.tb.service.pojo.UserDTO;
import org.zhinanzhen.tb.utils.MD5Util;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * Date: 2021/06/16 下午 3:51
 * Description:
 * Version: V1.0
 */
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/mailRemid")
public class MailRemindController extends BaseController {

    @Resource
    MailRemindService mailRemindService;

    @Resource
    ServiceOrderService serviceOrderService;

    @Resource
    AdviserService adviserService;

    @Resource
    OfficialService officialService;

    @Resource
    KjService kjService;

    @Resource
    CommissionOrderService commissionOrderService;

    @Resource
    VisaService visaService;

    @Resource
    UserService userService;


    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

	@GetMapping(value = "/list")
	public Response<List<MailRemindDTO>> list(
			@RequestParam(value = "isToday", required = false, defaultValue = "false") boolean isToday,
			@RequestParam(value = "isAll", required = false, defaultValue = "true") boolean isAll,
			@RequestParam(value = "serviceOrderId", required = false) Integer serviceOrderId,
			@RequestParam(value = "visaId", required = false) Integer visaId,
			@RequestParam(value = "commissionOrderId", required = false) Integer commissionOrderId,
			@RequestParam(value = "userId", required = false) Integer userId, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			if (getAdminUserLoginInfo(request) == null)
				return new Response(1, "先登录");
			Integer adviserId = getAdviserId(request);
			Integer offcialId = getOfficialId(request);
			Integer kjId = getKjId(request);

			return new Response<List<MailRemindDTO>>(0, "", mailRemindService.list(adviserId, offcialId, kjId,
					serviceOrderId, visaId, commissionOrderId, userId, isToday, isAll));
		} catch (ServiceException e) {
			e.printStackTrace();
			return new Response<List<MailRemindDTO>>(e.getCode(), e.getMessage(), null);
		}
	}

    @PostMapping(value = "/add")
    public Response add(@RequestParam(value = "sendDate") String sendDate,
                        @RequestParam(value = "content")String content,
                        @RequestParam(value = "adviserId" , required = false)Integer adviserId,
                        @RequestParam(value = "offcialId" , required = false)Integer offcialId,
                        @RequestParam(value = "kjId" , required = false)Integer kjId,
                        @RequestParam(value = "serviceOrderId" , required = false)Integer serviceOrderId,
                        @RequestParam(value = "visaId" , required = false)Integer visaId,
                        @RequestParam(value = "commissionOrderId" , required = false)Integer commissionOrderId,
                        @RequestParam(value = "userId" , required = false)Integer userId,
                        HttpServletRequest request, HttpServletResponse response){
        try {
            super.setPostHeader(response);

            if (getAdminUserLoginInfo(request) == null)
                return new Response(1,"先登录");


            MailRemindDTO mailRemindDTO = new MailRemindDTO();
            Integer _adviserId = getAdviserId(request);
            if (_adviserId != null){
                adviserId = _adviserId;
                AdviserDTO adviserDTO = adviserService.getAdviserById(adviserId);
                if (adviserDTO != null){
                    mailRemindDTO.setMail(adviserDTO.getEmail());
                }
            }else
                adviserId = null;


            Integer _offcialId = getOfficialId(request);
            if (_offcialId != null) {
                offcialId = _offcialId;
                OfficialDTO officialDTO = officialService.getOfficialById(offcialId);
                if (officialDTO != null){
                    mailRemindDTO.setMail(officialDTO.getEmail());
                }
            }else
                offcialId = null;

            Integer _kjlId = getKjId(request);
            if (_kjlId != null) {
                kjId = _kjlId;
                KjDTO kjDTO = kjService.getKjById(kjId);
                if (kjDTO != null){
                    mailRemindDTO.setMail(kjDTO.getEmail());
                }
            }else
                kjId = null;

            //kj 可以设置的提醒不关联佣金订单
            if (serviceOrderId == null && visaId == null && commissionOrderId == null && userId == null && kjId == null)
                return new Response(1,"至少传一个:服务,佣金订单,用户id !");

            if (serviceOrderId != null && serviceOrderId > 0) {
                String str = "";
                ServiceOrderDTO serviceOrderDTO = serviceOrderService.getServiceOrderById(serviceOrderId);
                if (serviceOrderDTO != null && serviceOrderDTO.getUser() != null){
                    UserDTO userDTO = serviceOrderDTO.getUser();
                    str = userDTO.getName()+"  ";
                }
                if (serviceOrderDTO != null && serviceOrderDTO.getSchool() != null){
                    SchoolDTO schoolDTO = serviceOrderDTO.getSchool();
                    str = str + "留学-" + schoolDTO.getName() + "  服务订单:"+ serviceOrderDTO.getId() + "今日有一个提醒待处理";
                }
                if (serviceOrderDTO != null && serviceOrderDTO.getService() != null){
                    ServiceDTO serviceDTO = serviceOrderDTO.getService();
                    str = str + "签证-" + serviceDTO.getName() + "  服务订单:"+ serviceOrderDTO.getId() + "今日有一个提醒待处理";
                }
                mailRemindDTO.setTitle(str);
            }else if (visaId != null && visaId > 0){
                VisaDTO visaDTO = visaService.getVisaById(visaId);
                if (visaDTO != null ){
                    mailRemindDTO.setTitle("客户:" + visaDTO.getUserName() + "  签证佣金订单ID:" + visaDTO.getId() + " 今日有一个提醒待处理");
                }
            }else if (commissionOrderId != null && commissionOrderId > 0){
                CommissionOrderDTO commissionOrderDTO = commissionOrderService.getCommissionOrderById(commissionOrderId);
                if (commissionOrderDTO != null){
                    ServiceOrderDTO serviceOrderDTO = serviceOrderService.getServiceOrderById(commissionOrderDTO.getServiceOrderId());
                    if (serviceOrderDTO != null){
                        mailRemindDTO.setTitle("客户:" + serviceOrderDTO.getUser().getName() + "  留学佣金订单ID:" + commissionOrderDTO.getId() + " 今日有一个提醒待处理");
                    }
                }
            }else if (userId != null && userId > 0){
                UserDTO userDTO = userService.getUserById(userId);
                if (userDTO != null)
                    mailRemindDTO.setTitle("客户提醒:客户姓名" + userDTO.getName() + userDTO.getId() + " 今日有一个提醒待处理");
            }else
                mailRemindDTO.setTitle("您有一个新提醒待处理");


            mailRemindDTO.setAdviserId(adviserId);
            mailRemindDTO.setOffcialId(offcialId);
            mailRemindDTO.setKjId(kjId);
            mailRemindDTO.setContent(content);
            mailRemindDTO.setVisaId(visaId);
            mailRemindDTO.setServiceOrderId(serviceOrderId);
            mailRemindDTO.setCommissionOrderId(commissionOrderId);
            mailRemindDTO.setUserId(userId);
            mailRemindDTO.setSendDate(new Date(Long.parseLong(sendDate)));
            try {
                mailRemindDTO.setCode(MD5Util.getMD5(content));
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (mailRemindService.add(mailRemindDTO) > 0)
                return new Response(0, "添加成功");
            else
                return new Response(1, "添加失败");
        } catch (ServiceException e) {
            e.printStackTrace();
            return new Response<Integer>(e.getCode(), e.getMessage(), 0);
        }
    }


    @PostMapping(value = "/delete")
    public Response delete(@RequestParam(value = "id")int id,
                           HttpServletRequest request,HttpServletResponse response){
        if (getAdminUserLoginInfo(request) == null)
            return new Response(1,"先登录");
        Integer adviserId = getAdviserId(request);
        Integer offcialId = getOfficialId(request);
        if (mailRemindService.delete(id,adviserId,offcialId) > 0)
            return new Response(0,"成功");
        else
            return new Response(1,"失败");
    }

    @PostMapping(value = "/update")
    public Response update(@RequestParam(value = "id") int id,
                           @RequestParam(value = "sendDate",required = false) String sendDate,
                           @RequestParam(value = "content",required = false) String content,
                           HttpServletRequest request, HttpServletResponse response) {
        try {
            super.setPostHeader(response);
            if (getAdminUserLoginInfo(request) == null)
                return new Response(1,"先登录");
            MailRemindDTO mailRemindDTO = mailRemindService.getByid(id);
            if (mailRemindDTO == null)
                return new Response(1, "提醒不存在,修改失败");
            if (StringUtil.isNotEmpty(sendDate)) {
                mailRemindDTO.setSendDate(new Date(Long.parseLong(sendDate)));
            }

            mailRemindDTO.setContent(content);

            if (mailRemindService.update(mailRemindDTO) > 0)
                return new Response(0, "修改成功");
            else
                return new Response(1, "修改失败");
        } catch (ServiceException e) {
            e.printStackTrace();
            return new Response(e.getCode(),e.getMessage());
        }
    }

}
