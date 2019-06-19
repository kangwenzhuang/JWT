package com.kang.jwt.config;

import com.kang.jwt.bean.out.ReturnOut;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author jinbin
 * @date 2018-07-08 22:37
 */
@ControllerAdvice
public class GloablExceptionHandler {
    @ResponseBody
    @ExceptionHandler(Exception.class)
    public ReturnOut handleException(Exception e) {
        String msg = e.getMessage();
        int resultCode=0;
        if (msg == null || msg.equals("")) {
            msg = "服务器出错";
            resultCode=1111;
        }
        ReturnOut returnOut = new ReturnOut();
        returnOut.setResultCode(resultCode);
        returnOut.setResultMessage(msg);
        return returnOut;
    }
}
