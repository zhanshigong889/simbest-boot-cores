/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.component.distributed.lock;

/**
 * 用途：分布式锁回调接口
 * 参考：https://layznet.iteye.com/blog/2307179
 * 作者: lishuyi
 * 时间: 2018/6/22  15:05
 */
public interface DistributedLockCallback<T> {
    /**
     * 调用者必须在此方法中实现需要加分布式锁的业务逻辑
     *
     * @return
     */
    T process();

    /**
     * 得到分布式锁名称
     *
     * @return
     */
    String getLockName();

}
