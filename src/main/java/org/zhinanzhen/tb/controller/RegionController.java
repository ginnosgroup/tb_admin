package org.zhinanzhen.tb.controller;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zhinanzhen.tb.service.RegionService;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.pojo.RegionDTO;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/region")
public class RegionController extends BaseController {

    @Resource
    RegionService regionService;

    @RequestMapping(value = "/list_all", method = RequestMethod.GET)
    @ResponseBody
    public Response<List<RegionDTO>> listAllRegion(HttpServletResponse response) throws ServiceException {
	super.setGetHeader(response);
	List<RegionDTO> list = regionService.listAllRegion();
	return new Response<List<RegionDTO>>(0, list);

    }

    @RequestMapping(value = "/addRegion", method = RequestMethod.GET)
    @ResponseBody
    public Response<Integer> addRegion(String name,int weight,HttpServletResponse response) throws ServiceException {
	super.setGetHeader(response);
	return new Response<Integer>(0, regionService.addRegion(name,weight));
    }

    @RequestMapping(value = "/addCity", method = RequestMethod.GET)
    @ResponseBody
    public Response<Integer> addCity(String name, int parentsId,int weight, HttpServletResponse response) throws ServiceException {
	super.setGetHeader(response);
	return new Response<Integer>(0, regionService.addCity(parentsId, name,weight));
    }

    @RequestMapping(value = "/update", method = RequestMethod.GET)
    @ResponseBody
    public Response<Boolean> update(String name, int id, int weight,HttpServletResponse response) throws ServiceException {
	super.setGetHeader(response);
	return new Response<Boolean>(0, regionService.updateRegion(id, name,weight));
    }
}
