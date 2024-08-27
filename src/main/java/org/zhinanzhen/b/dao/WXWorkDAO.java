package org.zhinanzhen.b.dao;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.ChatDO;
import org.zhinanzhen.b.dao.pojo.SetupExcelDO;

/**
 * Created with IntelliJ IDEA.
 * Date: 2021/03/24 10:15
 * Description:
 * Version: V1.0
 */
public interface WXWorkDAO {

    int addChat(ChatDO chatDO);

    ChatDO ChatDOByServiceOrderId(@Param("serviceOrderId") int serviceOrderId);

    int addExcel(SetupExcelDO setupExcelDO);
}
