package org.zhinanzhen.tb.service;

import java.util.Date;
import java.util.List;

import org.zhinanzhen.tb.service.pojo.UserDTO;

public interface UserService {

	public int addUser(String name, String authNickname, Date birthday, String phone, String wechatUsername,
			String firstControllerContents, String visaCode, Date visaExpirationDate, String source, int adviserId)
			throws ServiceException;

	public int countUser(String name, UserAuthTypeEnum authType, String authNickname, String phone, int adviserId)
			throws ServiceException;

	public int countUserByThisMonth(Integer adviserId) throws ServiceException;

	public List<UserDTO> listUser(String name, UserAuthTypeEnum authType, String authNickname, String phone,
			int adviserId, int pageNum, int pageSize) throws ServiceException;

	public List<UserDTO> listUser(String name, UserAuthTypeEnum authType, String authNickname, String phone,
			int adviserId, String orderByField, Boolean isDesc, int pageNum, int pageSize) throws ServiceException;

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

	boolean update(int id, String name, Date birthday, String phone, String wechatUsername,
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

}
