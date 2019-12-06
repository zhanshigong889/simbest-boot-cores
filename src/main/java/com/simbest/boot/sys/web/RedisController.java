/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.sys.web;

import com.google.common.collect.Maps;
import com.simbest.boot.base.web.response.JsonResponse;
import com.simbest.boot.constants.ApplicationConstants;
import com.simbest.boot.util.redis.RedisUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

/**
 * 用途：定时任务日志记录控制器
 * 作者: lishuyi
 * 时间: 2018/2/22  10:14
 */
@Api(description = "RedisController", tags = {"系统管理-Redis缓存管理"})
@RestController
@RequestMapping("/sys/redis")
public class RedisController {

    @ApiOperation(value = "模糊查找Key键")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER','ROLE_ADMIN')")
    @PostMapping("/findKeys")
    public JsonResponse findKeys(String pattern) {
        Set<String> keys = RedisUtil.globalKeys(ApplicationConstants.STAR+pattern+ApplicationConstants.STAR);
        return JsonResponse.success(keys);
    }

    @ApiOperation(value = "精确通过Key键删除缓存Value值", notes = "注意RedisUtil会为应用增加编码前缀")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER','ROLE_ADMIN')")
    @PostMapping("/delCache")
    public JsonResponse delCache(String key) {
        Long number = RedisUtil.delete(key) == true ? 1L : 0L;
        Map<String, Long> delCache = Maps.newHashMap();
        delCache.put("caches", number);
        return JsonResponse.success(delCache);
    }

    @ApiOperation(value = "全局精确通过Key键删除缓存Value值")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER','ROLE_ADMIN')")
    @PostMapping("/delCacheGlobal")
    public JsonResponse delCacheGlobal(String key) {
        Long number = RedisUtil.deleteGlobal(key) == true ? 1L : 0L;
        Map<String, Long> delCache = Maps.newHashMap();
        delCache.put("caches", number);
        return JsonResponse.success(delCache);
    }

    @ApiOperation(value = "通过Key键删除缓存Value值", notes = "注意是模糊删除")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER','ROLE_ADMIN')")
    @PostMapping("/mulDeleteCache")
    public JsonResponse mulDeleteCache(String pattern) {
        Long number = RedisUtil.mulDelete(pattern);
        Map<String, Long> delCache = Maps.newHashMap();
        delCache.put("caches", number);
        return JsonResponse.success(delCache);
    }

    @ApiOperation(value = "通过Key键删除缓存Value值", notes = "注意是模糊删除")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER','ROLE_ADMIN')")
    @PostMapping("/mulDeleteCacheGlobal")
    public JsonResponse mulDeleteCacheGlobal(String pattern) {
        Long number = RedisUtil.mulDeleteGlobal(pattern);
        Map<String, Long> delCache = Maps.newHashMap();
        delCache.put("caches", number);
        return JsonResponse.success(delCache);
    }

}
