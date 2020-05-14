/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.util.distribution.id.repository;

import com.simbest.boot.base.repository.GenericRepository;
import com.simbest.boot.util.distribution.id.model.SysRedisIdKey;
import org.springframework.stereotype.Repository;

/**
 * 用途：Redis序列号前缀持久层
 * 作者: lishuyi
 * 时间: 2020/5/14  15:57
 */
@Repository
public interface SysRedisIdKeyRepository extends GenericRepository<SysRedisIdKey, String> {

    SysRedisIdKey findByName(String name);
}
