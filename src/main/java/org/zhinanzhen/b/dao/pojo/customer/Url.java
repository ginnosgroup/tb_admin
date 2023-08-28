package org.zhinanzhen.b.dao.pojo.customer;

import lombok.Data;

@Data
public class Url {
    // 身份证图片路径
    private PhotoId photoId;
    // 护照图片路径
    private Passport passport;
    // 曾持有护照图片路径
    private Tpassport Tpassport;
    // 户口本图片路径
    private Birth birth;
    // 其他图片路径
    private Other other;

}
