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

	/** 清空案件生成的合同、Letter和Form 956文件路径。 */
	int clearGeneratedDocumentPaths(int id) throws ServiceException;

	int updatePortalStateIfCurrent(int id, String fromState, String toState) throws ServiceException;

	List<PortalDTO> listPortal(Integer typeId, Integer id, String caseType, String strState, String keyword, String name, int pageNum, int pageSize,
			Integer adviserId, Integer adviserRegionId, Integer officialId, Integer officialRegionId, Integer maraId,
			boolean officialStateRange)
			throws ServiceException;

	int countPortal(Integer typeId, Integer id, String caseType, String strState, String keyword, String name, Integer adviserId,
			Integer adviserRegionId, Integer officialId, Integer officialRegionId, Integer maraId,
			boolean officialStateRange)
			throws ServiceException;

	PortalDTO getPortal(Integer id, Integer adviserId, Integer adviserRegionId, Integer officialId,
			Integer officialRegionId, Integer maraId) throws ServiceException;

	PortalDTO getPortalByName(Integer id, String name, Integer adviserId, Integer adviserRegionId, Integer officialId,
			Integer officialRegionId, Integer maraId) throws ServiceException;

	int deletePortal(int id) throws ServiceException;

	int updateAiConsultContent(int id, String aiConsultContent) throws ServiceException;

	void sendMaraPortalNotification(PortalDTO portalDto, String remark, String caseUrl) throws ServiceException;

	void sendMaraPortalReviewNotification(PortalDTO portalDto, String caseUrl) throws ServiceException;

	void sendMaraPortalMaterialsReviewNotification(PortalDTO portalDto, String caseUrl) throws ServiceException;

	void sendMaraSupplementReviewNotification(PortalDTO portalDto, String caseUrl) throws ServiceException;

	void sendOfficialSupplementReviewNotification(PortalDTO portalDto, String remark, String caseUrl,
			boolean approved) throws ServiceException;

	/** 客户上传补充材料后，通知文案并附上补充材料。 */
	void sendOfficialSupplementaryMaterialsUploadedNotification(PortalDTO portalDto, List<String> attachmentPaths,
			String caseUrl) throws ServiceException;

	/** 客户退回补充材料后，通知文案并附上补充材料。 */
	void sendOfficialSupplementaryMaterialsReturnedNotification(PortalDTO portalDto, List<String> attachmentPaths,
			String caseUrl) throws ServiceException;

	/** 客户已确认补充材料后，通知文案并附上补充材料。 */
	void sendOfficialSupplementaryMaterialsConfirmedNotification(PortalDTO portalDto,
			List<String> attachmentPaths, String caseUrl) throws ServiceException;

	void sendOfficialPortalNotification(PortalDTO portalDto, String caseUrl) throws ServiceException;

	/** 服务订单下单后，通知案件对应的文案开始处理。 */
	void sendOfficialServiceOrderCreatedNotification(PortalDTO portalDto, String caseUrl) throws ServiceException;

	void sendOfficialPortalMaterialsRejectedNotification(PortalDTO portalDto, String remark, String caseUrl)
			throws ServiceException;

	void sendOfficialPortalMaterialsApprovedNotification(PortalDTO portalDto, String caseUrl) throws ServiceException;

	void sendAdviserPortalNotification(PortalDTO portalDto, String caseUrl) throws ServiceException;

}
