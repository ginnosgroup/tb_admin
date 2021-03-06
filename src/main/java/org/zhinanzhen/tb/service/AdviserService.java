package org.zhinanzhen.tb.service;

import java.util.List;

import org.zhinanzhen.tb.service.pojo.AdviserDTO;

public interface AdviserService {

	public int addAdviser(AdviserDTO adviserDto) throws ServiceException;

	public int updateAdviser(AdviserDTO adviserDto) throws ServiceException;

	public int countAdviser(String name, List<Integer> regionIdList) throws ServiceException;

	public List<AdviserDTO> listAdviser(String name, List<Integer> regionIdList, int pageNum, int pageSize)
			throws ServiceException;

	public AdviserDTO getAdviserById(int id) throws ServiceException;

}
