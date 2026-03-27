package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.ServiceOrderOfficialRemarksDO;

public interface ServiceOrderOfficialRemarksDAO {

	int add(ServiceOrderOfficialRemarksDO serviceOrderOfficialRemarksDo);
	
	int update(ServiceOrderOfficialRemarksDO serviceOrderOfficialRemarksDo);

	public List<ServiceOrderOfficialRemarksDO> list(@Param("officialId") Integer officialId,
			@Param("serviceOrderId") Integer serviceOrderId);

	public List<ServiceOrderOfficialRemarksDO> listByServiceOrderIds(@Param("officialId") Integer officialId,
			@Param("serviceOrderIds") List<Integer> serviceOrderIds);

	public int delete(int id);

}
