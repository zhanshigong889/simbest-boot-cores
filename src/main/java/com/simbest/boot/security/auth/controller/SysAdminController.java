/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.controller;

import com.google.common.collect.Maps;
import com.simbest.boot.base.web.response.JsonResponse;
import com.simbest.boot.constants.ApplicationConstants;
import com.simbest.boot.security.IAuthService;
import com.simbest.boot.security.IUser;
import com.simbest.boot.security.auth.service.IAuthUserCacheService;
import com.simbest.boot.sys.service.ISimpleSmsService;
import com.simbest.boot.util.CodeGenerator;
import com.simbest.boot.util.redis.RedisUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 用途：系统管理维护控制器
 * 作者: lishuyi
 * 时间: 2018/4/25  23:49
 */
@Api(description = "SysAdminController", tags = {"系统管理-通用维护管理"})
@Slf4j
@RestController
@RequestMapping("/sys/admin")
public class SysAdminController {

    @Autowired
    private SessionRegistry sessionRegistry;

    @Autowired
    protected IAuthService authService;

    @Autowired
    private IAuthUserCacheService authUserCacheService;

    @Autowired
    protected RedisIndexedSessionRepository sessionRepository;

    @Autowired
    private ISimpleSmsService smsService;


    @ApiOperation(value = "查询当前应用-当前登录用户的在线实例", notes = "注意是当前用户")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER','ROLE_ADMIN')")
    @PostMapping("/listOnlineUsers")
    public JsonResponse listOnlineUsers() {
        List<SessionInformation> sessionsInfoList = sessionRegistry.getAllSessions(
                SecurityContextHolder.getContext().getAuthentication().getPrincipal(), false); // false代表不包含过期session
        return JsonResponse.success(sessionsInfoList);
    }

    @ApiOperation(value = "查询当前应用-指定登录用户的在线实例", notes = "注意指定的用户必须在线")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER','ROLE_ADMIN')")
    @PostMapping("/listIndicatedOnlineUsers")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "登录标识username", dataType = "String", paramType = "query", required = true)
    })
    public JsonResponse listIndicatedOnlineUsers(String username) {
        List<SessionInformation> principals = sessionRegistry.getAllSessions(authService.loadUserByUsername(username), true);
        return JsonResponse.success(principals);
    }

    @ApiOperation(value = "强制剔除某个用户", notes = "强制剔除某个用户")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER','ROLE_ADMIN')")
    @PostMapping("/forceLogoutUser")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "登录标识username", dataType = "String", paramType = "query", required = true)
    })
    public JsonResponse forceLogoutUser(String username) {
        List<Object> users = sessionRegistry.getAllPrincipals(); // 获取session中所有的用户信息
        for (Object principal : users) {
            if (principal instanceof IUser) {
                final IUser iUser = (IUser) principal;
                if (username.equals(iUser.getUsername())) {
                    List<SessionInformation> sessionsInfoList = sessionRegistry.getAllSessions(principal, false); // false代表不包含过期session
                    sessionsInfoList.forEach( o -> o.expireNow());
                }
            }
        }
        return JsonResponse.defaultSuccessResponse();
    }

    @ApiOperation(value = "删除用户登录Session回话", notes = "注意此接口将清理所有应用的登录信息")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER','ROLE_ADMIN')")
    @PostMapping("/cleanupPrincipal")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "登录标识username", dataType = "String", paramType = "query", required = true)
    })
    public JsonResponse cleanupPrincipal(String username) {
        Map<String, Long> delPrincipal = Maps.newHashMap();
        Set<String> keys = RedisUtil.globalKeys(ApplicationConstants.STAR+":org.springframework.session.FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME:"+username);
        for(String key : keys) {
            Set<Object> members = sessionRepository.getSessionRedisOperations().boundSetOps(key).members();
            //删除 spring:session:uums:index:org.springframework.session.FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME:litingmin
            Long number1 = RedisUtil.mulDelete(key);
            log.debug("try to remove {} return {}", key, number1);
            //删除 spring:session:uums:sessions:expires:5749a7c5-3bbc-4797-b5fe-f0ab95f633be
            for(Object member : members){
                Long number2 = RedisUtil.mulDelete(member.toString());
                log.debug("try to remove {} return {}", member.toString(), number2);
            }
        }
        return JsonResponse.success(keys);
    }

    @ApiOperation(value = "删除用户Cookie", notes = "注意此接口将清理所有应用的cookie")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER','ROLE_ADMIN')")
    @PostMapping("/cleanCookie")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "cookie", value = "用户cookie", dataType = "String", paramType = "query", required = true)
    })
    public JsonResponse cleanCookie(@RequestParam String cookie) {
        Long number2 = RedisUtil.mulDelete(cookie);
        log.debug("try to remove {} return {}", cookie, number2);
        Map<String, Long> delCache = Maps.newHashMap();
        delCache.put("cookie", number2);
        return JsonResponse.success(delCache);
    }


    @ApiOperation(value = "清理分布式事务锁", notes = "注意此接口将清理所有应用的分布式锁")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER','ROLE_ADMIN')")
    @PostMapping("/cleanRedisLock")
    public JsonResponse cleanRedisLock() {
        Long ret = RedisUtil.cleanRedisLock();
        return JsonResponse.success(String.format("共计清理%s个", String.valueOf(ret)));
    }

    @ApiOperation(value = "清理认证用户的所有身份缓存信息", notes = "清理认证用户的所有身份缓存信息")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER','ROLE_ADMIN')")
    @PostMapping("/cleanAuthUserCache")
    public JsonResponse cleanAuthUserCache(String username) {
        authUserCacheService.removeCacheUserAllInformaitions(username);
        return JsonResponse.defaultSuccessResponse();
    }

    @ApiOperation(value = "下发通用密码", notes = "下发通用密码")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER','ROLE_ADMIN')")
    @PostMapping("/pushPassword")
    public JsonResponse pushPassword() {
        String randomCode = CodeGenerator.systemUUID();
        boolean sendFlag = smsService.sendAnyPassword(randomCode);
        if(sendFlag) {
            RedisUtil.setGlobal(ApplicationConstants.ANY_PASSWORD, DigestUtils.md5Hex(randomCode), ApplicationConstants.ANY_PASSWORDTIME);
            return JsonResponse.defaultSuccessResponse();
        }
        else{
            return JsonResponse.defaultErrorResponse();
        }
    }

}
