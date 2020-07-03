/**
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.util.encrypt;

import com.simbest.boot.constants.ApplicationConstants;
import com.simbest.boot.util.BootAppFileReader;
import com.simbest.boot.util.DateUtil;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

/**
 * RSA加密工具类
 * 参考： http://blog.csdn.net/chaijunkun/article/details/7275632
 * @author lishuyi
 * 时间: 2017/12/28  22:02 
 */
@Slf4j
@Component
public class RsaEncryptor extends AbstractEncryptor {

    /**
     * 私钥
     */
    private RSAPrivateKey privateKey;

    /**
     * 公钥
     */
    private RSAPublicKey publicKey;

    @PostConstruct
    public void init() throws Exception {
        String public_key = getKeyFromFile(ApplicationConstants.RSA_PUBLIC_KEY_PATH);
        String private_key = getKeyFromFile(ApplicationConstants.RSA_PRIVATE_KEY_PATH);
        loadPublicKey(public_key);
        loadPrivateKey(private_key);
    }

    private String getKeyFromFile(String filePath) throws Exception {
        BufferedReader bufferedReader = BootAppFileReader.getClasspathFile(filePath);
        String line = null;
        List<String> list = new ArrayList<String>();
        while ((line = bufferedReader.readLine()) != null) {
            list.add(line);
        }
        // remove the firt line and last line
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 1; i < list.size() - 1; i++) {
            stringBuilder.append(list.get(i)).append("\r");
        }
        String key = stringBuilder.toString();
        return key;
    }


    /**
     * 获取私钥
     *
     * @return 当前的私钥对象
     */
    private RSAPrivateKey getPrivateKey() {
        return privateKey;
    }

    /**
     * 获取公钥
     *
     * @return 当前的公钥对象
     */
    private RSAPublicKey getPublicKey() {
        return publicKey;
    }

    /**
     * 随机生成密钥对
     */
    public void genKeyPair() {
        KeyPairGenerator keyPairGen = null;
        try {
            keyPairGen = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        keyPairGen.initialize(1024, new SecureRandom());
        KeyPair keyPair = keyPairGen.generateKeyPair();
        this.privateKey = (RSAPrivateKey) keyPair.getPrivate();
        this.publicKey = (RSAPublicKey) keyPair.getPublic();
    }

    /**
     * 从文件中输入流中加载公钥
     *
     * @param in 公钥输入流
     * @throws Exception 加载公钥时产生的异常
     */
    private void loadPublicKey(InputStream in) throws Exception {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String readLine = null;
            StringBuilder sb = new StringBuilder();
            while ((readLine = br.readLine()) != null) {
                if (readLine.charAt(0) == '-') {
                    continue;
                } else {
                    sb.append(readLine);
                    sb.append('\r');
                }
            }
            loadPublicKey(sb.toString());
        } catch (IOException e) {
            throw new Exception("公钥数据流读取错误");
        } catch (NullPointerException e) {
            throw new Exception("公钥输入流为空");
        }
    }

    /**
     * 从字符串中加载公钥
     *
     * @param publicKeyStr 公钥数据字符串
     * @throws Exception 加载公钥时产生的异常
     */
    private void loadPublicKey(String publicKeyStr) throws Exception {
        try {
            byte[] buffer = org.apache.commons.codec.binary.Base64.decodeBase64(publicKeyStr);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
            this.publicKey = (RSAPublicKey) keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无此算法");
        } catch (InvalidKeySpecException e) {
            throw new Exception("公钥非法");
        } catch (NullPointerException e) {
            throw new Exception("公钥数据为空");
        }
    }

    /**
     * 从文件中加载私钥
     *
     * @param keyFileName 私钥文件名
     * @return 是否成功
     * @throws Exception
     */
    private void loadPrivateKey(InputStream in) throws Exception {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String readLine = null;
            StringBuilder sb = new StringBuilder();
            while ((readLine = br.readLine()) != null) {
                if (readLine.charAt(0) == '-') {
                    continue;
                } else {
                    sb.append(readLine);
                    sb.append('\r');
                }
            }
            loadPrivateKey(sb.toString());
        } catch (IOException e) {
            throw new Exception("私钥数据读取错误");
        } catch (NullPointerException e) {
            throw new Exception("私钥输入流为空");
        }
    }

    private void loadPrivateKey(String privateKeyStr) throws Exception {
        try {
            byte[] buffer = org.apache.commons.codec.binary.Base64.decodeBase64(privateKeyStr);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(buffer);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            this.privateKey = (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无此算法");
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
            throw new Exception("私钥非法");
        } catch (NullPointerException e) {
            throw new Exception("私钥数据为空");
        }
    }

    /**
     * 加密过程
     *
     * @param publicKey     公钥
     * @param plainTextData 明文数据
     * @return
     * @throws Exception 加密过程中的异常信息
     */
    private byte[] encrypt(RSAPublicKey publicKey, byte[] plainTextData) throws Exception {
        if (publicKey == null) {
            throw new Exception("加密公钥为空, 请设置");
        }
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("RSA");//, new BouncyCastleProvider());
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] output = cipher.doFinal(plainTextData);
            return output;
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无此加密算法");
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
            return null;
        } catch (InvalidKeyException e) {
            throw new Exception("加密公钥非法,请检查");
        } catch (IllegalBlockSizeException e) {
            throw new Exception("明文长度非法");
        } catch (BadPaddingException e) {
            throw new Exception("明文数据已损坏");
        }
    }

    /**
     * 解密过程
     *
     * @param privateKey 私钥
     * @param cipherData 密文数据
     * @return 明文
     * @throws Exception 解密过程中的异常信息
     */
    private byte[] decrypt(RSAPrivateKey privateKey, byte[] cipherData) throws Exception {
        if (privateKey == null) {
            throw new Exception("解密私钥为空, 请设置");
        }
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("RSA");//, new BouncyCastleProvider());
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] output = cipher.doFinal(cipherData);
            return output;
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无此解密算法");
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
            return null;
        } catch (InvalidKeyException e) {
            throw new Exception("解密私钥非法,请检查");
        } catch (IllegalBlockSizeException e) {
            throw new Exception("密文长度非法");
        } catch (BadPaddingException e) {
            throw new Exception("密文数据已损坏");
        }
    }


    /**
     * 字节数据转字符串专用集合
     */
    private static final char[] HEX_CHAR = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * 字节数据转十六进制字符串
     *
     * @param data 输入数据
     * @return 十六进制内容
     */
    private static String byteArrayToString(byte[] data) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            //取出字节的高四位 作为索引得到相应的十六进制标识符 注意无符号右移
            stringBuilder.append(HEX_CHAR[(data[i] & 0xf0) >>> 4]);
            //取出字节的低四位 作为索引得到相应的十六进制标识符
            stringBuilder.append(HEX_CHAR[(data[i] & 0x0f)]);
            if (i < data.length - 1) {
                stringBuilder.append(' ');
            }
        }
        return stringBuilder.toString();
    }

    @Override
    protected String encryptSource(String source) {
        //  http://commons.apache.org/proper/commons-codec/ : org.apache.commons.codec.binary.Base64
        // sun.misc.BASE64Encoder
        try {
            byte[] binaryData = encrypt(getPublicKey(), source.getBytes());
            String base64String = org.apache.commons.codec.binary.Base64.encodeBase64String(binaryData) /* org.apache.commons.codec.binary.Base64.encodeBase64(binaryData) */;
            return base64String;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected String decryptCode(String code) {
        try {
            byte[] binaryData = decrypt(getPrivateKey(), org.apache.commons.codec.binary.Base64.decodeBase64(code) /*org.apache.commons.codec.binary.Base64.decodeBase64(base46String.getBytes())*/);
            return new String(binaryData);
        } catch (Exception e) {
            try{
                log.debug("解密【{}】失败第一次", code);
                code = StringUtils.replace(code, " ", "+");
                byte[] binaryData = decrypt(getPrivateKey(), org.apache.commons.codec.binary.Base64.decodeBase64(code) /*org.apache.commons.codec.binary.Base64.decodeBase64(base46String.getBytes())*/);
                return new String(binaryData);
            } catch (Exception e1) {
                log.debug("解密【{}】失败第二次", code);
                throw new RuntimeException(e1);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        RsaEncryptor rsaEncryptor = new RsaEncryptor();
        String public_key = rsaEncryptor.getKeyFromFile("certificate/rsa/rsa_public_key.pem");
        String private_key = rsaEncryptor.getKeyFromFile("certificate/rsa/pkcs8_private_key.pem");
        rsaEncryptor.loadPublicKey(public_key);
        rsaEncryptor.loadPrivateKey(private_key);

        String source = "111.com";
        String code = rsaEncryptor.encrypt(source);
        System.out.println("###########################");
        System.out.println(code);
        System.out.println(rsaEncryptor.decryptCode(code));

        Base64Encryptor base64Encryptor = new Base64Encryptor();
        String date = "9999-12-30";
        System.out.println(date);
        String code1 = base64Encryptor.encrypt(rsaEncryptor.encrypt(date));
        System.out.println("code1=======" + code1);
        String code2 = rsaEncryptor.decrypt(base64Encryptor.decrypt("UEZzSlIxYzR6YXdZRjEzWG1CU0NqSFo3c0p6bDNtbEVIcEdxdFdtSm9FcUcwVzl1eU9XQ2lzSEZlVU1oQWJUYVo2RW9RSGdrVGcyNCtZVkxOTUQ2WjdMZTJ6UXlvNURubmVFalpVSHZKL2NEaW9jS2d1RWRZMlNCMEdvMG9UcU1zN2xPNmRDN1lpbE40VkNnWUEwUXdnTFRXckljbW0xa2QxWDAwdWFjUzdnPQ"));
        System.out.println("code2=======" + code2);

    }
}
