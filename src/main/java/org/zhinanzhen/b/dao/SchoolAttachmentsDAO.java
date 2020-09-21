package org.zhinanzhen.b.dao;

import org.zhinanzhen.b.dao.pojo.SchoolAttachmentsDO;

public interface SchoolAttachmentsDAO {

	int addSchoolAttachments(SchoolAttachmentsDO schoolAttachmentsDo);

	int updateSchoolAttachments(SchoolAttachmentsDO schoolAttachmentsDo);

	SchoolAttachmentsDO getBySchoolName(String schoolName);

}
