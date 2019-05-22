package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.service.pojo.SourceDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface SourceService {

	int addSource(SourceDTO sourceDto) throws ServiceException;

	int updateSource(SourceDTO sourceDto) throws ServiceException;

	List<SourceDTO> listSource(Integer sourceRegionId) throws ServiceException;

	int deleteSource(int id) throws ServiceException;

}
