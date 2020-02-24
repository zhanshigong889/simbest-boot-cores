/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.oauth2;

import com.simbest.boot.base.exception.Exceptions;
import com.simbest.boot.constants.AuthoritiesConstants;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.token.AbstractTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

/**
 * 用途：抽象的AbstractTokenGranter
 * 参考：ResourceOwnerPasswordTokenGranter http://www.voidcn.com/article/p-kkszxmkr-bqq.html
 * 作者: lishuyi
 * 时间: 2020/2/15  16:57
 */
@Slf4j
public abstract class Oauth2ExtendTokenGranter extends AbstractTokenGranter {

    @Setter
    private AuthenticationManager authenticationManager;

    protected Oauth2ExtendTokenGranter(AuthorizationServerTokenServices tokenServices, ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory, String grantType) {
        super(tokenServices, clientDetailsService, requestFactory, grantType);
    }

    protected OAuth2Authentication genericOAuth2Authentication(Authentication userAuth, ClientDetails client, TokenRequest tokenRequest) {
        try {
            userAuth = authenticationManager.authenticate(userAuth);
        }
        catch (OAuth2Exception e){
            log.error("OAUTH2 通用认证器认证发生错误【{}】", e.getMessage());
            Exceptions.printException(e);
            throw new InvalidGrantException(e.getMessage());
        }
        catch (AccountStatusException e) {
            log.error("OAUTH2 通用认证器认证发生错误【{}】", e.getMessage());
            Exceptions.printException(e);
            throw new InvalidGrantException(e.getMessage());
        }
        catch (BadCredentialsException e) {
            log.error("OAUTH2 通用认证器认证发生错误【{}】", e.getMessage());
            Exceptions.printException(e);
            throw new InvalidGrantException(e.getMessage());
        }
        catch (AuthenticationException e){
            log.error("OAUTH2 通用认证器认证发生错误【{}】", e.getMessage());
            Exceptions.printException(e);
            throw new InvalidGrantException(e.getMessage());
        }
        catch (Exception e){
            log.error("OAUTH2 通用认证器认证发生错误【{}】", e.getMessage());
            Exceptions.printException(e);
            throw new InvalidGrantException(AuthoritiesConstants.BadCredentialsException);
        }
        if (userAuth == null || !userAuth.isAuthenticated()) {
            throw new InvalidGrantException(AuthoritiesConstants.BadCredentialsException);
        }
        OAuth2Request storedOAuth2Request = getRequestFactory().createOAuth2Request(client, tokenRequest);
        return new OAuth2Authentication(storedOAuth2Request, userAuth);
    }

}
