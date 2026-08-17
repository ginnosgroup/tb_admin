package org.zhinanzhen.b.service;

import org.zhinanzhen.b.service.pojo.SeatReservationResult;
import org.zhinanzhen.tb.service.ServiceException;

import java.util.List;

public interface SeatReservationService {

    SeatReservationResult reserve(String seatRow, Integer seatNumber, String name, String email, String phone,
                                  String consultantName, String ip)
            throws ServiceException;

    List<String> listOccupiedSeatCodes() throws ServiceException;
}
