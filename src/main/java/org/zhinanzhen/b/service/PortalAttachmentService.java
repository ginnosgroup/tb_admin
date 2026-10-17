package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.service.pojo.PortalAttachmentDTO;
import org.zhinanzhen.b.service.pojo.PortalAttachmentUploadResult;
import org.zhinanzhen.tb.service.ServiceException;

public interface PortalAttachmentService {

	int addPortalAttachment(PortalAttachmentDTO portalAttachmentDto) throws ServiceException;

	/** 保存申请材料：同名替换；同组多附件依次编号为1-1、1-2等。 */
	PortalAttachmentUploadResult addApplicationPortalAttachment(PortalAttachmentDTO portalAttachmentDto)
			throws ServiceException;

	PortalAttachmentDTO getPortalAttachment(Integer id) throws ServiceException;

	PortalAttachmentDTO getPortalAttachmentByPath(String filePath) throws ServiceException;

	List<PortalAttachmentDTO> listPortalAttachmentByPortalId(Integer portalId) throws ServiceException;

	List<PortalAttachmentDTO> listPortalAttachmentByPortalIdAndFileSorting(Integer portalId, Integer fileSorting)
			throws ServiceException;

	List<PortalAttachmentDTO> listPortalAttachmentByPortalIdAndStage(Integer portalId, String stage)
			throws ServiceException;

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

	/** 将已上传附件挂到指定补料轮次；未关联案件的附件会同时挂到该案件。 */
	int updateSupplementAttachmentPaths(List<String> filePathList, int portalId, int supplementId)
			throws ServiceException;

	/** 清除指定补料轮次所关联的附件。 */
	int clearSupplementAttachmentPaths(int portalId, int supplementId) throws ServiceException;

	List<String> listPortalAttachmentPathsBySupplementId(int supplementId) throws ServiceException;

	List<PortalAttachmentDTO> listPortalAttachmentBySupplementId(int supplementId) throws ServiceException;

	int deletePortalAttachmentById(int id) throws ServiceException;

	int deletePortalAttachmentByIdAndPortalIdAndFileNameAndStage(int id, int portalId, String fileName, String stage)
			throws ServiceException;

	int deletePortalAttachmentByIdAndPortalIdAndFileSorting(int id, int portalId, int fileSorting)
			throws ServiceException;

	int deletePortalArchiveAttachmentByIdAndPortalIdAndStage(int id, int portalId, String stage)
			throws ServiceException;

	int deletePortalAttachmentByPath(String filePath) throws ServiceException;

}
