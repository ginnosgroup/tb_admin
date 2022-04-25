package org.zhinanzhen.b.dao;

import java.util.List;

import org.zhinanzhen.b.dao.pojo.ApplicantDO;

public interface ApplicantDAO {
	
	int add(ApplicantDO applicantDo);
	
	int count(int userId);
	
	List<ApplicantDO> list(int userId);
	
	ApplicantDO getById(int id);
	
	int update(ApplicantDO applicantDo);
	
	int deleteById(int id);

}
