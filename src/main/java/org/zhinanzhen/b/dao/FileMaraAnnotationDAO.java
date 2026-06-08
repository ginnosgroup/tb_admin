package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.FileMaraAnnotationDO;

public interface FileMaraAnnotationDAO {

    int add(FileMaraAnnotationDO fileMaraAnnotationDO);

    int update(FileMaraAnnotationDO fileMaraAnnotationDO);

    FileMaraAnnotationDO getById(@Param("id") int id);

    List<FileMaraAnnotationDO> list(@Param("serviceOrderId") Integer serviceOrderId,
                                    @Param("userId") Integer userId,
                                    @Param("officialId") Integer officialId);

    int deleteById(@Param("id") int id);

    int updateIsCheckByServiceOrderId(@Param("serviceOrderId") int serviceOrderId);

}