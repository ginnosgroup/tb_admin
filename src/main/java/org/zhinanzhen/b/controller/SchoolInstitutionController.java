package org.zhinanzhen.b.controller;

import javax.servlet.http.HttpServletResponse;

import com.ikasoa.core.utils.StringUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.zhinanzhen.b.service.SchoolInstitutionService;
import org.zhinanzhen.b.service.pojo.SchoolDTO;
import org.zhinanzhen.b.service.pojo.SchoolInstitutionDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.ListResponse;
import org.zhinanzhen.tb.controller.Response;
import javax.annotation.Resource;
import java.util.List;

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

    @RequestMapping(value = "/list",method = RequestMethod.GET)
    @ResponseBody
    public ListResponse list(@RequestParam(value = "id",required = false ) Integer id, @RequestParam(value = "name" ,required =  false) String name,
                             @RequestParam(value = "type",required = false) String type,
                             @RequestParam(value = "pageNum") int pageNum, @RequestParam(value = "pageSize") int pageSize){
        if ( id != null && id > 0)
            return  new ListResponse(true , pageSize,1,schoolInstitutionService.getSchoolInstitutionById(id),"ok");
        int total =  schoolInstitutionService.count(name,type);
        return  new ListResponse(true , pageSize,total,schoolInstitutionService.listSchoolInstitutionDTO(name,type,pageNum,pageSize),"ok");
    }

    @RequestMapping(value = "/get",method = RequestMethod.GET)
    @ResponseBody
    public Response<SchoolInstitutionDTO> get(@RequestParam(value = "id")Integer id){
        SchoolInstitutionDTO schoolInstitutionDTO = schoolInstitutionService.getSchoolInstitutionById(id);
        return new Response<SchoolInstitutionDTO>( 0 ,schoolInstitutionDTO);
    }

    @RequestMapping(value = "/update" ,method =  RequestMethod.POST)
    @ResponseBody
    public Response update(@RequestBody SchoolInstitutionDTO schoolInstitutionDTO ,HttpServletResponse response){
        super.setPostHeader(response);
        if (StringUtil.isEmpty(schoolInstitutionDTO.getName()))
            return new Response<SchoolDTO>(1, "学校名称不能为空!", null);
        List<SchoolInstitutionDTO> listSchoolInstitutionDTO = schoolInstitutionService.listSchoolInstitutionDTO(schoolInstitutionDTO.getName(),null,0,9999);
        for (SchoolInstitutionDTO si : listSchoolInstitutionDTO){
            if (si.getId() != schoolInstitutionDTO.getId())
                return new Response(1,"名称重复!");
        }
        if (schoolInstitutionService.update(schoolInstitutionDTO))
            return new Response(0,schoolInstitutionDTO);
        else
            return new Response(1,"修改失败");

    }

    @RequestMapping(value = "/add" ,method = RequestMethod.POST)
    @ResponseBody
    public Response add(@RequestBody SchoolInstitutionDTO schoolInstitutionDTO, HttpServletResponse response){
        super.setPostHeader(response); 
        List<SchoolInstitutionDTO> listSchoolInstitutionDTO = schoolInstitutionService.listSchoolInstitutionDTO(schoolInstitutionDTO.getName(),null,0,9999);
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
}
