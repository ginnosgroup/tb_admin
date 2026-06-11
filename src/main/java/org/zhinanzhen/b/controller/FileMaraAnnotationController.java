package org.zhinanzhen.b.controller;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zhinanzhen.b.service.FileMaraAnnotationService;
import org.zhinanzhen.b.service.OrderMaraAnnotationService;
import org.zhinanzhen.b.service.pojo.FileMaraAnnotationDTO;
import org.zhinanzhen.b.service.pojo.SelectOfficialCheckDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;


@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/fileMaraAnnotation")
public class FileMaraAnnotationController extends BaseController {

    @Resource
    private FileMaraAnnotationService fileMaraAnnotationService;

    @Resource
    private OrderMaraAnnotationService orderMaraAnnotationService;

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public Response<Integer> add(@RequestParam(value = "serviceOrderId") int serviceOrderId,
                                @RequestParam(value = "userId") int userId,
                                @RequestParam(value = "officialId", required = false, defaultValue = "0") int officialId,
                                @RequestParam(value = "maraId", required = false, defaultValue = "0") int maraId,
                                @RequestParam(value = "cloudDiskFileId", required = false) String cloudDiskFileId,
                                @RequestParam(value = "isAnnotation", required = false, defaultValue = "0") String isAnnotation,
                                @RequestParam(value = "annotationMark", required = false) String annotationMark,
                                HttpServletRequest request, HttpServletResponse response) {
        try {
            super.setPostHeader(response);
            FileMaraAnnotationDTO dto = new FileMaraAnnotationDTO();
            dto.setServiceOrderId(serviceOrderId);
            dto.setUserId(userId);
            dto.setOfficialId(officialId);
            dto.setMaraId(maraId);
            dto.setCloudDiskFileId(cloudDiskFileId);
            dto.setIsAnnotation("1".equals(isAnnotation));
            dto.setAnnotationMark(annotationMark);
            if (fileMaraAnnotationService.add(dto) > 0) {
                return new Response<Integer>(0, dto.getId());
            } else {
                return new Response<Integer>(1, "创建失败.", 0);
            }
        } catch (ServiceException e) {
            return new Response<Integer>(e.getCode(), e.getMessage(), 0);
        }
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseBody
    public Response<Integer> update(@RequestParam(value = "id") int id,
                                   @RequestParam(value = "serviceOrderId", required = false) Integer serviceOrderId,
                                   @RequestParam(value = "userId", required = false) Integer userId,
                                   @RequestParam(value = "officialId", required = false) Integer officialId,
                                   @RequestParam(value = "maraId", required = false) Integer maraId,
                                   @RequestParam(value = "cloudDiskFileId", required = false) String cloudDiskFileId,
                                   @RequestParam(value = "isAnnotation", required = false) String isAnnotation,
                                   @RequestParam(value = "annotationMark", required = false) String annotationMark,
                                   HttpServletRequest request, HttpServletResponse response) {
        try {
            super.setPostHeader(response);
            FileMaraAnnotationDTO dto = new FileMaraAnnotationDTO();
            dto.setId(id);
            if (serviceOrderId != null) {
                dto.setServiceOrderId(serviceOrderId);
            }
            if (userId != null) {
                dto.setUserId(userId);
            }
            if (officialId != null) {
                dto.setOfficialId(officialId);
            }
            if (maraId != null) {
                dto.setMaraId(maraId);
            }
            if (cloudDiskFileId != null) {
                dto.setCloudDiskFileId(cloudDiskFileId);
            }
            if (isAnnotation != null) {
                dto.setIsAnnotation("1".equals(isAnnotation));
            }
            if (annotationMark != null) {
                dto.setAnnotationMark(annotationMark);
            }
            int result = fileMaraAnnotationService.update(dto);
            return new Response<Integer>(0, result);
        } catch (ServiceException e) {
            return new Response<Integer>(e.getCode(), e.getMessage(), 0);
        }
    }

    @RequestMapping(value = "/addMaraMark", method = RequestMethod.POST)
    @ResponseBody
    public Response<Integer> addMaraMark(@RequestParam(value = "serviceOrderId") int serviceOrderId,
                                         @RequestParam(value = "maraMark", required = false) String maraMark,
                                         HttpServletResponse response) {
        try {
            super.setPostHeader(response);
            orderMaraAnnotationService.saveMaraMarkFromServiceOrder(serviceOrderId, maraMark == null ? "" : maraMark);
            return new Response<Integer>(0, 1);
        } catch (ServiceException e) {
            return new Response<Integer>(e.getCode(), e.getMessage(), 0);
        }
    }

    @RequestMapping(value = "/updateMaraMark", method = RequestMethod.GET)
    @ResponseBody
    public Response<Integer> updateMaraMark(@RequestParam(value = "serviceOrderId") int serviceOrderId,
                                            @RequestParam(value = "maraMark", required = false) String maraMark,
                                            @RequestParam(value = "isCheck", required = false) String isCheck,
                                            @RequestParam(value = "officialId", required = false, defaultValue = "0") int officialId,
                                            HttpServletResponse response) {
        try {
            super.setPostHeader(response);
            boolean check = "1".equals(isCheck);
            if (maraMark != null) {
                String newMaraMark = maraMark;
                String oldMaraMark = orderMaraAnnotationService.getMaraMarkByServiceOrderId(serviceOrderId);
                // maraMark 有修改则重置 isCheck 为 0
                if (!newMaraMark.equals(oldMaraMark == null ? "" : oldMaraMark)) {
                    check = false;
                }
            }
            orderMaraAnnotationService.saveMaraMark(serviceOrderId, maraMark, check, officialId);
            return new Response<Integer>(0, 1);
        } catch (ServiceException e) {
            return new Response<Integer>(e.getCode(), e.getMessage(), 0);
        }
    }

    @RequestMapping(value = "/get", method = RequestMethod.GET)
    @ResponseBody
    public Response<FileMaraAnnotationDTO> get(
            @RequestParam(value = "serviceOrderId", required = false) Integer serviceOrderId,
            @RequestParam(value = "userId", required = false) Integer userId,
            @RequestParam(value = "officialId", required = false) Integer officialId,
            HttpServletResponse response) {
        try {
            super.setGetHeader(response);
            List<FileMaraAnnotationDTO> dtoList = fileMaraAnnotationService.list(serviceOrderId, userId, officialId);
            if (dtoList != null && !dtoList.isEmpty()) {
                return new Response<FileMaraAnnotationDTO>(0, dtoList.get(0));
            } else {
                return new Response<FileMaraAnnotationDTO>(1, "未找到数据.", null);
            }
        } catch (ServiceException e) {
            return new Response<FileMaraAnnotationDTO>(e.getCode(), e.getMessage(), null);
        }
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public Response<List<FileMaraAnnotationDTO>> list(
            @RequestParam(value = "serviceOrderId", required = false) Integer serviceOrderId,
            @RequestParam(value = "userId", required = false) Integer userId,
            @RequestParam(value = "officialId", required = false) Integer officialId,
            HttpServletResponse response) {
        try {
            super.setGetHeader(response);
            List<FileMaraAnnotationDTO> dtoList = fileMaraAnnotationService.list(serviceOrderId, userId, officialId);
            return new Response<List<FileMaraAnnotationDTO>>(0, dtoList);
        } catch (ServiceException e) {
            return new Response<List<FileMaraAnnotationDTO>>(e.getCode(), e.getMessage(), null);
        }
    }

    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    @ResponseBody
    public Response<Integer> delete(@RequestParam(value = "id") int id,
                                    HttpServletResponse response) {
        try {
            super.setGetHeader(response);
            return new Response<Integer>(0, fileMaraAnnotationService.deleteById(id));
        } catch (ServiceException e) {
            return new Response<Integer>(e.getCode(), e.getMessage(), 0);
        }
    }

    @RequestMapping(value = "/officialCheck", method = RequestMethod.POST)
    @ResponseBody
    public Response<Integer> officialCheck(@RequestParam(value = "serviceOrderId") int serviceOrderId,
                                           HttpServletRequest request,
                                           HttpServletResponse response) {
        try {
            super.setPostHeader(response);
            AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
            int officialId = adminUserLoginInfo.getOfficialId() != null ? adminUserLoginInfo.getOfficialId() : 0;
            int result = orderMaraAnnotationService.officialCheck(serviceOrderId, officialId);
            return new Response<Integer>(0, result);
        } catch (ServiceException e) {
            return new Response<Integer>(e.getCode(), e.getMessage(), 0);
        }
    }

    @RequestMapping(value = "/selectOfficialCheck", method = RequestMethod.GET)
    @ResponseBody
    public Response<List<SelectOfficialCheckDTO>> selectOfficialCheck(HttpServletRequest request, HttpServletResponse response) {
        try {
            super.setGetHeader(response);
            AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
            if (adminUserLoginInfo.getOfficialId() == null) {
                return new Response<List<SelectOfficialCheckDTO>>(1, "当前用户没有 officialId", null);
            }
            int officialId = adminUserLoginInfo.getOfficialId();
            List<SelectOfficialCheckDTO> resultList = orderMaraAnnotationService.selectOfficialCheck(officialId);
            if (resultList == null || resultList.isEmpty()) {
                return new Response<List<SelectOfficialCheckDTO>>(1, "未查询到 b_order_mara_annotation 数据", null);
            }
            return new Response<List<SelectOfficialCheckDTO>>(0, resultList);
        } catch (ServiceException e) {
            return new Response<List<SelectOfficialCheckDTO>>(e.getCode(), e.getMessage(), null);
        }
    }

}
