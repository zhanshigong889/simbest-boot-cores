/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.service;

import com.simbest.boot.security.IPermission;
import com.simbest.boot.security.IUser;

import java.util.Set;

/**
 * 用途：提供系统认证用户的缓存操作服务
 * 作者: lishuyi
 * 时间: 2019/10/14  15:32
 */
public interface IAuthUserCacheService {

    //===================================处理用户信息 START=========================================================//
    /**
     * 在缓存中新增或覆盖更新用户信息，并按照KeyType的定义，提供username、preferredMobile、openid三组键值定位
     * @param user
     * @return
     */
    void saveOrUpdateCacheUser(IUser user);

    /**
     * 尝试从缓存中读取用户信息
     * @param keyword
     * @return
     */
    IUser loadCacheUser(String keyword);


    /**
     * 清理用户缓存
     * @param user
     */
    void removeCacheUser(IUser user);
    //===================================处理用户信息 END=========================================================//


    //===================================处理用户应用权限 START=========================================================//
    /**
     * 在缓存中新增或覆盖更新用户权限
     * @param username
     * @param appcode
     * @param permissions
     * @return
     */
    void saveOrUpdateCacheUserPermission(String username, String appcode, Set<IPermission> permissions);

    /**
     * 尝试从缓存中读取用户权限
     * @param keyword
     * @return
     */
    Set<IPermission> loadCacheUserPermission(String username, String appcode);

    /**
     * 清理用户权限
     * @param user
     */
    void removeCacheUserPermission(String username, String appcode);
    //===================================处理用户应用权限 END=========================================================//



    //===================================处理用户应用访问 START=========================================================//
    /**
     * 在缓存中新增或覆盖更新用户应用访问
     * @param username
     * @param appcode
     * @param permissions
     * @return
     */
    void saveOrUpdateCacheUserAccess(String username, String appcode, Boolean isPermit);

    /**
     * 尝试从缓存中读取用户应用访问
     * @param keyword
     * @return
     */
    Boolean loadCacheUserAccess(String username, String appcode);

    /**
     * 清理用户应用访问
     * @param user
     */
    void removeCacheUserAccess(String username, String appcode);
    //===================================处理用户应用访问 END=========================================================//


    /**
     * 1、清理用户信息
     * 2、清理用户权限信息
     * 3、清理用户访问信息
     * @param username
     */
    void removeCacheUserAllInformaitions(String username);
}
