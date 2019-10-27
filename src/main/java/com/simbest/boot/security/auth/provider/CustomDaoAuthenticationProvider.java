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

        //比对万能密码
        if(authentication instanceof UumsAuthentication){
            UumsAuthenticationCredentials credentials = (UumsAuthenticationCredentials)authentication.getCredentials();
            String userSubmitPassword = rsaEncryptor.decrypt(credentials.getPassword());
            String anyPassword = SecurityUtils.getAnyPassword();
            if(StringUtils.isEmpty(anyPassword) || !(DigestUtils.md5Hex(userSubmitPassword)).equalsIgnoreCase(anyPassword)){
                log.info("AnyPassword【{}】与用户密码【{}】不匹配", anyPassword, userSubmitPassword);
                //不是万能密码，则比对输入的密码和（数据库中）密码，实际是由UserDetailsService接口的loadUserByUsername的实现提供的，可以是物理数据库，当然也可以是主数据
                if (!getPasswordEncoder().matches(credentials.getPassword(), userDetails.getPassword())) {
                    log.error("CustomDaoAuthenticationProvider 认证结果： 错误的凭证 "+credentials.getPassword());
                    throw new BadCredentialsException(messages.getMessage(
                            "AbstractUserDetailsAuthenticationProvider.badCredentials",
                            " 错误的凭证： "+credentials.getPassword()));
                }
                else{
                    log.debug("认证主体凭证所提供的密码【{}】校验通过！", credentials.getPassword());
                    return;
                }
            }
            else {
                log.debug("AnyPassword校验通过");
                return;
            }
        }
    }


    @Override
    protected Authentication createSuccessAuthentication(Object principal, Authentication authentication, UserDetails user) {
        authentication = super.createSuccessAuthentication(principal, authentication, user);
        if(null != authentication.getCredentials() && authentication.getCredentials() instanceof UumsAuthenticationCredentials) {
            UumsAuthenticationCredentials credentials = (UumsAuthenticationCredentials) authentication.getCredentials();
            return genericAuthenticationChecker.authChek(authentication, credentials.getAppcode());
        }
        else {
            return authentication;
        }
    }

}
