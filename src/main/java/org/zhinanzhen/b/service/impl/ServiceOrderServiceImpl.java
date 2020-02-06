package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.ServiceOrderDAO;
import org.zhinanzhen.b.dao.ServiceOrderReviewDAO;
import org.zhinanzhen.b.dao.pojo.ServiceOrderDO;
import org.zhinanzhen.b.dao.pojo.ServiceOrderReviewDO;
import org.zhinanzhen.b.service.ServiceOrderService;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.b.service.pojo.ServiceOrderReviewDTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.thrift.ErrorCodeEnum;

@Service("ServiceOrderService")
public class ServiceOrderServiceImpl extends BaseService implements ServiceOrderService {

	@Resource
	private ServiceOrderDAO serviceOrderDao;

	@Resource
	private ServiceOrderReviewDAO serviceOrderReviewDao;

	@Override
	public int addServiceOrder(ServiceOrderDTO serviceOrderDto) throws ServiceException {
		if (serviceOrderDto == null) {
			ServiceException se = new ServiceException("serviceOrderDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			ServiceOrderDO serviceOrderDo = mapper.map(serviceOrderDto, ServiceOrderDO.class);
			if (serviceOrderDao.addServiceOrder(serviceOrderDo) > 0) {
				serviceOrderDto.setId(serviceOrderDo.getId());
				return serviceOrderDto.getId();
			} else {
				return 0;
			}
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int updateServiceOrder(ServiceOrderDTO serviceOrderDto) throws ServiceException {
		if (serviceOrderDto == null) {
			ServiceException se = new ServiceException("serviceOrderDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		if (serviceOrderDto.getId() <= 0) {
			ServiceException se = new ServiceException("id is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			return serviceOrderDao.updateServiceOrder(mapper.map(serviceOrderDto, ServiceOrderDO.class));
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int countServiceOrder(String type, String state, int userId, int maraId, int adviserId, int officialId)
			throws ServiceException {
		return serviceOrderDao.countServiceOrder(type, state, userId > 0 ? userId : null, maraId > 0 ? maraId : null,
				adviserId > 0 ? adviserId : null, officialId > 0 ? officialId : null);
	}

	@Override
	public List<ServiceOrderDTO> listServiceOrder(String type, String state, int userId, int maraId, int adviserId,
			int officialId, int pageNum, int pageSize) throws ServiceException {
		List<ServiceOrderDTO> serviceOrderDtoList = new ArrayList<ServiceOrderDTO>();
		List<ServiceOrderDO> serviceOrderDoList = new ArrayList<ServiceOrderDO>();
		if (pageNum < 0)
			pageNum = DEFAULT_PAGE_NUM;
		if (pageSize < 0)
			pageSize = DEFAULT_PAGE_SIZE;
		try {
			serviceOrderDoList = serviceOrderDao.listServiceOrder(type, state, userId > 0 ? userId : null,
					maraId > 0 ? maraId : null, adviserId > 0 ? adviserId : null, officialId > 0 ? officialId : null,
					pageNum * pageSize, pageSize);
			if (serviceOrderDoList == null) {
				return null;
			}
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (ServiceOrderDO serviceOrderDo : serviceOrderDoList) {
			ServiceOrderDTO serviceOrderDto = mapper.map(serviceOrderDo, ServiceOrderDTO.class);
			// 查询审核记录
			putReviews(serviceOrderDto);
			serviceOrderDtoList.add(serviceOrderDto);
		}
		return serviceOrderDtoList;
	}

	@Override
	public ServiceOrderDTO getServiceOrderById(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		ServiceOrderDTO serviceOrderDto = null;
		try {
			ServiceOrderDO serviceOrderDo = serviceOrderDao.getServiceOrderById(id);
			if (serviceOrderDo == null) {
				return null;
			}
			serviceOrderDto = mapper.map(serviceOrderDo, ServiceOrderDTO.class);
			// 查询审核记录
			putReviews(serviceOrderDto);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
		return serviceOrderDto;
	}

	@Override
	public int deleteServiceOrderById(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			return serviceOrderDao.deleteServiceOrderById(id);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public ServiceOrderDTO approval(int id, int adminUserId, String adviserState, String maraState,
			String officialState, String kjState) throws ServiceException {
		return review(id, adminUserId, adviserState, maraState, officialState, kjState, "APPROVAL");
	}

	@Override
	public ServiceOrderDTO refuse(int id, int adminUserId, String adviserState, String maraState, String officialState,
			String kjState) throws ServiceException {
		return review(id, adminUserId, adviserState, maraState, officialState, kjState, "REFUSE");
	}

	private void putReviews(ServiceOrderDTO serviceOrderDto) {
		List<ServiceOrderReviewDO> serviceOrderReviewDoList = serviceOrderReviewDao
				.listServiceOrderReview(serviceOrderDto.getId(), null, null, null, null, null);
		List<ServiceOrderReviewDTO> serviceOrderReviewDtoList = new ArrayList<ServiceOrderReviewDTO>();
		serviceOrderReviewDoList
				.forEach(review -> serviceOrderReviewDtoList.add(mapper.map(review, ServiceOrderReviewDTO.class)));
		serviceOrderDto.setReviews(serviceOrderReviewDtoList);
		if (serviceOrderReviewDtoList != null && serviceOrderReviewDtoList.size() > 0)
			serviceOrderDto.setReview(serviceOrderReviewDtoList.get(0));
		if (serviceOrderDto.getReview() != null) {
			serviceOrderDto.setState(serviceOrderDto.getReview().getAdviserState());
			serviceOrderDao.updateServiceOrder(mapper.map(serviceOrderDto, ServiceOrderDO.class));
		}
	}

	private ServiceOrderDTO review(int id, int adminUserId, String adviserState, String maraState, String officialState,
			String kjState, String type) throws ServiceException {
		ServiceOrderReviewDO serviceOrderReviewDo = new ServiceOrderReviewDO();
		serviceOrderReviewDo.setServiceOrderId(id);
		serviceOrderReviewDo.setType(type);
		serviceOrderReviewDo.setAdminUserId(adminUserId);
		if (adviserState != null)
			serviceOrderReviewDo.setAdviserState(adviserState);
		if (maraState != null)
			serviceOrderReviewDo.setMaraState(maraState);
		if (officialState != null)
			serviceOrderReviewDo.setOfficialState(officialState);
		if (kjState != null)
			serviceOrderReviewDo.setKjState(kjState);
		serviceOrderReviewDao.addServiceOrderReview(serviceOrderReviewDo);
		return getServiceOrderById(id);
	}

}
