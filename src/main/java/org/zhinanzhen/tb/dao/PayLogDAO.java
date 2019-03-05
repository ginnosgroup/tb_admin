package org.zhinanzhen.tb.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.tb.dao.pojo.PayLogDO;

public interface PayLogDAO {
    
    int deleteByPrimaryKey(Integer id);

    int insert(PayLogDO record);

    PayLogDO selectByPrimaryKey(Integer id);

    List<PayLogDO> selectAll();

    int updateByPrimaryKey(PayLogDO record);
    
    List<PayLogDO> selectByUserId(@Param("userId") int userId,
	    @Param("payType") String payType);
}