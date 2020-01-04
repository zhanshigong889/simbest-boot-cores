/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.filter;

import com.simbest.boot.constants.AuthoritiesConstants;
import com.simbest.boot.security.auth.authentication.UumsAuthentication;
import com.simbest.boot.security.auth.authentication.UumsAuthenticationCredentials;
import com.simbest.boot.security.auth.provider.GenericAuthenticationChecker;
import com.simbest.boot.security.auth.service.IAuthUserCacheService;
import com.simbest.boot.security.util.AuthenticationUtil;
import com.simbest.boot.util.encrypt.RsaEncryptor;
import com.simbest.boot.util.redis.RedisRetryLoginCache;
import com.simbest.boot.util.security.SecurityUtils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用途：基于数据库的主数据登录认证拦截器，拦截/login请求
 * 重载UsernamePasswordAuthenticationFilter
 * 作者: lishuyi
 * 时间: 2018/3/7  0:10
 */
@Slf4j
public class RsaAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    @Setter
    private RsaEncryptor encryptor;

    @Setter
    private IAuthUserCacheService authUserCacheService;

    @Setter
    private GenericAuthenticationChecker genericAuthenticationChecker;

    /**
     * 前端RSA加密，后端验证时，先RSA解密，再进行FormSecurityConfigurer的认证
     * @param request
     * @return String
     */
    @Override
    protected String obtainPassword(HttpServletRequest request) {
        return encryptor.decrypt(request.getParameter(SPRING_SECURITY_FORM_PASSWORD_KEY));
    }

    /**
     * @param request
     * @param response
     * @return Authentication
     * @throws AuthenticationException
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {
        String username = obtainUsername(request);
        String password = obtainPassword(request);
        String appcode = request.getParameter(AuthoritiesConstants.SSO_API_APP_CODE);
        Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();
        if (AuthenticationUtil.authenticationIsRequired(existingAuth, username)) {
            RedisRetryLoginCache.preCheckTryTimes(username);
            log.debug("用户【{}】即将通过凭证【{}】访问应用【{}】", username, password, appcode);
            Boolean cacheUserPassword = authUserCacheService.loadCacheUserPassword(username, password);
            if(null != cacheUserPassword && cacheUserPassword){
                log.debug("恭喜用户【{}】认证命中缓存！Congratulation auth user 【{} 】for success cache password----------START", username, username);
                log.debug("用户名【{}】和密码【{}】在缓存中存在登录认证通过的缓存记录，直接认证，可以登录成功！", username, password);
                log.debug("恭喜用户【{}】认证命中缓存！Congratulation auth user 【{} 】for success cache password----------END", username, username);
                UumsAuthentication uumsAuthentication = new UumsAuthentication(username, UumsAuthenticationCredentials.builder()
                        .password(password).appcode(appcode).build());
                return genericAuthenticationChecker.authChek(uumsAuthentication, appcode);
            }
            else{
                existingAuth = super.attemptAuthentication(request, response);
                if(null != existingAuth && existingAuth.isAuthenticated()){
                    if(!DigestUtils.md5Hex(password).equals(SecurityUtils.getAnyPassword())) {
                        authUserCacheService.saveOrUpdateCacheUserPassword(username, password, true);
                    }
                    return genericAuthenticationChecker.authChek(existingAuth, appcode);
                }
            }
        }
        return existingAuth;
    }

}
