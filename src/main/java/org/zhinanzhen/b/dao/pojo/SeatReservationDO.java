package org.zhinanzhen.b.dao.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/** 座位预约记录。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatReservationDO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private Date gmtCreate;
    private Date gmtModify;
    private String seatRow;
    private Integer seatNumber;
    private String seatCode;
    private String name;
    private String email;
    private String phone;
    private String ip;
    private String consultantName;
    private String consultantCode;
    private Integer consultantSequence;
    private String posterUrl;
    /** 页面查询时展示的完整票根图片路径。 */
    private String ticketImagePath;
    /** 邮件中发送的独立海报图片路径。 */
    private String emailImagePath;
}
