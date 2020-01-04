/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.util.http;

import javax.servlet.http.HttpServletRequest;

/**
 * 用途：浏览器工具类
 * 作者: lishuyi
 * 时间: 2019/1/4  11:44
 */
public class BrowserUtil {

    private static String[] IEBrowserSignals = {"MSIE", "Trident", "Edge"};

    /**
     * 判断是否为微软浏览器
     * @param request
     * @return boolean
     */
    public static boolean isMSBrowser(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        for (String signal : IEBrowserSignals) {
            if (userAgent.contains(signal))
                return true;
        }
        return false;
    }
}
