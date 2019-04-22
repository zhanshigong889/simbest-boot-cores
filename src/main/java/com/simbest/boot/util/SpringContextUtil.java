/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;

/**
 * 用途：Spring Context 工具类
 * 作者: lishuyi
 * 时间: 2019/4/22  17:49
 */
@Component
public class SpringContextUtil {

    @Autowired
    private ApplicationContext context;

    // 国际化使用
    public String getMessage(String key) {
        return context.getMessage(key, null, Locale.getDefault());
    }

    /// 获取当前环境
    public String getActiveProfile() {
        return context.getEnvironment().getActiveProfiles()[0];
    }

    // 获取Bean
    public <T> T getBean(String beanName, @Nullable Class<T> requiredType) {
        return  context.getBean(beanName, requiredType);
    }

    public <T> T getBean(Class<T> requiredType) {
        return context.getBean(requiredType);
    }

    public <T> Map<String, T> getBeansOfType(@Nullable Class<T> type) {
        return context.getBeansOfType(type);
    }


}
