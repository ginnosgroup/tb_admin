package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.PortalAttachmentDAO;
import org.zhinanzhen.b.dao.pojo.PortalAttachmentDO;
import org.zhinanzhen.b.service.PortalAttachmentService;
import org.zhinanzhen.b.service.pojo.PortalAttachmentDTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.ErrorCodeEnum;
import com.ikasoa.core.utils.StringUtil;

@Service("PortalAttachmentService")
public class PortalAttachmentServiceImpl extends BaseService implements PortalAttachmentService {

	@Resource
	private PortalAttachmentDAO portalAttachmentDao;

	@Override
	public int addPortalAttachment(PortalAttachmentDTO portalAttachmentDto) throws ServiceException {
		if (portalAttachmentDto == null) {
			ServiceException se = new ServiceException("portalAttachmentDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			PortalAttachmentDO portalAttachmentDo = mapper.map(portalAttachmentDto, PortalAttachmentDO.class);
			if (portalAttachmentDao.addPortalAttachment(portalAttachmentDo) > 0) {
				portalAttachmentDto.setId(portalAttachmentDo.getId());
				return portalAttachmentDo.getId();
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
	public PortalAttachmentDTO getPortalAttachment(Integer id) throws ServiceException {
		if (id == null || id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			PortalAttachmentDO portalAttachmentDo = portalAttachmentDao.getPortalAttachmentById(id);
			if (portalAttachmentDo == null) {
				ServiceException se = new ServiceException("No data !");
				se.setCode(ErrorCodeEnum.DATA_ERROR.code());
				throw se;
			}
			return mapper.map(portalAttachmentDo, PortalAttachmentDTO.class);
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
	}

	@Override
	public PortalAttachmentDTO getPortalAttachmentByPath(String filePath) throws ServiceException {
		if (filePath == null || filePath.isEmpty()) {
			ServiceException se = new ServiceException("filePath error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			PortalAttachmentDO portalAttachmentDo = portalAttachmentDao.getPortalAttachmentByPath(filePath);
			if (portalAttachmentDo == null)
				return null;
			return mapper.map(portalAttachmentDo, PortalAttachmentDTO.class);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
	}

	@Override
	public List<PortalAttachmentDTO> listPortalAttachmentByPortalId(Integer portalId) throws ServiceException {
		if (portalId == null || portalId <= 0) {
			ServiceException se = new ServiceException("portalId error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			List<PortalAttachmentDO> portalAttachmentDoList = portalAttachmentDao
					.listPortalAttachmentByPortalId(portalId);
			if (portalAttachmentDoList == null || portalAttachmentDoList.isEmpty()) {
				return new ArrayList<PortalAttachmentDTO>();
			}
			List<PortalAttachmentDTO> portalAttachmentDtoList = new ArrayList<PortalAttachmentDTO>();
			for (PortalAttachmentDO portalAttachmentDo : portalAttachmentDoList) {
				portalAttachmentDtoList.add(mapper.map(portalAttachmentDo, PortalAttachmentDTO.class));
			}
			return portalAttachmentDtoList;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
	}

	@Override
	public int updatePortalIdByPathList(List<String> filePathList, int portalId) throws ServiceException {
		if (filePathList == null || filePathList.isEmpty() || portalId <= 0) {
			ServiceException se = new ServiceException("filePathList or portalId error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			return portalAttachmentDao.updatePortalIdByPathList(filePathList, portalId);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int updatePortalIdAndStageByPathList(List<String> filePathList, int portalId, String stage)
			throws ServiceException {
		if (filePathList == null || filePathList.isEmpty() || portalId <= 0 || StringUtil.isEmpty(stage)) {
			ServiceException se = new ServiceException("filePathList, portalId or stage error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			return portalAttachmentDao.updatePortalIdAndStageByPathList(filePathList, portalId, stage);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int deletePortalAttachmentById(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			return portalAttachmentDao.deletePortalAttachmentById(id);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int deletePortalAttachmentByPath(String filePath) throws ServiceException {
		if (filePath == null || filePath.isEmpty()) {
			ServiceException se = new ServiceException("filePath error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			return portalAttachmentDao.deletePortalAttachmentByPath(filePath);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

}
