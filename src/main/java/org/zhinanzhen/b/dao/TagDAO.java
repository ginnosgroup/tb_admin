package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.TagDO;
import org.zhinanzhen.b.dao.pojo.UserTagDO;

public interface TagDAO {

	public int addTag(TagDO tagDo);

	public int addUserTag(UserTagDO userTagDo);

	public List<TagDO> listTag();

	public List<TagDO> listTagByUserId(@Param("userId") Integer userId);

	public TagDO getTagById(@Param("id") Integer id);

	public int deleteTagById(@Param("id") Integer id);

	public int deleteUserTagByTagId(@Param("tagId") Integer tagId);

	public int deleteUserTagByUserId(@Param("userId") Integer userId);

	public int deleteUserTagByTagIdAndUserId(@Param("tagId") Integer tagId, @Param("userId") Integer userId);

}
