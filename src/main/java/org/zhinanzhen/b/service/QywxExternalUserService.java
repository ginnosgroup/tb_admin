package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.service.pojo.QywxExternalUserDTO;
import org.zhinanzhen.b.service.pojo.QywxExternalUserDescriptionDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface QywxExternalUserService {

	int add(QywxExternalUserDTO qywxExternalUserDto) throws ServiceException;

	int update(QywxExternalUserDTO qywxExternalUserDto) throws ServiceException;

	int count(int adviserId, String state, String startDate, String endDate) throws ServiceException;

	List<QywxExternalUserDTO> list(int adviserId, String state, String startDate, String endDate, int pageNum,
			int pageSize) throws ServiceException;

	QywxExternalUserDTO getByExternalUserid(String externalUserid) throws ServiceException;
	
	int addDesc(QywxExternalUserDescriptionDTO qywxExternalUserDescriptionDto) throws ServiceException;

	int updateDesc(QywxExternalUserDescriptionDTO qywxExternalUserDescriptionDto) throws ServiceException;

	List<QywxExternalUserDescriptionDTO> listDesc(String externalUserid, String key) throws ServiceException;

}
