package org.zhinanzhen.tb.utils;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.zhinanzhen.b.service.WXWorkService;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * Date: 2021/02/02 10:27
 * Description:
 * Version: V1.0
 */
@Aspect
@Component
public class WXWorkAOP {

    @Resource
    private WXWorkService wxWorkService;

    @Before("execution(* org.zhinanzhen.b.controller.WXWorkController.getExternalContactList(..))")
    public  void  beforeGetExternalContactList(JoinPoint joinPoint){
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpSession session=attr.getRequest().getSession(true);
        if (session.getAttribute("corpToken") == null) {
            Map<String, Object> tokenMap = wxWorkService.getToken(WXWorkAPI.SECRET_CORP);
            if ((int)tokenMap.get("errcode") != 0)
                throw  new RuntimeException(tokenMap.get("errmsg").toString());
            session.setAttribute("corpToken",tokenMap.get("access_token"));
            session.setMaxInactiveInterval((Integer) tokenMap.get("expires_in"));

            //session.setAttribute("corpToken", "mSX7E2148e7-HlJNxheKxSpPp6rE9mg6e5x1GBOtzletsY4RqJrI-s_2Qgb0Uqh8ykTwXXULqnWPR1GrFjXibiXIp6Q_GbL4Ys_-KCXOzcYweuvj4B40vLAHlY_wASZoEWYSsw4nDRkaXJUqMVse7FlyvMf9Mnxb5Ho6_6srScgrftH-VKB7sxJfKTAY0_IJLwwGk2DK9DUDWCjG-7Qw0A");
            //System.out.println("jinlail corp");
            //session.setMaxInactiveInterval(1800);
            //System.out.println("corpToken=========="+tokenMap.get("access_token"));
        }
        if (session.getAttribute("customerToken") == null) {
            Map<String, Object> tokenMap = wxWorkService.getToken(WXWorkAPI.SECRET_CUSTOMER);
            if ((int)tokenMap.get("errcode") != 0)
                throw  new RuntimeException(tokenMap.get("errmsg").toString());
            session.setAttribute("customerToken",tokenMap.get("access_token"));
            session.setMaxInactiveInterval((Integer) tokenMap.get("expires_in"));

            //session.setAttribute("customerToken", "T3zYHHM-iZRwBavvrtMaUJYghlImLkABcoVCdpIpVdZ5HYHenz2D0GlmerdArvEBqR6CmZ8r6Olh_qzU45R7woc6v13sxmRg9QlUSHX7nuJcgemaZUSkIAFg7LsAkLRLiGziij6XtZWEYEQaiqKta5JwnkvJCN-g6Ago7w_XN6wpTU7vP8cJW9IxwQuhL_CPBP9EQPIMA6FnmgKN5oVkMQ");
            //System.out.println("jinlail customer");
            //session.setMaxInactiveInterval(1800);
            //System.out.println("customerToken=========="+tokenMap.get("access_token"));
        }
    }

    @Before("execution(* org.zhinanzhen.b.controller.WXWorkController.getUserId(..))")
    public  void  beforegetUserId(JoinPoint joinPoint) throws Exception {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpSession session=attr.getRequest().getSession(true);
        if (session.getAttribute("corpToken") == null) {
            Map<String, Object> tokenMap = wxWorkService.getToken(WXWorkAPI.SECRET_CORP);
                if ((int)tokenMap.get("errcode") != 0)
                    throw  new RuntimeException( tokenMap.get("errmsg").toString());
                session.setAttribute("corpToken",tokenMap.get("access_token"));
                session.setMaxInactiveInterval((Integer) tokenMap.get("expires_in"));




            //session.setAttribute("corpToken", "mSX7E2148e7-HlJNxheKxSpPp6rE9mg6e5x1GBOtzletsY4RqJrI-s_2Qgb0Uqh8ykTwXXULqnWPR1GrFjXibiXIp6Q_GbL4Ys_-KCXOzcYweuvj4B40vLAHlY_wASZoEWYSsw4nDRkaXJUqMVse7FlyvMf9Mnxb5Ho6_6srScgrftH-VKB7sxJfKTAY0_IJLwwGk2DK9DUDWCjG-7Qw0A");
            //System.out.println("jinlail corp");
            //session.setMaxInactiveInterval(1800);
            //System.out.println("corpToken=========="+tokenMap.get("access_token"));
        }
        if (session.getAttribute("customerToken") == null) {
            Map<String, Object> tokenMap = wxWorkService.getToken(WXWorkAPI.SECRET_CUSTOMER);
            if ((int)tokenMap.get("errcode") != 0)
                throw  new RuntimeException( tokenMap.get("errmsg").toString());
            session.setAttribute("customerToken",tokenMap.get("access_token"));
            session.setMaxInactiveInterval((Integer) tokenMap.get("expires_in"));


            //session.setAttribute("customerToken", "T3zYHHM-iZRwBavvrtMaUJYghlImLkABcoVCdpIpVdZ5HYHenz2D0GlmerdArvEBqR6CmZ8r6Olh_qzU45R7woc6v13sxmRg9QlUSHX7nuJcgemaZUSkIAFg7LsAkLRLiGziij6XtZWEYEQaiqKta5JwnkvJCN-g6Ago7w_XN6wpTU7vP8cJW9IxwQuhL_CPBP9EQPIMA6FnmgKN5oVkMQ");
            //System.out.println("jinlail customer");
            //session.setMaxInactiveInterval(1800);
            //System.out.println("customerToken=========="+tokenMap.get("access_token"));
        }
    }

    @Before("execution(* org.zhinanzhen.b.service.impl.WXWorkServiceImpl.sendMsg(..))")
    public  void  beforeSendMsg(JoinPoint joinPoint) throws Exception {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpSession session=attr.getRequest().getSession(true);
        if (session.getAttribute("corpToken") == null) {
            Map<String, Object> tokenMap = wxWorkService.getToken(WXWorkAPI.SECRET_CORP);
            if ((int)tokenMap.get("errcode") != 0)
                throw  new RuntimeException( tokenMap.get("errmsg").toString());
            session.setAttribute("corpToken",tokenMap.get("access_token"));
            session.setMaxInactiveInterval((Integer) tokenMap.get("expires_in"));
        }
        if (session.getAttribute("customerToken") == null) {
            Map<String, Object> tokenMap = wxWorkService.getToken(WXWorkAPI.SECRET_CUSTOMER);
            if ((int)tokenMap.get("errcode") != 0)
                throw  new RuntimeException( tokenMap.get("errmsg").toString());
            session.setAttribute("customerToken",tokenMap.get("access_token"));
            session.setMaxInactiveInterval((Integer) tokenMap.get("expires_in"));
        }
    }
}
