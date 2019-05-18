/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.util.json;

import com.simbest.boot.constants.ApplicationConstants;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 用途：从HttpServletRequest获取JSON数据
 * 作者: lishuyi
 * 时间: 2019/5/16  20:18
 */
public class GetJsonRequestUtil {

    /***
     * 获取 request 中 json 字符串的内容
     * @param request
     * @return : <code>byte[]</code>
     * @throws IOException
     */
    public static String getRequestJsonString(HttpServletRequest request)
            throws IOException {
        String submitMehtod = request.getMethod();
        if (submitMehtod.equals(ApplicationConstants.HTTPPOST)) {
            return getRequestPostStr(request);
        }
        else {
            return ApplicationConstants.EMPTY;
        }
    }

    /**
     * 描述:获取 post 请求的 byte[] 数组
     * @param request
     * @return
     * @throws IOException
     */
    private static byte[] getRequestPostBytes(HttpServletRequest request)
            throws IOException {
        int contentLength = request.getContentLength();
        if(contentLength<0){
            return null;
        }
        byte buffer[] = new byte[contentLength];
        for (int i = 0; i < contentLength;) {

            int readlen = request.getInputStream().read(buffer, i,
                    contentLength - i);
            if (readlen == -1) {
                break;
            }
            i += readlen;
        }
        return buffer;
    }

    /**
     * 描述:获取 post 请求内容
     * @param request
     * @return
     * @throws IOException
     */
    private static String getRequestPostStr(HttpServletRequest request)
            throws IOException {
        byte buffer[] = getRequestPostBytes(request);
        String charEncoding = request.getCharacterEncoding();
        if (charEncoding == null) {
            charEncoding = "UTF-8";
        }
        return new String(buffer, charEncoding);
    }

}
