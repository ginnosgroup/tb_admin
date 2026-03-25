package org.zhinanzhen.b.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.zhinanzhen.b.dao.pojo.UserCloud;
import org.zhinanzhen.b.service.CloudDiskService;
import org.zhinanzhen.b.service.pojo.CloudDiskFile;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.ListResponse;
import org.zhinanzhen.tb.controller.Response;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/cloudDisk")
@Slf4j
public class CloudDiskController extends BaseController {

    @Resource
    private CloudDiskService cloudDiskService;

    @RequestMapping(value = "/put", method = RequestMethod.POST)
    @ResponseBody
    public Response<String> put(
            @RequestParam(value = "id", required = false) Integer id,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "type") String type,
            @RequestParam(value = "applicantId", required = false) Integer applicantId,
            @RequestParam(value = "userId", required = false) Integer userId,
            @RequestParam(value = "parentFileId") String parentFileId,
            @RequestParam(value = "folderName", required = false) String folderName,
            @RequestParam(value = "relativePath", required = false) String relativePath,
            HttpServletRequest request, HttpServletResponse response) {
        super.setPostHeader(response);
        AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
        Integer adviserId = adminUserLoginInfo.getAdviserId();
        Integer officialId = adminUserLoginInfo.getOfficialId();
        try {
            int add = 0;
            if (StringUtils.isEmpty(relativePath)) {
                add = cloudDiskService.addAndUpdate(file, type, applicantId, userId, parentFileId, adviserId, id, folderName, officialId, relativePath);
            } else {
                add = cloudDiskService.addAndUpdate(file, userId, parentFileId, adviserId, officialId, relativePath);
            }
            if (add == -1) {
                return new Response<String>(1, "文件或文件夹已存在", "");
            }
            if (add > 0) {
                return new Response<String>(0, "上传成功", "");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Response<String>(1, "上传失败", "");
        }
        return new Response<String>(0, "上传成功", "");
    }

    @RequestMapping(value = "/update", method = RequestMethod.GET)
    @ResponseBody
    public Response<String> update(
            @RequestParam(value = "id", required = false) Integer id,
            @RequestParam(value = "fileId", required = false) String fileId,
            @RequestParam(value = "type") String type,
            @RequestParam(value = "applicantId", required = false) Integer applicantId,
            @RequestParam(value = "applicantId", required = false) Integer userId,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "relativePath", required = false) String relativePath,
            HttpServletRequest request, HttpServletResponse response) {
        super.setPostHeader(response);
        AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
        Integer adviserId = adminUserLoginInfo.getAdviserId();
        Integer officialId = adminUserLoginInfo.getOfficialId();
        try {
            int add = cloudDiskService.update(fileId, type, userId, applicantId, adviserId, id, name, officialId, relativePath);
            if (add == -1) {
                return new Response<String>(1, "文件或文件夹不存在", null);
            }
            if (add > 0) {
                return new Response<String>(0, "修改成功", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Response<String>(1, "修改失败", null);
        }
        return new Response<String>(0, "修改成功", null);
    }


    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    @ResponseBody
    public Response<String> delete(
            @RequestParam(value = "id") Integer id,
            @RequestParam(value = "fileId") String fileId,
            HttpServletRequest request, HttpServletResponse response) {
        try {
            super.setPostHeader(response);
            int a = cloudDiskService.deleteById(id, fileId);
            if (a == -2) {
                return new Response<String>(1, "请清空文件夹后操作", null);
            }
            if (a < 0) {
                return new Response<String>(1, "删除失败", null);
            }
            return new Response<String>(0, "删除成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response<String>(1, "上传失败", null);
        }
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public ListResponse<List<CloudDiskFile>> list(
            @RequestParam(value = "id", required = false) Integer id,
            @RequestParam(value = "parentFileId", required = false) String parentFileId,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "applicantId", required = false) Integer applicantId,
            @RequestParam(value = "userId", required = false) Integer userId,
            @RequestParam(value = "pageNum") int pageNum, @RequestParam(value = "pageSize") int pageSize,
            HttpServletRequest request, HttpServletResponse response) {
        try {
            super.setPostHeader(response);
            int total = cloudDiskService.count(id, parentFileId, name, applicantId, userId);
            List<CloudDiskFile> cloudDiskFileList =  cloudDiskService.list(id, parentFileId, name, applicantId, userId, pageNum, pageSize);
            return new ListResponse<List<CloudDiskFile>>(true, pageSize, total, cloudDiskFileList, "");
        } catch (Exception e) {
            e.printStackTrace();
            return new ListResponse<List<CloudDiskFile>>(false, pageSize, 0, null, e.getMessage());
        }
    }

    @RequestMapping(value = "/getUrl", method = RequestMethod.GET)
    @ResponseBody
    public Response<String> getUrl(
            @RequestParam(value = "userCode", required = false) String userCode,
            @RequestParam(value = "parentFileId") String parentFileId,
            HttpServletRequest request, HttpServletResponse response) {
        try {
            super.setPostHeader(response);
            String shareUrl = cloudDiskService.getShareUrl(userCode, parentFileId);
            return new Response<String>(0, "获取成功", shareUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response<String>(1, "获取失败", null);
        }
    }


    @RequestMapping(value = "/getFileStructure", method = RequestMethod.GET)
    @ResponseBody
    public Response<String> getFileStructure(@RequestParam(value = "parentFileStructures", required = false) String parentFileStructures,
                                             @RequestParam(value = "folderName", required = false) String folderName,
                                             @RequestParam(value = "synchronizeName", required = false) String synchronizeName,
                                             HttpServletRequest request, HttpServletResponse response) {
        try {
            AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
            Integer adviserId = adminUserLoginInfo.getAdviserId();
            Integer officialId = adminUserLoginInfo.getOfficialId();
            Map<String, Integer> addCountMap = new HashMap<>();
            Map<String, String> belongFolderMap = new HashMap<>();
            cloudDiskService.getFileStructure(parentFileStructures, adviserId, officialId, belongFolderMap, addCountMap, folderName, null, synchronizeName);
            return new Response<String>(0, "获取成功", "v");
        } catch (Exception e) {
            e.printStackTrace();
            return new Response<String>(1, "获取失败", null);
        }
    }

    @RequestMapping(value = "/initializationFolder", method = RequestMethod.GET)
    @ResponseBody
    public Response<List<CloudDiskFile>> initializationFloder(
            @RequestParam(value = "userId") Integer userId,
            @RequestParam(value = "applicantId", required = false) Integer applicantId,
            HttpServletRequest request, HttpServletResponse response) {
        super.setPostHeader(response);
        AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
        Integer adviserId = adminUserLoginInfo.getAdviserId();
        Integer officialId = adminUserLoginInfo.getOfficialId();
        try {
            List<CloudDiskFile> cloudDiskFileList = cloudDiskService.initializationFolder(userId, applicantId, adviserId, officialId);
            List<CloudDiskFile> collect1 = cloudDiskFileList.stream().filter(p -> "root".equalsIgnoreCase(p.getParentFileId())).collect(Collectors.toList());
//            List<CloudDiskFile> collect = collect1.stream().sorted(Comparator.comparing(p -> "文案资料".equalsIgnoreCase(p.getName()) ? 0 : 2)).collect(Collectors.toList());
//            List<CloudDiskFile> collectT = collect.stream().sorted(Comparator.comparing(p -> "顾问资料".equalsIgnoreCase(p.getName()) ? 0 : 2)).collect(Collectors.toList());
            if (cloudDiskFileList.isEmpty()) {
                return new Response<List<CloudDiskFile>>(1, "初始化失败", null);
            } else {
                return new Response<List<CloudDiskFile>>(0, "已初始化", collect1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Response<List<CloudDiskFile>>(1, "初始化失败", null);
        }
    }

    @RequestMapping(value = "/getDownLink", method = RequestMethod.GET)
    @ResponseBody
    public Response<String> getDownLink(
            @RequestParam(value = "id") Integer id,
            @RequestParam(value = "fileId", required = false) String fileId,
            HttpServletRequest request, HttpServletResponse response) {
        super.setPostHeader(response);
        try {
            String downLink = cloudDiskService.getDownLink(id, fileId);
            String[] split = downLink.split("&&&");
            if (split != null && split.length > 0) {
                downLink = split[0];
            }
            return new Response<String>(0, "获取成功", downLink);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response<String>(1, "获取失败", e.getMessage());
        }
    }

    @RequestMapping(value = "/synchronizeUserCloud", method = RequestMethod.GET)
    @ResponseBody
    public Response<String> synchronizeUserCloud(
            HttpServletRequest request, HttpServletResponse response) {
        try {
            cloudDiskService.synchronizeUserCloud();
            return new Response<String>(0, "获取成功", "");
        } catch (Exception e) {
            e.printStackTrace();
            return new Response<String>(1, "获取失败", e.getMessage());
        }
    }

    @RequestMapping(value = "/addUserCloud", method = RequestMethod.POST)
    @ResponseBody
    public Response<UserCloud> addUserCloud(@RequestParam(value = "userName", required = false) String userName,
                                         @RequestParam(value = "email", required = false) String email,
                                         @RequestParam(value = "role", required = false) String role,
                                            @RequestParam(value = "phone", required = false) String phone,
            HttpServletRequest request, HttpServletResponse response) {
        try {
            UserCloud userCloud = cloudDiskService.addUserCloud(userName, email, role, phone);
            if (userCloud == null) {
                return new Response<UserCloud>(1, "用户已存在", null);
            }
            return new Response<UserCloud>(0, "获取成功", userCloud);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response<UserCloud>(1, "获取失败", null);
        }
    }

    @RequestMapping(value = "/listUserCloud", method = RequestMethod.GET)
    @ResponseBody
    public ListResponse<List<UserCloud>> listUserCloud(@RequestParam(value = "userName", required = false) String userName,
                                                   @RequestParam(value = "email", required = false) String email,
                                                   @RequestParam(value = "pageNum") int pageNum, @RequestParam(value = "pageSize") int pageSize,
            HttpServletRequest request, HttpServletResponse response) {
        super.setPostHeader(response);
        try {
            int total = cloudDiskService.countUserCloud(userName, email);
            List<UserCloud> userClouds = cloudDiskService.listUserCloud(userName, email, pageNum, pageSize);
            return new ListResponse<List<UserCloud>>(true, pageSize, total, userClouds, "");
        } catch (Exception e) {
            e.printStackTrace();
            return new ListResponse<List<UserCloud>>(false, pageSize, 0, null, e.getMessage());
        }
    }

    @RequestMapping(value = "/deleteUserCloud", method = RequestMethod.GET)
    @ResponseBody
    public Response<String> deleteUserCloud(@RequestParam(value = "id") Integer id,
                                            HttpServletRequest request, HttpServletResponse response) {
        super.setPostHeader(response);
        try {
            cloudDiskService.deleteUserCloud(id);
            return new Response<String>(0, "删除成功", "");
        } catch (Exception e) {
            e.printStackTrace();
            return new Response<String>(1, "删除失败", e.getMessage());
        }
    }

    @RequestMapping(value = "/fileTransformation", method = RequestMethod.GET)
    @ResponseBody
    public void fileTransformation(@RequestParam(value = "fileId", required = false) String fileId, HttpServletResponse response) {
        HttpURLConnection connection = null;
        try {
            String url = cloudDiskService.getDownLink(null, fileId);
            String[] split = url.split("&&&");
            String fileName = split[1];
            url = split[0];
            // 1. 创建连接
            URL fileUrl = new URL(url);
            connection = (HttpURLConnection) fileUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);

            // 2. 获取文件信息
            String contentType = getContentType(fileName);
            boolean isPreviewable = isPreviewable(fileName);

            // 3. 设置响应头
            response.setContentType(contentType);

            // 4. 设置 Content-Disposition（预览 vs 下载）
            String encodedFileName = URLEncoder.encode(fileName, "UTF-8")
                    .replaceAll("\\+", "%20");

            if (isPreviewable) {
                // 可预览的文件类型：使用 inline，让浏览器直接显示
                response.setHeader("Content-Disposition",
                        "inline; filename*=UTF-8''" + encodedFileName);
            } else {
                // 不可预览的文件类型：使用 attachment，强制下载
                response.setHeader("Content-Disposition",
                        "attachment; filename*=UTF-8''" + encodedFileName);
            }

            // 5. 设置内容长度（可选，提升传输效率）
            long contentLength = connection.getContentLengthLong();
            if (contentLength > 0) {
                response.setContentLengthLong(contentLength);
            }

            // 6. 设置缓存控制（可选）
            response.setHeader("Cache-Control", "max-age=3600");

            // 7. 转发文件流
            try (InputStream inputStream = connection.getInputStream();
                 OutputStream outputStream = response.getOutputStream()) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
            }

        } catch (Exception e) {
            log.error("文件代理失败, fileId: {}", fileId, e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            try {
                response.getWriter().write("文件获取失败: " + e.getMessage());
            } catch (IOException ioException) {
                log.error("写入错误响应失败", ioException);
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * 根据文件名获取 MIME 类型
     */
    private String getContentType(String fileName) {
        if (fileName == null) {
            return "application/octet-stream";
        }

        String lowerFileName = fileName.toLowerCase();

        // 图片类型
        if (lowerFileName.endsWith(".jpg") || lowerFileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerFileName.endsWith(".png")) {
            return "image/png";
        } else if (lowerFileName.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerFileName.endsWith(".bmp")) {
            return "image/bmp";
        } else if (lowerFileName.endsWith(".webp")) {
            return "image/webp";
        } else if (lowerFileName.endsWith(".svg")) {
            return "image/svg+xml";
        } else if (lowerFileName.endsWith(".ico")) {
            return "image/x-icon";
        }

        // 文档类型
        else if (lowerFileName.endsWith(".pdf")) {
            return "application/pdf";
        } else if (lowerFileName.endsWith(".doc")) {
            return "application/msword";
        } else if (lowerFileName.endsWith(".docx")) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        } else if (lowerFileName.endsWith(".xls")) {
            return "application/vnd.ms-excel";
        } else if (lowerFileName.endsWith(".xlsx")) {
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        } else if (lowerFileName.endsWith(".ppt")) {
            return "application/vnd.ms-powerpoint";
        } else if (lowerFileName.endsWith(".pptx")) {
            return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        }

        // 文本类型
        else if (lowerFileName.endsWith(".txt")) {
            return "text/plain";
        } else if (lowerFileName.endsWith(".html") || lowerFileName.endsWith(".htm")) {
            return "text/html";
        } else if (lowerFileName.endsWith(".css")) {
            return "text/css";
        } else if (lowerFileName.endsWith(".js")) {
            return "application/javascript";
        } else if (lowerFileName.endsWith(".json")) {
            return "application/json";
        } else if (lowerFileName.endsWith(".xml")) {
            return "application/xml";
        }

        // 视频类型
        else if (lowerFileName.endsWith(".mp4")) {
            return "video/mp4";
        } else if (lowerFileName.endsWith(".avi")) {
            return "video/x-msvideo";
        } else if (lowerFileName.endsWith(".mov")) {
            return "video/quicktime";
        } else if (lowerFileName.endsWith(".wmv")) {
            return "video/x-ms-wmv";
        } else if (lowerFileName.endsWith(".flv")) {
            return "video/x-flv";
        } else if (lowerFileName.endsWith(".mkv")) {
            return "video/x-matroska";
        }

        // 音频类型
        else if (lowerFileName.endsWith(".mp3")) {
            return "audio/mpeg";
        } else if (lowerFileName.endsWith(".wav")) {
            return "audio/wav";
        } else if (lowerFileName.endsWith(".flac")) {
            return "audio/flac";
        } else if (lowerFileName.endsWith(".aac")) {
            return "audio/aac";
        }

        // 默认二进制流
        else {
            return "application/octet-stream";
        }
    }


    /**
     * 判断文件是否可以在浏览器中直接预览
     * 注意：Word、Excel、PPT 需要额外转换才能预览，直接返回浏览器会下载
     */
    private boolean isPreviewable(String fileName) {
        if (fileName == null) {
            return false;
        }

        String lowerFileName = fileName.toLowerCase();

        // 浏览器原生支持预览的类型
        return lowerFileName.endsWith(".jpg") ||
                lowerFileName.endsWith(".jpeg") ||
                lowerFileName.endsWith(".png") ||
                lowerFileName.endsWith(".gif") ||
                lowerFileName.endsWith(".bmp") ||
                lowerFileName.endsWith(".webp") ||
                lowerFileName.endsWith(".svg") ||
                lowerFileName.endsWith(".pdf") ||
                lowerFileName.endsWith(".txt") ||
                lowerFileName.endsWith(".html") ||
                lowerFileName.endsWith(".htm") ||
                lowerFileName.endsWith(".mp4") ||
                lowerFileName.endsWith(".mp3") ||
                lowerFileName.endsWith(".wav");

        // 注意：.doc, .docx, .xls, .xlsx, .ppt, .pptx 浏览器不支持直接预览
        // 这些需要转换为 PDF 或使用 Office Online 才能预览
    }
}
