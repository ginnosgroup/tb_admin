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
}
