package org.zhinanzhen.b.service.pojo;

import lombok.Data;
import org.zhinanzhen.b.dao.pojo.QywxExternalUserDO;

import java.util.List;

@Data
public class QywxExternalUserDTO extends QywxExternalUserDO {
    private String tags;

    private List<TagsDTO> tagsDTOS;
}
