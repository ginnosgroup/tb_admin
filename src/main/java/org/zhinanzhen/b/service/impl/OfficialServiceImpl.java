package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.OfficialDAO;
import org.zhinanzhen.b.dao.pojo.OfficialDO;
import org.zhinanzhen.b.service.OfficialService;
import org.zhinanzhen.b.service.OfficialStateEnum;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;
import org.zhinanzhen.b.service.pojo.OfficialDTO;
import com.ikasoa.core.thrift.ErrorCodeEnum;
import com.ikasoa.core.utils.StringUtil;

@Service("OfficialService")
public class OfficialServiceImpl extends BaseService implements OfficialService {
	@Resource
	private OfficialDAO OfficialDAO;
	@Override
	public int addOfficial(OfficialDTO OfficialDto) throws ServiceException {
		if (OfficialDto == null) {
			ServiceException se = new ServiceException("OfficialDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			OfficialDO OfficialDo = mapper.map(OfficialDto, OfficialDO.class);
			if (OfficialDto.getState() != null) {
				OfficialDo.setState(OfficialDto.getState().toString());
			}
			if (OfficialDAO.addOfficial(OfficialDo) > 0) {
				return OfficialDo.getId();
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
	public int updateOfficial(OfficialDTO OfficialDto) throws ServiceException {
		if (OfficialDto == null) {
			ServiceException se = new ServiceException("OfficialDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			OfficialDO OfficialDo = mapper.map(OfficialDto, OfficialDO.class);
			if (OfficialDto.getState() != null) {
				OfficialDo.setState(OfficialDto.getState().toString());
			}
			return OfficialDAO.updateOfficial(OfficialDo);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int countOfficial(String name, Integer regionId) throws ServiceException {
		return OfficialDAO.countOfficial(name, regionId);
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
		List<OfficialDTO> OfficialDtoList = new ArrayList<OfficialDTO>();
		List<OfficialDO> OfficialDoList = new ArrayList<OfficialDO>();
		try {
			OfficialDoList = OfficialDAO.listOfficial(name, regionId, pageNum * pageSize, pageSize);
			if (OfficialDoList == null) {
				return null;
			}
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (OfficialDO OfficialDo : OfficialDoList) {
			OfficialDTO OfficialDto = mapper.map(OfficialDo, OfficialDTO.class);
			if (StringUtil.isNotEmpty(OfficialDo.getState())) {
				OfficialDto.setState(OfficialStateEnum.get(OfficialDo.getState()));
			}
			OfficialDtoList.add(OfficialDto);
		}
		return OfficialDtoList;
	}

	@Override
	public OfficialDTO getOfficialById(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		OfficialDTO OfficialDto = null;
		try {
			OfficialDO OfficialDo = OfficialDAO.getOfficialById(id);
			if (OfficialDo == null) {
//				ServiceException se = new ServiceException("the Official is't exist .");
//				se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
//				throw se;
				return null;
			}
			OfficialDto = mapper.map(OfficialDo, OfficialDTO.class);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
		return OfficialDto;
	}
}