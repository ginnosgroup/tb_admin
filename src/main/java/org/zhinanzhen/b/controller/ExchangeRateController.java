package org.zhinanzhen.b.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.Response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/exchangeRate")
public class ExchangeRateController extends BaseController {

	@RequestMapping(value = "/getDailyCNYExchangeRate", method = RequestMethod.GET)
	@ResponseBody
	public Response<ExchangeRateData> getDailyExchangeRate(HttpServletRequest request, HttpServletResponse response) {
		try {
			return new Response<ExchangeRateData>(0, null, new ExchangeRateData(4.58,
					new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2022-07-11 10:33:00"), new Date()));
		} catch (ParseException e) {
			return new Response<ExchangeRateData>(1, e.getMessage(), null);
		}
	}

	@Data
	@AllArgsConstructor
	class ExchangeRateData {

		private Double rate; // 汇率

		private Date updateDate; // 汇率更新时间

		private Date currentDate; // 获取汇率时间

	}

}
