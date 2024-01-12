package org.zhinanzhen.b.dao.pojo;

import lombok.Data;

@Data
public class ApplicantListDO {
    private Integer id;

    private Integer applicantId;

    private Integer applicantParentId;

    private Integer servicePackageId;
}
