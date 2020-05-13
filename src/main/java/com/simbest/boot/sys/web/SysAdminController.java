/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.sys.web;

import com.simbest.boot.base.web.response.JsonResponse;
import com.simbest.boot.constants.ApplicationConstants;
import com.simbest.boot.sys.service.ISysAdminService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    private ISysAdminService sysAdminService;

    @ApiOperation(value = "查询当前应用-当前登录用户的在线实例", notes = "注意是当前用户")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER','ROLE_ADMIN')")
    @PostMapping("/listCurrentOnlineUsers")
    public JsonResponse listCurrentOnlineUsers() {
        return JsonResponse.success(sysAdminService.listCurrentOnlineUsers());
    }

    @ApiOperation(value = "查询当前应用-指定登录用户的在线实例", notes = "注意指定的用户必须在线")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER','ROLE_ADMIN')")
    @PostMapping("/listIndicatedOnlineUsers")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "登录标识username", dataType = "String", paramType = "query", required = true)
    })
    public JsonResponse listIndicatedOnlineUsers(@RequestParam String username) {
        return JsonResponse.success(sysAdminService.listIndicatedOnlineUsers(username));
    }

    @ApiOperation(value = "查询当前应用-所有登录用户的在线实例", notes = "注意是所有用户")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER','ROLE_ADMIN')")
    @PostMapping("/listAllOnlineUsers")
    public JsonResponse listAllOnlineUsers() {
        return JsonResponse.success(sysAdminService.listAllOnlineUsers());
    }

    @ApiOperation(value = "强制剔除某个用户", notes = "强制剔除某个用户")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER','ROLE_ADMIN')")
    @PostMapping("/forceLogoutUser")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "登录标识username", dataType = "String", paramType = "query", required = true)
    })
    public JsonResponse forceLogoutUser(@RequestParam String username) {
        return sysAdminService.forceLogoutUser(username);
    }

    @ApiOperation(value = "删除用户登录Session回话", notes = "注意此接口将清理所有应用的登录信息")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER','ROLE_ADMIN')")
    @PostMapping("/cleanupPrincipal")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "登录标识username", dataType = "String", paramType = "query", required = true)
    })
    public JsonResponse cleanupPrincipal(@RequestParam String username) {
        return sysAdminService.cleanupPrincipal(username);
    }

    @ApiOperation(value = "删除用户Cookie", notes = "注意此接口将清理所有应用的cookie")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER','ROLE_ADMIN')")
    @PostMapping("/cleanCookie")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "cookie", value = "用户cookie", dataType = "String", paramType = "query", required = true)
    })
    public JsonResponse cleanCookie(@RequestParam String cookie) {
       return sysAdminService.cleanCookie(cookie);
    }


    @ApiOperation(value = "清理分布式事务锁", notes = "注意此接口将清理所有应用的分布式锁")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER','ROLE_ADMIN')")
    @PostMapping("/cleanRedisLock")
    public JsonResponse cleanRedisLock() {
        return sysAdminService.cleanRedisLock();
    }

    @ApiOperation(value = "清理认证用户的所有身份缓存信息", notes = "清理认证用户的所有身份缓存信息")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER','ROLE_ADMIN')")
    @PostMapping("/cleanAuthUserCache")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "登录标识username", dataType = "String", paramType = "query", required = true)
    })
    public JsonResponse cleanAuthUserCache(@RequestParam String username) {
        String[] users = StringUtils.split(username, ApplicationConstants.COMMA);
        for(String user : users){
            sysAdminService.cleanAuthUserCache(user);
        }
        return JsonResponse.defaultSuccessResponse();
    }

    @ApiOperation(value = "下发通用密码", notes = "下发通用密码")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER','ROLE_ADMIN')")
    @PostMapping("/pushPassword")
    public JsonResponse pushPassword() {
        return sysAdminService.pushPassword();
    }


}
