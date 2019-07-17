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
import org.zhinanzhen.b.service.MessageService;
import org.zhinanzhen.b.service.pojo.MessageDTO;
import org.zhinanzhen.b.service.pojo.MessageListDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/message")
public class MessageController extends BaseController {

	@Resource
	MessageService messageService;

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> addMessage(@RequestParam(value = "adminUserId") Integer adminUserId,
			@RequestParam(value = "knowledgeId") Integer knowledgeId, @RequestParam(value = "content") String content,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			MessageDTO messageDto = new MessageDTO();
			messageDto.setAdminUserId(adminUserId);
			messageDto.setKnowledgeId(knowledgeId);
			messageDto.setContent(content);
			if (messageService.addMessage(messageDto) > 0)
				return new Response<Integer>(0, messageDto.getId());
			else
				return new Response<Integer>(1, "创建失败.", 0);
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/zan", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> zan(@RequestParam(value = "adminUserId") Integer adminUserId,
			@RequestParam(value = "messageId") Integer messageId, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			if (messageService.zan(adminUserId, messageId) > 0)
				return new Response<Integer>(0, "赞!");
			else
				return new Response<Integer>(1, "创建失败.", 0);
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<MessageListDTO>> listMessage(@RequestParam(value = "knowledgeId") Integer knowledgeId,
			HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<List<MessageListDTO>>(0, messageService.listMessage(knowledgeId));
		} catch (ServiceException e) {
			return new Response<List<MessageListDTO>>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/delete", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> deleteMessage(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, messageService.deleteMessage(id));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), 0);
		}
	}

}
