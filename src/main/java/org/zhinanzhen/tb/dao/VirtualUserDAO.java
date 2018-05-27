package org.zhinanzhen.tb.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.tb.dao.pojo.VirtualUserDO;

public interface VirtualUserDAO {
	
	int addVirtualUser(@Param("name") String name, @Param("authNickname") String authNickname, @Param("authLogo") String authLogo);
	
	List<VirtualUserDO> listVirtualUser();
	
	int deleteById(int id);

}
