package org.zhinanzhen.b.service.impl;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zhinanzhen.b.dao.AdviserDataDAO;
import org.zhinanzhen.b.dao.ServiceOrderDAO;
import org.zhinanzhen.b.dao.ServiceOrderOriginallyDAO;
import org.zhinanzhen.b.dao.WebLogDAO;
import org.zhinanzhen.b.dao.pojo.*;
import org.zhinanzhen.b.service.AdviserDataService;
import org.zhinanzhen.b.service.pojo.*;
import org.zhinanzhen.tb.dao.pojo.ServiceOrderOriginallyDO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.ErrorCodeEnum;
import com.ikasoa.core.utils.MapUtil;

@Service("AdviserDataService")
public class AdviserDataServiceImpl extends BaseService implements AdviserDataService {

	@Resource
	private AdviserDataDAO adviserDataDao;

	@Resource
	private ServiceOrderOriginallyDAO serviceOrderOriginallyDAO;

	@Resource
	private ServiceOrderDAO serviceOrderDAO;

	@Resource
	private WebLogDAO webLogDAO;

	@Override
	public List<AdviserServiceOrderDTO> listServiceOrder(Integer adviserId) throws ServiceException {
		List<AdviserServiceOrderDTO> adviserServiceOrderDtoList = new ArrayList<AdviserServiceOrderDTO>();
		List<AdviserServiceOrderDO> adviserServiceOrderDoList = new ArrayList<AdviserServiceOrderDO>();
		try {
			adviserServiceOrderDoList = adviserDataDao.listServiceOrder(adviserId);
			if (adviserServiceOrderDoList == null)
				return null;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (AdviserServiceOrderDO adviserServiceOrderDo : adviserServiceOrderDoList) {
			AdviserServiceOrderDTO adviserServiceOrderDto = mapper.map(adviserServiceOrderDo,
					AdviserServiceOrderDTO.class);
			adviserServiceOrderDtoList.add(adviserServiceOrderDto);
		}
		return adviserServiceOrderDtoList;
	}

	@Override
	public List<AdviserVisaDTO> listVisa(Integer adviserId) throws ServiceException {
		List<AdviserVisaDTO> adviserVisaDtoList = new ArrayList<AdviserVisaDTO>();
		List<AdviserVisaDO> adviserVisaDoList = new ArrayList<AdviserVisaDO>();
		try {
			adviserVisaDoList = adviserDataDao.listVisa(adviserId);
			if (adviserVisaDoList == null)
				return null;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (AdviserVisaDO adviserVisaDo : adviserVisaDoList) {
			AdviserVisaDTO adviserVisaDto = mapper.map(adviserVisaDo, AdviserVisaDTO.class);
			adviserVisaDtoList.add(adviserVisaDto);
		}
		return adviserVisaDtoList;
	}

	@Override
	public List<AdviserCommissionOrderDTO> listCommissionOrder(Integer adviserId) throws ServiceException {
		List<AdviserCommissionOrderDTO> adviserCommissionOrderDtoList = new ArrayList<AdviserCommissionOrderDTO>();
		List<AdviserCommissionOrderDO> adviserCommissionOrderDoList = new ArrayList<AdviserCommissionOrderDO>();
		try {
			adviserCommissionOrderDoList = adviserDataDao.listCommissionOrder(adviserId);
			if (adviserCommissionOrderDoList == null)
				return null;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (AdviserCommissionOrderDO adviserCommissionOrderDo : adviserCommissionOrderDoList) {
			AdviserCommissionOrderDTO adviserCommissionOrderDto = mapper.map(adviserCommissionOrderDo,
					AdviserCommissionOrderDTO.class);
			adviserCommissionOrderDtoList.add(adviserCommissionOrderDto);
		}
		return adviserCommissionOrderDtoList;
	}

	@Override
	public List<AdviserUserDTO> listUser(Integer adviserId) throws ServiceException {
		List<AdviserUserDTO> adviserUserDtoList = new ArrayList<AdviserUserDTO>();
		List<AdviserUserDO> adviserUserDoList = new ArrayList<AdviserUserDO>();
		try {
			adviserUserDoList = adviserDataDao.listUser(adviserId);
			if (adviserUserDoList == null)
				return null;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (AdviserUserDO adviserUserDo : adviserUserDoList) {
			AdviserUserDTO adviserUserDto = mapper.map(adviserUserDo, AdviserUserDTO.class);
			adviserUserDtoList.add(adviserUserDto);
		}
		return adviserUserDtoList;
	}

	@Override
	@Transactional(rollbackFor = ServiceException.class)
	public Map<String, Integer> adviserDataMigration(Integer newAdviserId, Integer adviserId, List<Integer> userIdList, Integer operateUserId, String apList)
			throws ServiceException {
		switch (apList) {
                case "GW":
                    apList = "顾问";
                    break;
                case "WA":
                    apList = "文案";
                    break;
                case "KJ":
                    apList = "会计";
                    break;
                case "SUPERAD":
                    apList = "超级管理员";
                    break;
                default: apList = apList;
		}
		for (Integer userId : userIdList) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			WebLogDTO webLogDTOTmp = new WebLogDTO();
			webLogDTOTmp.setUserId(operateUserId);
			webLogDTOTmp.setRole(apList);
			webLogDTOTmp.setOperatedUser(userId);
			webLogDTOTmp.setStartTime(sdf.format(new Date()));
			webLogDTOTmp.setUri("/admin_v2.1/user/adviserDataMigration");
			webLogDAO.addWebLogs(webLogDTOTmp);

			ServiceOrderOriginallyDO serviceOrderOriginallyDO = new ServiceOrderOriginallyDO();
			serviceOrderOriginallyDO.setAdviserId(adviserId);
			serviceOrderOriginallyDO.setNewAdviserId(newAdviserId);
			serviceOrderOriginallyDO.setUserId(userId);
			serviceOrderOriginallyDO.setWebLogId(webLogDTOTmp.getId());
			serviceOrderOriginallyDAO.addServiceOrderOriginallyDO(serviceOrderOriginallyDO);
			List<ServiceOrderDO> serviceOrderDOS = serviceOrderDAO.listServiceOrder(null, null, null, null, null, null, null,
					null, null, null, null,
					null, null, null,
					null, null, null, null, userId,
					null, null, null, null, null, null
					, null, null, null, null, null
					, null, null, null,null, 0, 9999, null);
			for (ServiceOrderDO serviceOrderDO : serviceOrderDOS) {
				WebLogDTO webLogDTO = new WebLogDTO();
				webLogDTO.setUserId(operateUserId);
				webLogDTO.setRole(apList);
				webLogDTO.setServiceOrderId(serviceOrderDO.getId());
				webLogDTO.setStartTime(sdf.format(new Date()));
				webLogDTO.setUri("/admin_v2.1/adviserData/adviserDataMigration");
				webLogDAO.addWebLogs(webLogDTO);

				serviceOrderOriginallyDO = new ServiceOrderOriginallyDO();
				serviceOrderOriginallyDO.setServiceOrderId(serviceOrderDO.getId());
				serviceOrderOriginallyDO.setAdviserId(serviceOrderDO.getAdviserId());
				serviceOrderOriginallyDO.setUserId(userId);
				serviceOrderOriginallyDO.setNewAdviserId(newAdviserId);
				serviceOrderOriginallyDO.setWebLogId(webLogDTO.getId());
				serviceOrderOriginallyDAO.addServiceOrderOriginallyDO(serviceOrderOriginallyDO);
			}
		}


		Map<String, Integer> map = MapUtil.buildHashMap("ud",
				adviserDataDao.userDataMigration(newAdviserId, adviserId, userIdList), "uad",
				adviserDataDao.userAdviserDataMigration(newAdviserId, adviserId, userIdList), "ad",
				adviserDataDao.applicantDataMigration(newAdviserId, adviserId, userIdList), "sod",
				adviserDataDao.serviceOrderDataMigration(newAdviserId, adviserId, userIdList), "vd",
				adviserDataDao.visaDataMigration(newAdviserId, adviserId, userIdList), "cod",
				adviserDataDao.commissionOrderDataMigration(newAdviserId, adviserId, userIdList));
		LOG.info("顾问" + adviserId + "数据迁移到顾问" + newAdviserId + "(" + userIdList + "):" + map);
		return map;
	}

	@Override
	public Map<String, Integer> checkAdviserDataMigration(Integer newAdviserId, Integer adviserId,
			List<Integer> userIdList) throws ServiceException {
		Map<String, Integer> map = MapUtil.buildHashMap("ud",
				adviserDataDao.countUserDataMigration(newAdviserId, adviserId, userIdList), "uad",
				adviserDataDao.countUserAdviserDataMigration(newAdviserId, adviserId, userIdList), "ad",
				adviserDataDao.countApplicantDataMigration(newAdviserId, adviserId, userIdList), "sod",
				adviserDataDao.countServiceOrderDataMigration(newAdviserId, adviserId, userIdList), "vd",
				adviserDataDao.countVisaDataMigration(newAdviserId, adviserId, userIdList), "cod",
				adviserDataDao.countCommissionOrderDataMigration(newAdviserId, adviserId, userIdList));
		LOG.info("顾问" + adviserId + "数据迁移到顾问" + newAdviserId + "(" + userIdList + ")数据统计:" + map);
		return map;
	}

}
