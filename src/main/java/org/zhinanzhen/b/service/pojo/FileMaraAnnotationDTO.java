package org.zhinanzhen.b.service.pojo;

import java.util.Date;

import lombok.Data;
import org.zhinanzhen.b.dao.pojo.OrderMaraAnnotationDO;
import org.zhinanzhen.b.dao.pojo.ServiceOrderDO;
import org.zhinanzhen.b.service.pojo.CloudDiskFile;
import org.zhinanzhen.tb.dao.pojo.UserDO;

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

    private Boolean isAnnotation;

    private String annotationMark;

    private OrderMaraAnnotationDO orderMaraAnnotation;

    private UserDO user;

    private CloudDiskFile cloudDiskFile;

    private ServiceOrderDO serviceOrder;

}