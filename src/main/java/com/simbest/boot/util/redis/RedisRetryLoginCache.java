/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.util.redis;

import com.simbest.boot.constants.ApplicationConstants;
import com.simbest.boot.constants.AuthoritiesConstants;
import com.simbest.boot.constants.ErrorCodeConstants;
import com.simbest.boot.exceptions.AttempMaxLoginFaildException;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * 用途：在Redis中记录尝试登录的次数
 * 作者: lishuyi
 * 时间: 2019/5/20  20:11
 */
@Slf4j
public class RedisRetryLoginCache {

    /**
     * 获取用户尝试错误登录的Cache Key
     * @param username
     * @return
     */
    public static String getKey(String username){
        String key = AuthoritiesConstants.LOGIN_FAILED_KEY + username;
        log.debug("当前用户【{}】错误登录缓存键值为【{}】", username, key);
        return key;
    }

    /**
     * 获取用户尝试登录的错误次数，可能返回null
     * @param username
     * @return
     */
    public static Integer getTryTimes(String username){
        Integer failedTimes = RedisUtil.getBean(getKey(username), Integer.class);
        failedTimes = null == failedTimes ? ApplicationConstants.ZERO:failedTimes;
        log.debug("当前用户【{}】错误登录已达到【{}】次", username, failedTimes);
        return failedTimes;
    }

    /**
     * 登录前校验尝试登录的发生次数, 最大错误登录次数不超过5次可尝试进行登录
     * @param username
     */
    public static void preCheckTryTimes(String username){
        Integer failedTimes = getTryTimes(username);
        if(null != failedTimes && failedTimes > AuthoritiesConstants.ATTEMPT_LOGIN_MAX_TIMES) {
            log.error("用户【{}】尝试错误登录已达到【{}】次，超过上限设置的【{}】次，锁定登录！", username, failedTimes, AuthoritiesConstants.ATTEMPT_LOGIN_MAX_TIMES);
            throw new AttempMaxLoginFaildException(ErrorCodeConstants.LOGIN_ERROR_EXCEED_MAX_TIMES);
        }
    }

    /**
     * 登录成功后，立即清除失败缓存，不再等待错误缓存的到期时间
     * @param username
     */
    public static void cleanTryTimes(String username){
        Boolean flag = RedisUtil.delete(getKey(username));
        log.debug("清空用户【{}】的历史登录错误记录数结果返回为【{}】", username, flag);
    }

    /**
     * 登录发生错误计数，每错误一次，即向后再延时等待5分钟
     * @param username
     */
    public static void addTryTimes(String username){
        Integer failedTimes = getTryTimes(username);
        failedTimes = null == failedTimes ? AuthoritiesConstants.ATTEMPT_LOGIN_INIT_TIMES : failedTimes + AuthoritiesConstants.ATTEMPT_LOGIN_INIT_TIMES;
        RedisUtil.setBean(getKey(username), failedTimes);
        RedisUtil.expire(getKey(username), AuthoritiesConstants.ATTEMPT_LOGIN_FAILED_WAIT_SECONDS, TimeUnit.SECONDS);
        log.debug("当前用户【{}】错误登录已达到【{}】次", username, failedTimes);
    }

}
