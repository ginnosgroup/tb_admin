package org.zhinanzhen.b.controller;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zhinanzhen.b.service.SchoolService;
import org.zhinanzhen.tb.controller.BaseController;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/school")
public class SchoolController extends BaseController {

	@Resource
	SchoolService schoolService;

}
