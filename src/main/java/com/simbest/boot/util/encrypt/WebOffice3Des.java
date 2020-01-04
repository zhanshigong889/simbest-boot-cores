/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.util.encrypt;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.net.URLEncoder;
import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;

/**
 * 用途：WebOffice365 的3DES加密工具类
 * 作者: lishuyi
 * 时间: 2018/8/6  23:38
 */
@Component
public class WebOffice3Des {
    private static final byte[] DESkey = "16388534".getBytes();// 设置密钥，略去
    // private static final byte[] DESIV = "dkjeerdf".getBytes() ;// 设置向量，略去
    private static final byte[] DESIV = "60880301".getBytes() ;// 设置向量，略去
    //加密算法的参数接口，IvParameterSpec是它的一个实现
    static AlgorithmParameterSpec iv = null;
    private static Key key = null;

    public WebOffice3Des() throws Exception {
        this(DESkey,DESIV);
    }

    public WebOffice3Des(String DESkey,String DESIV) throws Exception {
        this(DESkey.getBytes(),DESIV.getBytes());
    }

    private WebOffice3Des(byte[] DESkey,byte[] DESIV) throws Exception {
        // 设置密钥参数
        DESKeySpec keySpec = new DESKeySpec(DESkey);
        // 设置向量
        iv = new IvParameterSpec(DESIV);
        // 获得密钥工厂
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        key = keyFactory.generateSecret(keySpec);// 得到密钥对象
    }
    /**
     * 加密
     * @param data
     * @return String
     * @throws Exception
     */
    public static String encode(String data) throws Exception {
        // 得到加密对象Cipher
        Cipher enCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        // 设置工作模式为加密模式，给出密钥和向量
        enCipher.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] pasByte = enCipher.doFinal(data.getBytes("utf-8"));
        return Base64.encodeBase64String(pasByte).replaceAll("\\+", "_").replaceAll("\\/", "@");
    }
    /**
     * 解密
     * @param data
     * @return String
     * @throws Exception
     */
    public static String decode(String data) throws Exception {
        Cipher deCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        deCipher.init(Cipher.DECRYPT_MODE, key, iv);
        byte[] pasByte = deCipher.doFinal(Base64.decodeBase64(data.replaceAll("_", "+").replaceAll("@", "/")));
        return new String(pasByte, "UTF-8");
    }

    public static void main(String[] args) throws Exception {
        WebOffice3Des tools = new WebOffice3Des();
        //System.out.println("加密:" + tools.encode("http://10.87.9.143:9081/uud/filedownload.jsp?webname=CCPN&filen=201706301728493655.pptx&showfilen=test0630.pptx"));
        //System.out.println("解密:" + tools.decode(tools.encode("http://10.87.9.143:9081/uud/filedownload.jsp?webname=CCPN&filen=201706301728493655.pptx&showfilen=test0630.pptx")));
        String url = "http://211.138.31.211:8088/webOffice/?furl=http://10.87.10.245:8080/entry/waittask/attachment?id=621";
        String firstUrl = StringUtils.substringBefore(url, "furl=")+"furl=";
        String secondUrl = StringUtils.substringAfter(url, "furl=");
        System.out.println(firstUrl);
        System.out.println(secondUrl);
        //secondUrl = URLEncoder.encode(secondUrl, "UTF-8");
        System.out.println(tools.encode(secondUrl));
        String webOfficeUrl = firstUrl+tools.encode(secondUrl);
        System.out.println(webOfficeUrl);
    }
}
