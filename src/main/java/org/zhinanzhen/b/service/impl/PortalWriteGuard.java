package org.zhinanzhen.b.service.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.PortalDAO;
import org.zhinanzhen.b.dao.pojo.PortalDO;
import org.zhinanzhen.tb.service.ServiceException;

import com.ikasoa.core.ErrorCodeEnum;

/** 所有案件写入共用的归档保护；调用方的事务持有案件行锁直到写入完成。 */
@Service
public class PortalWriteGuard {
	public static final String ARCHIVED_MESSAGE = "案件已归档结案，仅允许查看，不能修改、删除或变更附件。";

	@Resource
	private PortalDAO portalDao;

	public void requireEditable(int portalId) throws ServiceException {
		PortalDO portal = portalDao.getPortalForUpdate(portalId);
		if (portal == null)
			throw failure("案件不存在。");
		if ("013".equals(portal.getStrState()))
			throw failure(ARCHIVED_MESSAGE);
	}

	public void requireFileEditable(String filePath) throws ServiceException {
		if (portalDao.countArchivedDocumentReferences(filePath) > 0)
			throw failure(ARCHIVED_MESSAGE);
	}

	private ServiceException failure(String message) {
		ServiceException error = new ServiceException(message);
		error.setCode(ErrorCodeEnum.DATA_ERROR.code());
		return error;
	}
}
