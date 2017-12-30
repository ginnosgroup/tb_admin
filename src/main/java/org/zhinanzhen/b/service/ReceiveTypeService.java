package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.service.pojo.ReceiveTypeDTO;

public interface ReceiveTypeService {

	public int addReceiveType(ReceiveTypeDTO receiveTypeDto);

	public int updateReceiveType(ReceiveTypeDTO receiveTypeDto);

	public List<ReceiveTypeDTO> listReceiveType(ReceiveTypeStateEnum state);

}
