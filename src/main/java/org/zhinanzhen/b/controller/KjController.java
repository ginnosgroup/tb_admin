package org.zhinanzhen.b.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
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
import org.springframework.web.multipart.MultipartFile;
import org.zhinanzhen.b.service.CommissionOrderService;
import org.zhinanzhen.b.service.KjService;
import org.zhinanzhen.b.service.KjStateEnum;
import org.zhinanzhen.b.service.ServiceOrderService;
import org.zhinanzhen.b.service.VisaService;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.b.service.pojo.CommissionOrderListDTO;
import org.zhinanzhen.b.service.pojo.KjDTO;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.b.service.pojo.VisaDTO;

import com.ikasoa.core.utils.StringUtil;

import lombok.AllArgsConstructor;
import lombok.Data;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/kj")
public class KjController extends BaseController {

	@Resource
	KjService kjService;

	@Resource
	ServiceOrderService serviceOrderService;

	@Resource
	VisaService visaService;

	@Resource
	CommissionOrderService commissionOrderService;

	@RequestMapping(value = "/upload_img", method = RequestMethod.POST)
	@ResponseBody
	public Response<String> uploadLogo(@RequestParam MultipartFile file, HttpServletRequest request,
			HttpServletResponse response) throws IllegalStateException, IOException {
		super.setPostHeader(response);
		return super.upload(file, request.getSession(), "/uploads/kj_img/");
	}

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> addKj(@RequestParam(value = "name") String name,
			@RequestParam(value = "phone") String phone, @RequestParam(value = "email") String email,
			@RequestParam(value = "password", required = false) String password,
			@RequestParam(value = "imageUrl") String imageUrl, @RequestParam(value = "regionId") Integer regionId,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			List<KjDTO> kjDtoList = kjService.listKj(null, null, 0, 1000);
			for (KjDTO kjDto : kjDtoList) {
				if (kjDto.getPhone().equals(phone))
					return new Response<Integer>(1, "该电话号已被使用,添加失败.", 0);
				if (kjDto.getEmail().equals(email))
					return new Response<Integer>(1, "该邮箱已被使用,添加失败.", 0);
			}
			if (adminUserService.getAdminUserByUsername(email) != null)
				return new Response<Integer>(1, "该邮箱已被管理员使用,添加失败.", 0);
			KjDTO kjDto = new KjDTO();
			kjDto.setName(name);
			kjDto.setPhone(phone);
			kjDto.setEmail(email);
			kjDto.setImageUrl(imageUrl);
			kjDto.setRegionId(regionId);
			if (kjService.addKj(kjDto) > 0) {
				if (password == null)
					password = email; // 如果没有传入密码,则密码和email相同
				adminUserService.add(email, password, "KJ", null, null, null, kjDto.getId());
				return new Response<Integer>(0, kjDto.getId());
			} else
				return new Response<Integer>(0, "创建失败.", 0);
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	@ResponseBody
	public Response<KjDTO> updateKj(@RequestParam(value = "id") int id,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "phone", required = false) String phone,
			@RequestParam(value = "email", required = false) String email,
			@RequestParam(value = "state", required = false) String state,
			@RequestParam(value = "imageUrl", required = false) String imageUrl,
			@RequestParam(value = "regionId", required = false) Integer regionId, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			KjDTO kjDto = new KjDTO();
			kjDto.setId(id);
			if (StringUtil.isNotEmpty(name))
				kjDto.setName(name);
			if (StringUtil.isNotEmpty(phone))
				kjDto.setPhone(phone);
			if (StringUtil.isNotEmpty(email))
				kjDto.setEmail(email);
			if (StringUtil.isNotEmpty(state))
				kjDto.setState(KjStateEnum.get(state));
			if (StringUtil.isNotEmpty(imageUrl))
				kjDto.setImageUrl(imageUrl);
			if (regionId != null && regionId > 0)
				kjDto.setRegionId(regionId);
			if (kjService.updateKj(kjDto) > 0)
				return new Response<KjDTO>(0, kjDto);
			else
				return new Response<KjDTO>(0, "修改失败.", null);
		} catch (ServiceException e) {
			return new Response<KjDTO>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/count", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> countKj(@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "regionId", required = false) Integer regionId, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, kjService.countKj(name, regionId));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<KjDTO>> listKj(@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "regionId", required = false) Integer regionId,
			@RequestParam(value = "pageNum") int pageNum, @RequestParam(value = "pageSize") int pageSize,
			HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<List<KjDTO>>(0, kjService.listKj(name, regionId, pageNum, pageSize));
		} catch (ServiceException e) {
			return new Response<List<KjDTO>>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/get", method = RequestMethod.GET)
	@ResponseBody
	public Response<KjDTO> getKj(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<KjDTO>(0, kjService.getKjById(id));
		} catch (ServiceException e) {
			return new Response<KjDTO>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/check", method = RequestMethod.POST)
	@ResponseBody
	public Response<List<CheckOrderDTO>> check(@RequestParam(value = "text") String text,
			HttpServletResponse response) {
		List<CheckOrderDTO> checkOrderList = new ArrayList<>();
		String message = "";
		String[] array1 = text.split("\n");
		for (String s : array1) {
			try {
				String[] array2 = s.split("[,]");
				if (array2.length >= 2) {
					String _id = array2[0].split("-")[0];
					String _amount = array2[1];
					if (_id.charAt(0) == 'V') { // 签证服务订单
						int id = Integer.parseInt(_id.substring(1, _id.length()).trim());
						double amount = Double.parseDouble(_amount.trim());
						ServiceOrderDTO serviceOrderDto = serviceOrderService.getServiceOrderById(id);
						if (serviceOrderDto != null) {
							VisaDTO visaDto = visaService.getFirstVisaByServiceOrderId(serviceOrderDto.getId());
							if (visaDto != null)
								if (visaDto.getAmount() == amount) {
									visaDto.setBankCheck(_id);
									visaDto.setChecked(true);
									visaService.updateVisa(visaDto);
									checkOrderList.add(
											new CheckOrderDTO(visaDto.getId(), visaDto.getGmtCreate(), "V", _id, true, ""));
								} else
									checkOrderList.add(new CheckOrderDTO(visaDto.getId(), visaDto.getGmtCreate(), "V",
											_id, false, ""));
							else
								checkOrderList.add(new CheckOrderDTO(-1, null, "V", _id, false,
										"未找到签证佣金订单(" + serviceOrderDto.getId() + ")"));
						} else
							checkOrderList.add(new CheckOrderDTO(-1, null, "V", _id, false, "未找到服务订单(" + id + ")"));
					} else if (_id.charAt(0) == 'C') { // 佣金订单
						if (_id.charAt(1) == 'V') { // 签证佣金订单
							int id = Integer.parseInt(_id.substring(2, _id.length()).trim());
							double amount = Double.parseDouble(_amount.trim());
							VisaDTO visaDto = visaService.getVisaById(id);
							if (visaDto != null)
								if (visaDto.getAmount() == amount) {
									visaDto.setBankCheck(_id);
									visaDto.setChecked(true);
									visaService.updateVisa(visaDto);
									checkOrderList.add(new CheckOrderDTO(visaDto.getId(), visaDto.getGmtCreate(), "CV",
											_id, true, ""));
								} else
									checkOrderList.add(new CheckOrderDTO(visaDto.getId(), visaDto.getGmtCreate(), "CV",
											_id, false, ""));
							else
								checkOrderList
										.add(new CheckOrderDTO(-1, null, "CV", _id, false, "未找到签证佣金订单(" + id + ")"));
						} else if (_id.charAt(1) == 'S') { // 留学佣金订单
							int id = Integer.parseInt(_id.substring(2, _id.length()).trim());
							double amount = Double.parseDouble(_amount.trim());
							CommissionOrderListDTO commissionOrderListDto = commissionOrderService
									.getCommissionOrderById(id);
							if (commissionOrderListDto != null)
								if (commissionOrderListDto.getAmount() == amount) {
									commissionOrderListDto.setBankCheck(_id);
									commissionOrderListDto.setChecked(true);
									commissionOrderService.updateCommissionOrder(commissionOrderListDto);
									checkOrderList.add(new CheckOrderDTO(commissionOrderListDto.getId(),
											commissionOrderListDto.getGmtCreate(), "CS", _id, true, ""));
								} else
									checkOrderList.add(new CheckOrderDTO(commissionOrderListDto.getId(),
											commissionOrderListDto.getGmtCreate(), "CS", _id, false, ""));
							else
								checkOrderList
										.add(new CheckOrderDTO(-1, null, "CS", _id, false, "未找到留学佣金订单(" + id + ")"));
						}
					} else { // 留学服务订单
						int id = Integer.parseInt(_id.trim());
						double amount = Double.parseDouble(_amount.trim());
						ServiceOrderDTO serviceOrderDto = serviceOrderService.getServiceOrderById(id);
						if (serviceOrderDto != null) {
							CommissionOrderListDTO commissionOrderListDto = commissionOrderService
									.getFirstCommissionOrderByServiceOrderId(serviceOrderDto.getId());
							if (commissionOrderListDto != null)
								if (commissionOrderListDto.getAmount() == amount) {
									commissionOrderListDto.setBankCheck(_id);
									commissionOrderListDto.setChecked(true);
									commissionOrderService.updateCommissionOrder(commissionOrderListDto);
									checkOrderList.add(new CheckOrderDTO(commissionOrderListDto.getId(),
											commissionOrderListDto.getGmtCreate(), "S", _id, true, ""));
								} else
									checkOrderList.add(new CheckOrderDTO(commissionOrderListDto.getId(),
											commissionOrderListDto.getGmtCreate(), "S", _id, false, ""));
							else
								checkOrderList.add(new CheckOrderDTO(-1, null, "S", _id, false,
										"未找到留学佣金订单(" + serviceOrderDto.getId() + ")"));
						} else
							checkOrderList.add(new CheckOrderDTO(-1, null, "S", _id, false, "未找到服务订单(" + id + ")"));
					}
				} else
					message += "格式错误(" + s + ");";
			} catch (Exception e) {
				message += "异常:" + e.getMessage() + ";";
			}
		}
		return new Response<>(0, message, checkOrderList);
	}

	@AllArgsConstructor
	@Data
	class CheckOrderDTO {

		private int id;

		private Date gmtCreate;

		private String type;

		private String bankContent;

		private boolean isChedked;
		
		private String message;

	}

}
