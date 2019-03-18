package org.zhinanzhen.tb.dao;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.tb.dao.pojo.UserDO;

public interface UserDAO {

	public int addUser(@Param("name") String name, @Param("authNickname") String authNickname,
			@Param("birthday") Date birthday, @Param("phone") String phone,
			@Param("firstControllerContents") String firstControllerContents, @Param("visaCode") String visaCode,
			@Param("visaExpirationDate") Date visaExpirationDate, @Param("source") String source,
			@Param("adviserId") int adviserId);

	public int countUser(@Param("name") String name, @Param("authType") String authType,
			@Param("authNickname") String authNickname, @Param("phone") String phone,
			@Param("adviserId") Integer adviserId);

	public int countUserByThisMonth();

	public List<UserDO> listUser(@Param("name") String name, @Param("authType") String authType,
			@Param("authNickname") String authNickname, @Param("phone") String phone,
			@Param("adviserId") Integer adviserId, @Param("orderByField") String orderByField,
			@Param("isDesc") Boolean isDesc, @Param("offset") int offset, @Param("rows") int rows);

	public UserDO getUserById(int id);

	UserDO getUserByThird(@Param("thirdType") String thirdType, @Param("thirdId") String thirdId);

	boolean update(@Param("id") int id, @Param("name") String name, @Param("birthday") Date birthday,
			@Param("phone") String phone, @Param("firstControllerContents") String firstControllerContents);

	boolean updateAdviserById(@Param("adviserId") int adviserId, @Param("id") int id);

	boolean updateBalanceById(@Param("id") int id, @Param("balance") double balance);

	public List<UserDO> listUserByRecommendOpenId(String recommendOpenId);
}
