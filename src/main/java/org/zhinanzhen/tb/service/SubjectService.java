package org.zhinanzhen.tb.service;

import java.util.List;

import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.pojo.SubjectDTO;

public interface SubjectService {

	public int addSubject(SubjectDTO subjectDto) throws ServiceException;

	public int updateSubjectState(int id, SubjectStateEnum state) throws ServiceException;

	public int updateSubject(SubjectDTO subjectDto) throws ServiceException;
	
	public boolean updateSubjectCategoryId(int id, int categoryId) throws ServiceException;

	public int countSubject(String keyword, Integer categoryId, SubjectStateEnum state) throws ServiceException;

	public List<SubjectDTO> listSubject(String keyword, Integer categoryId, SubjectStateEnum state, int pageNum,
			int pageSize) throws ServiceException;
	
	public int sortSubject(int frontId, int id) throws ServiceException;

	public SubjectDTO getSubjectById(int id) throws ServiceException;
	
	public int deleteSubjectById(int id) throws ServiceException;

}
