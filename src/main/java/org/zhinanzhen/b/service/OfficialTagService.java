package org.zhinanzhen.b.service;

import org.zhinanzhen.b.service.pojo.OfficialTagDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface OfficialTagService {

	int add(OfficialTagDTO officialTagDto) throws ServiceException;
	
	int update(OfficialTagDTO officialTagDto) throws ServiceException;
	
	OfficialTagDTO get(int id) throws ServiceException;
	
	int delete(int id) throws ServiceException;

}
