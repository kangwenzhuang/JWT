package com.kang.jwt.mapper;

import com.kang.jwt.bean.entity.User;

import org.apache.ibatis.annotations.Param;


public interface UserMapper {
    User findByUsername(@Param("username") String username);
    User findUserById(@Param("Id") String Id);
}