package com.kang.jwt.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.kang.jwt.annotation.PassToken;
import com.kang.jwt.annotation.UserLoginToken;
import com.kang.jwt.bean.entity.CheckResult;
import com.kang.jwt.bean.entity.User;
import com.kang.jwt.bean.out.ReturnOut;
import com.kang.jwt.constant.PropertyConstant;
import com.kang.jwt.constant.SystemConstant;
import com.kang.jwt.service.TokenService;
import com.kang.jwt.service.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.method.HandlerMethod;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import io.jsonwebtoken.Claims;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;


public class AuthenticationInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationInterceptor.class);
    @Autowired
    UserService userService;

    @Autowired
    TokenService tokenService;

    @Autowired
    PropertyConstant propertyConstant;

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
            Object object) throws Exception {
        String token = httpServletRequest.getHeader("token");// 从 http 请求头中取出 token
        // 如果不是映射到方法直接通过
        if (!(object instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) object;
        Method method = handlerMethod.getMethod();
        // 检查是否有passtoken注释，有则跳过认证
        if (method.isAnnotationPresent(PassToken.class)) {
            PassToken passToken = method.getAnnotation(PassToken.class);
            if (passToken.required()) {
                return true;
            }
        }
        // 检查有没有需要用户权限的注解
        if (method.isAnnotationPresent(UserLoginToken.class)) {
            UserLoginToken userLoginToken = method.getAnnotation(UserLoginToken.class);
            if (userLoginToken.required()) {
                // 执行认证
                if (token == null) {
                    logger.info("验证失败");
                    ReturnOut returnOut = new ReturnOut();
                    returnOut.setResultCode(SystemConstant.JWT_ERRCODE_NULL);
                    returnOut.setResultMessage("签名验证不存在");
                    // print(httpServletResponse, R.error(SystemConstant.JWT_ERRCODE_NULL,
                    // "签名验证不存在"));
                    print(httpServletResponse, returnOut);
                    return false;
                } else {
                    // 验证JWT的签名，返回CheckResult对象
                    CheckResult checkResult = TokenService.validateJWT(token);
                    Claims claims = checkResult.getClaims();
                    long now = System.currentTimeMillis();
                    System.out.println("now:" + now);
                    User user = new User();
                    long cha = 0;
                    // 解析成功的话，如果剩余的过期时间大于规定时间，不做处理，
                    // 如果小于规定时间则需要更新token，由于仍然要请求数据，所以把更新的token放到response的header部
                    if (checkResult.isSuccess()) {
                        System.out.println("过期时间：" + claims.getExpiration().getTime());
                        cha = claims.getExpiration().getTime() - now;
                    }
                    if (checkResult.isSuccess() && (cha > propertyConstant.updatetime)) {
                        System.out.println("cha:" + cha);
                        System.out.println("claims.getExpiration().getTime():" + claims.getExpiration().getTime());
                        return true;
                    } else if (checkResult.isSuccess() && (cha <= propertyConstant.updatetime)) {
                        System.out.println("claims.getExpiration().getTime():" + claims.getExpiration().getTime());
                        System.out.println("cha:" + cha);
                        httpServletResponse.setHeader("newtoken", tokenService.getToken(user));
                        return true;
                    } else {
                        JSONObject jsonObject=new JSONObject();
                        switch (checkResult.getErrCode()) {
                        // 签名验证不通过
                        case SystemConstant.JWT_ERRCODE_FAIL:
                            logger.info("签名验证不通过");
                            jsonObject.put("resultCode",checkResult.getErrCode());
                            jsonObject.put("resultMessage","签名验证不通过");
                            print(httpServletResponse, jsonObject);
                            break;
                        // 签名过期，返回过期提示码
                        case SystemConstant.JWT_ERRCODE_EXPIRE:
                            logger.info("签名过期");
                            jsonObject.put("resultCode",checkResult.getErrCode());
                            jsonObject.put("resultMessage","签名过期");
                            print(httpServletResponse, jsonObject);
                            break;
                        default:
                            break;
                        }
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o,
            ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
            Object o, Exception e) throws Exception {

    }

    public void print(HttpServletResponse response, Object message) {
        try {
            response.setStatus(HttpStatus.OK.value());
            response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
            response.setHeader("Cache-Control", "no-cache, must-revalidate");
            PrintWriter writer = response.getWriter();
            writer.write(message.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
