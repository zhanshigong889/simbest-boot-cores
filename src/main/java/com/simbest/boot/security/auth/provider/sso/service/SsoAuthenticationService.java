/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.provider.sso.service;


/**
 * 用途：单点登录验证服务
 * 作者: lishuyi 
 * 时间: 2018/1/20  15:06 
 */
public interface SsoAuthenticationService extends Comparable<SsoAuthenticationService> {

    Integer ORDER_RSA = 1;
    Integer ORDER_MOCHA = 2;
    Integer ORDER_3DES = 3;

    /**
     * 解密请求中的用户名
     * @param request 验证请求
     * @return 用户名
     */
    String decryptKeyword(String encodeKeyword);

    Integer getOrder();

}
