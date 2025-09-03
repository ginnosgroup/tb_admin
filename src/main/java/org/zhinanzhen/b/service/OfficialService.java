package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.dao.pojo.OfficialEvaluate;
import org.zhinanzhen.b.service.pojo.OfficialDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface OfficialService {

	public int addOfficial(OfficialDTO officialDto) throws ServiceException;

	public int updateOfficial(OfficialDTO officialDto) throws ServiceException;

	public int countOfficial(String name, Integer regionId, Integer gradeId) throws ServiceException;

	public List<OfficialDTO> listOfficial(String name, Integer regionId, Integer gradeId, boolean isbuiltOrder, int pageNum, int pageSize)
			throws ServiceException;

	public OfficialDTO getOfficialById(int id) throws ServiceException;

	int updateWorkState(OfficialDTO officialDTO) throws ServiceException;

    int addOfficialEvaluate(OfficialEvaluate officialEvaluate);

	List<OfficialEvaluate> listOfficialEvaluate(List<Integer> officialIds, Integer adviserId, String startCollaborationTime, String endCollaborationTime, Integer pageNum, Integer pageSize);

	int countOfficialEvaluate(List<Integer> officialIds, Integer adviserId, String startCollaborationTime, String endCollaborationTime);


	int updateOfficialEvaluate(OfficialEvaluate officialEvaluate);

	OfficialEvaluate getOfficialEvaluate(Integer integer, Integer adviserId, String startCollaborationTime, String endCollaborationTime);

	Double getAverageScore(Integer integer, Integer adviserId, String collaborationTime, int mounths, boolean isCurrentMonth);

}
