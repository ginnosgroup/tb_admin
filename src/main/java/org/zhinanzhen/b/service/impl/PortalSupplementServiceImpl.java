package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zhinanzhen.b.dao.PortalSupplementDAO;
import org.zhinanzhen.b.dao.pojo.PortalSupplementDO;
import org.zhinanzhen.b.service.PortalAttachmentService;
import org.zhinanzhen.b.service.PortalLogService;
import org.zhinanzhen.b.service.PortalSupplementService;
import org.zhinanzhen.b.service.pojo.PortalSupplementDTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.ErrorCodeEnum;

@Service("PortalSupplementService")
public class PortalSupplementServiceImpl extends BaseService implements PortalSupplementService {

	private static final String WAITING_CLIENT = "WAITING_CLIENT";
	private static final String CUSTOMER_SUBMITTED = "CUSTOMER_SUBMITTED";
	private static final String REVIEWING = "REVIEWING";
	private static final String REJECTED = "REJECTED";
	private static final String APPROVED = "APPROVED";

	@Resource
	private PortalSupplementDAO portalSupplementDao;

	@Resource
	private PortalAttachmentService portalAttachmentService;

	@Resource
	private PortalLogService portalLogService;

	@Resource
	private PortalWriteGuard portalWriteGuard;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public PortalSupplementDTO addPortalSupplement(PortalSupplementDTO supplement, List<String> filePaths)
			throws ServiceException {
		if (supplement == null || supplement.getPortalId() <= 0)
			throw parameterError("portalId必须是有效数字。");
		try {
			// 通过案件行锁串行化同一案件的轮次分配，避免并发新增产生相同round_no。
			portalWriteGuard.requireEditable(supplement.getPortalId());
			PortalSupplementDO supplementDo = mapper.map(supplement, PortalSupplementDO.class);
			supplementDo.setRoundNo(portalSupplementDao.getMaxRoundNoByPortalId(supplement.getPortalId()) + 1);
			supplementDo.setStatus(WAITING_CLIENT);
			supplementDo.setRequestedAt(supplement.getRequestedAt() == null ? new Date() : supplement.getRequestedAt());
			if (portalSupplementDao.addPortalSupplement(supplementDo) <= 0)
				throw dataError("补料记录保存失败。");
			if (filePaths != null && !filePaths.isEmpty())
				portalAttachmentService.updateSupplementAttachmentPaths(filePaths, supplement.getPortalId(),
						supplementDo.getId());
			PortalSupplementDTO created = mapper.map(supplementDo, PortalSupplementDTO.class);
			created.setFilePath(joinFilePaths(
					portalAttachmentService.listPortalAttachmentPathsBySupplementId(supplementDo.getId())));
			return created;
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			throw executeError(e);
		}
	}

	@Override
	public PortalSupplementDTO getPortalSupplement(int id) throws ServiceException {
		if (id <= 0)
			throw parameterError("id必须是有效数字。");
		try {
			PortalSupplementDO supplement = portalSupplementDao.getPortalSupplementById(id);
			if (supplement == null)
				return null;
			PortalSupplementDTO result = mapper.map(supplement, PortalSupplementDTO.class);
			result.setFilePath(joinFilePaths(portalAttachmentService
					.listPortalAttachmentPathsBySupplementId(supplement.getId())));
			result.setPortalLogList(portalLogService.listPortalLogBySupplementId(supplement.getId()));
			result.setPortalAttachmentList(
					portalAttachmentService.listPortalAttachmentBySupplementId(supplement.getId()));
			return result;
		} catch (Exception e) {
			throw executeError(e);
		}
	}

	@Override
	public List<PortalSupplementDTO> listPortalSupplementByPortalId(int portalId) throws ServiceException {
		if (portalId <= 0)
			throw parameterError("portalId必须是有效数字。");
		try {
			List<PortalSupplementDO> rows = portalSupplementDao.listPortalSupplementByPortalId(portalId);
			List<PortalSupplementDTO> result = new ArrayList<PortalSupplementDTO>();
			if (rows != null) {
				for (PortalSupplementDO row : rows)
					result.add(mapper.map(row, PortalSupplementDTO.class));
			}
			return result;
		} catch (Exception e) {
			throw executeError(e);
		}
	}

	@Override
	public List<PortalSupplementDTO> listPortalSupplement(Integer portalId, Integer maraId, Integer officialId,
			int pageNum, int pageSize)
			throws ServiceException {
		validateListParameters(portalId, maraId, officialId, pageNum, pageSize);
		try {
			List<PortalSupplementDO> rows = portalSupplementDao.listPortalSupplement(portalId, maraId, officialId,
					pageNum * pageSize, pageSize);
			List<PortalSupplementDTO> result = new ArrayList<PortalSupplementDTO>();
			if (rows != null) {
				for (PortalSupplementDO row : rows) {
					PortalSupplementDTO item = mapper.map(row, PortalSupplementDTO.class);
					item.setFilePath(joinFilePaths(portalAttachmentService
							.listPortalAttachmentPathsBySupplementId(row.getId())));
					item.setPortalLogList(portalLogService.listPortalLogBySupplementId(row.getId()));
					item.setPortalAttachmentList(portalAttachmentService.listPortalAttachmentBySupplementId(row.getId()));
					result.add(item);
				}
			}
			return result;
		} catch (Exception e) {
			throw executeError(e);
		}
	}

	@Override
	public int countPortalSupplement(Integer portalId, Integer maraId, Integer officialId) throws ServiceException {
		validateFilterParameters(portalId, maraId, officialId);
		try {
			return portalSupplementDao.countPortalSupplement(portalId, maraId, officialId);
		} catch (Exception e) {
			throw executeError(e);
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int updatePortalSupplement(PortalSupplementDTO supplement, List<String> filePaths,
			boolean updateFilePaths) throws ServiceException {
		if (supplement == null || supplement.getId() <= 0 || supplement.getPortalId() <= 0)
			throw parameterError("id和portalId必须是有效数字。");
		try {
			PortalSupplementDO current = portalSupplementDao.getPortalSupplementById(supplement.getId());
			if (current == null || current.getPortalId() != supplement.getPortalId())
				throw dataError("补料记录不存在。");
			portalWriteGuard.requireEditable(current.getPortalId());
			// 锁内重读，确保删除或修改并发时不会更新错误记录。
			current = portalSupplementDao.getPortalSupplementForUpdate(supplement.getId());
			if (current == null || current.getPortalId() != supplement.getPortalId())
				throw dataError("补料记录不存在。");
			PortalSupplementDO update = mapper.map(supplement, PortalSupplementDO.class);
			applyStatusTimestamp(update, current);
			int updated = portalSupplementDao.updatePortalSupplement(update);
			// filePath表示本次新增或补充关联的附件；保留该轮次已关联的其他附件。
			if (updateFilePaths && filePaths != null && !filePaths.isEmpty())
				portalAttachmentService.updateSupplementAttachmentPaths(filePaths, current.getPortalId(),
						current.getId());
			return updated;
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			throw executeError(e);
		}
	}

	/** 状态发生变化时自动记录对应节点时间，避免客户端传入或伪造时间。 */
	private void applyStatusTimestamp(PortalSupplementDO update, PortalSupplementDO current) {
		String targetStatus = update.getStatus();
		if (targetStatus == null || targetStatus.trim().isEmpty()
				|| targetStatus.equals(current.getStatus()))
			return;
		if (WAITING_CLIENT.equals(targetStatus)) {
			update.setClearSubsequentTimestamps(true);
			update.setReopenAt(new Date());
			update.setReopenRemark(update.getReviewRemark() == null
					? current.getReviewRemark() : update.getReviewRemark());
			return;
		}
		Date now = new Date();
		if (CUSTOMER_SUBMITTED.equals(targetStatus)) {
			update.setCustomerSubmittedAt(now);
			update.setClearReopenFields(true);
		} else if (REVIEWING.equals(targetStatus)) {
			update.setSubmittedToMaraAt(now);
		} else if (REJECTED.equals(targetStatus) || APPROVED.equals(targetStatus)) {
			update.setReviewedAt(now);
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int deletePortalSupplement(int id) throws ServiceException {
		if (id <= 0)
			throw parameterError("id必须是有效数字。");
		try {
			PortalSupplementDO current = portalSupplementDao.getPortalSupplementById(id);
			if (current == null)
				return 0;
			portalWriteGuard.requireEditable(current.getPortalId());
			current = portalSupplementDao.getPortalSupplementForUpdate(id);
			if (current == null)
				return 0;
			portalAttachmentService.clearSupplementAttachmentPaths(current.getPortalId(), current.getId());
			return portalSupplementDao.deletePortalSupplementById(id);
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			throw executeError(e);
		}
	}

	private void validateListParameters(Integer portalId, Integer maraId, Integer officialId, int pageNum, int pageSize)
			throws ServiceException {
		validateFilterParameters(portalId, maraId, officialId);
		if (pageNum < 0 || pageSize <= 0)
			throw parameterError("portalId、pageNum或pageSize参数错误。");
	}

	private void validateFilterParameters(Integer portalId, Integer maraId, Integer officialId) throws ServiceException {
		if ((portalId != null && portalId <= 0) || (maraId != null && maraId <= 0)
				|| (officialId != null && officialId <= 0))
			throw parameterError("portalId、maraId或officialId必须是有效数字。");
	}

	private String joinFilePaths(List<String> paths) {
		if (paths == null || paths.isEmpty())
			return null;
		StringBuilder joined = new StringBuilder();
		for (String path : paths) {
			if (path == null || path.trim().isEmpty())
				continue;
			if (joined.length() > 0)
				joined.append(',');
			joined.append(path.trim());
		}
		return joined.length() == 0 ? null : joined.toString();
	}

	private ServiceException parameterError(String message) {
		return serviceError(message, ErrorCodeEnum.PARAMETER_ERROR.code(), null);
	}

	private ServiceException dataError(String message) {
		return serviceError(message, ErrorCodeEnum.DATA_ERROR.code(), null);
	}

	private ServiceException executeError(Exception cause) {
		return serviceError(cause.getMessage(), ErrorCodeEnum.EXECUTE_ERROR.code(), cause);
	}

	private ServiceException serviceError(String message, int code, Exception cause) {
		ServiceException error = cause == null ? new ServiceException(message) : new ServiceException(cause);
		error.setCode(code);
		return error;
	}
}
