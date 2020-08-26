/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.sys.service;

import com.simbest.boot.base.web.response.JsonResponse;
import org.springframework.security.core.session.SessionInformation;

import java.util.List;
import java.util.Set;

/**
 * 用途：系统管理维护服务层
 * 作者: lishuyi
 * 时间: 2019/11/15  19:01
 */
public interface ISysAdminService {

    List<SessionInformation> listCurrentOnlineUsers();

    List<SessionInformation> listIndicatedOnlineUsers(String username);

    List<SessionInformation> listAllOnlineUsers();

    JsonResponse forceLogoutUser(String username);

    JsonResponse cleanupPrincipal(String username);

    JsonResponse cleanCookie(String cookie);

    JsonResponse cleanRedisLock();

    JsonResponse cleanAuthUserCache(String username);

    JsonResponse getCacheUserPassword(String username);

    JsonResponse pushPassword();
}
