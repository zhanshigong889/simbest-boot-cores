/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.component.distributed.lock;

import com.simbest.boot.base.exception.Exceptions;
import com.simbest.boot.config.AppConfig;
import com.simbest.boot.constants.ApplicationConstants;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 用途：Redisson分布式锁和同步器
 * 参考：https://github.com/redisson/redisson/wiki/8.-%E5%88%86%E5%B8%83%E5%BC%8F%E9%94%81%E5%92%8C%E5%90%8C%E6%AD%A5%E5%99%A8
 * 作者: lishuyi
 * 时间: 2018/6/22  17:40
 */
@Component
public class DistributedRedisLock {

    private static long WAITING_FOR_LOCK_SECONDES = 10;
    
    @Autowired
    private RedissonClient redisson;

    @Autowired
    private AppConfig config;

    private static String redisKeyPrefix;

    private static DistributedRedisLock lockUtils;

    @PostConstruct
    public void init() {
        lockUtils = this;
        lockUtils.redisson = this.redisson;
        lockUtils.redisKeyPrefix = config.getRedisKeyPrefix();
    }

    /**
     * 强制获取锁
     * @param lockName
     */
    public static void lock(String lockName){
        String key = redisKeyPrefix + lockName;
        RLock mylock = lockUtils.redisson.getFairLock(key);
        mylock.lock();
    }

    /**
     * 强制获取锁
     * @param lockName
     */
    public static void lock(String lockName, int seconds){
        String key = redisKeyPrefix + lockName;
        RLock mylock = lockUtils.redisson.getFairLock(key);
        //lock提供带timeout参数，timeout结束强制解锁，防止死锁
        mylock.lock(seconds, ApplicationConstants.REDIS_LOCK_DEFAULT_TIME_UNIT);
    }

    /**
     * 强制释放锁
     * @param lockName
     */
    public static void unlock(String lockName){
        String key = redisKeyPrefix + lockName;
        RLock mylock = lockUtils.redisson.getFairLock(key);
        mylock.unlock();
    }


    /**
     * 尝试获得锁
     * @param lockName
     * @param callback
     * @param <T>
     * @return
     */
    public static <T> T tryLock(String lockName, DistributedLockCallback<T> callback){
        return tryLock(lockName, ApplicationConstants.REDIS_LOCK_RELEASE_TIMEOUT, ApplicationConstants.REDIS_LOCK_WAIT_TIMEOUT, callback);
    }

    /**
     * 尝试获得锁
     * @param lockName
     * @param waitSeconds
     * @param releaseSeconds
     * @param callback
     * @param <T>
     * @return
     */
    public static <T> T tryLock(String lockName, long waitSeconds, long releaseSeconds, DistributedLockCallback<T> callback){
        T returnObj = null;
        String key = redisKeyPrefix + lockName;
        RLock mylock = lockUtils.redisson.getFairLock(key);
        try {
            //最多等待60秒，获得锁后10秒自动解锁
            boolean locked = mylock.tryLock(waitSeconds, releaseSeconds, ApplicationConstants.REDIS_LOCK_DEFAULT_TIME_UNIT);
            if(locked){
                returnObj = callback.process();
            }
        } catch (InterruptedException e) {
            Exceptions.printException(e);
        } finally {
            mylock.unlock();
        }
        return returnObj;
    }
}
