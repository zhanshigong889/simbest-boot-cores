/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.filter;

import com.simbest.boot.config.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * 用途：验证码过滤器
 * 作者: lishuyi
 * 时间: 2018/3/7  0:10
 */
public class RestCaptchaAuthenticationFilter extends CaptchaAuthenticationFilter {

    @Autowired
    private AppConfig config;

    public RestCaptchaAuthenticationFilter(RequestMatcher requiresAuthenticationRequestMatcher) {
        super(requiresAuthenticationRequestMatcher);
    }

}
