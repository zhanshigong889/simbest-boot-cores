/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.sys.service;

import com.simbest.boot.sys.model.SysHealth;

/**
 * 用途：心跳测试服务
 * 作者: lishuyi
 * 时间: 2018/5/29  16:39
 */
public interface IHeartTestService {

    SysHealth doTest();
}
