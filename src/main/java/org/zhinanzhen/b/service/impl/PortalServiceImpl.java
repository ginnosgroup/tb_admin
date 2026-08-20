package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.MaraDAO;
import org.zhinanzhen.b.dao.OfficialDAO;
import org.zhinanzhen.b.dao.PortalDAO;
import org.zhinanzhen.b.dao.PortalTypeDAO;
import org.zhinanzhen.b.dao.pojo.MaraDO;
import org.zhinanzhen.b.dao.pojo.OfficialDO;
import org.zhinanzhen.b.dao.pojo.PortalDO;
import org.zhinanzhen.b.dao.pojo.PortalTypeDO;
import org.zhinanzhen.b.service.PortalService;
import org.zhinanzhen.b.service.pojo.PortalDTO;
import org.zhinanzhen.b.service.pojo.PortalTypeDTO;
import org.zhinanzhen.tb.dao.AdviserDAO;
import org.zhinanzhen.tb.dao.pojo.AdviserDO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.ErrorCodeEnum;

@Service("PortalService")
public class PortalServiceImpl extends BaseService implements PortalService {

	@Resource
	private PortalDAO portalDao;

	@Resource
	private PortalTypeDAO portalTypeDao;

	@Resource
	private AdviserDAO adviserDao;

	@Resource
	private OfficialDAO officialDao;

	@Resource
	private MaraDAO maraDao;

	@Override
	public int addPortal(PortalDTO portalDto) throws ServiceException {
		if (portalDto == null) {
			ServiceException se = new ServiceException("portalDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			// 查重校验：typeId+name 只允许存在一条记录，不允许重复创建
			PortalDO existPortalDo = portalDao.getPortalByTypeIdAndName(portalDto.getTypeId(),
					portalDto.getName());
			if (existPortalDo != null) {
				ServiceException se = new ServiceException("案件已存在：typeId=" + portalDto.getTypeId() + ", name="
						+ portalDto.getName() + "，不允许重复创建.");
				se.setCode(1);
				throw se;
			}
			PortalDO portalDo = mapper.map(portalDto, PortalDO.class);
			if (portalDao.addPortal(portalDo) > 0) {
				portalDto.setId(portalDo.getId());
				return portalDo.getId();
			} else {
				return 0;
			}
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int updatePortal(PortalDTO portalDto) throws ServiceException {
		if (portalDto == null) {
			ServiceException se = new ServiceException("portalDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			PortalDO portalDo = mapper.map(portalDto, PortalDO.class);
			return portalDao.updatePortal(portalDo);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public List<PortalDTO> listPortal(Integer typeId, String strState, String keyword, int pageNum, int pageSize,
			Integer adviserId, Integer adviserRegionId, Integer officialId, Integer officialRegionId, Integer maraId)
			throws ServiceException {
		List<PortalDTO> portalDtoList = new ArrayList<PortalDTO>();
		List<PortalDO> portalDoList = new ArrayList<PortalDO>();
		try {
			portalDoList = portalDao.listPortal(typeId, strState, keyword, pageNum * pageSize, pageSize, adviserId,
					adviserRegionId, officialId, officialRegionId, maraId);
			if (portalDoList == null)
				return null;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (PortalDO portalDo : portalDoList) {
			PortalDTO portalDto = mapper.map(portalDo, PortalDTO.class);
			assemblePortalNames(portalDto);
			portalDtoList.add(portalDto);
		}
		return portalDtoList;
	}

	@Override
	public int countPortal(Integer typeId, String strState, String keyword, Integer adviserId,
			Integer adviserRegionId, Integer officialId, Integer officialRegionId, Integer maraId)
			throws ServiceException {
		try {
			return portalDao.countPortal(typeId, strState, keyword, adviserId, adviserRegionId, officialId,
					officialRegionId, maraId);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
	}

	@Override
	public PortalDTO getPortal(Integer id, Integer adviserId, Integer adviserRegionId, Integer officialId,
			Integer officialRegionId, Integer maraId) throws ServiceException {
		if (id == null || id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			PortalDO portalDo = portalDao.getPortalById(id, adviserId, adviserRegionId, officialId, officialRegionId,
					maraId);
			if (portalDo == null) {
				ServiceException se = new ServiceException("No data !");
				se.setCode(ErrorCodeEnum.DATA_ERROR.code());
				throw se;
			}
			PortalDTO portalDto = mapper.map(portalDo, PortalDTO.class);
			assemblePortalNames(portalDto);
			return portalDto;
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
	}

	/**
	 * 按id查案件类型/顾问/文案/mara并组装名称到DTO，同时把完整的案件类型对象组装进 portalType
	 */
	private void assemblePortalNames(PortalDTO portalDto) {
		if (portalDto.getTypeId() > 0) {
			PortalTypeDO portalTypeDo = portalTypeDao.getPortalTypeById(portalDto.getTypeId());
			if (portalTypeDo != null) {
				portalDto.setPortalTypeName(portalTypeDo.getName());
				portalDto.setPortalType(mapper.map(portalTypeDo, PortalTypeDTO.class));
			}
		}
		if (portalDto.getAdviserId() > 0) {
			AdviserDO adviserDo = adviserDao.getAdviserById(portalDto.getAdviserId());
			if (adviserDo != null)
				portalDto.setAdviserName(adviserDo.getName());
		}
		if (portalDto.getOfficialId() > 0) {
			OfficialDO officialDo = officialDao.getOfficialById(portalDto.getOfficialId());
			if (officialDo != null)
				portalDto.setOfficialName(officialDo.getName());
		}
		if (portalDto.getMaraId() > 0) {
			MaraDO maraDo = maraDao.getMaraById(portalDto.getMaraId());
			if (maraDo != null)
				portalDto.setMaraName(maraDo.getName());
		}
	}

	@Override
	public int updateAiConsultContent(int id, String aiConsultContent) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			return portalDao.updateAiConsultContent(id, aiConsultContent);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int deletePortal(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			return portalDao.deletePortal(id);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

}
