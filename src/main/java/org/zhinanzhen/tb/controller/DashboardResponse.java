package org.zhinanzhen.tb.controller;

import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 * Date: 2021/12/20 上午 11:02
 * Description:仪表板 图标展示数据API接口返回对象
 * Version: V1.0
 */
@Data
public class DashboardResponse<T> {
    /**
     * 编码
     */
    private int code;

    /**
     * 信息
     */
    private String message;

    /**
     * 数据
     */
    private T data;

    /**
     * 开始时间
     */
    private String dateOne;

    /**
     * 结束时间
     */
    private String dateTwo;

    /**
     * 补充数据
     */
    private T supplyData;

    public DashboardResponse(int code, String message, T data, String dateOne, String dateTwo) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.dateOne = dateOne;
        this.dateTwo = dateTwo;
    }

    public DashboardResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public DashboardResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public DashboardResponse(int code, String message, T data, String dateOne, String dateTwo, T supplyData) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.dateOne = dateOne;
        this.dateTwo = dateTwo;
        this.supplyData = supplyData;
    }
}
