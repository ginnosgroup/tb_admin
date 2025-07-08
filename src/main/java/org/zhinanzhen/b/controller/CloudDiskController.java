package org.zhinanzhen.b.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.zhinanzhen.b.service.CloudDiskService;
import org.zhinanzhen.b.service.pojo.CloudDiskFile;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.ListResponse;
import org.zhinanzhen.tb.controller.Response;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/cloudDisk")
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
            HttpServletRequest request, HttpServletResponse response) {
        super.setPostHeader(response);
        AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
        Integer adviserId = adminUserLoginInfo.getAdviserId();
        Integer officialId = adminUserLoginInfo.getOfficialId();
        try {
            int add = cloudDiskService.addAndUpdate(file, type, applicantId, userId, parentFileId, adviserId, id, folderName, officialId);
            if (add == -1) {
                return new Response<String>(-2, "文件或文件夹已存在", null);
            }
            if (add > 0) {
                return new Response<String>(0, "上传成功", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Response<String>(-1, "上传失败", null);
        }
        return new Response<String>(0, "上传成功", null);
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseBody
    public Response<String> update(
            @RequestParam(value = "id", required = false) Integer id,
            @RequestParam(value = "fileId", required = false) String fileId,
            @RequestParam(value = "type") String type,
            @RequestParam(value = "applicantId", required = false) Integer applicantId,
            @RequestParam(value = "applicantId", required = false) Integer userId,
            @RequestParam(value = "name", required = false) String name,
            HttpServletRequest request, HttpServletResponse response) {
        super.setPostHeader(response);
        AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
        Integer adviserId = adminUserLoginInfo.getAdviserId();
        Integer officialId = adminUserLoginInfo.getOfficialId();
        try {
            int add = cloudDiskService.update(fileId, type, userId, applicantId, adviserId, id, name, officialId);
            if (add == -1) {
                return new Response<String>(-1, "文件或文件夹不存在", null);
            }
            if (add > 0) {
                return new Response<String>(0, "修改成功", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Response<String>(-1, "修改失败", null);
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
            if (a < 0) {
                return new Response<String>(-1, "删除失败", null);
            }
            return new Response<String>(0, "上传成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response<String>(-1, "上传失败", null);
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
    public Response<String> getFileStructure(@RequestParam(value = "parentFileStructures") String parentFileStructures,
                                             HttpServletRequest request, HttpServletResponse response) {
        try {
            AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
            Integer adviserId = adminUserLoginInfo.getAdviserId();
            Integer officialId = adminUserLoginInfo.getOfficialId();
            cloudDiskService.getFileStructure(parentFileStructures, adviserId, officialId);
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
                return new Response<List<CloudDiskFile>>(-1, "初始化失败", null);
            } else {
                return new Response<List<CloudDiskFile>>(0, "已初始化", collect1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Response<List<CloudDiskFile>>(-1, "初始化失败", null);
        }
    }


}
