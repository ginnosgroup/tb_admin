package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.QywxExternalUserDO;
import org.zhinanzhen.b.dao.pojo.QywxExternalUserDescriptionDO;
import org.zhinanzhen.b.service.pojo.QywxExternalUserDTO;

public interface QywxExternalUserDAO {

	int add(QywxExternalUserDO qywxExternalUserDo);

	int update(QywxExternalUserDO qywxExternalUserDo);

	int count(@Param("adviserId") Integer adviserId, @Param("state") String state, @Param("startDate") String startDate,
			@Param("endDate") String endDate);

	List<QywxExternalUserDO> list(@Param("adviserId") Integer adviserId, @Param("state") String state,
			@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("offset") int offset,
			@Param("rows") int rows);

	QywxExternalUserDO get(@Param("id") Integer id);

	QywxExternalUserDO getByExternalUserid(@Param("externalUserid") String externalUserid);

	int addDesc(QywxExternalUserDescriptionDO qywxExternalUserDescriptionDo);

	int updateDesc(QywxExternalUserDescriptionDO qywxExternalUserDescriptionDo); // 仅更新value

	List<QywxExternalUserDescriptionDO> listDescByExternalUserid(@Param("qywxExternalUserId") String externalUserid,
			@Param("qywxKey") String key);

	List<QywxExternalUserDescriptionDO> listDescByApplicantId(@Param("applicantId") Integer applicantId);

	int bacthAdd(List<QywxExternalUserDTO> qywxExternalUserDTOS);
}
