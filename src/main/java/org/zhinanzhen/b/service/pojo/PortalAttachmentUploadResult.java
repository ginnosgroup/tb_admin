package org.zhinanzhen.b.service.pojo;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/** 申请材料入库结果，旧同名文件在事务提交后由上传接口清理。 */
@Data
public class PortalAttachmentUploadResult {
	private int attachmentId;
	private List<String> replacedFilePaths = new ArrayList<String>();
}
