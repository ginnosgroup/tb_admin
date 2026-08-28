package org.zhinanzhen.b.service;

import org.zhinanzhen.b.service.pojo.SeatReservationResult;
import org.zhinanzhen.tb.service.ServiceException;

import java.util.List;
import java.util.Map;

public interface SeatReservationService {

    SeatReservationResult reserve(String seatRow, Integer seatNumber, String name, String email, String phone,
                                  String consultantName, String ip)
            throws ServiceException;

    SeatReservationResult getByNameAndEmail(String name, String email) throws ServiceException;

    SeatReservationResult getById(Integer id) throws ServiceException;

    void updateImagePaths(Integer id, String ticketImagePath, String emailImagePath) throws ServiceException;

    void sendTicketEmail(String name, String email) throws ServiceException;

    List<String> listOccupiedSeatCodes() throws ServiceException;

	/** 查询全部预约记录，并按唯一有效邮箱提交电影包场提醒邮件。 */
	Map<String, Integer> sendMovieReminderToAll() throws ServiceException;
}
