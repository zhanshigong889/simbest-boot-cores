/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.oauth2;

import com.simbest.boot.security.auth.authentication.PhoneAuthenticationToken;
import com.simbest.boot.security.auth.authentication.PhoneCodeAuthenticationCredentials;
import com.simbest.boot.security.auth.authentication.PhoneCodeAuthenticationToken;
import com.simbest.boot.util.encrypt.RsaEncryptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 用途：基于RSA加密手机号码与短信的认证OAUTH2方式提交的认证主体
 * 参考：ResourceOwnerPasswordTokenGranter http://www.voidcn.com/article/p-kkszxmkr-bqq.html
 * 作者: lishuyi
 * 时间: 2019/3/27  16:57
 *
 * 请求URL：http://10.87.57.23:6004/ischool/oauth/token?grant_type=wxmacode&scope=all&client_id=wxma_client&client_secret=e10adc3949ba59abbe56e057f20f883e&appcode=ischool&appid=wxa0d10b26a0d997c1&wxcode=微信换取的code
 */
@Slf4j
public class PhoneCodeTokenGranter extends Oauth2ExtendTokenGranter {

    private static final String GRANT_TYPE = "phonecode";

    private final AuthenticationManager authenticationManager;

    private RsaEncryptor encryptor;

    public PhoneCodeTokenGranter(AuthenticationManager authenticationManager, AuthorizationServerTokenServices tokenServices,
                                 ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory,
                                 RsaEncryptor encryptor) {
        this(authenticationManager, tokenServices, clientDetailsService, requestFactory, encryptor, GRANT_TYPE);
    }

    protected PhoneCodeTokenGranter(AuthenticationManager authenticationManager, AuthorizationServerTokenServices tokenServices,
                                    ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory,
                                    RsaEncryptor encryptor, String grantType) {
        super(tokenServices, clientDetailsService, requestFactory, grantType);
        this.authenticationManager = authenticationManager;
        this.encryptor = encryptor;
        super.setAuthenticationManager(authenticationManager);
    }

    @Override
    protected OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest tokenRequest) {
        Map<String, String> parameters = new LinkedHashMap<String, String>(tokenRequest.getRequestParameters());
        String appcode = parameters.get("appcode");
        String phone = encryptor.decrypt(parameters.get("phone"));
        String smscode = parameters.get("smscode");
        log.debug("OAUTH2即将通过PhoneCodeTokenGranter进行认证，appcode【{}】phone【{}】", appcode, phone);
        Authentication userAuth = new PhoneCodeAuthenticationToken(phone, PhoneCodeAuthenticationCredentials.builder()
        .appcode(appcode).smscode(smscode).build());
        ((AbstractAuthenticationToken) userAuth).setDetails(parameters);
        return this.genericOAuth2Authentication(userAuth, client, tokenRequest);
    }

}
