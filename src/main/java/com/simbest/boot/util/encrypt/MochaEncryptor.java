/**
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.util.encrypt;


import com.mochasoft.portal.encrypt.EncryptorUtil;
import com.simbest.boot.base.exception.Exceptions;
import com.simbest.boot.config.AppConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;

/**
 * 用途：摩卡加密工具类
 * 作者: lishuyi 
 * 时间: 2017/12/28  22:11 
 */
@Slf4j
@Component
public class MochaEncryptor extends AbstractEncryptor {
    private final static Integer TIMEOUT = 1800;

    @Autowired
    private AppConfig config;

    /**
     *
     * @param source 普通文本
     * @return code
     */
    @Override
    protected String encryptSource(String source) {
        String result = null;
        try {
            result = EncryptorUtil.encode(config.getMochaPortalToken(), source);
        } catch (Exception e) {
            Exceptions.printException(e);
        }
        return result;
    }

    /**
     *
     * @param code 加密文本
     * @return value
     */
    @Override
    protected String decryptCode(String code) {
        String result = null;
        try {
            result = EncryptorUtil.decode(config.getMochaPortalToken(), code, TIMEOUT);// 解密
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public static void main(String[] args) throws ParseException {
        String code = EncryptorUtil.encode("SIMBEST_SSO", "hadmin3");
        System.out.println(code);
        System.out.println(EncryptorUtil.decode("SIMBEST_SSO", code, TIMEOUT));
    }

}
