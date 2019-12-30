/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * 用途：REST方式基于UUMS主数据的单点登录拦截器
 * 作者: lishuyi
 * 时间: 2018/1/20  15:05
 */
@Slf4j
public class RestUumsAuthenticationFilter extends UumsAuthenticationFilter {

    public RestUumsAuthenticationFilter(RequestMatcher requiresAuthenticationRequestMatcher) {
        super(requiresAuthenticationRequestMatcher);
    }


}
