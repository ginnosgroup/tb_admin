package org.zhinanzhen.b.service.pojo;

import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 * Date: 2020/12/04 16:27
 * Description:
 * Version: V1.0
 */
@Data
public class EachRegionNumberDTO {

    private  String name;

    private  String institutionName;

    private int total;

    private int sydney;

    private int melbourne;

    private int brisbane;

    private int adelaide;

    private int hobart;

    private int canberra;

    private int sydney2;

    /**
     * 攻坚部
     */
    private int crucial;

    private int other;
}
