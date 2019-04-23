/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

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
@Data
@Configuration
public class AppConfig {

    @Value("${logback.artifactId}")
    private String appcode;

    @Value("${server.servlet.context-path}")
    private String contextPath;

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

    @Value("${spring.session.cookie.path:}")
    private String cookiePath;

    @Value("${app.swagger.address}")
    private String swaggerUrl;

    // 是否开启验证码功能
    @Value("${app.captcha.enable}")
    private boolean isOpenValidateCode = true;

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
}
