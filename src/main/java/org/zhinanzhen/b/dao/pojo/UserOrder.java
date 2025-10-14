package org.zhinanzhen.b.dao.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserOrder implements Serializable {
    private Integer serviceOrderCount;

    private Double serviceOrderAmount;

    private List<ServiceOrderManage> serviceOrderManageList;

    private List<ServiceOrderDO> serviceOrderList;
}
