package org.zhinanzhen.tb.service;

import org.zhinanzhen.tb.service.pojo.AdminUserDTO;

public interface AdminUserService {

	int add(String username, String password, String apList, Integer adviserId, Integer maraId, Integer officialId,
			Integer kjId, Integer regionId) throws ServiceException;

	public int login(String username, String password) throws ServiceException;

	public boolean updateSessionId(int id, String sessionId) throws ServiceException;

	public boolean updateRegionId(int id, Integer regionId) throws ServiceException;

	public AdminUserDTO getAdminUserById(int id) throws ServiceException;

	public AdminUserDTO getAdminUserByUsername(String username) throws ServiceException;

	public boolean updatePassword(String username, String newPassword) throws ServiceException;

	boolean updateOfficialId(int id, int officialId) throws ServiceException;

	boolean updateOfficialAdmin(int id, boolean isOfficialAdmin) throws ServiceException;

	boolean updateOperUserId(int id ,String operUserId);
}
