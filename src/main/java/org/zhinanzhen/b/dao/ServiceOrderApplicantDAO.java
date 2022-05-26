package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.ServiceOrderApplicantDO;

public interface ServiceOrderApplicantDAO {

	int add(ServiceOrderApplicantDO serviceOrderApplicantDo);

	List<ServiceOrderApplicantDO> list(@Param("serviceOrderId") Integer serviceOrderId,
			@Param("applicantId") Integer applicantId);

	int delete(@Param("id") Integer id);

}
