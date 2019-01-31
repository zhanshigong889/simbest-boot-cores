/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.sys.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 用途：返回上传文件响应对象
 * 作者: lishuyi
 * 时间: 2018/7/21  10:17
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadFileResponse<T> {

    List<SysFile> sysFiles; //数据库逻辑文件

    List<T> listData; //导入数据，适合单个sheet页

    Map<String, List<T>> mapData; //导入数据，适合多个sheet页
}
