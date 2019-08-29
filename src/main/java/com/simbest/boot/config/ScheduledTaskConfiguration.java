/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.Executors;

/**
 * 用途：定时任务配置
 * 作者: lishuyi
 * 时间: 2018/4/17  23:28
 * springboot中，默认的定时任务线程池是只有一个线程的，所以如果在一堆定时任务中，有一个发生了延时或者死循环之类的异常，很大可能会影响到其他的定时任务。
 *
 * 方案一 异步执行
 * 方案二 自定义定时任务线程池数量，如本例
 *
 * 两种方案对比
 * 两种方案都可以解决各个任务之间互相干扰的问题，但是需要根据实际情况选择合适的。我们就以上面出现死循环的代码来分析。
 * 如果在定时任务中真的发生了死循环，那么使用异步执行则会带来灾难性的后果。因为在定时任务这个线程中，每次任务执行完毕后，他会计算下次时间，再次添加一个任务进入异步线程池。而添加进异步线程池的任务因为死循环而一直占用着线程资源。随着时间的增加异步线程池的所有线程资源都会被死循环的任务占据，导致其他服务全部阻塞。
 * 而使用自定义定时任务线程池则会好一点，因为只有当任务执行完成后，才会计算时间，在执行下次任务。虽然因为死循环任务一直在执行，但是也顶多占据一个线程的资源，不至于更大范围的影响。
 *
 */
@Configuration
@EnableAsync
@EnableScheduling
public class ScheduledTaskConfiguration implements SchedulingConfigurer {

    @Override
    public void configureTasks( ScheduledTaskRegistrar taskRegistrar) {
        //设定一个长度10的定时任务线程池
        taskRegistrar.setScheduler( Executors.newScheduledThreadPool(10));
    }

}
