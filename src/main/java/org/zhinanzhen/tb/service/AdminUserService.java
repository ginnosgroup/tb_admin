package org.zhinanzhen.tb.service;

import org.zhinanzhen.tb.service.pojo.AdminUserDTO;


public interface AdminUserService {

	int add(String username, String password, String apList, int adviserId) throws ServiceException;

	public int login(String username, String password) throws ServiceException;
	
	public boolean updateSessionId(int id, String sessionId) throws ServiceException;

	public AdminUserDTO getAdminUserById(int id) throws ServiceException;

}
