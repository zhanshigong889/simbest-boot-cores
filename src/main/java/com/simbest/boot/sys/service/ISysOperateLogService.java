package com.simbest.boot.sys.service;

import com.simbest.boot.base.service.ILogicService;
import com.simbest.boot.sys.model.SysOperateLog;

import java.util.Map;

/**
 * <strong>Title : ISysOperateLogService</strong><br>
 * <strong>Description : 系统操作日志</strong><br>
 * <strong>Create on : 2018/10/10</strong><br>
 * <strong>Modify on : 2018/10/10</strong><br>
 * <strong>Copyright (C) Ltd.</strong><br>
 *
 * @author LJW lijianwu@simbest.com.cn
 * @version <strong>V1.0.0</strong><br>
 * <strong>修改历史:</strong><br>
 * 修改人 修改日期 修改描述<br>
 * -------------------------------------------<br>
 */
public interface ISysOperateLogService extends ILogicService<SysOperateLog,String> {

    /**
     * 保存系统操作日志
     * @param sysOperateLog         操作日志对象
     * @return SysOperateLog
     */
    SysOperateLog saveLog(SysOperateLog sysOperateLog);

    /**
     * 保存系统操作日志
     * @param sysOperateLogParam         操作日志对象
     * @return SysOperateLog
     */
    SysOperateLog saveLog( Map<String,Object> sysOperateLogParam);
}
