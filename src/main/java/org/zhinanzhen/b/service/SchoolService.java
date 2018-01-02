package org.zhinanzhen.b.service;

import org.zhinanzhen.b.dao.pojo.SchoolDO;
import org.zhinanzhen.tb.service.ServiceException;

public interface SchoolService {

	public int addSchool(SchoolDO schoolDO) throws ServiceException;

	public int updateSchool(String name, String subject, String country) throws ServiceException;

	public SchoolDO listSchool(String name, String subject, String country) throws ServiceException;

	public SchoolDO getSchoolById(int id) throws ServiceException;

	public int deleteSchoolById(int id) throws ServiceException;

}
