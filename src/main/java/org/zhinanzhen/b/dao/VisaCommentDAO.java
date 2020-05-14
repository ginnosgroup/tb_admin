package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.VisaCommentDO;

public interface VisaCommentDAO {

	int add(VisaCommentDO visaCommentDo);

	public List<VisaCommentDO> list(@Param("visaId") Integer visaId);

	public int delete(int id);

}
