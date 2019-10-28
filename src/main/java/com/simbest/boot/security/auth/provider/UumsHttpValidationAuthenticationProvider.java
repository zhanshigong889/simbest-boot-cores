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
import com.simbest.boot.security.auth.service.IAuthUserCacheService;
import com.simbest.boot.util.encrypt.RsaEncryptor;
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
                    log.debug("用户【{}】即将通过凭证【{}】访问应用【{}】，即将到UUMS远程认证", username, password, appcode);
                    JsonResponse response = HttpClient.post(config.getUumsAddress() + UUMS_URL)
                            .param(AuthoritiesConstants.SSO_UUMS_USERNAME, username)
                            .param(AuthoritiesConstants.SSO_UUMS_PASSWORD, password)
                            .param(AuthoritiesConstants.SSO_API_APP_CODE, appcode)
                            .asBean(JsonResponse.class);
                    if (!response.getErrcode().equals(JsonResponse.SUCCESS_CODE)) {
                        authResult = false;
                    }else{
                        authUserCacheService.saveOrUpdateCacheUserPassword(username, passwordDecode, true);
                    }
                }
                if(authResult){
                    log.info(LOGTAG + "UUMS认证用户 【{}】 访问 【{}】 成功！", principal, uumsCredentials.getAppcode());
                    UumsAuthentication uumsAuthentication = new UumsAuthentication(username, UumsAuthenticationCredentials.builder()
                            .password(password).appcode(appcode).build());
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
