package org.zhinanzhen.b.controller;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.zhinanzhen.b.service.SeatReservationService;
import org.zhinanzhen.b.service.pojo.SeatReservationResult;
import org.zhinanzhen.b.utils.SeatReservationFileStorage;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    @RequestMapping(value = "/query", method = RequestMethod.GET)
    @ResponseBody
    public Response<SeatReservationResult> query(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "email", required = false) String email,
            HttpServletResponse response) {
        try {
            setGetHeader(response);
            SeatReservationResult result = seatReservationService.getByNameAndEmail(name, email);
            if (result == null) {
                return new Response<>(1, "没有找到对应的座位预约记录", null);
            }
            return new Response<>(0, "查询成功", result);
        } catch (ServiceException e) {
            return new Response<>(1, e.getMessage(), null);
        }
    }

    /**
     * 给前端生成票根时读取原始海报。
     *
     * 海报通过后端返回，避免前端页面与静态资源不在同一域名时，canvas 因跨域而无法导出。
     */
    @RequestMapping(value = "/poster", method = RequestMethod.GET)
    public void getPoster(
            @RequestParam(value = "type", required = false) String type,
            HttpServletResponse response) throws IOException {
        setGetHeader(response);
        boolean emailPoster = "email".equalsIgnoreCase(StringUtils.trimToEmpty(type));
        String fileName = emailPoster ? "seat-poster-1.jpg" : "seat-poster-2.png";
        File poster = findPoster(fileName);
        if (poster == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "电影海报文件不存在");
            return;
        }

        writeImage(poster, emailPoster ? "image/jpeg" : "image/png", response);
    }

    /** 读取已经生成并保存到 /data 的票根图片。 */
    @RequestMapping(value = "/image", method = RequestMethod.GET)
    public void getReservationImage(
            @RequestParam(value = "reservationId", required = false) Integer reservationId,
            @RequestParam(value = "type", required = false) String type,
            HttpServletResponse response) throws IOException {
        setGetHeader(response);
        try {
            SeatReservationResult result = seatReservationService.getById(reservationId);
            if (result == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "预约记录不存在");
                return;
            }
            boolean emailImage = "email".equalsIgnoreCase(StringUtils.trimToEmpty(type));
            String imagePath = emailImage ? result.getEmailImagePath() : result.getTicketImagePath();
            if (StringUtils.isBlank(imagePath)) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "票根图片尚未生成");
                return;
            }
            File image = SeatReservationFileStorage.resolve(imagePath);
            if (!image.isFile()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "票根图片不存在");
                return;
            }
            String lowerName = image.getName().toLowerCase();
            String contentType = lowerName.endsWith(".png") ? "image/png" : "image/jpeg";
            writeImage(image, contentType, response);
        } catch (ServiceException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        }
    }

    private void writeImage(File image, String contentType, HttpServletResponse response) throws IOException {
        response.setContentType(contentType);
        response.setContentLength((int) image.length());
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        try (InputStream input = new FileInputStream(image);
             OutputStream output = response.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = input.read(buffer)) >= 0) {
                output.write(buffer, 0, length);
            }
            output.flush();
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

    @RequestMapping(value = "/images", method = RequestMethod.POST)
    @ResponseBody
    public Response<SeatReservationResult> saveImages(
            @RequestParam(value = "reservationId", required = false) Integer reservationId,
            @RequestParam(value = "ticketImage", required = false) MultipartFile ticketImage,
            @RequestParam(value = "emailImage", required = false) MultipartFile emailImage,
            HttpServletResponse response) {
        String ticketImagePath = null;
        String emailImagePath = null;
        try {
            setPostHeader(response);
            if (reservationId == null) {
                throw new ServiceException("预约记录ID不能为空");
            }
            ticketImagePath = uploadSeatImage(ticketImage, reservationId,
                    "/uploads/seat_reservation/ticket/");
            emailImagePath = uploadSeatImage(emailImage, reservationId,
                    "/uploads/seat_reservation/email/");
            seatReservationService.updateImagePaths(reservationId, ticketImagePath, emailImagePath);
            return new Response<>(0, "票根图片保存成功", seatReservationService.getById(reservationId));
        } catch (ServiceException e) {
            deleteUploadedImage(ticketImagePath);
            deleteUploadedImage(emailImagePath);
            return new Response<>(1, e.getMessage(), null);
        } catch (Exception e) {
            deleteUploadedImage(ticketImagePath);
            deleteUploadedImage(emailImagePath);
            e.printStackTrace();
            return new Response<>(1, "票根图片保存失败", null);
        }
    }

    @RequestMapping(value = "/sendEmail", method = RequestMethod.POST)
    @ResponseBody
    public Response<String> sendEmail(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "email", required = false) String email,
            HttpServletResponse response) {
        try {
            setPostHeader(response);
            seatReservationService.sendTicketEmail(name, email);
            return new Response<>(0, "票根已发送到预约邮箱", null);
        } catch (ServiceException e) {
            return new Response<>(1, e.getMessage(), null);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response<>(1, "票根邮件发送失败", null);
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

    private String uploadSeatImage(MultipartFile file, Integer reservationId, String directory)
            throws Exception {
        if (file == null || file.isEmpty()) {
            throw new ServiceException("票根图片不能为空，请刷新页面后重新选择座位");
        }
        String originalFileName = StringUtils.trimToEmpty(file.getOriginalFilename());
        int dotIndex = originalFileName.lastIndexOf('.');
        String extension = dotIndex >= 0 ? originalFileName.substring(dotIndex + 1).toLowerCase() : "";
        if (!("png".equals(extension) || "jpg".equals(extension) || "jpeg".equals(extension))) {
            throw new ServiceException("票根图片格式只能是 png、jpg 或 jpeg");
        }

        String fileName = reservationId + "_" + System.currentTimeMillis() + "." + extension;
        String webPath = directory + fileName;
        File target = SeatReservationFileStorage.resolve(webPath);
        File parent = target.getParentFile();
        if ((!parent.isDirectory() && !parent.mkdirs()) || !parent.isDirectory()) {
            throw new IOException("无法创建票根图片保存目录：" + parent.getAbsolutePath());
        }
        try (InputStream input = file.getInputStream();
             OutputStream output = new FileOutputStream(target)) {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = input.read(buffer)) >= 0) {
                output.write(buffer, 0, length);
            }
            output.flush();
        }
        return webPath;
    }

    private void deleteUploadedImage(String path) {
        if (StringUtils.isNotBlank(path) && path.startsWith("/uploads/seat_reservation/")) {
            try {
                File file = SeatReservationFileStorage.resolve(path);
                if (file.isFile()) {
                    file.delete();
                }
            } catch (Exception ignored) {
                // 清理失败不影响预约结果提示。
            }
        }
    }

    private File findPoster(String fileName) {
        String customDirectory = System.getProperty("seat.poster.dir");
        String[] directories = new String[]{
                customDirectory,
                "E:/webroot_new/seat-posters",
                "/opt/webroot_new/seat-posters"
        };
        for (String directory : directories) {
            if (StringUtils.isBlank(directory)) {
                continue;
            }
            File file = new File(directory, fileName);
            if (file.isFile() && file.canRead()) {
                return file;
            }
        }
        return null;
    }
}
