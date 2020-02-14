package org.zhinanzhen.tb.dao;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.tb.dao.pojo.AdminUserDO;

public interface AdminUserDAO {

	int add(AdminUserDO adminUserDo);

	public AdminUserDO login(@Param("username") String username, @Param("password") String password);

	public boolean updateSessionId(@Param("id") int id, @Param("sessionId") String sessionId);

	public boolean updatePassword(@Param("username") String username, @Param("password") String password);

	public AdminUserDO getAdminUserById(int id);
	
	AdminUserDO getAdminUserByUsername(String username);

}
