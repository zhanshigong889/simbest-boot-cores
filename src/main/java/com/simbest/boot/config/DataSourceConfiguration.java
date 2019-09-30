/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.config;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

/**
 * 用途：配置静态文件目录
 * 作者: lishuyi
 * 时间: 2018/4/17  23:28
 */
@Configuration
public class DataSourceConfiguration {

    @Bean
    @Primary
    @Qualifier("dataSource")
    @ConfigurationProperties(prefix = "spring.datasource.druid")
    public DataSource defaultDataSource() {
        return DruidDataSourceBuilder.create().build();
    }

    @Bean(name = "jdbcTemplate")
    public JdbcTemplate jdbcTemplate(@Qualifier("dataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "namedParameterJdbcTemplate")
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate( @Qualifier("dataSource") DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }

    /**
     * 建议：
     * 1、主体工程基于JPA，默认数据源为dataSource
     * 2、扩展多数据源使用JdbcTemplate，即每增加一个dataSource，对应增加一个JdbcTemplate
     * 3、application.properties和application-dev.properties增加如下配置，其中application.properties不要写死，在pom.xml进行占位设置和profile切换
     * custom.datasource.druid.sms82008201.name=sms82008201
     * custom.datasource.druid.sms82008201.driver-class-name=com.mysql.jdbc.Driver
     * custom.datasource.druid.sms82008201.url=jdbc:mysql://localhost/test?useUnicode=true&characterEncoding=UTF-8&characterSetResults=utf8&serverTimezone=GMT%2B8
     * custom.datasource.druid.sms82008201.port=3306
     * custom.datasource.druid.sms82008201.username=root
     * custom.datasource.druid.sms82008201.password=123456
     * @return
     */
//    @Bean
//    @Qualifier("sms82008201DataSource")
//    @ConfigurationProperties(prefix = "custom.datasource.druid.sms82008201")
//    public DataSource sms82008201Ds() {
//        return DruidDataSourceBuilder.create().build();
//    }
//
//    @Bean(name = "sms82008201JdbcTemplate")
//    public JdbcTemplate sms82008201JdbcTemplate(@Qualifier("sms82008201DataSource") DataSource dataSource) {
//        return new JdbcTemplate(dataSource);
//    }
}
