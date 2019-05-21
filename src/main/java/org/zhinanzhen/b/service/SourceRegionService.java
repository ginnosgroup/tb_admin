package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.service.pojo.SourceRegionDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface SourceRegionService {

	int addSourceRegion(SourceRegionDTO sourceRegionDto) throws ServiceException;

	int updateSourceRegion(SourceRegionDTO sourceRegionDto) throws ServiceException;

	List<SourceRegionDTO> listSourceRegion(int parentId) throws ServiceException;

	int deleteSourceRegion(int id) throws ServiceException;

}
