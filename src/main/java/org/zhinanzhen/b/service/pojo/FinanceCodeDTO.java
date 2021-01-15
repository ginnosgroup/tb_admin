package org.zhinanzhen.b.service.pojo;

import lombok.Data;
import org.zhinanzhen.b.dao.pojo.FinanceCodeDO;
import org.zhinanzhen.tb.service.pojo.AdviserDTO;
import org.zhinanzhen.tb.service.pojo.UserDTO;
/**
 * Created with IntelliJ IDEA.
 * Date: 2020/12/21 15:21
 * Description:
 * Version: V1.0
 */
@Data
public class FinanceCodeDTO extends FinanceCodeDO {

    private UserDTO user;

    private AdviserDTO adviser;
}
