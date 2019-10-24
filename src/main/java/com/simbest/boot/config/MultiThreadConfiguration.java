/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 用途：Spring Boot使用@Async实现异步调用：自定义线程池
 * 作者: lishuyi
 * 时间: 2019/9/5  17:08
 * 参考：http://blog.didispace.com/springbootasync-2/
 *      https://www.cnblogs.com/sxdcgaq8080/p/8074567.html
 */
@EnableAsync
@Configuration
public class MultiThreadConfiguration {

    @Autowired
    private AppConfig appConfig;

    /**
     * 核心线程数10：线程池创建时候初始化的线程数
     * 最大线程数20：线程池最大的线程数，只有在缓冲队列满了之后才会申请超过核心线程数的线程
     * 缓冲队列200：用来缓冲执行任务的队列
     * 允许线程的空闲时间60秒：当超过了核心线程出之外的线程在空闲时间到达之后会被销毁
     * 线程池名的前缀：设置好了之后可以方便我们定位处理任务所在的线程池
     * 线程池对拒绝任务的处理策略：这里采用了CallerRunsPolicy策略，当线程池没有处理能力的时候，该策略会直接在 execute 方法的调用线程中运行被拒绝的任务；如果执行程序已关闭，则会丢弃该任务
     * @return
     */
    @Bean("simbestThreadExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(appConfig.getThreadCorePoolSize()); // 线程池大小
        executor.setMaxPoolSize(appConfig.getThreadMaxPoolSize()); // 线程池最大线程数
        executor.setQueueCapacity(appConfig.getThreadQueueCapacity()); // 最大等待任务数
        executor.setKeepAliveSeconds(appConfig.getThreadKeepAliveSeconds()); // 空闲时间
        executor.setThreadNamePrefix("simbestThreadExecutor-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;
    }

}
