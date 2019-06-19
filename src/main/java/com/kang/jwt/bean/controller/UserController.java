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
