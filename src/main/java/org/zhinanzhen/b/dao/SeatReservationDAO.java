package org.zhinanzhen.b.dao;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.SeatReservationDO;

import java.util.List;

public interface SeatReservationDAO {

    int add(SeatReservationDO record);

    SeatReservationDO getBySeatCode(@Param("seatCode") String seatCode);

    SeatReservationDO getByEmail(@Param("email") String email);

    SeatReservationDO getByIp(@Param("ip") String ip);

    Integer getMaxConsultantSequence(@Param("consultantName") String consultantName);

    List<String> listOccupiedSeatCodes();
}
