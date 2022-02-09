package org.zhinanzhen.b.controller;

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

}
