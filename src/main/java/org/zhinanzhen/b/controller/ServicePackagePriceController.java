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
import org.zhinanzhen.b.service.ServicePackagePriceService;
import org.zhinanzhen.b.service.ServicePackageService;
import org.zhinanzhen.b.service.pojo.ServicePackagePriceDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.ListResponse;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/servicePackagePrice")
public class ServicePackagePriceController extends BaseController {

	@Resource
	ServicePackageService servicePackageService;

	@Resource
	ServicePackagePriceService servicePackagePriceService;

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> add(@RequestParam(value = "minPrice") Double minPrice,
			@RequestParam(value = "maxPrice") Double maxPrice,
			@RequestParam(value = "servicePackageId") int servicePackageId,
			@RequestParam(value = "regionId") int regionId, HttpServletRequest request, HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			if (servicePackageService.getById(servicePackageId) == null)
				return new Response<Integer>(1, "服务包不存在(" + servicePackageId + ")!", 0);
			List<ServicePackagePriceDTO> list = servicePackagePriceService.listServicePackagePrice(servicePackageId,
					regionId, 0, 1);
			if (list != null && list.size() > 0)
				return new Response<Integer>(1, "服务包价格已存在!", 0);
			ServicePackagePriceDTO servicePackagePriceDto = new ServicePackagePriceDTO();
			servicePackagePriceDto.setMinPrice(minPrice);
			servicePackagePriceDto.setMaxPrice(maxPrice);
			servicePackagePriceDto.setServicePackageId(servicePackageId);
			servicePackagePriceDto.setRegionId(regionId);
			if (servicePackagePriceService.addServicePackagePrice(servicePackagePriceDto) > 0)
				return new Response<Integer>(0, servicePackagePriceDto.getId());
			else
				return new Response<Integer>(1, "创建失败.", 0);
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}
	
	@RequestMapping(value = "/update", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> update(@RequestParam(value = "id") int id,
			@RequestParam(value = "minPrice", required = false) Double minPrice,
			@RequestParam(value = "maxPrice", required = false) Double maxPrice,
			@RequestParam(value = "servicePackageId", required = false) int servicePackageId,
			@RequestParam(value = "regionId", required = false) int regionId, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			if (servicePackageService.getById(servicePackageId) == null)
				return new Response<Integer>(1, "服务包不存在(" + servicePackageId + ")!", 0);
			List<ServicePackagePriceDTO> list = servicePackagePriceService.listServicePackagePrice(servicePackageId,
					regionId, 0, 1);
			if (list != null && list.size() > 0)
				return new Response<Integer>(1, "服务包价格已存在!", 0);
			ServicePackagePriceDTO servicePackagePriceDto = servicePackagePriceService.getServicePackagePriceById(id);
			if (minPrice != null)
				servicePackagePriceDto.setMinPrice(minPrice);
			if (maxPrice != null)
				servicePackagePriceDto.setMaxPrice(maxPrice);
			if (servicePackageId > 0)
				servicePackagePriceDto.setServicePackageId(servicePackageId);
			if (regionId > 0)
				servicePackagePriceDto.setRegionId(regionId);
			if (servicePackagePriceService.updateServicePackagePrice(servicePackagePriceDto) > 0)
				return new Response<Integer>(0, servicePackagePriceDto.getId());
			else
				return new Response<Integer>(1, "修改失败.", 0);
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public ListResponse<List<ServicePackagePriceDTO>> list(
			@RequestParam(value = "servicePackageId", required = false) Integer servicePackageId,
			@RequestParam(value = "pageNum") int pageNum, @RequestParam(value = "pageSize") int pageSize,
			HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new ListResponse<List<ServicePackagePriceDTO>>(true, pageSize,
					servicePackagePriceService.countServicePackagePrice(servicePackageId, 0),
					servicePackagePriceService.listServicePackagePrice(servicePackageId, 0, pageNum, pageSize), "");
		} catch (ServiceException e) {
			return new ListResponse<List<ServicePackagePriceDTO>>(false, pageSize, 0, null, e.getMessage());
		}
	}

	@RequestMapping(value = "/delete", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> delete(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			servicePackagePriceService.deleteById(id);
			return new Response<Integer>(0);
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), 0);
		}
	}

}
