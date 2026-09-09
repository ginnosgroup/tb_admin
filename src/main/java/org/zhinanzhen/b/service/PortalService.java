package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.service.pojo.PortalDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface PortalService {

	int addPortal(PortalDTO portalDto) throws ServiceException;

	int updatePortal(PortalDTO portalDto) throws ServiceException;

	/** 在同一事务中更新案件及本次上传附件，归档后拒绝写入。 */
	int updatePortalWithAttachments(PortalDTO portalDto, List<String> filePaths, String stage) throws ServiceException;

	void requireEditablePortal(int id) throws ServiceException;

	void requireEditableDocument(String filePath) throws ServiceException;

	int clearGeneratedDocumentPaths(int id) throws ServiceException;

	int updatePortalStateIfCurrent(int id, String fromState, String toState) throws ServiceException;

	List<PortalDTO> listPortal(Integer typeId, String caseType, String strState, String keyword, int pageNum, int pageSize,
			Integer adviserId, Integer adviserRegionId, Integer officialId, Integer officialRegionId, Integer maraId,
			boolean officialStateRange)
			throws ServiceException;

	int countPortal(Integer typeId, String caseType, String strState, String keyword, Integer adviserId,
			Integer adviserRegionId, Integer officialId, Integer officialRegionId, Integer maraId,
			boolean officialStateRange)
			throws ServiceException;

	PortalDTO getPortal(Integer id, Integer adviserId, Integer adviserRegionId, Integer officialId,
			Integer officialRegionId, Integer maraId) throws ServiceException;

	int deletePortal(int id) throws ServiceException;

	int updateAiConsultContent(int id, String aiConsultContent) throws ServiceException;

	void sendMaraPortalNotification(PortalDTO portalDto, String remark, String caseUrl) throws ServiceException;

	void sendMaraPortalReviewNotification(PortalDTO portalDto, String caseUrl) throws ServiceException;

	void sendMaraPortalMaterialsReviewNotification(PortalDTO portalDto, String caseUrl) throws ServiceException;

	void sendMaraSupplementReviewNotification(PortalDTO portalDto, String caseUrl) throws ServiceException;

	void sendOfficialSupplementReviewNotification(PortalDTO portalDto, String remark, String caseUrl,
			boolean approved) throws ServiceException;

	void sendOfficialPortalNotification(PortalDTO portalDto, String caseUrl) throws ServiceException;

	/** 服务订单下单后，通知案件对应的文案开始处理。 */
	void sendOfficialServiceOrderCreatedNotification(PortalDTO portalDto, String caseUrl) throws ServiceException;

	void sendOfficialPortalMaterialsRejectedNotification(PortalDTO portalDto, String remark, String caseUrl)
			throws ServiceException;

	void sendOfficialPortalMaterialsApprovedNotification(PortalDTO portalDto, String caseUrl) throws ServiceException;

	void sendAdviserPortalNotification(PortalDTO portalDto, String caseUrl) throws ServiceException;

}
