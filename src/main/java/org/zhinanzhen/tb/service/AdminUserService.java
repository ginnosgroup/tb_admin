package org.zhinanzhen.tb.service;

import org.zhinanzhen.tb.service.pojo.AdminUserDTO;


public interface AdminUserService {

	public int login(String username, String password) throws ServiceException;
	
	public boolean updateSessionId(int id, String sessionId) throws ServiceException;

	public AdminUserDTO getAdminUserById(int id) throws ServiceException;

}
