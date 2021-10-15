package org.zhinanzhen.b.dao.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * Date: 2021/10/15 上午 10:26
 * Description:学校评论DTO
 * Version: V1.0
 */
@Data
public class SchoolInstitutionCommentDO implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;

    private Date gmtCreate;

    private Date gmtModify;

    private int schoolInstitutionId;

    private String userName;

    private String content;

    private int parentId;

    private String toUserName;
}
