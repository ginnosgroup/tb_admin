package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.service.pojo.OfficialTagDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface OfficialTagService {

	int add(OfficialTagDTO officialTagDto) throws ServiceException;

	int update(OfficialTagDTO officialTagDto) throws ServiceException;

	List<OfficialTagDTO> list() throws ServiceException;

	OfficialTagDTO get(int id) throws ServiceException;

	int delete(int id) throws ServiceException;

	int addServiceOrderOfficialTag(int id, int serviceOrderId) throws ServiceException;

	int updateServiceOrderOfficialTag(int id, int serviceOrderId) throws ServiceException;

	int deleteServiceOrderOfficialTagByTagIdAndServiceOrderId(int id, int serviceOrderId) throws ServiceException;

}
