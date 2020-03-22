/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.sys.service.impl;

import com.simbest.boot.sys.service.IAppShutdownService;
import lombok.extern.slf4j.Slf4j;

import static com.simbest.boot.component.GracefulShutdown.SHUTDOWN_FLAG;

/**
 * 用途：完美关闭应用的钩子
 * 作者: lishuyi
 * 时间: 2020/3/21  22:36
 */
@Slf4j
public class GenericAppShutdownService implements IAppShutdownService {

    @Override
    public void gracefulShutdown() {
        log.debug("应用即将关闭，I will do nothing！ ".concat(SHUTDOWN_FLAG));
    }

}
