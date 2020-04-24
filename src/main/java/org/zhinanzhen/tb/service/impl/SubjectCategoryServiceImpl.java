package org.zhinanzhen.tb.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.tb.dao.SubjectCategoryDAO;
import org.zhinanzhen.tb.dao.pojo.SubjectCategoryDO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.SubjectCategoryService;
import org.zhinanzhen.tb.service.SubjectCategoryStateEnum;
import org.zhinanzhen.tb.service.pojo.SubjectCategoryDTO;
import com.ikasoa.core.ErrorCodeEnum;

@Service("subjectCategoryService")
public class SubjectCategoryServiceImpl extends BaseService implements SubjectCategoryService {

	@Resource
	private SubjectCategoryDAO subjectCategoryDao;

	@Override
	public int addSubjectCategory(SubjectCategoryDTO subjectCategoryDto) throws ServiceException {
		if (subjectCategoryDto == null) {
			ServiceException se = new ServiceException("subjectCategoryDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			SubjectCategoryDO subjectCategoryDo = mapper.map(subjectCategoryDto, SubjectCategoryDO.class);
			subjectCategoryDo.setState(subjectCategoryDto.getState().toString());
			return subjectCategoryDao.addSubjectCategory(subjectCategoryDo);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int updateSubjectCategoryState(int id, SubjectCategoryStateEnum state) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		if (state == null) {
			ServiceException se = new ServiceException("state is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		return subjectCategoryDao.updateSubjectCategoryState(id, state.toString());
	}

	@Override
	public int updateSubjectCategory(SubjectCategoryDTO subjectCategoryDto) throws ServiceException {
		if (subjectCategoryDto == null) {
			ServiceException se = new ServiceException("subjectCategoryDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			SubjectCategoryDO subjectCategoryDo = mapper.map(subjectCategoryDto, SubjectCategoryDO.class);
			subjectCategoryDo.setState(subjectCategoryDto.getState().toString());
			return subjectCategoryDao.updateSubjectCategory(subjectCategoryDo);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int countSubjectCategory(SubjectCategoryStateEnum state) throws ServiceException {
		if (state == null) {
			return subjectCategoryDao.countSubjectCategory(null);
		} else {
			return subjectCategoryDao.countSubjectCategory(state.toString());
		}
	}

	@Override
	public List<SubjectCategoryDTO> listSubjectCategory(SubjectCategoryStateEnum state, int pageNum, int pageSize)
			throws ServiceException {
		if (pageNum < 0) {
			pageNum = DEFAULT_PAGE_NUM;
		}
		if (pageSize < 0) {
			pageSize = DEFAULT_PAGE_SIZE;
		}
		List<SubjectCategoryDTO> subjectCategoryDtoList = new ArrayList<SubjectCategoryDTO>();
		List<SubjectCategoryDO> subjectCategoryDoList = new ArrayList<SubjectCategoryDO>();
		try {
			if (state == null) {
				subjectCategoryDoList = subjectCategoryDao.listSubjectCategory(null, pageNum * pageSize, pageSize);
			} else {
				subjectCategoryDoList = subjectCategoryDao.listSubjectCategory(state.toString(), pageNum * pageSize,
						pageSize);
			}
			if (subjectCategoryDoList == null) {
				return null;
			}
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (SubjectCategoryDO subjectCategoryDo : subjectCategoryDoList) {
			SubjectCategoryDTO subjectCategoryDto = mapper.map(subjectCategoryDo, SubjectCategoryDTO.class);
			subjectCategoryDtoList.add(subjectCategoryDto);
		}
		return subjectCategoryDtoList;
	}

	@Override
	public SubjectCategoryDTO getSubjectCategoryById(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		SubjectCategoryDTO subjectCategoryDto = null;
		try {
			SubjectCategoryDO subjectCategoryDo = subjectCategoryDao.getSubjectCategoryById(id);
			if (subjectCategoryDo == null) {
				ServiceException se = new ServiceException("the category is't exist .");
				se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
				throw se;
			}
			subjectCategoryDto = mapper.map(subjectCategoryDo, SubjectCategoryDTO.class);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
		return subjectCategoryDto;
	}
}
