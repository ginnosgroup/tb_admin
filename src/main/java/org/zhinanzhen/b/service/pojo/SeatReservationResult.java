package org.zhinanzhen.b.service.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 座位预约成功后返回的票根数据。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatReservationResult {

    private Integer reservationId;
    private String customerName;
    private String consultantName;
    private String consultantCode;
    private String consultantSequence;
    private String seatRow;
    private Integer seatNumber;
    private String seatCode;
    private String posterUrl;
    private String emailPosterUrl;
    private String ticketImagePath;
    private String emailImagePath;
}
