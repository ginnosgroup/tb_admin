package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.WechatUserTagDO;

public interface WechatUserTagDAO {

	int addWechatUserTag(WechatUserTagDO wechatUserTagDo);

	int deleteWechatUserTagByUserId(@Param("userId") int userId);

	List<WechatUserTagDO> listWechatUserTagByUserId(@Param("userId") int userId);

}
