/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * 用途：基于Spring Session with Redis的工具类
 * 作者: lishuyi
 * 时间: 2018/5/26  15:18
 */
public class AppSessionUtil {
    private final static int FiveMinute = 5 * 60;

    /**
     * 获取默认HttpSession, 超时时间基于server.servlet.session.timeout
     * 参考 com.simbest.boot.config.RedisConfiguration
     * @param request
     * @return HttpSession
     */
    public static HttpSession getSession(HttpServletRequest request) {
        return request.getSession();
    }

    /**
     * 返回五分钟内有效的短Session
     * @param request
     * @return HttpSession
     */
    public static HttpSession getShortSession(HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.setMaxInactiveInterval(FiveMinute);
        return session;
    }

    /**
     * 返回任意时间超时的Session， 超时单位：秒
     * @param request
     * @param timeout
     * @return HttpSession
     */
    public static HttpSession getCustomSession(HttpServletRequest request, int timeout) {
        HttpSession session = request.getSession();
        return session;
    }

    /**
     * 获取默认HttpSession, 超时时间基于server.servlet.session.timeout
     * 参考 com.simbest.boot.config.RedisConfiguration
     * @param request
     * @return HttpSession
     */
    public static HttpSession getNewSession(HttpServletRequest request) {
        return request.getSession(true);
    }

    /**
     * 返回五分钟内有效的短Session
     * @param request
     * @return HttpSession
     */
    public static HttpSession getNewShortSession(HttpServletRequest request) {
        HttpSession session = getNewSession(request);
        session.setMaxInactiveInterval(FiveMinute);
        return session;
    }

    /**
     * 返回任意时间超时的Session， 超时单位：秒
     * @param request
     * @param timeout
     * @return HttpSession
     */
    public static HttpSession getNewCustomSession(HttpServletRequest request, int timeout) {
        HttpSession session = getNewSession(request);
        session.setMaxInactiveInterval(timeout);
        return session;
    }
}
