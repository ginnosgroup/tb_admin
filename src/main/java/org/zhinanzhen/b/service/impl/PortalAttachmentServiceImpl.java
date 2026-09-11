package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

	@Resource
	private PortalWriteGuard portalWriteGuard;

	private void requireAttachmentEditable(PortalAttachmentDO attachment) throws ServiceException {
		if (attachment != null && attachment.getPortalId() != null && attachment.getPortalId() > 0)
			portalWriteGuard.requireEditable(attachment.getPortalId());
	}

	private void validateAttachmentPaths(List<String> paths, int portalId) throws ServiceException {
		portalWriteGuard.requireEditable(portalId);
		for (String path : paths) {
			PortalAttachmentDO attachment = portalAttachmentDao.getPortalAttachmentByPath(path);
			if (attachment == null) {
				ServiceException error = new ServiceException("未找到已上传的附件：" + path);
				error.setCode(ErrorCodeEnum.DATA_ERROR.code());
				throw error;
			}
			requireAttachmentEditable(attachment);
			if (attachment.getPortalId() != null && attachment.getPortalId() > 0
					&& attachment.getPortalId() != portalId) {
				ServiceException error = new ServiceException("附件已属于其他案件，不能关联到当前案件：" + path);
				error.setCode(ErrorCodeEnum.DATA_ERROR.code());
				throw error;
			}
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int addPortalAttachment(PortalAttachmentDTO portalAttachmentDto) throws ServiceException {
		if (portalAttachmentDto == null) {
			ServiceException se = new ServiceException("portalAttachmentDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			if (portalAttachmentDto.getPortalId() != null && portalAttachmentDto.getPortalId() > 0)
				portalWriteGuard.requireEditable(portalAttachmentDto.getPortalId());
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
	public List<PortalAttachmentDTO> listPortalAttachmentByPortalIdAndFileNameAndStage(Integer portalId,
			String fileName, String stage) throws ServiceException {
		if (portalId == null || portalId <= 0 || StringUtil.isEmpty(fileName) || StringUtil.isEmpty(stage)) {
			ServiceException se = new ServiceException("portalId、fileName或stage error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			List<PortalAttachmentDO> portalAttachmentDoList = portalAttachmentDao
					.listPortalAttachmentByPortalIdAndFileNameAndStage(portalId, fileName.trim(), stage.trim());
			if (portalAttachmentDoList == null || portalAttachmentDoList.isEmpty())
				return new ArrayList<PortalAttachmentDTO>();
			List<PortalAttachmentDTO> portalAttachmentDtoList = new ArrayList<PortalAttachmentDTO>();
			for (PortalAttachmentDO portalAttachmentDo : portalAttachmentDoList)
				portalAttachmentDtoList.add(mapper.map(portalAttachmentDo, PortalAttachmentDTO.class));
			return portalAttachmentDtoList;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
	}

	@Override
	public List<PortalAttachmentDTO> listPortalAttachment(Integer id, Integer portalId, String attachmentState,
			String stage, String filePath, String fileName, int pageNum, int pageSize) throws ServiceException {
		if (id != null && id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		if (portalId != null && portalId <= 0) {
			ServiceException se = new ServiceException("portalId error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		if (pageNum < 0 || pageSize <= 0) {
			ServiceException se = new ServiceException("pageNum或pageSize参数错误！");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			List<PortalAttachmentDO> portalAttachmentDoList = portalAttachmentDao.listPortalAttachment(id, portalId,
					normalizeOptionalText(attachmentState), normalizeOptionalText(stage), normalizeOptionalText(filePath),
					normalizeOptionalText(fileName), pageNum * pageSize, pageSize);
			List<PortalAttachmentDTO> portalAttachmentDtoList = new ArrayList<PortalAttachmentDTO>();
			if (portalAttachmentDoList != null) {
				for (PortalAttachmentDO portalAttachmentDo : portalAttachmentDoList) {
					portalAttachmentDtoList.add(mapper.map(portalAttachmentDo, PortalAttachmentDTO.class));
				}
			}
			return portalAttachmentDtoList;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int updatePortalAttachment(PortalAttachmentDTO portalAttachmentDto) throws ServiceException {
		if (portalAttachmentDto == null || portalAttachmentDto.getId() <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			PortalAttachmentDO currentAttachment = portalAttachmentDao
					.getPortalAttachmentById(portalAttachmentDto.getId());
			if (currentAttachment == null) {
				ServiceException se = new ServiceException("附件不存在.");
				se.setCode(ErrorCodeEnum.DATA_ERROR.code());
				throw se;
			}
			requireAttachmentEditable(currentAttachment);
			if (StringUtil.isNotEmpty(currentAttachment.getFilePath()))
				portalWriteGuard.requireFileEditable(currentAttachment.getFilePath());
			if (portalAttachmentDto.getPortalId() != null) {
				if (portalAttachmentDto.getPortalId() <= 0) {
					ServiceException se = new ServiceException("portalId error !");
					se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
					throw se;
				}
				portalWriteGuard.requireEditable(portalAttachmentDto.getPortalId());
			}
			PortalAttachmentDO portalAttachmentDo = mapper.map(portalAttachmentDto, PortalAttachmentDO.class);
			return portalAttachmentDao.updatePortalAttachment(portalAttachmentDo);
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	private String normalizeOptionalText(String value) {
		if (value == null)
			return null;
		String normalizedValue = value.trim();
		return normalizedValue.isEmpty() ? null : normalizedValue;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int updatePortalIdByPathList(List<String> filePathList, int portalId) throws ServiceException {
		if (filePathList == null || filePathList.isEmpty() || portalId <= 0) {
			ServiceException se = new ServiceException("filePathList or portalId error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			validateAttachmentPaths(filePathList, portalId);
			return portalAttachmentDao.updatePortalIdByPathList(filePathList, portalId);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int updatePortalIdAndStageByPathList(List<String> filePathList, int portalId, String stage)
			throws ServiceException {
		if (filePathList == null || filePathList.isEmpty() || portalId <= 0 || StringUtil.isEmpty(stage)) {
			ServiceException se = new ServiceException("filePathList, portalId or stage error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			validateAttachmentPaths(filePathList, portalId);
			return portalAttachmentDao.updatePortalIdAndStageByPathList(filePathList, portalId, stage);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int deletePortalAttachmentById(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			PortalAttachmentDO attachment = portalAttachmentDao.getPortalAttachmentById(id);
			requireAttachmentEditable(attachment);
			if (attachment != null)
				portalWriteGuard.requireFileEditable(attachment.getFilePath());
			return portalAttachmentDao.deletePortalAttachmentById(id);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int deletePortalAttachmentByIdAndPortalIdAndFileNameAndStage(int id, int portalId, String fileName,
			String stage) throws ServiceException {
		if (id <= 0 || portalId <= 0 || StringUtil.isEmpty(fileName) || StringUtil.isEmpty(stage)) {
			ServiceException se = new ServiceException("id、portalId、fileName或stage error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			portalWriteGuard.requireEditable(portalId);
			PortalAttachmentDO attachment = portalAttachmentDao.getPortalAttachmentById(id);
			if (attachment == null || attachment.getPortalId() == null || attachment.getPortalId() != portalId
					|| !fileName.trim().equalsIgnoreCase(attachment.getFileName() == null ? "" : attachment.getFileName().trim())
					|| !stage.trim().equalsIgnoreCase(attachment.getStage() == null ? "" : attachment.getStage().trim()))
				return 0;
			requireAttachmentEditable(attachment);
			if (StringUtil.isNotEmpty(attachment.getFilePath()))
				portalWriteGuard.requireFileEditable(attachment.getFilePath());
			return portalAttachmentDao.deletePortalAttachmentByIdAndPortalIdAndFileNameAndStage(id, portalId,
					fileName.trim(), stage.trim());
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int deletePortalAttachmentByPath(String filePath) throws ServiceException {
		if (filePath == null || filePath.isEmpty()) {
			ServiceException se = new ServiceException("filePath error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			portalWriteGuard.requireFileEditable(filePath);
			requireAttachmentEditable(portalAttachmentDao.getPortalAttachmentByPath(filePath));
			return portalAttachmentDao.deletePortalAttachmentByPath(filePath);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

}
