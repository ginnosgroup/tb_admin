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
import java.util.List;

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
            @RequestParam(value = "file") MultipartFile file,
            @RequestParam(value = "type") String type,
            @RequestParam(value = "applicantId") Integer applicantId,
            @RequestParam(value = "parentFileId") String parentFileId,
            HttpServletRequest request, HttpServletResponse response) {
        super.setPostHeader(response);
        AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
        Integer adviserId = adminUserLoginInfo.getAdviserId();
        try {
            int add = cloudDiskService.addAndUpdate(file, type, applicantId, parentFileId, adviserId, id);
            if (add > 0) {
                return new Response<String>(0, "上传成功", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Response<String>(-1, "上传失败", null);
        }
        return new Response<String>(0, "上传成功", null);
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
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

    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public ListResponse<List<CloudDiskFile>> list(
            @RequestParam(value = "id", required = false) Integer id,
            @RequestParam(value = "parentFileId", required = false) String parentFileId,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "pageNum") int pageNum, @RequestParam(value = "pageSize") int pageSize,
            HttpServletRequest request, HttpServletResponse response) {
        try {
            super.setPostHeader(response);
            int total = cloudDiskService.count(id, parentFileId, name);
            List<CloudDiskFile> cloudDiskFileList =  cloudDiskService.list(id, parentFileId, name, pageNum, pageSize);
            return new ListResponse<List<CloudDiskFile>>(true, pageSize, total, cloudDiskFileList, "");
        } catch (Exception e) {
            e.printStackTrace();
            return new ListResponse<List<CloudDiskFile>>(false, pageSize, 0, null, e.getMessage());
        }
    }

    @RequestMapping(value = "/getUrl", method = RequestMethod.POST)
    @ResponseBody
    public Response<String> getUrl(
            @RequestParam(value = "userCode") String userCode,
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
            cloudDiskService.getFileStructure(parentFileStructures);
            return new Response<String>(0, "获取成功", "v");
        } catch (Exception e) {
            e.printStackTrace();
            return new Response<String>(1, "获取失败", null);
        }
    }



}
