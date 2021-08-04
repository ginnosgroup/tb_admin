package org.zhinanzhen.b.service.pojo;

import lombok.Data;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Date: 2021/08/03 下午 5:07
 * Description:
 * Version: V1.0
 */
@Data
public class AdviserServiceCountDTO {

    //private int count;

    //private String type;

    //private int serviceId;

    //private int schoolId;

    //private int servicePackageId;

    //private int adviserId;

    private String adviserName;

    private List<AdviserServiceDetail> details;
}
