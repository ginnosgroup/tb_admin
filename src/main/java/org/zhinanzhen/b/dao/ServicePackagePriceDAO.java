package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.ServicePackagePriceDO;

public interface ServicePackagePriceDAO {

	int add(ServicePackagePriceDO servicePackagePriceDO);

	int update(@Param("id") int id, @Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice,
			@Param("servicePackageId") Integer servicePackageId, @Param("regionId") Integer regionId);

	List<ServicePackagePriceDO> list(@Param("servicePackageId") Integer servicePackageId);

	ServicePackagePriceDO getById(Integer id);

	int delete(int id);

}
