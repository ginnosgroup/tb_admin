package org.zhinanzhen.tb.service;

import java.util.List;

import org.zhinanzhen.tb.service.pojo.VirtualUserDTO;

public interface VirtualUserService {
	
	int addVirtualUser(String name, String authNickname, String authLogo) throws ServiceException;
	
	int countVirtualUser() throws ServiceException;
	
	List<VirtualUserDTO> listVirtualUser(int pageNum, int pageSize) throws ServiceException;
	
	int deleteById(int id) throws ServiceException;

}
