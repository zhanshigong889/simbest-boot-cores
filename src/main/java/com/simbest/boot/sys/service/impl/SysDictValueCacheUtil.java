/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.sys.service.impl;

import com.simbest.boot.config.AppConfig;
import com.simbest.boot.constants.ApplicationConstants;
import com.simbest.boot.sys.model.SysDictValue;
import com.simbest.boot.util.redis.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 用途：数据字典值缓存工具类
 * 作者: lishuyi
 * 时间: 2019/12/22  22:53
 */
@Slf4j
@Component
public class SysDictValueCacheUtil {
    public static final String CACHE_KEY = "SYS_DICT_VALUE_CACHE:";
    public static final int CACHE_EXPIRE_SECONDES = 3600;

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private RedisTemplate<String, SysDictValue> dvRedisTemplate;

    @Autowired
    private RedisTemplate<String, List<SysDictValue>> dvListRedisTemplate;

    private String blocidCorpIdDictTypeNameKey(String dictType, String name, String blocid, String corpid) {
        String key = CACHE_KEY.concat(appConfig.getAppcode()).concat(ApplicationConstants.COLON)
                .concat(dictType).concat(ApplicationConstants.COLON).concat(name)
                .concat(ApplicationConstants.COLON).concat(blocid)
                .concat(ApplicationConstants.COLON).concat(corpid);
        return key;
    }

    public SysDictValue loadByDictTypeAndNameAndBlocidAndCorpid(String dictType, String name, String blocid, String corpid) {
        String key = blocidCorpIdDictTypeNameKey(dictType, name, blocid, corpid);
        SysDictValue dv = dvRedisTemplate.opsForValue().get(key);
        log.debug("通过Key键【{}】返回数据字典值【{}】", key, dv);
        return dv;
    }

    public void setByDictTypeAndNameAndBlocidAndCorpid(String dictType, String name, String blocid, String corpid, SysDictValue dv) {
        String key = blocidCorpIdDictTypeNameKey(dictType, name, blocid, corpid);
        dvRedisTemplate.opsForValue().set(key, dv, CACHE_EXPIRE_SECONDES, TimeUnit.SECONDS);
    }




    private String dictTypeNameKey(String dictType, String name) {
        String key = CACHE_KEY.concat(appConfig.getAppcode()).concat(ApplicationConstants.COLON)
                .concat(dictType).concat(ApplicationConstants.COLON).concat(name);
        return key;
    }

    public SysDictValue loadByDictTypeAndName(String dictType, String name) {
        String key = dictTypeNameKey(dictType, name);
        SysDictValue dv = dvRedisTemplate.opsForValue().get(key);
        log.debug("通过Key键【{}】返回数据字典值【{}】", key, dv);
        return dv;
    }

    public void setByDictTypeAndName(String dictType, String name, SysDictValue dv) {
        String key = dictTypeNameKey(dictType, name);
        dvRedisTemplate.opsForValue().set(key, dv, CACHE_EXPIRE_SECONDES, TimeUnit.SECONDS);
    }




    private String parametersKey(List<String> params) {
        String key = CACHE_KEY.concat(appConfig.getAppcode()).concat(ApplicationConstants.COLON)
                + StringUtils.join(params, ApplicationConstants.LINE);
        return key;
    }

    public List<SysDictValue> loadByParameters(List<String> params) {
        String key = parametersKey(params);
        List<SysDictValue> listDv = dvListRedisTemplate.opsForValue().get(key);
        log.debug("通过Key键【{}】返回数据字典值【{}】", key, listDv);
        return listDv;
    }

    public void setByParameters(List<String> params, List<SysDictValue> listDv) {
        String key = parametersKey(params);
        dvListRedisTemplate.opsForValue().set(key, listDv, CACHE_EXPIRE_SECONDES, TimeUnit.SECONDS);
    }


    public void expireAllCache(){
        RedisUtil.mulDeleteGlobal(CACHE_KEY.concat(appConfig.getAppcode()).concat(ApplicationConstants.COLON));
    }

}
