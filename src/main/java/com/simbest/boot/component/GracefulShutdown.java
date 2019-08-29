/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.component;

import com.simbest.boot.constants.ApplicationConstants;
import com.simbest.boot.util.redis.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Connector;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.simbest.boot.component.distributed.lock.DistributedRedisLock.REDISSON_LOCK;
import static com.simbest.boot.component.distributed.lock.DistributedRedisLock.TASK_SCHEDULE_LOCK;

/**
 * 用途：
 * 作者: lishuyi
 * 时间: 2019/8/29  14:49
 */
@Slf4j
public class GracefulShutdown implements TomcatConnectorCustomizer, ApplicationListener<ContextClosedEvent> {

    private static final int TIMEOUT = 30;

    private volatile Connector connector;

    @Override
    public void customize(Connector connector) {
        this.connector = connector;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        log.debug("应用即将被关闭，开始销毁前清理工作................................");
        RedisUtil.mulDelete(TASK_SCHEDULE_LOCK);
        Set<String> lockKeys = RedisUtil.getRedisTemplate().keys( REDISSON_LOCK + ApplicationConstants.STAR);
        RedisUtil.getRedisTemplate().delete(lockKeys);

        this.connector.pause();
        Executor executor = this.connector.getProtocolHandler().getExecutor();
        if (executor instanceof ThreadPoolExecutor) {
            try {
                ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executor;
                threadPoolExecutor.shutdown();
                if (!threadPoolExecutor.awaitTermination(TIMEOUT, TimeUnit.SECONDS)) {
                    log.warn("Tomcat线程池在【{}】秒内未成功结束，将手动执行关闭", TIMEOUT);
                    threadPoolExecutor.shutdownNow();
                    if (!threadPoolExecutor.awaitTermination(TIMEOUT, TimeUnit.SECONDS)) {
                        log.error("Tomcat线程池未能彻底关闭，请检查应用代码！！！");
                    }
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        log.debug("应用即将被关闭，销毁前清理工作执行完毕............................");
    }

}
