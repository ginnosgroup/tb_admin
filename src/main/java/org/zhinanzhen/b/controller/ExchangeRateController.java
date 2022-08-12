package org.zhinanzhen.b.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zhinanzhen.b.service.ExchangeRateService;
import org.zhinanzhen.b.service.pojo.ExchangeRateDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

import lombok.AllArgsConstructor;
import lombok.Data;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/exchangeRate")
public class ExchangeRateController extends BaseController {

	@Resource
	ExchangeRateService exchangeRateService;

	@RequestMapping(value = "/getDailyCNYExchangeRate", method = RequestMethod.GET)
	@ResponseBody
	public Response<ExchangeRateData> getDailyExchangeRate(HttpServletRequest request, HttpServletResponse response) {
		try {
			ExchangeRateDTO exchangeRateDto = exchangeRateService.getExchangeRate();
			if (exchangeRateDto == null)
				return new Response<ExchangeRateData>(0, null, new ExchangeRateData(4.58,
						new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2022-07-11 10:33:00"), new Date())); // 如果获取汇率失败就用默认汇率
			return new Response<ExchangeRateData>(0, null,
					new ExchangeRateData(exchangeRateDto.getRate(), exchangeRateDto.getUpdateDate(), new Date()));
		} catch (ServiceException | ParseException e) {
			return new Response<ExchangeRateData>(1, e.getMessage(), null);
		}
	}
	
	@RequestMapping(value = "/listDailyCNYExchangeRate", method = RequestMethod.GET)
	@ResponseBody
	public Response<String> listDailyExchangeRate(HttpServletRequest request,
			HttpServletResponse response) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			List<ExchangeRateDTO> list = exchangeRateService.listExchangeRate();
			if (list == null)
				return new Response<String>(1, "", null);
			String s = "";
			for (ExchangeRateDTO r : list) {
				s += r.getRate() + "|" + sdf.format(r.getUpdateDate()) + ";";
			}
			return new Response<String>(0, "", s);
		} catch (ServiceException e) {
			return new Response<String>(1, e.getMessage(), null);
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
