package org.zhinanzhen.b.dao;

import java.util.List;

import org.zhinanzhen.b.dao.pojo.SchoolAttachmentsDO;

public interface SchoolAttachmentsDAO {

	int addSchoolAttachments(SchoolAttachmentsDO schoolAttachmentsDo);

	int updateSchoolAttachments(SchoolAttachmentsDO schoolAttachmentsDo);

	List<SchoolAttachmentsDO> listBySchoolName(String schoolName); // 理论上只会有一条
	
	int deleteBySchoolName(String schoolName);

    List<SchoolAttachmentsDO> listByProviderId(int providerId);
}
