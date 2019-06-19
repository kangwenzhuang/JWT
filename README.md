                                       该教程适用于新手，授之以鱼不如授之以渔

# JWT
入门级教程，超详细，手把手教你搭建springboot+jwt框架，包括异常处理，token更新问题

克隆项目，导入maven工程，更新项目即可运行
先运行，后自行搭建完善，导入test.sql文件，mysql版本8.0，其他版本自行百度解决问题,配置自己数据库

你将学到的东西
1.自定义注解
2.全局异常捕获和拦截器
3.全局常量定义的方法
4.前后端分离是数据的返回格式，必须统一
5.jwt的生成和解析还有更新
当然你的ide要安装lombok插件
因为类写的简单了，不安装也行，自己写get，set方法等
package com.kang.jwt.bean.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    String Id;
    String username;
    String password;
}


第一步：先写两个自定义注解，passToken,允许不带token，适用于场景登陆注册等，UserLoginToken，强行带token
package com.kang.jwt.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author kangwenzhuang
 * @data 2019/6/19
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PassToken {
    boolean required() default true;
}

package  com.kang.jwt.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author kangwenzhuang
 * @data 2019/6/19
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface UserLoginToken {
    boolean required() default true;
}
第二步：写一个全局常量，作用：高大上
第一种写法
package com.kang.jwt.constant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;



@Component

public class PropertyConstant{
    @Value("${ttlMillis}")
    public long ttlMillis;
    @Value("${updatetime}")
    public long updatetime;
}
但是需要在application.yml添加
ttlMillis: 120000

updatetime: 60000

第二种写法相对简单不做推荐

package com.kang.jwt.constant;

public class SystemConstant {
    /**
	 * token
	 */
	public static final int RESCODE_REFTOKEN_MSG = 1006;		//刷新TOKEN(有返回数据)
	public static final int RESCODE_REFTOKEN = 1007;			//刷新TOKEN
	
	public static final int JWT_ERRCODE_NULL = 4000;			//Token不存在
	public static final int JWT_ERRCODE_EXPIRE = 4001;			//Token过期
	public static final int JWT_ERRCODE_FAIL = 4002;			//验证不通过

	/**
	 * JWT
	 */
	public static final String JWT_SECERT = "8677df7fc3a34e26a61c034d5ec8245d";			//密匙
	public static final long JWT_TTL = 60 * 60 * 1000;									//token有效时间
}
第三步：写一个token服务，作用：生成token，解析，签名，验证有效性，部分代码

package com.kang.jwt.service;

import java.util.Date;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.kang.jwt.bean.entity.CheckResult;
import com.kang.jwt.bean.entity.User;
import com.kang.jwt.constant.PropertyConstant;
import com.kang.jwt.constant.SystemConstant;

import org.bouncycastle.util.encoders.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;


/**
 * @author jinbin
 * @date 2018-07-08 21:04
 */
@Service("TokenService")
public class TokenService {

    @Autowired
		PropertyConstant propertyConstant;
		
	//获取生成token
    public String getToken(User user) {
        long nowMillis = System.currentTimeMillis();
        SecretKey secretKey = generalKey();
        Date now=new Date(nowMillis);
        JwtBuilder builder=Jwts.builder()
第四步：写一个拦截器进行拦截，部分代码
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
第五步：将拦截器加入到spring中
package com.kang.jwt.config;
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authenticationInterceptor())
                .addPathPatterns("/**");    // 拦截所有请求，通过判断是否有 @LoginRequired 注解 决定是否需要登录
    }
    @Bean
    public AuthenticationInterceptor authenticationInterceptor() {
        return new AuthenticationInterceptor();
    }
}
第六步：就是全局异常处理，目的：请求时不会报错，仍然restful风格返回数据，这里没有分400,404等异常，统一处理
package com.kang.jwt.config;

import com.kang.jwt.bean.out.ReturnOut;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

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
第七步：就是controller编写了
package com.kang.jwt.bean.controller;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.fastjson.JSONObject;
import com.kang.jwt.annotation.UserLoginToken;
import com.kang.jwt.bean.entity.User;
import com.kang.jwt.bean.out.ReturnOut;
import com.kang.jwt.service.TokenService;
import com.kang.jwt.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/user")
public class UserController {
    @Autowired
    UserService userService;
    @Autowired
    TokenService tokenService;

    // 登录
    @PostMapping("/login")
    public ReturnOut login(@RequestBody User user) {
        ReturnOut returnOut = new ReturnOut<>();
        User userForBase = userService.findByUsername(user);
        if (userForBase == null) {
            returnOut.setResultCode(10);
            returnOut.setResultMessage("登录失败,用户不存在");
            return returnOut;
        } else {
            if (!userForBase.getPassword().equals(user.getPassword())) {
                returnOut.setResultCode(20);
                returnOut.setResultMessage("登录失败,密码错误");
                return returnOut;
            } else {
                String token = tokenService.getToken(userForBase);
                Map<String,String> map=new ConcurrentHashMap<>();
                map.put("token", token);
                returnOut.setData(map);
                return returnOut;
            }
        }
    }

    @UserLoginToken
    @GetMapping("/getMessage")
    public ReturnOut getMessage() {
        ReturnOut returnOut = new ReturnOut<>();
        returnOut.setResultMessage("你已通过验证");
        return returnOut;
    }
}
全局返回格式统一

接下来就是测试：