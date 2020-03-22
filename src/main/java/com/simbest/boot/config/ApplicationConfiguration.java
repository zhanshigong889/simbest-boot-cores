/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.config;

import com.simbest.boot.sys.service.IAppShutdownService;
import com.simbest.boot.sys.service.ISimpleSmsService;
import com.simbest.boot.sys.service.impl.CloopenSmsService;
import com.simbest.boot.sys.service.impl.GenericAppShutdownService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 用途：应用通用配置
 * 作者: lishuyi
 * 时间: 2019/8/10  10:18
 */
@Slf4j
@Configuration
public class ApplicationConfiguration {

    @Autowired
    private AppConfig config;

    /**
     * 配置缺省的简单短信接口
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(value = ISimpleSmsService.class)
    public ISimpleSmsService cloopenSmsService() {
        return new CloopenSmsService(config.getSmsAccount(), config.getSmsToken(), config.getSmsAppId(), config.getSmsTemplateId());
    }

    /**
     * 配置缺省的简单应用关闭钩子
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(value = IAppShutdownService.class)
    public IAppShutdownService genericAppShutdownService() {
        return new GenericAppShutdownService();
    }

}
