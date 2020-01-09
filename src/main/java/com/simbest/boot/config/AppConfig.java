/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * 用途：应用配置
 * 参考: https://segmentfault.com/a/1190000016941757
 *
 * @ConfigurationProperties(prefix = "doc")
 *
 * private boolean preferIpAddress;
 * private int maxConnections=0;
 * private int port;
 * private AuthInfo authInfo;
 * private List<String> whitelist;
 * private Map<String,String> converter;
 * private List<Person> defaultShareUsers;
 *
 * doc.prefer-ip-address=true
 * doc.port=8080
 * doc.max-connections=30
 * #doc.whitelist=192.168.0.1,192.168.0.2
 * # 这种等同于下面的doc.whitelist[0] doc.whitelist[1]
 * doc.whitelist[0]=192.168.0.1
 * doc.whitelist[1]=192.168.0.2
 * doc.default-share-users[0].name=jack
 * doc.default-share-users[0].age=18
 * doc.converter.a=xxConverter
 * doc.converter.b=xxConverter
 * doc.auth-info.username=user
 * doc.auth-info.password=password
 *
 * 作者: lishuyi
 * 时间: 2018/8/16  13:52
 */
@Slf4j
@Data
@Configuration
public class AppConfig {

    @Value("${logback.artifactId}")
    private String appcode;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String datasourceUsername;

    @Value("${spring.datasource.password}")
    private String datasourcePassword;

    @Value("${spring.redis.cluster.nodes}")
    private String redisClusterNodes;

    @Value("${spring.redis.cluster.password}")
    private String redisPassword;

    @Value("${spring.redis.cluster.max-redirects}")
    private String redisMaxRedirects;

    @Value("${server.servlet.session.timeout}")
    private Integer redisMaxInactiveIntervalInSeconds;

    @Value("${spring.session.redis.namespace}")
    private String redisNamespace;

    @Value("${spring.cache.redis.key-prefix}")
    private String redisKeyPrefix;

    @Value("${spring.cache.redis.lock.wait.seconds:3}")
    private int redisLockWaitSeconds;

    @Value("${spring.cache.redis.lock.release.seconds:7200}")
    private int redisLockReleaseSeconds;

    @Value("${spring.session.cookie.path:}")
    private String cookiePath;

    @Value("${app.swagger.address}")
    private String swaggerUrl;

    // 是否开启验证码功能
    @Value("${app.captcha.enable}")
    private boolean isOpenValidateCode = true;

    // 是否开启心跳检测功能
    @Value("${app.heart.check.enable:false}")
    private boolean isOpenHeartCheck;

    @Value("${app.uums.address}")
    private String uumsAddress;

    @Value("${app.oa.portal.token:SIMBEST_SSO}")
    private String mochaPortalToken;

    @Value("${app.host.port}")
    private String appHostPort;

    @Value("${app.file.upload.path}")
    private String uploadPath;

    @Value("${app.file.upload.location}")
    private String uploadLocation;

    @Value("${app.security.white.hosts}")
    private String whiteHostList;

    @Value("${thread.core.pool.size:10}")
    private int threadCorePoolSize;
    @Value("${thread.max.pool.size:20}")
    private int threadMaxPoolSize;
    @Value("${thread.queue.capacity:200}")
    private int threadQueueCapacity;
    @Value("${thread.keep.alive.seconds:60}")
    private int threadKeepAliveSeconds;

    private String uploadTmpFileLocation;

    //简单实时短信接口配置
    @Value("${app.sms.account:8a48b5515018a0f40150467da6134cddsim}")
    private String smsAccount;
    @Value("${app.sms.token:6cde887f4355445fa2c16f9fb073fbf7be}")
    private String smsToken;
    @Value("${app.sms.appId:8a48b5515018a0f4015046d3765c4ea3st}")
    private String smsAppId;
    @Value("${app.sms.templateId:408992008}")
    private String smsTemplateId;

    @PostConstruct
    public void init() {
        log.info("Congratulations------------------------------------------------应用核心配置加载完成");
        log.info("应用注册代码【{}】", appcode);
        log.info("应用访问上下文【{}】", contextPath);
        log.info("应用获准访问白名单【{}】", whiteHostList);
        log.info("数据库URL【{}】", datasourceUrl);
        log.info("数据库账号【{}】", datasourceUsername);
        log.info("数据库密码【{}】", datasourcePassword);
        log.info("Redis节点【{}】", redisClusterNodes);
        log.info("Redis密码【{}】", redisPassword);
        log.info("Redis重定向次数【{}】", redisMaxRedirects);
        log.info("Redis缓存空间前缀【{}】", redisNamespace);
        log.info("Redis缓存Key键前缀【{}】", redisKeyPrefix);
        log.info("Redis缓存默认等待加锁时间【{}】秒", redisLockWaitSeconds);
        log.info("Redis缓存默认加锁后释放时间【{}】秒", redisLockReleaseSeconds);
        log.info("应用Cookie路径【{}】", cookiePath);
        //log.info("API接口文档地址【{}】", swaggerUrl);
        log.info("API接口文档地址【{}】", String.format("%s%s/swagger-ui.html", appHostPort, contextPath));
        log.info("登录是否开启验证码【{}】", isOpenValidateCode);
        log.info("主数据请求地址【{}】", uumsAddress);
        log.info("门户单点加密令牌【{}】", mochaPortalToken);
        log.info("应用访问地址【{}】", appHostPort);
        log.info("应用文件上传方式【{}】", uploadLocation);
        log.info("应用文件上传路径【{}】", uploadPath);
        log.info("多线程核心线程数【{}】", threadCorePoolSize);
        log.info("多线程最大线程数【{}】", threadMaxPoolSize);
        log.info("多线程缓冲队列【{}】", threadQueueCapacity);
        log.info("多线程空闲时间【{}】", threadKeepAliveSeconds);
        uploadTmpFileLocation = System.getProperty("user.dir").concat("/springboottmp").concat(contextPath);
        log.info("临时文件上传目录为【{}】", uploadTmpFileLocation);
        log.info("心跳定时器开关打开状态【{}】", isOpenHeartCheck ? true : false);
    }


}
