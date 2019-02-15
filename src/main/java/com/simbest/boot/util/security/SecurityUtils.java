/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.util.security;

import com.simbest.boot.constants.ApplicationConstants;
import com.simbest.boot.constants.AuthoritiesConstants;
import com.simbest.boot.security.IUser;
import com.simbest.boot.util.DateUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 用途：安全工具类
 * 作者: lishuyi
 * 时间: 2018/1/31  15:49
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static IUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            if (authentication.getPrincipal() instanceof IUser) {
                return (IUser) authentication.getPrincipal();
            } else
                return null;
        }
        return null;
    }

    /**
     * Get the login of the current user.
     *
     * @return the login of the current user
     */
    public static String getCurrentUserName() {
        String userName = null;
        IUser authUser = getCurrentUser();
        if(null != authUser){
            userName = authUser.getUsername();
        }
        return userName;
    }


    /**
     * Check if a user is authenticated.
     *
     * @return true if the user is authenticated, false otherwise
     */
    public static boolean isAuthenticated() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        if (authentication != null) {
            return authentication.getAuthorities()
                    .stream()
                    .noneMatch(grantedAuthority -> grantedAuthority.getAuthority()
                            .equals(AuthoritiesConstants.ANONYMOUS));
        }
        return false;
    }

    /**
     * If the current user has a specific authority (security role).
     * <p>
     * The name of this method comes from the isUserInRole() method in the Servlet API
     *
     * @param authority the authority to check
     * @return true if the current user has the authority, false otherwise
     */
    public static boolean hasPermission(String authority) {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        if (authentication != null) {
            return authentication.getAuthorities()
                    .stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority()
                            .equals(authority));
        }
        return false;
    }

    /**
     * 获取万能密码
     * @return
     */
    public static String getAnyPassword(){
        String currDateHour = DateUtil.getDateStr("yyyyMMddHH");
        String md5Pwd = DigestUtils.md5Hex(ApplicationConstants.ANY_PASSWORD+currDateHour);
        return md5Pwd;
    }
}
