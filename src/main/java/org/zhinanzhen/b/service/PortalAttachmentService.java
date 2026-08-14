package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.service.pojo.PortalAttachmentDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface PortalAttachmentService {

	int addPortalAttachment(PortalAttachmentDTO portalAttachmentDto) throws ServiceException;

	PortalAttachmentDTO getPortalAttachment(Integer id) throws ServiceException;

	PortalAttachmentDTO getPortalAttachmentByPath(String filePath) throws ServiceException;

	int updatePortalIdByPathList(List<String> filePathList, int portalId) throws ServiceException;

	int deletePortalAttachmentById(int id) throws ServiceException;

	int deletePortalAttachmentByPath(String filePath) throws ServiceException;

}
