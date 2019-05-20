/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.provider;

import com.mzlion.easyokhttp.HttpClient;
import com.mzlion.easyokhttp.exception.HttpClientException;
import com.simbest.boot.base.web.response.JsonResponse;
import com.simbest.boot.config.AppConfig;
import com.simbest.boot.constants.AuthoritiesConstants;
import com.simbest.boot.constants.ErrorCodeConstants;
import com.simbest.boot.exceptions.AccesssAppDeniedException;
import com.simbest.boot.exceptions.AttempMaxLoginFaildException;
import com.simbest.boot.security.IUser;
import com.simbest.boot.security.auth.authentication.UumsAuthentication;
import com.simbest.boot.security.auth.authentication.UumsAuthenticationCredentials;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Object principal = authentication.getPrincipal();
        Object credentials = authentication.getCredentials();
        if (null != principal && credentials != null) {
            IUser authUser = null;
            try {
                UumsAuthenticationCredentials uumsCredentials = (UumsAuthenticationCredentials)credentials;
                String username = principal.toString();
                String password = uumsCredentials.getPassword();
                String appcode = uumsCredentials.getAppcode();
                log.debug("用户【{}】即将通过凭证【{}】访问应用【{}】", username, password, appcode);
                JsonResponse response = HttpClient.post(config.getUumsAddress() + UUMS_URL)
                        .param(AuthoritiesConstants.SSO_UUMS_USERNAME, username)
                        .param(AuthoritiesConstants.SSO_UUMS_PASSWORD, password)
                        .param(AuthoritiesConstants.SSO_API_APP_CODE, appcode)
                        .asBean(JsonResponse.class);
                if(!response.getErrcode().equals(JsonResponse.SUCCESS_CODE)) {
                    log.warn(LOGTAG + "UUMS认证用户 【{}】 访问 【{}】 失败, 错误信息: 【{}】", principal, uumsCredentials.getAppcode(), ErrorCodeConstants.LOGIN_ERROR_BAD_CREDENTIALS);
                    throw new
                            BadCredentialsException(ErrorCodeConstants.LOGIN_ERROR_BAD_CREDENTIALS);
                }else {
                    log.info(LOGTAG + "UUMS认证用户 【{}】 访问 【{}】 成功！", principal, uumsCredentials.getAppcode());
                    UumsAuthentication uumsAuthentication = new UumsAuthentication(username, UumsAuthenticationCredentials.builder()
                            .password(password).appcode(appcode).build());
                    return genericAuthenticationChecker.authChek(authentication, appcode);
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
            catch (HttpClientException e){
                log.warn(LOGTAG + "UUMS认证 【{}】 失败， 捕获【{}】异常!", principal, e.getMessage());
                throw new
                        BadCredentialsException(principal + ErrorCodeConstants.LOGIN_ERROR_BAD_CREDENTIALS);
            }catch (Exception e){
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
