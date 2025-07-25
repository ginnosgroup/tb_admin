package org.zhinanzhen.b.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.zhinanzhen.b.dao.pojo.OfficialDO;
import org.zhinanzhen.b.service.CloudDiskService;
import org.zhinanzhen.b.service.ExternalInterfaceService;
import org.zhinanzhen.b.service.pojo.CloudDiskFile;
import org.zhinanzhen.b.service.pojo.ServiceDTO;
import org.zhinanzhen.b.service.pojo.UserDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.ListResponse;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.dao.pojo.AdminUserDO;
import org.zhinanzhen.tb.dao.pojo.AdviserDO;
import org.zhinanzhen.tb.dao.pojo.UserDO;
import org.zhinanzhen.tb.service.ServiceException;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/externalInterface")
public class ExternalInterfaceController extends BaseController {

    @Autowired
    private ExternalInterfaceService externalInterfaceService;


    @RequestMapping(value = "/addCloudDiskFile", method = RequestMethod.POST)
    @ResponseBody
    public Response<Integer> addCloudDiskFile(@RequestParam(value = "applicantId", required = false) Integer applicantId,
                                              @RequestParam(value = "adviserId", required = false) Integer adviserId,
                                              @RequestParam(value = "name", required = false) String name, @RequestParam(value = "type") String type,
                                              @RequestParam(value = "url") String url, @RequestParam(value = "parentFileId") String parentFileId,
                                              @RequestParam(value = "domainId") String domainId, @RequestParam(value = "driveId") String driveId,
                                              @RequestParam(value = "fileId") String fileId, @RequestParam(value = "officialId") Integer officialId,
                                              @RequestParam(value = "userId") Integer userId, @RequestParam(value = "operator") String operator,
                                              @RequestParam(value = "relativePath") String relativePath, @RequestParam(value = "fileSize") String fileSize,
                                              @RequestParam(value = "downloadUrl") String downloadUrl, HttpServletResponse response) {
        try {
            super.setGetHeader(response);
            Integer id = externalInterfaceService.addCloudDiskFile(applicantId, adviserId, name, type, url, parentFileId, domainId, driveId, fileId, officialId, userId, operator, relativePath, fileSize, downloadUrl);
            return new Response<Integer>(0, "添加成功", id);
        } catch (Exception e) {
            return new Response<Integer>(1, "添加失败:" + e.getMessage(), 1);
        }
    }

    @RequestMapping(value = "/getCloudDiskFileById", method = RequestMethod.GET)
    @ResponseBody
    public Response<CloudDiskFile> getCloudDiskFileById(@RequestParam(value = "id", required = false) Integer id,
                                              @RequestParam(value = "adviserId", required = false) Integer adviserId,
                                              @RequestParam(value = "parentFileId") String parentFileId,
                                              @RequestParam(value = "fileId") String fileId, @RequestParam(value = "folderName") String folderName,
                                              HttpServletResponse response) {
        try {
            super.setGetHeader(response);
            CloudDiskFile cloudDiskFile = externalInterfaceService.getCloudDiskFileById(id, adviserId, parentFileId, fileId, folderName);
            return new Response<CloudDiskFile>(0, "获取成功", cloudDiskFile);
        } catch (Exception e) {
            return new Response<CloudDiskFile>(1, "获取失败:" + e.getMessage(), null);
        }
    }

    @RequestMapping(value = "/updateCloudDiskFile", method = RequestMethod.GET)
    @ResponseBody
    public Response<Integer> updateCloudDiskFile(@RequestParam(value = "applicantId", required = false) Integer applicantId,
                                                 @RequestParam(value = "adviserId", required = false) Integer adviserId,
                                                 @RequestParam(value = "name", required = false) String name, @RequestParam(value = "type") String type,
                                                 @RequestParam(value = "url") String url, @RequestParam(value = "parentFileId") String parentFileId,
                                                 @RequestParam(value = "domainId") String domainId, @RequestParam(value = "driveId") String driveId,
                                                 @RequestParam(value = "fileId") String fileId, @RequestParam(value = "officialId") Integer officialId,
                                                 @RequestParam(value = "userId") Integer userId, @RequestParam(value = "operator") String operator,
                                                 @RequestParam(value = "relativePath") String relativePath, @RequestParam(value = "fileSize") String fileSize,
                                                 @RequestParam(value = "downloadUrl") String downloadUrl, HttpServletResponse response) {
        try {
            super.setGetHeader(response);
            Integer id = externalInterfaceService.updateCloudDiskFile(applicantId, adviserId, name, type, url, parentFileId, domainId, driveId, fileId, officialId, userId, operator, relativePath, fileSize, downloadUrl);
            return new Response<Integer>(0, "更新成功", id);
        } catch (Exception e) {
            return new Response<Integer>(1, "更新失败:" + e.getMessage(), 1);
        }
    }

    @RequestMapping(value = "/listCloudDiskFile", method = RequestMethod.GET)
    @ResponseBody
    public ListResponse<List<CloudDiskFile>> listCloudDiskFile(@RequestParam(value = "parentFileId", required = false) String parentFileId, @RequestParam(value = "id", required = false) Integer id,
                                                 @RequestParam(value = "name", required = false) String name, @RequestParam(value = "applicantId") Integer applicantId,
                                                 @RequestParam(value = "userId") Integer userId, HttpServletResponse response) {
        try {
            super.setGetHeader(response);
            List<CloudDiskFile> cloudDiskFileList = externalInterfaceService.listCloudDiskFile(parentFileId, id, name, applicantId, userId);
            return new ListResponse<List<CloudDiskFile>>(true, cloudDiskFileList.size(), cloudDiskFileList.size(), cloudDiskFileList, "获取成功");
        } catch (Exception e) {
            return new ListResponse<List<CloudDiskFile>>(false, 0, 0, null, e.getMessage());
        }
    }

    @RequestMapping(value = "/getUserByName", method = RequestMethod.GET)
    @ResponseBody
    public Response<UserDO> getUserByName(@RequestParam(value = "name", required = false) String name, HttpServletResponse response) {
        try {
            super.setGetHeader(response);
            UserDO userDO = externalInterfaceService.getUserByName(name);
            return new Response<UserDO>(0, "获取成功", userDO);
        } catch (Exception e) {
            return new Response<UserDO>(1, "获取失败:" + e.getMessage(), null);
        }
    }

    @RequestMapping(value = "/getAdviserById", method = RequestMethod.GET)
    @ResponseBody
    public Response<AdviserDO> getAdviserById(@RequestParam(value = "id", required = false) Integer id, HttpServletResponse response) {
        try {
            super.setGetHeader(response);
            AdviserDO adviserDO = externalInterfaceService.getAdviserById(id);
            return new Response<AdviserDO>(0, "获取成功", adviserDO);
        } catch (Exception e) {
            return new Response<AdviserDO>(1, "获取失败:" + e.getMessage(), null);
        }
    }

    @RequestMapping(value = "/getOfficialById", method = RequestMethod.GET)
    @ResponseBody
    public Response<OfficialDO> getOfficialById(@RequestParam(value = "id", required = false) Integer id, HttpServletResponse response) {
        try {
            super.setGetHeader(response);
            OfficialDO officialDO = externalInterfaceService.getOfficialById(id);
            return new Response<OfficialDO>(0, "获取成功", officialDO);
        } catch (Exception e) {
            return new Response<OfficialDO>(1, "获取失败:" + e.getMessage(), null);
        }
    }

    @RequestMapping(value = "/getAdminuserByUserName", method = RequestMethod.GET)
    @ResponseBody
    public Response<AdminUserDO> getAdminuserByUserName(@RequestParam(value = "username", required = false) String username, HttpServletResponse response) {
        try {
            super.setGetHeader(response);
            AdminUserDO adminUserDO = externalInterfaceService.getAdminuserByUserName(username);
            return new Response<AdminUserDO>(0, "获取成功", adminUserDO);
        } catch (Exception e) {
            return new Response<AdminUserDO>(1, "获取失败:" + e.getMessage(), null);
        }
    }

    @RequestMapping(value = "/getCloudDisk", method = RequestMethod.GET)
    @ResponseBody
    public Response<CloudDiskFile> getCloudDisk(@RequestParam(value = "relativePath", required = false) String relativePath, HttpServletResponse response) {
        try {
            super.setGetHeader(response);
            CloudDiskFile cloudDiskFile = externalInterfaceService.getCloudDisk(relativePath);
            return new Response<CloudDiskFile>(0, "获取成功", cloudDiskFile);
        } catch (Exception e) {
            return new Response<CloudDiskFile>(1, "获取失败:" + e.getMessage(), null);
        }
    }


}
