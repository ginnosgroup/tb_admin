package org.zhinanzhen.b.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.zhinanzhen.b.service.pojo.PartInfo;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadResponseData {
    private String fileName;
    private boolean rapidUpload;
    private String driveId;
    private String parentFileId;
    private String uploadId;
    private List<PartInfo> partInfoList;
    private String type;
    private String domainId;
    private String fileId;
}
