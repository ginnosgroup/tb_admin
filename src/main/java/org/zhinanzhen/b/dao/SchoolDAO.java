package org.zhinanzhen.b.dao;

import org.zhinanzhen.b.dao.pojo.SchoolDO;

public interface SchoolDAO {

	public int addSchool(SchoolDO schoolDO);
	
	public SchoolDO listSchool(String name);

}
