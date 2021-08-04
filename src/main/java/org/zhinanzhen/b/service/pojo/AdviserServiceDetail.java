package org.zhinanzhen.b.service.pojo;

import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 * Date: 2021/08/02 下午 6:31
 * Description:
 * Version: V1.0
 */
@Data
public class AdviserServiceDetail {
    private int count;
    private String serviceName;

    public AdviserServiceDetail(int count) {
        this.count = count;
    }
    public AdviserServiceDetail(){}
}
