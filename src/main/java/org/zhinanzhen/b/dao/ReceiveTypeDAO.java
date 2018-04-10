package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.ReceiveTypeDO;

public interface ReceiveTypeDAO {

	public int addReceiveType(ReceiveTypeDO receiveTypeDo);

	public int updateReceiveType(ReceiveTypeDO receiveTypeDo);

	public List<ReceiveTypeDO> listReceiveType(@Param("state") String state);

	public ReceiveTypeDO getReceiveTypeById(int id);

}
