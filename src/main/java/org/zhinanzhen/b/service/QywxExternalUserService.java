package org.zhinanzhen.b.service;

import org.zhinanzhen.b.service.pojo.QywxExternalUserDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface QywxExternalUserService {

	int add(QywxExternalUserDTO qywxExternalUserDto) throws ServiceException;

	QywxExternalUserDTO getByExternalUserid(String externalUserid) throws ServiceException;

}
