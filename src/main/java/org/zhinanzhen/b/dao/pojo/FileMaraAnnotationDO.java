package org.zhinanzhen.b.dao.pojo;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class FileMaraAnnotationDO implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;

    private Date gmtCreate;

    private Date gmtModify;

    private int serviceOrderId;

    private int userId;

    private int officialId;

    private int maraId;

    private String cloudDiskFileId;

    private Boolean isAnnotation;

    private Boolean isCheck;

    private String maraMark;

}