package org.zhinanzhen.tb.dao;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.tb.dao.pojo.OrderDO;

public interface OrderDAO {

    public boolean updateAdviserIdById(@Param("id") int id, @Param("adviserId") int adviserId);

    public int countOrder(@Param("id") Integer id, @Param("name") String name, @Param("regionId") Integer regionId,
	    @Param("state") String state, @Param("userName") String userName, @Param("userPhone") String userPhone);

    public List<OrderDO> listOrder(@Param("id") Integer id, @Param("name") String name,
	    @Param("regionId") Integer regionId, @Param("state") String state, @Param("userName") String userName,
	    @Param("userPhone") String userPhone, @Param("offset") int offset, @Param("rows") int rows);

    public OrderDO getOrderById(int id);

    public List<OrderDO> listOrderByUserId(@Param("userId") int userId);

    public List<OrderDO> listOrderByIntroducerId(@Param("introducerId") int introducerId);

    boolean updateRemainPay(@Param("id") int id, @Param("remainPayAmount") double remainPayAmount,
	    @Param("remainPayDate") Date remainPayDate, @Param("remainPayBalance") double remainPayBalance);

}
