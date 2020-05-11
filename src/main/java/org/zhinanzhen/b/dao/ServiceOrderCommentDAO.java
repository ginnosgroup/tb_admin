package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.ServiceOrderCommentDO;

public interface ServiceOrderCommentDAO {

	int add(ServiceOrderCommentDO serviceOrderMessageDo);

	public List<ServiceOrderCommentDO> list(@Param("serviceOrderId") Integer serviceOrderId);

	public int delete(int id);

}
