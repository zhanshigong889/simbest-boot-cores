package com.simbest.boot.sys.service.impl;

import com.simbest.boot.base.service.impl.LogicService;
import com.simbest.boot.sys.model.SysOperateLog;
import com.simbest.boot.sys.repository.SysOperateLogRepository;
import com.simbest.boot.sys.service.ISysOperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
@Slf4j
@Service
public class SysOperateLogService extends LogicService<SysOperateLog,String> implements ISysOperateLogService {


    private SysOperateLogRepository sysOperateLogRepository;

    @Autowired
    public SysOperateLogService ( SysOperateLogRepository repository ) {
        super( repository );
        this.sysOperateLogRepository = repository;
    }

    /**
     * 保存系统操作日志
     * @param sysOperateLog         操作日志对象
     * @return
     */
    @Override
    public SysOperateLog saveLog ( SysOperateLog sysOperateLog ) {
        return insert( sysOperateLog );
    }

}
