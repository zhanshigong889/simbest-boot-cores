/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.provider;

import com.simbest.boot.security.auth.authentication.UumsAuthentication;
import com.simbest.boot.security.auth.authentication.UumsAuthenticationCredentials;
import com.simbest.boot.util.encrypt.RsaEncryptor;
import com.simbest.boot.util.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 用途：自定义密码认证器
 * 作者: lishuyi
 * 时间: 2018/9/6  16:57
 */
@Slf4j
public class CustomDaoAuthenticationProvider extends DaoAuthenticationProvider {

    private GenericAuthenticationChecker genericAuthenticationChecker;

    private RsaEncryptor rsaEncryptor;

    public CustomDaoAuthenticationProvider(GenericAuthenticationChecker genericAuthenticationChecker, RsaEncryptor rsaEncryptor){
        this.genericAuthenticationChecker = genericAuthenticationChecker;
        this.rsaEncryptor = rsaEncryptor;
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails,
                                                  UsernamePasswordAuthenticationToken authentication)
            throws AuthenticationException {
        if (authentication.getCredentials() == null) {
            log.error("Authentication failed: 密码不能为空");
            throw new BadCredentialsException(messages.getMessage(
                    "AbstractUserDetailsAuthenticationProvider.badCredentials",
                    "密码不能为空----Bad credentials"));
        }

        //只有主数据中是UsernamePasswordAuthenticationToken。其他应用都是UumsAuthentication
        String userSubmitPasswordEncode = null;
        String userSubmitPasswordDecode = null;

        if(authentication instanceof UumsAuthentication) {
            UumsAuthenticationCredentials credentials = (UumsAuthenticationCredentials) authentication.getCredentials();
            userSubmitPasswordEncode = credentials.getPassword();
            userSubmitPasswordDecode = rsaEncryptor.decrypt(userSubmitPasswordEncode);
        }
        else if(authentication instanceof UsernamePasswordAuthenticationToken) {
            userSubmitPasswordEncode = authentication.getCredentials().toString();
            userSubmitPasswordDecode = userSubmitPasswordEncode;
        }
        if(StringUtils.isEmpty(userSubmitPasswordEncode)){
            log.error("用户加密密码不能为空！");
            throw new BadCredentialsException("账号或密码错误");
        }
        else if(StringUtils.isEmpty(userSubmitPasswordDecode)){
            log.error("用户解密密码不能为空！");
            throw new BadCredentialsException("账号或密码错误");
        }
        //比对万能密码
        String anyPassword = SecurityUtils.getAnyPassword();
        if(StringUtils.isEmpty(anyPassword) || !(DigestUtils.md5Hex(userSubmitPasswordDecode)).equalsIgnoreCase(anyPassword)){
            log.debug("AnyPassword【{}】与用户密码【{}】不匹配", anyPassword, userSubmitPasswordDecode);
            //不是万能密码，则比对输入的密码和（数据库中）密码，实际是由UserDetailsService接口的loadUserByUsername的实现提供的，可以是物理数据库，当然也可以是主数据
            if (!getPasswordEncoder().matches(userSubmitPasswordEncode, userDetails.getPassword())) {
                log.warn("CustomDaoAuthenticationProvider认证用户【{}】密码【{}】加密密码【{}】解密密码【{}】认证失败",
                        userDetails.getUsername(), userDetails.getPassword(), userSubmitPasswordEncode, userSubmitPasswordDecode);
                throw new BadCredentialsException(String.format("CustomDaoAuthenticationProvider认证用户%s密码%s加密密码%s解密密码%s认证失败",
                        userDetails.getUsername(), userDetails.getPassword(), userSubmitPasswordEncode, userSubmitPasswordDecode));
            }
            else{
                log.debug("认证主体【{}】凭证所提供的密码【{}】校验通过！", userDetails.getUsername(), userSubmitPasswordDecode);
                return;
            }
        }
        else {
            log.debug("认证主体【{}】通过AnyPassword校验通过", userDetails.getUsername());
            return;
        }
    }


    /**
     * 注释下面代码，因为应用验证userDetails.getPassword()用于在Http请求为空，不可能进行createSuccessAuthentication。
     * 而UUMS验证，Authentication不可能为UumsAuthentication，这是在UumsHttpValidationAuthenticationController组装成了UsernamePasswordAuthenticationToken，在数据库进行验证
     */
//    @Override
//    protected Authentication createSuccessAuthentication(Object principal, Authentication authentication, UserDetails user) {
//        authentication = super.createSuccessAuthentication(principal, authentication, user);
//        if(null != authentication.getCredentials() && authentication.getCredentials() instanceof UumsAuthenticationCredentials) {
//            UumsAuthenticationCredentials credentials = (UumsAuthenticationCredentials) authentication.getCredentials();
//            return genericAuthenticationChecker.authChek(authentication, credentials.getAppcode());
//        }
//        else {
//            return authentication;
//        }
//    }

}
