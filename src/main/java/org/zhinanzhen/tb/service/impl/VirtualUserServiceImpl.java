package org.zhinanzhen.tb.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.tb.dao.OrderDAO;
import org.zhinanzhen.tb.dao.VirtualUserDAO;
import org.zhinanzhen.tb.dao.pojo.OrderDO;
import org.zhinanzhen.tb.dao.pojo.VirtualUserDO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.VirtualUserService;
import org.zhinanzhen.tb.service.pojo.VirtualUserDTO;
import org.zhinanzhen.tb.utils.Base64Util;

import com.ikasoa.core.ErrorCodeEnum;

@Service("virtualUserService")
public class VirtualUserServiceImpl extends BaseService implements VirtualUserService {

	@Resource
	private VirtualUserDAO virtualUserDao;

	@Resource
	private OrderDAO orderDao;

	@Override
	public int addVirtualUser(String name, String authNickname, String authLogo) throws ServiceException {
		if (name == null) {
			ServiceException se = new ServiceException("name is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		if (authNickname == null) {
			ServiceException se = new ServiceException("authNickname is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		if (authLogo == null) {
			ServiceException se = new ServiceException("authLogo is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			authNickname = new String(Base64Util.encodeBase64(authNickname.getBytes()));
		} catch (Exception e) {
			ServiceException se = new ServiceException("authNickname error : " + e.getMessage());
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		return virtualUserDao.addVirtualUser(name, authNickname, authLogo);
	}
	
	public int countVirtualUser() throws ServiceException {
		return virtualUserDao.countVirtualUser();
	}

	@Override
	public List<VirtualUserDTO> listVirtualUser(int pageNum, int pageSize) throws ServiceException {
		List<VirtualUserDTO> virtualUserDtoList = new ArrayList<>();
		List<VirtualUserDO> virtualUserDoList = virtualUserDao.listVirtualUser(pageNum * pageSize, pageSize);
		for (VirtualUserDO virtualUserDo : virtualUserDoList) {
			VirtualUserDTO virtualUserDto = new VirtualUserDTO();
			try {
				virtualUserDto.setId(virtualUserDo.getId());
				virtualUserDto.setGmtCreate(virtualUserDo.getGmtCreate());
				virtualUserDto.setName(virtualUserDo.getName());
				virtualUserDto.setAuthLogo(virtualUserDo.getAuthLogo());
				virtualUserDto
						.setAuthNickname(new String(Base64Util.decodeBase64(virtualUserDo.getAuthNickname()), "utf-8"));
			} catch (Exception e) {
				ServiceException se = new ServiceException(e.getMessage());
				se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
				throw se;
			}
			virtualUserDtoList.add(virtualUserDto);
		}
		return virtualUserDtoList;
	}

	@Override
	public int deleteById(int id) throws ServiceException {
		List<OrderDO> orderDoList = orderDao.listOrderByUserId(id);
		if (orderDoList.size() > 0) {
			ServiceException se = new ServiceException("该虚拟用户已有" + orderDoList.size() + "条订单，不能删除！");
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
		return virtualUserDao.deleteById(id);
	}

}
