package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.PortalAttachmentDO;

public interface PortalAttachmentDAO {

	int addPortalAttachment(PortalAttachmentDO portalAttachmentDo);

	PortalAttachmentDO getPortalAttachmentById(@Param("id") int id);

	PortalAttachmentDO getPortalAttachmentByPath(@Param("filePath") String filePath);

	List<PortalAttachmentDO> listPortalAttachmentByPortalId(@Param("portalId") Integer portalId);

	List<PortalAttachmentDO> listPortalAttachmentByPortalIdAndFileNameAndStage(@Param("portalId") Integer portalId,
			@Param("fileName") String fileName, @Param("stage") String stage);

	List<PortalAttachmentDO> listPortalArchiveAttachmentByPortalIdAndStage(@Param("portalId") Integer portalId,
			@Param("stage") String stage);

	List<PortalAttachmentDO> listPortalAttachment(@Param("id") Integer id,
			@Param("portalId") Integer portalId, @Param("attachmentState") String attachmentState,
			@Param("stage") String stage, @Param("filePath") String filePath,
			@Param("fileName") String fileName, @Param("offset") int offset, @Param("rows") int rows);

	int updatePortalAttachment(PortalAttachmentDO portalAttachmentDo);

	int updatePortalIdByPathList(@Param("filePathList") List<String> filePathList, @Param("portalId") int portalId);

	int updatePortalIdAndStageByPathList(@Param("filePathList") List<String> filePathList,
			@Param("portalId") int portalId, @Param("stage") String stage);

	int deletePortalAttachmentById(int id);

	int deletePortalAttachmentByIdAndPortalIdAndFileNameAndStage(@Param("id") int id,
			@Param("portalId") int portalId, @Param("fileName") String fileName, @Param("stage") String stage);

	int deletePortalArchiveAttachmentByIdAndPortalIdAndStage(@Param("id") int id, @Param("portalId") int portalId,
			@Param("stage") String stage);

	int deletePortalAttachmentByPath(@Param("filePath") String filePath);

}
