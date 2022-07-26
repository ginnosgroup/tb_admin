package org.zhinanzhen.b.service.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;

import javax.annotation.Resource;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.EverydayExchangeRateDAO;
import org.zhinanzhen.b.dao.pojo.EverydayExchangeRateDO;
import org.zhinanzhen.b.service.ExchangeRateService;
import org.zhinanzhen.b.service.pojo.ExchangeRateDTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ikasoa.core.utils.StringUtil;

import lombok.extern.slf4j.Slf4j;

@Service("ExchangeRateService")
@EnableScheduling
@Slf4j
public class ExchangeRateServiceImpl extends BaseService implements ExchangeRateService {

	@Resource
	private EverydayExchangeRateDAO everydayExchangeRateDao;

	@Override
	public ExchangeRateDTO getExchangeRate() throws ServiceException {
		EverydayExchangeRateDO everydayExchangeRateDo = everydayExchangeRateDao.getLastOne();
		if (everydayExchangeRateDo != null)
			return new ExchangeRateDTO(everydayExchangeRateDo.getZnzExchangeRate(),
					everydayExchangeRateDo.getUpdateTime());
		return null;
	}
	
	@org.springframework.scheduling.annotation.Scheduled(cron = "0 0 12 * * ?")
	public void everyDayTask(){
		try {
			JSONObject jsonObject = getJsonObject("http://web.juhe.cn/finance/exchange/rmbquot?key=459f1492038689af44230eb125de38c7");
			JSONArray resultArray = jsonObject.getJSONArray("result");
			JSONObject result = (JSONObject) resultArray.get(0);
			JSONObject data6 = (JSONObject) result.get("data6");
			Double fSellPri = data6.getDoubleValue("fSellPri") / 100;

			EverydayExchangeRateDO everydayExchangeRateDo = new EverydayExchangeRateDO();
			everydayExchangeRateDo.setCurrency("AUD");
			everydayExchangeRateDo.setOriginalExchangeRate(fSellPri);
			everydayExchangeRateDo.setZnzExchangeRate(fSellPri + 0.1);
			everydayExchangeRateDo.setUpdateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
					.parse(StringUtil.merge(data6.getString("date"), " ", data6.getString("time"))));
			log.info("获取实时汇率:" + everydayExchangeRateDo);
			everydayExchangeRateDao.add(everydayExchangeRateDo);
		} catch (Exception e) {
			log.error("获取实时汇率异常:" + e.getMessage());
		}
	}
	
	private static JSONObject getJsonObject(String url) {
		JSONObject json = null;
		BufferedReader in = null;
		try {
			URL realUrl = new URL(url);
			URLConnection connection = realUrl.openConnection();
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("connection", "Keep-Alive");
			connection.connect();
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line;
			while ((line = in.readLine()) != null)
				json = com.alibaba.fastjson.JSONObject.parseObject(line);
		} catch (Exception e) {
			log.error("发送GET请求出现异常！" + e.getMessage());
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return json;
	}

}
