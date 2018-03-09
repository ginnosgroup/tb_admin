package org.zhinanzhen.b.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.RefundDAO;
import org.zhinanzhen.b.dao.pojo.RefundDO;
import org.zhinanzhen.b.service.RefundService;
import org.zhinanzhen.b.service.pojo.RefundDTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.thrift.ErrorCodeEnum;

@Service("RefundService")
public class RefundServiceImpl extends BaseService implements RefundService {

	@Resource
	private RefundDAO refundDao;

	@Override
	public int addRefund(RefundDTO refundDto) throws ServiceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int updateRefund(RefundDTO refundDto) throws ServiceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int countRefund(String keyword, String startHandlingDate, String endHandlingDate, String startDate,
			String endDate, Integer adviserId, Integer officialId) throws ServiceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<RefundDTO> listRefund(String keyword, String startHandlingDate, String endHandlingDate,
			String startDate, String endDate, Integer adviserId, Integer officialId, int pageNum, int pageSize)
			throws ServiceException {
		// TODO Auto-generated method stub
		return null;
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
