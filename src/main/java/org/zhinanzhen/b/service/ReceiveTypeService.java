package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.service.pojo.ReceiveTypeDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface ReceiveTypeService {

	public int addReceiveType(ReceiveTypeDTO receiveTypeDto) throws ServiceException;

	public int updateReceiveType(ReceiveTypeDTO receiveTypeDto) throws ServiceException;

	public List<ReceiveTypeDTO> listReceiveType(AbleStateEnum state) throws ServiceException;

}
