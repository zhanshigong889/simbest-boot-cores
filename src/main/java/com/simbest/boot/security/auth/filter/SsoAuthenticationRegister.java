/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.filter;

import com.google.common.collect.Sets;
import com.simbest.boot.security.IAuthService;
import com.simbest.boot.security.IUser;
import com.simbest.boot.security.auth.provider.sso.service.SsoAuthenticationService;
import com.simbest.boot.util.UrlEncoderUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;
import java.util.SortedSet;

import static com.simbest.boot.constants.ApplicationConstants.UTF_8;

/**
 * 用途：所有单点认证Token注册器
 * 作者: lishuyi
 * 时间: 2018/4/26  15:28
 */
@Slf4j
@Component
public class SsoAuthenticationRegister {

    @Autowired
    private ApplicationContext appContext;

    @Autowired
    private IAuthService authService;

    public SortedSet<SsoAuthenticationService> getSsoAuthenticationService() {
        SortedSet sortedSet = Sets.newTreeSet();
        Map<String, SsoAuthenticationService> auths = appContext.getBeansOfType(SsoAuthenticationService.class);
        auths.values().forEach( a -> sortedSet.add(a));
        return sortedSet;
    }

    public String decodeKeyword(String encodeKeyword, IAuthService.KeyType keyType) {
        String decodeKeyword = null;
        for(SsoAuthenticationService decryptService : getSsoAuthenticationService()) {
            //防止从前端 加密后的参数通过浏览器后，+之类的字符变成空格
            try {
                if ( UrlEncoderUtils.hasUrlEncoded( encodeKeyword ) ){
                    encodeKeyword = URLDecoder.decode(encodeKeyword,UTF_8);
                }
            } catch ( UnsupportedEncodingException e ) {
                //不支持该字符编码方式
                log.error("解密参数时，发现不支持该字符编码方式(UTF-8)，关键字【{}】和关键字类型【{}】,请立即检查！", encodeKeyword, keyType);
            }
            decodeKeyword = decryptService.decryptKeyword(encodeKeyword);
            if(StringUtils.isNotEmpty(decodeKeyword)) {
                log.debug("通过关键字【{}】解密后为【{}】", encodeKeyword, decodeKeyword);
                IUser iUser = authService.findByKey(decodeKeyword, keyType);
                if (null != iUser) {
                    //成功返回
                    break;
                } else {
                    decodeKeyword = null;
                    log.warn("请注意关键字【{}】拉取用户信息为NULL！", decodeKeyword);
                }
            }
        }
        if(StringUtils.isEmpty(decodeKeyword)) {
            log.error("遍历完所有SSO解密服务器，关键字【{}】和关键字类型【{}】依旧为空，请立即检查！", encodeKeyword, keyType);
        }
        return decodeKeyword;
    }

    public static void main ( String[] args ) {
        try {
            String en = URLEncoder.encode( "cq+DdHFotno=","UTF-8" );
            System.out.println( en );
            System.out.println(URLDecoder.decode( "cq+DdHFotno=","UTF-8" ));
        } catch ( UnsupportedEncodingException e ) {
            e.printStackTrace( );
        }
    }
}
