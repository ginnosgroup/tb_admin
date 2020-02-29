package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zhinanzhen.b.dao.CommissionOrderDAO;
import org.zhinanzhen.b.dao.SchoolDAO;
import org.zhinanzhen.b.dao.ServiceOrderReviewDAO;
import org.zhinanzhen.b.dao.pojo.CommissionOrderDO;
import org.zhinanzhen.b.dao.pojo.CommissionOrderListDO;
import org.zhinanzhen.b.dao.pojo.SchoolDO;
import org.zhinanzhen.b.dao.pojo.ServiceOrderReviewDO;
import org.zhinanzhen.b.service.CommissionOrderService;
import org.zhinanzhen.b.service.pojo.CommissionOrderDTO;
import org.zhinanzhen.b.service.pojo.CommissionOrderListDTO;
import org.zhinanzhen.b.service.pojo.SchoolDTO;
import org.zhinanzhen.tb.service.pojo.UserDTO;
import org.zhinanzhen.tb.dao.UserDAO;
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
					.listServiceOrderReview(commissionOrderDo.getServiceOrderId(), null, null, null, null, "OVST");
			if (serviceOrderReviews == null || serviceOrderReviews.size() == 0) {
				ServiceException se = new ServiceException("服务订单需要审核后才能创建佣金订单!");
				se.setCode(ErrorCodeEnum.DATA_ERROR.code());
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
			String wechatUsername, Integer schoolId, Boolean isSettle, String state) throws ServiceException {
		return commissionOrderDao.countCommissionOrder(maraId, adviserId, officialId, name, phone, wechatUsername,
				schoolId, isSettle, state);
	}

	@Override
	public List<CommissionOrderListDTO> listCommissionOrder(Integer maraId, Integer adviserId, Integer officialId,
			String name, String phone, String wechatUsername, Integer schoolId, Boolean isSettle, String state,
			int pageNum, int pageSize) throws ServiceException {
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
					phone, wechatUsername, schoolId, isSettle, state, pageNum * pageSize, pageSize);
			if (commissionOrderListDoList == null)
				return null;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (CommissionOrderListDO commissionOrderListDo : commissionOrderListDoList) {
			CommissionOrderListDTO commissionOrderListDto = mapper.map(commissionOrderListDo,
					CommissionOrderListDTO.class);
			if (commissionOrderListDo.getUserId() != null && commissionOrderListDo.getUserId() > 0) {
				UserDO userDo = userDao.getUserById(commissionOrderListDo.getUserId());
				if (userDo != null)
					commissionOrderListDto.setUser(mapper.map(userDo, UserDTO.class));
			}
			if (commissionOrderListDo.getSchoolId() != null && commissionOrderListDo.getSchoolId() > 0) {
				SchoolDO schoolDo = schoolDao.getSchoolById(commissionOrderListDo.getSchoolId());
				if (schoolDo != null)
					commissionOrderListDto.setSchool(mapper.map(schoolDo, SchoolDTO.class));
			}
			commissionOrderListDtoList.add(commissionOrderListDto);
		}
		return commissionOrderListDtoList;
	}

	@Override
	public int updateCommissionOrder(CommissionOrderDTO commissionOrderDto) throws ServiceException {
		if (commissionOrderDto == null) {
			ServiceException se = new ServiceException("commissionOrderDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			CommissionOrderDO commissionOrderDo = mapper.map(commissionOrderDto, CommissionOrderDO.class);
			return commissionOrderDao.updateCommissionOrder(commissionOrderDo);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public CommissionOrderDTO getCommissionOrderById(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		CommissionOrderDTO commissionOrderDto = null;
		try {
			CommissionOrderDO commissionOrderDo = commissionOrderDao.getCommissionOrderById(id);
			if (commissionOrderDo == null)
				return null;
			commissionOrderDto = mapper.map(commissionOrderDo, CommissionOrderDTO.class);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
		return commissionOrderDto;
	}

}
