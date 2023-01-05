package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.QywxExternalUserDO;
import org.zhinanzhen.b.dao.pojo.QywxExternalUserDescriptionDO;

public interface QywxExternalUserDAO {

	int add(QywxExternalUserDO qywxExternalUserDo);

	int count(@Param("adviserId") Integer adviserId, @Param("state") String state, @Param("startDate") String startDate,
			@Param("endDate") String endDate);

	List<QywxExternalUserDO> list(@Param("adviserId") Integer adviserId, @Param("state") String state,
			@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("offset") int offset,
			@Param("rows") int rows);

	QywxExternalUserDO getByExternalUserid(@Param("externalUserid") String externalUserid);

	int addDesc(QywxExternalUserDescriptionDO qywxExternalUserDescriptionDo);

	List<QywxExternalUserDescriptionDO> listDesc(@Param("externalUserid") String externalUserid,
			@Param("key") String key);

}
