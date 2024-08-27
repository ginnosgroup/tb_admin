package org.zhinanzhen.b.service;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.ChatDO;
import org.zhinanzhen.b.dao.pojo.SetupExcelDO;
import org.zhinanzhen.b.service.pojo.ChatDTO;
import org.zhinanzhen.tb.service.pojo.UserDTO;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * Date: 2021/01/26 14:05
 * Description:
 * Version: V1.0
 */
public interface WXWorkService {

    String getWXWorkUrl();

    Map<String, Object> getToken(String SECRET);

    Map<String, Object> getUserInfo(String token, String code);

    Map<String, Object> getexternalContactList(String token, String userId, String cursor, int limit);

    int add(UserDTO userDTO);

    boolean sendMsg(int id, String token);

    int updateByAuthopenid(UserDTO userDTO);

    boolean updateAuthopenidByPhone(String authOpenid ,  String phone, String areaCode);

    int addChat(ChatDTO chatDTO);

    ChatDO ChatDOByServiceOrderId(@Param("serviceOrderId") int serviceOrderId);

    void createChat(int serviceOrderId , String token);

    int addExcel(SetupExcelDO setupExcelDO);
}
