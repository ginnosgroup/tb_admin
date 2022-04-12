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
import org.zhinanzhen.b.service.ServiceService;
import org.zhinanzhen.b.service.pojo.ServicePackagePriceDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.ListResponse;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

import com.ikasoa.core.utils.StringUtil;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/servicePackagePrice")
public class ServicePackagePriceController extends BaseController {

	@Resource
	ServicePackageService servicePackageService;

	@Resource
	ServiceService serviceService;

	@Resource
	ServicePackagePriceService servicePackagePriceService;

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	public Response<String> add(@RequestParam(value = "minPrice") Double minPrice,
			@RequestParam(value = "maxPrice") Double maxPrice,
			@RequestParam(value = "serviceId") Integer serviceId,
			@RequestParam(value = "regionId") String regionId, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			if (serviceId != null && serviceId > 0 && serviceService.getServiceById(serviceId) == null)
				return new Response<String>(1, "服务项目不存在(" + serviceId + ")!", null);
			String[] regionIds = regionId.split(",");
			String msg = "";
			String ids = "";
			boolean isFail = false;
			for (String regionIdStr : regionIds) {
				if (StringUtil.isEmpty(regionIdStr))
					continue;
				List<ServicePackagePriceDTO> list = servicePackagePriceService.listServicePackagePrice(serviceId,
						Integer.parseInt(regionIdStr.trim()), 0, 1);
				if (list != null && list.size() > 0) {
					msg += "(地区ID:" + regionId + ",服务项目ID:" + serviceId + ")已存在!; ";
					isFail = true;
					continue;
				}
				ServicePackagePriceDTO servicePackagePriceDto = new ServicePackagePriceDTO();
				servicePackagePriceDto.setMinPrice(minPrice);
				servicePackagePriceDto.setMaxPrice(maxPrice);
				servicePackagePriceDto.setServiceId(serviceId);
				servicePackagePriceDto.setRegionId(Integer.parseInt(regionIdStr.trim()));
				if (servicePackagePriceService.addServicePackagePrice(servicePackagePriceDto) > 0) {
					msg += "(地区ID:" + regionId + ")创建成功!; ";
					ids += servicePackagePriceDto.getId() + ",";
				} else {
					msg += "(地区ID:" + regionId + ")创建失败!; ";
					isFail = true;
				}
			}
			if (isFail)
				return new Response<String>(1, "[服务包价格创建失败] " + msg, null);
			else
				return new Response<String>(0, "[服务包价格创建成功] " + msg, ids);
		} catch (ServiceException e) {
			return new Response<String>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	@ResponseBody
	public Response<String> update(@RequestParam(value = "id") int id,
			@RequestParam(value = "minPrice", required = false) Double minPrice,
			@RequestParam(value = "maxPrice", required = false) Double maxPrice,
			@RequestParam(value = "serviceId", required = false) Integer serviceId,
			@RequestParam(value = "regionId", required = false) String regionId, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			if (serviceId != null && serviceId > 0 && serviceService.getServiceById(serviceId) == null)
				return new Response<String>(1, "服务项目不存在(" + serviceId + ")!", null);
			String[] regionIds = regionId.split(",");
			String msg = "";
			String ids = "";
			boolean isFail = false;
			for (String regionIdStr : regionIds) {
				ServicePackagePriceDTO servicePackagePriceDto = servicePackagePriceService
						.getServicePackagePriceById(id);
				if (minPrice != null)
					servicePackagePriceDto.setMinPrice(minPrice);
				if (maxPrice != null)
					servicePackagePriceDto.setMaxPrice(maxPrice);
				if (serviceId != null && serviceId > 0)
					servicePackagePriceDto.setServiceId(serviceId);
				if (StringUtil.isNotEmpty(regionIdStr))
					servicePackagePriceDto.setRegionId(Integer.parseInt(regionIdStr.trim()));
				if (servicePackagePriceService.updateServicePackagePrice(servicePackagePriceDto) > 0) {
					msg += "(地区ID:" + regionId + ")修改成功!; ";
					ids += servicePackagePriceDto.getId() + ",";
				} else {
					msg += "(地区ID:" + regionId + ")修改失败!; ";
					isFail = true;
				}
			}
			if (isFail)
				return new Response<String>(1, "[服务包价格修改失败] " + msg, null);
			else
				return new Response<String>(0, "[服务包价格修改成功] " + msg, ids);
		} catch (ServiceException e) {
			return new Response<String>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public ListResponse<List<ServicePackagePriceDTO>> list(
			@RequestParam(value = "serviceId") Integer serviceId,
			@RequestParam(value = "pageNum") int pageNum, @RequestParam(value = "pageSize") int pageSize,
			HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new ListResponse<List<ServicePackagePriceDTO>>(true, pageSize,
					servicePackagePriceService.countServicePackagePrice(serviceId, 0),
					servicePackagePriceService.listServicePackagePrice(serviceId, 0, pageNum, pageSize), "");
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
