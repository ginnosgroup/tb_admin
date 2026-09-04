package org.zhinanzhen.b.service;

import java.util.Map;

import org.zhinanzhen.b.service.pojo.PortalDTO;
import org.zhinanzhen.tb.service.ServiceException;

/** 根据案件客户资料生成合同和建议信。 */
public interface PortalDocumentService {

	Map<String, String> generateDocuments(PortalDTO portalDto) throws ServiceException;

	/** 将本次生成的合同和建议信作为附件发送给客户。 */
	void sendGeneratedDocuments(PortalDTO portalDto, Map<String, String> generatedDocumentPaths)
			throws ServiceException;

	/** 将合同和建议信作为附件发送给客户，并附加确认签署/退回修改按钮。 */
	void sendGeneratedDocuments(PortalDTO portalDto, Map<String, String> generatedDocumentPaths,
			String confirmUrl, String returnUrl) throws ServiceException;

	/** 通知客户确认申请材料，并附加本次提交的申请材料及确认/退回按钮。 */
	void sendApplicationMaterialsConfirmation(PortalDTO portalDto, String filePath, String confirmUrl,
			String returnUrl) throws ServiceException;

	/** 通知客户案件已经正式提交申请，并附加本次提交的申请材料。 */
	void sendApplicationSubmittedNotification(PortalDTO portalDto, String filePath, String caseUrl)
			throws ServiceException;

	/** 将案件已经生成的合同和建议信作为附件发送给指定收件人。 */
	void sendDocumentsToEmail(String recipientEmail, String subject, String content, String contractFilePath,
			String letterFilePath) throws ServiceException;
}
