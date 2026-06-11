package org.zhinanzhen.b.service.pojo;

import lombok.Data;
import org.zhinanzhen.b.dao.pojo.OrderMaraAnnotationDO;
import org.zhinanzhen.b.dao.pojo.ServiceOrderDO;

import java.util.List;

@Data
public class SelectOfficialCheckDTO {

    private OrderMaraAnnotationDO orderMaraAnnotation;

    private ServiceOrderDO serviceOrder;

    private List<FileMaraAnnotationDTO> fileMaraAnnotationList;

}
