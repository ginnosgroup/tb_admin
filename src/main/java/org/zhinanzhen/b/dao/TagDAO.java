package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.TagDO;

public interface TagDAO {

	public int addTag(TagDO tagDo);

	public List<TagDO> listTag();

	public List<TagDO> listTagByUserId(@Param("userId") Integer userId);

	public int deleteTagById(@Param("id") Integer id);

}
