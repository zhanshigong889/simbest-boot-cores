/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Connector;
import org.apache.commons.lang3.StringUtils;
import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;

/**
 * 用途：内部Servlet容器配置信息
 * 作者: lishuyi
 * 时间: 2018/7/21  17:38
 */
@Slf4j
@Configuration
public class EmbeddedServletConfiguration {

    public static int serverPort;

    @Autowired
    private Environment env;

    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxFileSize;

    @Bean
    public TomcatServletWebServerFactory containerFactory() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {
            protected void customizeConnector(Connector connector) {
                super.customizeConnector(connector);
                if (connector.getProtocolHandler() instanceof AbstractHttp11Protocol) {
                    /**
                     * 通过设置MaxSwallowSize，解决在发生MaxUploadSizeExceededException异常时，Tomcat不停的尝试上传，导致前端拿不到错误信息
                     * 参考：
                     * http://www.baeldung.com/spring-maxuploadsizeexceeded
                     * https://blog.csdn.net/a349687999/article/details/81120091
                     * https://github.com/Apress/beg-spring-boot-2/blob/master/chapter-10/springboot-thymeleaf-demo/src/main/java/com/apress/demo/config/WebConfig.java
                     */
                    ((AbstractHttp11Protocol <?>) connector.getProtocolHandler()).setMaxSwallowSize(-1);
                    log.info("应用支持的附件上传最大限制为【{}】",maxFileSize);
                    serverPort = connector.getPort();
                    log.info("内置Tomcat将在端口【{}】启动", serverPort);
                }
            }
        };
        if(StringUtils.isNotEmpty(env.getProperty("force.another.http.port"))) {
            tomcat.addAdditionalTomcatConnectors(createStandardConnector());
        }
        return tomcat;
    }

    /**
     * 强制确保http协议的监听端口可用
     * @return
     */
    private Connector createStandardConnector() {
        int anotherHttpPort = Integer.parseInt(env.getProperty("force.another.http.port"));
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setPort(anotherHttpPort);
        log.info("内置Tomcat将再监听一个端口【{}】启动", anotherHttpPort);
        return connector;
    }

}
