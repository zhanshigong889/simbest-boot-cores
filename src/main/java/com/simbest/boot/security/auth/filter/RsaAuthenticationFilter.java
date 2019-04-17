/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.filter;

import com.simbest.boot.constants.AuthoritiesConstants;
import com.simbest.boot.constants.ErrorCodeConstants;
import com.simbest.boot.exceptions.AttempMaxLoginFaildException;
import com.simbest.boot.util.encrypt.RsaEncryptor;
import com.simbest.boot.util.redis.RedisUtil;
import lombok.Setter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 用途：重载UsernamePasswordAuthenticationFilter
 * 作者: lishuyi
 * 时间: 2018/3/7  0:10
 */
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
     * 最大错误登录次数不超过5次可尝试进行登录
     * @param request
     * @param response
     * @return
     * @throws AuthenticationException
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {
        String key = AuthoritiesConstants.LOGIN_FAILED_KEY + obtainUsername(request);
        Integer failedTimes = RedisUtil.getBean(key, Integer.class);
        if(null != failedTimes && failedTimes >= AuthoritiesConstants.ATTEMPT_LOGIN_MAX_TIMES){
            throw new AttempMaxLoginFaildException(ErrorCodeConstants.LOGIN_ERROR_EXCEED_MAX_TIMES);
        } else {
            return super.attemptAuthentication(request, response);
        }
    }

}
