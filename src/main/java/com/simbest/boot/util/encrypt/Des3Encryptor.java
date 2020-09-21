/**
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.util.encrypt;


import cn.hutool.core.util.StrUtil;
import com.simbest.boot.base.exception.Exceptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.security.Key;
import java.util.Base64;

/**
 * 用途：3DES加密工具类
 * 作者: lishuyi 
 * 时间: 2017/12/28  22:11 
 */
@Slf4j
@Component
public class Des3Encryptor extends AbstractEncryptor {

    // 密钥
    private static final String SECRETKEY = "2018#@SimBest$#soft0803#$@";
    // 向量
    private static final String VECTOR = "60880301";

    private static final String SPACE = " ";

    private static final String ADD = "+";

    public static void main(String[] args) throws Exception {
        Des3Encryptor encryptor = new Des3Encryptor();
        log.debug(encryptor.encrypt("koushaoguo"));
        log.debug(encryptor.decrypt(encryptor.encrypt("koushaoguo")));
    }

    /**
     * 3DES加密
     *
     * @param source 普通文本
     * @return code
     */
    @Override
    protected String encryptSource(String source) {
        String result = null;
        try {
            Key deskey = null;
            DESedeKeySpec spec = new DESedeKeySpec(SECRETKEY.getBytes());
            SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("desede");
            deskey = keyfactory.generateSecret(spec);

            Cipher cipher = Cipher.getInstance("desede/CBC/PKCS5Padding");
            IvParameterSpec ips = new IvParameterSpec(VECTOR.getBytes());
            cipher.init(Cipher.ENCRYPT_MODE, deskey, ips);
            byte[] encryptData = cipher.doFinal(source.getBytes(DEFAULT_URL_ENCODING));
            result = Base64.getEncoder().encodeToString(encryptData);
        } catch (Exception e) {
            Exceptions.printException(e);
        }
        return result;
    }

    /**
     * 3DES解密
     *
     * @param code 加密文本
     * @return value
     */
    @Override
    protected String decryptCode(String code) {
        String result = null;
        try {
            if (StrUtil.contains(code,SPACE)){
                code = StrUtil.replaceIgnoreCase(code,SPACE,ADD);
            }
            Key deskey = null;
            DESedeKeySpec spec = new DESedeKeySpec(SECRETKEY.getBytes());
            SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("desede");
            deskey = keyfactory.generateSecret(spec);
            Cipher cipher = Cipher.getInstance("desede/CBC/PKCS5Padding");
            IvParameterSpec ips = new IvParameterSpec(VECTOR.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, deskey, ips);
            byte[] decryptData = cipher.doFinal(Base64.getDecoder().decode(code));
            result =  new String(decryptData, DEFAULT_URL_ENCODING);
        } catch (Exception e) {
            log.warn("解密【{}】失败", code);
            throw new RuntimeException(e);
        }
        return result;
    }



}
