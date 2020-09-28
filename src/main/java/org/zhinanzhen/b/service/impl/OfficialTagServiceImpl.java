package org.zhinanzhen.b.service.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.OfficialTagDAO;
import org.zhinanzhen.b.dao.pojo.OfficialTagDO;
import org.zhinanzhen.b.service.OfficialTagService;
import org.zhinanzhen.b.service.pojo.OfficialTagDTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.ErrorCodeEnum;

@Service("OfficialTagService")
public class OfficialTagServiceImpl extends BaseService implements OfficialTagService {

	@Resource
	private OfficialTagDAO officialTagDao;

	@Override
	public int addOfficialTag(OfficialTagDTO officialTagDto) throws ServiceException {
		if (officialTagDto == null) {
			ServiceException se = new ServiceException("officialTagDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		OfficialTagDO officialTagDo = mapper.map(officialTagDto, OfficialTagDO.class);
		return officialTagDao.addOfficialTag(officialTagDo) > 0 ? officialTagDo.getId() : 0;
	}

}
