/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.component.task;

import com.mzlion.easyokhttp.HttpClient;
import com.simbest.boot.component.distributed.lock.AppRuntimeMaster;
import com.simbest.boot.config.AppConfig;
import com.simbest.boot.constants.ApplicationConstants;
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
 * 用途：心跳测试服务定时器
 * 作者: lishuyi
 * 时间: 2018/5/29  18:53
 */
@Slf4j
@Component
public class HeartTestTask extends AbstractTaskSchedule {

    @Autowired
    private ApplicationContext appContext;

    @Autowired
    private AppConfig config;

    @Autowired
    private HostUtil hostUtil;

    @Autowired
    public HeartTestTask(AppRuntimeMaster master, SysTaskExecutedLogRepository repository) {
        super(master, repository);
    }

    @Scheduled(cron = "${app.task.heart.test.job}")
    public void checkAndExecute() {
        super.checkAndExecute(false);
    }

    /**
     * @see com.simbest.boot.sys.service.ISysHealthService
     * @return
     */
    @Override
    public String execute() {
        if(config.isOpenHeartCheck()) {
            Map<String, IHeartTestService> heartTests = appContext.getBeansOfType(IHeartTestService.class);
            if (heartTests.size() > 0) {
                for (Map.Entry<String, IHeartTestService> entry : heartTests.entrySet()) {
                    entry.getValue().doTest();
                }
            } else {
                String testUrl = "http://localhost:" + hostUtil.getRunningPort() + config.getContextPath() + ApplicationConstants.LOGIN_PAGE;
                String response = HttpClient
                        // 请求方式和请求url
                        .get(testUrl)
                        .asString();
                if (StringUtils.contains(response, "username")) {
                    log.debug("Heart test login url check ok!");
                } else {
                    log.debug("Heart test login url check failed!");
                    return CHECK_FAILED;
                }
            }
        }
        return CHECK_SUCCESS;
    }
}
