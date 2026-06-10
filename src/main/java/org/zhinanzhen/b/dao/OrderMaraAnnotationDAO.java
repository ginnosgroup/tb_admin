package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.OrderMaraAnnotationDO;

public interface OrderMaraAnnotationDAO {

    int add(OrderMaraAnnotationDO orderMaraAnnotationDO);

    int update(OrderMaraAnnotationDO orderMaraAnnotationDO);

    OrderMaraAnnotationDO getByServiceOrderId(@Param("serviceOrderId") int serviceOrderId);

    List<OrderMaraAnnotationDO> listByServiceOrderIds(@Param("serviceOrderIds") List<Integer> serviceOrderIds);

}
