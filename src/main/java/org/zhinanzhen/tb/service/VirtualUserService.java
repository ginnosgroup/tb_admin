package org.zhinanzhen.tb.service;

import java.util.List;

import org.zhinanzhen.tb.service.pojo.VirtualUserDTO;

public interface VirtualUserService {
	
	int addVirtualUser(String name, String authNickname, String authLogo) throws ServiceException;
	
	List<VirtualUserDTO> listVirtualUser() throws ServiceException;
	
	int deleteById(int id) throws ServiceException;

}
