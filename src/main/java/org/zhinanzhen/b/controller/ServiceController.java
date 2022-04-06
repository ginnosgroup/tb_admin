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
import org.zhinanzhen.tb.controller.ListResponse;
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
	public Response<Integer> addService(@RequestParam(value = "code") String code,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "role",required = false)String role,
			@RequestParam(value = "isZx",required = false)String isZx,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			ServiceDTO serviceDto = new ServiceDTO();
			serviceDto.setName(name);
			serviceDto.setCode(code);
			serviceDto.setRole(role);
			serviceDto.setZx(isZx != null && "true".equalsIgnoreCase(isZx));
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
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "code", required = false) String code,
			@RequestParam(value = "role", required = false) String role, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			int i = serviceService.updateService(id, name, code, role);
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
	public ListResponse<List<ServiceDTO>> listService(@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "isZx", required = false) String isZx, @RequestParam(value = "pageNum") int pageNum,
			@RequestParam(value = "pageSize") int pageSize, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new ListResponse<List<ServiceDTO>>(true, pageSize,
					serviceService.countService(name, isZx != null && "true".equalsIgnoreCase(isZx)),
					serviceService.listService(name, isZx != null && "true".equalsIgnoreCase(isZx), pageNum, pageSize),
					"");
		} catch (ServiceException e) {
			return new ListResponse<List<ServiceDTO>>(false, pageSize, 0, null, e.getMessage());
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
	public Response<Integer> deleteService(@RequestParam(value = "id") int id,
										   @RequestParam(value = "isZx",required = false)String isZx,
										   HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, serviceService.deleteServiceById(id, isZx != null && "true".equalsIgnoreCase(isZx)));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), 0);
		}
	}

}