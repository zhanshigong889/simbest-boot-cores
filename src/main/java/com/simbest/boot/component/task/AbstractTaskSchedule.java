/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.component.task;

import com.simbest.boot.base.exception.Exceptions;
import com.simbest.boot.component.distributed.lock.AppRuntimeMaster;
import com.simbest.boot.sys.model.SysTaskExecutedLog;
import com.simbest.boot.sys.repository.SysTaskExecutedLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * 用途：
 * 作者: lishuyi
 * 时间: 2018/5/29  15:51
 */
@Slf4j
public abstract class AbstractTaskSchedule {

    public final static String CHECK_SUCCESS = "PASS";

    public final static String CHECK_FAILED = "FAILED";

    private AppRuntimeMaster appRuntime;

    private SysTaskExecutedLogRepository repository;

    public AbstractTaskSchedule(AppRuntimeMaster appRuntime, SysTaskExecutedLogRepository repository){
        this.appRuntime = appRuntime;
        this.repository = repository;
    }

    /**
     * 执行具体任务，并根据writeLog判断是否记录执行日志（需要子类使用@Scheduled明确执行周期）
     * @param writeLog
     */
    public void checkAndExecute(boolean writeLog) {
        if(appRuntime.getMyHost().equals(appRuntime.getMasterHost())
                && appRuntime.getMyPort().equals(appRuntime.getMasterPort())) {
            log.debug("当前主机地址【{}】运行端口【{}】是集群主控节点，即将执行定时任务", appRuntime.getMyHost(), appRuntime.getMyPort());
            Long beginTime = System.currentTimeMillis();
            Boolean executeFlag = true;
            String content = CHECK_FAILED;
            try {
                content = this.execute();
            } catch (Exception e) {
                executeFlag = false;
                log.error("Execute taskName with {} failed.", this.getClass().getSimpleName());
                Exceptions.printException(e);
            }
            if(writeLog) {
                Long endTime = System.currentTimeMillis();
                SysTaskExecutedLog log = SysTaskExecutedLog.builder()
                        .taskName(this.getClass().getSimpleName())
                        .hostname(appRuntime.getMyHost())
                        .port(appRuntime.getMyPort())
                        .durationTime(endTime - beginTime)
                        .content(StringUtils.substring(content, 0, 2000))
                        .executeFlag(executeFlag)
                        .build();
                repository.save(log);
            }
        } else {
            log.debug("集群主控节点主机地址【{}】运行端口【{}】,当前主机地址【{}】运行端口【{}】, 无法执行定时任务",
                    appRuntime.getMasterHost(), appRuntime.getMasterPort(), appRuntime.getMyHost(), appRuntime.getMyPort());
        }
    }

    /**
     * 执行具体任务，并记录执行日志（需要子类使用@Scheduled明确执行周期）
     */
    public void checkAndExecute() {
        checkAndExecute(true);
    }

    /**
     * 由子类具体实现执行任务
     * @return
     */
    public abstract String execute();


}
