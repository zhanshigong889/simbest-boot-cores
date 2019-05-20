/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.util.redis;

import com.simbest.boot.constants.AuthoritiesConstants;
import com.simbest.boot.constants.ErrorCodeConstants;
import com.simbest.boot.exceptions.AttempMaxLoginFaildException;

import java.util.concurrent.TimeUnit;

/**
 * 用途：在Redis中记录尝试登录的次数
 * 作者: lishuyi
 * 时间: 2019/5/20  20:11
 */
public class RedisRetryLoginCache {

    /**
     * 登录前校验尝试登录的发生次数, 最大错误登录次数不超过5次可尝试进行登录
     * @param username
     */
    public static void preCheckTryTimes(String username){
        String key = AuthoritiesConstants.LOGIN_FAILED_KEY + username;
        Integer failedTimes = RedisUtil.getBean(key, Integer.class);
        if(null != failedTimes && failedTimes >= AuthoritiesConstants.ATTEMPT_LOGIN_MAX_TIMES){
            throw new AttempMaxLoginFaildException(ErrorCodeConstants.LOGIN_ERROR_EXCEED_MAX_TIMES);
        }
    }

    /**
     * 登录成功后，立即清除失败缓存，不再等待错误缓存的到期时间
     * @param username
     */
    public static void cleanTryTimes(String username){
        String key = AuthoritiesConstants.LOGIN_FAILED_KEY + username;
        Boolean value = RedisUtil.delete(key);
    }

    /**
     * 登录发生错误计数，每错误一次，即向后再延时等待5分钟
     * @param username
     */
    public static void addTryTimes(String username){
        String key = AuthoritiesConstants.LOGIN_FAILED_KEY + username;
        Integer failedTimes = RedisUtil.getBean(key, Integer.class);
        failedTimes = null == failedTimes ? AuthoritiesConstants.ATTEMPT_LOGIN_INIT_TIMES : failedTimes + AuthoritiesConstants.ATTEMPT_LOGIN_INIT_TIMES;
        RedisUtil.setBean(key, failedTimes);
        RedisUtil.expire(key, AuthoritiesConstants.ATTEMPT_LOGIN_FAILED_WAIT_SECONDS, TimeUnit.SECONDS);
    }

}
