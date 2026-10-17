package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zhinanzhen.b.dao.PortalAttachmentDAO;
import org.zhinanzhen.b.dao.pojo.PortalAttachmentDO;
import org.zhinanzhen.b.service.PortalAttachmentService;
import org.zhinanzhen.b.service.pojo.PortalAttachmentDTO;
import org.zhinanzhen.b.service.pojo.PortalAttachmentUploadResult;
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
		// 请求可能携带重复路径，按路径去重后再查，避免重复校验。
		java.util.Set<String> uniquePaths = new java.util.LinkedHashSet<String>(paths);
		for (String path : uniquePaths) {
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
	@Transactional(rollbackFor = Exception.class)
	public PortalAttachmentUploadResult addApplicationPortalAttachment(PortalAttachmentDTO attachment)
			throws ServiceException {
		if (attachment == null || !"application".equalsIgnoreCase(attachment.getStage())
				|| StringUtil.isEmpty(attachment.getFileName())) {
			ServiceException error = new ServiceException("申请材料附件参数错误.");
			error.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw error;
		}
		try {
			PortalAttachmentUploadResult result = new PortalAttachmentUploadResult();
			Integer portalId = attachment.getPortalId();
			if (portalId != null && portalId > 0) {
				// 案件行锁串行化同一案件的上传，同名删除与新增共享一个事务。
				portalWriteGuard.requireEditable(portalId);
				List<PortalAttachmentDO> sameName = portalAttachmentDao
						.listPortalAttachmentByPortalIdAndFileNameAndStage(portalId,
								attachment.getFileName().trim(), "application");
				if (sameName != null) {
					for (PortalAttachmentDO old : sameName) {
						if (old == null || !attachment.getFileName().trim().equalsIgnoreCase(
								old.getFileName() == null ? "" : old.getFileName().trim()))
							continue;
						portalWriteGuard.requireFileEditable(old.getFilePath());
						if (portalAttachmentDao.deletePortalAttachmentByIdAndPortalIdAndFileNameAndStage(
								old.getId(), portalId, attachment.getFileName().trim(), "application") <= 0)
							throw applicationSaveFailure("旧同名附件已发生变更，请重试.");
						if (StringUtil.isNotEmpty(old.getFilePath()))
							result.getReplacedFilePaths().add(old.getFilePath());
					}
				}
			}
			// file_sorting按前端传入的值直接保存，不再分配"5-1"这类顺序后缀。
			int id = addPortalAttachment(attachment);
			if (id <= 0)
				throw applicationSaveFailure("附件信息保存失败.");
			result.setAttachmentId(id);
			return result;
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			ServiceException error = new ServiceException(e);
			error.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw error;
		}
	}

	private ServiceException applicationSaveFailure(String message) {
		ServiceException error = new ServiceException(message);
		error.setCode(ErrorCodeEnum.OTHER_ERROR.code());
		return error;
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
	public List<PortalAttachmentDTO> listPortalAttachmentByPortalIdAndFileSorting(Integer portalId,
			Integer fileSorting) throws ServiceException {
		if (portalId == null || portalId <= 0 || fileSorting == null) {
			ServiceException se = new ServiceException("portalId或fileSorting error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			List<PortalAttachmentDO> attachments = portalAttachmentDao
					.listPortalAttachmentByPortalIdAndFileSorting(portalId, fileSorting.toString());
			List<PortalAttachmentDTO> result = new ArrayList<PortalAttachmentDTO>();
			if (attachments != null)
				for (PortalAttachmentDO attachment : attachments)
					result.add(mapper.map(attachment, PortalAttachmentDTO.class));
			return result;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
	}

	@Override
	public List<PortalAttachmentDTO> listPortalAttachmentByPortalIdAndStage(Integer portalId, String stage)
			throws ServiceException {
		if (portalId == null || portalId <= 0 || StringUtil.isEmpty(stage)) {
			ServiceException se = new ServiceException("portalId或stage error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			List<PortalAttachmentDO> portalAttachmentDoList = portalAttachmentDao
					.listPortalAttachmentByPortalIdAndStage(portalId, stage.trim());
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
	public List<PortalAttachmentDTO> listPortalArchiveAttachmentByPortalIdAndStage(Integer portalId, String stage)
			throws ServiceException {
		if (portalId == null || portalId <= 0 || StringUtil.isEmpty(stage)) {
			ServiceException se = new ServiceException("portalId或stage error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			List<PortalAttachmentDO> portalAttachmentDoList = portalAttachmentDao
					.listPortalArchiveAttachmentByPortalIdAndStage(portalId, stage.trim());
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
	public int updateSupplementAttachmentPaths(List<String> filePathList, int portalId, int supplementId)
			throws ServiceException {
		if (filePathList == null || filePathList.isEmpty() || portalId <= 0 || supplementId <= 0)
			throw parameterError("filePathList, portalId或supplementId参数错误。");
		try {
			List<String> uniquePaths = new ArrayList<String>(new java.util.LinkedHashSet<String>(filePathList));
			validateAttachmentPaths(uniquePaths, portalId);
			for (String path : uniquePaths) {
				Integer currentSupplementId = portalAttachmentDao.getSupplementIdByPath(path);
				if (currentSupplementId != null && currentSupplementId.intValue() != supplementId)
					throw parameterError("附件已经关联到其他补料轮次，不能重复关联：" + path);
			}
			return portalAttachmentDao.updateSupplementIdByPathList(uniquePaths, portalId, supplementId);
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
	public int clearSupplementAttachmentPaths(int portalId, int supplementId) throws ServiceException {
		if (portalId <= 0 || supplementId <= 0)
			throw parameterError("portalId或supplementId参数错误。");
		try {
			return portalAttachmentDao.clearSupplementIdByPortalIdAndSupplementId(portalId, supplementId);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public List<String> listPortalAttachmentPathsBySupplementId(int supplementId) throws ServiceException {
		if (supplementId <= 0)
			throw parameterError("supplementId参数错误。");
		try {
			return portalAttachmentDao.listPortalAttachmentPathsBySupplementId(supplementId);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public List<PortalAttachmentDTO> listPortalAttachmentBySupplementId(int supplementId) throws ServiceException {
		if (supplementId <= 0)
			throw parameterError("supplementId参数错误。");
		try {
			List<PortalAttachmentDO> attachments = portalAttachmentDao.listPortalAttachmentBySupplementId(supplementId);
			List<PortalAttachmentDTO> result = new ArrayList<PortalAttachmentDTO>();
			if (attachments != null) {
				for (PortalAttachmentDO attachment : attachments)
					result.add(mapper.map(attachment, PortalAttachmentDTO.class));
			}
			return result;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	private ServiceException parameterError(String message) {
		ServiceException se = new ServiceException(message);
		se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
		return se;
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
	public int deletePortalAttachmentByIdAndPortalIdAndFileSorting(int id, int portalId, int fileSorting)
			throws ServiceException {
		if (id <= 0 || portalId <= 0) {
			ServiceException se = new ServiceException("id或portalId error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			portalWriteGuard.requireEditable(portalId);
			PortalAttachmentDO attachment = portalAttachmentDao.getPortalAttachmentById(id);
			if (attachment == null || attachment.getPortalId() == null
					|| attachment.getPortalId().intValue() != portalId || attachment.getFileSorting() == null
					|| !Integer.toString(fileSorting).equals(attachment.getFileSorting()))
				return 0;
			requireAttachmentEditable(attachment);
			if (StringUtil.isNotEmpty(attachment.getFilePath()))
				portalWriteGuard.requireFileEditable(attachment.getFilePath());
			return portalAttachmentDao.deletePortalAttachmentByIdAndPortalIdAndFileSorting(id, portalId,
					Integer.toString(fileSorting));
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
	public int deletePortalArchiveAttachmentByIdAndPortalIdAndStage(int id, int portalId, String stage)
			throws ServiceException {
		if (id <= 0 || portalId <= 0 || StringUtil.isEmpty(stage)) {
			ServiceException se = new ServiceException("id、portalId或stage error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			portalWriteGuard.requireEditable(portalId);
			PortalAttachmentDO attachment = portalAttachmentDao.getPortalAttachmentById(id);
			if (attachment == null || attachment.getPortalId() == null || attachment.getPortalId() != portalId
					|| !stage.trim().equalsIgnoreCase(attachment.getStage() == null ? "" : attachment.getStage().trim())
					|| !isArchiveAttachment(attachment))
				return 0;
			requireAttachmentEditable(attachment);
			if (StringUtil.isNotEmpty(attachment.getFilePath()))
				portalWriteGuard.requireFileEditable(attachment.getFilePath());
			return portalAttachmentDao.deletePortalArchiveAttachmentByIdAndPortalIdAndStage(id, portalId,
					stage.trim());
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	private boolean isArchiveAttachment(PortalAttachmentDO attachment) {
		String fileExt = attachment.getFileExt() == null ? ""
				: attachment.getFileExt().trim().toLowerCase(Locale.ENGLISH);
		if ("zip".equals(fileExt) || "rar".equals(fileExt))
			return true;
		String fileType = attachment.getFileType() == null ? ""
				: attachment.getFileType().trim().toLowerCase(Locale.ENGLISH);
		if ("application/zip".equals(fileType) || "application/vnd.rar".equals(fileType)
				|| "application/x-rar-compressed".equals(fileType))
			return true;
		String fileName = attachment.getFileName() == null ? ""
				: attachment.getFileName().trim().toLowerCase(Locale.ENGLISH);
		return fileName.endsWith(".zip") || fileName.endsWith(".rar");
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
