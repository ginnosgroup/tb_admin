package org.zhinanzhen.b.service;

import java.io.IOException;
import com.fasterxml.jackson.databind.node.ObjectNode;

/** application 附件原始文件内容提取；在上传落盘和替换旧附件前执行。 */
public interface PortalApplicationContentService {
    ObjectNode extract(byte[] fileBytes, String fileName, String mimeType) throws IOException;
}
