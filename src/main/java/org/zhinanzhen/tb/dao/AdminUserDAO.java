package org.zhinanzhen.tb.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.zhinanzhen.tb.annotation.UnRecordLog;
import org.zhinanzhen.tb.dao.pojo.AdminUserDO;
import org.zhinanzhen.tb.dao.pojo.UserDO;

public interface AdminUserDAO {

	int add(AdminUserDO adminUserDo);

	public AdminUserDO login(@Param("username") String username, @Param("password") String password);

	@UnRecordLog
	public boolean updateSessionId(@Param("id") int id, @Param("sessionId") String sessionId);

	public boolean updatePassword(@Param("username") String username, @Param("password") String password);

	boolean updateOfficialId(@Param("id") int id, @Param("officialId") int officialId);

	boolean updateOfficialAdmin(@Param("id") int id, @Param("isOfficialAdmin") boolean isOfficialAdmin);

	boolean updateRegionId(@Param("id") int id, @Param("regionId") int regionId);
	
	boolean updateState(@Param("id") int id, @Param("state") String state);
	
	boolean updateStateByAdviserId(@Param("adviserId") int adviserId, @Param("state") String state);
	
	boolean updateUsername(@Param("id") int id, @Param("username") String username);

	public AdminUserDO getAdminUserById(int id);
	
	AdminUserDO getAdminUserByAdviserId(int adviserId);
	
	List<AdminUserDO> listAdminUserByAp(String ap);
	
	AdminUserDO getAdminUserByUsername(String username);
	
	AdminUserDO getAdminUserByOpenUserId(String openUserId); // 我猜是把open拼错成oper, 所以在添加这个接口时我更正成open了

	boolean updateOperUserId(@Param("id")int id ,@Param("operUserId") String operUserId);

	@Select("select * from tb_admin_user where adviser_id = #{adviserId}")
	AdminUserDO getUserByAdviserId(int id);

}
