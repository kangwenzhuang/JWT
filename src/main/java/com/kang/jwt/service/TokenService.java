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
        .setId(user.getId())
        .setSubject("happy")
        .setIssuer("fzu")
        .setIssuedAt(now)
        .signWith(SignatureAlgorithm.HS256, secretKey);
        if (propertyConstant.ttlMillis >= 0) {
			long expMillis = nowMillis + propertyConstant.ttlMillis;
			Date expDate = new Date(expMillis);
			builder.setExpiration(expDate); // 过期时间
		}
        return builder.compact();
		}
		//验证token有效性
    public static CheckResult validateJWT(String jwtStr) {
		CheckResult checkResult = new CheckResult();
		Claims claims = null;
		try {
			claims = parseJWT(jwtStr);
			checkResult.setSuccess(true);
			checkResult.setClaims(claims);
		} catch (ExpiredJwtException e) {
			checkResult.setErrCode(SystemConstant.JWT_ERRCODE_EXPIRE);
			checkResult.setSuccess(false);
		} catch (SignatureException e) {
			checkResult.setErrCode(SystemConstant.JWT_ERRCODE_FAIL);
			checkResult.setSuccess(false);
		} catch (Exception e) {
			checkResult.setErrCode(SystemConstant.JWT_ERRCODE_FAIL);
			checkResult.setSuccess(false);
		}
		return checkResult;
		}
		//解析token
    public static Claims parseJWT(String jwt) throws Exception {
		SecretKey secretKey = generalKey();
		return Jwts.parser()
			.setSigningKey(secretKey)
			.parseClaimsJws(jwt)
			.getBody();
		}
		//签名加密
    public static SecretKey generalKey() {
		byte[] encodedKey = Base64.decode(SystemConstant.JWT_SECERT);
	    SecretKey key = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
	    return key;
	}
}
