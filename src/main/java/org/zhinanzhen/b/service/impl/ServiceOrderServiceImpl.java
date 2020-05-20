package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.KjDAO;
import org.zhinanzhen.b.dao.MaraDAO;
import org.zhinanzhen.b.dao.OfficialDAO;
import org.zhinanzhen.b.dao.ReceiveTypeDAO;
import org.zhinanzhen.b.dao.SchoolDAO;
import org.zhinanzhen.b.dao.ServiceDAO;
import org.zhinanzhen.b.dao.ServiceOrderCommentDAO;
import org.zhinanzhen.b.dao.ServiceOrderDAO;
import org.zhinanzhen.b.dao.ServiceOrderReviewDAO;
import org.zhinanzhen.b.dao.ServicePackageDAO;
import org.zhinanzhen.b.dao.SubagencyDAO;
import org.zhinanzhen.b.dao.pojo.KjDO;
import org.zhinanzhen.b.dao.pojo.MaraDO;
import org.zhinanzhen.b.dao.pojo.OfficialDO;
import org.zhinanzhen.b.dao.pojo.ReceiveTypeDO;
import org.zhinanzhen.b.dao.pojo.SchoolDO;
import org.zhinanzhen.b.dao.pojo.ServiceDO;
import org.zhinanzhen.b.dao.pojo.ServiceOrderCommentDO;
import org.zhinanzhen.b.dao.pojo.ServiceOrderDO;
import org.zhinanzhen.b.dao.pojo.ServiceOrderReviewDO;
import org.zhinanzhen.b.dao.pojo.ServicePackageDO;
import org.zhinanzhen.b.dao.pojo.SubagencyDO;
import org.zhinanzhen.b.service.ServiceOrderService;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.b.service.pojo.ServiceOrderReviewDTO;
import org.zhinanzhen.b.service.pojo.ServicePackageDTO;
import org.zhinanzhen.tb.dao.AdminUserDAO;
import org.zhinanzhen.tb.dao.AdviserDAO;
import org.zhinanzhen.tb.dao.UserDAO;
import org.zhinanzhen.tb.dao.pojo.AdminUserDO;
import org.zhinanzhen.tb.dao.pojo.AdviserDO;
import org.zhinanzhen.tb.dao.pojo.UserDO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;
import org.zhinanzhen.tb.service.pojo.AdviserDTO;
import org.zhinanzhen.tb.service.pojo.UserDTO;
import org.zhinanzhen.tb.utils.SendEmailUtil;
import org.zhinanzhen.b.service.pojo.MaraDTO;
import org.zhinanzhen.b.service.pojo.OfficialDTO;
import org.zhinanzhen.b.service.pojo.SchoolDTO;
import org.zhinanzhen.b.service.pojo.ServiceDTO;
import org.zhinanzhen.b.service.pojo.ServiceOrderCommentDTO;
import org.zhinanzhen.b.service.pojo.SubagencyDTO;
import org.zhinanzhen.b.service.pojo.ReceiveTypeDTO;

import com.ikasoa.core.ErrorCodeEnum;

@Service("ServiceOrderService")
public class ServiceOrderServiceImpl extends BaseService implements ServiceOrderService {

	@Resource
	private ServiceOrderDAO serviceOrderDao;

	@Resource
	private ServiceOrderReviewDAO serviceOrderReviewDao;

	@Resource
	private SchoolDAO schoolDao;

	@Resource
	private SubagencyDAO subagencyDao;

	@Resource
	private ServiceDAO serviceDao;

	@Resource
	private ReceiveTypeDAO receiveTypeDao;

	@Resource
	private UserDAO userDao;

	@Resource
	private MaraDAO maraDao;

	@Resource
	private AdviserDAO adviserDao;

	@Resource
	private OfficialDAO officialDao;

	@Resource
	private KjDAO kjDao;

	@Resource
	private ServicePackageDAO servicePackageDao;

	@Resource
	private ServiceOrderCommentDAO serviceOrderCommentDao;

	@Resource
	private AdminUserDAO adminUserDao;

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
	public int updateServiceOrderRviewState(int id, String reviewState) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			return serviceOrderDao.updateReviewState(id, reviewState);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int countServiceOrder(String type, String excludeState, List<String> stateList, List<String> reviewStateList,
			int userId, int maraId, int adviserId, int officialId) throws ServiceException {
		return serviceOrderDao.countServiceOrder(type, excludeState, stateList, reviewStateList,
				userId > 0 ? userId : null, maraId > 0 ? maraId : null, adviserId > 0 ? adviserId : null,
				officialId > 0 ? officialId : null);
	}

	@Override
	public List<ServiceOrderDTO> listServiceOrder(String type, String excludeState, List<String> stateList,
			List<String> reviewStateList, int userId, int maraId, int adviserId, int officialId, int pageNum,
			int pageSize) throws ServiceException {
		List<ServiceOrderDTO> serviceOrderDtoList = new ArrayList<ServiceOrderDTO>();
		List<ServiceOrderDO> serviceOrderDoList = new ArrayList<ServiceOrderDO>();
		if (pageNum < 0)
			pageNum = DEFAULT_PAGE_NUM;
		if (pageSize < 0)
			pageSize = DEFAULT_PAGE_SIZE;
		try {
			serviceOrderDoList = serviceOrderDao.listServiceOrder(type, excludeState, stateList, reviewStateList,
					userId > 0 ? userId : null, maraId > 0 ? maraId : null, adviserId > 0 ? adviserId : null,
					officialId > 0 ? officialId : null, pageNum * pageSize, pageSize);
			if (serviceOrderDoList == null)
				return null;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (ServiceOrderDO serviceOrderDo : serviceOrderDoList) {
			ServiceOrderDTO serviceOrderDto = mapper.map(serviceOrderDo, ServiceOrderDTO.class);
			// 查询学校课程
			if (serviceOrderDto.getSchoolId() > 0) {
				SchoolDO schoolDo = schoolDao.getSchoolById(serviceOrderDto.getSchoolId());
				if (schoolDo != null)
					serviceOrderDto.setSchool(mapper.map(schoolDo, SchoolDTO.class));
			}
			// 查询Subagency
			if (serviceOrderDto.getSubagencyId() > 0) {
				SubagencyDO subagencyDo = subagencyDao.getSubagencyById(serviceOrderDto.getSubagencyId());
				if (subagencyDo != null)
					serviceOrderDto.setSubagency(mapper.map(subagencyDo, SubagencyDTO.class));
			}
			// 查询服务
			ServiceDO serviceDo = serviceDao.getServiceById(serviceOrderDto.getServiceId());
			if (serviceDo != null)
				serviceOrderDto.setService(mapper.map(serviceDo, ServiceDTO.class));
			// 查询服务包类型
			if (serviceOrderDto.getServicePackageId() > 0) {
				ServicePackageDO servicePackageDo = servicePackageDao.getById(serviceOrderDto.getServicePackageId());
				if (servicePackageDo != null)
					serviceOrderDto.setServicePackage(mapper.map(servicePackageDo, ServicePackageDTO.class));
			}
			// 查询收款方式
			ReceiveTypeDO receiveTypeDo = receiveTypeDao.getReceiveTypeById(serviceOrderDto.getReceiveTypeId());
			if (receiveTypeDo != null)
				serviceOrderDto.setReceiveType(mapper.map(receiveTypeDo, ReceiveTypeDTO.class));
			// 查询用户
			UserDO userDo = userDao.getUserById(serviceOrderDto.getUserId());
			if (userDo != null)
				serviceOrderDto.setUser(mapper.map(userDo, UserDTO.class));
			// 查询Mara
			MaraDO maraDo = maraDao.getMaraById(serviceOrderDto.getMaraId());
			if (maraDo != null)
				serviceOrderDto.setMara(mapper.map(maraDo, MaraDTO.class));
			// 查询顾问
			AdviserDO adviserDo = adviserDao.getAdviserById(serviceOrderDto.getAdviserId());
			if (adviserDo != null)
				serviceOrderDto.setAdviser(mapper.map(adviserDo, AdviserDTO.class));
			// 查询顾问2
			if (serviceOrderDto.getAdviserId2() > 0) {
				AdviserDO adviserDo2 = adviserDao.getAdviserById(serviceOrderDto.getAdviserId2());
				if (adviserDo2 != null)
					serviceOrderDto.setAdviser2(mapper.map(adviserDo2, AdviserDTO.class));
			}
			// 查询文案
			OfficialDO officialDo = officialDao.getOfficialById(serviceOrderDto.getOfficialId());
			if (officialDo != null)
				serviceOrderDto.setOfficial(mapper.map(officialDo, OfficialDTO.class));
			// 查询子服务包
			if (serviceOrderDto.getParentId() <= 0)
				serviceOrderDto.setChildrenServicePackageIds(
						serviceOrderDao.listServicePackageIdByParentId(serviceOrderDto.getId()));
			// 查询审核记录
			putReviews(serviceOrderDto);
			serviceOrderDtoList.add(serviceOrderDto);
		}
		return serviceOrderDtoList;
	}

	@Override
	public ServiceOrderDTO getServiceOrderById(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("service order id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		ServiceOrderDTO serviceOrderDto = null;
		try {
			ServiceOrderDO serviceOrderDo = serviceOrderDao.getServiceOrderById(id);
			if (serviceOrderDo == null)
				return null;
			serviceOrderDto = mapper.map(serviceOrderDo, ServiceOrderDTO.class);
			// 查询学校课程
			if (serviceOrderDto.getSchoolId() > 0) {
				SchoolDO schoolDo = schoolDao.getSchoolById(serviceOrderDto.getSchoolId());
				if (schoolDo != null)
					serviceOrderDto.setSchool(mapper.map(schoolDo, SchoolDTO.class));
			}
			// 查询Subagency
			if (serviceOrderDto.getSubagencyId() > 0) {
				SubagencyDO subagencyDo = subagencyDao.getSubagencyById(serviceOrderDto.getSubagencyId());
				if (subagencyDo != null)
					serviceOrderDto.setSubagency(mapper.map(subagencyDo, SubagencyDTO.class));
			}
			// 查询服务
			ServiceDO serviceDo = serviceDao.getServiceById(serviceOrderDto.getServiceId());
			if (serviceDo != null)
				serviceOrderDto.setService(mapper.map(serviceDo, ServiceDTO.class));
			// 查询服务包类型
			if (serviceOrderDto.getServicePackageId() > 0) {
				ServicePackageDO servicePackageDo = servicePackageDao.getById(serviceOrderDto.getServicePackageId());
				if (servicePackageDo != null)
					serviceOrderDto.setServicePackage(mapper.map(servicePackageDo, ServicePackageDTO.class));
			}
			// 查询收款方式
			ReceiveTypeDO receiveTypeDo = receiveTypeDao.getReceiveTypeById(serviceOrderDto.getReceiveTypeId());
			if (receiveTypeDo != null)
				serviceOrderDto.setReceiveType(mapper.map(receiveTypeDo, ReceiveTypeDTO.class));
			// 查询用户
			UserDO userDo = userDao.getUserById(serviceOrderDto.getUserId());
			if (userDo != null)
				serviceOrderDto.setUser(mapper.map(userDo, UserDTO.class));
			// 查询Mara
			MaraDO maraDo = maraDao.getMaraById(serviceOrderDto.getMaraId());
			if (maraDo != null)
				serviceOrderDto.setMara(mapper.map(maraDo, MaraDTO.class));
			// 查询顾问
			AdviserDO adviserDo = adviserDao.getAdviserById(serviceOrderDto.getAdviserId());
			if (adviserDo != null)
				serviceOrderDto.setAdviser(mapper.map(adviserDo, AdviserDTO.class));
			// 查询顾问2
			if (serviceOrderDto.getAdviserId2() > 0) {
				AdviserDO adviserDo2 = adviserDao.getAdviserById(serviceOrderDto.getAdviserId2());
				if (adviserDo2 != null)
					serviceOrderDto.setAdviser2(mapper.map(adviserDo2, AdviserDTO.class));
			}
			// 查询文案
			OfficialDO officialDo = officialDao.getOfficialById(serviceOrderDto.getOfficialId());
			if (officialDo != null)
				serviceOrderDto.setOfficial(mapper.map(officialDo, OfficialDTO.class));
			// 查询子服务包
			if (serviceOrderDto.getParentId() <= 0)
				serviceOrderDto.setChildrenServicePackageIds(
						serviceOrderDao.listServicePackageIdByParentId(serviceOrderDto.getId()));
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
	public int finish(int id) throws ServiceException {
		return serviceOrderDao.finishServiceOrder(id);
	}

	@Override
	public ServiceOrderDTO approval(int id, int adminUserId, String adviserState, String maraState,
			String officialState, String kjState) throws ServiceException {
		ServiceOrderDO serviceOrderDo = serviceOrderDao.getServiceOrderById(adminUserId);
		if (serviceOrderDo != null) {
			String title = "提醒邮件";
			String type = "";
			if ("VISA".equalsIgnoreCase(serviceOrderDo.getType()))
				type = "签证";
			else if ("OVST".equalsIgnoreCase(serviceOrderDo.getType()))
				type = "留学";
			else if ("SIV".equalsIgnoreCase(serviceOrderDo.getType()))
				type = "独立技术移民";
			else if ("MT".equalsIgnoreCase(serviceOrderDo.getType()))
				type = "曼拓";
			AdviserDO adviserDo = adviserDao.getAdviserById(serviceOrderDo.getAdviserId());
			OfficialDO officialDo = officialDao.getOfficialById(serviceOrderDo.getOfficialId());
			Date date = serviceOrderDo.getGmtCreate();
			if (adviserDo != null && officialDo != null) {
				if ("REVIEW".equals(maraState)) {
					MaraDO maraDo = maraDao.getMaraById(serviceOrderDo.getMaraId());
					if (maraDo != null)
						SendEmailUtil.send(maraDo.getEmail(), title,
								"亲爱的" + maraDo.getName() + ":<br/>您有一条新的服务订单任务请及时处理。<br/>订单号:" + id + "/服务类型:" + type
										+ "/顾问:" + adviserDo.getName() + "/文案:" + officialDo.getName() + "/创建时间:"
										+ date);
				}
				if ("REVIEW".equals(officialState)) {
					SendEmailUtil.send(officialDo.getEmail(), title,
							"亲爱的" + officialDo.getName() + ":<br/>您有一条新的服务订单任务请及时处理。<br/>订单号:" + id + "/服务类型:" + type
									+ "/顾问:" + adviserDo.getName() + "/文案:" + officialDo.getName() + "/创建时间:" + date);
				}
				if ("REVIEW".equals(kjState)) {
					List<AdminUserDO> adminUserDoList = adminUserDao.listAdminUserByAp("KJ");
					for (AdminUserDO adminUserDo : adminUserDoList)
						if (adminUserDo != null) {
							KjDO kjDo = kjDao.getKjById(adminUserDo.getKjId());
							if (kjDo != null)
								SendEmailUtil.send(kjDo.getEmail(), title,
										"亲爱的" + kjDo.getName() + ":<br/>您有一条新的服务订单任务请及时处理。<br/>订单号:" + id + "/服务类型:"
												+ type + "/顾问:" + adviserDo.getName() + "/文案:" + officialDo.getName()
												+ "/创建时间:" + date);
						}
				}
			}
		}
		return review(id, adminUserId, adviserState, maraState, officialState, kjState, "APPROVAL");
	}

	@Override
	public ServiceOrderDTO refuse(int id, int adminUserId, String adviserState, String maraState, String officialState,
			String kjState) throws ServiceException {
		return review(id, adminUserId, adviserState, maraState, officialState, kjState, "REFUSE");
	}

	@Override
	public List<ServiceOrderReviewDTO> reviews(int serviceOrderId) throws ServiceException {
		List<ServiceOrderReviewDTO> serviceOrderReviewDtoList = new ArrayList<>();
		List<ServiceOrderReviewDO> serviceOrderReviewDoList = serviceOrderReviewDao
				.listServiceOrderReview(serviceOrderId, null, null, null, null, null);
		serviceOrderReviewDoList.forEach(serviceOrderReviewDo -> serviceOrderReviewDtoList
				.add(mapper.map(serviceOrderReviewDo, ServiceOrderReviewDTO.class)));
		return serviceOrderReviewDtoList;
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
		if (serviceOrderDto.getReview() != null && serviceOrderDto.getReview().getAdviserState() != null) {
			ServiceOrderDO serviceOrderDo = serviceOrderDao.getServiceOrderById(serviceOrderDto.getId());
			serviceOrderDo.setState(serviceOrderDto.getReview().getAdviserState());
			serviceOrderDao.updateServiceOrder(serviceOrderDo);
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

	@Override
	public int addComment(ServiceOrderCommentDTO serviceOrderCommentDto) throws ServiceException {
		if (serviceOrderCommentDto == null) {
			ServiceException se = new ServiceException("serviceOrderCommentDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			ServiceOrderCommentDO serviceOrderCommentDo = mapper.map(serviceOrderCommentDto,
					ServiceOrderCommentDO.class);
			if (serviceOrderCommentDao.add(serviceOrderCommentDo) > 0) {
				serviceOrderCommentDto.setId(serviceOrderCommentDo.getId());
				return serviceOrderCommentDo.getId();
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
	public List<ServiceOrderCommentDTO> listComment(int id) throws ServiceException {
		List<ServiceOrderCommentDTO> serviceOrderCommentDtoList = new ArrayList<>();
		List<ServiceOrderCommentDO> serviceOrderCommentDoList = new ArrayList<>();
		try {
			serviceOrderCommentDoList = serviceOrderCommentDao.list(id);
			if (serviceOrderCommentDoList == null)
				return null;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (ServiceOrderCommentDO serviceOrderCommentDo : serviceOrderCommentDoList) {
			ServiceOrderCommentDTO serviceOrderCommentDto = mapper.map(serviceOrderCommentDo,
					ServiceOrderCommentDTO.class);
			AdminUserDO adminUserDo = adminUserDao.getAdminUserById(serviceOrderCommentDo.getAdminUserId());
			if (adminUserDo != null)
				serviceOrderCommentDto.setAdminUserName(adminUserDo.getUsername());
			serviceOrderCommentDtoList.add(serviceOrderCommentDto);
		}
		return serviceOrderCommentDtoList;
	}

	@Override
	public int deleteComment(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			return serviceOrderCommentDao.delete(id);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

}
