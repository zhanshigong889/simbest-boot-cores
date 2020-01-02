/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.filter;

import com.simbest.boot.constants.AuthoritiesConstants;
import com.simbest.boot.security.IAuthService;
import com.simbest.boot.security.auth.authentication.GenericAuthentication;
import com.simbest.boot.security.auth.authentication.SsoUsernameAuthentication;
import com.simbest.boot.security.auth.authentication.principal.KeyTypePrincipal;
import com.simbest.boot.security.auth.authentication.principal.UsernamePrincipal;
import com.simbest.boot.security.util.AuthenticationUtil;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;

/**
 * 用途：单点登录拦截器
 * 作者: lishuyi
 * 时间: 2018/1/20  15:05
 */
//@WebFilter(filterName = "ssoAuthenticationFilter", urlPatterns = "/sso/*")
@Slf4j
public class SsoAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    @Setter
    private SsoAuthenticationRegister ssoAuthenticationRegister;



    public SsoAuthenticationFilter(RequestMatcher requiresAuthenticationRequestMatcher) {
        super(requiresAuthenticationRequestMatcher);
    }

    /**
     * 普通单点方法
     * /findByXXX/sso?loginuser=加密串&appcode=nma
     * /findByXXX/sso?uid=加密串&appcode=nma
     * /findByXXX/sso?keyword=加密串&keytype=keytype&appcode=nma
     *
     * 特殊单点方法-1
     * /findByUsername/sso?loginuser=加密串&username=username明文&appcode=nma
     * /findByUsername/sso?uid=加密串&username=username明文&appcode=nma
     * /findByUsername/sso?keyword=username加密串&keytype=username&username=username明文&appcode=nma
     *
     * 特殊单点方法-2
     * /findByKey/sso?keyword=keyword加密串&keytype=keytype&appcode=nma
     * @param request
     * @return
     */
    protected Principal obtainPrincipal(HttpServletRequest request, String appcode) {
        Principal principal = null;
        if(StringUtils.isNotEmpty(request.getParameter(AuthoritiesConstants.SSO_API_USERNAME))){
            principal = UsernamePrincipal.builder().username(ssoAuthenticationRegister.decodeKeyword(request.getParameter(AuthoritiesConstants.SSO_API_USERNAME), IAuthService.KeyType.username, appcode)).build();
            log.debug("SSO 单点认证过滤器从【{}】提取到Principal为：【{}】", AuthoritiesConstants.SSO_API_USERNAME, principal.getName());
        } else if(StringUtils.isNotEmpty(request.getParameter(AuthoritiesConstants.SSO_API_UID))){
            principal = UsernamePrincipal.builder().username(ssoAuthenticationRegister.decodeKeyword(request.getParameter(AuthoritiesConstants.SSO_API_UID), IAuthService.KeyType.username, appcode)).build();
            log.debug("SSO 单点认证过滤器从【{}】提取到Principal为：【{}】", AuthoritiesConstants.SSO_API_UID, principal.getName());
        } else if(StringUtils.isNotEmpty(request.getParameter(AuthoritiesConstants.SSO_API_KEYWORD))){
            principal = KeyTypePrincipal.builder().keyword(ssoAuthenticationRegister.decodeKeyword(request.getParameter(AuthoritiesConstants.SSO_API_KEYWORD), IAuthService.KeyType.valueOf(request.getParameter(AuthoritiesConstants.SSO_API_KEYTYPE)), appcode))
                    .keyType(IAuthService.KeyType.valueOf(request.getParameter(AuthoritiesConstants.SSO_API_KEYTYPE))).build();
            log.debug("SSO 单点认证过滤器从【{}】和【{}】提取到Principal为：【{}】", AuthoritiesConstants.SSO_API_KEYWORD, request.getParameter(AuthoritiesConstants.SSO_API_KEYTYPE),
                    principal.getName());
        }
        return principal;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        String appcode = request.getParameter(AuthoritiesConstants.SSO_API_APP_CODE);
        Principal principal = obtainPrincipal(request, appcode);
        log.debug("SSO 认证主体Principal【{}】即将访问应用【{}】的URL路径【{}】！", principal, appcode, request.getRequestURI());
        if (null == principal || StringUtils.isEmpty(appcode) || StringUtils.isEmpty(principal.getName())) {
            log.error("SSO 认证主体Principal【{}】在访问应用【{}】时不能为空，请求路径为【{}】！", principal, appcode, request.getRequestURI());
            throw new BadCredentialsException(
                    "SSO 认证主体Principal【"+principal+"】在访问应用【"+appcode+"】不能为空，请求路径为【"+request.getRequestURI()+"】！");
        }
        Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();
        if (AuthenticationUtil.authenticationIsRequired(existingAuth, principal.getName())) {
            SsoUsernameAuthentication ssoUsernameAuthentication = new SsoUsernameAuthentication(principal, appcode);
            return this.getAuthenticationManager().authenticate(ssoUsernameAuthentication);
        }
        return existingAuth;
    }





}
