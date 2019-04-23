/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.service;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mzlion.core.lang.Assert;
import com.simbest.boot.config.AppConfig;
import com.simbest.boot.constants.ApplicationConstants;
import com.simbest.boot.security.IAuthService;
import com.simbest.boot.security.IPermission;
import com.simbest.boot.security.IUser;
import com.simbest.boot.security.SimplePermission;
import com.simbest.boot.util.redis.RedisUtil;
import com.simbest.boot.uums.api.user.UumsSysUserinfoApi;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.session.data.redis.RedisOperationsSessionRepository;

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

    private AppConfig appConfig;

    private UumsSysUserinfoApi userinfoApi;

    private RedisOperationsSessionRepository redisOperationsSessionRepository;

    public AbstractAuthService(AppConfig appConfig, UumsSysUserinfoApi userinfoApi, RedisOperationsSessionRepository redisOperationsSessionRepository){
        this.appConfig = appConfig;
        this.userinfoApi = userinfoApi;
        this.redisOperationsSessionRepository = redisOperationsSessionRepository;
    }

    @Override
    public IUser findByKey(String keyword, KeyType keyType) {
        IUser user = userinfoApi.findByKey(keyword, keyType, appConfig.getAppcode());
        log.debug("通过关键字【{}】和关键字类型【{}】应用代码【{}】获取用户信息为【{}】", keyword, keyType.name(), appConfig.getAppcode(), user);
        return user;
    }

    @Override
    public Set<? extends IPermission> findUserPermissionByAppcode(String username, String appcode) {
        Set<IPermission> permissions = Sets.newHashSet();
        Set<SimplePermission> simplePermissions = userinfoApi.findPermissionByAppUserNoSession(username, appcode);
        if(null != simplePermissions && !simplePermissions.isEmpty()){
            for(SimplePermission s : simplePermissions) {
                permissions.add(s);
            }
        }
        log.debug("用户【{}】从应用【{}】获取到【{}】权限", username, appcode, permissions.size());
        return permissions;
    }

    @Override
    public boolean checkUserAccessApp(String username, String appcode) {
        return userinfoApi.checkUserAccessAppNoSession(username, appcode);
    }

    @Override
    public void changeUserSessionByCorp(IUser newUser) {
        Assert.notNull(newUser, "更新用户不能为空！");
        Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();
        Assert.notNull(existingAuth, "当前认证信息不能为空！");
        //清空当前浏览器会话
        SecurityContextHolder.getContext().setAuthentication(null);
        Map<String, Long> delPrincipal = Maps.newHashMap();
        Set<String> keys = RedisUtil.globalKeys(ApplicationConstants.STAR + ":org.springframework.session.FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME:" + newUser.getUsername());
        for (String key : keys) {
            Set<Object> members = redisOperationsSessionRepository.getSessionRedisOperations().boundSetOps(key).members();
            //删除 spring:session:uums:index:org.springframework.session.FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME:litingmin
            Long number1 = RedisUtil.mulDelete(key);
            log.debug("即将清理键值【{}】结果为【{}】", key, number1);
            //删除 spring:session:uums:sessions:expires:5749a7c5-3bbc-4797-b5fe-f0ab95f633be
            for (Object member : members) {
                Long number2 = RedisUtil.mulDelete(member.toString());
                log.debug("即将清理键值【{}】结果为【{}】", member.toString(), number2);
            }
        }

        UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
                newUser, existingAuth.getCredentials(), existingAuth.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(newAuth);
        log.debug("更新后的认证信息为【{}】", newAuth);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDetails userDetails = userinfoApi.findByUsername(username, appConfig.getAppcode());
        log.debug("通过用户名【{}】和应用代码【{}】提取到的用户信息为【{}】", username, appConfig.getAppcode(), userDetails);
        return userDetails;
    }

}
