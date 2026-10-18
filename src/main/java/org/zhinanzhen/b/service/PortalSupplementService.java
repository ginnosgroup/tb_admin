package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.service.pojo.PortalSupplementDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface PortalSupplementService {

	PortalSupplementDTO addPortalSupplement(PortalSupplementDTO supplement, List<String> filePaths)
			throws ServiceException;

	PortalSupplementDTO getPortalSupplement(int id) throws ServiceException;

	PortalSupplementDTO getPortalSupplement(int id, Integer portalId, String name) throws ServiceException;

	/** 查询案件补料记录的基础字段，不组装日志或附件。 */
	List<PortalSupplementDTO> listPortalSupplementByPortalId(int portalId) throws ServiceException;

	List<PortalSupplementDTO> listPortalSupplement(Integer portalId, Integer maraId, Integer officialId,
			String strState, int pageNum, int pageSize)
			throws ServiceException;

	List<PortalSupplementDTO> listPortalSupplement(Integer id, Integer portalId, Integer maraId, Integer officialId,
			String strState, String name, int pageNum, int pageSize) throws ServiceException;

	int countPortalSupplement(Integer portalId, Integer maraId, Integer officialId, String strState)
			throws ServiceException;

	int countPortalSupplement(Integer id, Integer portalId, Integer maraId, Integer officialId, String strState,
			String name) throws ServiceException;

	/** Compatibility overload for callers that do not filter by supplement status. */
	List<PortalSupplementDTO> listPortalSupplement(Integer portalId, Integer maraId, Integer officialId,
			int pageNum, int pageSize) throws ServiceException;

	/** Compatibility overload for callers that do not filter by supplement status. */
	int countPortalSupplement(Integer portalId, Integer maraId, Integer officialId) throws ServiceException;

	int updatePortalSupplement(PortalSupplementDTO supplement, List<String> filePaths, boolean updateFilePaths)
			throws ServiceException;

	int deletePortalSupplement(int id) throws ServiceException;
}
