/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.sys.service;

/**
 * 用途：完美关闭应用的钩子
 * 作者: lishuyi
 * 时间: 2020/3/21  22:34
 */
public interface IAppShutdownService {

    /**
     * 应用退出销毁前的关闭动作
     */
    void gracefulShutdown();

}
