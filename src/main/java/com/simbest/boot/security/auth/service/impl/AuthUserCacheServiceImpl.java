/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.service.impl;

import com.simbest.boot.constants.ApplicationConstants;
import com.simbest.boot.security.IPermission;
import com.simbest.boot.security.IUser;
import com.simbest.boot.security.SimpleUser;
import com.simbest.boot.security.auth.service.IAuthUserCacheService;
import com.simbest.boot.util.redis.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 用途：提供系统认证用户的缓存操作服务
 * 作者: lishuyi
 * 时间: 2019/10/14  15:32
 */
@Slf4j
@Component
public class AuthUserCacheServiceImpl implements IAuthUserCacheService {

    public static final String AUTH_USER_GLOBAL_KEY = "auth:user:";

    public static final String AUTH_USER_PERMISSION_GLOBAL_KEY = "auth:user:permission:";

    public static final String AUTH_USER_ACCESS_GLOBAL_KEY = "auth:user:access:";

    public static final String AUTH_USER_PASSWORD_GLOBAL_KEY = "auth:user:password:";


    //处理用户应用权限
    @Autowired
    private RedisTemplate<String, Set<IPermission>> redisUserPermissionTemplate;

    private ValueOperations<String, Set<IPermission>> redisUserPermissionOps;

    @PostConstruct
    public void init(){
        redisUserPermissionOps = redisUserPermissionTemplate.opsForValue();
    }

    //===================================处理用户信息 START=========================================================//

    /**
     * 在缓存中新增或覆盖更新用户信息，并按照KeyType的定义，提供username、preferredMobile、openid三组键值定位
     * @param user
     * @return
     */
    public void saveOrUpdateCacheUser(IUser user) {
        Assert.notNull(user.getId(), "用户主键唯一标识不允许为空!");
        //新增存放用户对象
        RedisUtil.setBeanGlobal(getUserCacheKey(user.getId()), user);
        log.debug("用户【{}】已通过键值【{}】完成缓存", user, getUserCacheKey(user.getId()));

        //存放username、preferredMobile、openid三组Key键
        if(StringUtils.isNotEmpty(user.getUsername())) {
            RedisUtil.setGlobal(getUserCacheKey(user.getUsername()), user.getId());
            log.debug("用户主键唯一标识【{}】已通过用户名键值【{}】完成缓存", user.getId(), getUserCacheKey(user.getUsername()));
        }
        if(StringUtils.isNotEmpty(user.getPreferredMobile())) {
            RedisUtil.setGlobal(getUserCacheKey(user.getPreferredMobile()), user.getId());
            log.debug("用户主键唯一标识【{}】已通过手机号码键值【{}】完成缓存", user.getId(), getUserCacheKey(user.getPreferredMobile()));
        }
        if(StringUtils.isNotEmpty(user.getOpenid())) {
            RedisUtil.setGlobal(getUserCacheKey(user.getOpenid()), user.getId());
            log.debug("用户主键唯一标识【{}】已通过openid键值【{}】完成缓存", user.getId(), getUserCacheKey(user.getOpenid()));
        }
    }

    /**
     * 尝试从缓存中读取用户信息
     * @param keyword
     * @return
     */
    public IUser loadCacheUser(String keyword) {
        String userId = RedisUtil.getGlobal(getUserCacheKey(keyword));
        log.debug("通过关键字【{}】获取到用户主键ID为【{}】", getUserCacheKey(keyword), userId);
        if(StringUtils.isNotEmpty(userId)){
            IUser user = RedisUtil.getBeanGlobal(getUserCacheKey(userId), SimpleUser.class);
            if(null != user) {
                log.debug("通过用户主键ID【{}】即将返回用户【{}】", getUserCacheKey(userId), user);
                return user;
            }
            else{
                log.warn("无法通过用户主键ID【{}】读取用户信息", getUserCacheKey(userId));
                return null;
            }
        }
        else{
            log.warn("无法通过关键字【{}】读取用户主键ID", getUserCacheKey(keyword));
            return null;
        }
    }



    /**
     * 清理用户缓存
     * @param user
     */
    public void removeCacheUser(IUser user) {
        Assert.notNull(user.getId(), "删除用户缓存时，用户主键唯一标识不允许为空!");
        //清理存放用户对象
        RedisUtil.expireGlobal(getUserCacheKey(user.getId()), 0, TimeUnit.NANOSECONDS);
        log.debug("已清理键值【{}】的缓存", getUserCacheKey(user.getId()));

        //清理username、preferredMobile、openid三组Key键
        if(StringUtils.isNotEmpty(user.getUsername())) {
            RedisUtil.expireGlobal(getUserCacheKey(user.getUsername()), 0, TimeUnit.NANOSECONDS);
            log.debug("已清理用户名键值【{}】的缓存", getUserCacheKey(user.getUsername()));
        }
        if(StringUtils.isNotEmpty(user.getPreferredMobile())) {
            RedisUtil.expireGlobal(getUserCacheKey(user.getPreferredMobile()), 0, TimeUnit.NANOSECONDS);
            log.debug("已清理手机号码键值【{}】的缓存", getUserCacheKey(user.getPreferredMobile()));
        }
        if(StringUtils.isNotEmpty(user.getOpenid())) {
            RedisUtil.expireGlobal(getUserCacheKey(user.getOpenid()), 0, TimeUnit.NANOSECONDS);
            log.debug("已清理openid键值【{}】的缓存", getUserCacheKey(user.getOpenid()));
        }
    }

    /**
     * 获取用户信息key键，以保证所有应用通用
     * @param key
     * @return
     */
    private String getUserCacheKey(String key){
        return AUTH_USER_GLOBAL_KEY.concat(key);
    }

    //===================================处理用户信息 END=========================================================//


    //===================================处理用户应用权限 START=========================================================//
    /**
     * 在缓存中新增或覆盖更新用户权限
     * @param username
     * @param appcode
     * @param permissions
     * @return
     */
    public void saveOrUpdateCacheUserPermission(String username, String appcode, Set<IPermission> permissions) {
        Assert.notNull(username, "用户账号不允许为空!");
        Assert.notNull(appcode, "应用标识不允许为空!");
        Assert.notNull(permissions, "用户权限不允许为空!");
        redisUserPermissionOps.set(getUserPermissionCacheKey(username, appcode), permissions);
    }

    /**
     * 尝试从缓存中读取用户权限
     * @param keyword
     * @return
     */
    public Set<IPermission> loadCacheUserPermission(String username, String appcode) {
        Set<IPermission> permissions = redisUserPermissionOps.get(getUserPermissionCacheKey(username, appcode));
        if(null != permissions) {
            log.debug("通过用户账号【{}】和应用标识【{}】即将返回用户权限个数为【{}】", username, appcode, permissions.size());
            return permissions;
        }
        else{
            log.debug("通过用户账号【{}】和应用标识【{}】无法读取用户权限", username, appcode);
            return null;
        }
    }

    /**
     * 清理用户权限
     * @param user
     */
    public void removeCacheUserPermission(String username, String appcode) {
        Assert.notNull(username, "用户账号不允许为空!");
        Assert.notNull(appcode, "应用标识不允许为空!");
        //清理存放用户对象
        redisUserPermissionTemplate.expire(getUserPermissionCacheKey(username, appcode), 0, TimeUnit.NANOSECONDS);
        log.debug("已清理键值【{}】的缓存", getUserPermissionCacheKey(username, appcode));
    }

    /**
     * 获取用户权限key键，以保证所有应用通用
     * @param username
     * @param appcode
     * @return
     */
    private String getUserPermissionCacheKey(String username, String appcode){
        return AUTH_USER_PERMISSION_GLOBAL_KEY.concat(username).concat(ApplicationConstants.COLON).concat(appcode);
    }
    //===================================处理用户应用权限 END=========================================================//



    //===================================处理用户应用访问 START=========================================================//
    /**
     * 在缓存中新增或覆盖更新用户应用访问
     * @param username
     * @param appcode
     * @param isPermit
     * @return
     */
    public void saveOrUpdateCacheUserAccess(String username, String appcode, Boolean isPermit) {
        Assert.notNull(username, "用户账号不允许为空!");
        Assert.notNull(appcode, "应用标识不允许为空!");
        Assert.notNull(isPermit, "用户应用访问不允许为空!");
        RedisUtil.setBeanGlobal(getAuthUserAccessGlobalKey(username, appcode), isPermit);
    }

    /**
     * 尝试从缓存中读取用户应用访问
     * @param keyword
     * @return
     */
    public Boolean loadCacheUserAccess(String username, String appcode) {
        Boolean isPermit = RedisUtil.getBeanGlobal(getAuthUserAccessGlobalKey(username, appcode), Boolean.class);
        if(null != isPermit) {
            log.debug("通过用户账号【{}】和应用标识【{}】即将返回用户访问应用结果为【{}】", username, appcode, isPermit);
            return isPermit;
        }
        else{
            log.debug("通过用户账号【{}】和应用标识【{}】无法读取用户访问应用结果", username, appcode);
            return null;
        }
    }

    /**
     * 清理用户应用访问
     * @param user
     */
    public void removeCacheUserAccess(String username, String appcode) {
        Assert.notNull(username, "用户账号不允许为空!");
        Assert.notNull(appcode, "应用标识不允许为空!");
        //清理存放用户对象
        RedisUtil.expireGlobal(getAuthUserAccessGlobalKey(username, appcode), 0, TimeUnit.NANOSECONDS);
        log.debug("已清理键值【{}】的缓存", getAuthUserAccessGlobalKey(username, appcode));
    }

    /**
     * 获取用户应用访问key键，以保证所有应用通用
     * @param username
     * @param appcode
     * @return
     */
    private String getAuthUserAccessGlobalKey(String username, String appcode){
        return AUTH_USER_ACCESS_GLOBAL_KEY.concat(username).concat(ApplicationConstants.COLON).concat(appcode);
    }
    //===================================处理用户应用访问 END=========================================================//



    //===================================处理用户密码 START=========================================================//
    /**
     * 在缓存中新增或覆盖更新用户密码
     * @param username
     * @param password
     * @param isRight
     * @return
     */
    public void saveOrUpdateCacheUserPassword(String username, String password, Boolean isRight) {
        Assert.notNull(username, "用户账号不允许为空!");
        Assert.notNull(password, "应用标识不允许为空!");
        Assert.notNull(isRight, "用户密码校验结果不允许为空!");
        //保存新密码时，先删除该用户的历史密码
        RedisUtil.mulDeleteGlobal(AUTH_USER_PASSWORD_GLOBAL_KEY.concat(username).concat(ApplicationConstants.COLON));
        //保存该用户的新密码
        RedisUtil.setBeanGlobal(getAuthUserPasswordGlobalKey(username, password), isRight);
    }

    /**
     * 尝试从缓存中读取用户密码
     * @param keyword
     * @return
     */
    public Boolean loadCacheUserPassword(String username, String password) {
        Boolean isRight = RedisUtil.getBeanGlobal(getAuthUserPasswordGlobalKey(username, password), Boolean.class);
        if(null != isRight) {
            log.debug("通过用户账号【{}】和密码【{}】即将返回用户访问应用结果为【{}】", username, password, isRight);
            return isRight;
        }
        else{
            log.debug("通过用户账号【{}】和密码【{}】在缓存校验失败", username, password);
            return null;
        }
    }

    /**
     * 清理用户应用访问
     * @param user
     */
    public void removeCacheUserPassword(String username) {
        Assert.notNull(username, "用户账号不允许为空!");
        //清理存放用户对象
        Set<String> userPasswordKeys = RedisUtil.globalKeys(AUTH_USER_PASSWORD_GLOBAL_KEY.concat(username).concat(ApplicationConstants.COLON).concat(ApplicationConstants.STAR));
        userPasswordKeys.forEach( k -> RedisUtil.expireGlobal(k, 0, TimeUnit.NANOSECONDS));

    }

    /**
     * 获取用户应用访问key键，以保证所有应用通用
     * @param username
     * @param appcode
     * @return
     */
    private String getAuthUserPasswordGlobalKey(String username, String password) {
        return AUTH_USER_PASSWORD_GLOBAL_KEY.concat(username).concat(ApplicationConstants.COLON).concat(password);
    }
    //===================================处理用户密码 END=========================================================//






    /**
     * 1、清理用户信息
     * 2、清理用户权限信息
     * 3、清理用户访问信息
     * @param username
     */
    @Override
    public void removeCacheUserAllInformaitions(String username) {
        //1、清理用户信息
        IUser user = loadCacheUser(username);
        if(null != user){
            removeCacheUser(user);
        }
        //2、清理用户权限信息
        Set<String> permissionKeys = RedisUtil.globalKeys(AUTH_USER_PERMISSION_GLOBAL_KEY.concat(username).concat(ApplicationConstants.COLON).concat(ApplicationConstants.STAR));
        permissionKeys.forEach( k -> RedisUtil.expireGlobal(k, 0, TimeUnit.NANOSECONDS));
        //3、清理用户访问信息
        Set<String> accessKeys = RedisUtil.globalKeys(AUTH_USER_ACCESS_GLOBAL_KEY.concat(username).concat(ApplicationConstants.COLON).concat(ApplicationConstants.STAR));
        accessKeys.forEach( k -> RedisUtil.expireGlobal(k, 0, TimeUnit.NANOSECONDS));
        //4、清理用户密码
        removeCacheUserPassword(username);
    }

}
