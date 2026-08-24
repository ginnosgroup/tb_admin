package org.zhinanzhen.b.service.impl;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zhinanzhen.b.dao.SeatReservationDAO;
import org.zhinanzhen.b.dao.pojo.SeatReservationDO;
import org.zhinanzhen.b.service.SeatReservationService;
import org.zhinanzhen.b.service.pojo.SeatReservationResult;
import org.zhinanzhen.b.utils.SeatReservationFileStorage;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.utils.SendEmailUtil;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Service("SeatReservationService")
public class SeatReservationServiceImpl implements SeatReservationService {

    private static final Logger LOG = LoggerFactory.getLogger(SeatReservationServiceImpl.class);

    /** 页面票根使用的海报（对应示例 1.png 左侧海报）。 */
    private static final String DISPLAY_POSTER_URL = "/webroot_new/seat-posters/seat-poster-2.png";
    /** 邮件中发送的单独海报（对应示例 2.jpg）。 */
    private static final String EMAIL_POSTER_URL = "/webroot_new/seat-posters/seat-poster-1.jpg";
    /** 允许填写的顾问姓名白名单（统一小写比较，不区分大小写）。 */
    private static final List<String> ALLOWED_CONSULTANT_NAMES = Arrays.asList(
            "jay", "winnie", "emily", "michael", "shawn", "echo", "mia");
    /** 需要在票根图片前展示无障碍座位调整说明的座位。 */
    private static final List<String> DISABLED_SEAT_NOTICE_CODES = Arrays.asList("H2", "H17", "E17");

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
        // 顾问姓名只允许填写白名单内的 7 个姓名（不区分大小写）
        if (!ALLOWED_CONSULTANT_NAMES.contains(normalizedConsultantName)) {
            throw new ServiceException("顾问姓名填写错误，请填写正确的顾问姓名");
        }
        // 电话必填且不能重复；IP 不再必填，IP 不再做唯一限制。

        String seatCode = normalizedRow + seatNumber;
        if (seatReservationDAO.getBySeatCode(seatCode) != null) {
            throw new ServiceException("该座位已经被选走，请重新选择");
        }
        if (seatReservationDAO.getByEmail(normalizedEmail) != null) {
            throw new ServiceException("每个邮箱只能选择一个座位");
        }
        if (seatReservationDAO.getByPhone(normalizedPhone) != null) {
            throw new ServiceException("该电话号码已经选座");
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
            record.setPosterUrl(DISPLAY_POSTER_URL);
            try {
                if (seatReservationDAO.add(record) <= 0) {
                    throw new ServiceException("座位预约失败");
                }
                return toResult(record);
            } catch (DuplicateKeyException e) {
                // 座位/邮箱冲突直接提示；若只是序号并发冲突，则重新取最大值再试。
                // 记录冲突现场（哪个唯一键冲突一眼可见，例如残留的 uk_b_seat_reservation_ip 索引），
                // 避免出现"顾问名字有数字报错"这类误判。
                LOG.warn("座位预约插入冲突: seatCode={}, email={}, consultantName={}, sequence={}, attempt={}",
                        seatCode, normalizedEmail, normalizedConsultantName, sequence, attempt, e);
                if (seatReservationDAO.getBySeatCode(seatCode) != null) {
                    throw new ServiceException("该座位已经被选走，请重新选择");
                }
                if (seatReservationDAO.getByEmail(normalizedEmail) != null) {
                    throw new ServiceException("每个邮箱只能选择一个座位");
                }
                if (seatReservationDAO.getByPhone(normalizedPhone) != null) {
                    throw new ServiceException("该电话号码已经选座");
                }
                if (attempt == 2) {
                    throw new ServiceException("顾问code生成失败，请稍后重试", e);
                }
            }
        }
        throw new ServiceException("座位预约失败，请稍后重试");
    }

    @Override
    public SeatReservationResult getByNameAndEmail(String name, String email) throws ServiceException {
        String normalizedName = StringUtils.trimToEmpty(name);
        String normalizedEmail = StringUtils.trimToEmpty(email).toLowerCase(Locale.ROOT);
        if (StringUtils.isBlank(normalizedName) || !isValidEmail(normalizedEmail)) {
            return null;
        }
        try {
            return toResult(seatReservationDAO.getByNameAndEmail(normalizedName, normalizedEmail));
        } catch (Exception e) {
            throw new ServiceException("查询票根失败", e);
        }
    }

    @Override
    public SeatReservationResult getById(Integer id) throws ServiceException {
        if (id == null) {
            return null;
        }
        try {
            return toResult(seatReservationDAO.getById(id));
        } catch (Exception e) {
            throw new ServiceException("查询票根失败", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateImagePaths(Integer id, String ticketImagePath, String emailImagePath)
            throws ServiceException {
        if (id == null || StringUtils.isBlank(ticketImagePath) || StringUtils.isBlank(emailImagePath)) {
            throw new ServiceException("票根图片保存失败");
        }
        if (seatReservationDAO.updateImagePaths(id, ticketImagePath, emailImagePath) <= 0) {
            throw new ServiceException("票根图片记录失败");
        }
    }

    @Override
    public void sendTicketEmail(String name, String email) throws ServiceException {
        String normalizedName = StringUtils.trimToEmpty(name);
        String normalizedEmail = StringUtils.trimToEmpty(email).toLowerCase(Locale.ROOT);
        if (StringUtils.isBlank(normalizedName) || !isValidEmail(normalizedEmail)) {
            throw new ServiceException("请输入正确的姓名和邮箱");
        }
        SeatReservationDO record;
        try {
            record = seatReservationDAO.getByNameAndEmail(normalizedName, normalizedEmail);
        } catch (Exception e) {
            throw new ServiceException("查询票根失败", e);
        }
        if (record == null) {
            throw new ServiceException("没有找到对应的座位预约记录，请先查询票根");
        }
        if (!isValidEmail(record.getEmail())) {
            throw new ServiceException("预约邮箱无效，无法发送票根");
        }
        String ticketImage = readImageAsDataUrl(record.getEmailImagePath());
        StringBuilder content = new StringBuilder();
        String seatCode = StringUtils.upperCase(StringUtils.trimToEmpty(record.getSeatCode()));
        if (DISABLED_SEAT_NOTICE_CODES.contains(seatCode)) {
            content.append("<p>同学您好，我们注意到您之前选择的座位为 <strong>Seat (Disabled)</strong>（无障碍座位）。")
                    .append("为了将无障碍座位优先保留给有实际需要的同学，我们已为您重新安排了其他座位，")
                    .append("<strong>最新的电子票根也已发送给您</strong>。请您明天入场时务必使用")
                    .append("<strong>最新的电子票根</strong>，并按照新票根上的座位号入座。之前的票根将不再使用。")
                    .append("如果您本人确实有无障碍座位的需求，请直接联系您的顾问，我们会协助您安排合适的座位。")
                    .append("感谢您的理解与配合！指南针留学移民</p>");
        } else {
            content.append("<p>请持电子票根入场。</p>");
        }
        content.append("<p><img src=\"").append(ticketImage)
                .append("\" alt=\"电影票根\" style=\"max-width:100%;height:auto;\" /></p>");
        SendEmailUtil.send(record.getEmail(), "您的电影票根", content.toString());
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
            return number >= 5 && number <= 17;
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

    private SeatReservationResult toResult(SeatReservationDO record) {
        if (record == null) {
            return null;
        }
        String ticketPath = valueOrDefault(record.getTicketImagePath(),
                valueOrDefault(record.getPosterUrl(), DISPLAY_POSTER_URL));
        String emailPath = valueOrDefault(record.getEmailImagePath(), EMAIL_POSTER_URL);
        return new SeatReservationResult(record.getId(), record.getName(), record.getConsultantName(),
                record.getConsultantCode(), formatSequenceValue(record.getConsultantSequence()),
                record.getSeatRow(), record.getSeatNumber(), record.getSeatCode(),
                ticketPath, emailPath, record.getTicketImagePath(), record.getEmailImagePath());
    }

    private String formatSequenceValue(Integer sequence) {
        return sequence == null ? null : formatSequence(sequence);
    }

    private String valueOrDefault(String value, String defaultValue) {
        return StringUtils.isBlank(value) ? defaultValue : value;
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String readImageAsDataUrl(String imagePath) throws ServiceException {
        String path = StringUtils.trimToEmpty(imagePath);
        if (StringUtils.isBlank(path) || !path.startsWith("/uploads/seat_reservation/")
                || path.contains("..") || path.indexOf('\0') >= 0) {
            throw new ServiceException("邮件图片路径无效，请重新生成票根");
        }
        File imageFile;
        try {
            imageFile = SeatReservationFileStorage.resolve(path);
        } catch (IOException e) {
            throw new ServiceException("邮件图片路径无效，请重新生成票根", e);
        }
        if (!imageFile.isFile() || imageFile.length() > 8 * 1024 * 1024) {
            throw new ServiceException("邮件图片不存在，请重新生成票根");
        }
        try {
            byte[] bytes = Files.readAllBytes(imageFile.toPath());
            String lower = StringUtils.lowerCase(imageFile.getName());
            String mimeType = lower.endsWith(".png") ? "image/png"
                    : (lower.endsWith(".gif") ? "image/gif" : "image/jpeg");
            return "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (IOException e) {
            throw new ServiceException("读取邮件图片失败", e);
        }
    }
}
