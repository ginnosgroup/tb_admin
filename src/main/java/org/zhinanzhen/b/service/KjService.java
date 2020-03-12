package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.service.pojo.KjDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface KjService {

	public int addKj(KjDTO kjDto) throws ServiceException;

	public int updateKj(KjDTO kjDto) throws ServiceException;

	public int countKj(String name, Integer regionId) throws ServiceException;

	public List<KjDTO> listKj(String name, Integer regionId, int pageNum, int pageSize) throws ServiceException;

	public KjDTO getKjById(int id) throws ServiceException;

}
