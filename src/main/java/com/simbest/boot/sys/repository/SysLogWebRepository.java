/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.sys.repository;

import com.simbest.boot.base.repository.SystemRepository;
import com.simbest.boot.sys.model.SysLogWeb;
import org.springframework.stereotype.Repository;

/**
 * 用途：系统Web请求日志
 * 作者: lishuyi
 * 时间: 2018/6/10  13:42
 */
@Repository
public interface SysLogWebRepository extends SystemRepository<SysLogWeb, String> {

}

