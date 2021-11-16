package org.zhinanzhen.b.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ikasoa.core.utils.StringUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.zhinanzhen.b.service.SchoolCourseService;
import org.zhinanzhen.b.service.SchoolInstitutionService;
import org.zhinanzhen.b.service.pojo.SchoolInstitutionCommentDTO;
import org.zhinanzhen.b.service.pojo.SchoolInstitutionDTO;
import org.zhinanzhen.b.service.pojo.SchoolSettingNewDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.ListResponse;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * Created with IntelliJ IDEA.
 * Date: 2021/04/02 9:58
 * Description:
 * Version: V1.0
 */
@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/schoolInstitution")
public class SchoolInstitutionController extends BaseController {

    @Resource
    private SchoolInstitutionService schoolInstitutionService;

    @Resource
    private SchoolCourseService schoolCourseService;

    @RequestMapping(value = "/upload_contract_file", method = RequestMethod.POST)
    @ResponseBody
    public Response<String> uploadContractFile(@RequestParam MultipartFile file, HttpServletRequest request,
                                               HttpServletResponse response) throws IllegalStateException, IOException {
        super.setPostHeader(response);
        return super.upload2(file, request.getSession(), "/uploads/school_contract_files/");
    }

    @RequestMapping(value = "/updateSchoolAttachments", method = RequestMethod.POST)
    @ResponseBody
    public Response<String> updateSchoolAttachments(//@RequestParam(value = "providerId") int providerId,
                                                    //@RequestParam(value = "contractFile1", required = false) String contractFile1,
                                                    //@RequestParam(value = "contractFile2", required = false) String contractFile2,
                                                    //@RequestParam(value = "contractFile3", required = false) String contractFile3,
                                                    //@RequestParam(value = "remarks", required = false) String remarks,
                                                    @RequestBody Map<String,String> param,
                                                    HttpServletRequest request, HttpServletResponse response) {
        super.setPostHeader(response);
        int providerId = StringUtil.toInt(param.get("providerId"));
        String contractFile1 = param.get("contractFile1");
        String contractFile2 = param.get("contractFile2");
        String contractFile3 = param.get("contractFile3");
        String remarks = param.get("remarks");
        try {
            if(providerId <= 0 )
                return new Response<String>(1, "provider error !", providerId + "");
            if (schoolInstitutionService.updateSchoolAttachments(providerId, contractFile1, contractFile2, contractFile3, remarks) > 0) {
                return new Response<String>(0, null, providerId + "");
            } else {
                return new Response<String>(1, "修改失败.", providerId + "");
            }
        } catch (ServiceException e) {
            return new Response<String>(e.getCode() , e.getMessage(), providerId + "");
        }
    }

    @RequestMapping(value = "/deleteSchoolAttachments", method = RequestMethod.GET)
    @ResponseBody
    public Response deleteSchoolAttachments(@RequestParam(value = "providerId")int providerId,
                                            @RequestParam(value = "isDeleteFile1", required = false, defaultValue = "false")boolean isDeleteFile1,
                                            @RequestParam(value = "isDeleteFile2", required = false, defaultValue = "false")boolean isDeleteFile2,
                                            @RequestParam(value = "isDeleteFile3", required = false, defaultValue = "false")boolean isDeleteFile3,
                                            HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        super.setGetHeader(response);
        if (schoolInstitutionService.deleteSchoolAttachments(providerId,isDeleteFile1,isDeleteFile2,isDeleteFile3) > 0)
            return new Response(0,"success");
        else
            return new Response(1,"fail");
    }

    @RequestMapping(value = "/list",method = RequestMethod.GET)
    @ResponseBody
    public ListResponse list(@RequestParam(value = "id",required = false ) Integer id, @RequestParam(value = "name" ,required =  false) String name,
                             @RequestParam(value = "type",required = false) String type,@RequestParam(value = "code",required = false) String code,
                             @RequestParam(value = "isFreeze",required = false) Boolean isFreeze,
                             @RequestParam(value = "pageNum") int pageNum, @RequestParam(value = "pageSize") int pageSize,
                             @RequestParam(value = "orderBy",required = false)String orderBy,
                             HttpServletRequest request,HttpServletResponse response){
        super.setGetHeader(response);
        if ( id != null && id > 0)
            return  new ListResponse(true , pageSize,1,schoolInstitutionService.getSchoolInstitutionById(id),"ok");
        int total =  schoolInstitutionService.count(name,type,code,isFreeze);
        return  new ListResponse(true , pageSize,total,schoolInstitutionService.listSchoolInstitutionDTO(name,type,code, isFreeze,
                pageNum,pageSize,orderBy),"ok");
    }

    @RequestMapping(value = "/get",method = RequestMethod.GET)
    @ResponseBody
    public Response<SchoolInstitutionDTO> get(@RequestParam(value = "id")Integer id){
        SchoolInstitutionDTO schoolInstitutionDTO = schoolInstitutionService.getSchoolInstitutionById(id);
        return new Response<SchoolInstitutionDTO>( 0 ,schoolInstitutionDTO);
    }

    @RequestMapping(value = "/update" ,method =  RequestMethod.POST)
    @ResponseBody
    public Response update(@RequestBody SchoolInstitutionDTO schoolInstitutionDTO ,
                           HttpServletRequest request,HttpServletResponse response) throws ServiceException {
        //TODO 需要设置权限，顾问只能查看不能修改
        super.setPostHeader(response);
        if (StringUtil.isNotEmpty(schoolInstitutionDTO.getName())){
            List<SchoolInstitutionDTO> listSchoolInstitutionDTO = schoolInstitutionService.listSchoolInstitutionDTO(schoolInstitutionDTO.getName(),
                    null,null,null,0,9999,null);
            for (SchoolInstitutionDTO si : listSchoolInstitutionDTO){
                if (si.getId() != schoolInstitutionDTO.getId())
                    return new Response(1,"名称重复!");
            }
        }
        if (schoolInstitutionService.update(schoolInstitutionDTO))
            return new Response(0,schoolInstitutionDTO);
        else
            return new Response(1,"修改失败");

    }

    @RequestMapping(value = "/add" ,method = RequestMethod.POST)
    @ResponseBody
    public Response add(@RequestBody SchoolInstitutionDTO schoolInstitutionDTO, HttpServletResponse response) throws ServiceException {
        super.setPostHeader(response); 
        List<SchoolInstitutionDTO> listSchoolInstitutionDTO = schoolInstitutionService.listSchoolInstitutionDTO(schoolInstitutionDTO.getName(),
                null, null,null ,0,9999,null);
        if (listSchoolInstitutionDTO.size() > 0 )
            return new Response(1,"学校名字已经存在");
        if (schoolInstitutionService.getSchoolInstitutionByCode(schoolInstitutionDTO.getCode()) != null)
            return new Response(1,"编码已经存在!");
        if (schoolInstitutionService.add(schoolInstitutionDTO) > 0 ){
            return  new Response(0,"成功",schoolInstitutionDTO.getId());
        }
        else
            return  new Response(1,"失败");

    }

    @RequestMapping(value = "/delete",method = RequestMethod.GET)
    @ResponseBody
    public Response delete(@RequestParam(value = "id",defaultValue = "0") int id){
        if (schoolInstitutionService.delete(id))
            return new Response(0,id + "删除成功");
        else
            return new Response(1,"删除失败");
    }

    /**
     * 更新Setting
     * Level：1 表示学校（全部）级别RATE，2表示学历级别RATE，3表示专业级别RATE
     * @param paramMap
     * @param request
     * @param response
     * @return Response
     * @throws ServiceException
     */
    @RequestMapping(value = "/updateSetting",method = RequestMethod.POST)
    @ResponseBody
    public Response updateSetting(@RequestBody Map<String,String> paramMap,
                                  HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        super.setPostHeader(response);
        SchoolSettingNewDTO schoolSettingNewDTO = schoolSettingNewOfType(paramMap);
        SchoolSettingNewDTO _schoolSettingNewDTO = schoolInstitutionService.getSchoolSettingNewById(schoolSettingNewDTO.getId());
        if ( _schoolSettingNewDTO == null)
            return new Response(1, "没有这个佣金规则 id :" + schoolSettingNewDTO.getId());
        schoolSettingNewDTO.setGmtCreate(_schoolSettingNewDTO.getGmtCreate());
        schoolSettingNewDTO.setGmtModify(_schoolSettingNewDTO.getGmtModify());
        if (schoolSettingNewDTO.getLevel() == 1){
            //_schoolSettingNewDTO = schoolInstitutionService.getByProviderIdAndLevel(schoolSettingNewDTO.getProviderId(), 1 , null, null);
            //if (_schoolSettingNewDTO != null && _schoolSettingNewDTO.getId() != schoolSettingNewDTO.getId()) //存在全部RATE时，验证id；不存在时，直接添加
            //   return new Response(1,"学校已存在全部级别的RATE,id :" + _schoolSettingNewDTO.getId(),_schoolSettingNewDTO);
            schoolSettingNewDTO.setCourseId(null);
            schoolSettingNewDTO.setCourseLevel(null);
        } else if (schoolSettingNewDTO.getLevel() == 2){
            if (StringUtil.isEmpty(schoolSettingNewDTO.getCourseLevel()))
                return new Response(1, "设置学历级别RATE,Course Level 不能为空");
            if (schoolCourseService.list(schoolSettingNewDTO.getProviderId(),null,false,schoolSettingNewDTO.getCourseLevel(),null,0,1).size() == 0)
                return new Response(1, "不存在此学历：" + schoolSettingNewDTO.getCourseLevel());
            //_schoolSettingNewDTO = schoolInstitutionService.getByProviderIdAndLevel(schoolSettingNewDTO.getProviderId(), 2 , schoolSettingNewDTO.getCourseLevel(), null);
            //if ( _schoolSettingNewDTO != null &&  _schoolSettingNewDTO.getId() != schoolSettingNewDTO.getId())//存在学历RATE时，验证id；不存在时，直接添加
            //   return new Response(1, "学校已存在这个学历级别的RATE,id : "+ _schoolSettingNewDTO.getId(),_schoolSettingNewDTO);
            //if (schoolInstitutionService.getSchoolSettingNewById(schoolSettingNewDTO.getId()) == null)
            //    return new Response(1, "没有这个佣金规则 id :" + schoolSettingNewDTO.getId());
            schoolSettingNewDTO.setCourseId(null);
        } else if (schoolSettingNewDTO.getLevel() == 3){
            if (schoolSettingNewDTO.getCourseId() == null)
                return new Response(1, "设置专业级别RATE,Course Id 不能为空");
            if (schoolCourseService.schoolCourseById(schoolSettingNewDTO.getCourseId()) == null)
                return new Response(1, "不存在此专业或者此专业被冻结：" + schoolSettingNewDTO.getCourseId());
            //if (schoolInstitutionService.getSchoolSettingNewById(schoolSettingNewDTO.getId()) == null)
            //   return new Response(1, "没有这个佣金规则 id :" + schoolSettingNewDTO.getId());
            //_schoolSettingNewDTO = schoolInstitutionService.getByProviderIdAndLevel(schoolSettingNewDTO.getProviderId(), 3 , null , schoolSettingNewDTO.getCourseId());
            //if (_schoolSettingNewDTO != null && _schoolSettingNewDTO.getId() != schoolSettingNewDTO.getId())//存在专业RATE时，验证id；不存在时，直接添加
            //    return new Response(1, "学校已存在这个专业级别的RATE,不能再修改成这个专业的RATE,id : " + _schoolSettingNewDTO.getId(),_schoolSettingNewDTO);
            schoolSettingNewDTO.setCourseLevel(null);
        }else
            return new Response(1, "level error");
        int i = schoolInstitutionService.updateSetting(schoolSettingNewDTO);
        if (i > 0)
            return new Response(0, "success");
        else if (i == -1)
            return new Response(1, "佣金规则 type 值错误:" + schoolSettingNewDTO.getType());
        return new Response(1, "fail");

    }

    private SchoolSettingNewDTO schoolSettingNewOfType(Map<String,String> paramMap){
        SchoolSettingNewDTO schoolSettingNewDTO = JSON.parseObject(JSON.toJSONString(paramMap), SchoolSettingNewDTO.class);
        if (schoolSettingNewDTO.getType() == 2){
            String fee1 = paramMap.get("fee1");
            String number1_1 = paramMap.get("number1_1");
            String number1_2 = paramMap.get("number1_2");
            String fee2 = paramMap.get("fee2");
            String number2_1 = paramMap.get("number2_1");
            String number2_2 = paramMap.get("number2_2");
            String fee3 = paramMap.get("fee3");
            String number3_1 = paramMap.get("number3_1");
            String number3_2 = paramMap.get("number3_2");
            String fee4 = paramMap.get("fee4");
            String number4_1 = paramMap.get("number4_1");
            String number4_2 = paramMap.get("number4_2");
            String parameters = schoolSettingNewDTO.getParameters();
            if (StringUtil.isNotEmpty(fee1) && StringUtil.isNotEmpty(number1_1) && StringUtil.isNotEmpty(number1_2))
                parameters = parameters + "|" + fee1 + "/" + number1_1 + "/" + number1_2;
            if (StringUtil.isNotEmpty(fee2) && StringUtil.isNotEmpty(number2_1) && StringUtil.isNotEmpty(number2_2))
                parameters = parameters + "|" + fee2 + "/" + number2_1 + "/" + number2_2;
            if (StringUtil.isNotEmpty(fee3) && StringUtil.isNotEmpty(number3_1) && StringUtil.isNotEmpty(number3_2))
                parameters = parameters + "|" + fee3 + "/" + number3_1 + "/" + number3_2;
            if (StringUtil.isNotEmpty(fee4) && StringUtil.isNotEmpty(number4_1) && StringUtil.isNotEmpty(number4_2))
                parameters = parameters + "|" + fee4 + "/" + number4_1 + "/" + number4_2;
            schoolSettingNewDTO.setParameters(parameters);
        }
        if (schoolSettingNewDTO.getType() == 4){
            String parameters = "";
            String proportion1 = paramMap.get("proportion1");
            String proportion2 = paramMap.get("proportion2");
            String proportion3 = paramMap.get("proportion3");
            String proportion4 = paramMap.get("proportion4");
            String number1 = paramMap.get("number1");
            String number2 = paramMap.get("number2");
            String number3 = paramMap.get("number3");
            String number4 = paramMap.get("number4");
            if (StringUtil.isNotEmpty(proportion1) && StringUtil.isNotEmpty(number1))
                parameters = parameters + "|" + proportion1 + "/" + number1;
            if (StringUtil.isNotEmpty(proportion2) && StringUtil.isNotEmpty(number2))
                parameters = parameters + "|" + proportion2 + "/" + number2;
            if (StringUtil.isNotEmpty(proportion3) && StringUtil.isNotEmpty(number3))
                parameters = parameters + "|" + proportion3 + "/" + number3;
            if (StringUtil.isNotEmpty(proportion4) && StringUtil.isNotEmpty(number4))
                parameters = parameters + "|" + proportion4 + "/" + number4;
            schoolSettingNewDTO.setParameters(parameters);
        }
        return schoolSettingNewDTO;
    }


    // 固定比例-无额外补贴
    @RequestMapping(value = "/addSetting1",method = RequestMethod.POST)
    @ResponseBody
    public Response addSetting1(@RequestBody SchoolSettingNewDTO schoolSettingNewDTO,
                                HttpServletRequest request, HttpServletResponse response) {
        super.setPostHeader(response);
        if (schoolSettingNewDTO.getType() != 1)
            return new Response(1,"RATE类型值错误：" + schoolSettingNewDTO.getType());
        Response rs;
        if ((  rs = lookschoolSettingLevel(schoolSettingNewDTO)) != null)
            return rs;
        return new Response(1, "fail");
    }

    // 固定比例-每人补贴
    @RequestMapping(value = "/addSetting2",method = RequestMethod.POST)
    @ResponseBody
    public Response addSetting2(//@RequestBody SchoolSettingNewDTO schoolSettingNewDTO,
                                //@RequestParam(value = "fee1", required = false) String fee1,
                                //@RequestParam(value = "number1_1", required = false) String number1_1,
                                //@RequestParam(value = "number1_2", required = false) String number1_2,
                                //@RequestParam(value = "fee2", required = false) String fee2,
                                //@RequestParam(value = "number2_1", required = false) String number2_1,
                                //@RequestParam(value = "number2_2", required = false) String number2_2,
                                //@RequestParam(value = "fee3", required = false) String fee3,
                                //@RequestParam(value = "number3_1", required = false) String number3_1,
                                //@RequestParam(value = "number3_2", required = false) String number3_2,
                                //@RequestParam(value = "fee4", required = false) String fee4,
                                //@RequestParam(value = "number4_1", required = false) String number4_1,
                                //@RequestParam(value = "number4_2", required = false) String number4_2,
                                @RequestBody Map<String, String> paramMap,
                                HttpServletRequest request, HttpServletResponse response) {
        super.setPostHeader(response);
        SchoolSettingNewDTO schoolSettingNewDTO = JSON.parseObject(JSON.toJSONString(paramMap), SchoolSettingNewDTO.class);
        if (schoolSettingNewDTO.getType() != 2)
            return new Response(1,"RATE类型值错误：" + schoolSettingNewDTO.getType());
        String fee1 = paramMap.get("fee1");
        String number1_1 = paramMap.get("number1_1");
        String number1_2 = paramMap.get("number1_2");
        String fee2 = paramMap.get("fee2");
        String number2_1 = paramMap.get("number2_1");
        String number2_2 = paramMap.get("number2_2");
        String fee3 = paramMap.get("fee3");
        String number3_1 = paramMap.get("number3_1");
        String number3_2 = paramMap.get("number3_2");
        String fee4 = paramMap.get("fee4");
        String number4_1 = paramMap.get("number4_1");
        String number4_2 = paramMap.get("number4_2");
        String parameters = schoolSettingNewDTO.getParameters();
        if (StringUtil.isNotEmpty(fee1) && StringUtil.isNotEmpty(number1_1) && StringUtil.isNotEmpty(number1_2))
            parameters = parameters + "|" + fee1 + "/" + number1_1 + "/" + number1_2;
        if (StringUtil.isNotEmpty(fee2) && StringUtil.isNotEmpty(number2_1) && StringUtil.isNotEmpty(number2_2))
            parameters = parameters + "|" + fee2 + "/" + number2_1 + "/" + number2_2;
        if (StringUtil.isNotEmpty(fee3) && StringUtil.isNotEmpty(number3_1) && StringUtil.isNotEmpty(number3_2))
            parameters = parameters + "|" + fee3 + "/" + number3_1 + "/" + number3_2;
        if (StringUtil.isNotEmpty(fee4) && StringUtil.isNotEmpty(number4_1) && StringUtil.isNotEmpty(number4_2))
            parameters = parameters + "|" + fee4 + "/" + number4_1 + "/" + number4_2;
        schoolSettingNewDTO.setParameters(parameters);
        Response rs;
        if ((  rs = lookschoolSettingLevel(schoolSettingNewDTO)) != null)
            return rs;
        return new Response(1, "fail");
    }

    // 变动比例
    @RequestMapping(value = "/addSetting4",method = RequestMethod.POST)
    @ResponseBody
    public Response addSetting4(//@RequestBody SchoolSettingNewDTO schoolSettingNewDTO,
                                @RequestBody Map<String, String> parammMap,
                                HttpServletRequest request, HttpServletResponse response){
        super.setPostHeader(response);
        SchoolSettingNewDTO schoolSettingNewDTO = JSON.parseObject(JSON.toJSONString(parammMap), SchoolSettingNewDTO.class);
        String parameters = "";
        if (schoolSettingNewDTO.getType() != 4)
            return new Response(1,"RATE类型值错误：" + schoolSettingNewDTO.getType());
        String proportion1 = parammMap.get("proportion1");
        String proportion2 = parammMap.get("proportion2");
        String proportion3 = parammMap.get("proportion3");
        String proportion4 = parammMap.get("proportion4");
        String number1 = parammMap.get("number1");
        String number2 = parammMap.get("number2");
        String number3 = parammMap.get("number3");
        String number4 = parammMap.get("number4");
        if (StringUtil.isNotEmpty(proportion1) && StringUtil.isNotEmpty(number1))
            parameters = parameters + "|" + proportion1 + "/" + number1;
        if (StringUtil.isNotEmpty(proportion2) && StringUtil.isNotEmpty(number2))
            parameters = parameters + "|" + proportion2 + "/" + number2;
        if (StringUtil.isNotEmpty(proportion3) && StringUtil.isNotEmpty(number3))
            parameters = parameters + "|" + proportion3 + "/" + number3;
        if (StringUtil.isNotEmpty(proportion4) && StringUtil.isNotEmpty(number4))
            parameters = parameters + "|" + proportion4 + "/" + number4;
        schoolSettingNewDTO.setParameters(parameters);
        Response rs;
        if ((  rs = lookschoolSettingLevel(schoolSettingNewDTO)) != null)
            return rs;
        return new Response(1, "fail");
    }

    // 固定底价-无额外补贴
    @RequestMapping(value = "/addSetting7", method = RequestMethod.POST)
    @ResponseBody
    public Response addSchoolSetting7(@RequestBody SchoolSettingNewDTO schoolSettingNewDTO,
                                      HttpServletRequest request, HttpServletResponse response){
        super.setPostHeader(response);
        if (schoolSettingNewDTO.getType() != 7)
            return new Response(1,"RATE类型值错误：" + schoolSettingNewDTO.getType());
        Response rs;
        if ((  rs = lookschoolSettingLevel(schoolSettingNewDTO)) != null)
            return rs;
        return new Response(1, "fail");
    }

    /**
     * 删除Setting
     * @param paramMap
     * @param request
     * @param response
     * @return
     * @throws ServiceException
     */
    @RequestMapping(value = "/deleteSetting",method = RequestMethod.POST)
    @ResponseBody
    public Response deleteSetting(@RequestBody Map<String,String> paramMap,
                                  HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        super.setPostHeader(response);
        int id = StringUtil.toInt(paramMap.get("id"));
        if (schoolInstitutionService.deleteSetting(id) > 0)
            return new Response(0, "success");
        return new Response(1, "fail");

    }

    /**
     * id 查询 规则记录
     * @param id
     * @param request
     * @param response
     * @return
     */
    @GetMapping(value = "/getSetting")
    @ResponseBody
    public Response<SchoolSettingNewDTO> getSetting(@RequestParam(value = "id")int id,
                               HttpServletRequest request, HttpServletResponse response){
        super.setGetHeader(response);
        if ( id <= 0 )
            return new Response(1," id error");
        return new Response(0,schoolInstitutionService.getSchoolSettingNewById(id));
    }

    @PostMapping(value = "/addComment")
    @ResponseBody
    public Response addComment(@RequestBody JSONObject jsonObject,
            HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        super.setHeader(response);
        AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
        if (adminUserLoginInfo == null)
            return new Response(1,"请登录");
        SchoolInstitutionCommentDTO commentDTO =  JSON.parseObject(JSON.toJSONString(jsonObject), SchoolInstitutionCommentDTO.class);
        commentDTO.setUserName(adminUserLoginInfo.getUsername());
        if (commentDTO.getParentId() > 0){
            if (StringUtil.isBlank(commentDTO.getToUserName()))
                return new Response(1,"toUserName is null");
        }else
            commentDTO.setToUserName(null);
        if (schoolInstitutionService.addComment(commentDTO) > 0)
            return new Response(0,"添加成功",commentDTO.getId());
        return new Response(1,"添加失败", 0);
    }

    @GetMapping(value = "/listComment")
    @ResponseBody
    public ListResponse<List<SchoolInstitutionCommentDTO>> listComment(@RequestParam(value = "schoolInstitutionId") int schoolInstitutionId,
                                                                   HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        super.setGetHeader(response);
        List<SchoolInstitutionCommentDTO> list = schoolInstitutionService.listComment(schoolInstitutionId);
        return new ListResponse<>(true,list.size(),list.size(),list,"");
    }

    @GetMapping(value = "/deleteComment")
    @ResponseBody
    public Response deleteComment(@RequestParam(value = "id") int id,
                                  HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        super.setGetHeader(response);
        AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
        if (adminUserLoginInfo == null)
            return new Response(1,"没有登录");
        SchoolInstitutionCommentDTO commentDTO = schoolInstitutionService.getCommentById(id);
        if (commentDTO.getUserName().equals(adminUserLoginInfo.getUsername()))
            if (schoolInstitutionService.deleteComment(id) > 0)
                return new Response(0,id);
        return new Response(1,"删除评论失败");
    }


    /**
     * Level：1 表示学校（全部）级别RATE，2表示学历级别RATE，3表示专业级别RATE
     * Level = 1  CourseLevel 和 CourseId 设置为null
     * Level = 2  CourseId      设置为null
     * Level = 3  CourseLevel   设置为null
     * @param schoolSettingNewDTO
     * @return Response
     */
    private Response lookschoolSettingLevel(SchoolSettingNewDTO schoolSettingNewDTO){
        if (schoolInstitutionService.getSchoolInstitutionById(schoolSettingNewDTO.getProviderId()) == null)
            return new Response(1, "学校不存在");
        if (schoolSettingNewDTO.getLevel() < 0 || schoolSettingNewDTO.getLevel() > 3)
            return new Response(1, "Level 错误");
        if (schoolSettingNewDTO.getLevel() == 1) {  //1级是学校级别(全部)
            /*
            if (schoolInstitutionService.getByProviderIdAndLevel(schoolSettingNewDTO.getProviderId(), 1 , null, null, schoolSettingNewDTO.getStartDate()) != null)
                return new Response(1, "创建新规则的开始时间只能在已有规则的结束时间之后");
            List<SchoolSettingNewDTO> settingList = schoolInstitutionService.getByProviderIdAndLevel(schoolSettingNewDTO.getProviderId(), 1 , null, null);
            Date startDate = schoolSettingNewDTO.getStartDate();
            Date endDate = schoolSettingNewDTO.getEndDate();
            for (SchoolSettingNewDTO setting : settingList){
                if (startDate.after(setting.getEndDate()))
                    continue;
                if (startDate.before(setting.getEndDate()) && startDate.after(setting.getStartDate())){
                    setting.setEndDate(startDate);
                }
                if (startDate.before(setting.getStartDate()))
                    setting.setEndDate(startDate);
            }
             */

            schoolSettingNewDTO.setCourseLevel(null);
            schoolSettingNewDTO.setCourseId(null);
            if (schoolInstitutionService.addSetting(schoolSettingNewDTO) > 0)
                return new Response(0, "success");
        }
        if (schoolSettingNewDTO.getLevel() == 2) {  //2级是学历级别
            if (StringUtil.isEmpty(schoolSettingNewDTO.getCourseLevel()))
                return new Response(1, "设置学历级别RATE,Course Level 不能为空");
            String [] courseLevelArr = schoolSettingNewDTO.getCourseLevel().split(",");
            for (String courseLevel : courseLevelArr) {
                if (schoolCourseService.list(schoolSettingNewDTO.getProviderId(), null, false, courseLevel, null, 0, 1).size() == 0)
                    return new Response(1, "不存在此学历：" + courseLevel);
                //if (schoolInstitutionService.getByProviderIdAndLevel(schoolSettingNewDTO.getProviderId(), 2, courseLevel, null) != null)
                //   return new Response(1, "学校已存在这个学历级别的RATE");
                /*List<SchoolSettingNewDTO> settingList = schoolInstitutionService.getByProviderIdAndLevel(schoolSettingNewDTO.getProviderId(), 2, courseLevel, null);
                for (SchoolSettingNewDTO setting : settingList){
                    if (setting.getEndDate().after(schoolSettingNewDTO.getStartDate()))
                        return new Response(1,"创建新规则的开始时间只能在已有规则的结束时间之后");
                }
                 */

            }
            schoolSettingNewDTO.setCourseId(null);
            String str = "";
            int num = 0;
            for (String courseLevel : courseLevelArr){
                schoolSettingNewDTO.setCourseLevel(courseLevel);
                if (schoolInstitutionService.addSetting(schoolSettingNewDTO) > 0)
                    num += 1;
                else
                    str = str + courseLevel + " 添加 Setting 失败 ,";
            }
            if (num == courseLevelArr.length)
                return new Response(0, "success");
            return new Response(1, "fail", str );
        }
        if (schoolSettingNewDTO.getLevel() == 3) {  //3级是专业级别
            if (schoolSettingNewDTO.getCourseId() == null)
                return new Response(1, "设置专业级别RATE,Course Id 不能为空");
            if (schoolCourseService.schoolCourseById(schoolSettingNewDTO.getCourseId()) == null)
                return new Response(1, "不存在此专业或者此专业被冻结：" + schoolSettingNewDTO.getCourseId());
            //if (schoolInstitutionService.getByProviderIdAndLevel(schoolSettingNewDTO.getProviderId(), 3 , null , schoolSettingNewDTO.getCourseId()) != null)
            //    return new Response(1, "学校已存在这个专业级别的RATE");
            /*List<SchoolSettingNewDTO> settingList = schoolInstitutionService.getByProviderIdAndLevel(schoolSettingNewDTO.getProviderId(), 3 , null , schoolSettingNewDTO.getCourseId());
            for (SchoolSettingNewDTO setting : settingList){
                if (setting.getEndDate().after(schoolSettingNewDTO.getStartDate()))
                    return new Response(1,"创建新规则的开始时间只能在已有规则的结束时间之后");
            }
             */

            schoolSettingNewDTO.setCourseLevel(null);
            if (schoolInstitutionService.addSetting(schoolSettingNewDTO) > 0)
                return new Response(0, "success");
        }
        return null;
    }

}
