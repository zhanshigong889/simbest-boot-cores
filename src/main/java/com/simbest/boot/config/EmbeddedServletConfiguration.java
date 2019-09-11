/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.config;

import com.simbest.boot.component.GracefulShutdown;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Connector;
import org.apache.commons.lang3.StringUtils;
import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    private AppConfig appConfig;

    @Autowired
    private Environment env;

    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxFileSize;

    @Bean
    public GracefulShutdown gracefulShutdown() {
        return new GracefulShutdown();
    }

    @Bean
    public TomcatServletWebServerFactory containerFactory(final GracefulShutdown gracefulShutdown) {
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
//                    ((AbstractHttp11Protocol <?>) connector.getProtocolHandler()).setMaxSwallowSize(-1);
                    Http11NioProtocol protocol = (Http11NioProtocol)connector.getProtocolHandler();
                    protocol.setMaxSwallowSize(-1);
                    log.info("应用支持的附件上传最大限制为【{}】",maxFileSize);

                    //设置最大连接数
                    protocol.setMaxConnections(200);
                    //设置最大线程数
                    protocol.setMaxThreads(300);
                    //连接超时时间
                    protocol.setConnectionTimeout(10000);

                    serverPort = connector.getPort();
                    log.info("内置Tomcat将在端口【{}】启动", serverPort);
                }
            }
        };

        //判断是否需要强制启动一个http监听端口
        if(StringUtils.isNotEmpty(env.getProperty("force.another.http.port"))) {
            tomcat.addAdditionalTomcatConnectors(createStandardConnector());
        }

        //根据kill PID信号，执行应用关闭销毁前的动作
        log.debug("【{}】即将被关闭，开始销毁前清理工作................................", appConfig.getAppcode());
        tomcat.addConnectorCustomizers(gracefulShutdown);

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
