/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.oauth2;

import com.simbest.boot.base.exception.Exceptions;
import com.simbest.boot.constants.AuthoritiesConstants;
import com.simbest.boot.security.auth.provider.GenericAuthenticationChecker;
import com.simbest.boot.util.encrypt.RsaEncryptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.token.AbstractTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_PASSWORD_KEY;
import static org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY;

/**
 * 用途：对password进行RSA加密后认证OAUTH2方式提交的认证主体
 * 参考：ResourceOwnerPasswordTokenGranter http://www.voidcn.com/article/p-kkszxmkr-bqq.html
 * 作者: lishuyi
 * 时间: 2020/2/15  16:57
 */
@Slf4j
public class RsaPasswordTokenGranter extends AbstractTokenGranter {

    private static final String GRANT_TYPE = "rsapassword";

    private final AuthenticationManager authenticationManager;

    private RsaEncryptor encryptor;

    private GenericAuthenticationChecker genericAuthenticationChecker;

    public RsaPasswordTokenGranter(AuthenticationManager authenticationManager, AuthorizationServerTokenServices tokenServices,
                                   ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory,
                                   RsaEncryptor encryptor, GenericAuthenticationChecker genericAuthenticationChecker) {
        this(authenticationManager, tokenServices, clientDetailsService, requestFactory, GRANT_TYPE, encryptor, genericAuthenticationChecker);
    }

    protected RsaPasswordTokenGranter(AuthenticationManager authenticationManager, AuthorizationServerTokenServices tokenServices,
                                      ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory, String grantType,
                                      RsaEncryptor encryptor, GenericAuthenticationChecker genericAuthenticationChecker) {
        super(tokenServices, clientDetailsService, requestFactory, grantType);
        this.authenticationManager = authenticationManager;
        this.encryptor = encryptor;
        this.genericAuthenticationChecker = genericAuthenticationChecker;
    }

    @Override
    protected OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest tokenRequest) {
        Map<String, String> parameters = new LinkedHashMap<>(tokenRequest.getRequestParameters());
        String username = parameters.get(SPRING_SECURITY_FORM_USERNAME_KEY);
        String password = encryptor.decrypt(parameters.get(SPRING_SECURITY_FORM_PASSWORD_KEY));
        //解决：http空格传输、加号传输、Base64加号变空格问题
        //参考：https://blog.csdn.net/zc375039901/article/details/80443574
        password = StringUtils.replace(password, " ", "+");
        // Protect from downstream leaks of password
        parameters.remove(SPRING_SECURITY_FORM_PASSWORD_KEY);
        String appcode = parameters.get("appcode");
        log.debug("OAUTH2即将通过UsernamePasswordAuthenticationToken进行认证，用户名【{}】密码【{}】应用【{}】", username, password, appcode);
        Authentication userAuth = new UsernamePasswordAuthenticationToken(username, password);
        ((AbstractAuthenticationToken) userAuth).setDetails(parameters);
        try {
            userAuth = authenticationManager.authenticate(userAuth);
        }
        catch (AuthenticationException e){
            log.error("OAUTH2 通用认证器认证发生错误【{}】", e.getMessage());
            Exceptions.printException(e);
            throw new BadCredentialsException(AuthoritiesConstants.BadCredentialsException);
        }
        catch (OAuth2Exception e){
            log.error("OAUTH2 通用认证器认证发生错误【{}】", e.getMessage());
            Exceptions.printException(e);
            throw new BadCredentialsException(AuthoritiesConstants.BadCredentialsException);
        }
        if (userAuth == null || !userAuth.isAuthenticated()) {
            throw new BadCredentialsException(AuthoritiesConstants.BadCredentialsException);
        }
        else{
            userAuth = genericAuthenticationChecker.authChek(userAuth, appcode);
        }
        OAuth2Request storedOAuth2Request = getRequestFactory().createOAuth2Request(client, tokenRequest);
        return new OAuth2Authentication(storedOAuth2Request, userAuth);
    }

}
