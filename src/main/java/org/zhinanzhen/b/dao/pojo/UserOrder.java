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
    private Integer serviceOrderCount; // 订单数量

    private Double serviceOrderAmount; // 订单金额

    private List<ServiceOrderManage> serviceOrderManageList; // 订单管理列表

    private List<ServiceOrderDO> serviceOrderList; // 订单列表
}
