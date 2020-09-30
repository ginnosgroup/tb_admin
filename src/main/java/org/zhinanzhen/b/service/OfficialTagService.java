package org.zhinanzhen.b.service;

import org.zhinanzhen.b.service.pojo.OfficialTagDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface OfficialTagService {

	int addOfficialTag(OfficialTagDTO officialTagDto) throws ServiceException;
	
	int updateOfficialTag(OfficialTagDTO officialTagDto) throws ServiceException;

}
