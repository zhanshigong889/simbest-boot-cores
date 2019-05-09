/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.provider.sso.service.impl;

import com.simbest.boot.security.auth.provider.sso.service.SsoAuthenticationService;
import com.simbest.boot.util.encrypt.AbstractEncryptor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * 用途：抽象加密名称单点登录验证服务
 * 作者: lishuyi 
 * 时间: 2018/1/20  15:06 
 */
@Slf4j
@Data
@NoArgsConstructor
public abstract class AbstractEncryptorSsoAuthenticationService implements SsoAuthenticationService {

    private AbstractEncryptor encryptor;

    @Override
    public String decryptKeyword(String encodeKeyword) {
        String decodeKeyword = null;
        if(StringUtils.isNotEmpty(encodeKeyword)){
            try {
                decodeKeyword = this.getEncryptor().decrypt(encodeKeyword);
                log.debug("SSO解密服务【{}】解密用户名为【{}】", this.getClass().getSimpleName(), decodeKeyword);
            } catch (Exception e) {
                log.debug("SSO解密服务【{}】解密密钥【{}】发生【{}】异常", this.getClass().getSimpleName(), encodeKeyword, e.getMessage());
            }
        }
        return decodeKeyword;
    }

}
