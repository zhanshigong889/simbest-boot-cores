/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.util.distribution.id;

import com.github.wenhao.jpa.Specifications;
import com.google.common.base.Strings;
import com.simbest.boot.base.service.IGenericService;
import com.simbest.boot.component.distributed.lock.DistributedRedisLock;
import com.simbest.boot.config.AppConfig;
import com.simbest.boot.constants.ApplicationConstants;
import com.simbest.boot.util.DateUtil;
import com.simbest.boot.util.distribution.id.model.SysRedisIdKey;
import com.simbest.boot.util.redis.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;

import static com.simbest.boot.config.MultiThreadConfiguration.MULTI_THREAD_BEAN;
import static jodd.util.StringPool.ZERO;

/**
 * 用途：基于Redis分布式下全局的ID，保证ID生成的顺序性、无重复性、高可用，
 * 利用Redis排除了受单点故障问题的影响，生成ID规则： 年份（2位） + 一年中第几天 （3位）+ 小时（2位） +
 * Redis自增序号 （6位），总计13位， 支持一小时内近100w个订单号的生成和使用， 比如 1813217008249
 * 作者: lishuyi
 * 时间: 2018/5/12  17:06
 */

@Slf4j
@Component
@DependsOn(value = {"redisUtil"})
public class RedisIdGenerator {

    public static int DEFAULT_FORMAT_ADD_LENGTH = 3;

    public static int LOCK_WAIT_TIME_SECONDS = 5000;

    @Autowired
    private AppConfig appConfig;

    @Autowired
    @Qualifier("sysRedisIdKeyService")
    private IGenericService<SysRedisIdKey, String> sysRedisIdKeyService;

//    @Autowired
//    private SysRedisIdKeyRepository sysRedisIdKeyRepository;

    /**
     * 增加Id值(与数据字典绑定)
     * @param cacheName 内部缓存空间名称
     * @param prefix    缓存前缀
     * @param length    编号生成长度
     * @return
     */
    @Async(MULTI_THREAD_BEAN)
    public String incrId(String cacheName, String prefix, int length) {
        //分布式加锁
        String rediskey = appConfig.getAppcode().concat(ApplicationConstants.COLON).concat(cacheName).concat(ApplicationConstants.COLON).concat(prefix);
        DistributedRedisLock.lock(rediskey, LOCK_WAIT_TIME_SECONDS);
        log.info("【{}】已获得锁【{}】", Thread.currentThread().getName(), rediskey);
        Long currentIndex = RedisUtil.getBean(rediskey, Long.class);
        //缓存没有
        if(null == currentIndex){
            log.debug("根据Redis的类型键值【{}】无法读取缓存", rediskey);
            Specification<SysRedisIdKey> specification = Specifications.<SysRedisIdKey>and()
                    .eq("name", rediskey)
                    .build();
            SysRedisIdKey sysRedisIdKey = sysRedisIdKeyService.findOne(specification);
//            SysRedisIdKey sysRedisIdKey = sysRedisIdKeyRepository.findByName(rediskey);
            //缓存没有，数据库也没有，缓存和数据库都初始为1
            if (sysRedisIdKey == null) {
                currentIndex = Long.parseLong(ZERO);
                log.debug("Redis-null-Database-null，数据库通过Redis键值【{}】获取字典值为空，即将写入初始值", rediskey);
                sysRedisIdKey = SysRedisIdKey.builder().name(rediskey).value(currentIndex).build();
                sysRedisIdKeyService.insert(sysRedisIdKey);
//                sysRedisIdKeyRepository.saveAndFlush(sysRedisIdKey);
                RedisUtil.setBean(rediskey, currentIndex);
                log.debug("数据库键值【{}】已完成数据库和缓存初始化，数据库初始化值为【{}】", rediskey, currentIndex);
            }
            //缓存没有，数据库有，缓存以数据库为准
            else{
                //将当前值集值作为起始编号
                currentIndex = sysRedisIdKey.getValue();
                log.debug("Redis-null-Database-notnull，数据库通过键值【{}】获取字典值不为空,即将写入值【{}】", rediskey, currentIndex);
                RedisUtil.setBean(rediskey, currentIndex);
                log.debug("Redis键值【{}】已设置值为【{}】", rediskey, currentIndex);
            }
        }
        else{
            log.debug("Redis-notnull，Redis键值【{}】值为【{}】", rediskey, currentIndex);
        }

        //Redis递增
        Long incIndex = RedisUtil.incrBy(rediskey);
        log.debug("Redis键值【{}】的值已更新为【{}】", rediskey, incIndex);
        saveRedisIndexToDatabase(rediskey, incIndex);

        // 字符串补位操作，length会通过format函数会自动加位数
        String formatter = "%1$0xd".replace("x", String.valueOf(length));
        String orderId = prefix.concat(String.format(formatter, incIndex));
        //分布式解锁
        DistributedRedisLock.unlock(rediskey);
        return Strings.isNullOrEmpty(orderId) ? null : orderId;
    }

    public void saveRedisIndexToDatabase(String rediskey, Long incIndex) {
        Specification<SysRedisIdKey> specification = Specifications.<SysRedisIdKey>and()
                .eq("name", rediskey)
                .build();
        SysRedisIdKey sysRedisIdKey = sysRedisIdKeyService.findOne(specification);
//        SysRedisIdKey sysRedisIdKey = sysRedisIdKeyRepository.findByName(rediskey);
        if(null == sysRedisIdKey){
            log.debug("database-null，数据库键值【{}】", rediskey);
            sysRedisIdKey = SysRedisIdKey.builder().name(rediskey).value(incIndex).build();
            sysRedisIdKeyService.insert(sysRedisIdKey);
//            sysRedisIdKeyRepository.saveAndFlush(sysRedisIdKey);
        }
        else {
            log.debug("database-notnull，数据库键值【{}】更新值为【{}】", rediskey, incIndex);
            sysRedisIdKey.setValue(incIndex);
            sysRedisIdKeyService.update(sysRedisIdKey);
//            sysRedisIdKeyRepository.saveAndFlush(sysRedisIdKey);
        }
        log.debug("数据库键值【{}】的值已更新为【{}】", rediskey, incIndex);
    }


    /**
     * @param prefix
     * @return cumtom001
     */
    public String getCustomPrefixId(String prefix) {
        // 转成数字类型，可排序
        return incrId(prefix, prefix, DEFAULT_FORMAT_ADD_LENGTH);
    }

    /**
     * @param prefix
     * @param length
     * @return cumtom001, 001的位数取决于传参length
     */
    public String getCustomPrefixId(String prefix, int length) {
        // 转成数字类型，可排序
        return incrId(prefix, prefix, length);
    }

    /**
     * @return 1836517001
     */
    public Long getDateHourId() {
        // 转成数字类型，可排序
        return getDateHourId("default");
    }

    /**
     * @param cacheName
     * @return 1836517001
     */
    public Long getDateHourId(String cacheName) {
        // 转成数字类型，可排序
        return getDateHourId(cacheName, DEFAULT_FORMAT_ADD_LENGTH);
    }

    /**
     * @param cacheName
     * @return 1836517001, 001的位数取决于传参length
     */
    public Long getDateHourId(String cacheName, int length) {
        // 转成数字类型，可排序
        return Long.parseLong(incrId(cacheName, DateUtil.getDateHourPrefix(new Date()), length));
    }

    /**
     * @return 181231001
     */
    public Long getDateId() {
        // 转成数字类型，可排序
        return getDateId("default");
    }

    /**
     * @param cacheName
     * @return 181231001
     */
    public Long getDateId(String cacheName) {
        // 转成数字类型，可排序
        return getDateId(cacheName, DEFAULT_FORMAT_ADD_LENGTH);
    }

    /**
     * @param cacheName
     * @return 181231001, 001的位数取决于传参length
     */
    public Long getDateId(String cacheName, int length) {
        // 转成数字类型，可排序
        return Long.parseLong(incrId(cacheName, DateUtil.getDatePrefix(new Date()), length));
    }

}
