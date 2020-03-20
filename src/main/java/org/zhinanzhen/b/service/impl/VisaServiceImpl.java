package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.VisaDAO;
import org.zhinanzhen.b.dao.OfficialDAO;
import org.zhinanzhen.b.dao.ReceiveTypeDAO;
import org.zhinanzhen.b.dao.RemindDAO;
import org.zhinanzhen.b.dao.ServiceDAO;
import org.zhinanzhen.b.dao.ServiceOrderReviewDAO;
import org.zhinanzhen.b.dao.pojo.VisaDO;
import org.zhinanzhen.b.dao.pojo.VisaListDO;
import org.zhinanzhen.b.dao.pojo.OfficialDO;
import org.zhinanzhen.b.dao.pojo.ReceiveTypeDO;
import org.zhinanzhen.b.dao.pojo.RemindDO;
import org.zhinanzhen.b.dao.pojo.ServiceDO;
import org.zhinanzhen.b.dao.pojo.ServiceOrderReviewDO;
import org.zhinanzhen.b.service.AbleStateEnum;
import org.zhinanzhen.b.service.VisaService;
import org.zhinanzhen.b.service.pojo.ServiceOrderReviewDTO;
import org.zhinanzhen.b.service.pojo.VisaDTO;
import org.zhinanzhen.tb.dao.AdviserDAO;
import org.zhinanzhen.tb.dao.UserDAO;
import org.zhinanzhen.tb.dao.pojo.AdviserDO;
import org.zhinanzhen.tb.dao.pojo.UserDO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.thrift.ErrorCodeEnum;
import com.ikasoa.core.utils.StringUtil;

@Service("VisaService")
public class VisaServiceImpl extends BaseService implements VisaService {

	@Resource
	private VisaDAO visaDao;

	@Resource
	private AdviserDAO adviserDao;

	@Resource
	private OfficialDAO officialDao;

	@Resource
	private ReceiveTypeDAO receiveTypeDao;

	@Resource
	private ServiceDAO serviceDao;

	@Resource
	private RemindDAO remindDao;

	@Resource
	private UserDAO userDao;

	@Resource
	private ServiceOrderReviewDAO serviceOrderReviewDao;

	@Override
	public int addVisa(VisaDTO visaDto) throws ServiceException {
		if (visaDto == null) {
			ServiceException se = new ServiceException("visaDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			VisaDO visaDo = mapper.map(visaDto, VisaDO.class);
			if (visaDao.addVisa(visaDo) > 0) {
				visaDto.setId(visaDo.getId());
				return visaDo.getId();
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
	public int updateVisa(VisaDTO visaDto) throws ServiceException {
		if (visaDto == null) {
			ServiceException se = new ServiceException("visaDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			putReviews(visaDto);
			VisaDO visaDo = mapper.map(visaDto, VisaDO.class);
			return visaDao.updateVisa(visaDo);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int countVisa(String keyword, String startHandlingDate, String endHandlingDate, List<String> stateList,
			String stardDate, String endDate, Integer adviserId, Integer userId) throws ServiceException {
		return visaDao.countVisa(keyword, startHandlingDate, theDateTo23_59_59(endHandlingDate), stateList, stardDate,
				theDateTo23_59_59(endDate), adviserId, userId);
	}

	@Override
	public List<VisaDTO> listVisa(String keyword, String startHandlingDate, String endHandlingDate,
			List<String> stateList, String stardDate, String endDate, Integer adviserId, Integer userId, int pageNum,
			int pageSize) throws ServiceException {
		if (pageNum < 0) {
			pageNum = DEFAULT_PAGE_NUM;
		}
		if (pageSize < 0) {
			pageSize = DEFAULT_PAGE_SIZE;
		}
		List<VisaDTO> visaDtoList = new ArrayList<>();
		List<VisaListDO> visaListDoList = new ArrayList<>();
		try {
			visaListDoList = visaDao.listVisa(keyword, startHandlingDate, theDateTo23_59_59(endHandlingDate), stateList,
					stardDate, theDateTo23_59_59(endDate), adviserId, userId, pageNum * pageSize, pageSize);
			if (visaListDoList == null) {
				return null;
			}
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (VisaDO visaListDo : visaListDoList) {
			VisaDTO visaDto = mapper.map(visaListDo, VisaDTO.class);
			putReviews(visaDto);
			AdviserDO adviserDo = adviserDao.getAdviserById(visaListDo.getAdviserId());
			if (adviserDo != null) {
				visaDto.setAdviserName(adviserDo.getName());
			}
			OfficialDO officialDo = officialDao.getOfficialById(visaListDo.getOfficialId());
			if (officialDo != null) {
				visaDto.setOfficialName(officialDo.getName());
			}
			ReceiveTypeDO receiveTypeDo = receiveTypeDao.getReceiveTypeById(visaListDo.getReceiveTypeId());
			if (receiveTypeDo != null) {
				visaDto.setReceiveTypeName(receiveTypeDo.getName());
			}
			ServiceDO serviceDo = serviceDao.getServiceById(visaListDo.getServiceId());
			if (serviceDo != null) {
				visaDto.setServiceCode(serviceDo.getCode());
			}
			List<Date> remindDateList = new ArrayList<>();
			List<RemindDO> remindDoList = remindDao.listRemindByVisaId(visaDto.getId(), adviserId,
					AbleStateEnum.ENABLED.toString());
			for (RemindDO remindDo : remindDoList) {
				remindDateList.add(remindDo.getRemindDate());
			}
			visaDto.setRemindDateList(remindDateList);
			visaDtoList.add(visaDto);
		}
		return visaDtoList;
	}

	@Override
	public VisaDTO getVisaById(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		VisaDTO visaDto = null;
		try {
			VisaDO visaDo = visaDao.getVisaById(id);
			if (visaDo == null) {
				return null;
			}
			visaDto = mapper.map(visaDo, VisaDTO.class);
			putReviews(visaDto);
			if (visaDto.getUserId() > 0) {
				UserDO userDo = userDao.getUserById(visaDto.getUserId());
				visaDto.setUserName(userDo.getName());
				visaDto.setPhone(userDo.getPhone());
				visaDto.setBirthday(userDo.getBirthday());
			}
			if (visaDto.getReceiveTypeId() > 0) {
				ReceiveTypeDO receiveTypeDo = receiveTypeDao.getReceiveTypeById(visaDto.getReceiveTypeId());
				if (receiveTypeDo != null)
					visaDto.setReceiveTypeName(receiveTypeDo.getName());
			}
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
		return visaDto;
	}

	@Override
	public int deleteVisaById(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			return visaDao.deleteVisaById(id);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	private void putReviews(VisaDTO visaDto) throws ServiceException {
		List<ServiceOrderReviewDO> serviceOrderReviewDoList = serviceOrderReviewDao
				.listServiceOrderReview(visaDto.getServiceOrderId(), null, null, null, null, null);
		List<ServiceOrderReviewDTO> serviceOrderReviewDtoList = new ArrayList<ServiceOrderReviewDTO>();
		serviceOrderReviewDoList
				.forEach(review -> serviceOrderReviewDtoList.add(mapper.map(review, ServiceOrderReviewDTO.class)));
		if (serviceOrderReviewDtoList != null && serviceOrderReviewDtoList.size() > 0)
			for (ServiceOrderReviewDTO serviceOrderReviewDto : serviceOrderReviewDtoList)
				if (serviceOrderReviewDto != null && StringUtil.isNotEmpty(serviceOrderReviewDto.getKjState())) {
					visaDto.setState(serviceOrderReviewDto.getKjState());
					updateVisa(visaDto);
					break;
				}
	}

}
