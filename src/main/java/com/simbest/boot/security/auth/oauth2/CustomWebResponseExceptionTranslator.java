/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.oauth2;

import com.simbest.boot.constants.ApplicationConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.error.DefaultWebResponseExceptionTranslator;
import org.springframework.stereotype.Component;

import static com.simbest.boot.constants.ApplicationConstants.ONE;
import static com.simbest.boot.constants.ApplicationConstants.ZERO;
import static com.simbest.boot.security.auth.oauth2.CustomOauthException.OAUTH2_FORBIDDEN;

/**
 * 用途：自定义OAUTH2受保护的资源请求异常
 * 作者: lishuyi
 * 时间: 2018/8/29  21:28
 */
@Slf4j
@Component
public class CustomWebResponseExceptionTranslator extends DefaultWebResponseExceptionTranslator {

    /**
     * 异常将由CustomOauthExceptionSerializer进行处理
     * @param e
     * @return ResponseEntity
     */
    @Override
    public ResponseEntity<OAuth2Exception> translate(Exception e) {
        log.warn("OAuth2 认证过程出了点问题，即将组装返回的错误信息【{}】", e.getMessage());
        if(e instanceof OAuth2Exception) {
            OAuth2Exception oAuth2Exception = (OAuth2Exception) e;
            if(StringUtils.isNotEmpty(oAuth2Exception.getMessage())){
                String[] result = StringUtils.split(oAuth2Exception.getMessage(), ApplicationConstants.VERTICAL);
                if(null != result && result.length == 2){
                    return ResponseEntity
                            .status(oAuth2Exception.getHttpErrorCode())
                            //解析WxmaCodeAuthenticationProvider和WxmaMiniAuthenticationProvider抛出在CustomOauthException定义的OAUTH2_LOGIN_ERROR和OAUTH2_MINI_ERROR错误
                            .body(new CustomOauthException(result[ZERO], result[ONE]));
                }
            }
            return ResponseEntity
                    .status(oAuth2Exception.getHttpErrorCode())
                    .body(new CustomOauthException(oAuth2Exception.getMessage()));
        }
        else {
            return ResponseEntity
                    .status(Integer.parseInt(OAUTH2_FORBIDDEN))
                    .body(new OAuth2Exception(e.getMessage()));
        }
    }
}
