package org.zhinanzhen.tb.dao;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.tb.dao.pojo.AdminUserDO;

public interface AdminUserDAO {

	public AdminUserDO login(@Param("username") String username, @Param("password") String password);
	
	public boolean updateSessionId(@Param("id") int id, @Param("sessionId") String sessionId);

	public AdminUserDO getAdminUserById(int id);

}
