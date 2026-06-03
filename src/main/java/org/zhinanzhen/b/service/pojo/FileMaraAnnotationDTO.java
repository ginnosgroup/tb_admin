package org.zhinanzhen.b.service.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class FileMaraAnnotationDTO {

    private int id;

    private Date gmtCreate;

    private Date gmtModify;

    private int serviceOrderId;

    private int userId;

    private int officialId;

    private int cloudDiskFileId;

    private boolean isAnnotation;

    private boolean isCheck;

    private String maraMark;

}