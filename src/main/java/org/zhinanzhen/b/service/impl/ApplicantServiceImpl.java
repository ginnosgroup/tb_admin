package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zhinanzhen.b.dao.ApplicantDAO;
import org.zhinanzhen.b.dao.ServiceOrderDAO;
import org.zhinanzhen.b.dao.pojo.ApplicantDO;
import org.zhinanzhen.b.service.ApplicantService;
import org.zhinanzhen.b.service.pojo.ApplicantDTO;
import org.zhinanzhen.b.service.pojo.UserDTO;
import org.zhinanzhen.tb.dao.UserDAO;
import org.zhinanzhen.tb.dao.pojo.UserAdviserDO;
import org.zhinanzhen.tb.dao.pojo.UserDO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.ErrorCodeEnum;

@Service("ApplicantService")
public class ApplicantServiceImpl extends BaseService implements ApplicantService {
	
	@Resource
	private ServiceOrderDAO serviceOrderDao;
	
	@Resource
	private ApplicantDAO applicantDao;
	
	@Resource
	private UserDAO userDao;

	@Override
	@Transactional(rollbackFor = ServiceException.class)
	public int add(ApplicantDTO applicantDto) throws ServiceException {
		if (applicantDto == null) {
			ServiceException se = new ServiceException("applicantDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			List<UserAdviserDO> userAdviserList = userDao.listUserAdviserByUserId(applicantDto.getUserId());
			if (userAdviserList == null || userAdviserList.size() == 0)
				userDao.addUserAdviser(applicantDto.getUserId(), applicantDto.getAdviserId(), true);
			else {
				boolean isExists = false;
				for (UserAdviserDO userAdviserDo : userAdviserList)
					if (userAdviserDo.getAdviserId() == applicantDto.getAdviserId())
						isExists = true;
				if (!isExists)
					userDao.addUserAdviser(applicantDto.getUserId(), applicantDto.getAdviserId(), false);
			}
			ApplicantDO applicantDo = mapper.map(applicantDto, ApplicantDO.class);
			if (applicantDao.add(applicantDo) > 0) {
				applicantDto.setId(applicantDo.getId());
				return applicantDo.getId();
			} else {
				return 0;
			}
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int count(Integer id, String name, Integer userId, Integer adviserId) throws ServiceException {
		try {
			return applicantDao.count(id, name, userId, adviserId);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
	}

	@Override
	public List<ApplicantDTO> list(Integer id, String name, Integer userId, Integer adviserId, int pageNum, int pageSize)
			throws ServiceException {
		if (pageNum < 0)
			pageNum = DEFAULT_PAGE_NUM;
		if (pageSize < 0)
			pageSize = DEFAULT_PAGE_SIZE;
		List<ApplicantDTO> applicantDtoList = new ArrayList<ApplicantDTO>();
		List<ApplicantDO> applicantDoList = new ArrayList<ApplicantDO>();
		try {
			applicantDoList = applicantDao.list(id, name, userId, adviserId, pageNum * pageSize, pageSize);
			if (applicantDoList == null)
				return null;
			for (ApplicantDO applicantDo : applicantDoList) {
				ApplicantDTO applicantDto = mapper.map(applicantDo, ApplicantDTO.class);
				if (applicantDto.getUserId() > 0) {
					UserDO userDo = userDao.getUserById(applicantDto.getUserId());
					if (userDo != null)
						applicantDto.setUserDto(mapper.map(userDo, UserDTO.class));
				}
				applicantDtoList.add(applicantDto);
			}
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		return applicantDtoList;
	}

	@Override
	public ApplicantDTO getById(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		ApplicantDTO applicantDto = null;
		try {
			ApplicantDO applicantDo = applicantDao.getById(id);
			if (applicantDo == null)
				return null;
			applicantDto = mapper.map(applicantDo, ApplicantDTO.class);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
		return applicantDto;
	}

	@Override
	public int update(ApplicantDTO applicantDto) throws ServiceException {
		if (applicantDto == null) {
			ServiceException se = new ServiceException("applicantDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			return applicantDao.update(mapper.map(applicantDto, ApplicantDO.class));
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int deleteById(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			if (serviceOrderDao.countServiceOrderByApplicantId(id) > 0)
				return -1; // 已存在服务订单，不允许删除
			else
				return applicantDao.deleteById(id);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

}
