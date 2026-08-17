package org.zhinanzhen.b.controller;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.zhinanzhen.b.service.SeatReservationService;
import org.zhinanzhen.b.service.pojo.SeatReservationResult;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/** 座位选择和预约接口。 */
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/seat")
public class SeatReservationController extends BaseController {

    @Resource
    private SeatReservationService seatReservationService;

    @RequestMapping(value = "/occupied", method = RequestMethod.GET)
    @ResponseBody
    public Response<List<String>> listOccupied(HttpServletResponse response) {
        try {
            setGetHeader(response);
            return new Response<>(0, "查询成功", seatReservationService.listOccupiedSeatCodes());
        } catch (ServiceException e) {
            return new Response<>(1, e.getMessage(), null);
        }
    }

    @RequestMapping(value = "/reserve", method = RequestMethod.POST)
    @ResponseBody
    public Response<SeatReservationResult> reserve(
            @RequestParam(value = "seatRow", required = false) String seatRow,
            @RequestParam(value = "seatNumber", required = false) Integer seatNumber,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "consultantName", required = false) String consultantName,
            HttpServletRequest request, HttpServletResponse response) {
        try {
            setPostHeader(response);
            String ip = getClientIp(request);
            SeatReservationResult result = seatReservationService.reserve(seatRow, seatNumber, name, email, phone,
                    consultantName, ip);
            return new Response<>(0, "座位选择成功", result);
        } catch (ServiceException e) {
            return new Response<>(1, e.getMessage(), null);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response<>(1, "座位选择失败", null);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotBlank(forwarded)) {
            int commaIndex = forwarded.indexOf(',');
            return (commaIndex >= 0 ? forwarded.substring(0, commaIndex) : forwarded).trim();
        }
        return request.getRemoteAddr();
    }
}
