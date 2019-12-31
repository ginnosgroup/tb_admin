package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.b.service.pojo.MaraDTO;

public interface MaraService {

	public int addMara(MaraDTO maraDto) throws ServiceException;

	public int updateMara(MaraDTO maraDto) throws ServiceException;

	public int countMara(String name, Integer regionId) throws ServiceException;

	public List<MaraDTO> listMara(String name, Integer regionId, int pageNum, int pageSize) throws ServiceException;

	public MaraDTO getMaraById(int id) throws ServiceException;

}
