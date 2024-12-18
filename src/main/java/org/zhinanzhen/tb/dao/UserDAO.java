package org.zhinanzhen.tb.dao;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.zhinanzhen.tb.dao.pojo.UserAdviserDO;
import org.zhinanzhen.tb.dao.pojo.UserDO;

public interface UserDAO {

	public int addUser(UserDO userDo);

	public int addUserAdviser(@Param("userId") Integer userId, @Param("adviserId") Integer adviserId,
			@Param("isCreater") Boolean isCreater);

	public int countUser(@Param("name") String name, @Param("authType") String authType,
			@Param("authNickname") String authNickname, @Param("phone") String phone,
			@Param("areaCode") String areaCode, @Param("wechatUsername") String wechatUsername,
			@Param("adviserId") Integer adviserId, @Param("applicantName") String applicantName,
			@Param("regionIdList") List<Integer> regionIdList, @Param("tagId") Integer tagId);

	public int countUserByThisMonth(@Param("adviserId") Integer adviserId,
			@Param("regionIdList") List<Integer> regionIdList);

	public List<UserDO> listUser(@Param("name") String name, @Param("authType") String authType,
			@Param("authNickname") String authNickname, @Param("phone") String phone,
			@Param("areaCode") String areaCode, @Param("email") String email,
			@Param("wechatUsername") String wechatUsername, @Param("adviserId") Integer adviserId,
			@Param("applicantName") String applicantName,
			@Param("regionIdList") List<Integer> regionIdList,
			@Param("tagId") Integer tagId, @Param("orderByField") String orderByField, @Param("isDesc") Boolean isDesc,
			@Param("offset") int offset, @Param("rows") int rows);

	public List<UserAdviserDO> listUserAdviserByUserId(@Param("userId") Integer userId);

	public UserDO getUserById(int id);

	UserDO getUserByThird(@Param("thirdType") String thirdType, @Param("thirdId") String thirdId);

	boolean update(@Param("id") int id, @Param("name") String name, @Param("authNickname") String authNickname,
			@Param("birthday") Date birthday, @Param("phone") String phone, @Param("email") String email,
			@Param("areaCode") String areaCode, @Param("wechatUsername") String wechatUsername,
			@Param("firstControllerContents") String firstControllerContents, @Param("visaCode") String visaCode,
			@Param("visaExpirationDate") Date visaExpirationDate, @Param("source") String source,
			@Param("stateText") String stateText, @Param("channelSource") String channelSource,
		   @Param("adviserId") String adviserId);

	boolean updateAdviserById(@Param("adviserId") int adviserId, @Param("id") int id);

	boolean updateBalanceById(@Param("id") int id, @Param("balance") double balance);

	int updateDOB(@Param("dob") Date dob,@Param("id") int id);

	public List<UserDO> listUserByRecommendOpenId(String recommendOpenId);

	public List<UserDO> getUserByAuth_openid(@Param("authOpenid")String Auth_openid);

	boolean updateAuthopenidByPhone(@Param("authOpenid")String authOpenid ,@Param("phone") String phone);

	int updateByAuthopenid(UserDO userDo);

	@Select("select  * from tb_user where auth_type = 'WECHAT_WORK'")
	List<UserDO> getUserByWxWrok();

	int updateUserAdviserById(@Param("userId") Integer userId, @Param("adviserId") String adviserId);

	int updateUserApplicationById(@Param("userId") Integer userId, @Param("adviserId") String adviserId);

}
