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
import org.zhinanzhen.b.service.SourceRegionService;
import org.zhinanzhen.b.service.SourceService;
import org.zhinanzhen.b.service.pojo.SourceDTO;
import org.zhinanzhen.b.service.pojo.SourceRegionDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/source")
public class SourceController extends BaseController {

	@Resource
	SourceService sourceService;

	@Resource
	SourceRegionService sourceRegionService;

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> addSource(@RequestParam(value = "name") String name,
			@RequestParam(value = "sourceRegionId", required = false) Integer sourceRegionId,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			SourceDTO sourceDto = new SourceDTO();
			sourceDto.setName(name);
			sourceDto.setSourceRegionId(sourceRegionId);
			if (sourceService.addSource(sourceDto) > 0) {
				return new Response<Integer>(0, sourceDto.getId());
			} else {
				return new Response<Integer>(1, "创建失败.", 0);
			}
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/addSourceRegion", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> addSourceRegion(@RequestParam(value = "name") String name,
			@RequestParam(value = "sourceRegionId", required = false) Integer sourceRegionId,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			SourceRegionDTO sourceRegionDto = new SourceRegionDTO();
			sourceRegionDto.setName(name);
			sourceRegionDto.setParentId(sourceRegionId);
			if (sourceRegionService.addSourceRegion(sourceRegionDto) > 0) {
				return new Response<Integer>(0, sourceRegionDto.getId());
			} else {
				return new Response<Integer>(1, "创建失败.", 0);
			}
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<SourceDTO>> list(
			@RequestParam(value = "sourceRegionId", required = false) Integer sourceRegionId,
			HttpServletResponse response) { // 如果sourceRegionId为空则返回所有Source
		try {
			super.setGetHeader(response);
			return new Response<List<SourceDTO>>(0, sourceService.listSource(sourceRegionId));
		} catch (ServiceException e) {
			return new Response<List<SourceDTO>>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/listSourceRegion", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<SourceRegionDTO>> listSourceRegion(
			@RequestParam(value = "sourceRegionId", required = false) Integer sourceRegionId,
			HttpServletResponse response) { // 如果sourceRegionId为空则返回所有SourceRegion,如果如果sourceRegionId为0则返回根节点
		try {
			super.setGetHeader(response);
			return new Response<List<SourceRegionDTO>>(0, sourceRegionService.listSourceRegion(sourceRegionId));
		} catch (ServiceException e) {
			return new Response<List<SourceRegionDTO>>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/delete", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> deleteSource(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, sourceService.deleteSource(id));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/deleteSourceRegion", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> deleteSourceRegion(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, sourceRegionService.deleteSourceRegion(id));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), 0);
		}
	}

}
