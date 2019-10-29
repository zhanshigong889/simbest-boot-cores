/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.util.server;

import com.simbest.boot.config.EmbeddedServletConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * 用途：主机工具栏
 * 作者: lishuyi
 * 时间: 2018/5/12  15:53
 */
@Slf4j
@Component
@DependsOn(value = {"embeddedServletConfiguration"})
public class HostUtil {

    @Autowired
    private ServletWebServerApplicationContext server;

    private static HostUtil hostUtil;

    @PostConstruct
    public void init() {
        hostUtil = this;
    }

    public static boolean isWindowsOS() {
        boolean isWindowsOS = false;
        String osName = System.getProperty("os.name");
        if (osName.toLowerCase().indexOf("windows") > -1) {
            isWindowsOS = true;
        }
        return isWindowsOS;
    }

    public static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return InetAddress.getLoopbackAddress().getHostName();
        }
    }

    public static String getHostAddress() {
        String sIP = "";
        InetAddress ip = null;
        try {
            // 如果是Windows操作系统
            if (isWindowsOS()) {
                ip = InetAddress.getLocalHost();
            }
            // 如果是Linux操作系统
            else {
                boolean bFindIP = false;
                Enumeration<NetworkInterface> netInterfaces = (Enumeration<NetworkInterface>) NetworkInterface
                        .getNetworkInterfaces();
                while (netInterfaces.hasMoreElements()) {
                    if (bFindIP) {
                        break;
                    }
                    NetworkInterface ni = (NetworkInterface) netInterfaces
                            .nextElement();
                    // ----------特定情况，可以考虑用ni.getName判断
                    // 遍历所有ip
                    Enumeration<InetAddress> ips = ni.getInetAddresses();
                    while (ips.hasMoreElements()) {
                        ip = (InetAddress) ips.nextElement();
                        if ((ip.getHostAddress().endsWith(".0")) || (ip.getHostAddress().endsWith(".1"))) {
                            continue;
                        }
                        if (ip.isSiteLocalAddress() && !ip.isLoopbackAddress() // 127.开头的都是lookback地址
                                && ip.getHostAddress().indexOf(":") == -1) {
                            bFindIP = true;
                            break;
                        }
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (null != ip) {
            sIP = ip.getHostAddress();
        }
        return sIP;
    }

    /**
     * 获取服务器ip和端口信息
     * 参考：http://ruitao.name/blog/20160111/tomcat-port/
     *
     * @return
     */
    public int getRunningPort() {
        //return server.getWebServer().getPort();
        return EmbeddedServletConfiguration.serverPort;
    }

    public static String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        ip=ip.split(",")[0].trim();
        return ip;
    }

    public static boolean checkTelnet(String ip, int port) {
        boolean result = false;
        Socket server = null;
        try {
            server = new Socket();
            InetSocketAddress address = new InetSocketAddress(ip, port);
            server.connect(address, 2000);
            result = true;
        } catch (UnknownHostException e) {
            log.warn("Telnet 测试主机IP地址无法失败【{}】", ip);
        } catch (IOException e) {
            log.warn("Telnet 测试失败IP地址【{}】 端口【{}】", ip, port);
        } finally {
            if (server != null)
                try {
                    server.close();
                } catch (IOException e) {
                }
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println(checkTelnet("10.87.13.228", 1389));
    }
}
