package org.zhinanzhen.tb.dao;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.tb.dao.pojo.UserDO;

public interface UserDAO {

	public int addUser(UserDO userDo);

	public int countUser(@Param("name") String name, @Param("authType") String authType,
			@Param("authNickname") String authNickname, @Param("phone") String phone,
			@Param("wechatUsername") String wechatUsername, @Param("adviserId") Integer adviserId,
			@Param("regionId") Integer regionId);

	public int countUserByThisMonth(@Param("adviserId") Integer adviserId);

	public List<UserDO> listUser(@Param("name") String name, @Param("authType") String authType,
			@Param("authNickname") String authNickname, @Param("phone") String phone,
			@Param("wechatUsername") String wechatUsername, @Param("adviserId") Integer adviserId,
			@Param("regionId") Integer regionId, @Param("tagId") Integer tagId,
			@Param("orderByField") String orderByField, @Param("isDesc") Boolean isDesc, @Param("offset") int offset,
			@Param("rows") int rows);

	public UserDO getUserById(int id);

	UserDO getUserByThird(@Param("thirdType") String thirdType, @Param("thirdId") String thirdId);

	boolean update(@Param("id") int id, @Param("name") String name, @Param("authNickname") String authNickname,
			@Param("birthday") Date birthday, @Param("phone") String phone,
			@Param("wechatUsername") String wechatUsername,
			@Param("firstControllerContents") String firstControllerContents, @Param("visaCode") String visaCode,
			@Param("visaExpirationDate") Date visaExpirationDate, @Param("source") String source);

	boolean updateAdviserById(@Param("adviserId") int adviserId, @Param("id") int id);

	boolean updateBalanceById(@Param("id") int id, @Param("balance") double balance);

	int updateDOB(@Param("dob") Date dob,@Param("id") int id);

	public List<UserDO> listUserByRecommendOpenId(String recommendOpenId);
}
