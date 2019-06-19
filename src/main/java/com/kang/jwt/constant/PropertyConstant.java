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