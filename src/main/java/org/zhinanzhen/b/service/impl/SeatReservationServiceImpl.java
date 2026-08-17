package org.zhinanzhen.b.service.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zhinanzhen.b.dao.SeatReservationDAO;
import org.zhinanzhen.b.dao.pojo.SeatReservationDO;
import org.zhinanzhen.b.service.SeatReservationService;
import org.zhinanzhen.b.service.pojo.SeatReservationResult;
import org.zhinanzhen.tb.service.ServiceException;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

@Service("SeatReservationService")
public class SeatReservationServiceImpl implements SeatReservationService {

    private static final String[] POSTER_URLS = {
            "/webroot_new/seat-posters/seat-poster-1.jpg",
            "/webroot_new/seat-posters/seat-poster-2.png"
    };

    @Resource
    private SeatReservationDAO seatReservationDAO;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SeatReservationResult reserve(String seatRow, Integer seatNumber, String name, String email,
                                         String phone, String consultantName, String ip)
            throws ServiceException {
        String normalizedRow = normalizeSeatRow(seatRow);
        String normalizedName = StringUtils.trimToEmpty(name);
        String normalizedEmail = StringUtils.trimToEmpty(email).toLowerCase(Locale.ROOT);
        String normalizedPhone = StringUtils.trimToEmpty(phone);
        String normalizedConsultantName = StringUtils.trimToEmpty(consultantName).toLowerCase(Locale.ROOT);
        String normalizedIp = StringUtils.trimToEmpty(ip);

        if (seatNumber == null || !isSelectableSeat(normalizedRow, seatNumber)) {
            throw new ServiceException("请选择有效的座位");
        }
        if (StringUtils.isBlank(normalizedName)) {
            throw new ServiceException("姓名不能为空");
        }
        if (!isValidEmail(normalizedEmail)) {
            throw new ServiceException("请输入有效的邮箱");
        }
        if (StringUtils.isBlank(normalizedPhone)) {
            throw new ServiceException("电话号码不能为空");
        }
        if (StringUtils.isBlank(normalizedConsultantName)) {
            throw new ServiceException("顾问姓名不能为空");
        }
        if (StringUtils.isBlank(normalizedIp)) {
            throw new ServiceException("无法获取访问IP，请稍后重试");
        }

        String seatCode = normalizedRow + seatNumber;
        if (seatReservationDAO.getBySeatCode(seatCode) != null) {
            throw new ServiceException("该座位已经被选走，请重新选择");
        }
        if (seatReservationDAO.getByEmail(normalizedEmail) != null) {
            throw new ServiceException("每个邮箱只能选择一个座位");
        }
        if (seatReservationDAO.getByIp(normalizedIp) != null) {
            throw new ServiceException("每个IP只能选择一个座位");
        }

        // 按顾问姓名分别计数；唯一索引和重试逻辑保证并发请求不会拿到相同序号。
        for (int attempt = 0; attempt < 3; attempt++) {
            Integer maxSequence = seatReservationDAO.getMaxConsultantSequence(normalizedConsultantName);
            int sequence = maxSequence == null ? 1 : maxSequence + 1;
            SeatReservationDO record = new SeatReservationDO();
            record.setSeatRow(normalizedRow);
            record.setSeatNumber(seatNumber);
            record.setSeatCode(seatCode);
            record.setName(normalizedName);
            record.setEmail(normalizedEmail);
            record.setPhone(normalizedPhone);
            record.setIp(normalizedIp);
            record.setConsultantName(normalizedConsultantName);
            record.setConsultantSequence(sequence);
            record.setConsultantCode(formatSequence(sequence));
            try {
                if (seatReservationDAO.add(record) <= 0) {
                    throw new ServiceException("座位预约失败");
                }
                return new SeatReservationResult(record.getId(), normalizedName, normalizedConsultantName,
                        formatSequence(sequence), formatSequence(sequence), normalizedRow, seatNumber, seatCode,
                        randomPosterUrl());
            } catch (DuplicateKeyException e) {
                // 座位/邮箱/IP冲突直接提示；若只是序号并发冲突，则重新取最大值再试。
                if (seatReservationDAO.getBySeatCode(seatCode) != null) {
                    throw new ServiceException("该座位已经被选走，请重新选择");
                }
                if (seatReservationDAO.getByEmail(normalizedEmail) != null) {
                    throw new ServiceException("每个邮箱只能选择一个座位");
                }
                if (seatReservationDAO.getByIp(normalizedIp) != null) {
                    throw new ServiceException("每个IP只能选择一个座位");
                }
                if (attempt == 2) {
                    throw new ServiceException("顾问code生成失败，请稍后重试", e);
                }
            }
        }
        throw new ServiceException("座位预约失败，请稍后重试");
    }

    @Override
    public List<String> listOccupiedSeatCodes() throws ServiceException {
        try {
            List<String> result = seatReservationDAO.listOccupiedSeatCodes();
            return result == null ? Collections.<String>emptyList() : result;
        } catch (Exception e) {
            throw new ServiceException("查询座位状态失败", e);
        }
    }

    private String normalizeSeatRow(String seatRow) {
        return StringUtils.upperCase(StringUtils.trimToEmpty(seatRow));
    }

    private boolean isSelectableSeat(String row, int number) {
        if (number < 1 || number > 19) {
            return false;
        }
        if ("H".equals(row)) {
            return number >= 1 && number <= 19;
        }
        if ("G".equals(row)) {
            return (number >= 1 && number <= 2) || (number >= 5 && number <= 17);
        }
        if ("A".equals(row)) {
            return (number >= 6 && number <= 9) || (number >= 13 && number <= 17);
        }
        return ("B".equals(row) || "C".equals(row) || "D".equals(row)
                || "E".equals(row) || "F".equals(row)) && number >= 5 && number <= 17;
    }

    private boolean isValidEmail(String email) {
        return StringUtils.isNotBlank(email)
                && email.length() <= 255
                && email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    }

    private String formatSequence(int sequence) {
        return String.format(Locale.ROOT, "%02d", sequence);
    }

    private String randomPosterUrl() {
        return POSTER_URLS[ThreadLocalRandom.current().nextInt(POSTER_URLS.length)];
    }
}
