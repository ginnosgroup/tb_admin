package org.zhinanzhen.tb.service;

import java.util.List;

import org.zhinanzhen.tb.service.pojo.SubjectCategoryDTO;

public interface SubjectCategoryService {

	public int addSubjectCategory(SubjectCategoryDTO subjectCategoryDto) throws ServiceException;

	public int updateSubjectCategoryState(int id, SubjectCategoryStateEnum state) throws ServiceException;

	public int updateSubjectCategory(SubjectCategoryDTO subjectCategoryDto) throws ServiceException;

	public int countSubjectCategory(SubjectCategoryStateEnum state) throws ServiceException;

	public List<SubjectCategoryDTO> listSubjectCategory(SubjectCategoryStateEnum state, int pageNum, int pageSize)
			throws ServiceException;

	public SubjectCategoryDTO getSubjectCategoryById(int id) throws ServiceException;

}
