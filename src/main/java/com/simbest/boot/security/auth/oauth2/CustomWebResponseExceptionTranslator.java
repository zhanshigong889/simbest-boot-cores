/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.oauth2;

import com.simbest.boot.constants.ApplicationConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.error.DefaultWebResponseExceptionTranslator;
import org.springframework.stereotype.Component;

/**
 * 用途：自定义OAUTH2受保护的资源请求异常
 * 作者: lishuyi
 * 时间: 2018/8/29  21:28
 */
@Component
public class CustomWebResponseExceptionTranslator extends DefaultWebResponseExceptionTranslator {

    @Override
    public ResponseEntity<OAuth2Exception> translate(Exception e) {
        if(e instanceof OAuth2Exception) {
            OAuth2Exception oAuth2Exception = (OAuth2Exception) e;
            if(StringUtils.isNotEmpty(oAuth2Exception.getMessage())){
                String[] result = StringUtils.split(oAuth2Exception.getMessage(), ApplicationConstants.VERTICAL);
                if(null != result && result.length == 2){
                    return ResponseEntity
                            .status(oAuth2Exception.getHttpErrorCode())
                            .body(new CustomOauthException(result[0], result[1]));
                }
            }
            return ResponseEntity
                    .status(oAuth2Exception.getHttpErrorCode())
                    .body(new CustomOauthException(oAuth2Exception.getMessage()));
        }
        else {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(new OAuth2Exception(e.getMessage()));
        }
    }
}
