package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.ReceiveTypeDAO;
import org.zhinanzhen.b.dao.RefundDAO;
import org.zhinanzhen.b.dao.pojo.ReceiveTypeDO;
import org.zhinanzhen.b.dao.pojo.RefundDO;
import org.zhinanzhen.b.dao.pojo.RefundListDO;
import org.zhinanzhen.b.service.RefundService;
import org.zhinanzhen.b.service.pojo.RefundDTO;
import org.zhinanzhen.tb.dao.AdviserDAO;
import org.zhinanzhen.tb.dao.pojo.AdviserDO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.thrift.ErrorCodeEnum;

@Service("RefundService")
public class RefundServiceImpl extends BaseService implements RefundService {

	@Resource
	private RefundDAO refundDao;

	@Resource
	private AdviserDAO adviserDao;

	@Resource
	private ReceiveTypeDAO receiveTypeDao;

	@Override
	public int addRefund(RefundDTO refundDto) throws ServiceException {
		if (refundDto == null) {
			ServiceException se = new ServiceException("refundDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			RefundDO refundDo = mapper.map(refundDto, RefundDO.class);
			if (refundDao.addRefund(refundDo) > 0) {
				return refundDo.getId();
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
	public int updateRefund(RefundDTO refundDto) throws ServiceException {
		if (refundDto == null) {
			ServiceException se = new ServiceException("refundDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			RefundDO refundDo = mapper.map(refundDto, RefundDO.class);
			return refundDao.updateRefund(refundDo);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int countRefund(String keyword, String startHandlingDate, String endHandlingDate, String startDate,
			String endDate, Integer adviserId, Integer officialId, Integer userId) throws ServiceException {
		return refundDao.countRefund(keyword, startHandlingDate, endHandlingDate, startDate, endDate, adviserId,
				officialId, userId);
	}

	@Override
	public List<RefundDTO> listRefund(String keyword, String startHandlingDate, String endHandlingDate,
			String startDate, String endDate, Integer adviserId, Integer officialId, Integer userId, int pageNum,
			int pageSize) throws ServiceException {
		if (pageNum < 0) {
			pageNum = DEFAULT_PAGE_NUM;
		}
		if (pageSize < 0) {
			pageSize = DEFAULT_PAGE_SIZE;
		}
		List<RefundDTO> refundDtoList = new ArrayList<>();
		List<RefundListDO> refundListDoList = new ArrayList<>();
		try {
			refundListDoList = refundDao.listRefund(keyword, startHandlingDate, endHandlingDate, startDate, endDate,
					adviserId, officialId, userId, pageNum * pageSize, pageSize);
			if (refundListDoList == null) {
				return null;
			}
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (RefundListDO refundListDo : refundListDoList) {
			RefundDTO refundDto = mapper.map(refundListDo, RefundDTO.class);
			AdviserDO adviserDo = adviserDao.getAdviserById(refundListDo.getAdviserId());
			if (adviserDo != null) {
				refundDto.setAdviserName(adviserDo.getName());
			}
			ReceiveTypeDO receiveTypeDo = receiveTypeDao.getReceiveTypeById(refundListDo.getReceiveTypeId());
			if (receiveTypeDo != null) {
				refundDto.setReceiveTypeName(receiveTypeDo.getName());
			}
			refundDtoList.add(refundDto);
		}
		return refundDtoList;
	}

	@Override
	public RefundDTO getRefundById(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		RefundDTO refundDto = null;
		try {
			RefundDO refundDo = refundDao.getRefundById(id);
			if (refundDo == null) {
				return null;
			}
			refundDto = mapper.map(refundDo, RefundDTO.class);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
		return refundDto;
	}

	@Override
	public int deleteRefundById(int id) throws ServiceException {
		return refundDao.deleteRefund(id);
	}

}
