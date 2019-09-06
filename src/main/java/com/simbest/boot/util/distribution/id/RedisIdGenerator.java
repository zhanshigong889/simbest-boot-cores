/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.util.distribution.id;

import com.github.wenhao.jpa.Specifications;
import com.google.common.base.Strings;
import com.simbest.boot.base.exception.Exceptions;
import com.simbest.boot.component.distributed.lock.DistributedRedisLock;
import com.simbest.boot.config.AppConfig;
import com.simbest.boot.constants.ApplicationConstants;
import com.simbest.boot.security.IUser;
import com.simbest.boot.sys.model.SysDict;
import com.simbest.boot.sys.model.SysDictValue;
import com.simbest.boot.sys.service.ISysDictService;
import com.simbest.boot.sys.service.ISysDictValueService;
import com.simbest.boot.util.DateUtil;
import com.simbest.boot.util.redis.RedisUtil;
import com.simbest.boot.util.security.LoginUtils;
import com.simbest.boot.util.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;

/**
 * 用途：基于Redis分布式下全局的ID，保证ID生成的顺序性、无重复性、高可用，
 * 利用Redis排除了受单点故障问题的影响，生成ID规则： 年份（2位） + 一年中第几天 （3位）+ 小时（2位） +
 * Redis自增序号 （6位），总计13位， 支持一小时内近100w个订单号的生成和使用， 比如 1813217008249
 * 作者: lishuyi
 * 时间: 2018/5/12  17:06
 */
@Component
@Slf4j
public class RedisIdGenerator {

    public static int DEFAULT_FORMAT_ADD_LENGTH = 3;

    @Autowired
    private ISysDictService dictService;

    @Autowired
    private ISysDictValueService dictValueService;

    @Autowired
    private LoginUtils loginUtils;

    @Autowired
    private AppConfig appConfig;

    /**
     * 增加Id值(与数据字典绑定)
     * @param cacheName 内部缓存空间名称
     * @param prefix    缓存前缀
     * @param length    编号生成长度
     * @return
     */
    private String incrId(String cacheName, String prefix, int length) {
        String orderId = null;
        String distributedKey = this.getClass().getSimpleName() + ApplicationConstants.COLON+ cacheName + ApplicationConstants.COLON + prefix;
        try {
            DistributedRedisLock.lock(distributedKey, ApplicationConstants.REDIS_LOCK_TIMEOUT);
            IUser currentUser = SecurityUtils.getCurrentUser();
            if (null == currentUser) {
                loginUtils.adminLogin();
            }
            String rediskey = appConfig.getAppcode().concat(ApplicationConstants.COLON).concat(cacheName).concat(ApplicationConstants.COLON).concat(prefix);
            //查询数据字典类型为genCode的数据字典
            Specification<SysDict> dictCondition = Specifications.<SysDict>and()
                    .eq("dictType", "genCode").build();
            Iterable<SysDict> dicts = dictService.findAllNoPage(dictCondition);
            //如果类型为genCode的数据字典不存在，则需要创建一条类型为genCode的数据字典
            SysDict dict = null;
            if (!dicts.iterator().hasNext()) {
                dict = new SysDict();
                dict.setDictType("genCode");
                dict.setName("工单编号值集");
                dict.setDisplayOrder(1);
                dictService.insert(dict);
            }
            // 如果存在工单编号值集genCode，则不需要新建记录
            else {
                dict = dicts.iterator().next();
            }
            //查询数据字典类型为genCode，名称为cacheName+prefix的数据字典值
            Specification<SysDictValue> dictValueCondition = Specifications.<SysDictValue>and()
                    .eq("dictType", "genCode").eq("name", rediskey)
                    .build();
            Iterable<SysDictValue> dictValues = dictValueService.findAllNoPage(dictValueCondition);
            //如果应用的数据字典值集不存在代码生成的字典值的值集，则需要创建一条记录
            SysDictValue dictValue = null;
            if (!dictValues.iterator().hasNext()) {
                dictValue = new SysDictValue();
                dictValue.setDictType(dict.getDictType());
                dictValue.setDisplayOrder(dict.getDisplayOrder());
                dictValue.setName(rediskey);
                dictValue.setValue("0"); //从0开始，自增后实际从1开始
                dictValueService.insert(dictValue);
            }
            //如果存在，则取当前当前值集值，用来递增
            else {
                dictValue = dictValues.iterator().next();
            }
            //将当前值集值作为起始编号
            RedisUtil.setBean(rediskey, Long.parseLong(dictValue.getValue()));
            //进行递增
            Long index = RedisUtil.incrBy(rediskey);
            dictValue.setValue(String.valueOf(index));
            //递增结果保存持久化到数据库
            dictValueService.update(dictValue);
            // 字符串补位操作，length会通过format函数会自动加位数
            String formatter = "%1$0xd".replace("x", String.valueOf(length));
            orderId = prefix.concat(String.format(formatter, index));

        } catch (Exception e){
            log.error("Redis生成序列发生异常【{}】", e.getMessage());
            Exceptions.printException(e);
        } finally {
            DistributedRedisLock.unlock(distributedKey);
        }
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
