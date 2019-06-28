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
import org.zhinanzhen.b.service.KnowledgeMenuService;
import org.zhinanzhen.b.service.KnowledgeService;
import org.zhinanzhen.b.service.pojo.KnowledgeDTO;
import org.zhinanzhen.b.service.pojo.KnowledgeMenuDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/knowledge")
public class KnowledgeController extends BaseController {

	// @Resource
	KnowledgeService knowledgeService;

	@Resource
	KnowledgeMenuService knowledgeMenuService;

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> add(@RequestParam(value = "title") String title,
			@RequestParam(value = "content") String content,
			@RequestParam(value = "knowledgeMenuId", required = false) Integer knowledgeMenuId,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			KnowledgeDTO knowledgeDto = new KnowledgeDTO();
			knowledgeDto.setTitle(title);
			knowledgeDto.setContent(content);
			knowledgeDto.setKnowledgeMenuId(knowledgeMenuId);
			if (knowledgeService.addKnowledge(knowledgeDto) > 0) {
				return new Response<Integer>(0, knowledgeDto.getId());
			} else {
				return new Response<Integer>(1, "创建失败.", 0);
			}
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/addMenu", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> addMenu(@RequestParam(value = "name") String name,
			@RequestParam(value = "knowledgeMenuId", required = false) Integer knowledgeMenuId,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			KnowledgeMenuDTO knowledgeMenuDto = new KnowledgeMenuDTO();
			knowledgeMenuDto.setName(name);
			knowledgeMenuDto.setParentId(knowledgeMenuId == null ? 0 : knowledgeMenuId);
			if (knowledgeMenuService.addKnowledgeMenu(knowledgeMenuDto) > 0) {
				return new Response<Integer>(0, knowledgeMenuDto.getId());
			} else {
				return new Response<Integer>(1, "创建失败.", 0);
			}
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<KnowledgeDTO>> list(
			@RequestParam(value = "knowledgeMenuId", required = false) Integer knowledgeMenuId,
			HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<List<KnowledgeDTO>>(0, knowledgeService.listKnowledge(knowledgeMenuId));
		} catch (ServiceException e) {
			return new Response<List<KnowledgeDTO>>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/listMenu", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<KnowledgeMenuDTO>> listMenu(
			@RequestParam(value = "knowledgeMenuId", required = false) Integer knowledgeMenuId,
			HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<List<KnowledgeMenuDTO>>(0, knowledgeMenuService.listKnowledgeMenu(knowledgeMenuId));
		} catch (ServiceException e) {
			return new Response<List<KnowledgeMenuDTO>>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/delete", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> delete(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, knowledgeService.deleteKnowledge(id));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/deleteMenu", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> deleteMenu(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, knowledgeMenuService.deleteKnowledgeMenu(id));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), 0);
		}
	}

}
