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
import org.zhinanzhen.b.service.pojo.FileMaraAnnotationDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.dao.UserDAO;
import org.zhinanzhen.tb.dao.pojo.UserDO;
import org.zhinanzhen.tb.service.ServiceException;

import com.ikasoa.core.utils.StringUtil;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/fileMaraAnnotation")
public class FileMaraAnnotationController extends BaseController {

    @Resource
    private FileMaraAnnotationService fileMaraAnnotationService;

    @Resource
    private UserDAO userDao;

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public Response<Integer> add(@RequestParam(value = "serviceOrderId") int serviceOrderId,
                                @RequestParam(value = "userId") int userId,
                                @RequestParam(value = "officialId", required = false, defaultValue = "0") int officialId,
                                @RequestParam(value = "cloudDiskFileId", required = false, defaultValue = "0") int cloudDiskFileId,
                                @RequestParam(value = "isAnnotation", required = false, defaultValue = "0") String isAnnotation,
                                @RequestParam(value = "maraMark", required = false) String maraMark,
                                HttpServletRequest request, HttpServletResponse response) {
        try {
            super.setPostHeader(response);
            FileMaraAnnotationDTO dto = new FileMaraAnnotationDTO();
            dto.setServiceOrderId(serviceOrderId);
            dto.setUserId(userId);
            dto.setOfficialId(officialId);
            dto.setCloudDiskFileId(cloudDiskFileId);
            dto.setAnnotation("1".equals(isAnnotation));
            dto.setCheck(false);
            if (fileMaraAnnotationService.add(dto) > 0) {
                if (StringUtil.isNotEmpty(maraMark)) {
                    userDao.updateMaraMark(userId, maraMark);
                }
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
                                   @RequestParam(value = "cloudDiskFileId", required = false) Integer cloudDiskFileId,
                                   @RequestParam(value = "isAnnotation", required = false) String isAnnotation,
                                   @RequestParam(value = "isCheck", required = false) String isCheck,
                                   @RequestParam(value = "maraMark", required = false) String maraMark,
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
            if (cloudDiskFileId != null) {
                dto.setCloudDiskFileId(cloudDiskFileId);
            }
            if (isAnnotation != null) {
                dto.setAnnotation("1".equals(isAnnotation));
            }
            if (isCheck != null) {
                dto.setCheck("1".equals(isCheck));
            }
            int result = fileMaraAnnotationService.update(dto);
            if (result > 0 && StringUtil.isNotEmpty(maraMark) && userId != null) {
                userDao.updateMaraMark(userId, maraMark);
            }
            return new Response<Integer>(0, result);
        } catch (ServiceException e) {
            return new Response<Integer>(e.getCode(), e.getMessage(), 0);
        }
    }

    @RequestMapping(value = "/get", method = RequestMethod.GET)
    @ResponseBody
    public Response<FileMaraAnnotationDTO> get(@RequestParam(value = "id") int id,
                                               HttpServletResponse response) {
        try {
            super.setGetHeader(response);
            FileMaraAnnotationDTO dto = fileMaraAnnotationService.getById(id);
            if (dto != null) {
                return new Response<FileMaraAnnotationDTO>(0, dto);
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
            if (dtoList != null) {
                for (FileMaraAnnotationDTO dto : dtoList) {
                    if (dto.getUserId() > 0) {
                        UserDO userDo = userDao.getUserById(dto.getUserId());
                        if (userDo != null) {
                            dto.setMaraMark(userDo.getMaraMark());
                        }
                    }
                }
            }
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

}