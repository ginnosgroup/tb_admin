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
import org.zhinanzhen.b.service.ServiceService;
import org.zhinanzhen.b.service.pojo.ServiceDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/service")
public class ServiceController extends BaseController {

	@Resource
	ServiceService serviceService;

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> addService(@RequestParam(value = "type") String type,
			@RequestParam(value = "code") String code, HttpServletRequest request, HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			if (!"VISA".equals(type) && !"OVST".equals(type))
				return new Response<Integer>(1, "服务类型错误!", 0);
			ServiceDTO serviceDto = new ServiceDTO();
			serviceDto.setType(type);
			serviceDto.setCode(code);
			if (serviceService.addService(serviceDto) > 0) {
				return new Response<Integer>(0, serviceDto.getId());
			} else {
				return new Response<Integer>(1, "创建失败.", 0);
			}
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> updateService(@RequestParam(value = "id") int id,
			@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "code", required = false) String code, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			if (type != null && (!"VISA".equals(type) && !"OVST".equals(type)))
				return new Response<Integer>(1, "服务类型错误!", 0);
			int i = serviceService.updateService(id, type, code);
			if (i > 0) {
				return new Response<Integer>(0, i);
			} else {
				return new Response<Integer>(1, "修改失败.", 0);
			}
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<ServiceDTO>> listService(@RequestParam(value = "type", required = false) String type,
			HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<List<ServiceDTO>>(0, serviceService.listService(type));
		} catch (ServiceException e) {
			return new Response<List<ServiceDTO>>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/get", method = RequestMethod.GET)
	@ResponseBody
	public Response<ServiceDTO> getService(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<ServiceDTO>(0, serviceService.getServiceById(id));
		} catch (ServiceException e) {
			return new Response<ServiceDTO>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/delete", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> deleteService(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, serviceService.deleteServiceById(id));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), 0);
		}
	}

}