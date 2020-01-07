/**
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.util.encrypt;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * 用途：Base64加密工具类
 * 作者: lishuyi 
 * 时间: 2017/12/28  22:11 
 */
@Slf4j
@Component
public class Base64Encryptor extends AbstractEncryptor {

    public static void main(String[] args) {
        Base64Encryptor encryptor = new Base64Encryptor();
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
        try {
            byte[] binaryData = source.getBytes();
            String base64String = Base64.encodeBase64String(binaryData);
            return base64String;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param code 加密文本
     * @return value
     */
    @Override
    protected String decryptCode(String code) {
        try {
            byte[] binaryData = Base64.decodeBase64(code);
            String string = new String(binaryData,DEFAULT_URL_ENCODING);
            return string;
        } catch (Exception e) {
            log.warn("解密【{}】失败", code);
            throw new RuntimeException(e);
        }
    }

    public static String fileToBase64(File file) throws Exception {
        Base64 b64 = new Base64();
        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[(int)file.length()];
        fis.read(buffer);
        fis.close();
        return b64.encodeToString(buffer);
    }

    public static void getFileByString(String string, String filePath) throws Exception {
        Base64 b64 = new Base64();
        byte[] buffer = b64.decode(string);
        FileOutputStream fos = new FileOutputStream(filePath);
        fos.write(buffer);
        fos.close();
    }

}
