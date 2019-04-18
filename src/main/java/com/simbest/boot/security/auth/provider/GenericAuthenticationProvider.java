/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.provider;

import com.simbest.boot.security.auth.authentication.GenericAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * 用途：自定义密码认证器
 * 作者: lishuyi
 * 时间: 2018/9/6  16:57
 */
public class GenericAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private GenericAuthenticationChecker genericAuthenticationChecker;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        GenericAuthentication genericAuthentication = (GenericAuthentication)authentication;
        return genericAuthenticationChecker.authChek(authentication, genericAuthentication.getCredentials().getAppcode());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(GenericAuthentication.class);
    }
}
