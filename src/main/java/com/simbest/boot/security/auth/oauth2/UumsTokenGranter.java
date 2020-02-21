/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.oauth2;

import com.simbest.boot.security.auth.authentication.UumsAuthentication;
import com.simbest.boot.security.auth.authentication.UumsAuthenticationCredentials;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
 * 用途：基于UUMS认证OAUTH2方式提交的认证主体
 * 参考：ResourceOwnerPasswordTokenGranter http://www.voidcn.com/article/p-kkszxmkr-bqq.html
 * 作者: lishuyi
 * 时间: 2019/3/27  16:57
 */
@Slf4j
public class UumsTokenGranter extends Oauth2ExtendTokenGranter {

    private static final String GRANT_TYPE = "uumspassword";

    private final AuthenticationManager authenticationManager;

    public UumsTokenGranter(AuthenticationManager authenticationManager,
                                             AuthorizationServerTokenServices tokenServices, ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory) {
        this(authenticationManager, tokenServices, clientDetailsService, requestFactory, GRANT_TYPE);
    }

    protected UumsTokenGranter(AuthenticationManager authenticationManager, AuthorizationServerTokenServices tokenServices,
                                                ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory, String grantType) {
        super(tokenServices, clientDetailsService, requestFactory, grantType);
        this.authenticationManager = authenticationManager;
        super.setAuthenticationManager(authenticationManager);
    }

    @Override
    protected OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest tokenRequest) {
        Map<String, String> parameters = new LinkedHashMap<String, String>(tokenRequest.getRequestParameters());
        String username = parameters.get("username");
        String password = parameters.get("password");
        //解决：http空格传输、加号传输、Base64加号变空格问题
        //参考：https://blog.csdn.net/zc375039901/article/details/80443574
        password = StringUtils.replace(password, " ", "+");
        String appcode = parameters.get("appcode");
        // Protect from downstream leaks of password
        parameters.remove("password");
        log.debug("OAUTH2即将通过UumsAuthentication进行认证，用户名【{}】密码【{}】应用【{}】", username, password, appcode);
        Authentication userAuth = new UumsAuthentication(username, UumsAuthenticationCredentials.builder()
                .password(password).appcode(appcode).build());
        ((AbstractAuthenticationToken) userAuth).setDetails(parameters);
        return this.genericOAuth2Authentication(userAuth, client, tokenRequest);
    }

}
