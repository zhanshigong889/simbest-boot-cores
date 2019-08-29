/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.util.server;

import com.simbest.boot.base.exception.Exceptions;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * 用途：Socket 工具类
 * 作者: lishuyi
 * 时间: 2018/5/12  15:56
 */
public class SocketUtil {

    /**
     * 心跳检查
     * 参考：
     * http://stackoverflow.com/questions/11547082/fastest-way-to-scan-ports-with-java
     * http://jupiterbee.blog.51cto.com/3364619/1301284
     *
     * @param host
     * @param port
     * @return
     */
    public static boolean checkHeartConnection(String host, int port) {
        Socket socket = null;
        try {
            socket = new Socket();
            //心跳等待1000毫秒
            socket.connect(new InetSocketAddress(host, port), 1000);
            return true;
        } catch (Exception ex) {
            return false;
        } finally {
            try {
                if (socket != null)
                    socket.close();
            } catch (IOException e) {
                Exceptions.printException(e);
            }
        }
    }
}
