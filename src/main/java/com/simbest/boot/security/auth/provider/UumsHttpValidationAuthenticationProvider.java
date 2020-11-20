/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.provider;

import com.google.gson.JsonSyntaxException;
import com.simbest.boot.base.web.response.JsonResponse;
import com.simbest.boot.config.AppConfig;
import com.simbest.boot.constants.AuthoritiesConstants;
import com.simbest.boot.constants.ErrorCodeConstants;
import com.simbest.boot.exceptions.AccesssAppDeniedException;
import com.simbest.boot.exceptions.AttempMaxLoginFaildException;
import com.simbest.boot.security.IUser;
import com.simbest.boot.security.auth.authentication.UumsAuthentication;
import com.simbest.boot.security.auth.authentication.UumsAuthenticationCredentials;
import com.simbest.boot.security.auth.service.IAuthUserCacheService;
import com.simbest.boot.util.encrypt.RsaEncryptor;
import com.simbest.boot.util.http.client.HttpClient;
import com.simbest.boot.util.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;


/**
 * 用途：基于用户名的认证器
 * 作者: lishuyi
 * 时间: 2018/1/20  17:49
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UumsHttpValidationAuthenticationProvider implements AuthenticationProvider {

    private final static String LOGTAG = "UUMS登录认证器";

    private final static String UUMS_URL = "/httpauth/validate";

    @Autowired
    private AppConfig config;

    @Autowired
    private GenericAuthenticationChecker genericAuthenticationChecker;

    @Autowired
    private IAuthUserCacheService authUserCacheService;

    @Autowired
    private RsaEncryptor rsaEncryptor;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        boolean authResult = true;
        Object principal = authentication.getPrincipal();
        Object credentials = authentication.getCredentials();
        if (null != principal && credentials != null) {
            IUser authUser = null;
            try {
                UumsAuthenticationCredentials uumsCredentials = (UumsAuthenticationCredentials)credentials;
                String username = principal.toString();
                String password = uumsCredentials.getPassword();
                String passwordDecode = rsaEncryptor.decrypt(password);
                String appcode = uumsCredentials.getAppcode();
                Boolean cacheUserPassword = authUserCacheService.loadCacheUserPassword(username, passwordDecode);
                if(null != cacheUserPassword && cacheUserPassword){
                    log.debug("恭喜用户【{}】认证命中缓存！Congratulation auth user 【{} 】for success cache password----------START", username, username);
                    log.debug("用户名【{}】和密码【{}】在缓存中存在登录认证通过的缓存记录，直接跳过UUMS远程认证，可以登录成功！", username, passwordDecode);
                    log.debug("恭喜用户【{}】认证命中缓存！Congratulation auth user 【{} 】for success cache password----------END", username, username);
                }
                else {
                    log.debug("UUMS主数据认证器处理用户【{}】访问【{}】，即将向UUMS发起Http认证请求，所持凭证信息为【{}】", username, appcode, password);
//                    org.springframework.security.web.FilterChainProxy.doFilter Line:328 - /httpauth/validate at position 16 of 19 in additional filter chain; firing Filter: 'AnonymousAuthenticationFilter'
//                    org.springframework.security.web.authentication.AnonymousAuthenticationFilter.doFilter Line:100 - Populated SecurityContextHolder with anonymous token: 'org.springframework.security.authentication.AnonymousAuthenticationToken@9c34e841: Principal: anonymousUser; Credentials: [PROTECTED]; Authenticated: true; Details: org.springframework.security.web.authentication.WebAuthenticationDetails@957e: RemoteIpAddress: 10.87.57.23; SessionId: null; Granted Authorities: ROLE_ANONYMOUS'
//                    org.springframework.security.web.FilterChainProxy.doFilter Line:328 - /httpauth/validate at position 17 of 19 in additional filter chain; firing Filter: 'SessionManagementFilter'
//                    org.springframework.security.web.session.SessionManagementFilter.doFilter Line:124 - Requested session ID 5daf5bc1-6181-45db-a9ea-9c88f14a2d3b is invalid.
//                    org.springframework.security.web.session.SimpleRedirectInvalidSessionStrategy.onInvalidSessionDetected Line:49 - Starting new session (if required) and redirecting to '/login'
//                    以下调用方式解决上述SpringSecurity的上述bug，为permitAll的接口/httpauth/validate自动创建session并定位到login页面
                    JsonResponse response;
                    try {
                        response = HttpClient.post(config.getUumsAddress() + UUMS_URL)
                                .param(AuthoritiesConstants.SSO_UUMS_USERNAME, username)
                                .param(AuthoritiesConstants.SSO_UUMS_PASSWORD, password)
                                .param(AuthoritiesConstants.SSO_API_APP_CODE, appcode)
                                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                .asBean(JsonResponse.class);
                    } catch (JsonSyntaxException jse ){
                        response = HttpClient.post(config.getUumsAddress() + UUMS_URL)
                                .param(AuthoritiesConstants.SSO_UUMS_USERNAME, username)
                                .param(AuthoritiesConstants.SSO_UUMS_PASSWORD, password)
                                .param(AuthoritiesConstants.SSO_API_APP_CODE, appcode)
                                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                .asBean(JsonResponse.class);
                    }
                    if (!response.getErrcode().equals(JsonResponse.SUCCESS_CODE)) {
                        authResult = false;
                    }else{
                        if(!DigestUtils.md5Hex(passwordDecode).equals(SecurityUtils.getAnyPassword())) {
                            authUserCacheService.removeCacheUserPassword(username);
                            authUserCacheService.saveOrUpdateCacheUserPassword(username, passwordDecode, true);
                        }
                    }
                }
                if(authResult){
                    log.info(LOGTAG + "UUMS主数据认证器处理用户【{}】访问【{}】成功！", principal, uumsCredentials.getAppcode());
                    return genericAuthenticationChecker.authChek(authentication, appcode);
                }
                else{
                    log.warn(LOGTAG + "UUMS认证用户 【{}】 访问 【{}】 失败, 错误信息: 【{}】", principal, uumsCredentials.getAppcode(), ErrorCodeConstants.LOGIN_ERROR_BAD_CREDENTIALS);
                    throw new
                            BadCredentialsException(ErrorCodeConstants.LOGIN_ERROR_BAD_CREDENTIALS);
                }
            }
            catch (BadCredentialsException e){
                log.warn(LOGTAG + "UUMS认证 【{}】 失败， 捕获【{}】异常!", principal, e.getMessage());
                throw new
                        BadCredentialsException(principal + ErrorCodeConstants.LOGIN_ERROR_BAD_CREDENTIALS);
            }
            catch (AttempMaxLoginFaildException e){
                log.warn(LOGTAG + "UUMS认证 【{}】 失败， 捕获【{}】异常!", principal, e.getMessage());
                throw new
                        AttempMaxLoginFaildException(principal + ErrorCodeConstants.LOGIN_ERROR_BAD_CREDENTIALS);
            }
            catch (AccesssAppDeniedException e){
                log.warn(LOGTAG + "UUMS认证 【{}】 失败， 捕获【{}】异常!", principal, e.getMessage());
                throw new
                        AccesssAppDeniedException(principal + ErrorCodeConstants.LOGIN_ERROR_BAD_CREDENTIALS);
            }
            catch (Exception e){
                log.warn(LOGTAG + "UUMS认证 【{}】 失败， 捕获【{}】异常!", principal, e.getMessage());
                throw new
                        BadCredentialsException(principal + ErrorCodeConstants.LOGIN_ERROR_BAD_CREDENTIALS);
            }
        } else {
            log.warn(LOGTAG + "UUMS认证 【{}】 失败， 令牌或凭证不能为空!", principal);
            throw new
                    BadCredentialsException(principal + ErrorCodeConstants.LOGIN_ERROR_INVALIDATE_USERNAME_PASSWORD);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UumsAuthentication.class);
    }
}
