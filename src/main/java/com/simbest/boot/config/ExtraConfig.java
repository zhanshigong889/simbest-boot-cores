/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.core.env.Environment;

/**
 * 用途：针对不同项目，读取额外的特殊配置
 * 作者: lishuyi
 * 时间: 2018/5/28  18:04
 */
@Configuration
@PropertySources(value = {@PropertySource(value = {"classpath:extra.properties",
        "classpath:application.properties", "classpath:fastdfs-client.properties"})})
public class ExtraConfig {

    @Autowired
    private Environment environment;

    public String getValue(String key) {
        return environment.getProperty(key);
    }
}
