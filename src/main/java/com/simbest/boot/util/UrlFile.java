/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 用途：
 * 作者: lishuyi
 * 时间: 2019/4/16  15:49
 */
@Data
@AllArgsConstructor
@Builder
public class UrlFile implements Serializable {
    String remoteFileUrl;
    HttpURLConnection conn;
    URL connUrl;
    //文件名 0PTR5440279975688156748.docx
    String fileName;
    //文件后缀 docx
    String fileSuffix;
}
