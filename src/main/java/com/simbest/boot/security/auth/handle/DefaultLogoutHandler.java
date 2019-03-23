/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.handle;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.CompositeLogoutHandler;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

/**
 * 用途：默认登录处理器
 * 参考：
 * https://niocoder.com/2018/01/18/Spring-Security%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90%E5%85%AB-Spring-Security-%E9%80%80%E5%87%BA/
 * https://blog.csdn.net/py_xin/article/details/52634880
 * 作者: lishuyi
 * 时间: 2019/3/23  10:57
 */
@Slf4j
@Component
public class DefaultLogoutHandler implements LogoutHandler {

    private LogoutHandler handler;

    public DefaultLogoutHandler(){
        SecurityContextLogoutHandler securityContextLogoutHandler = new SecurityContextLogoutHandler();
        CookieClearingLogoutHandler cookieClearingLogoutHandler = new CookieClearingLogoutHandler(AbstractRememberMeServices.SPRING_SECURITY_REMEMBER_ME_COOKIE_KEY);
        this.handler = new CompositeLogoutHandler(Arrays.asList(securityContextLogoutHandler, cookieClearingLogoutHandler));
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        handler.logout(request, response, authentication);
    }
}
