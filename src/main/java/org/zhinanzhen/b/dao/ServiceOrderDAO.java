package org.zhinanzhen.b.dao;

import java.util.List;

import org.zhinanzhen.b.dao.pojo.ServiceOrderDO;

public interface ServiceOrderDAO {

	public int addServiceOrder(ServiceOrderDO serviceOrderDo);

	public int updateServiceOrder(ServiceOrderDO serviceOrderDo);

	public List<ServiceOrderDO> listServiceOrder();

	public ServiceOrderDO getServiceOrderById(int id);

	public int deleteServiceOrderById(int id);

}
