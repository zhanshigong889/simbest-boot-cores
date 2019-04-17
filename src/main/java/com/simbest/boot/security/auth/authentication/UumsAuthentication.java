/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.authentication;


import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

/**
 * 用途：基于UUMS主数据的登录认证
 * 作者: lishuyi
 * 时间: 2018/1/20  15:25
 */
public class UumsAuthentication extends UsernamePasswordAuthenticationToken {


    public UumsAuthentication(Object principal, Object uumsAuthenticationCredentials) {
        super(principal, uumsAuthenticationCredentials);
    }

}
