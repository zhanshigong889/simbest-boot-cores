/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.constants;

import java.util.concurrent.TimeUnit;

/**
 * 用途：应用常量
 * 作者: lishuyi
 * 时间: 2018/2/27  17:45
 */
public class ApplicationConstants {
    public static final String DEV = "dev";
    public static final String TEST = "test";
    public static final String UAT = "uat";
    public static final String PRD = "prd";
    public static final String OTHER = "other";

    public static final int ZERO = 0;
    public static final int ONE = 1;
    public static final Boolean TRUE = true;
    public static final Boolean FALSE = false;

    public static final String ISO8859 = "ISO8859-1";
    public static final String UTF_8 = "UTF-8";
    public static final String GB2312 = "GB2312";
    public static final String GBK = "GBK";

    public static final String Linux = "Linux";
    public static final String Windows = "Windows";

    public static final String PACKAGE_NAME = "com.simbest.boot";
    public static final String ROOT_PAGE = "/";
    public static final String ROOT_SSO_PAGE = "/sso";
    public static final String WELCOME_PAGE = "/welcome"; //站点首页，不需要session
    public static final String LOGIN_PAGE = "/login";
    public static final String LOGOUT_PAGE = "/logout";
    public static final String ERROR_PAGE = "/error";
    public static final String HOME_PAGE = "/home"; //自动跳转至index，需要session
    public static final String UUMS_LOGIN_PAGE = "/uumslogin";
    public static final String REST_UUMS_LOGIN_PAGE = "/restuumslogin";
    public static final String REST_UUMS_LOGOUT_PAGE = "/restuumslogout";
    public static final String CAS_LOGIN_PAGE = "/caslogin/cas";
    public static final String CAS_LOGOUT_PAGE = "/logout/cas";
    public static final String LOGIN_ERROR_PAGE = "/login?loginError";
    public static final String LOGIN_SESSION_CODE = "validateCode";
    public static final String LOGIN_VALIDATE_CODE = "verifyCode";
    public static final String ANY_PASSWORD = "SimBest_SSO_2018";

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 100;

    public static final String FORMAT_DATE_TIME = "yyyy-MM-dd HH:mm:ss";
    public static final String FORMAT_DATE = "yyyy-MM-dd";
    public static final String FORMAT_TIME = "HH:mm:ss";
    public static final String FORMAT_TIME_ZONE = "GMT+8";

    public final static String HTTPPROTOCAL = "http://";
    public final static String HTTP = "http";
    public final static String HTTPS = "https";
    public final static String HTTPGET = "GET";
    public final static String HTTPPOST = "POST";
    public final static String COLON = ":";
    public final static String DOT = ".";
    public final static String COMMA = ",";
    public final static String STAR = "*";
    public final static String LINE = "-";
    public final static String SQUOTE = "'";
    public final static String MYSQL_SQUOTE = "`";
    public final static String SPACE = " ";
    public final static String UNDERLINE = "_";
    public final static String PERCENT = "%";
    public final static String EMPTY = "";
    public final static String NULL = "null";
    public final static String NEWLINE = "\n";
    public final static String LEFT_BRACKET="(";
    public final static String RIGHT_BRACKET=")";
    public final static String AND = "&";
    public final static String EQUAL = "=";
    public final static String SEPARATOR = System.getProperty("file.separator");
    public final static String SLASH = "/";
    public final static String VERTICAL = "|";

    public final static String REDIS_DEFAULT_CACHE_PREFIX = "runtime";

    public final static String RSA_PUBLIC_KEY_PATH = "certificate/rsa/rsa_public_key.pem";
    public final static String RSA_PRIVATE_KEY_PATH = "certificate/rsa/pkcs8_private_key.pem";

    public final static int REDIS_LOCK_WAIT_TIMEOUT = 1;
    public final static int REDIS_LOCK_RELEASE_TIMEOUT = 10;
    public final static int REDIS_LOCK_TIMEOUT = 2;
    public final static TimeUnit REDIS_LOCK_DEFAULT_TIME_UNIT = TimeUnit.SECONDS;
    public final static String REDIS_TEMP_KEY = "_redis_tmp_";
    public final static String  MASTER_HOST = "master_ip";
    public final static String  MASTER_PORT = "master_port";
    public final static String  MASTER_LOCK = "master_lock";

    public static final String ADMINISTRATOR = "hadmin";

    public static final String MSG_SUCCESS = "操作成功";
    public static final String MSG_ERRO = "操作失败";
}
