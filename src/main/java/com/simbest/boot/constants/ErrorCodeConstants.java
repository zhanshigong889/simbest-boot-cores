/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.constants;

/**
 * 用途：
 * 作者: lishuyi
 * 时间: 2018/3/24  17:24
 */
public class ErrorCodeConstants {
    public static final Integer ERRORCODE_LOGIN_APP_UNREGISTER_GROUP = 10;
    public static final Integer ERRORCODE_ATTACHMENT_SIZE_EXCEEDS = 20;

    public static final String LOGIN_ERROR_INVALIDATE_USER = "用户信息错误";
    public static final String LOGIN_ERROR_INVALIDATE_USERNAME_PASSWORD = "用户名密码参数错误";
    public static final String LOGIN_ERROR_INVALIDATE_CODE = "验证码错误";
    public static final String LOGIN_ERROR_BAD_CREDENTIALS = "密码校验错误";
    public static final String LOGIN_ERROR_EXCEED_MAX_TIMES = "尝试登录错误超过最大次数";
    public static final String LOGIN_APP_UNREGISTER_GROUP = "用户未注册此应用访问群组";

    public static final String SUCCESS_MSG = "操作成功!";
    public static final String UNKNOW_ERROR = "未知异常";
}
