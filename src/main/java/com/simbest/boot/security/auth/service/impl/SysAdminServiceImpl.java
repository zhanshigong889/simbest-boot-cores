/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.service.impl;

import com.google.common.collect.Maps;
import com.simbest.boot.base.web.response.JsonResponse;
import com.simbest.boot.constants.ApplicationConstants;
import com.simbest.boot.security.IAuthService;
import com.simbest.boot.security.auth.service.IAuthUserCacheService;
import com.simbest.boot.security.auth.service.ISysAdminService;
import com.simbest.boot.sys.service.ISimpleSmsService;
import com.simbest.boot.util.CodeGenerator;
import com.simbest.boot.util.DateUtil;
import com.simbest.boot.util.redis.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 用途：系统管理维护服务层
 * 作者: lishuyi
 * 时间: 2019/11/15  18:55
 */
@Slf4j
@Component
@DependsOn(value = {"sessionRegistry","sessionRepository"})
public class SysAdminServiceImpl implements ISysAdminService {

    @Autowired
    private SessionRegistry sessionRegistry;

    @Autowired
    protected RedisIndexedSessionRepository sessionRepository;

    @Autowired
    protected IAuthService authService;

    @Autowired
    private IAuthUserCacheService authUserCacheService;

    @Autowired
    private ISimpleSmsService smsService;

    @Override
    public JsonResponse listOnlineUsers(){
        List<SessionInformation> sessionsInfoList = sessionRegistry.getAllSessions(
                SecurityContextHolder.getContext().getAuthentication().getPrincipal(), false); // false代表不包含过期session
        return JsonResponse.success(sessionsInfoList);
    }

    @Override
    public JsonResponse listIndicatedOnlineUsers(String username) {
        List<SessionInformation> principals = sessionRegistry.getAllSessions(authService.loadUserByUsername(username), true);
        return JsonResponse.success(principals);
    }

    @Override
    public JsonResponse forceLogoutUser(String username) {
        List<SessionInformation> principals = sessionRegistry.getAllSessions(authService.loadUserByUsername(username), true);
        principals.forEach( o -> o.expireNow());
        return JsonResponse.defaultSuccessResponse();
    }

    @Override
    public JsonResponse cleanupPrincipal(String username) {
        Map<String, Long> delPrincipal = Maps.newHashMap();
        Set<String> keys = RedisUtil.globalKeys(ApplicationConstants.STAR+":org.springframework.session.FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME:"+username);
        for(String key : keys) {
            Set<Object> members = sessionRepository.getSessionRedisOperations().boundSetOps(key).members();
            //删除 spring:session:uums:index:org.springframework.session.FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME:litingmin
            Long number1 = RedisUtil.mulDelete(key);
            log.debug("已删除key键为【{}】的缓存【{}】个", key, number1);
            //删除 spring:session:uums:sessions:expires:5749a7c5-3bbc-4797-b5fe-f0ab95f633be
            for(Object member : members){
                Long number2 = RedisUtil.mulDelete(member.toString());
                log.debug("已删除key键为【{}】的缓存【{}】个", member.toString(), number2);
            }
        }
        return JsonResponse.success(keys);
    }

    @Override
    public JsonResponse cleanCookie(String cookie) {
        Long number2 = RedisUtil.mulDelete(cookie);
        log.debug("已删除key键为【{}】的缓存【{}】个", cookie, number2);
        Map<String, Long> delCache = Maps.newHashMap();
        delCache.put("cookie", number2);
        return JsonResponse.success(delCache);
    }

    @Override
    public JsonResponse cleanRedisLock() {
        Long ret = RedisUtil.cleanRedisLock();
        return JsonResponse.success(String.format("共计清理%s个", String.valueOf(ret)));
    }

    @Override
    public JsonResponse cleanAuthUserCache(String username) {
        authUserCacheService.removeCacheUserAllInformaitions(username);
        return JsonResponse.defaultSuccessResponse();
    }

    @Override
    public JsonResponse pushPassword() {
        String randomCode = CodeGenerator.systemUUID();
        boolean sendFlag = smsService.sendAnyPassword(randomCode);
        if(sendFlag) {
            String currDateHour = DateUtil.getDateStr("yyyyMMddHH");
            RedisUtil.setGlobal(DigestUtils.md5Hex(ApplicationConstants.ANY_PASSWORD+currDateHour), DigestUtils.md5Hex(randomCode), ApplicationConstants.ANY_PASSWORDTIME);
            return JsonResponse.defaultSuccessResponse();
        }
        else{
            return JsonResponse.defaultErrorResponse();
        }
    }

}
