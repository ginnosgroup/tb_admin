package org.zhinanzhen.tb.service;

import java.util.List;

import org.zhinanzhen.tb.service.pojo.RegionDTO;

public interface RegionService {

	public List<RegionDTO> listAllRegion() throws ServiceException;

	public List<RegionDTO> listRegion(Integer parentId) throws ServiceException;
	/**
	 * 新增大区
	 * @param name
	 * @throws ServiceException
	 */
	public int addRegion(String name,int weight) throws ServiceException;
	/**
	 * 新增城市
	 * @param parentId
	 * @param name
	 * @throws ServiceException
	 */
	public int addCity(int parentId,String name,int weight) throws ServiceException;
	/**
	 * 修改区域
	 * @param id
	 * @param name
	 * @return
	 * @throws ServiceException
	 */
	public boolean updateRegion(int id,String name,int weight) throws ServiceException;

}
