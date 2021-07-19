package org.zhinanzhen.tb.service.impl;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zhinanzhen.tb.dao.AdviserDAO;
import org.zhinanzhen.tb.dao.OrderDAO;
import org.zhinanzhen.tb.dao.SubjectDAO;
import org.zhinanzhen.tb.dao.SubjectPriceIntervalDAO;
import org.zhinanzhen.tb.dao.UserDAO;
import org.zhinanzhen.tb.dao.pojo.OrderDO;
import org.zhinanzhen.tb.dao.pojo.SubjectDO;
import org.zhinanzhen.tb.dao.pojo.SubjectPriceIntervalDO;
import org.zhinanzhen.tb.dao.pojo.SubjectUpdateDO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.SubjectService;
import org.zhinanzhen.tb.service.SubjectStateEnum;
import org.zhinanzhen.tb.service.SubjectTypeEnum;
import org.zhinanzhen.tb.service.pojo.OrderDTO;
import org.zhinanzhen.tb.service.pojo.SubjectDTO;
import org.zhinanzhen.tb.service.pojo.SubjectPriceIntervalDTO;
import com.ikasoa.core.ErrorCodeEnum;

@Service("subjectService")
public class SubjectServiceImpl extends BaseService implements SubjectService {

	@Resource
	private SubjectDAO subjectDao;

	@Resource
	private OrderDAO orderDao;

	@Resource
	private SubjectPriceIntervalDAO subjectPriceIntervalDao;
	
	@Resource
    private UserDAO userDao;
	
	@Resource
    AdviserDAO adviserDao;

	@Override
	@Transactional(rollbackFor = ServiceException.class)
	public int addSubject(SubjectDTO subjectDto) throws ServiceException {
		if (subjectDto == null) {
			ServiceException se = new ServiceException("subjectDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			SubjectDO subjectDo = mapper.map(subjectDto, SubjectDO.class);
			SubjectDO firstSubjectDo = subjectDao.getFirstSubject();
			if (firstSubjectDo != null) {
				subjectDo.setWeight(firstSubjectDo.getWeight() + 1);
			}
			int i = subjectDao.addSubject(subjectDo);
			for (SubjectPriceIntervalDTO subjectPriceIntervalDto : subjectDto.getPriceIntervalList()) {
				SubjectPriceIntervalDO subjectPriceIntervalDo = mapper.map(subjectPriceIntervalDto,
						SubjectPriceIntervalDO.class);
				subjectPriceIntervalDo.setSubjectId(subjectDo.getId());
				int j = subjectPriceIntervalDao.addSubjectPriceInterval(subjectPriceIntervalDo);
				if (j <= 0) {
					rollback();
					ServiceException se = new ServiceException();
					se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
					throw se;
				}
			}
			if (i > 0) {
				return subjectDo.getId();
			} else {
				rollback();
				return 0;
			}
		} catch (Exception e) {
			rollback();
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int updateSubjectState(int id, SubjectStateEnum state) throws ServiceException {
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
		return subjectDao.updateSubjectState(id, state.toString());
	}

	@Override
	@Transactional(rollbackFor = ServiceException.class)
	public int updateSubject(SubjectDTO subjectDto) throws ServiceException {
		if (subjectDto == null) {
			ServiceException se = new ServiceException("subjectDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		if (subjectDto.getId() <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			SubjectUpdateDO subjectDo = mapper.map(subjectDto, SubjectUpdateDO.class);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			if (subjectDo.getStartDate() != null) {
				subjectDo.setStartDateTimpstamp(Timestamp.valueOf(sdf.format(subjectDo.getStartDate())));
			}
			if (subjectDo.getEndDate() != null) {
				subjectDo.setEndDateTimpstamp(Timestamp.valueOf(sdf.format(subjectDo.getEndDate())));
			}
			int i = subjectDao.updateSubject(subjectDo);
			if (subjectPriceIntervalDao.listSubjectPriceInterval(subjectDto.getId()).size() > 0
					&& !subjectPriceIntervalDao.deleteBySubjectId(subjectDto.getId())) {
				rollback();
				ServiceException se = new ServiceException();
				se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
				throw se;
			}
			for (SubjectPriceIntervalDTO subjectPriceIntervalDto : subjectDto.getPriceIntervalList()) {
				SubjectPriceIntervalDO subjectPriceIntervalDo = mapper.map(subjectPriceIntervalDto,
						SubjectPriceIntervalDO.class);
				subjectPriceIntervalDo.setSubjectId(subjectDo.getId());
				int j = subjectPriceIntervalDao.addSubjectPriceInterval(subjectPriceIntervalDo);
				if (j <= 0) {
					rollback();
					ServiceException se = new ServiceException();
					se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
					throw se;
				}
			}
			if (i > 0) {
				return subjectDo.getId();
			} else {
				rollback();
				return 0;
			}
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public boolean updateSubjectCategoryId(int id, int categoryId) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		if (categoryId <= 0) {
			ServiceException se = new ServiceException("categoryId error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		if (subjectDao.updateSubjectCategory(id, categoryId) == 1) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int countSubject(String keyword, Integer categoryId, SubjectStateEnum state) throws ServiceException {
		if (state == null) {
			return subjectDao.countSubject(keyword, categoryId, null);
		} else {
			return subjectDao.countSubject(keyword, categoryId, state.toString());
		}

	}

	@Override
	public List<SubjectDTO> listSubject(String keyword, Integer categoryId, SubjectStateEnum state, int pageNum,
			int pageSize) throws ServiceException {
		if (pageNum < 0) {
			pageNum = DEFAULT_PAGE_NUM;
		}
		if (pageSize < 0) {
			pageSize = DEFAULT_PAGE_SIZE;
		}
		List<SubjectDTO> subjectDtoList = new ArrayList<SubjectDTO>();
		List<SubjectDO> subjectDoList = new ArrayList<SubjectDO>();
		try {
			if (state == null) {
				subjectDoList = subjectDao.listSubject(keyword, categoryId, null, pageNum * pageSize, pageSize);
			} else {
				subjectDoList = subjectDao.listSubject(keyword, categoryId, state.toString(), pageNum * pageSize,
						pageSize);
			}
			if (subjectDoList == null) {
				return null;
			}
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (SubjectDO subjectDo : subjectDoList) {
			SubjectDTO subjectDto = mapper.map(subjectDo, SubjectDTO.class);
			subjectDtoList.add(subjectDto);
		}
		return subjectDtoList;
	}

	@Override
	@Transactional
	public int sortSubject(int frontId, int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		SubjectDO subjectDo = new SubjectDO();
		try {
			subjectDo = subjectDao.getSubjectById(id);
			if (frontId > 0) {
				SubjectDO frontSubjectDo = subjectDao.getSubjectById(frontId);
				subjectDo.setWeight(frontSubjectDo.getWeight() - 1);
				subjectDao.updateSubjectWeightPlus(frontSubjectDo.getWeight());
			} else {
				SubjectDO firstSubjectDo = subjectDao.getFirstSubject();
				subjectDo.setWeight(firstSubjectDo.getWeight() + 1);
			}
			subjectDao.updateSubjectWeight(subjectDo.getId(), subjectDo.getWeight());
		} catch (Exception e) {
			rollback();
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		return subjectDo.getId();
	}

	@Override
	public SubjectDTO getSubjectById(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		SubjectDTO subjectDto = null;
		try {
			SubjectDO subjectDo = subjectDao.getSubjectById(id);
			if (subjectDo == null) {
				ServiceException se = new ServiceException("the subject is't exist .");
				se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
				throw se;
			}
			subjectDto = mapper.map(subjectDo, SubjectDTO.class);

			List<SubjectPriceIntervalDTO> subjectPriceIntervalDtoList = new ArrayList<SubjectPriceIntervalDTO>();
			List<SubjectPriceIntervalDO> subjectPriceIntervalDoList = subjectPriceIntervalDao
					.listSubjectPriceInterval(id);
			for (SubjectPriceIntervalDO subjectPriceIntervalDo : subjectPriceIntervalDoList) {
				SubjectPriceIntervalDTO subjectPriceIntervalDto = mapper.map(subjectPriceIntervalDo,
						SubjectPriceIntervalDTO.class);
				subjectPriceIntervalDtoList.add(subjectPriceIntervalDto);
			}
			subjectDto.setPriceIntervalList(subjectPriceIntervalDtoList);

			List<OrderDTO> orderList = new ArrayList<>();
			if (SubjectTypeEnum.INDIE.name().equalsIgnoreCase(subjectDo.getType())) { // 小团统计旗下子团所有订单
				List<SubjectDO> subjectList = subjectDao.listSubjectByParentId(subjectDo.getId());
				List<OrderDO> orderDoList = new ArrayList<>();
				subjectList.forEach(sub -> orderDao.listOrderBySubjectId(sub.getId()).forEach(o -> orderDoList.add(o)));
				orderList = orderDoList.stream().map(oDo -> mapper.map(oDo, OrderDTO.class))
						.collect(Collectors.toList());
			} else {
				List<OrderDO> orderDoList = orderDao.listOrderBySubjectId(subjectDo.getId());
				orderList = orderDoList.stream().map(oDo -> mapper.map(oDo, OrderDTO.class))
						.collect(Collectors.toList());
			}
			orderList.forEach(order -> {
				if (order.getUserId() > 0)
					order.setUserDo(userDao.getUserById(order.getUserId()));
				if (order.getAdviserId() > 0)
					order.setAdviserDo(adviserDao.getAdviserById(order.getAdviserId()));
			});
			subjectDto.setOrderList(orderList);

		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
		return subjectDto;
	}

	@Override
	public int deleteSubjectById(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		return subjectDao.deleteSubjectById(id);
	}

}
