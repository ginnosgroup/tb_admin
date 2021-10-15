package org.zhinanzhen.b.dao;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.SchoolInstitutionCommentDO;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Date: 2021/10/15 上午 10:56
 * Description:学校评论的Mapper层
 * Version: V1.0
 */
public interface SchoolInstitutionCommentDAO {

    int addComment(SchoolInstitutionCommentDO commentDO);

    List<SchoolInstitutionCommentDO> list(@Param("schoolInstitutionId") int schoolInstitutionId,@Param("parentId") int parentId);

    int delete(int id);

    SchoolInstitutionCommentDO getCommentById(int id);
}
