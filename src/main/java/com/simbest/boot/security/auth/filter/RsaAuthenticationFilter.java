/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.filter;

import com.simbest.boot.util.encrypt.RsaEncryptor;
import com.simbest.boot.util.redis.RedisRetryLoginCache;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用途：重载UsernamePasswordAuthenticationFilter
 * 作者: lishuyi
 * 时间: 2018/3/7  0:10
 */
@Slf4j
public class RsaAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    @Setter
    private RsaEncryptor encryptor;

    /**
     * 前端RSA加密，后端验证时，先RSA解密，再进行FormSecurityConfigurer的认证
     * @param request
     * @return
     */
    @Override
    protected String obtainPassword(HttpServletRequest request) {
        return encryptor.decrypt(request.getParameter(SPRING_SECURITY_FORM_PASSWORD_KEY));
    }

    /**
     * @param request
     * @param response
     * @return
     * @throws AuthenticationException
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {
        RedisRetryLoginCache.preCheckTryTimes(obtainUsername(request));
        log.debug("用户【{}】即将通过凭证【{}】访问应用", obtainUsername(request), obtainPassword(request));
        return super.attemptAuthentication(request, response);
    }

}
