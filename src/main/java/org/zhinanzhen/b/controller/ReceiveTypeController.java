package org.zhinanzhen.b.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zhinanzhen.b.service.OfficialStateEnum;
import org.zhinanzhen.b.service.ReceiveTypeService;
import org.zhinanzhen.b.service.ReceiveTypeStateEnum;
import org.zhinanzhen.b.service.pojo.OfficialDTO;
import org.zhinanzhen.b.service.pojo.ReceiveTypeDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

import com.ikasoa.core.utils.StringUtil;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/receiveType")
public class ReceiveTypeController extends BaseController {

	@Resource
	ReceiveTypeService receiveTypeService;

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> addReceiveType(@RequestParam(value = "name") String name,
			@RequestParam(value = "state") String state, @RequestParam(value = "weight") String weight,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			ReceiveTypeDTO receiveTypeDto = new ReceiveTypeDTO();
			receiveTypeDto.setName(name);
			if (StringUtils.isNotEmpty(state)) {
				receiveTypeDto.setState(ReceiveTypeStateEnum.get(state));
			}
			receiveTypeDto.setWeight(Integer.parseInt(weight));
			if (receiveTypeService.addReceiveType(receiveTypeDto) > 0) {
				return new Response<Integer>(0, receiveTypeDto.getId());
			} else {
				return new Response<Integer>(0, "创建失败.", 0);
			}
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	@ResponseBody
	public Response<ReceiveTypeDTO> updateReceiveType(@RequestParam(value = "id") int id,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "state", required = false) String state,
			@RequestParam(value = "weight", required = false) String weight, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			ReceiveTypeDTO receiveTypeDto = new ReceiveTypeDTO();
			receiveTypeDto.setId(id);
			receiveTypeDto.setName(name);
			if (StringUtils.isNotEmpty(state)) {
				receiveTypeDto.setState(ReceiveTypeStateEnum.get(state));
			}
			receiveTypeDto.setWeight(Integer.parseInt(weight));
			if (receiveTypeService.updateReceiveType(receiveTypeDto) > 0) {
				return new Response<ReceiveTypeDTO>(0, receiveTypeDto);
			} else {
				return new Response<ReceiveTypeDTO>(0, "修改失败.", null);
			}
		} catch (ServiceException e) {
			return new Response<ReceiveTypeDTO>(e.getCode(), e.getMessage(), null);
		}
	}

}
