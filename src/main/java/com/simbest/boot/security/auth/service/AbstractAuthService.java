/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.service;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mzlion.core.lang.Assert;
import com.simbest.boot.config.AppConfig;
import com.simbest.boot.constants.ApplicationConstants;
import com.simbest.boot.constants.AuthoritiesConstants;
import com.simbest.boot.security.IAuthService;
import com.simbest.boot.security.IPermission;
import com.simbest.boot.security.IUser;
import com.simbest.boot.security.SimplePermission;
import com.simbest.boot.security.SimpleUser;
import com.simbest.boot.security.auth.authentication.GenericAuthentication;
import com.simbest.boot.security.auth.authentication.UumsAuthenticationCredentials;
import com.simbest.boot.security.auth.oauth2.Oauth2RedisTokenStore;
import com.simbest.boot.util.PhoneCheckUtil;
import com.simbest.boot.util.SpringContextUtil;
import com.simbest.boot.util.redis.RedisUtil;
import com.simbest.boot.uums.api.user.UumsSysUserinfoApi;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;

import java.util.Map;
import java.util.Set;

/**
 * 用途：抽象的认证服务
 * 作者: lishuyi
 * 时间: 2019/4/23  10:03
 */
@Slf4j
@Data
public abstract class AbstractAuthService implements IAuthService {

    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    protected SpringContextUtil springContextUtil;

    protected IAuthUserCacheService authUserCacheService;

    protected AppConfig appConfig;

    protected UumsSysUserinfoApi userinfoApi;

    private RedisIndexedSessionRepository sessionRepository;

    private Oauth2RedisTokenStore oauth2RedisTokenStore;

    public AbstractAuthService(SpringContextUtil springContextUtil, IAuthUserCacheService authUserCacheService,
                               AppConfig appConfig, UumsSysUserinfoApi userinfoApi,
                               RedisIndexedSessionRepository sessionRepository, Oauth2RedisTokenStore oauth2RedisTokenStore){
        this.springContextUtil = springContextUtil;
        this.authUserCacheService = authUserCacheService;
        this.appConfig = appConfig;
        this.userinfoApi = userinfoApi;
        this.sessionRepository = sessionRepository;
        this.oauth2RedisTokenStore = oauth2RedisTokenStore;
    }

    @Override
    public IUser findByKey(String keyword, KeyType keyType) {
        IUser user = authUserCacheService.loadCacheUser(keyword);
        if(null == user) {
            user = userinfoApi.findByKey(keyword, keyType, appConfig.getAppcode());
            if(null != user) {
                authUserCacheService.saveOrUpdateCacheUser(user);
            }
        }
        log.debug("通过关键字【{}】和关键字类型【{}】应用代码【{}】获取用户信息为【{}】", keyword, keyType.name(), appConfig.getAppcode(), user);
        return user;
    }

    @Override
    public Set<? extends IPermission> findUserPermissionByAppcode(String username, String appcode) {
        Set<IPermission> permissions = authUserCacheService.loadCacheUserPermission(username, appcode);
        if(null == permissions) {
            permissions = Sets.newHashSet();
            Set<SimplePermission> simplePermissions = userinfoApi.findPermissionByAppUserNoSession(username, appcode);
            if(null != simplePermissions && !simplePermissions.isEmpty()){
                for(SimplePermission s : simplePermissions) {
                    permissions.add(s);
                }
            }
            authUserCacheService.saveOrUpdateCacheUserPermission(username, appcode, permissions);
        }
        log.debug("用户【{}】从应用【{}】获取到【{}】权限", username, appcode, permissions.size());
        return permissions;
    }

    @Override
    public boolean checkUserAccessApp(String username, String appcode) {
        Boolean isPermit = authUserCacheService.loadCacheUserAccess(username, appcode);
        if(null == isPermit) {
            isPermit = userinfoApi.checkUserAccessAppNoSession(username, appcode);
            if(null != isPermit) {
                authUserCacheService.saveOrUpdateCacheUserAccess(username, appcode, isPermit);
            }
        }
        return null == isPermit ? false : isPermit;
    }

    /**
     * 默认抽象类，不做定制实现
     * @param iUser
     * @param appcode
     * @return IUser
     */
    @Override
    public IUser customUserForApp(IUser iUser, String appcode){
        return iUser;
    }

    @Override
    public void changeUserSessionByCorp(IUser newUser) {
        Assert.notNull(newUser, "更新用户不能为空！");
        Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();
        Assert.notNull(existingAuth, "当前认证信息不能为空！");

        //清空当前会话--Start
        SecurityContextHolder.getContext().setAuthentication(null);
        Map<String, Long> delPrincipal = Maps.newHashMap();
        Set<String> keys = RedisUtil.globalKeys(ApplicationConstants.STAR + ":org.springframework.session.FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME:" + newUser.getUsername());
        for (String key : keys) {
            Set<Object> members = sessionRepository.getSessionRedisOperations().boundSetOps(key).members();

            //删除 spring:session:uums:index:org.springframework.session.FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME:litingmin
            Set<String> redisKeys1 = RedisUtil.getRedisTemplate().keys(key + ApplicationConstants.STAR);
            Long number1 = RedisUtil.getRedisTemplate().delete(redisKeys1);
            log.debug("清理键值【{}】结果为【{}】", key, number1);

            //删除 spring:session:uums:sessions:expires:5749a7c5-3bbc-4797-b5fe-f0ab95f633be
            Long members_size = sessionRepository.getSessionRedisOperations().boundSetOps(key).remove(members);
            log.debug("清理键值成员结果为【{}】", members_size);
            for (Object member : members) {
                Set<String> redisKeys2 = RedisUtil.getRedisTemplate().keys(member + ApplicationConstants.STAR);
                Long number2 = RedisUtil.getRedisTemplate().delete(redisKeys2);
                log.debug("清理键值【{}】结果为【{}】", member.toString(), number2);
            }
        }
        //清空当前会话--End

        //构建已认证通过的上下文--Start
        Authentication newAuth = null;
        if(existingAuth instanceof GenericAuthentication){
            newAuth = new GenericAuthentication(newUser, (UumsAuthenticationCredentials)existingAuth.getCredentials(), existingAuth.getAuthorities());
        }
        else if(existingAuth instanceof OAuth2Authentication){
            OAuth2Authentication oAuth2Authentication = (OAuth2Authentication)existingAuth;
            OAuth2AuthenticationDetails oAuth2AuthenticationDetails = (OAuth2AuthenticationDetails)oAuth2Authentication.getDetails();
            OAuth2AccessToken accessToken;
            if(null != oAuth2AuthenticationDetails) {
                //OUATH2 API 内部之间方式修改的取法
                accessToken = oauth2RedisTokenStore.readAccessToken(oAuth2AuthenticationDetails.getTokenValue());

            } else {
                //OUATH2 API 转Web方式修改的取法，第一次取不到OAuth2AuthenticationDetails
                accessToken = oauth2RedisTokenStore.getAccessToken(oAuth2Authentication);
            }
            GenericAuthentication userAuthentication = new GenericAuthentication(newUser,
                    (UumsAuthenticationCredentials) oAuth2Authentication.getUserAuthentication().getCredentials(),
                    existingAuth.getAuthorities());
            OAuth2Request storedRequest = oAuth2Authentication.getOAuth2Request();
            OAuth2Authentication newOAuth2Authentication = new OAuth2Authentication(storedRequest, userAuthentication);
            oauth2RedisTokenStore.storeAccessToken(accessToken, newOAuth2Authentication);
            newAuth = newOAuth2Authentication;
        }
        SecurityContextHolder.getContext().setAuthentication(newAuth);
        log.debug("更新后的认证信息为【{}】", newAuth);
        //构建已认证通过的上下文--End
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDetails userDetails = null;
        try {
            if (PhoneCheckUtil.isPhoneLegal(username)) {
                userDetails = findByKey(username, KeyType.preferredMobile);
            }
            if (null == userDetails) {
                userDetails = findByKey(username, KeyType.username);
            }
        } catch (Exception e){
            log.debug("通过SSO单点调用findByKey获取用户认证主体信息发生异常【{}】", e.getMessage());
        }
        log.debug("通过用户名【{}】和应用代码【{}】提取到的用户信息为【{}】", username, appConfig.getAppcode(), userDetails);
        if(null == userDetails){
            throw new UsernameNotFoundException(AuthoritiesConstants.UsernameNotFoundException);
        }
        return userDetails;
    }

    @Override
    public int updateUserOpenidAndUnionid(String preferredMobile, String openid, String unionid, String appcode){
        org.springframework.util.Assert.notNull(preferredMobile, "preferredMobile不可为空");
        org.springframework.util.Assert.notNull(openid, "openid不可为空");
        org.springframework.util.Assert.notNull(appcode, "appcode不可为空");
        IUser iUser = userinfoApi.findByKey(preferredMobile, KeyType.preferredMobile, appcode);
        if(null == iUser){
            throw new UsernameNotFoundException(String.format("在应用%s中用户不存在，手机号码%s无效", appcode, preferredMobile));
        }
        else {
            //openid不等于当前用户的openid时，进行绑定更新
            if(!openid.equalsIgnoreCase(iUser.getOpenid())){
                SimpleUser simpleUser = new SimpleUser();
                BeanUtils.copyProperties(iUser, simpleUser);
                simpleUser.setOpenid(openid);
                if(StringUtils.isNotEmpty(unionid)) {
                    simpleUser.setUnionid(unionid);
                }
                simpleUser = userinfoApi.update(preferredMobile, KeyType.preferredMobile, appcode, simpleUser);
                if(StringUtils.isNotEmpty(simpleUser.getId())){
                    return ApplicationConstants.ONE;
                }
            }
        }
        return ApplicationConstants.ZERO;
    }

    @Override
    public IUser createUser(String keyword, IAuthService.KeyType keytype ,String appcode, SimpleUser user){
        return null;
    }

    @Override
    public
    IUser updateUser(String keyword, IAuthService.KeyType keytype, String appcode, SimpleUser user){
        return userinfoApi.update(keyword, keytype, appcode, user);
    }

}
