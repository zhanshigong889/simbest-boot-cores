/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.oauth2;

import com.simbest.boot.security.auth.authentication.wxma.WxmaAuthenticationCredentials;
import com.simbest.boot.security.auth.authentication.wxma.WxmaMiniAuthenticationToken;
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
 * 用途：基于微信小程序的认证OAUTH2方式提交的认证主体(Token失效时使用)
 * 参考：ResourceOwnerPasswordTokenGranter http://www.voidcn.com/article/p-kkszxmkr-bqq.html
 * 作者: lishuyi
 * 时间: 2019/3/27  16:57
 *
 * 请求URL：http://10.87.57.23:6004/ischool/oauth/token?grant_type=wxmamini&scope=all&client_id=wxma_client&client_secret=e10adc3949ba59abbe56e057f20f883e&appcode=ischool&appid=wxa0d10b26a0d997c1&wxcode=微信换取的code&encryptedData=微信encryptedData&iv=微信iv&forceCreate=false
 *
 */
@Slf4j
public class WxmaMiniTokenGranter extends Oauth2ExtendTokenGranter {

    private static final String GRANT_TYPE = "wxmamini";

    private final AuthenticationManager authenticationManager;

    public WxmaMiniTokenGranter(AuthenticationManager authenticationManager,
                                AuthorizationServerTokenServices tokenServices, ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory) {
        this(authenticationManager, tokenServices, clientDetailsService, requestFactory, GRANT_TYPE);
    }

    protected WxmaMiniTokenGranter(AuthenticationManager authenticationManager, AuthorizationServerTokenServices tokenServices,
                                   ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory, String grantType) {
        super(tokenServices, clientDetailsService, requestFactory, grantType);
        this.authenticationManager = authenticationManager;
        super.setAuthenticationManager(authenticationManager);
    }

    /**
     * 抛出的异常将在CustomWebResponseExceptionTranslator进行处理
     * @param client
     * @param tokenRequest
     * @return OAuth2Authentication
     */
    @Override
    protected OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest tokenRequest) {
        Map<String, String> parameters = new LinkedHashMap<String, String>(tokenRequest.getRequestParameters());
        String appcode = parameters.get("appcode");
        String appid = parameters.get("appid");
        String wxcode = parameters.get("wxcode");
        String encryptedData = parameters.get("encryptedData");
        String iv = parameters.get("iv");
        Boolean forceCreate = Boolean.valueOf(parameters.get("forceCreate"));
        log.debug("OAUTH2即将通过WxmaMiniTokenGranter进行认证，appcode【{}】appid【{}】wxcode【{}】encryptedData【{}】iv【{}】",
               appcode, appid, wxcode, encryptedData, iv);
        Authentication userAuth = new WxmaMiniAuthenticationToken(appid, WxmaAuthenticationCredentials.builder()
                .appcode(appcode).appid(appid).wxcode(wxcode).encryptedData(encryptedData).iv(iv).forceCreate(forceCreate).build());
        ((AbstractAuthenticationToken) userAuth).setDetails(parameters);
        return this.genericOAuth2Authentication(userAuth, client, tokenRequest);
    }

}
