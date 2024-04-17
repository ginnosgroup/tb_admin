package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zhinanzhen.b.dao.AdviserDataDAO;
import org.zhinanzhen.b.dao.pojo.AdviserCommissionOrderDO;
import org.zhinanzhen.b.dao.pojo.AdviserServiceOrderDO;
import org.zhinanzhen.b.dao.pojo.AdviserUserDO;
import org.zhinanzhen.b.dao.pojo.AdviserVisaDO;
import org.zhinanzhen.b.service.AdviserDataService;
import org.zhinanzhen.b.service.pojo.AdviserCommissionOrderDTO;
import org.zhinanzhen.b.service.pojo.AdviserServiceOrderDTO;
import org.zhinanzhen.b.service.pojo.AdviserUserDTO;
import org.zhinanzhen.b.service.pojo.AdviserVisaDTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.ErrorCodeEnum;
import com.ikasoa.core.utils.MapUtil;

@Service("AdviserDataService")
public class AdviserDataServiceImpl extends BaseService implements AdviserDataService {

	@Resource
	private AdviserDataDAO adviserDataDao;

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
	public Map<String, Integer> adviserDataMigration(Integer newAdviserId, Integer adviserId, List<Integer> userIdList)
			throws ServiceException {
		LOG.info("顾问" + adviserId + "数据迁移到顾问" + newAdviserId);
		return MapUtil.buildHashMap("ud", adviserDataDao.userDataMigration(newAdviserId, adviserId, userIdList), "uad",
				adviserDataDao.userAdviserDataMigration(newAdviserId, adviserId, userIdList), "ad",
				adviserDataDao.applicantDataMigration(newAdviserId, adviserId, userIdList), "sod",
				adviserDataDao.serviceOrderDataMigration(newAdviserId, adviserId, userIdList), "vd",
				adviserDataDao.visaDataMigration(newAdviserId, adviserId, userIdList), "cod",
				adviserDataDao.commissionOrderDataMigration(newAdviserId, adviserId, userIdList));
	}

}
