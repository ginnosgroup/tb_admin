package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.SubagencyDO;

public interface SubagencyDAO {

	public int addSubagency(SubagencyDO subagencyDO);

	public int updateSubagency(@Param("id") int id, @Param("name") String name,
			@Param("commissionRate") double commissionRate);

	public List<SubagencyDO> listSubagency(@Param("keyword") String keyword);

	public SubagencyDO getSubagencyById(int id);

	public int deleteSubagencyById(int id);

}
