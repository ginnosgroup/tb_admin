package org.zhinanzhen.b.dao.pojo;

import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 * Date: 2021/08/03 下午 4:21
 * Description:
 * Version: V1.0
 */
@Data
public class AdviserServiceCountDO {

    private int count;

    private String type;

    private int serviceId;

    private int schoolId;

    private int servicePackageId;

    private int adviserId;
}
