package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.service.pojo.SchoolDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface SchoolService {

	public int addSchool(SchoolDTO schoolDto) throws ServiceException;

	public int updateSchool(int id, String name, String subject, String country) throws ServiceException;
	
	public List<SchoolDTO> list(String name) throws ServiceException;

	public List<SchoolDTO> list(String name, String subject, String country) throws ServiceException;

	public List<SchoolDTO> listSchool(String name, String country) throws ServiceException;

	public SchoolDTO getSchoolById(int id) throws ServiceException;

	public int deleteSchoolById(int id) throws ServiceException;

	public int deleteSchoolByName(String name) throws ServiceException;

}
