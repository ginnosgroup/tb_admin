package org.zhinanzhen.b.service.pojo;

import lombok.Data;
import org.zhinanzhen.b.dao.pojo.SchoolInstitutionCommentDO;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Date: 2021/10/15 上午 10:26
 * Description:学校评论DTO
 * Version: V1.0
 */
@Data
public class SchoolInstitutionCommentDTO  {

    private int id;

    private Date gmtCreate;

    private Date gmtModify;

    private int schoolInstitutionId;

    private String userName;

    private String content;

    private int parentId;

    private String toUserName;

    List<SchoolInstitutionCommentDO> commentdos = new ArrayList<>();

}
