package org.zhinanzhen.b.service.impl;

import java.util.*;
import javax.annotation.Resource;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zhinanzhen.b.dao.*;
import org.zhinanzhen.b.dao.pojo.*;
import org.zhinanzhen.b.service.ServiceOrderService;
import org.zhinanzhen.b.service.pojo.*;
import org.zhinanzhen.b.service.pojo.ant.Sorter;
import org.zhinanzhen.tb.dao.AdminUserDAO;
import org.zhinanzhen.tb.dao.AdviserDAO;
import org.zhinanzhen.tb.dao.RegionDAO;
import org.zhinanzhen.tb.dao.UserDAO;
import org.zhinanzhen.tb.dao.pojo.AdminUserDO;
import org.zhinanzhen.tb.dao.pojo.AdviserDO;
import org.zhinanzhen.tb.dao.pojo.RegionDO;
import org.zhinanzhen.tb.dao.pojo.UserDO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;
import org.zhinanzhen.tb.service.pojo.AdviserDTO;
import org.zhinanzhen.tb.service.pojo.UserDTO;
import com.ikasoa.core.ErrorCodeEnum;
import com.ikasoa.core.utils.StringUtil;
import lombok.Data;
import org.zhinanzhen.tb.utils.WXWorkAPI;

@Service("ServiceOrderService")
public class ServiceOrderServiceImpl extends BaseService implements ServiceOrderService {

	@Resource
	private ServiceOrderDAO serviceOrderDao;

//	@Resource
//	private ServiceOrderReviewDAO serviceOrderReviewDao;

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
	private CommissionOrderDAO commissionOrderDao;

	@Resource
	private ServiceOrderOfficialRemarksDAO serviceOrderOfficialRemarksDao;

	@Resource
	private OfficialTagDAO officialTagDao;

	@Resource
	private VisaDAO visaDao;

	@Resource
	private AdminUserDAO adminUserDao;

	@Resource
	private ServiceAssessDao serviceAssessDao;

	@Resource
	private WXWorkDAO wxWorkDAO;

	@Resource
	private RegionDAO regionDAO;

	@Override
	public int addServiceOrder(ServiceOrderDTO serviceOrderDto) throws ServiceException {
		if (serviceOrderDto == null) {
			ServiceException se = new ServiceException("serviceOrderDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		if (serviceOrderDto.getVerifyCode() != null) {
			List<CommissionOrderDO> commissionOrderDOS = commissionOrderDao
					.listCommissionOrderByVerifyCode(serviceOrderDto.getVerifyCode());
			List<VisaDO> visaDOS = visaDao.listVisaByVerifyCode(serviceOrderDto.getVerifyCode());
			if (commissionOrderDOS.size() > 0 | serviceOrderDao.listByVerifyCode(serviceOrderDto.getVerifyCode()).size() > 0 || visaDOS.size() > 0) {
				ServiceException se = new ServiceException(
						"对账code:" + serviceOrderDto.getVerifyCode() + "已经存在,请重新创建新的code!");
				se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
				throw se;
			}
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
		if (serviceOrderDto.getVerifyCode() != null) {
			List<CommissionOrderDO> commissionOrderDOS = commissionOrderDao
					.listCommissionOrderByVerifyCode(serviceOrderDto.getVerifyCode());
			List<VisaDO> visaDOS = visaDao.listVisaByVerifyCode(serviceOrderDto.getVerifyCode());
			if (commissionOrderDOS.size() > 0) {
				ServiceException se = new ServiceException(
						"对账code:" + serviceOrderDto.getVerifyCode() + "已经存在,请重新创建新的code!");
				se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
				throw se;
			}
			for (VisaDO visaDO : visaDOS) {
				if (visaDO.getServiceOrderId() != serviceOrderDto.getId()) {
					ServiceException se = new ServiceException(
							"对账code:" + serviceOrderDto.getVerifyCode() + "已经存在,请重新创建新的code!");
					se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
					throw se;
				}
			}
		}
		try {
			ServiceOrderDO _serviceOrderDo = serviceOrderDao.getServiceOrderById(serviceOrderDto.getId());
			ServiceOrderDO serviceOrderDo = mapper.map(serviceOrderDto, ServiceOrderDO.class);
			int i = serviceOrderDao.updateServiceOrder(serviceOrderDo);
			if (i > 0
					&& ((_serviceOrderDo.getMaraId() > 0 && serviceOrderDo.getMaraId() > 0
							&& _serviceOrderDo.getMaraId() != serviceOrderDo.getMaraId())
							|| (_serviceOrderDo.getOfficialId() > 0 && serviceOrderDo.getOfficialId() > 0
									&& _serviceOrderDo.getOfficialId() != serviceOrderDo.getOfficialId()))
					&& !"PENDING".equalsIgnoreCase(serviceOrderDo.getState()))
				sendEmailOfUpdateOfficial(serviceOrderDo, _serviceOrderDo);
			return i;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	private void sendEmailOfUpdateOfficial(ServiceOrderDO serviceOrderDo, ServiceOrderDO _serviceOrderDo) {
		ServiceOrderMailDetail serviceOrderMailDetail = getServiceOrderMailDetail(serviceOrderDo, "任务提醒:");
		UserDO user = serviceOrderMailDetail.getUser();
		AdviserDO adviserDo = adviserDao.getAdviserById(serviceOrderDo.getAdviserId());
		OfficialDO officialDo = officialDao.getOfficialById(serviceOrderDo.getOfficialId());
		OfficialDO _officialDo = officialDao.getOfficialById(_serviceOrderDo.getOfficialId());
		Date date = serviceOrderDo.getGmtCreate();
		sendMail(adviserDo.getEmail(), "变更任务提醒:",
				StringUtil.merge("亲爱的:", adviserDo.getName(), "<br/>", "您的订单已经变更。", "<br>订单号:", serviceOrderDo.getId(),
						"<br/>客户名称:", user.getName(), "/顾问:", "/文案:", officialDo.getName(), adviserDo.getName(),
						"<br/>属性:", getPeopleTypeStr(serviceOrderDo.getPeopleType()), "<br/>坚果云资料地址:",
						serviceOrderDo.getNutCloud(), "<br/>客户基本信息:", serviceOrderDo.getInformation(), "<br/>备注:",
						serviceOrderDo.getRemarks(), "<br/>驳回原因:", serviceOrderDo.getRefuseReason(), "<br/>创建时间:", date,
						"<br/>", serviceOrderMailDetail.getServiceOrderUrl()));
		if ("VISA".equalsIgnoreCase(serviceOrderDo.getType())) {
			if (_serviceOrderDo.getMaraId() > 0 && serviceOrderDo.getMaraId() > 0
					&& _serviceOrderDo.getMaraId() != serviceOrderDo.getMaraId()) {
				MaraDO maraDo = maraDao.getMaraById(serviceOrderDo.getMaraId());
				MaraDO _maraDo = maraDao.getMaraById(_serviceOrderDo.getMaraId());
				sendMail(maraDo.getEmail(), "新任务提醒:",
						StringUtil.merge("亲爱的:", maraDo.getName(), "<br/>", "您有一条新的服务订单任务请及时处理。", "<br>订单号:",
								serviceOrderDo.getId(), "<br/>服务类型:签证/客户名称:", user.getName(), "/顾问:",
								adviserDo.getName(), "/文案:", officialDo.getName(), "/MARA:", maraDo.getName(),
								"<br/>属性:", getPeopleTypeStr(serviceOrderDo.getPeopleType()), "<br/>坚果云资料地址:",
								serviceOrderDo.getNutCloud(), "<br/>客户基本信息:", serviceOrderDo.getInformation(),
								"<br/>备注:", serviceOrderDo.getRemarks(), "<br/>驳回原因:", serviceOrderDo.getRefuseReason(),
								"<br/>创建时间:", date, "<br/>", serviceOrderMailDetail.getServiceOrderUrl()));
				sendMail(_maraDo.getEmail(), "变更任务提醒:", StringUtil.merge("亲爱的", _maraDo.getName(), ":<br/>", "您有的订单号:",
						serviceOrderDo.getId(), "已从您这更改为Mara:", maraDo.getName()));
			}
		}
		if (_serviceOrderDo.getOfficialId() > 0 && serviceOrderDo.getOfficialId() > 0
				&& _serviceOrderDo.getOfficialId() != serviceOrderDo.getOfficialId()) {
			sendMail(officialDo.getEmail() + ",maggie@zhinanzhen.org", "新任务提醒:",
					StringUtil.merge("亲爱的", officialDo.getName(), ":<br/>", "您有一条新的服务订单任务请及时处理。", "<br/>订单号:",
							serviceOrderDo.getId(), "<br/>服务类型:", serviceOrderMailDetail.getType(),
							serviceOrderMailDetail.getDetail(), "/顾问:", adviserDo.getName(), "/文案:",
							officialDo.getName(), "<br/>属性:", getPeopleTypeStr(serviceOrderDo.getPeopleType()),
							"<br/>坚果云资料地址:", serviceOrderDo.getNutCloud(), "<br/>客户基本信息:",
							serviceOrderDo.getInformation(), "<br/>备注:", serviceOrderDo.getRemarks(), "<br/>驳回原因:",
							serviceOrderDo.getRefuseReason(), "<br/>创建时间:", date, "<br/>",
							serviceOrderMailDetail.getServiceOrderUrl()));
			sendMail(_officialDo.getEmail() + ",maggie@zhinanzhen.org", "变更任务提醒:",
					StringUtil.merge("亲爱的", _officialDo.getName(), ":<br/>", "您有的订单号:", serviceOrderDo.getId(),
							"已从您这更改为文案:", officialDo.getName()));
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
	public int countServiceOrder(String type, String excludeState, List<String> stateList, String auditingState,
			List<String> reviewStateList, String startMaraApprovalDate, String endMaraApprovalDate,
			String startOfficialApprovalDate, String endOfficialApprovalDate, String startReadcommittedDate,
			String endReadcommittedDate, List<Integer> regionIdList, Integer userId,String userName,  Integer maraId, Integer adviserId,
			Integer officialId, Integer officialTagId, int parentId, boolean isNotApproved, Integer serviceId,
			Integer schoolId, Boolean isPay) throws ServiceException {
		return serviceOrderDao.countServiceOrder(type, excludeState, stateList, auditingState, reviewStateList,
				startMaraApprovalDate, endMaraApprovalDate, startOfficialApprovalDate, endOfficialApprovalDate,
				startReadcommittedDate, endReadcommittedDate, regionIdList, userId, userName,maraId, adviserId, officialId,
				officialTagId, parentId, isNotApproved, serviceId, schoolId, isPay);
	}

	@Override
	public List<ServiceOrderDTO> listServiceOrder(String type, String excludeState, List<String> stateList,
			String auditingState, List<String> reviewStateList, String startMaraApprovalDate,
			String endMaraApprovalDate, String startOfficialApprovalDate, String endOfficialApprovalDate,
			String startReadcommittedDate, String endReadcommittedDate, List<Integer> regionIdList, Integer userId,String userName,
			Integer maraId, Integer adviserId, Integer officialId, Integer officialTagId, int parentId,
			boolean isNotApproved, int pageNum, int pageSize, Sorter sorter, Integer serviceId, Integer schoolId, Boolean isPay)
			throws ServiceException {
		List<ServiceOrderDTO> serviceOrderDtoList = new ArrayList<ServiceOrderDTO>();
		List<ServiceOrderDO> serviceOrderDoList = new ArrayList<ServiceOrderDO>();
		if (pageNum < 0)
			pageNum = DEFAULT_PAGE_NUM;
		if (pageSize < 0)
			pageSize = DEFAULT_PAGE_SIZE;
		String orderBy = "ORDER BY so.id DESC";
		if (sorter != null) {
			if (sorter.getId() != null)
				orderBy = StringUtil.merge("ORDER BY ", sorter.getOrderBy("so.id", sorter.getId()));
			if (sorter.getAdviserName() != null)
				orderBy = StringUtil.merge("ORDER BY ", sorter.getOrderBy("a.name", sorter.getAdviserName()));
		}
		try {
			serviceOrderDoList = serviceOrderDao.listServiceOrder(type, excludeState, stateList, auditingState,
					reviewStateList, startMaraApprovalDate, endMaraApprovalDate, startOfficialApprovalDate,
					theDateTo23_59_59(endOfficialApprovalDate), startReadcommittedDate, endReadcommittedDate, regionIdList, userId, userName,maraId,
					adviserId, officialId, officialTagId, parentId, isNotApproved, serviceId, schoolId, isPay,
					pageNum * pageSize, pageSize, orderBy);
			if (serviceOrderDoList == null)
				return null;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (ServiceOrderDO serviceOrderDo : serviceOrderDoList) {
			/*
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
			if (adviserDo != null){
				RegionDO regionDO = regionDAO.getRegionById(adviserDo.getRegionId());
				serviceOrderDto.setAdviser(mapper.map(adviserDo, AdviserDTO.class));
				if (regionDO != null)
					serviceOrderDto.getAdviser().setRegionName(regionDO.getName());
				serviceOrderDto.getAdviser().setRegionDo(regionDO);
			}
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
			// 查询文案Tag
			OfficialTagDO officialTagDo = officialTagDao.getOfficialTagByServiceOrderId(serviceOrderDto.getId());
			if (officialTagDo != null)
				serviceOrderDto.setOfficialTag(mapper.map(officialTagDo, OfficialTagDTO.class));
			// 查询子服务
			if (serviceOrderDto.getParentId() <= 0) {
				List<ChildrenServiceOrderDTO> childrenServiceOrderList = new ArrayList<>();
				List<ServiceOrderDO> list = serviceOrderDao.listByParentId(serviceOrderDto.getId());
				list.forEach(serviceOrder -> {
					ChildrenServiceOrderDTO childrenServiceOrderDto = mapper.map(serviceOrder,
							ChildrenServiceOrderDTO.class);
					ServicePackageDO servicePackageDo = servicePackageDao
							.getById(childrenServiceOrderDto.getServicePackageId()); // TODO:
																						// 又偷懒了，性能比较差哦：）
					if (servicePackageDo != null)
						childrenServiceOrderDto.setServicePackageType(servicePackageDo.getType());
					childrenServiceOrderList.add(childrenServiceOrderDto);
				});
				serviceOrderDto.setChildrenServiceOrders(childrenServiceOrderList);
			}

			List<Integer> cIds = new ArrayList<>();
//			List<VisaDO> visaList = visaDao.listVisaByServiceOrderId(serviceOrderDo.getId());
//			if (visaList != null && visaList.size() > 0) {
//				for (VisaDO visaDo : visaList)
//					cIds.add(visaDo.getId());
//			}
//			List<CommissionOrderDO> commissionOrderList = commissionOrderDao
//					.listCommissionOrderByServiceOrderId(serviceOrderDto.getId());
//			if (commissionOrderList != null && commissionOrderList.size() > 0) {
//				for (CommissionOrderDO commissionOrderDo : commissionOrderList)
//					cIds.add(commissionOrderDo.getId());
//			}
			serviceOrderDto.setCIds(cIds);

			// 查询审核记录
			//putReviews(serviceOrderDto);
			serviceOrderDtoList.add(serviceOrderDto);

			// 查询职业名称
			ServiceAssessDO serviceAssessDO = serviceAssessDao.seleteAssessById(serviceOrderDto.getServiceAssessId());
			if (serviceAssessDO != null)
				serviceOrderDto.setServiceAssessDO(serviceAssessDO);
			*/
			serviceOrderDtoList.add(putServiceOrderDTO(serviceOrderDo));
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
			serviceOrderDto = putServiceOrderDTO(serviceOrderDo);
			/*
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
			// 查询文案Tag
			OfficialTagDO officialTagDo = officialTagDao.getOfficialTagByServiceOrderId(serviceOrderDto.getId());
			if (officialTagDo != null)
				serviceOrderDto.setOfficialTag(mapper.map(officialTagDo, OfficialTagDTO.class));
			*/
			// 是否有创建过佣金订单
			if ("OVST".equalsIgnoreCase(serviceOrderDto.getType()))
				serviceOrderDto.setHasCommissionOrder(commissionOrderDao
						.countCommissionOrderByServiceOrderIdAndExcludeCode(serviceOrderDto.getId(), null) > 0);
			else if ("VISA".equalsIgnoreCase(serviceOrderDto.getType()))
				serviceOrderDto.setHasCommissionOrder(
						visaDao.countVisaByServiceOrderIdAndExcludeCode(serviceOrderDto.getId(), null) > 0);
			else
				serviceOrderDto.setHasCommissionOrder(false);
			/*
			// 查询子服务
			if (serviceOrderDto.getParentId() <= 0) {
				List<ChildrenServiceOrderDTO> childrenServiceOrderList = new ArrayList<>();
				List<ServiceOrderDO> list = serviceOrderDao.listByParentId(serviceOrderDto.getId());
				list.forEach(serviceOrder -> {
					ChildrenServiceOrderDTO childrenServiceOrderDto = mapper.map(serviceOrder,
							ChildrenServiceOrderDTO.class);
					ServicePackageDO servicePackageDo = servicePackageDao
							.getById(childrenServiceOrderDto.getServicePackageId());
					if (servicePackageDo != null)
						childrenServiceOrderDto.setServicePackageType(servicePackageDo.getType());
					childrenServiceOrderList.add(childrenServiceOrderDto);
				});
				serviceOrderDto.setChildrenServiceOrders(childrenServiceOrderList);
			}
			// 查询审核记录
//			putReviews(serviceOrderDto);

			// 查询职业名称
			ServiceAssessDO serviceAssessDO = serviceAssessDao.seleteAssessById(serviceOrderDto.getServiceAssessId());
			if (serviceAssessDO != null)
				serviceOrderDto.setServiceAssessDO(serviceAssessDO);
			*/
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
		return serviceOrderDto;
	}

	public ServiceOrderDTO putServiceOrderDTO(ServiceOrderDO serviceOrderDO){
		ServiceOrderDTO serviceOrderDto = mapper.map(serviceOrderDO, ServiceOrderDTO.class);
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
		if (adviserDo != null){
			RegionDO regionDO = regionDAO.getRegionById(adviserDo.getRegionId());
			serviceOrderDto.setAdviser(mapper.map(adviserDo, AdviserDTO.class));
			if (regionDO != null)
				serviceOrderDto.getAdviser().setRegionName(regionDO.getName());
			serviceOrderDto.getAdviser().setRegionDo(regionDO);
		}
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
		// 查询文案Tag
		OfficialTagDO officialTagDo = officialTagDao.getOfficialTagByServiceOrderId(serviceOrderDto.getId());
		if (officialTagDo != null)
			serviceOrderDto.setOfficialTag(mapper.map(officialTagDo, OfficialTagDTO.class));
		// 查询子服务
		if (serviceOrderDto.getParentId() <= 0) {
			List<ChildrenServiceOrderDTO> childrenServiceOrderList = new ArrayList<>();
			List<ServiceOrderDO> list = serviceOrderDao.listByParentId(serviceOrderDto.getId());
			list.forEach(serviceOrder -> {
				ChildrenServiceOrderDTO childrenServiceOrderDto = mapper.map(serviceOrder,
						ChildrenServiceOrderDTO.class);
				ServicePackageDO servicePackageDo = servicePackageDao
						.getById(childrenServiceOrderDto.getServicePackageId()); // TODO:
				// 又偷懒了，性能比较差哦：）
				if (servicePackageDo != null)
					childrenServiceOrderDto.setServicePackageType(servicePackageDo.getType());
				childrenServiceOrderList.add(childrenServiceOrderDto);
			});
			serviceOrderDto.setChildrenServiceOrders(childrenServiceOrderList);
		}

		List<Integer> cIds = new ArrayList<>();
//			List<VisaDO> visaList = visaDao.listVisaByServiceOrderId(serviceOrderDo.getId());
//			if (visaList != null && visaList.size() > 0) {
//				for (VisaDO visaDo : visaList)
//					cIds.add(visaDo.getId());
//			}
//			List<CommissionOrderDO> commissionOrderList = commissionOrderDao
//					.listCommissionOrderByServiceOrderId(serviceOrderDto.getId());
//			if (commissionOrderList != null && commissionOrderList.size() > 0) {
//				for (CommissionOrderDO commissionOrderDo : commissionOrderList)
//					cIds.add(commissionOrderDo.getId());
//			}
		serviceOrderDto.setCIds(cIds);

		// 查询审核记录
		//putReviews(serviceOrderDto);
		//serviceOrderDtoList.add(serviceOrderDto);

		// 查询职业名称
		ServiceAssessDO serviceAssessDO = serviceAssessDao.seleteAssessById(serviceOrderDto.getServiceAssessId());
		if (serviceAssessDO != null)
			serviceOrderDto.setServiceAssessDO(serviceAssessDO);

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
	public int Readcommitted(int id) throws ServiceException {
		return serviceOrderDao.ReadcommittedServiceOrder(id);
	}

	@Override
	@Transactional
	public ServiceOrderDTO approval(int id, int adminUserId, String adviserState, String maraState,
			String officialState, String kjState) throws ServiceException {
		sendRemind(id, adviserState, maraState, officialState);
		//createChat(id);
		return review(id, adminUserId, adviserState, maraState, officialState, kjState, "APPROVAL");
	}

	@Override
	public ServiceOrderDTO refuse(int id, int adminUserId, String adviserState, String maraState, String officialState,
			String kjState) throws ServiceException {
		sendRemind2(id, adviserState, maraState, officialState);
		return review(id, adminUserId, adviserState, maraState, officialState, kjState, "REFUSE");
	}

	@Override
	public void sendRemind(int id, String state) {
		ServiceOrderDO serviceOrderDo = serviceOrderDao.getServiceOrderById(id);
		if (serviceOrderDo != null) {
			ServiceOrderMailDetail serviceOrderMailDetail = getServiceOrderMailDetail(serviceOrderDo, "新任务提醒:");
			UserDO user = serviceOrderMailDetail.getUser();
			AdviserDO adviserDo = adviserDao.getAdviserById(serviceOrderDo.getAdviserId());
			OfficialDO officialDo = officialDao.getOfficialById(serviceOrderDo.getOfficialId());
			Date date = serviceOrderDo.getGmtCreate();
			if (adviserDo != null && officialDo != null) {
//				if ("REJECT".equals(state) || "WAIT".equals(state)) {
				if ("VISA".equalsIgnoreCase(serviceOrderDo.getType()) && "FINISH".equals(state)) {
					String _title = StringUtil.merge("MARA审核通过提醒:", user.getName(), "/签证");
					// 发送给顾问
					sendMail(adviserDo.getEmail(), _title, StringUtil.merge("亲爱的:", adviserDo.getName(), "<br/>",
							"您的订单已经审核完成请查看并进行下一步操作。<br>订单号:", serviceOrderDo.getId(), "<br/>服务类型:签证/客户名称:",
							user.getName(), "/顾问:", adviserDo.getName(), "/文案:", officialDo.getName(), "<br/>属性:",
							getPeopleTypeStr(serviceOrderDo.getPeopleType()), "<br/>坚果云资料地址:",
							serviceOrderDo.getNutCloud(), "<br/>客户基本信息:", serviceOrderDo.getInformation(), "<br/>备注:",
							serviceOrderDo.getRemarks(), "<br/>驳回原因:", serviceOrderDo.getRefuseReason(), "<br/>创建时间:",
							date, "<br/>", serviceOrderMailDetail.getServiceOrderUrl()));
					// 发送给文案
					sendMail(officialDo.getEmail(), _title, StringUtil.merge("亲爱的:", officialDo.getName(), "<br/>",
							"您的订单已经审核完成请查看并进行下一步操作。<br>订单号:", serviceOrderDo.getId(), "<br/>服务类型:签证/客户名称:",
							user.getName(), "/顾问:", adviserDo.getName(), "/文案:", officialDo.getName(), "<br/>属性:",
							getPeopleTypeStr(serviceOrderDo.getPeopleType()), "<br/>坚果云资料地址:",
							serviceOrderDo.getNutCloud(), "<br/>客户基本信息:", serviceOrderDo.getInformation(), "<br/>备注:",
							serviceOrderDo.getRemarks(), "<br/>驳回原因:", serviceOrderDo.getRefuseReason(), "<br/>创建时间:",
							date, "<br/>", serviceOrderMailDetail.getServiceOrderUrl()));
				}
				// 写入审核时间
				if (serviceOrderDo.getMaraApprovalDate() == null)
					serviceOrderDo.setMaraApprovalDate(new Date());
			}
			if ("REVIEW".equals(state)) { // 给文案发邮件提醒，这时adviserState为REVIEW,officialState为NULL
				sendMail(officialDo.getEmail() + ",maggie@zhinanzhen.org", serviceOrderMailDetail.getTitle(),
						StringUtil.merge("亲爱的", officialDo.getName(), ":<br/>您有一条新的服务订单任务请及时处理。<br/>订单号:", id,
								"<br/>服务类型:", serviceOrderMailDetail.getType(), serviceOrderMailDetail.getDetail(),
								"/顾问:", adviserDo.getName(), "/文案:", officialDo.getName(), "<br/>属性:",
								getPeopleTypeStr(serviceOrderDo.getPeopleType()), "<br/>坚果云资料地址:",
								serviceOrderDo.getNutCloud(), "<br/>客户基本信息:", serviceOrderDo.getInformation(),
								"<br/>备注:", serviceOrderDo.getRemarks(), "<br/>驳回原因:", serviceOrderDo.getRefuseReason(),
								"<br/>创建时间:", date, "<br/>", serviceOrderMailDetail.getServiceOrderUrl()));
				// 写入文案审核时间
				if (serviceOrderDo.getOfficialApprovalDate() == null)
					serviceOrderDo.setOfficialApprovalDate(new Date());
			}
			if ("OREVIEW".equals(state)) { // 告诉顾问文案已经开始审核了
				sendMail(adviserDo.getEmail(), serviceOrderMailDetail.getTitle(),
						StringUtil.merge("亲爱的", adviserDo.getName(), ":<br/>您有一条服务订单已正在处理中。<br/>订单号:", id, "/服务类型:",
								serviceOrderMailDetail.getType(), serviceOrderMailDetail.getDetail(), "/顾问:",
								adviserDo.getName(), "/文案:", officialDo.getName(), "<br/>创建时间:", date, "<br/>属性:",
								getPeopleTypeStr(serviceOrderDo.getPeopleType()), "<br/>坚果云资料地址:",
								serviceOrderDo.getNutCloud(), "<br/>坚果云资料地址:", serviceOrderDo.getNutCloud(),
								"<br/>客户基本信息:", serviceOrderDo.getInformation(), "<br/>备注:",
								serviceOrderDo.getRemarks(), "<br/>驳回原因:", serviceOrderDo.getRefuseReason(), "<br/>",
								serviceOrderMailDetail.getServiceOrderUrl()));
			}
			if ("COMPLETE".equals(state)) {
				if ("VISA".equalsIgnoreCase(serviceOrderDo.getType())) {
					String _title = StringUtil.merge("审核完成提醒:", user.getName(), "/签证");
					// 发送给顾问
					sendMail(adviserDo.getEmail(), _title, StringUtil.merge("亲爱的:", adviserDo.getName(), "<br/>",
							"您的订单已经申请成功，请检查并进行下一步操作。<br>订单号:", serviceOrderDo.getId(), "/服务类型:签证/客户名称:", user.getName(),
							"/顾问:", adviserDo.getName(), "/文案:", officialDo.getName(), "<br/>属性:",
							getPeopleTypeStr(serviceOrderDo.getPeopleType()), "<br/>坚果云资料地址:",
							serviceOrderDo.getNutCloud(), "<br/>客户基本信息:", serviceOrderDo.getInformation(), "<br/>备注:",
							serviceOrderDo.getRemarks(), "<br/>驳回原因:", serviceOrderDo.getRefuseReason(), "<br/>创建时间:",
							date, "<br/>", serviceOrderMailDetail.getServiceOrderUrl()));
					MaraDO maraDo = maraDao.getMaraById(serviceOrderDo.getMaraId());
					if (maraDo != null) {
						// 发送给MARA
						sendMail(maraDo.getEmail(), _title, StringUtil.merge("亲爱的:", maraDo.getName(), "<br/>",
								"您的订单已经申请成功，请检查并进行下一步操作。<br>订单号:", serviceOrderDo.getId(), "<br/>服务类型:签证/客户名称:",
								user.getName(), "/顾问:", adviserDo.getName(), "/文案:", officialDo.getName(), "<br/>属性:",
								getPeopleTypeStr(serviceOrderDo.getPeopleType()), "<br/>坚果云资料地址:",
								serviceOrderDo.getNutCloud(), "<br/>客户基本信息:", serviceOrderDo.getInformation(),
								"<br/>备注:", serviceOrderDo.getRemarks(), "<br/>驳回原因:", serviceOrderDo.getRefuseReason(),
								"<br/>创建时间:", date, "<br/>", serviceOrderMailDetail.getServiceOrderUrl()));
					}

				}

				if ("OVST".equalsIgnoreCase(serviceOrderDo.getType())) {
					String _title = StringUtil.merge("审核完成提醒:", user.getName(), "/留学");
					// 发送给顾问
					SchoolDO schoolDo = schoolDao.getSchoolById(serviceOrderDo.getSchoolId());
					if (schoolDo != null)
						sendMail(adviserDo.getEmail(), _title, StringUtil.merge("亲爱的:", adviserDo.getName(), "<br/>",
								"您的订单已经申请成功等待coe支付，请检查并进行下一步操作。<br>订单号:", serviceOrderDo.getId(), "<br/>服务类型:留学/客户名称:",
								user.getName(), "/学校:", schoolDo.getName(), "/专业:", schoolDo.getSubject(), "/顾问:",
								adviserDo.getName(), "/文案:", officialDo.getName(), "<br/>属性:",
								getPeopleTypeStr(serviceOrderDo.getPeopleType()), "<br/>坚果云资料地址:",
								serviceOrderDo.getNutCloud(), "<br/>客户基本信息:", serviceOrderDo.getInformation(),
								"<br/>备注:", serviceOrderDo.getRemarks(), "<br/>驳回原因:", serviceOrderDo.getRefuseReason(),
								"<br/>创建时间:", date, "<br/>", serviceOrderMailDetail.getServiceOrderUrl()));
				}

			}
			if ("PAID".equals(state)) {
				// 写入会计审核时间
				if ("VISA".equalsIgnoreCase(serviceOrderDo.getType())
						|| "SIV".equalsIgnoreCase(serviceOrderDo.getType()))
					visaDao.listVisaByServiceOrderId(serviceOrderDo.getId()).forEach(visaDo -> {
						if (visaDo.getKjApprovalDate() == null) {
							visaDo.setKjApprovalDate(new Date());
							visaDao.updateVisa(visaDo);
						}
					});
				if ("OVST".equalsIgnoreCase(serviceOrderDo.getType())) {
					commissionOrderDao.listCommissionOrderByServiceOrderId(serviceOrderDo.getId())
							.forEach(commissionOrderDo -> {
								if (commissionOrderDo.getKjApprovalDate() == null) {
									commissionOrderDo.setKjApprovalDate(new Date());
									commissionOrderDao.updateCommissionOrder(commissionOrderDo);
								}
							});
					if ("PAID".equals(state)) {
						String _title = StringUtil.merge("审核完成提醒:", user.getName(), "/留学");
						// 发送给顾问
						SchoolDO schoolDo = schoolDao.getSchoolById(serviceOrderDo.getSchoolId());
						if (schoolDo != null)
							sendMail(adviserDo.getEmail(), _title, StringUtil.merge("亲爱的:", adviserDo.getName(),
									"<br/>", "您的订单已经申请成功coe支付成功，请检查并进行下一步操作。<br>订单号:", serviceOrderDo.getId(),
									"<br/>服务类型:留学/客户名称:", user.getName(), "/学校:", schoolDo.getName(), "/专业:",
									schoolDo.getSubject(), "/顾问:", adviserDo.getName(), "/文案:", officialDo.getName(),
									"<br/>属性:", getPeopleTypeStr(serviceOrderDo.getPeopleType()), "<br/>坚果云资料地址:",
									serviceOrderDo.getNutCloud(), "<br/>客户基本信息:", serviceOrderDo.getInformation(),
									"<br/>备注:", serviceOrderDo.getRemarks(), "<br/>驳回原因:",
									serviceOrderDo.getRefuseReason(), "<br/>创建时间:", date, "<br/>",
									serviceOrderMailDetail.getServiceOrderUrl()));
					}
				}
			}
			serviceOrderDao.updateServiceOrder(serviceOrderDo);
		}
	}

	@Deprecated
	public void sendRemind(int id, String adviserState, String maraState, String officialState) {
		ServiceOrderDO serviceOrderDo = serviceOrderDao.getServiceOrderById(id);
		if (serviceOrderDo != null) {
			ServiceOrderMailDetail serviceOrderMailDetail = getServiceOrderMailDetail(serviceOrderDo, "新任务提醒:");
			UserDO user = serviceOrderMailDetail.getUser();
			AdviserDO adviserDo = adviserDao.getAdviserById(serviceOrderDo.getAdviserId());
			OfficialDO officialDo = officialDao.getOfficialById(serviceOrderDo.getOfficialId());
			Date date = serviceOrderDo.getGmtCreate();
			if (adviserDo != null && officialDo != null) {
				if ("REVIEW".equals(maraState) || "WAIT".equals(maraState)) {
					MaraDO maraDo = maraDao.getMaraById(serviceOrderDo.getMaraId());
					if (maraDo != null)
						sendMail(maraDo.getEmail(), serviceOrderMailDetail.getTitle(), StringUtil.merge("亲爱的",
								maraDo.getName(), ":<br/>您有一条新的服务订单任务请及时处理。<br/>订单号:", id, "<br/>服务类型:",
								serviceOrderMailDetail.getType(), serviceOrderMailDetail.getDetail(),
								"/顾问:" + adviserDo.getName() + "/文案:" + officialDo.getName(), "<br/>属性:",
								getPeopleTypeStr(serviceOrderDo.getPeopleType()), "<br/>坚果云资料地址:",
								serviceOrderDo.getNutCloud(), "<br/>客户基本信息:", serviceOrderDo.getInformation(),
								"<br/>备注:", serviceOrderDo.getRemarks(), "<br/>驳回原因:", serviceOrderDo.getRefuseReason(),
								"<br/>创建时间:", date, "<br/>", serviceOrderMailDetail.getServiceOrderUrl()));
				}
				if ("VISA".equalsIgnoreCase(serviceOrderDo.getType()) && "FINISH".equals(maraState)) {
					String _title = StringUtil.merge("MARA审核通过提醒:", user.getName(), "/签证");
					// 发送给顾问
					sendMail(adviserDo.getEmail(), _title, StringUtil.merge("亲爱的:", adviserDo.getName(),
							"<br/>", "您的订单已经审核完成请查看并进行下一步操作。<br>订单号:", serviceOrderDo.getId(), "<br/>服务类型:签证/客户名称:",
							user.getName(), "/顾问:", adviserDo.getName(), "/文案:", officialDo.getName(), "<br/>属性:",
							getPeopleTypeStr(serviceOrderDo.getPeopleType()), "<br/>坚果云资料地址:",
							serviceOrderDo.getNutCloud(), "<br/>客户基本信息:", serviceOrderDo.getInformation(), "<br/>备注:",
							serviceOrderDo.getRemarks(), "<br/>驳回原因:", serviceOrderDo.getRefuseReason(), "<br/>创建时间:",
							date, "<br/>", serviceOrderMailDetail.getServiceOrderUrl()));
					// 发送给文案
					sendMail(officialDo.getEmail(), _title, StringUtil.merge("亲爱的:", officialDo.getName(),
							"<br/>", "您的订单已经审核完成请查看并进行下一步操作。<br>订单号:", serviceOrderDo.getId(), "<br/>服务类型:签证/客户名称:",
							user.getName(), "/顾问:", adviserDo.getName(), "/文案:", officialDo.getName(), "<br/>属性:",
							getPeopleTypeStr(serviceOrderDo.getPeopleType()), "<br/>坚果云资料地址:",
							serviceOrderDo.getNutCloud(), "<br/>客户基本信息:", serviceOrderDo.getInformation(), "<br/>备注:",
							serviceOrderDo.getRemarks(), "<br/>驳回原因:", serviceOrderDo.getRefuseReason(), "<br/>创建时间:",
							date, "<br/>", serviceOrderMailDetail.getServiceOrderUrl()));
					// 写入审核时间
					if (serviceOrderDo.getMaraApprovalDate() == null)
						serviceOrderDo.setMaraApprovalDate(new Date());
				}
				if ("REVIEW".equals(adviserState)) { // 给文案发邮件提醒，这时adviserState为REVIEW,officialState为NULL
					sendMail(officialDo.getEmail() + ",maggie@zhinanzhen.org",
							serviceOrderMailDetail.getTitle(),
							StringUtil.merge("亲爱的", officialDo.getName(), ":<br/>您有一条新的服务订单任务请及时处理。<br/>订单号:", id,
									"<br/>服务类型:", serviceOrderMailDetail.getType(), serviceOrderMailDetail.getDetail(),
									"/顾问:", adviserDo.getName(), "/文案:", officialDo.getName(), "<br/>属性:",
									getPeopleTypeStr(serviceOrderDo.getPeopleType()), "<br/>坚果云资料地址:",
									serviceOrderDo.getNutCloud(), "<br/>客户基本信息:", serviceOrderDo.getInformation(),
									"<br/>备注:", serviceOrderDo.getRemarks(), "<br/>驳回原因:",
									serviceOrderDo.getRefuseReason(), "<br/>创建时间:", date, "<br/>",
									serviceOrderMailDetail.getServiceOrderUrl()));
					// 写入文案审核时间
					if (serviceOrderDo.getOfficialApprovalDate() == null)
						serviceOrderDo.setOfficialApprovalDate(new Date());
				}
				if ("REVIEW".equals(officialState)) { // 告诉顾问文案已经开始审核了
					sendMail(adviserDo.getEmail(), serviceOrderMailDetail.getTitle(),
							StringUtil.merge("亲爱的", adviserDo.getName(), ":<br/>您有一条服务订单已正在处理中。<br/>订单号:", id,
									"<br/>服务类型:", serviceOrderMailDetail.getType(), serviceOrderMailDetail.getDetail(),
									"/顾问:", adviserDo.getName(), "/文案:", officialDo.getName(), "<br/>属性:",
									getPeopleTypeStr(serviceOrderDo.getPeopleType()), "<br/>坚果云资料地址:",
									serviceOrderDo.getNutCloud(), "<br/>客户基本信息:", serviceOrderDo.getInformation(),
									"<br/>备注:", serviceOrderDo.getRemarks(), "<br/>驳回原因:",
									serviceOrderDo.getRefuseReason(), "<br/>创建时间:", date, "<br/>",
									serviceOrderMailDetail.getServiceOrderUrl()));
				}
				if ("COMPLETE".equals(officialState)) {
					if ("VISA".equalsIgnoreCase(serviceOrderDo.getType())) {
						String _title = StringUtil.merge("审核完成提醒:", user.getName(), "/签证");
						// 发送给顾问
						sendMail(adviserDo.getEmail(), _title,
								StringUtil.merge("亲爱的:", adviserDo.getName(), "<br/>",
										"您的订单已经申请成功，请检查并进行下一步操作。<br>订单号:", serviceOrderDo.getId(), "<br/>服务类型:签证/客户名称:",
										user.getName(), "/顾问:", adviserDo.getName(), "/文案:", officialDo.getName(),
										"<br/>属性:", getPeopleTypeStr(serviceOrderDo.getPeopleType()), "<br/>坚果云资料地址:",
										serviceOrderDo.getNutCloud(), "<br/>客户基本信息:", serviceOrderDo.getInformation(),
										"<br/>备注:", serviceOrderDo.getRemarks(), "<br/>驳回原因:",
										serviceOrderDo.getRefuseReason(), "<br/>创建时间:", date, "<br/>",
										serviceOrderMailDetail.getServiceOrderUrl()));
						MaraDO maraDo = maraDao.getMaraById(serviceOrderDo.getMaraId());
						if (maraDo != null) {
							// 发送给MARA
							sendMail(maraDo.getEmail(), _title, StringUtil.merge("亲爱的:", maraDo.getName(),
									"<br/>", "您的订单已经申请成功，请检查并进行下一步操作。<br>订单号:", serviceOrderDo.getId(),
									"<br/>服务类型:签证/客户名称:", user.getName(), "/顾问:", adviserDo.getName(), "/文案:",
									officialDo.getName(), "<br/>属性:", getPeopleTypeStr(serviceOrderDo.getPeopleType()),
									"<br/>坚果云资料地址:", serviceOrderDo.getNutCloud(), "<br/>客户基本信息:",
									serviceOrderDo.getInformation(), "<br/>备注:", serviceOrderDo.getRemarks(),
									"<br/>驳回原因:", serviceOrderDo.getRefuseReason(), "<br/>创建时间:", date, "<br/>",
									serviceOrderMailDetail.getServiceOrderUrl()));
						}

					}

					if ("OVST".equalsIgnoreCase(serviceOrderDo.getType())) {
						String _title = StringUtil.merge("审核完成提醒:", user.getName(), "/留学");
						// 发送给顾问
						SchoolDO schoolDo = schoolDao.getSchoolById(serviceOrderDo.getSchoolId());
						if (schoolDo != null)
							sendMail(adviserDo.getEmail(), _title,
									StringUtil.merge("亲爱的:", adviserDo.getName(), "<br/>",
											"您的订单已经申请成功等待coe支付，请检查并进行下一步操作。<br>订单号:", serviceOrderDo.getId(),
											"<br/>服务类型:留学/客户名称:", user.getName(), "/学校:", schoolDo.getName(), "/专业:",
											schoolDo.getSubject(), "/顾问:", adviserDo.getName(), "/文案:",
											officialDo.getName(), "<br/>属性:",
											getPeopleTypeStr(serviceOrderDo.getPeopleType()), "<br/>坚果云资料地址:",
											serviceOrderDo.getNutCloud(), "<br/>客户基本信息:",
											serviceOrderDo.getInformation(), "<br/>备注:", serviceOrderDo.getRemarks(),
											"<br/>驳回原因:", serviceOrderDo.getRefuseReason(), "<br/>创建时间:", date, "<br/>",
											serviceOrderMailDetail.getServiceOrderUrl()));
					}

				}
				if ("PAID".equals(adviserState)) {
					// 写入会计审核时间
					if ("VISA".equalsIgnoreCase(serviceOrderDo.getType())
							|| "SIV".equalsIgnoreCase(serviceOrderDo.getType()))
						visaDao.listVisaByServiceOrderId(serviceOrderDo.getId()).forEach(visaDo -> {
							if (visaDo.getKjApprovalDate() == null) {
								visaDo.setKjApprovalDate(new Date());
								visaDao.updateVisa(visaDo);
							}
						});
					if ("OVST".equalsIgnoreCase(serviceOrderDo.getType())) {
						commissionOrderDao.listCommissionOrderByServiceOrderId(serviceOrderDo.getId())
								.forEach(commissionOrderDo -> {
									if (commissionOrderDo.getKjApprovalDate() == null) {
										commissionOrderDo.setKjApprovalDate(new Date());
										commissionOrderDao.updateCommissionOrder(commissionOrderDo);
									}
								});
						if ("PAID".equals(officialState)) {
							String _title = StringUtil.merge("审核完成提醒:", user.getName(), "/留学");
							// 发送给顾问
							SchoolDO schoolDo = schoolDao.getSchoolById(serviceOrderDo.getSchoolId());
							if (schoolDo != null)
								sendMail(adviserDo.getEmail(), _title, StringUtil.merge("亲爱的:",
										adviserDo.getName(), "<br/>", "您的订单已经申请成功coe支付成功，请检查并进行下一步操作。<br>订单号:",
										serviceOrderDo.getId(), "<br/>服务类型:留学/客户名称:", user.getName(), "/学校:",
										schoolDo.getName(), "/专业:", schoolDo.getSubject(), "/顾问:", adviserDo.getName(),
										"/文案:", officialDo.getName(), "<br/>属性:",
										getPeopleTypeStr(serviceOrderDo.getPeopleType()), "<br/>坚果云资料地址:",
										serviceOrderDo.getNutCloud(), "<br/>客户基本信息:", serviceOrderDo.getInformation(),
										"<br/>备注:", serviceOrderDo.getRemarks(), "<br/>驳回原因:",
										serviceOrderDo.getRefuseReason(), "<br/>创建时间:", date, "<br/>",
										serviceOrderMailDetail.getServiceOrderUrl()));
						}
					}
				}
				serviceOrderDao.updateServiceOrder(serviceOrderDo);
			}
		}
	}

	@Deprecated
	public void sendRemind2(int id, String adviserState, String maraState, String officialState) {
		ServiceOrderDO serviceOrderDo = serviceOrderDao.getServiceOrderById(id);
		if (serviceOrderDo != null) {
			ServiceOrderMailDetail serviceOrderMailDetail = getServiceOrderMailDetail(serviceOrderDo, "订单驳回提醒:");
			UserDO user = serviceOrderMailDetail.getUser();
			AdviserDO adviserDo = adviserDao.getAdviserById(serviceOrderDo.getAdviserId());
			OfficialDO officialDo = officialDao.getOfficialById(serviceOrderDo.getOfficialId());
			Date date = serviceOrderDo.getGmtCreate();
			if (adviserDo != null && officialDo != null) {
				if ("REVIEW".equals(adviserState)) {
					// 发送给顾问
					sendMail(adviserDo.getEmail(), serviceOrderMailDetail.getTitle(),
							StringUtil.merge("亲爱的:", adviserDo.getName(), "<br/>", "您的订单已被驳回。<br>订单号:",
									serviceOrderDo.getId(), "<br/>服务类型:签证/客户名称:", user.getName(), "/顾问:",
									adviserDo.getName(), "/文案:", officialDo.getName(), "<br/>属性:",
									getPeopleTypeStr(serviceOrderDo.getPeopleType()), "<br/>坚果云资料地址:",
									serviceOrderDo.getNutCloud(), "<br/>客户基本信息:", serviceOrderDo.getInformation(),
									"<br/>备注:", serviceOrderDo.getRemarks(), "<br/>驳回原因:",
									serviceOrderDo.getRefuseReason(), "<br/>创建时间:", date, "<br/>",
									serviceOrderMailDetail.getServiceOrderUrl()));
				}
				if ("REVIEW".equals(officialState)) {
					// 发送给文案
					sendMail(officialDo.getEmail(), serviceOrderMailDetail.getTitle(),
							StringUtil.merge("亲爱的:", officialDo.getName(), "<br/>", "您的订单已被驳回。<br>订单号:",
									serviceOrderDo.getId(), "<br/>服务类型:签证/客户名称:", user.getName(), "/顾问:",
									adviserDo.getName(), "/文案:", officialDo.getName(), "<br/>属性:",
									getPeopleTypeStr(serviceOrderDo.getPeopleType()), "<br/>坚果云资料地址:",
									serviceOrderDo.getNutCloud(), "<br/>客户基本信息:", serviceOrderDo.getInformation(),
									"<br/>备注:", serviceOrderDo.getRemarks(), "<br/>驳回原因:",
									serviceOrderDo.getRefuseReason(), "<br/>创建时间:", date, "<br/>",
									serviceOrderMailDetail.getServiceOrderUrl()));
				}
				if ("PENDING".equals(adviserState)) { // 文案驳回
					sendMail(adviserDo.getEmail(), serviceOrderMailDetail.getTitle(),
							StringUtil.merge("亲爱的", adviserDo.getName(), ":<br/>您有一条服务订单已被驳回。<br/>订单号:", id,
									"<br/>服务类型:", serviceOrderMailDetail.getType(), serviceOrderMailDetail.getDetail(),
									"/顾问:", adviserDo.getName(), "/文案:", officialDo.getName(), "<br/>属性:",
									getPeopleTypeStr(serviceOrderDo.getPeopleType()), "<br/>坚果云资料地址:",
									serviceOrderDo.getNutCloud(), "<br/>客户基本信息:", serviceOrderDo.getInformation(),
									"<br/>备注:", serviceOrderDo.getRemarks(), "<br/>驳回原因:",
									serviceOrderDo.getRefuseReason(), "<br/>创建时间:", date, "<br/>",
									serviceOrderMailDetail.getServiceOrderUrl()));
				}
				serviceOrderDao.updateServiceOrder(serviceOrderDo);
			}
		}
	}

	private ServiceOrderMailDetail getServiceOrderMailDetail(ServiceOrderDO serviceOrderDo, String title) {
		ServiceOrderMailDetail serviceOrderMailDetail = new ServiceOrderMailDetail();
		String type = "";
		String detail = "";
		serviceOrderMailDetail.setServiceOrderUrl(
				"<br/><a href='https://yongjinbiao.zhinanzhen.org/webroot_new/serviceorderdetail/id?"
						+ serviceOrderDo.getId() + "'>服务订单详情</a>");
		UserDO user = userDao.getUserById(serviceOrderDo.getUserId());
		if ("VISA".equalsIgnoreCase(serviceOrderDo.getType())) {
			type = "签证";
			if (user != null)
				detail += "/客户名称:" + user.getName();
			ServiceDO service = serviceDao.getServiceById(serviceOrderDo.getServiceId());
			if (service != null) {
				detail += "/类型:" + service.getName() + "(" + service.getCode() + ")";
				type += "(" + service.getCode() + ")";
			}
			title += user.getName() + "/" + type;
		} else if ("OVST".equalsIgnoreCase(serviceOrderDo.getType())) {
			type = "留学";
			if (user != null)
				detail += "/客户名称:" + user.getName();
			SchoolDO school = schoolDao.getSchoolById(serviceOrderDo.getSchoolId());
			if (school != null) {
				detail += "/学校:" + school.getName();
				detail += "/专业:" + school.getSubject();
			}
			title += user.getName() + "/" + type;
		} else if ("SIV".equalsIgnoreCase(serviceOrderDo.getType())) {
			type = "独立技术移民";
			title += user.getName() + "/" + type;
		} else if ("MT".equalsIgnoreCase(serviceOrderDo.getType())) {
			type = "曼拓";
			title += user.getName() + "/" + type;
		} else if ("ZX".equalsIgnoreCase(serviceOrderDo.getType())) {
			type = "咨询";
			title += user.getName() + "/" + type;
		}
		serviceOrderMailDetail.setTitle(title);
		serviceOrderMailDetail.setType(type);
		serviceOrderMailDetail.setDetail(detail);
		serviceOrderMailDetail.setUser(user);
		return serviceOrderMailDetail;
	}

	@Data
	class ServiceOrderMailDetail {
		String title = "";
		String type = "";
		String detail = "";
		String serviceOrderUrl = "";
		UserDO user;
	}

	@Override
	@Deprecated
	public List<ServiceOrderReviewDTO> reviews(int serviceOrderId) throws ServiceException {
		List<ServiceOrderReviewDTO> serviceOrderReviewDtoList = new ArrayList<>();
//		List<ServiceOrderReviewDO> serviceOrderReviewDoList = serviceOrderReviewDao
//				.listServiceOrderReview(serviceOrderId, null, null, null, null, null);
//		serviceOrderReviewDoList.forEach(serviceOrderReviewDo -> serviceOrderReviewDtoList
//				.add(mapper.map(serviceOrderReviewDo, ServiceOrderReviewDTO.class)));
		return serviceOrderReviewDtoList;
	}

	@Deprecated
	private void putReviews(ServiceOrderDTO serviceOrderDto) {
//		List<ServiceOrderReviewDO> serviceOrderReviewDoList = serviceOrderReviewDao
//				.listServiceOrderReview(serviceOrderDto.getId(), null, null, null, null, null);
//		List<ServiceOrderReviewDTO> serviceOrderReviewDtoList = new ArrayList<ServiceOrderReviewDTO>();
//		serviceOrderReviewDoList
//				.forEach(review -> serviceOrderReviewDtoList.add(mapper.map(review, ServiceOrderReviewDTO.class)));
//		serviceOrderDto.setReviews(serviceOrderReviewDtoList);
//		if (serviceOrderReviewDtoList != null && serviceOrderReviewDtoList.size() > 0)
//			serviceOrderDto.setReview(serviceOrderReviewDtoList.get(0));
//		if (serviceOrderDto.getReview() != null && serviceOrderDto.getReview().getAdviserState() != null) {
//			ServiceOrderDO serviceOrderDo = serviceOrderDao.getServiceOrderById(serviceOrderDto.getId());
//			serviceOrderDo.setState(serviceOrderDto.getReview().getAdviserState());
//			serviceOrderDao.updateServiceOrder(serviceOrderDo);
//		}
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
		if (officialState != null) {
			serviceOrderReviewDo.setOfficialState(officialState);
			if ("CLOSE".equalsIgnoreCase(officialState)) {
				ServiceOrderDO serviceOrderDo = serviceOrderDao.getServiceOrderById(id);
				AdviserDO adviserDo = adviserDao.getAdviserById(serviceOrderDo.getAdviserId());
				OfficialDO officialDo = officialDao.getOfficialById(serviceOrderDo.getOfficialId());
				Date date = serviceOrderDo.getGmtCreate();
				String serviceOrderUrl = "<br/><a href='https://yongjinbiao.zhinanzhen.org/webroot/serviceorder-detail.html?id="
						+ serviceOrderDo.getId() + "'>服务订单详情</a>";
				UserDO user = userDao.getUserById(serviceOrderDo.getUserId());
				if ("VISA".equalsIgnoreCase(serviceOrderDo.getType())) {
					String _title = StringUtil.merge("审核完成提醒:", user.getName(), "/签证");
					// 发送给顾问
					sendMail(adviserDo.getEmail(), _title,
							StringUtil.merge("亲爱的:", adviserDo.getName(), "<br/>", "您的订单已经关闭申请，请检查并进行下一步操作。<br>订单号:",
									serviceOrderDo.getId(), "/服务类型:签证/客户名称:", user.getName(), "/顾问:",
									adviserDo.getName(), "/文案:", officialDo.getName(), "/创建时间:", date, "/备注:",
									serviceOrderDo.getRemarks(), "<br/>", serviceOrderUrl));
					MaraDO maraDo = maraDao.getMaraById(serviceOrderDo.getMaraId());
					if (maraDo != null) {
						// 发送给MARA
						sendMail(maraDo.getEmail(), _title,
								StringUtil.merge("亲爱的:", maraDo.getName(), "<br/>", "您的订单已经关闭申请，请检查并进行下一步操作。<br>订单号:",
										serviceOrderDo.getId(), "/服务类型:签证/客户名称:", user.getName(), "/顾问:",
										adviserDo.getName(), "/文案:", officialDo.getName(), "/创建时间:", date, "/备注:",
										serviceOrderDo.getRemarks(), "<br/>", serviceOrderUrl));
					}
				}
			}
		}
		if (kjState != null)
			serviceOrderReviewDo.setKjState(kjState);
//		serviceOrderReviewDao.addServiceOrderReview(serviceOrderReviewDo);
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

	@Override
	public int addOfficialRemarks(ServiceOrderOfficialRemarksDTO serviceOrderOfficialRemarksDto)
			throws ServiceException {
		if (serviceOrderOfficialRemarksDto == null) {
			ServiceException se = new ServiceException("serviceOrderOfficialRemarksDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			ServiceOrderOfficialRemarksDO serviceOrderOfficialRemarksDo = mapper.map(serviceOrderOfficialRemarksDto,
					ServiceOrderOfficialRemarksDO.class);
			if (serviceOrderOfficialRemarksDao.add(serviceOrderOfficialRemarksDo) > 0)
				return serviceOrderOfficialRemarksDo.getId();
			else
				return 0;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int updateOfficialRemarks(ServiceOrderOfficialRemarksDTO serviceOrderOfficialRemarksDto)
			throws ServiceException {
		if (serviceOrderOfficialRemarksDto == null) {
			ServiceException se = new ServiceException("serviceOrderOfficialRemarksDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			if (serviceOrderOfficialRemarksDao
					.update(mapper.map(serviceOrderOfficialRemarksDto, ServiceOrderOfficialRemarksDO.class)) > 0)
				return serviceOrderOfficialRemarksDto.getId();
			else
				return 0;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public List<ServiceOrderOfficialRemarksDTO> listOfficialRemarks(int id, int officialId) throws ServiceException {
		List<ServiceOrderOfficialRemarksDTO> serviceOrderOfficialRemarksDtoList = new ArrayList<>();
		List<ServiceOrderOfficialRemarksDO> serviceOrderOfficialRemarksDoList = new ArrayList<>();
		try {
			serviceOrderOfficialRemarksDoList = serviceOrderOfficialRemarksDao.list(officialId, id);
			if (serviceOrderOfficialRemarksDoList == null)
				return null;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (ServiceOrderOfficialRemarksDO serviceOrderOfficialRemarksDo : serviceOrderOfficialRemarksDoList)
			serviceOrderOfficialRemarksDtoList
					.add(mapper.map(serviceOrderOfficialRemarksDo, ServiceOrderOfficialRemarksDTO.class));
		return serviceOrderOfficialRemarksDtoList;
	}

	@Override
	public int deleteServiceOrderOfficialRemarksDTO(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			return serviceOrderOfficialRemarksDao.delete(id);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public List<EachRegionNumberDTO> listServiceOrderGroupByForRegion(String type, String startOfficialApprovalDate,
			String endOfficialApprovalDate) {
		List<EachRegionNumberDO> eachRegionNumberDOS = serviceOrderDao.listServiceOrderGroupByForRegion(type,
				startOfficialApprovalDate, theDateTo23_59_59(endOfficialApprovalDate));
		List<EachRegionNumberDTO> eachRegionNumberDTOS = new ArrayList<>();
		Set<String> codeSet = new HashSet<>();
		eachRegionNumberDOS.forEach(eachRegionNumberDO -> {
			codeSet.add(eachRegionNumberDO.getCode());
		});
		for (String code : codeSet) {
			EachRegionNumberDTO eachRegionNumberDTO = new EachRegionNumberDTO();
			eachRegionNumberDOS.forEach(eachRegionNumberDO -> {
				if (eachRegionNumberDO.getCode().equalsIgnoreCase(code)) {
					if (eachRegionNumberDO.getName().equalsIgnoreCase("sydney")) {
						eachRegionNumberDTO.setSydney(eachRegionNumberDO.getCount() + eachRegionNumberDTO.getSydney());
					} else if (eachRegionNumberDO.getName().equalsIgnoreCase("melbourne")) {
						eachRegionNumberDTO
								.setMelbourne(eachRegionNumberDO.getCount() + eachRegionNumberDTO.getMelbourne());
					} else if (eachRegionNumberDO.getName().equalsIgnoreCase("brisbane")) {
						eachRegionNumberDTO
								.setBrisbane(eachRegionNumberDO.getCount() + eachRegionNumberDTO.getBrisbane());
					} else if (eachRegionNumberDO.getName().equalsIgnoreCase("adelaide")) {
						eachRegionNumberDTO
								.setAdelaide(eachRegionNumberDO.getCount() + eachRegionNumberDTO.getAdelaide());
					} else if (eachRegionNumberDO.getName().equalsIgnoreCase("hobart")) {
						eachRegionNumberDTO.setHobart(eachRegionNumberDO.getCount() + eachRegionNumberDTO.getHobart());
					} else if (eachRegionNumberDO.getName().equalsIgnoreCase("canberra")) {
						eachRegionNumberDTO
								.setCanberra(eachRegionNumberDO.getCount() + eachRegionNumberDTO.getCanberra());
					} else if (eachRegionNumberDO.getName().equalsIgnoreCase("sydney2")) {
						eachRegionNumberDTO
								.setSydney2(eachRegionNumberDO.getCount() + eachRegionNumberDTO.getSydney2());
					} else
						eachRegionNumberDTO.setOther(eachRegionNumberDO.getCount() + eachRegionNumberDTO.getOther());
				}
			});
			eachRegionNumberDTO.setTotal(eachRegionNumberDTO.getAdelaide() + eachRegionNumberDTO.getSydney()
					+ eachRegionNumberDTO.getBrisbane() + eachRegionNumberDTO.getCanberra()
					+ eachRegionNumberDTO.getHobart() + eachRegionNumberDTO.getMelbourne()
					+ eachRegionNumberDTO.getSydney2() + eachRegionNumberDTO.getOther());
			eachRegionNumberDTO.setName(code);
			eachRegionNumberDTOS.add(eachRegionNumberDTO);
		}
		Collections.sort(eachRegionNumberDTOS, new Comparator<EachRegionNumberDTO>() {
			@Override
			public int compare(EachRegionNumberDTO o1, EachRegionNumberDTO o2) {
				if (o1.getTotal() > o2.getTotal())
					return -1;
				else if (o1.getTotal() < o2.getTotal())
					return 1;
				else
					return 0;
			}
		});
		return eachRegionNumberDTOS;
	}

	@Override
	public List<EachSubjectCountDTO> eachSubjectCount(String startOfficialApprovalDate, String endOfficialApprovalDate) {
		List<EachSubjectCountDO> eachSubjectCountDOList = serviceOrderDao.eachSubjectCount(startOfficialApprovalDate,theDateTo23_59_59(endOfficialApprovalDate));
		List<EachSubjectCountDTO> subjectCountDTOList = new ArrayList<>();
		HashSet<String> nameSet = new HashSet();
		eachSubjectCountDOList.forEach(eachSubjectCountDO -> {
			nameSet.add(eachSubjectCountDO.getName());
		});
		for (String name : nameSet){
			EachSubjectCountDTO dTO = new EachSubjectCountDTO();
			dTO.setName(name);
			List<EachSubjectCountDTO.Subject> subjects = new ArrayList<>();

			eachSubjectCountDOList.forEach(eachSubjectCountDO ->{
				if (name.equalsIgnoreCase(eachSubjectCountDO.getName())){
					dTO.setTotal(dTO.getTotal() + eachSubjectCountDO.getNumber());
					EachSubjectCountDTO.Subject subject = dTO.new Subject();
					subject.setNumber(eachSubjectCountDO.getNumber());
					subject.setSubjectName(eachSubjectCountDO.getSubject());
					subjects.add(subject);
				}
			});
			dTO.setSubject(subjects);
			subjectCountDTOList.add(dTO);
		}
		return  subjectCountDTOList;
	}

	@Override
	public List<ServiceOrderDTO> NotReviewedServiceOrder(Integer officialId,boolean thisMonth) {
		List<ServiceOrderDO>  serviceOrderDOList = serviceOrderDao.NotReviewedServiceOrder(officialId,thisMonth);
		List<ServiceOrderDTO> serviceOrderDTOList = new ArrayList<>();
		copyServiceOrder(serviceOrderDOList,serviceOrderDTOList);
		return  serviceOrderDTOList;
	}

	public void copyServiceOrder(List<ServiceOrderDO>  serviceOrderDOList ,List<ServiceOrderDTO> serviceOrderDTOList){
		serviceOrderDOList.forEach(serviceOrderDo -> {
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
			// 查询文案Tag
			OfficialTagDO officialTagDo = officialTagDao.getOfficialTagByServiceOrderId(serviceOrderDto.getId());
			if (officialTagDo != null)
				serviceOrderDto.setOfficialTag(mapper.map(officialTagDo, OfficialTagDTO.class));
			// 查询子服务
			if (serviceOrderDto.getParentId() <= 0) {
				List<ChildrenServiceOrderDTO> childrenServiceOrderList = new ArrayList<>();
				List<ServiceOrderDO> list = serviceOrderDao.listByParentId(serviceOrderDto.getId());
				list.forEach(serviceOrder -> {
					ChildrenServiceOrderDTO childrenServiceOrderDto = mapper.map(serviceOrder,
							ChildrenServiceOrderDTO.class);
					ServicePackageDO servicePackageDo = servicePackageDao
							.getById(childrenServiceOrderDto.getServicePackageId()); // TODO:
					// 又偷懒了，性能比较差哦：）
					if (servicePackageDo != null)
						childrenServiceOrderDto.setServicePackageType(servicePackageDo.getType());
					childrenServiceOrderList.add(childrenServiceOrderDto);
				});
				serviceOrderDto.setChildrenServiceOrders(childrenServiceOrderList);
			}

			List<Integer> cIds = new ArrayList<>();
//			List<VisaDO> visaList = visaDao.listVisaByServiceOrderId(serviceOrderDo.getId());
//			if (visaList != null && visaList.size() > 0) {
//				for (VisaDO visaDo : visaList)
//					cIds.add(visaDo.getId());
//			}
//			List<CommissionOrderDO> commissionOrderList = commissionOrderDao
//					.listCommissionOrderByServiceOrderId(serviceOrderDto.getId());
//			if (commissionOrderList != null && commissionOrderList.size() > 0) {
//				for (CommissionOrderDO commissionOrderDo : commissionOrderList)
//					cIds.add(commissionOrderDo.getId());
//			}
			serviceOrderDto.setCIds(cIds);

			serviceOrderDTOList.add(serviceOrderDto);
			// 查询职业名称
			ServiceAssessDO serviceAssessDO = serviceAssessDao.seleteAssessById(serviceOrderDto.getServiceAssessId());
			if (serviceAssessDO != null)
				serviceOrderDto.setServiceAssessDO(serviceAssessDO);
		});
	}

	@Override
	public Integer caseCount(Integer officialId, String days, String state) {
		return serviceOrderDao.caseCount(officialId,days,state);
	}

	/**
	 * 这个方法
	 * @param serviceOrderId
	 */
	public  void createChat(int serviceOrderId , String token) {
		List<String> userList = new ArrayList<>();
		JSONObject parm = new JSONObject();
		ChatDO chatDO = new ChatDO();
		ServiceOrderDO serviceOrderDO = serviceOrderDao.getServiceOrderById(serviceOrderId);
		if (serviceOrderDO != null){
			chatDO.setServiceOrderId(serviceOrderId);
			chatDO.setUserId(serviceOrderDO.getUserId());
			AdviserDO adviserDO =  adviserDao.getAdviserById(serviceOrderDO.getAdviserId());
			if (adviserDO != null){
				chatDO.setAdviserId(adviserDO.getId());
				AdminUserDO adminUserDO =  adminUserDao.getAdminUserByUsername(adviserDO.getEmail());
				if (adminUserDO != null)
					if ( StringUtil.isNotEmpty(adminUserDO.getOperUserId())){
						userList.add(adminUserDO.getOperUserId());
						parm.put("owner",adminUserDO.getOperUserId());
					}
			}
			OfficialDO officialDO =  officialDao.getOfficialById(serviceOrderDO.getOfficialId());
			if (officialDO != null){
				chatDO.setOfficialId(officialDO.getId());
				AdminUserDO adminUserDO =  adminUserDao.getAdminUserByUsername(officialDO.getEmail());
				if (adminUserDO != null)
					if ( StringUtil.isNotEmpty(adminUserDO.getOperUserId()))
						userList.add(adminUserDO.getOperUserId());
			}
			MaraDO maraDO =  maraDao.getMaraById(serviceOrderDO.getMaraId());
			if (maraDO != null){
				chatDO.setMaraId(maraDO.getId());
				AdminUserDO adminUserDO =  adminUserDao.getAdminUserByUsername(maraDO.getEmail());
				if (adminUserDO != null)
					if ( StringUtil.isNotEmpty(adminUserDO.getOperUserId()) & serviceOrderDO.getType().equalsIgnoreCase("VISA"))
						userList.add(adminUserDO.getOperUserId());
			}
			parm.put("userlist",userList);
			parm.put("name","服务订单" + serviceOrderDO.getId() + "的群聊");
			parm.put("chatid","ZNZ"+serviceOrderDO.getId());
		}
		//ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
		//HttpSession session=attr.getRequest().getSession(true);
		//String token = (String) session.getAttribute("corpToken" + BaseController.VERSION); // TODO:小包  不要在Service层依赖Session，有时间把这几行代码移到Controller层
		JSONObject json =  WXWorkAPI.sendPostBody_Map(WXWorkAPI.CREATECHAT.replace("ACCESS_TOKEN",token),parm);// TODO:磊哥 好的，我的代码还有哪里不规范的，麻烦你帮我指出来
		Map<String,Object> result = JSON.parseObject(JSON.toJSONString(json), Map.class);
		if ((int)result.get("errcode") == 0){
			chatDO.setChatId("ZNZ"+serviceOrderDO.getId());
			int re = wxWorkDAO.addChat(chatDO);
			JSONObject msgParm = new JSONObject();
			JSONObject content = new JSONObject();
			msgParm.put("chatid",result.get("chatid"));
			msgParm.put("msgtype","text");
			content.put("content","这里是订单编号为:" + serviceOrderDO.getId() + "的群聊");
			msgParm.put("text",content);
			msgParm.put("safe",0);
			JSONObject sendFirstMsgResultJson =  WXWorkAPI.sendPostBody_Map(WXWorkAPI.SENDMESSAGE.replace("ACCESS_TOKEN",token),msgParm);
		}
	}

	private String getPeopleTypeStr(String peopleType) {
		if ("1A".equalsIgnoreCase(peopleType))
			return "单人";
		else if ("1B".equalsIgnoreCase(peopleType))
			return "单人提配偶";
		else if ("2A".equalsIgnoreCase(peopleType))
			return "带配偶";
		else if ("XA".equalsIgnoreCase(peopleType))
			return "带孩子";
		else if ("XB".equalsIgnoreCase(peopleType))
			return "带配偶孩子";
		else if ("XC".equalsIgnoreCase(peopleType))
			return "其它";
		else
			return "未知";
	}

}
