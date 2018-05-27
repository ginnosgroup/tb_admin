package org.zhinanzhen.tb.controller;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.VirtualUserService;
import org.zhinanzhen.tb.service.pojo.VirtualUserDTO;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/virtual_user")
public class VirtualUserController extends BaseController {
	
	@Resource
	VirtualUserService virtualUserService;
	
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> addUser(@RequestParam(value = "name") String name, @RequestParam(value = "authNickname", required = false) String authNickname,
			@RequestParam(value = "authLogo") String authLogo, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, virtualUserService.addVirtualUser(name, authNickname, authLogo));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), -1);
		}
	}
	
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<VirtualUserDTO>> listVirtualUser(HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<List<VirtualUserDTO>>(0, virtualUserService.listVirtualUser());
		} catch (ServiceException e) {
			return new Response<List<VirtualUserDTO>>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/delete", method = RequestMethod.GET)
    @ResponseBody
    public Response<Boolean> deleteVirtualUser(@RequestParam(value = "id") int id, HttpServletResponse response) {
	try {
	    super.setGetHeader(response);
	    return new Response<Boolean>(0, virtualUserService.deleteById(id) > 0);
	} catch (ServiceException e) {
	    return new Response<Boolean>(1, e.getMessage(), false);
	}
    }

}
