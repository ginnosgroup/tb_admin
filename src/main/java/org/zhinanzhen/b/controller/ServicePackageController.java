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
import org.zhinanzhen.b.service.ServicePackageService;
import org.zhinanzhen.b.service.ServicePackageTypeEnum;
import org.zhinanzhen.b.service.ServiceService;
import org.zhinanzhen.b.service.pojo.ServicePackageDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/servicePackage")
public class ServicePackageController extends BaseController {

	@Resource
	ServiceService serviceService;

	@Resource
	ServicePackageService servicePackageService;

	public enum TypeEnum {
		CA, EOI, SA, VA, ZD, TM, DB;
		public static TypeEnum get(String name) {
			for (TypeEnum e : TypeEnum.values())
				if (e.toString().equals(name))
					return e;
			return null;
		}
	}

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> add(@RequestParam(value = "type") String type,
			@RequestParam(value = "serviceId") int serviceId, @RequestParam(value = "num") int num,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			if (ServicePackageTypeEnum.getServicePackageTypeEnum(type) == null)
				return new Response<Integer>(1, "服务包类型错误(" + type + ")!", 0);
			super.setPostHeader(response);
			if (serviceService.getServiceById(serviceId) == null)
				return new Response<Integer>(1, "服务项目不存在(" + serviceId + ")!", 0);
			ServicePackageDTO servicePackageDto = new ServicePackageDTO();
			servicePackageDto.setType(type);
			servicePackageDto.setServiceId(serviceId);
			servicePackageDto.setNum(num);
			if (servicePackageService.add(servicePackageDto) > 0)
				return new Response<Integer>(0, servicePackageDto.getId());
			else
				return new Response<Integer>(1, "创建失败.", 0);
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> update(@RequestParam(value = "id") int id,
			@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "serviceId", required = false) int serviceId,
			@RequestParam(value = "num", required = false) int num, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			ServicePackageDTO servicePackageDto = new ServicePackageDTO();
			servicePackageDto.setId(id);
			if (type != null) {
				if (ServicePackageTypeEnum.getServicePackageTypeEnum(type) == null)
					return new Response<Integer>(1, "服务包类型错误(" + type + ")!", 0);
				servicePackageDto.setType(type);
			}
			if (serviceId >= 0) {
				if (serviceService.getServiceById(serviceId) == null)
					return new Response<Integer>(1, "服务项目不存在(" + serviceId + ")!", 0);
				servicePackageDto.setServiceId(serviceId);
			}
			if (num >= 0)
				servicePackageDto.setNum(num);
			if (servicePackageService.update(servicePackageDto) > 0)
				return new Response<Integer>(0, servicePackageDto.getId());
			else
				return new Response<Integer>(1, "修改失败.", 0);
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<ServicePackageDTO>> list(
			@RequestParam(value = "serviceId", required = false) Integer serviceId, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<List<ServicePackageDTO>>(0, servicePackageService.list(serviceId));
		} catch (ServiceException e) {
			return new Response<List<ServicePackageDTO>>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/get", method = RequestMethod.GET)
	@ResponseBody
	public Response<ServicePackageDTO> get(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<ServicePackageDTO>(0, servicePackageService.getById(id));
		} catch (ServiceException e) {
			return new Response<ServicePackageDTO>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/delete", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> delete(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, servicePackageService.delete(id));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), 0);
		}
	}

}