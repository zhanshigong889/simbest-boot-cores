/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.component;

import com.simbest.boot.base.exception.Exceptions;
import com.simbest.boot.config.AppConfig;
import com.simbest.boot.constants.ApplicationConstants;
import com.simbest.boot.util.redis.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Connector;
import org.apache.commons.io.FileUtils;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 用途：完美关闭Springboot应用
 * 作者: lishuyi
 * 时间: 2019/8/29  14:49
 */
@Slf4j
public class GracefulShutdown implements TomcatConnectorCustomizer, ApplicationListener<ContextClosedEvent> {

    private AppConfig appConfig;

    private static final int TIMEOUT = 30;

    private volatile Connector connector;

    public GracefulShutdown(AppConfig appConfig){
        this.appConfig = appConfig;
    }

    @Override
    public void customize(Connector connector) {
        this.connector = connector;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        log.debug("应用即将被关闭，销毁前清理工作START................................");
        this.connector.pause();
        Executor executor = this.connector.getProtocolHandler().getExecutor();
        log.debug("当前执行器为【{}】，匹配ThreadPoolExecutor结果为【{}】", executor, executor instanceof ThreadPoolExecutor);
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
                //程序销毁的时候， 删除reids缓存中放的tmp开头的临时变量
                RedisUtil.mulDelete(ApplicationConstants.REDIS_TEMP_KEY );
                //程序销毁的时候， 删除应用临时上传的文件
                FileUtils.cleanDirectory(new File(appConfig.getUploadTmpFileLocation()));
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                Exceptions.printException(e);
            }
        }
        log.debug("应用即将被关闭，销毁前清理工作END................................");
    }

}
