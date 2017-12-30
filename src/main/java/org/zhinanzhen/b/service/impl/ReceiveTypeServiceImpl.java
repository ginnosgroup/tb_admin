package org.zhinanzhen.b.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.ReceiveTypeDAO;
import org.zhinanzhen.b.service.ReceiveTypeService;
import org.zhinanzhen.b.service.ReceiveTypeStateEnum;
import org.zhinanzhen.b.service.pojo.ReceiveTypeDTO;
import org.zhinanzhen.tb.service.impl.BaseService;

@Service("ReceiveTypeService")
public class ReceiveTypeServiceImpl extends BaseService implements ReceiveTypeService {

	@Resource
	private ReceiveTypeDAO ReceiveTypeDAO;

	@Override
	public int addReceiveType(ReceiveTypeDTO receiveTypeDto) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int updateReceiveType(ReceiveTypeDTO receiveTypeDto) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<ReceiveTypeDTO> listReceiveType(ReceiveTypeStateEnum state) {
		// TODO Auto-generated method stub
		return null;
	}

}
