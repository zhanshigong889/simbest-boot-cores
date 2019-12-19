/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.sys.service;

import com.simbest.boot.sys.model.SysHealth;

/**
 * 用途：系统健康检查服务层
 * 作者: lishuyi
 * 时间: 2019/12/6  9:51
 */
public interface ISysHealthService {

    SysHealth databaseCheck();

    SysHealth redisCheck();

    SysHealth fileSystemCheck();

}
