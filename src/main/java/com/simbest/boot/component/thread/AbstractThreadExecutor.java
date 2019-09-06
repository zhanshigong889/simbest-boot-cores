/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.component.thread;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.Future;

/**
 * 用途：Spring Boot使用@Async实现异步调用：自定义线程池
 * 作者: lishuyi
 * 时间: 2019/9/5  17:08
 * 参考：http://blog.didispace.com/springbootasync-2/
 *      https://www.cnblogs.com/sxdcgaq8080/p/8074567.html
 *
 * 线程参数配置位于：MultiThreadConfiguration
 */
@Slf4j
public abstract class AbstractThreadExecutor {

    @Async("simbestThreadExecutor")
    public abstract void executeNoReturn();

    @Async("simbestThreadExecutor")
    public abstract <T> Future<T> executeWithReturn();

}
