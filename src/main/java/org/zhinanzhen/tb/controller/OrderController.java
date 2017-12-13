package org.zhinanzhen.tb.controller;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zhinanzhen.tb.service.AdviserService;
import org.zhinanzhen.tb.service.AdviserStateEnum;
import org.zhinanzhen.tb.service.ListOrderEnum;
import org.zhinanzhen.tb.service.OrderService;
import org.zhinanzhen.tb.service.OrderStateEnum;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.UserService;
import org.zhinanzhen.tb.service.pojo.AdviserDTO;
import org.zhinanzhen.tb.service.pojo.OrderDTO;
import org.zhinanzhen.tb.service.pojo.UserDTO;
import org.zhinanzhen.tb.utils.MailUtil;

import com.ikasoa.core.utils.StringUtil;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/order")
public class OrderController extends BaseController {
    private static final Logger LOG = LoggerFactory.getLogger(OrderController.class);
    @Resource
    OrderService orderService;
    @Resource
    UserService userService;
    @Resource
    AdviserService adviserService;

    @RequestMapping(value = "/count", method = RequestMethod.GET)
    @ResponseBody
    public Response<Integer> countOrder(@RequestParam(value = "id", required = false) Integer id,
	    @RequestParam(value = "name", required = false) String name,
	    @RequestParam(value = "regionId", required = false) Integer regionId,
	    @RequestParam(value = "state", required = false) String state,
	    @RequestParam(value = "userName", required = false) String userName,
	    @RequestParam(value = "userPhone", required = false) String userPhone, HttpServletResponse response) {
	try {
	    super.setGetHeader(response);
	    OrderStateEnum stateEnum = null;
	    if (StringUtil.isNotEmpty(state)) {
		stateEnum = OrderStateEnum.get(state);
		if (stateEnum == null) {
		    return new Response<Integer>(2, "状态参数错误.", null);
		}
	    }
	    return new Response<Integer>(0,
		    orderService.countOrder(id, name, regionId, stateEnum, userName, userPhone));
	} catch (ServiceException e) {
	    return new Response<Integer>(1, e.getMessage(), null);
	}
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public Response<List<OrderDTO>> listOrder(@RequestParam(value = "id", required = false) Integer id,
	    @RequestParam(value = "name", required = false) String name,
	    @RequestParam(value = "regionId", required = false) Integer regionId,
	    @RequestParam(value = "state", required = false) String state,
	    @RequestParam(value = "userName", required = false) String userName,
	    @RequestParam(value = "userPhone", required = false) String userPhone,
	    @RequestParam(value = "pageNum") int pageNum, @RequestParam(value = "pageSize") int pageSize,
	    HttpServletResponse response) throws ServiceException {

	super.setGetHeader(response);
	OrderStateEnum stateEnum = null;
	if (StringUtil.isNotEmpty(state)) {
	    stateEnum = OrderStateEnum.get(state);
	    if (stateEnum == null) {
		return new Response<List<OrderDTO>>(2, "状态参数错误.", null);
	    }
	}
	return new Response<List<OrderDTO>>(0,
		orderService.listOrder(id, name, regionId, stateEnum, userName, userPhone, pageNum, pageSize));

    }

    @RequestMapping(value = "/listByUser", method = RequestMethod.GET)
    @ResponseBody
    public Response<List<OrderDTO>> listOrderByUser(@RequestParam(value = "userId") int userId,
	    @RequestParam(value = "classify") String classify, HttpServletResponse response) throws ServiceException {
	super.setGetHeader(response);
	if (ListOrderEnum.get(classify) == null) {
	    return new Response<List<OrderDTO>>(2, "状态参数错误.", null);
	}
	return new Response<List<OrderDTO>>(0, orderService.listOrder(userId, classify));
    }

    @RequestMapping(value = "/get", method = RequestMethod.GET)
    @ResponseBody
    public Response<OrderDTO> getOrder(@RequestParam(value = "id") int id, HttpServletResponse response) {
	try {
	    super.setGetHeader(response);
	    return new Response<OrderDTO>(0, orderService.getOrderById(id));
	} catch (ServiceException e) {
	    return new Response<OrderDTO>(1, e.getMessage(), null);
	}
    }

    @RequestMapping(value = "/allocating_adviser", method = RequestMethod.GET)
    @ResponseBody
    public Response<Boolean> allocatingAdviser(@RequestParam(value = "id") int id,
	    @RequestParam(value = "adviserId") int adviserId, HttpServletResponse response) {
	try {
	    super.setGetHeader(response);
	    OrderDTO oldOrderDto = orderService.getOrderById(id);
	    int olsAdviserId = oldOrderDto.getAdviserId();
	    AdviserDTO adviserDto = adviserService.getAdviserById(adviserId);
	    if (AdviserStateEnum.DISABLED.toString().equals(adviserDto.getState().toString())) {
		return new Response<Boolean>(2, "该顾问已被禁用.", null);
	    }
	    if (orderService.allocatingAdviser(id, adviserId)) {
		if (!sendAllocatingAdviserMail(id, adviserDto, olsAdviserId)) {
		    LOG.warn("send mail error !");
		}
		return new Response<Boolean>(0, true);
	    } else {
		return new Response<Boolean>(0, false);
	    }
	} catch (ServiceException e) {
	    return new Response<Boolean>(1, e.getMessage(), null);
	}
    }

    @RequestMapping(value = "/pay_finish_sendmail", method = RequestMethod.GET)
    @ResponseBody
    public Response<Boolean> sendPayFinishMail(@RequestParam(value = "id") int id, HttpServletResponse response) {
	try {
	    super.setGetHeader(response);
	    OrderDTO orderDto = orderService.getOrderById(id);
	    if (orderDto == null) {
		return new Response<Boolean>(2, "没有找到订单.", null);
	    }
	    int userId = orderDto.getUserId();
	    UserDTO userDto = userService.getUserById(userId);
	    if (userDto == null) {
		return new Response<Boolean>(2, "没有找到用户.", null);
	    }
	    int adviserId = orderDto.getAdviserId();
	    if (adviserId <= 0) {
		return new Response<Boolean>(3, "顾问编号为空.", null);
	    }
	    AdviserDTO adviserDto = adviserService.getAdviserById(adviserId);
	    if (adviserDto == null) {
		return new Response<Boolean>(3, "顾问不存在.", null);
	    }
	    if (AdviserStateEnum.DISABLED.toString().equals(adviserDto.getState().toString())) {
		return new Response<Boolean>(4, "该顾问已被禁用.", null);
	    }
	    return new Response<Boolean>(0, MailUtil.sendMail(adviserDto.getEmail(),
		    getPayFinishMailSubject(orderDto, userDto), getPayFinishMailContent(orderDto, userDto)));
	} catch (ServiceException e) {
	    return new Response<Boolean>(1, e.getMessage(), null);
	}
    }

    @RequestMapping(value = "/remainPay", method = RequestMethod.GET)
    @ResponseBody
    public Response<Boolean> remainPay(int orderId, double remainPayAmount, double remainPayBalance,long remainPayDateStamp) throws ServiceException {
	if (orderId <= 0) {
	    return new Response<Boolean>(2,"订单标号错误,orderId="+orderId,null);
	}
	if (remainPayAmount < 0) {
	    return new Response<Boolean>(2,"实际付款不能小于0",null);
	}
	if (remainPayBalance < 0) {
	    return new Response<Boolean>(2,"余额抵扣不能小于0",null);
	}
	Date remainPayDate = new Date(remainPayDateStamp);
	return new Response<Boolean>(0,orderService.endingMoney(orderId, remainPayAmount, remainPayBalance, remainPayDate));
    }

    @RequestMapping(value = "/subject_end_sendmail", method = RequestMethod.GET)
    @ResponseBody
    public Response<Boolean> sendSubjectEndMail(@RequestParam(value = "id") int id, HttpServletResponse response) {
	try {
	    super.setGetHeader(response);
	    OrderDTO orderDto = orderService.getOrderById(id);
	    if (orderDto == null) {
		return new Response<Boolean>(2, "没有找到订单.", null);
	    }
	    int userId = orderDto.getUserId();
	    UserDTO userDto = userService.getUserById(userId);
	    if (userDto == null) {
		return new Response<Boolean>(2, "没有找到用户.", null);
	    }
	    int adviserId = orderDto.getAdviserId();
	    if (adviserId <= 0) {
		return new Response<Boolean>(3, "顾问编号为空.", null);
	    }
	    AdviserDTO adviserDto = adviserService.getAdviserById(adviserId);
	    if (adviserDto == null) {
		return new Response<Boolean>(3, "顾问不存在.", null);
	    }
	    if (AdviserStateEnum.DISABLED.toString().equals(adviserDto.getState().toString())) {
		return new Response<Boolean>(4, "该顾问已被禁用.", null);
	    }
	    return new Response<Boolean>(0,
		    MailUtil.sendMail(userDto.getEmail(), getSubjectEndMailSubjectToUser(orderDto, userDto),
			    getSubjectEndMailContentToUser(orderDto, userDto, adviserDto))
			    && MailUtil.sendMail(adviserDto.getEmail(), getSubjectEndMailSubject(orderDto, userDto),
				    getSubjectEndMailContent(orderDto, userDto)));
	} catch (ServiceException e) {
	    return new Response<Boolean>(1, e.getMessage(), null);
	}
    }

    // 发送分配顾问邮件
    private boolean sendAllocatingAdviserMail(int id, AdviserDTO adviserDto, int olsAdviserId) throws ServiceException {
	OrderDTO orderDto = orderService.getOrderById(id);
	int userId = orderDto.getUserId();
	UserDTO userDto = userService.getUserById(userId);
	AdviserDTO oldAdviserDto = adviserService.getAdviserById(olsAdviserId);
	return MailUtil.sendMail(adviserDto.getEmail(),
		getAllocatingAdviserMailSubject(orderDto, userDto, oldAdviserDto, adviserDto),
		getAllocatingAdviserMailContent(orderDto, userDto));
    }

    // 分配顾问邮件title
    private String getAllocatingAdviserMailSubject(OrderDTO orderDto, UserDTO userDto, AdviserDTO oldAdviserDto,
	    AdviserDTO adviserDto) {
	return orderDto.getId() + "_" + userDto.getName() + "_课程" + orderDto.getName() + "由【" + oldAdviserDto.getName()
		+ "】改为【" + adviserDto.getName() + "】负责";
    }

    // 分配顾问邮件content
    private String getAllocatingAdviserMailContent(OrderDTO orderDto, UserDTO userDto) {
	return "【" + userDto.getName() + "】客户购买了【" + orderDto.getName() + "】课程。<br/>已支付金额：【" + orderDto.getPayAmount()
		+ "】，支付方式：【" + orderDto.getPayType().getValue() + "】，下团单价：【" + orderDto.getCreatePrice() + "】，成团单价：【"
		+ orderDto.getFinishPrice() + "】；<br/>尾款金额：【" + (orderDto.getFinishPrice() - orderDto.getPayAmount())
		+ "】，待支付剩余尾款：【" + (orderDto.getFinishPrice() - orderDto.getPayAmount() - userDto.getBalance())
		+ "】，订单状态：【" + orderDto.getState().getValue() + "】。<br/><br/>客户信息<br/>电话号码：【" + userDto.getPhone()
		+ "】<br/>" + userDto.getAuthType().getValue() + "号：【" + userDto.getAuthUsername() + "】<br/>邮箱：【"
		+ userDto.getEmail() + "】";
    }

    // 订单支付完成邮件title
    private String getPayFinishMailSubject(OrderDTO orderDto, UserDTO userDto) {
	return orderDto.getId() + "_" + userDto.getName() + "__" + orderDto.getName() + "，客户已交预付款，请随时关注拼团动态";
    }

    // 订单支付完成邮件content
    private String getPayFinishMailContent(OrderDTO orderDto, UserDTO userDto) {
	return "【" + userDto.getName() + "】客户购买了【" + orderDto.getName() + "】课程。<br/>已支付金额：【" + orderDto.getPayAmount()
		+ "】，支付方式：【" + orderDto.getPayType().getValue() + "】；下团单价：【" + orderDto.getCreatePrice() + "】，订单状态：【"
		+ orderDto.getState().getValue() + "】。<br/><br/>客户信息<br/>电话号码：【" + userDto.getPhone() + "】<br/>"
		+ userDto.getAuthType().getValue() + "号：【" + userDto.getAuthUsername() + "】<br/>邮箱：【"
		+ userDto.getEmail() + "】";
    }

    // 课程结束邮件title(顾客)
    private String getSubjectEndMailSubjectToUser(OrderDTO orderDto, UserDTO userDto) {
	return orderDto.getId() + "_" + userDto.getName() + "__" + orderDto.getName() + "已结束，请及时联系顾问交尾款";
    }

    // 课程结束邮件content(顾客)
    private String getSubjectEndMailContentToUser(OrderDTO orderDto, UserDTO userDto, AdviserDTO adviserDto) {
	return "【" + userDto.getName() + "】客户购买了【" + orderDto.getName() + "】课程。<br/>已支付金额：【" + orderDto.getPayAmount()
		+ "】，支付方式：【" + orderDto.getPayType().getValue() + "】；下团单价：【" + orderDto.getCreatePrice() + "】，成团单价：【"
		+ orderDto.getFinishPrice() + "】；<br/>尾款金额：【" + (orderDto.getFinishPrice() - orderDto.getPayAmount())
		+ "】，待支付剩余尾款：【" + (orderDto.getFinishPrice() - orderDto.getPayAmount() - userDto.getBalance())
		+ "】，订单状态：【" + orderDto.getState().getValue() + "】。<br/><br/>顾问信息<br/>姓名：【" + adviserDto.getName()
		+ "】<br/>邮箱：【" + adviserDto.getEmail() + "】";
    }

    // 课程结束邮件title(顾问)
    private String getSubjectEndMailSubject(OrderDTO orderDto, UserDTO userDto) {
	return orderDto.getId() + "_" + userDto.getName() + "__" + orderDto.getName() + "已结束，请及时联系客户交尾款";
    }

    // 课程结束邮件content(顾问)
    private String getSubjectEndMailContent(OrderDTO orderDto, UserDTO userDto) {
	return "【" + userDto.getName() + "】客户购买了【" + orderDto.getName() + "】课程。<br/>已支付金额：【" + orderDto.getPayAmount()
		+ "】，支付方式：【" + orderDto.getPayType().getValue() + "】；下团单价：【" + orderDto.getCreatePrice() + "】，成团单价：【"
		+ orderDto.getFinishPrice() + "】；<br/>尾款金额：【" + (orderDto.getFinishPrice() - orderDto.getPayAmount())
		+ "】，待支付剩余尾款：【" + (orderDto.getFinishPrice() - orderDto.getPayAmount() - userDto.getBalance())
		+ "】，订单状态：【" + orderDto.getState().getValue() + "】。<br/><br/>客户信息<br/>电话号码：【" + userDto.getPhone()
		+ "】<br/>" + userDto.getAuthType().getValue() + "号：【" + userDto.getAuthUsername() + "】<br/>邮箱：【"
		+ userDto.getEmail() + "】";
    }

}
