/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.provider.sso.service.impl;

import com.mochasoft.portal.encrypt.EncryptorUtil;
import com.simbest.boot.config.AppConfig;
import com.simbest.boot.security.auth.provider.sso.service.SsoAuthenticationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;

/**
 * 用途：门户Portal单点登录验证服务
 * 作者: lishuyi 
 * 时间: 2018/1/20  15:06 
 */
@Slf4j
@Component
public class MochaSsoAuthenticationServiceImpl extends AbstractEncryptorSsoAuthenticationService {

    private final static Integer TIMEOUT = 1800;

    @Autowired
    private AppConfig config;

    @Override
    public String decryptKeyword(String encodeKeyword) {
        String decodeKeyword = null;
        if(StringUtils.isNotEmpty(encodeKeyword)){
            try {
                decodeKeyword = EncryptorUtil.decode(config.getMochaPortalToken(), encodeKeyword, TIMEOUT);
            } catch (Exception e) {
                log.debug("SSO解密服务【{}】解密密钥【{}】， 解密后用户名为【{}】, 发生【{}】异常", this.getClass().getSimpleName(), encodeKeyword, decodeKeyword, e.getMessage());
            }
        }
        return decodeKeyword;
    }

    @Override
    public int compareTo(SsoAuthenticationService o) {
        return this.getOrder().compareTo(o.getOrder());
    }

    @Override
    public Integer getOrder() {
        return ORDER_MOCHA;
    }

    public static void main(String[] args) {
        System.out.println(EncryptorUtil.encode("SIMBEST_SSO", "xindanhua"));
        try {
            System.out.println(EncryptorUtil.decode("SIMBEST_SSO", "09EC2988F6BC63DDA752A7007FEDF2E47F274DE24BD8C47138D7EA8BE76AE65F9B77AE78D4F51A3938C8BE09AF1CCF8751A72BA86A58389DFE2DBBCB7D18C36F", TIMEOUT));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
