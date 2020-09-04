package org.zhinanzhen.tb.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.tb.dao.pojo.AdminUserDO;

public interface AdminUserDAO {

	int add(AdminUserDO adminUserDo);

	public AdminUserDO login(@Param("username") String username, @Param("password") String password);

	public boolean updateSessionId(@Param("id") int id, @Param("sessionId") String sessionId);

	public boolean updatePassword(@Param("username") String username, @Param("password") String password);

	boolean updateOfficialId(@Param("id") int id, @Param("officialId") int officialId);

	boolean updateRegionId(@Param("id") int id, @Param("regionId") int regionId);

	public AdminUserDO getAdminUserById(int id);
	
	List<AdminUserDO> listAdminUserByAp(String ap);
	
	AdminUserDO getAdminUserByUsername(String username);
	
}
