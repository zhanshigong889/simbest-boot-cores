/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.component.task;

import com.mzlion.easyokhttp.HttpClient;
import com.simbest.boot.component.distributed.lock.AppRuntimeMaster;
import com.simbest.boot.config.AppConfig;
import com.simbest.boot.constants.ApplicationConstants;
import com.simbest.boot.security.license.VerifyLicense;
import com.simbest.boot.sys.repository.SysTaskExecutedLogRepository;
import com.simbest.boot.sys.service.IHeartTestService;
import com.simbest.boot.util.server.HostUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * <strong>Title : LicenseCheckTask</strong><br>
 * <strong>Description : License检测是否过期定时任务</strong><br>
 * <strong>Create on : 2020/7/2</strong><br>
 * <strong>Modify on : 2020/7/2</strong><br>
 * <strong>Copyright (C) Ltd.</strong><br>
 *
 * @author LJW lijianwu@simbest.com.cn
 * @version <strong>V1.0.0</strong><br>
 *          <strong>修改历史:</strong><br>
 *          修改人 修改日期 修改描述<br>
 *          -------------------------------------------<br>
 */
@Slf4j
@Component
public class LicenseCheckTask extends AbstractTaskSchedule {

    @Autowired
    private ApplicationContext appContext;

    @Autowired
    private AppConfig config;

    @Autowired
    private HostUtil hostUtil;

    @Autowired
    private VerifyLicense verifyLicense;

    @Autowired
    public LicenseCheckTask(AppRuntimeMaster master, SysTaskExecutedLogRepository repository) {
        super(master, repository);
    }

    @Scheduled(cron = "* 0/30 * * * ?")
    public void checkAndExecute() {
        super.checkAndExecute(false);
    }

    @Override
    public String execute() {
        if (verifyLicense.vertify()) {
            Runtime.getRuntime().halt(1);
        }
        return CHECK_SUCCESS;
    }
}
