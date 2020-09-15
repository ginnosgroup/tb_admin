package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.OfficialDAO;
import org.zhinanzhen.b.dao.pojo.OfficialDO;
import org.zhinanzhen.b.service.OfficialService;
import org.zhinanzhen.b.service.OfficialStateEnum;
import org.zhinanzhen.tb.dao.AdminUserDAO;
import org.zhinanzhen.tb.dao.RegionDAO;
import org.zhinanzhen.tb.dao.pojo.AdminUserDO;
import org.zhinanzhen.tb.dao.pojo.RegionDO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;
import org.zhinanzhen.b.service.pojo.OfficialDTO;
import com.ikasoa.core.ErrorCodeEnum;
import com.ikasoa.core.utils.StringUtil;

@Service("OfficialService")
public class OfficialServiceImpl extends BaseService implements OfficialService {
	@Resource
	private OfficialDAO officialDao;
	@Resource
	private RegionDAO regionDao;
	@Resource
	private AdminUserDAO adminUserDao;

	@Override
	public int addOfficial(OfficialDTO officialDto) throws ServiceException {
		if (officialDto == null) {
			ServiceException se = new ServiceException("officialDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			OfficialDO officialDo = mapper.map(officialDto, OfficialDO.class);
			if (officialDto.getState() != null) {
				officialDo.setState(officialDto.getState().toString());
			}
			if (officialDao.addOfficial(officialDo) > 0) {
				officialDto.setId(officialDo.getId());
				return officialDo.getId();
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
	public int updateOfficial(OfficialDTO officialDto) throws ServiceException {
		if (officialDto == null) {
			ServiceException se = new ServiceException("officialDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			OfficialDO officialDo = mapper.map(officialDto, OfficialDO.class);
			if (officialDto.getState() != null) {
				officialDo.setState(officialDto.getState().toString());
			}
			return officialDao.updateOfficial(officialDo);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int countOfficial(String name, Integer regionId) throws ServiceException {
		return officialDao.countOfficial(name, regionId);
	}

	@Override
	public List<OfficialDTO> listOfficial(String name, Integer regionId, int pageNum, int pageSize)
			throws ServiceException {
		if (pageNum < 0) {
			pageNum = DEFAULT_PAGE_NUM;
		}
		if (pageSize < 0) {
			pageSize = DEFAULT_PAGE_SIZE;
		}
		List<OfficialDTO> officialDtoList = new ArrayList<OfficialDTO>();
		List<OfficialDO> officialDoList = new ArrayList<OfficialDO>();
		try {
			officialDoList = officialDao.listOfficial(name, regionId, pageNum * pageSize, pageSize);
			if (officialDoList == null) {
				return null;
			}
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (OfficialDO officialDo : officialDoList) {
			OfficialDTO officialDto = mapper.map(officialDo, OfficialDTO.class);
			if (StringUtil.isNotEmpty(officialDo.getState())) {
				officialDto.setState(OfficialStateEnum.get(officialDo.getState()));
			}
			RegionDO regionDo = regionDao.getRegionById(officialDo.getRegionId());
			if (regionDo != null) {
				officialDto.setRegionName(regionDo.getName());
				officialDto.setRegionDo(regionDo);
			}
			AdminUserDO adminUserDo = adminUserDao.getAdminUserByUsername(officialDo.getEmail());
			if (adminUserDo != null)
				officialDto.setIsOfficialAdmin(adminUserDo.isOfficialAdmin());
			officialDtoList.add(officialDto);
		}
		return officialDtoList;
	}

	@Override
	public OfficialDTO getOfficialById(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		OfficialDTO officialDto = null;
		try {
			OfficialDO officialDo = officialDao.getOfficialById(id);
			if (officialDo == null) {
				// ServiceException se = new ServiceException("the Official is't
				// exist .");
				// se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
				// throw se;
				return null;
			}
			officialDto = mapper.map(officialDo, OfficialDTO.class);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
		return officialDto;
	}
}