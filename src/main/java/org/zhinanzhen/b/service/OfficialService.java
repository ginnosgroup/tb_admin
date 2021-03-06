package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.service.pojo.OfficialDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface OfficialService {

	public int addOfficial(OfficialDTO officialDto) throws ServiceException;

	public int updateOfficial(OfficialDTO officialDto) throws ServiceException;

	public int countOfficial(String name, Integer regionId) throws ServiceException;

	public List<OfficialDTO> listOfficial(String name, Integer regionId, int pageNum, int pageSize)
			throws ServiceException;

	public OfficialDTO getOfficialById(int id) throws ServiceException;

}
