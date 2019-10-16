/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.config;

import com.jcraft.jsch.Logger;
import com.simbest.boot.base.exception.Exceptions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.MultipartConfigElement;
import java.io.File;
import java.io.IOException;

/**
 * 用途：Springboot 上传文件临时目录设置
 * 作者: lishuyi
 * 时间: 2019/2/13  16:20
 */
@Slf4j
@Configuration
public class MultipartConfiguration {
    @Autowired
    private AppConfig appConfig;

    @Bean
    MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        //配置为用户部署目录下的springboottmp下
        String location = appConfig.getUploadTmpFileLocation();
        try {
            File tmpUploadDirectroy = new File(location);
            FileUtils.forceMkdir(tmpUploadDirectroy);
            log.debug("创建文件上传临时目录成功，临时路径地址：【{}】", tmpUploadDirectroy.getAbsolutePath());
        } catch (IOException e) {
            log.error("创建文件上传临时目录发生异常【{}】", e.getMessage());
            Exceptions.printException(e);
        }
        factory.setLocation(location);
        return factory.createMultipartConfig();
    }

}
