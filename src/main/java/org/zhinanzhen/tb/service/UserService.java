package org.zhinanzhen.tb.service;

import java.util.Date;
import java.util.List;

import org.zhinanzhen.tb.service.pojo.UserDTO;
import org.zhinanzhen.tb.service.pojo.TagDTO;

public interface UserService {

	public int addUser(String name, String authNickname, Date birthday, String phone, String wechatUsername,
			String firstControllerContents, String visaCode, Date visaExpirationDate, String source, int adviserId)
			throws ServiceException;

	public int countUser(String name, UserAuthTypeEnum authType, String authNickname, String phone,
			String wechatUsername, int adviserId) throws ServiceException;

	public int countUserByThisMonth(Integer adviserId) throws ServiceException;

	public List<UserDTO> listUser(String name, UserAuthTypeEnum authType, String authNickname, String phone,
			String wechatUsername, int adviserId, int pageNum, int pageSize) throws ServiceException;

	public List<UserDTO> listUser(String name, UserAuthTypeEnum authType, String authNickname, String phone,
			String wechatUsername, int adviserId, Integer tagId, String orderByField, Boolean isDesc, int pageNum,
			int pageSize) throws ServiceException;

	public UserDTO getUserById(int id) throws ServiceException;

	/**
	 * 根据openId获取用户信息
	 * 
	 * @param thirdType
	 * @param thirdId
	 * @return
	 * @throws ServiceException
	 */
	UserDTO getUserByOpenId(String thirdType, String thirdId) throws ServiceException;

	boolean update(int id, String name, String authNickname, Date birthday, String phone, String wechatUsername,
			String firstControllerContents, String visaCode, Date visaExpirationDate, String source)
			throws ServiceException;

	/**
	 * 用户重新绑定顾问
	 * 
	 * @param adviserId
	 * @param id
	 * @return
	 * @throws ServiceException
	 */
	boolean updateAdviserId(int adviserId, int id) throws ServiceException;

	/**
	 * 根据推荐人openId获取被推荐人列表
	 * 
	 * @param recommendOpenId
	 *            推荐人openId
	 */
	public List<UserDTO> listUserByRecommendOpenId(String recommendOpenId) throws ServiceException;

	public int newTag(String name) throws ServiceException;

	public int addTag(int userId, int tagId) throws ServiceException;

	public List<TagDTO> listTag() throws ServiceException;

	public List<TagDTO> listTagByUserId(int userId) throws ServiceException;

	public TagDTO getTag(int tagId) throws ServiceException;

	public int deleteTagById(int id) throws ServiceException;

	public int deleteUserTagByUserId(int userId) throws ServiceException;

	public int deleteUserTagByTagIdAndUserId(int tagId, int userId) throws ServiceException;

	public int updateDOB(Date dob,int id) throws ServiceException;

}
