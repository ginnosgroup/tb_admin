package org.zhinanzhen.b.dao.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 用户信息实体类
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo {
    
    // 基础信息字段
    private String creator;
    private String role;
    private Map<String, Object> userData;
    private String nickName;
    private String description;
    private String avatar;
    private String userName;
    private String userId;
    private String defaultDriveId;
    private String domainId;
    private Long createdAt;
    private String phone;
    private String email;
    private String status;
    private Long updatedAt;


    // 工具方法：将时间戳转换为日期字符串
    public String getCreatedAtFormatted() {
        if (createdAt == null) return null;
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(new java.util.Date(createdAt));
    }

    public String getUpdatedAtFormatted() {
        if (updatedAt == null) return null;
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(new java.util.Date(updatedAt));
    }
}