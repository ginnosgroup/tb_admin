package org.zhinanzhen.tb.utils;

import com.alibaba.fastjson.JSONObject;
import com.ikasoa.core.utils.StringUtil;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * Date: 2021/01/26 14:09
 * Description:
 * Version: V1.0
 */
@Slf4j
public class WXWorkAPI {

    //群聊id
    public final static  String CHATID = "zhinanzhen";
    //public final static  String CHATID = "nameid";

    public  final  static  String  CORPID = "ww605a1531f63a3629";//企业id
    //public  final  static  String  CORPID = "wwd3243681b49f8414";//企业id

    public  final  static  String  AGENTID = "1000010";
    //public  final  static  String  AGENTID = "1000003";

    //应用的凭证密钥
    public  final  static  String  SECRET_CORP = "6jgmyQa32YLJMIdN5RNaXOOS2z2sDhnZ5p26193Lhp8";

    public  final  static  String  SECRET_CUSTOMER = "OJd0c4VImqx3EJitBCoCMosjmFOIOdlIgmiuegAiTHs";

    // 创建表格
    public static final String  SECRET_EXCEL = "SIn5ClSNgIR5KWRa6TWq0M2yjtC8rF-eLizmRjphjjg";


//    public  final  static  String  SECRET_EXCEL = "bjrJzyULUPxlvcUGIV5QTaVfp-jLwRWyVyAmRSlQX_k";

    //应用的凭证密钥
    //public  final  static  String  SECRET_CORP = "u9YgNImH-UjKwZwNMZmKIz174oiiuuPQpjnJT8s4kfs";

    //public  final  static  String  SECRET_CUSTOMER = "1mb5VhsAwYl1rLFWaMeCdLap2FHxQk2VNHPKicPVFZk";

    public   final  static String  WXWORK_STRING_CODE =
            "https://open.work.weixin.qq.com/wwopen/sso/qrConnect?appid=CORPID&agentid=AGENTID&redirect_uri=REDIRECT_URI&state=STATE";

    public  final  static  String USERINFO = "https://qyapi.weixin.qq.com/cgi-bin/user/getuserinfo?access_token=ACCESS_TOKEN&code=CODE";

    public  final  static  String ACCESS_TOKEN = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=ID&corpsecret=SECRET";

    public  final  static  String  EXTERNAL_CONTACT_LIST = "https://qyapi.weixin.qq.com/cgi-bin/externalcontact/batch/get_by_user?access_token=ACCESS_TOKEN";

    public  final  static  String SENDMESSAGE = "https://qyapi.weixin.qq.com/cgi-bin/appchat/send?access_token=ACCESS_TOKEN";

    public final static  String CREATECHAT = "https://qyapi.weixin.qq.com/cgi-bin/appchat/create?access_token=ACCESS_TOKEN";

    //统计管理里面获取客户统计数据
    public final static String BEHAVIOR_DATA = "https://qyapi.weixin.qq.com/cgi-bin/externalcontact/get_user_behavior_data?access_token=";

    //获取客户列表，List里面只有客户的userid
    public final static String CUSTOMERLIST = "https://qyapi.weixin.qq.com/cgi-bin/externalcontact/list?access_token=ACCESS_TOKEN&userid=USERID";
    
    private static final String WECOM_WEBHOOK = "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=1afa665e-642b-4098-b4d3-4f553efe06bf";

    // 在线文档分享机器人
    private static final String ONLINE_DOCUMENTS_WEBHOOK = "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=addc9829-eb26-49a0-8379-8b4e9ba077f6";

    // 创建表格
    public static final String SETUP_EXCEL = "https://qyapi.weixin.qq.com/cgi-bin/wedoc/create_doc?access_token=ACCESS_TOKEN";
    // 获取表格基础信息
    public static final String INFORMATION_EXCEL = "https://qyapi.weixin.qq.com/cgi-bin/wedoc/spreadsheet/get_sheet_properties?access_token=ACCESS_TOKEN";
    //编辑表格
    public static final String REDACT_EXCEL = "https://qyapi.weixin.qq.com/cgi-bin/wedoc/spreadsheet/batch_update?access_token=ACCESS_TOKEN";

    //发送GET请求
    public static JSONObject sendGet(String url) {

        JSONObject jsonss = null;
        BufferedReader in = null;
        try {
            //String urlNameString = url + "?" + param;
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            //connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 建立实际的连接
            connection.connect();
            // 获取所有响应头字段
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line;

            while ((line = in.readLine()) != null) {
                //result += line;
                jsonss =com.alibaba.fastjson.JSONObject.parseObject(line);
            }
        } catch (Exception e) {
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
        }
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return jsonss;
    }


    //发送POST请求
    public static JSONObject sendPostBody(String url, String userId , String cursor ,int limit) {
        JSONObject jsonss = null;
        try {
            String strRead = null;
            URL realUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();
            connection.setRequestMethod("POST");//请求post方式
            connection.setDoInput(true);
            connection.setDoOutput(true);

            //connection.setRequestProperty("Content-Type", "application/json");
            //connection.setRequestProperty("Authorization", "Bearer 59e0-9fcc-c3faea0e2a6c");
            connection.setRequestProperty("Accept", "application/json"); // 设置接收数据的格式
            connection.setRequestProperty("Content-Type", "application/json"); // 设置发送数据的格式
            connection.connect();
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
            //body参数在这里put到JSONObject中
            JSONObject parm = new JSONObject();
            parm.put("userid", userId);
            parm.put("cursor", cursor);
            parm.put("limit", limit);
            writer.write(parm.toString());
            writer.flush();
            InputStream is = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            while ((strRead = reader.readLine()) != null) {
                //sbf.append(strRead);
                jsonss =com.alibaba.fastjson.JSONObject.parseObject(strRead);
            }
            reader.close();
            connection.disconnect();
        }catch (Exception e){
            e.printStackTrace();
        }
        return  jsonss;
    }


    public static JSONObject sendPostBody_Map(String url,JSONObject parm){
        JSONObject jsonss = null;
        try {
            String strRead = null;
            URL realUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();
            connection.setRequestMethod("POST");//请求post方式
            connection.setDoInput(true);
            connection.setDoOutput(true);

            //connection.setRequestProperty("Content-Type", "application/json");
            //connection.setRequestProperty("Authorization", "Bearer 59e0-9fcc-c3faea0e2a6c");
            connection.setRequestProperty("Accept", "application/json"); // 设置接收数据的格式
            connection.setRequestProperty("Content-Type", "application/json"); // 设置发送数据的格式
            connection.connect();
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
            writer.write(parm.toString());
            writer.flush();
            InputStream is = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            while ((strRead = reader.readLine()) != null) {
                //sbf.append(strRead);
                jsonss =com.alibaba.fastjson.JSONObject.parseObject(strRead);
            }
            reader.close();
            connection.disconnect();
        }catch (Exception e){
            e.printStackTrace();
        }
        return  jsonss;
    }
    
    // added by sulei
	public static boolean sendWecomRotMsg(String content) {
		if (StringUtil.isEmpty(content)) {
			log.error("企业微信机器人信息发送失败:信息为空！");
			return false;
		}
		String json = StringUtil.merge("{\"msgtype\": \"markdown\", \"markdown\": {\"content\": \"", content, "\"}}");
		log.info("企业微信机器人发送信息:" + json);
		try {
			HttpClient client = new HttpClient();
			client.getHttpConnectionManager().getParams().setConnectionTimeout(5 * 1000);
			client.getHttpConnectionManager().getParams().setSoTimeout(2 * 60 * 1000);
			client.getParams().setContentCharset("UTF-8");
			PostMethod postMethod = new PostMethod(WECOM_WEBHOOK);
			postMethod.setRequestHeader("Content-Type", "applicantion/json");
			postMethod.setRequestEntity(new StringRequestEntity(json, "applicantion/json", "UTF-8"));
			int code = client.executeMethod(postMethod);
			log.info("企业微信机器人信息状态:" + code);
			return code == HttpStatus.SC_OK;
		} catch (Exception e) {
			log.error("企业微信机器人信息发送失败:", e.getMessage());
			return false;
		}
	}

    // added by sulei
    public static boolean sendShareLinkMsg(String url, String userName, String text) {
        JSONObject json = new JSONObject();
        json.put("msgtype", "template_card");
        JSONObject templateCard = new JSONObject();
        templateCard.put("card_type", "text_notice");
        JSONObject source = new JSONObject();
        source.put("icon_url", "https://wework.qpic.cn/wwpic/252813_jOfDHtcISzuodLa_1629280209/0");
        source.put("desc", "生成了在线excel：");
        source.put("desc_color", 0);
        templateCard.put("source", source);
        List<JSONObject> objects = new ArrayList<>();
        JSONObject horizontalContent = new JSONObject();
        horizontalContent.put("keyname", "导出用户");
        horizontalContent.put("value", userName);
        objects.add(horizontalContent);
        JSONObject horizontalContent2 = new JSONObject();
        horizontalContent2.put("keyname", "excel地址");
        horizontalContent2.put("value", "点击访问");
        horizontalContent2.put("type", 1);
        horizontalContent2.put("url", url);
        objects.add(horizontalContent2);
        templateCard.put("horizontal_content_list", objects);
        JSONObject mainTitle = new JSONObject();
        mainTitle.put("title", "导出信息");
        mainTitle.put("desc", text);
        templateCard.put("main_title", mainTitle);
        JSONObject cardAction = new JSONObject();
        cardAction.put("type", 1);
        cardAction.put("url", url);
        templateCard.put("card_action", cardAction);
        json.put("template_card", templateCard);
        String jsonString = JSONObject.toJSONString(json);
        log.info("企业微信机器人发送信息:" + jsonString);
        try {
            HttpClient client = new HttpClient();
            client.getHttpConnectionManager().getParams().setConnectionTimeout(5 * 1000);
            client.getHttpConnectionManager().getParams().setSoTimeout(2 * 60 * 1000);
            client.getParams().setContentCharset("UTF-8");
            PostMethod postMethod = new PostMethod(ONLINE_DOCUMENTS_WEBHOOK);
            postMethod.setRequestHeader("Content-Type", "applicantion/json");
            postMethod.setRequestEntity(new StringRequestEntity(jsonString, "applicantion/json", "UTF-8"));
            int code = client.executeMethod(postMethod);
            log.info("企业微信机器人信息状态:" + code);
            return code == HttpStatus.SC_OK;
        } catch (Exception e) {
            log.error("企业微信机器人信息发送失败:", e.getMessage());
            return false;
        }
    }

}
