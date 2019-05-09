/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.provider.sso.service.impl;

import com.simbest.boot.security.auth.provider.sso.service.SsoAuthenticationService;
import com.simbest.boot.util.encrypt.RsaEncryptor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 用途：RSA加密名称单点登录验证服务
 * 作者: lishuyi 
 * 时间: 2018/1/20  15:06 
 */
@Slf4j
@Data
@Component
public class RsaSsoAuthenticationServiceImpl extends AbstractEncryptorSsoAuthenticationService {

    private RsaEncryptor encryptor;

    @Autowired
    public RsaSsoAuthenticationServiceImpl(RsaEncryptor encryptor) {
        this.encryptor = encryptor;
    }


    @Override
    public int compareTo(SsoAuthenticationService o) {
        return this.getOrder().compareTo(o.getOrder());
    }

    @Override
    public Integer getOrder() {
        return ORDER_RSA;
    }
}
