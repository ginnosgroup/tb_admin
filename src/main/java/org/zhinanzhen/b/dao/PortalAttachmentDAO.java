package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.PortalAttachmentDO;

public interface PortalAttachmentDAO {

	int addPortalAttachment(PortalAttachmentDO portalAttachmentDo);

	PortalAttachmentDO getPortalAttachmentById(@Param("id") int id);

	PortalAttachmentDO getPortalAttachmentByPath(@Param("filePath") String filePath);

	int updatePortalIdByPathList(@Param("filePathList") List<String> filePathList, @Param("portalId") int portalId);

	int deletePortalAttachmentById(int id);

	int deletePortalAttachmentByPath(@Param("filePath") String filePath);

}
