/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.component.distributed.lock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.redisson.api.RLock;

/**
 * 用途：Redisson分布式锁和同步器
 * 参考：https://github.com/redisson/redisson/wiki/8.-%E5%88%86%E5%B8%83%E5%BC%8F%E9%94%81%E5%92%8C%E5%90%8C%E6%AD%A5%E5%99%A8
 * 作者: lishuyi
 * 时间: 2018/6/22  17:40
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistributedRedissonLock {

    private RLock rLock;

    private boolean isLocked;

}
