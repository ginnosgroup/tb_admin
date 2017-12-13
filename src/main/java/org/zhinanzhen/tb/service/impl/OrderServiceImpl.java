package org.zhinanzhen.tb.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zhinanzhen.tb.dao.AdviserDAO;
import org.zhinanzhen.tb.dao.OrderDAO;
import org.zhinanzhen.tb.dao.PayLogDAO;
import org.zhinanzhen.tb.dao.RegionDAO;
import org.zhinanzhen.tb.dao.UserDAO;
import org.zhinanzhen.tb.dao.pojo.AdviserDO;
import org.zhinanzhen.tb.dao.pojo.OrderDO;
import org.zhinanzhen.tb.dao.pojo.PayLogDO;
import org.zhinanzhen.tb.dao.pojo.RegionDO;
import org.zhinanzhen.tb.dao.pojo.UserDO;
import org.zhinanzhen.tb.service.ListOrderEnum;
import org.zhinanzhen.tb.service.OrderPayTypeEnum;
import org.zhinanzhen.tb.service.OrderService;
import org.zhinanzhen.tb.service.OrderStateEnum;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.SubjectService;
import org.zhinanzhen.tb.service.pojo.OrderDTO;
import org.zhinanzhen.tb.service.pojo.SubjectDTO;

import com.ikasoa.core.thrift.ErrorCodeEnum;
import com.ikasoa.core.utils.StringUtil;

@Service("orderService")
public class OrderServiceImpl extends BaseService implements OrderService {
    @Resource
    private OrderDAO orderDAO;
    @Resource
    private SubjectService subjectService;
    @Resource
    private RegionDAO regionDAO;
    @Resource
    private UserDAO userDAO;
    @Resource
    AdviserDAO adviserDAO;
    @Resource
    PayLogDAO payLogDAO;

    @Override
    public int countOrder(Integer id, String name, Integer regionId, OrderStateEnum state, String userName,
	    String userPhone) throws ServiceException {
	if (state == null) {
	    return orderDAO.countOrder(id, name, regionId, null, userName, userPhone);
	} else {
	    return orderDAO.countOrder(id, name, regionId, state.toString(), userName, userPhone);
	}
    }

    @Override
    public List<OrderDTO> listOrder(Integer id, String name, Integer regionId, OrderStateEnum state, String userName,
	    String userPhone, int pageNum, int pageSize) throws ServiceException {
	if (pageNum < 0) {
	    pageNum = DEFAULT_PAGE_NUM;
	}
	if (pageSize < 0) {
	    pageSize = DEFAULT_PAGE_SIZE;
	}
	List<OrderDTO> orderDtoList = new ArrayList<OrderDTO>();
	List<OrderDO> orderDoList = new ArrayList<OrderDO>();
	try {
	    if (state == null) {
		orderDoList = orderDAO.listOrder(id, name, regionId, null, userName, userPhone, pageNum * pageSize,
			pageSize);
	    } else {
		orderDoList = orderDAO.listOrder(id, name, regionId, state.toString(), userName, userPhone,
			pageNum * pageSize, pageSize);
	    }
	    if (orderDoList == null) {
		return null;
	    }
	} catch (Exception e) {
	    ServiceException se = new ServiceException(e);
	    se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
	    throw se;
	}
	for (OrderDO orderDo : orderDoList) {
	    OrderDTO orderDto = mapper.map(orderDo, OrderDTO.class);
	    if (orderDto.getUserId() > 0) {
		UserDO userDo = userDAO.getUserById(orderDto.getUserId());
		orderDto.setUserDo(userDo);
	    }
	    if (orderDto.getIntroducerUserId() > 0) {
		UserDO introducer = userDAO.getUserById(orderDto.getIntroducerUserId());
		orderDto.setUserDo(introducer);
	    }
	    if (orderDto.getRegionId() > 0) {
		RegionDO regionDo = regionDAO.getRegionById(orderDto.getRegionId());
		orderDto.setRegionDo(regionDo);
	    }
	    if (orderDto.getSubjectId() > 0) {
		SubjectDTO subjectDto = subjectService.getSubjectById(orderDto.getSubjectId());
		orderDto.setSubjectDto(subjectDto);
	    }
	    if (orderDto.getRegionId() > 0) {
		RegionDO regionDo = regionDAO.getRegionById(orderDto.getRegionId());
		orderDto.setRegionDo(regionDo);
	    }
	    if (orderDto.getAdviserId() > 0) {
		AdviserDO adviserDo = adviserDAO.getAdviserById(orderDto.getAdviserId());
		orderDto.setAdviserDo(adviserDo);
	    }
	    double finalPayAmount = 0;
	    if (orderDto.getFinishPrice() > 0 && OrderStateEnum.SUCCESS.equals(orderDto.getState())) {
		finalPayAmount = new BigDecimal(orderDto.getFinishPrice() - orderDto.getSubjectDto().getPreAmount())
			.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	    }
	    orderDto.setFinalPayAmount(finalPayAmount);
	    orderDtoList.add(orderDto);
	}
	return orderDtoList;
    }

    @Override
    public OrderDTO getOrderById(int id) throws ServiceException {
	if (id <= 0) {
	    ServiceException se = new ServiceException("id error !");
	    se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
	    throw se;
	}
	OrderDTO orderDto = null;
	try {
	    OrderDO orderDo = orderDAO.getOrderById(id);
	    if (orderDo == null) {
		ServiceException se = new ServiceException("the order is't exist .");
		se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
		throw se;
	    }
	    orderDto = mapper.map(orderDo, OrderDTO.class);
	    if (orderDto.getUserId() > 0) {
		UserDO userDo = userDAO.getUserById(orderDto.getUserId());
		orderDto.setUserDo(userDo);
	    }
	    if (orderDto.getIntroducerUserId() > 0) {
		UserDO introducer = userDAO.getUserById(orderDto.getIntroducerUserId());
		orderDto.setIntroducerDo(introducer);
	    }
	    if (orderDto.getRegionId() > 0) {
		RegionDO regionDo = regionDAO.getRegionById(orderDto.getRegionId());
		orderDto.setRegionDo(regionDo);
	    }
	    if (orderDto.getSubjectId() > 0) {
		SubjectDTO subjectDto = subjectService.getSubjectById(orderDto.getSubjectId());
		orderDto.setSubjectDto(subjectDto);
	    }
	    if (orderDto.getAdviserId() > 0) {
		AdviserDO adviserDo = adviserDAO.getAdviserById(orderDto.getAdviserId());
		orderDto.setAdviserDo(adviserDo);
	    }
	    double finalPayAmount = 0;
	    if (orderDto.getFinishPrice() > 0 && OrderStateEnum.SUCCESS.equals(orderDto.getState())) {
		finalPayAmount = new BigDecimal(orderDto.getFinishPrice() - orderDto.getSubjectDto().getPreAmount())
			.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	    }
	    orderDto.setFinalPayAmount(finalPayAmount);
	} catch (Exception e) {
	    ServiceException se = new ServiceException(e);
	    se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
	    throw se;
	}
	return orderDto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean allocatingAdviser(int id, int adviserId) throws ServiceException {
	if (id <= 0) {
	    ServiceException se = new ServiceException("id error !");
	    se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
	    throw se;
	}
	if (adviserId <= 0) {
	    ServiceException se = new ServiceException("adviserId error !");
	    se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
	    throw se;
	}
	return orderDAO.updateAdviserIdById(id, adviserId);
    }

    @Override
    public List<OrderDTO> listOrder(int userId, String classify) throws ServiceException {
	if (userId <= 0) {
	    ServiceException se = new ServiceException("userId error !userId = " + userId);
	    se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
	    throw se;
	}
	if (StringUtil.isEmpty(classify) || ListOrderEnum.get(classify) == null) {
	    ServiceException se = new ServiceException("classify error !classify = " + classify);
	    se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
	    throw se;
	}
	List<OrderDO> orderDoList = null;
	List<OrderDTO> orderDtoList = new ArrayList<OrderDTO>();
	if (ListOrderEnum.USER.toString().equals(classify)) {
	    orderDoList = orderDAO.listOrderByUserId(userId);
	}
	if (ListOrderEnum.RECOMMEND.toString().equals(classify)) {
	    orderDoList = orderDAO.listOrderByIntroducerId(userId);
	}
	for (OrderDO orderDo : orderDoList) {
	    OrderDTO orderDto = mapper.map(orderDo, OrderDTO.class);
	    if (orderDto.getUserId() > 0) {
		UserDO userDo = userDAO.getUserById(orderDto.getUserId());
		orderDto.setUserDo(userDo);
	    }
	    if (orderDto.getIntroducerUserId() > 0) {
		UserDO introducer = userDAO.getUserById(orderDto.getIntroducerUserId());
		orderDto.setIntroducerDo(introducer);
	    }
	    if (orderDto.getSubjectId() > 0) {
		SubjectDTO subjectDto = subjectService.getSubjectById(orderDto.getSubjectId());
		orderDto.setSubjectDto(subjectDto);
	    }
	    if (orderDto.getRegionId() > 0) {
		RegionDO regionDo = regionDAO.getRegionById(orderDto.getRegionId());
		orderDto.setRegionDo(regionDo);
	    }
	    if (orderDto.getAdviserId() > 0) {
		AdviserDO adviserDo = adviserDAO.getAdviserById(orderDto.getAdviserId());
		orderDto.setAdviserDo(adviserDo);
	    }
	    double finalPayAmount = 0;
	    if (orderDto.getFinishPrice() > 0 && OrderStateEnum.SUCCESS.toString().equals(orderDto.getState())) {
		finalPayAmount = new BigDecimal(orderDto.getFinishPrice() - orderDto.getSubjectDto().getPreAmount())
			.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	    }
	    orderDto.setFinalPayAmount(finalPayAmount);
	    orderDtoList.add(orderDto);
	}
	return orderDtoList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean endingMoney(int orderId, double remainPayAmount, double remainPayBalance, Date remainPayDate)
	    throws ServiceException {
	if (orderId < 0) {
	    ServiceException se = new ServiceException("orderId error !orderId = " + orderId);
	    se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
	    throw se;
	}
	if (remainPayAmount < 0) {
	    ServiceException se = new ServiceException("remainPayAmount error !remainPayAmount = " + remainPayAmount);
	    se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
	    throw se;
	}
	if (remainPayBalance < 0) {
	    ServiceException se = new ServiceException(
		    "remainPayBalance error !remainPayBalance = " + remainPayBalance);
	    se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
	    throw se;
	}
	OrderDTO orderDto = getOrderById(orderId);
	if (!OrderStateEnum.SUCCESS.equals(orderDto.getState())) {
	    ServiceException se = new ServiceException(
		    "order's state error !orderId=" + orderId + ",orderState = " + orderDto.getState().toString());
	    se.setCode(ErrorCodeEnum.DATA_ERROR.code());
	    throw se;
	}
	UserDO userDo = orderDto.getUserDo();
	int userId = userDo.getId();
	double finalPayAmount = orderDto.getFinalPayAmount();
	double userBalance = userDo.getBalance();
	double oldRemainPayAmount = orderDto.getRemainPayAmount();
	double oldRemainPayBalance = orderDto.getRemainPayBalance();
	if (remainPayBalance > userBalance) {
	    ServiceException se = new ServiceException("user's balance > remainPayBalance , user's=" + userBalance
		    + ",remainPayBalance = " + remainPayBalance);
	    se.setCode(ErrorCodeEnum.DATA_ERROR.code());
	    throw se;
	}
	double remainTotal = new BigDecimal(remainPayAmount + remainPayBalance).setScale(2, BigDecimal.ROUND_HALF_UP)
		.doubleValue();
	double waitFinalPay = new BigDecimal(finalPayAmount - oldRemainPayAmount - oldRemainPayBalance)
		.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	if (waitFinalPay - remainTotal < 0) {
	    ServiceException se = new ServiceException("waitFinalPay < remainTotal !waitFinalPay=" + waitFinalPay
		    + ",remainPayAmount=" + remainPayAmount + ",remainPayBalance=" + remainPayBalance);
	    se.setCode(ErrorCodeEnum.DATA_ERROR.code());
	    throw se;
	}
	// 更新订单
	double remainPayAmountResult = remainPayAmount+oldRemainPayAmount;
	double remainPayBalanceResult = remainPayBalance+oldRemainPayBalance;
	if (!orderDAO.updateRemainPay(orderId, remainPayAmountResult, remainPayDate, remainPayBalanceResult)) {
	    ServiceException se = new ServiceException("updateRemainPay fail ! orderId = " + orderId);
	    se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
	    throw se;
	}
	double balanceResult = new BigDecimal(userBalance - remainPayBalance).setScale(2, BigDecimal.ROUND_HALF_UP)
		.doubleValue();
	// 更新余额
	if (remainPayBalance > 0 && !userDAO.updateBalanceById(userId, balanceResult)) {
	    ServiceException se = new ServiceException("updateBalanceById fail ! orderId = " + orderId);
	    se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
	    throw se;
	}
	// 添加日至 1.余额抵扣日志 2.最终支付日志
	PayLogDO payLogDo = new PayLogDO();
	payLogDo.setOrderId(orderId);
	payLogDo.setUserId(userId);
	payLogDo.setPayDate(remainPayDate);
	if (remainPayBalance > 0) {
	    payLogDo.setPayType(OrderPayTypeEnum.OFFSET.toString());
	    payLogDo.setPayCode(remainPayBalance + "");
	    payLogDo.setPayAmount(new BigDecimal(remainPayBalance));
	    if (payLogDAO.insert(payLogDo) != 1) {
		ServiceException se = new ServiceException("insert remainBalancePayLog fail ! orderId = " + orderId);
		se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
		throw se;
	    }
	}
	if (remainPayAmount > 0) {
	    payLogDo.setPayType(OrderPayTypeEnum.REMAIN.toString());
	    payLogDo.setPayCode(remainPayAmount + "");
	    payLogDo.setPayAmount(new BigDecimal(remainPayAmount));
	    if (payLogDAO.insert(payLogDo) != 1) {
		ServiceException se = new ServiceException("insert remainPayLog fail ! orderId = " + orderId);
		se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
		throw se;
	    }
	}
	return true;
    }
}
