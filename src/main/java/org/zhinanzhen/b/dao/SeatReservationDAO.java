package org.zhinanzhen.b.dao;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.SeatReservationDO;

import java.util.List;

public interface SeatReservationDAO {

    int add(SeatReservationDO record);

    SeatReservationDO getById(@Param("id") Integer id);

    int updateImagePaths(@Param("id") Integer id,
                         @Param("ticketImagePath") String ticketImagePath,
                         @Param("emailImagePath") String emailImagePath);

    SeatReservationDO getBySeatCode(@Param("seatCode") String seatCode);

    SeatReservationDO getByEmail(@Param("email") String email);

    SeatReservationDO getByPhone(@Param("phone") String phone);

    SeatReservationDO getByNameAndEmail(@Param("name") String name, @Param("email") String email);

    Integer getMaxConsultantSequence(@Param("consultantName") String consultantName);

    List<String> listOccupiedSeatCodes();

	/** 查询全部座位预约记录，用于后台批量通知。 */
	List<SeatReservationDO> listAll();
}
