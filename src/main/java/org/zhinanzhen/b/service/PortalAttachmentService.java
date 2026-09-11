package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.service.pojo.PortalAttachmentDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface PortalAttachmentService {

	int addPortalAttachment(PortalAttachmentDTO portalAttachmentDto) throws ServiceException;

	PortalAttachmentDTO getPortalAttachment(Integer id) throws ServiceException;

	PortalAttachmentDTO getPortalAttachmentByPath(String filePath) throws ServiceException;

	List<PortalAttachmentDTO> listPortalAttachmentByPortalId(Integer portalId) throws ServiceException;

	List<PortalAttachmentDTO> listPortalAttachmentByPortalIdAndFileNameAndStage(Integer portalId, String fileName,
			String stage) throws ServiceException;

	List<PortalAttachmentDTO> listPortalArchiveAttachmentByPortalIdAndStage(Integer portalId, String stage)
			throws ServiceException;

	List<PortalAttachmentDTO> listPortalAttachment(Integer id, Integer portalId, String attachmentState,
			String stage, String filePath, String fileName, int pageNum, int pageSize) throws ServiceException;

	int updatePortalAttachment(PortalAttachmentDTO portalAttachmentDto) throws ServiceException;

	int updatePortalIdByPathList(List<String> filePathList, int portalId) throws ServiceException;

	int updatePortalIdAndStageByPathList(List<String> filePathList, int portalId, String stage)
			throws ServiceException;

	int deletePortalAttachmentById(int id) throws ServiceException;

	int deletePortalAttachmentByIdAndPortalIdAndFileNameAndStage(int id, int portalId, String fileName, String stage)
			throws ServiceException;

	int deletePortalArchiveAttachmentByIdAndPortalIdAndStage(int id, int portalId, String stage)
			throws ServiceException;

	int deletePortalAttachmentByPath(String filePath) throws ServiceException;

}
