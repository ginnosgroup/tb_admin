package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zhinanzhen.b.controller.BaseCommissionOrderController.ReviewKjStateEnum;
import org.zhinanzhen.b.dao.CommissionOrderDAO;
import org.zhinanzhen.b.dao.ReceiveTypeDAO;
import org.zhinanzhen.b.dao.SchoolDAO;
import org.zhinanzhen.b.dao.ServiceDAO;
import org.zhinanzhen.b.dao.ServiceOrderReviewDAO;
import org.zhinanzhen.b.dao.SubagencyDAO;
import org.zhinanzhen.b.dao.pojo.CommissionOrderDO;
import org.zhinanzhen.b.dao.pojo.CommissionOrderListDO;
import org.zhinanzhen.b.dao.pojo.ReceiveTypeDO;
import org.zhinanzhen.b.dao.pojo.SchoolDO;
import org.zhinanzhen.b.dao.pojo.ServiceDO;
import org.zhinanzhen.b.dao.pojo.ServiceOrderReviewDO;
import org.zhinanzhen.b.dao.pojo.SubagencyDO;
import org.zhinanzhen.b.service.CommissionOrderService;
import org.zhinanzhen.b.service.pojo.CommissionOrderDTO;
import org.zhinanzhen.b.service.pojo.CommissionOrderListDTO;
import org.zhinanzhen.b.service.pojo.SchoolDTO;
import org.zhinanzhen.b.service.pojo.SubagencyDTO;
import org.zhinanzhen.b.service.pojo.ReceiveTypeDTO;
import org.zhinanzhen.b.service.pojo.ServiceDTO;
import org.zhinanzhen.tb.service.pojo.AdviserDTO;
import org.zhinanzhen.tb.service.pojo.UserDTO;
import org.zhinanzhen.tb.dao.AdviserDAO;
import org.zhinanzhen.tb.dao.UserDAO;
import org.zhinanzhen.tb.dao.pojo.AdviserDO;
import org.zhinanzhen.tb.dao.pojo.UserDO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.thrift.ErrorCodeEnum;

@Service("CommissionOrderService")
public class CommissionOrderServiceImpl extends BaseService implements CommissionOrderService {

	@Resource
	private UserDAO userDao;

	@Resource
	private SchoolDAO schoolDao;

	@Resource
	private CommissionOrderDAO commissionOrderDao;

	@Resource
	private ServiceOrderReviewDAO serviceOrderReviewDao;

	@Resource
	private SubagencyDAO subagencyDao;

	@Resource
	private AdviserDAO adviserDao;

	@Resource
	private ReceiveTypeDAO receiveTypeDao;

	@Resource
	private ServiceDAO serviceDao;

	@Override
	@Transactional
	public int addCommissionOrder(CommissionOrderDTO commissionOrderDto) throws ServiceException {
		if (commissionOrderDto == null) {
			ServiceException se = new ServiceException("commissionOrderDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			CommissionOrderDO commissionOrderDo = mapper.map(commissionOrderDto, CommissionOrderDO.class);
			List<ServiceOrderReviewDO> serviceOrderReviews = serviceOrderReviewDao
					.listServiceOrderReview(commissionOrderDo.getServiceOrderId(), null, null, null, null, null);
			if (serviceOrderReviews == null || serviceOrderReviews.size() == 0) {
				ServiceException se = new ServiceException("服务订单需要审核后才能创建佣金订单!");
				se.setCode(ErrorCodeEnum.DATA_ERROR.code());
				throw se;
			}
			if (commissionOrderDao.countCommissionOrderByServiceOrderIdAndExcludeCode(
					commissionOrderDo.getServiceOrderId(), commissionOrderDo.getCode()) > 0) {
				ServiceException se = new ServiceException("已创建过佣金订单,不能重复创建!");
				se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
				throw se;
			}
			if (commissionOrderDao.addCommissionOrder(commissionOrderDo) > 0) {
				commissionOrderDto.setId(commissionOrderDo.getId());
				ServiceOrderReviewDO serviceOrderReviewDo = serviceOrderReviews.get(0);
				serviceOrderReviewDo.setCommissionOrderId(commissionOrderDo.getId());
				serviceOrderReviewDao.addServiceOrderReview(serviceOrderReviewDo);
				return commissionOrderDo.getId();
			} else
				return 0;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int countCommissionOrder(Integer maraId, Integer adviserId, Integer officialId, String name, String phone,
			String wechatUsername, Integer schoolId, Boolean isSettle, List<String> stateList,
			List<String> commissionStateList, Boolean isYzyAndYjy) throws ServiceException {
		return commissionOrderDao.countCommissionOrder(maraId, adviserId, officialId, name, phone, wechatUsername,
				schoolId, isSettle, stateList, commissionStateList, isYzyAndYjy);
	}

	@Override
	public List<CommissionOrderListDTO> listCommissionOrder(Integer maraId, Integer adviserId, Integer officialId,
			String name, String phone, String wechatUsername, Integer schoolId, Boolean isSettle,
			List<String> stateList, List<String> commissionStateList, Boolean isYzyAndYjy, int pageNum, int pageSize)
			throws ServiceException {
		if (pageNum < 0) {
			pageNum = DEFAULT_PAGE_NUM;
		}
		if (pageSize < 0) {
			pageSize = DEFAULT_PAGE_SIZE;
		}
		List<CommissionOrderListDTO> commissionOrderListDtoList = new ArrayList<>();
		List<CommissionOrderListDO> commissionOrderListDoList = new ArrayList<>();
		try {
			commissionOrderListDoList = commissionOrderDao.listCommissionOrder(maraId, adviserId, officialId, name,
					phone, wechatUsername, schoolId, isSettle, stateList, commissionStateList, isYzyAndYjy,
					pageNum * pageSize, pageSize);
			if (commissionOrderListDoList == null)
				return null;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		commissionOrderListDoList.forEach(commissionOrderListDo -> commissionOrderListDtoList
				.add(buildCommissionOrderListDto(commissionOrderListDo)));
		return commissionOrderListDtoList;
	}

	@Override
	@Transactional
	public int updateCommissionOrder(CommissionOrderDTO commissionOrderDto) throws ServiceException {
		if (commissionOrderDto == null) {
			ServiceException se = new ServiceException("commissionOrderDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			CommissionOrderDO commissionOrderDo = mapper.map(commissionOrderDto, CommissionOrderDO.class);
			// 同步修改同一批佣金订单的顾问和文案
			if (commissionOrderDo.getAdviserId() > 0 || commissionOrderDo.getOfficialId() > 0) {
				CommissionOrderListDO commissionOrderListDo = commissionOrderDao
						.getCommissionOrderById(commissionOrderDo.getId());
				if (commissionOrderListDo == null) {
					ServiceException se = new ServiceException("佣金订单ID:" + commissionOrderDo.getId() + ",数据不正确或不存在!");
					se.setCode(ErrorCodeEnum.DATA_ERROR.code());
					throw se;
				}
				for (CommissionOrderDO _commissionOrderDo : commissionOrderDao
						.listCommissionOrderByCode(commissionOrderListDo.getCode())) {
					if (_commissionOrderDo.getId() != commissionOrderDo.getId() && (ReviewKjStateEnum.PENDING.toString()
							.equalsIgnoreCase(_commissionOrderDo.getState())
							|| ReviewKjStateEnum.REVIEW.toString().equalsIgnoreCase(_commissionOrderDo.getState())
							|| ReviewKjStateEnum.WAIT.toString().equalsIgnoreCase(_commissionOrderDo.getState()))) {
						_commissionOrderDo.setAdviserId(commissionOrderDo.getAdviserId());
						_commissionOrderDo.setOfficialId(commissionOrderDo.getOfficialId());
						commissionOrderDao.updateCommissionOrder(_commissionOrderDo);
					}
				}
			}
			return commissionOrderDao.updateCommissionOrder(commissionOrderDo);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public CommissionOrderListDTO getCommissionOrderById(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		CommissionOrderListDTO commissionOrderListDto = null;
		try {
			CommissionOrderListDO commissionOrderListDo = commissionOrderDao.getCommissionOrderById(id);
			if (commissionOrderListDo == null)
				return null;
			commissionOrderListDto = buildCommissionOrderListDto(commissionOrderListDo);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
		return commissionOrderListDto;
	}

	private CommissionOrderListDTO buildCommissionOrderListDto(CommissionOrderListDO commissionOrderListDo) {
		CommissionOrderListDTO commissionOrderListDto = mapper.map(commissionOrderListDo, CommissionOrderListDTO.class);
		if (commissionOrderListDo.getUserId() > 0) {
			UserDO userDo = userDao.getUserById(commissionOrderListDo.getUserId());
			if (userDo != null)
				commissionOrderListDto.setUser(mapper.map(userDo, UserDTO.class));
		}
		if (commissionOrderListDo.getSchoolId() > 0) {
			SchoolDO schoolDo = schoolDao.getSchoolById(commissionOrderListDo.getSchoolId());
			if (schoolDo != null)
				commissionOrderListDto.setSchool(mapper.map(schoolDo, SchoolDTO.class));
		}
		if (commissionOrderListDo.getSubagencyId() > 0) {
			SubagencyDO subagencyDo = subagencyDao.getSubagencyById(commissionOrderListDo.getSubagencyId());
			if (subagencyDo != null)
				commissionOrderListDto.setSubagency(mapper.map(subagencyDo, SubagencyDTO.class));
		}
		if (commissionOrderListDo.getAdviserId() > 0) {
			AdviserDO adviserDo = adviserDao.getAdviserById(commissionOrderListDo.getAdviserId());
			if (adviserDo != null)
				commissionOrderListDto.setAdviser(mapper.map(adviserDo, AdviserDTO.class));
		}
		if (commissionOrderListDo.getReceiveTypeId() > 0) {
			ReceiveTypeDO receiveTypeDo = receiveTypeDao.getReceiveTypeById(commissionOrderListDo.getReceiveTypeId());
			if (receiveTypeDo != null)
				commissionOrderListDto.setReceiveType(mapper.map(receiveTypeDo, ReceiveTypeDTO.class));
		}
		if (commissionOrderListDo.getServiceId() > 0) {
			ServiceDO serviceDo = serviceDao.getServiceById(commissionOrderListDo.getServiceId());
			if (serviceDo != null)
				commissionOrderListDto.setService(mapper.map(serviceDo, ServiceDTO.class));
		}
		double totalPerAmount = 0.00;
		double totalAmount = 0.00;
		List<CommissionOrderDO> list = commissionOrderDao.listCommissionOrderByCode(commissionOrderListDo.getCode());
		if (list != null) {
			for (CommissionOrderDO commissionOrderDo : list) {
				totalPerAmount += commissionOrderDo.getPerAmount();
				totalAmount += commissionOrderDo.getAmount();
			}
			commissionOrderListDto.setTotalPerAmount(totalPerAmount);
			commissionOrderListDto.setTotalAmount(totalAmount);
		}
		return commissionOrderListDto;
	}

}
