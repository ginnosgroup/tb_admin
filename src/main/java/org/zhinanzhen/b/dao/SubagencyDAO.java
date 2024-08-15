package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.zhinanzhen.b.dao.pojo.SubagencyDO;

public interface SubagencyDAO {

	public int addSubagency(SubagencyDO subagencyDo);

	public int updateSubagency(@Param("id") int id, @Param("name") String name, @Param("country") String country,
			@Param("commissionRate") double commissionRate);

	public List<SubagencyDO> listSubagency(@Param("keyword") String keyword);

	public SubagencyDO getSubagencyById(int id);

	public int deleteSubagencyById(int id);

	@Select("SELECT bs.`name` FROM b_subagency bs LEFT JOIN b_service_order so ON bs.id = so.subagency_id WHERE so.id = #{serviceOrderId}")
    String getSubagencyByServiceOrderId(int serviceOrderId);
}
