package org.zhinanzhen.b.service.pojo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CloudDiskFile {
    private Integer id;

    private Date gmtCreate;

    private Date gmtModify;

    private Integer applicantId;

    private Integer adviserId;

    private String name;

    private String type;

    private String url;

    private String parentFileId;

    private String domainId;

    private String driveId;

    private String fileId;

    private int isDelete;

    private Integer officialId;

    private Integer userId;

    private String operator;
}
