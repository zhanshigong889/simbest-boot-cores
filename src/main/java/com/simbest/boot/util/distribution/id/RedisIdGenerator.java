/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.util.distribution.id;

import com.github.wenhao.jpa.Specifications;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.simbest.boot.config.MultiThreadConfiguration.MULTI_THREAD_BEAN;
import static jodd.util.StringPool.ZERO;

/**
 * 用途：基于Redis分布式下全局的ID，保证ID生成的顺序性、无重复性、高可用，
 * 利用Redis排除了受单点故障问题的影响，生成ID规则： 年份（2位） + 一年中第几天 （3位）+ 小时（2位） +
 * Redis自增序号 （6位），总计13位， 支持一小时内近100w个订单号的生成和使用， 比如 1813217008249
 * 作者: lishuyi
 * 时间: 2018/5/12  17:06
 *
 * 分布式衍生阅读
 * https://blog.csdn.net/wangyj1992/article/details/79446808
 * https://dbaplus.cn/news-159-2469-1.html
 * https://youzhixueyuan.com/3-implementations-of-distributed-locks.html
 *
 */

@Slf4j
@Component
@DependsOn(value = {"redisUtil"})
public class RedisIdGenerator {

    public static int DEFAULT_FORMAT_ADD_LENGTH = 3;

    public static int LOCK_WAIT_SECONDS = 2;

    public static int LOCK_RELEASE_SECONDS = 3;

    @Autowired
    private AppConfig appConfig;

    @Autowired
    @Qualifier("sysRedisIdKeyService")
    private IGenericService<SysRedisIdKey, String> sysRedisIdKeyService;

    // day-name-value
    private Map<String, Map<String,Long>> rediskeyMap = Maps.newHashMap();

    @PostConstruct
    public void startup(){
        log.debug("START***********************应用即将启动，从数据库提取当天REDIS ID保存至JVM Map***********************START");
        String today = DateUtil.getCurrentStr();
        Specification<SysRedisIdKey> specification = Specifications.<SysRedisIdKey>and().eq("day", today).build();
        Iterable<SysRedisIdKey> currentDayKeys = sysRedisIdKeyService.findAllNoPage(specification);
        Map<String,Long> todayRediskeyMap = StreamSupport.stream(currentDayKeys.spliterator(), false)
                .collect(Collectors.toMap(SysRedisIdKey::getName, SysRedisIdKey::getValue));
        todayRediskeyMap.entrySet().forEach(entry -> log.debug("Key键【{}】Value值【{}】", entry.getKey(), entry.getValue()));
        rediskeyMap.put(today, todayRediskeyMap);
        log.debug("END***********************应用即将启动，从数据库提取当天REDIS ID保存至JVM Map***********************END");
    }

    @PreDestroy
    public void shutdown(){
        log.debug("START***********************应用即将关闭，将当天留存在JVM Map中REDIS ID保存至数据库***********************START");
        String today = DateUtil.getCurrentStr();
        Map<String,Long> todayRediskeyMap = rediskeyMap.get(today);
        if(null != todayRediskeyMap){
            todayRediskeyMap.entrySet().forEach(entry -> {
                SysRedisIdKey sysRedisIdKey = SysRedisIdKey.builder().day(today).name(entry.getKey()).value(entry.getValue()).build();
                sysRedisIdKeyService.insert(sysRedisIdKey);
            });
        }
        log.debug("END***********************应用即将关闭，将当天留存在JVM Map中REDIS ID保存至数据库***********************END");
    }

    /**
     * 每天12：10重置rediskeyMap，确保隔天数据不堆积在JVM中
     */
    @Scheduled(cron = "0 10 0 * * ?")
    public void doTask() {
        Map<String,Long> todayRediskeyMap = Maps.newHashMap();
        rediskeyMap = Maps.newHashMap();
        rediskeyMap.put(DateUtil.getCurrentStr(), todayRediskeyMap);
    }

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
        String today = DateUtil.getCurrentStr();
        String rediskey = appConfig.getAppcode().concat(ApplicationConstants.COLON).concat(cacheName).concat(ApplicationConstants.COLON).concat(prefix);
        DistributedRedisLock.tryLock(rediskey, LOCK_WAIT_SECONDS, LOCK_RELEASE_SECONDS);
        log.info("【{}】已获得RedisIdGenerator锁【{}】", Thread.currentThread().getName(), rediskey);
        Long currentIndex = RedisUtil.getBean(rediskey, Long.class);
        //缓存没有
        if(null == currentIndex){
            log.debug("RedisNull-键值【{}】无法读取缓存", rediskey);
            currentIndex = rediskeyMap.get(today).get(rediskey);
            //缓存没有，JVM也没有，缓存和JVM都初始为0
            if (null == currentIndex) {
                Long initCurrentIndex = Long.parseLong(ZERO);
                log.debug("RedisNull-JVMNull-键值【{}】无法读取缓存", rediskey);
                Map<String,Long> todayRediskeyMap = new HashMap<String, Long>(){{
                    put(rediskey, initCurrentIndex);
                }};
                rediskeyMap.put(today, todayRediskeyMap);
                RedisUtil.setBean(rediskey, initCurrentIndex);
                log.debug("JVM和Redis键值【{}】已初始化为【{}】", rediskey, currentIndex);
            }
            //缓存没有，数据库有，缓存以数据库为准
            else{
                //将当前值集值作为起始编号
                log.debug("RedisNull-JVMNotNull，JVM通过键值【{}】获取字典值为【{}】", rediskey, currentIndex);
                RedisUtil.setBean(rediskey, currentIndex);
                log.debug("Redis键值【{}】已设置值为【{}】", rediskey, currentIndex);
            }
        }
        else{
            log.debug("RedisNotNull，Redis通过键值【{}】获取字典值为【{}】", rediskey, currentIndex);
        }

        //Redis递增
        Long incIndex = RedisUtil.incrBy(rediskey);
        log.debug("Redis键值【{}】的值已更新为【{}】", rediskey, incIndex);
        Map<String,Long> todayRediskeyMap = rediskeyMap.get(today);
        todayRediskeyMap.put(rediskey, incIndex);
        log.debug("JVM键值【{}】的值已更新为【{}】", rediskey, incIndex);

        // 字符串补位操作，length会通过format函数会自动加位数
        String formatter = "%1$0xd".replace("x", String.valueOf(length));
        String orderId = prefix.concat(String.format(formatter, incIndex));
        //分布式解锁
        DistributedRedisLock.unlock(rediskey);
        log.info("【{}】已释放RedisIdGenerator锁【{}】", Thread.currentThread().getName(), rediskey);
        return Strings.isNullOrEmpty(orderId) ? null : orderId;
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
