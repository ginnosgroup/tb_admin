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

    private int maraId;

    private String cloudDiskFileId;

    private boolean annotationStatus;

    private boolean isCheck;

    private String maraAnnotation;

    private String maraMark;

}