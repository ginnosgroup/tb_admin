package org.zhinanzhen.b.exception;

import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zhinanzhen.tb.controller.Response;

/**
 * Created with IntelliJ IDEA.
 * User: bsz
 * Date: 2020/10/14 11:39
 * Description:异常处理
 * Version: V1.0
 */
@ControllerAdvice
public class ExceptionHandlerAll {


    /**
     * 处理当前项目中的所有异常 Throwable
     * @return
     */
    @ExceptionHandler(Throwable.class)
    @ResponseBody
    public Response handlerException(Exception ex){
        //Response result = new Response(0," 系统出现错误，请联系管理员! ");
        ex.printStackTrace();
        Response result = new Response(1,ex.getMessage());
        return  result;
    }



}
