package org.zhinanzhen.tb.utils;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.zhinanzhen.b.service.WXWorkService;
import org.zhinanzhen.tb.controller.BaseController;

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
       setToken();
    }

    @Before("execution(* org.zhinanzhen.b.controller.WXWorkController.getUserId(..))")
    public  void  beforegetUserId(JoinPoint joinPoint) throws Exception {
        setToken();
    }

    @Before("execution(* org.zhinanzhen.b.service.impl.WXWorkServiceImpl.sendMsg(..))")
    public  void  beforeSendMsg(JoinPoint joinPoint) throws Exception {
        setToken();
    }

    @Before("execution(* org.zhinanzhen.b.service.impl.ServiceOrderServiceImpl.approval(..))")
    public  void  token(JoinPoint joinPoint) throws Exception {
        setToken();
    }

    @Before("execution(* org.zhinanzhen.b.controller.ServiceOrderController.nextFlow(..))")
    public  void  beforeNextFlow(JoinPoint joinPoint) throws Exception {
        setToken();
    }

    private void setToken(){
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpSession session=attr.getRequest().getSession(true);
        if (session.getAttribute("corpToken" + BaseController.VERSION) == null) {
            Map<String, Object> tokenMap = wxWorkService.getToken(WXWorkAPI.SECRET_CORP);
            if ((int)tokenMap.get("errcode") != 0)
                throw  new RuntimeException( tokenMap.get("errmsg").toString());
            session.setAttribute("corpToken" + BaseController.VERSION,tokenMap.get("access_token"));
            session.setMaxInactiveInterval((Integer) tokenMap.get("expires_in"));
        }
        if (session.getAttribute("customerToken" + BaseController.VERSION) == null) {
            Map<String, Object> tokenMap = wxWorkService.getToken(WXWorkAPI.SECRET_CUSTOMER);
            if ((int)tokenMap.get("errcode") != 0)
                throw  new RuntimeException( tokenMap.get("errmsg").toString());
            session.setAttribute("customerToken" + BaseController.VERSION,tokenMap.get("access_token"));
            session.setMaxInactiveInterval((Integer) tokenMap.get("expires_in"));
        }
    }

}
