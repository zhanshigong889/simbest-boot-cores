/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.oauth2;

import com.simbest.boot.security.auth.authentication.wxma.WxmaAuthenticationCredentials;
import com.simbest.boot.security.auth.authentication.wxma.WxmaBindAuthenticationToken;
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
 * 用途：基于微信小程序的认证OAUTH2方式提交的认证主体(首次注册绑定)
 * 参考：ResourceOwnerPasswordTokenGranter http://www.voidcn.com/article/p-kkszxmkr-bqq.html
 * 作者: lishuyi
 * 时间: 2019/3/27  16:57
 *
 * 请求URL：http://10.87.57.23:6004/ischool/oauth/token?grant_type=wxmabind&scope=all&client_id=wxma_client&client_secret=e10adc3949ba59abbe56e057f20f883e&appcode=ischool&appid=wxa0d10b26a0d997c1&wxcode=微信换取的code&preferredMobile=13111111111&smscode=测试环境固定1111
 *
 */
@Slf4j
public class WxmaBindTokenGranter extends Oauth2ExtendTokenGranter {

    private static final String GRANT_TYPE = "wxmabind";

    private final AuthenticationManager authenticationManager;

    public WxmaBindTokenGranter(AuthenticationManager authenticationManager,
                                AuthorizationServerTokenServices tokenServices, ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory) {
        this(authenticationManager, tokenServices, clientDetailsService, requestFactory, GRANT_TYPE);
    }

    protected WxmaBindTokenGranter(AuthenticationManager authenticationManager, AuthorizationServerTokenServices tokenServices,
                                   ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory, String grantType) {
        super(tokenServices, clientDetailsService, requestFactory, grantType);
        this.authenticationManager = authenticationManager;
        super.setAuthenticationManager(authenticationManager);
    }

    @Override
    protected OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest tokenRequest) {
        Map<String, String> parameters = new LinkedHashMap<String, String>(tokenRequest.getRequestParameters());
        String preferredMobile = parameters.get("preferredMobile");
        String appcode = parameters.get("appcode");
        String appid = parameters.get("appid");
        String smscode = parameters.get("smscode");
        String wxcode = parameters.get("wxcode");
        log.debug("OAUTH2即将通过WxmaBindTokenGranter进行认证，preferredMobile【{}】appcode【{}】appid【{}】smscode【{}】wxcode【{}】",
                preferredMobile, appcode, appid, smscode, wxcode);
        Authentication userAuth = new WxmaBindAuthenticationToken(preferredMobile, WxmaAuthenticationCredentials.builder()
                .appcode(appcode).appid(appid).smscode(smscode).wxcode(wxcode).build());
        ((AbstractAuthenticationToken) userAuth).setDetails(parameters);
        return this.genericOAuth2Authentication(userAuth, client, tokenRequest);
    }

}
