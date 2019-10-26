package com.simbest.boot.util.redis;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.simbest.boot.config.AppConfig;
import com.simbest.boot.constants.ApplicationConstants;
import com.simbest.boot.util.json.JacksonUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.simbest.boot.component.distributed.lock.DistributedRedisLock.REDISSON_REDIS_LOCK;

/**
 * 用途：Redis客户端工具类
 * 作者: lishuyi
 * 时间: 2018/5/1  19:04
 */
@Slf4j
@Component
@DependsOn(value = {"redisTemplate"})
public class RedisUtil {

    @Autowired
    @Getter
    private RedisTemplate<String, String> redisTemplate;

    private static RedisUtil cacheUtils;

    private static String appcode;

    @Autowired
    private AppConfig config;

    private static String prefix;

    /**
     * @see RedisConfiguration#redisTemplate
     */
    @PostConstruct
    public void init() {
        cacheUtils = this;
        cacheUtils.redisTemplate = this.redisTemplate;
        cacheUtils.prefix = config.getRedisKeyPrefix();
        appcode = config.getAppcode();
    }

    public static RedisTemplate<String, String> getRedisTemplate() {
        return cacheUtils.redisTemplate;
    }

/** -------------------key相关操作--------------------- */

	/**
	 * 删除key
	 * 
	 * @param key
	 */
	public static Boolean delete(String key) {
        return cacheUtils.redisTemplate.delete(prefix+key);
	}

	/**
	 * 批量删除key
	 * 
	 * @param keys
	 */
	public static Long delete(Collection<String> keys) {
        Collection<String> realKeys = Sets.newHashSet();
        for(String k : keys){
            realKeys.add(prefix+k);
        }
        return cacheUtils.redisTemplate.delete(realKeys);
	}

    /**
     * 模糊删除key
     *
     * @param key
     */
    public static Long mulDelete(String pattern) {
        Set<String> keys = cacheUtils.redisTemplate.keys(prefix + pattern + ApplicationConstants.STAR);
        return cacheUtils.redisTemplate.delete(keys);
    }

	/**
	 * 序列化key
	 * 
	 * @param key
	 * @return
	 */
	public static byte[] dump(String key) {
		return cacheUtils.redisTemplate.dump(prefix+key);
	}

	/**
	 * 是否存在key
	 * 
	 * @param key
	 * @return
	 */
	public static Boolean hasKey(String key) {
		return cacheUtils.redisTemplate.hasKey(prefix+key);
	}

	/**
	 * 设置过期时间
	 * 
	 * @param key
	 * @param timeout
	 * @param unit
	 * @return
	 */
	public static Boolean expire(String key, long timeout, TimeUnit unit) {
		return cacheUtils.redisTemplate.expire(prefix+key, timeout, unit);
	}

    public static Boolean expireGlobal(String key, long timeout, TimeUnit unit) {
        return cacheUtils.redisTemplate.expire(key, timeout, unit);
    }

	/**
	 * 设置过期时间
	 *
	 * @param key
	 * @param date
	 * @return
	 */
	public static Boolean expireAt(String key, Date date) {
		return cacheUtils.redisTemplate.expireAt(prefix+key, date);
	}

    /**
     * 设置过期时间
     *
     * @param key
     * @param date
     * @return
     */
    public static Boolean expireGlobalAt(String key, Date date) {
        return cacheUtils.redisTemplate.expireAt(key, date);
    }

	/**
	 * 查找匹配的key
	 * 
	 * @param pattern
	 * @return
	 */
	public static Set<String> keys(String pattern) {
		return cacheUtils.redisTemplate.keys(prefix+pattern);
	}

    public static Set<String> globalKeys(String pattern) {
        return cacheUtils.redisTemplate.keys(pattern);
    }

	/**
	 * 将当前数据库的 key 移动到给定的数据库 db 当中
	 * 
	 * @param key
	 * @param dbIndex
	 * @return
	 */
	public static Boolean move(String key, int dbIndex) {
		return cacheUtils.redisTemplate.move(prefix+key, dbIndex);
	}

	/**
	 * 移除 key 的过期时间，key 将持久保持
	 * 
	 * @param key
	 * @return
	 */
	public static Boolean persist(String key) {
		return cacheUtils.redisTemplate.persist(prefix+key);
	}

	/**
	 * 返回 key 的剩余的过期时间
	 * 
	 * @param key
	 * @param unit
	 * @return
	 */
	public static Long getExpire(String key, TimeUnit unit) {
		return cacheUtils.redisTemplate.getExpire(prefix+key, unit);
	}

	/**
	 * 返回 key 的剩余的过期时间
	 * 
	 * @param key
	 * @return
	 */
	public static Long getExpire(String key) {
		return cacheUtils.redisTemplate.getExpire(prefix+key);
	}

	/**
	 * 从当前数据库中随机返回一个 key
	 * 
	 * @return
	 */
	public static String randomKey() {
		return cacheUtils.redisTemplate.randomKey();
	}

	/**
	 * 修改 key 的名称
	 * 
	 * @param oldKey
	 * @param newKey
	 */
	public static void rename(String oldKey, String newKey) {
		cacheUtils.redisTemplate.rename(prefix+oldKey, prefix+newKey);
	}

	/**
	 * 仅当 newkey 不存在时，将 oldKey 改名为 newkey
	 * 
	 * @param oldKey
	 * @param newKey
	 * @return
	 */
	public static Boolean renameIfAbsent(String oldKey, String newKey) {
		return cacheUtils.redisTemplate.renameIfAbsent(prefix+oldKey, prefix+newKey);
	}

	/**
	 * 返回 key 所储存的值的类型
	 * 
	 * @param key
	 * @return
	 */
	public static DataType type(String key) {
		return cacheUtils.redisTemplate.type(prefix+key);
	}

    public static DataType globalType(String key) {
        return cacheUtils.redisTemplate.type(key);
    }

	/** -------------------string相关操作--------------------- */

	/**
	 * 设置指定 key 的值
	 * @param key
	 * @param value
	 */
	public static void set(String key, String value) {
		cacheUtils.redisTemplate.opsForValue().set(prefix+key, value);
	}

    public static void setGlobal(String key, String value) {
        cacheUtils.redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置指定 key 的值
     * @param key
     * @param value
     * @param seconds
     */
    public static void set(String key, String value, int seconds) {
        cacheUtils.redisTemplate.opsForValue().set(prefix+key, value, seconds, TimeUnit.SECONDS);
    }

    public static void setGlobal(String key, String value, int seconds) {
        cacheUtils.redisTemplate.opsForValue().set(key, value, seconds, TimeUnit.SECONDS);
    }

	/**
	 * 获取指定 key 的值
	 * @param key
	 * @return
	 */
	public static String get(String key) {
		return cacheUtils.redisTemplate.opsForValue().get(prefix+key);
	}

    public static String getGlobal(String key) {
        return cacheUtils.redisTemplate.opsForValue().get(key);
    }

    /**
     * 设置指定 key 的对象
     * @param key
     * @param obj
     * @param <T>
     */
    public static <T> void setBean(String key, T obj) {
        cacheUtils.redisTemplate.opsForValue().set(prefix + key, JacksonUtils.obj2json(obj));
    }

    public static <T> void setBeanGlobal(String key, T obj) {
        cacheUtils.redisTemplate.opsForValue().set(key, JacksonUtils.obj2json(obj));
    }

    /**
     * 保存复杂类型数据到缓存（并设置失效时间）
     *
     * @param prefix + key
     * @param Object
     * @param seconds
     * @return
     */
    public static <T> void setBean(String key, T obj, int seconds) {
        cacheUtils.redisTemplate.opsForValue().set(prefix + key, JacksonUtils.obj2json(obj), seconds, TimeUnit.SECONDS);
    }

    public static <T> void setBeanGlobal(String key, T obj, int seconds) {
        cacheUtils.redisTemplate.opsForValue().set(key, JacksonUtils.obj2json(obj), seconds, TimeUnit.SECONDS);
    }


    /**
     * 取得复杂类型数据
     *
     * @param prefix + key
     * @param obj
     * @param clazz
     * @return
     */
    public static <T> T getBean(String key, Class<T> clazz) {
        String value = cacheUtils.redisTemplate.opsForValue().get(prefix + key);
        if (null == value) {
            return null;
        } else {
            return JacksonUtils.json2obj(value, clazz);
        }
    }

    public static <T> T getBeanGlobal(String key, Class<T> clazz) {
        String value = cacheUtils.redisTemplate.opsForValue().get(key);
        if (null == value) {
            return null;
        } else {
            return JacksonUtils.json2obj(value, clazz);
        }
    }

	/**
	 * 返回 key 中字符串值的子字符
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 */
	public static String getRange(String key, long start, long end) {
		return cacheUtils.redisTemplate.opsForValue().get(prefix+key, start, end);
	}

	/**
	 * 将给定 key 的值设为 value ，并返回 key 的旧值(old value)
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public static String getAndSet(String key, String value) {
		return cacheUtils.redisTemplate.opsForValue().getAndSet(prefix+key, value);
	}

	/**
	 * 对 key 所储存的字符串值，获取指定偏移量上的位(bit)
	 * 
	 * @param key
	 * @param offset
	 * @return
	 */
	public static Boolean getBit(String key, long offset) {
		return cacheUtils.redisTemplate.opsForValue().getBit(prefix+key, offset);
	}

	/**
	 * 批量获取
	 * 
	 * @param keys
	 * @return
	 */
	public static List<String> multiGet(Collection<String> keys) {
        Collection<String> realKeys = Sets.newHashSet();
        for(String k : keys){
            realKeys.add(prefix+k);
        }
		return cacheUtils.redisTemplate.opsForValue().multiGet(realKeys);
	}

	/**
	 * 设置ASCII码, 字符串'a'的ASCII码是97, 转为二进制是'01100001', 此方法是将二进制第offset位值变为value
	 * 
	 * @param key
	 * @param postion
	 *            位置
	 * @param value
	 *            值,true为1, false为0
	 * @return
	 */
	public static boolean setBit(String key, long offset, boolean value) {
		return cacheUtils.redisTemplate.opsForValue().setBit(prefix+key, offset, value);
	}

	/**
	 * 将值 value 关联到 key ，并将 key 的过期时间设为 timeout
	 * 
	 * @param key
	 * @param value
	 * @param timeout
	 *            过期时间
	 * @param unit
	 *            时间单位, 天:TimeUnit.DAYS 小时:TimeUnit.HOURS 分钟:TimeUnit.MINUTES
	 *            秒:TimeUnit.SECONDS 毫秒:TimeUnit.MILLISECONDS
	 */
	public static void setEx(String key, String value, long timeout, TimeUnit unit) {
		cacheUtils.redisTemplate.opsForValue().set(prefix+key, value, timeout, unit);
	}

	/**
	 * 只有在 key 不存在时设置 key 的值
	 * 
	 * @param key
	 * @param value
	 * @return 之前已经存在返回false,不存在返回true
	 */
	public static boolean setIfAbsent(String key, String value) {
		return cacheUtils.redisTemplate.opsForValue().setIfAbsent(prefix+key, value);
	}

	/**
	 * 用 value 参数覆写给定 key 所储存的字符串值，从偏移量 offset 开始
	 * 
	 * @param key
	 * @param value
	 * @param offset
	 *            从指定位置开始覆写
	 */
	public static void setRange(String key, String value, long offset) {
		cacheUtils.redisTemplate.opsForValue().set(prefix+key, value, offset);
	}

	/**
	 * 获取字符串的长度
	 * 
	 * @param key
	 * @return
	 */
	public static Long size(String key) {
		return cacheUtils.redisTemplate.opsForValue().size(prefix+key);
	}

	/**
	 * 批量添加
	 * 
	 * @param maps
	 */
	public static void multiSet(Map<String, String> maps) {
	    Map<String, String> realMaps = Maps.newHashMap();
        for(Map.Entry<String, String> entry: maps.entrySet()) {
            realMaps.put(prefix+entry.getKey(), entry.getValue());
        }
        cacheUtils.redisTemplate.opsForValue().multiSet(realMaps);
	}

	/**
	 * 同时设置一个或多个 key-value 对，当且仅当所有给定 key 都不存在
	 * 
	 * @param maps
	 * @return 之前已经存在返回false,不存在返回true
	 */
	public static boolean multiSetIfAbsent(Map<String, String> maps) {
        Map<String, String> realMaps = Maps.newHashMap();
        for(Map.Entry<String, String> entry: maps.entrySet()) {
            realMaps.put(prefix+entry.getKey(), entry.getValue());
        }
		return cacheUtils.redisTemplate.opsForValue().multiSetIfAbsent(realMaps);
	}

    /**
     * 增加(自增长)
     * @param key
     * @return
     */
    public static Long incrBy(String key) {
        return cacheUtils.cacheUtils.redisTemplate.execute((RedisCallback<Long>) connection ->
                connection.incr((prefix + key).getBytes()));
    }
    
	/**
	 * 增加(自增长)
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public static Long incrBy(String key, long increment) {
		return cacheUtils.redisTemplate.opsForValue().increment(prefix+key, increment);
	}

    /**
     * 减少(自减少)
     * @param key
     * @return
     */
    public static Long decrBy(String key) {
        return cacheUtils.cacheUtils.redisTemplate.execute((RedisCallback<Long>) connection ->
                connection.decr((prefix + key).getBytes()));
    }

    /**
     * 减少(自减少)
     *
     * @param key
     * @param value
     * @return
     */
    public static Long decrBy(String key, long increment) {
        return cacheUtils.redisTemplate.opsForValue().increment(prefix+key, increment*-1);
    }

	/**
	 * 追加到末尾
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public static Integer append(String key, String value) {
		return cacheUtils.redisTemplate.opsForValue().append(prefix+key, value);
	}

	/** -------------------hash相关操作------------------------- */

	/**
	 * 获取存储在哈希表中指定字段的值
	 * 
	 * @param key
	 * @param field
	 * @return
	 */
	public static Object hGet(String key, Object hkey) {
		return cacheUtils.redisTemplate.opsForHash().get(prefix+key, hkey);
	}

	/**
	 * 获取所有给定字段的值
	 * 
	 * @param key
	 * @return
	 */
	public static Map<Object, Object> hGetAll(String key) {
		return cacheUtils.redisTemplate.opsForHash().entries(prefix+key);
	}

	/**
	 * 获取所有给定字段的值
	 * 
	 * @param key
	 * @param fields
	 * @return
	 */
	public static List<Object> hMultiGet(String key, Collection<Object> hkeys) {
		return cacheUtils.redisTemplate.opsForHash().multiGet(prefix+key, hkeys);
	}

	public static void hPut(String key, Object hkey, Object value) {
		cacheUtils.redisTemplate.opsForHash().put(prefix+key, hkey, value);
	}

	public static void hPutAll(String key, Map<Object, Object> maps) {
		cacheUtils.redisTemplate.opsForHash().putAll(prefix+key, maps);
	}

	/**
	 * 仅当hashKey不存在时才设置
	 * 
	 * @param key
	 * @param hashKey
	 * @param value
	 * @return
	 */
	public static Boolean hPutIfAbsent(String key, Object hkey, Object value) {
		return cacheUtils.redisTemplate.opsForHash().putIfAbsent(prefix+key, hkey, value);
	}

	/**
	 * 删除一个或多个哈希表字段
	 * 
	 * @param key
	 * @param fields
	 * @return
	 */
	public static Long hDelete(String key, Object... hkeys) {
		return cacheUtils.redisTemplate.opsForHash().delete(prefix+key, hkeys);
	}

	/**
	 * 查看哈希表 key 中，指定的字段是否存在
	 * 
	 * @param key
	 * @param field
	 * @return
	 */
	public static boolean hExists(String key, Object hkey) {
		return cacheUtils.redisTemplate.opsForHash().hasKey(prefix+key, hkey);
	}

	/**
	 * 为哈希表 key 中的指定字段的整数值加上增量 increment
	 * 
	 * @param key
	 * @param field
	 * @param increment
	 * @return
	 */
	public static Long hIncrBy(String key, Object hkey, long increment) {
		return cacheUtils.redisTemplate.opsForHash().increment(prefix+key, hkey, increment);
	}

	/**
	 * 为哈希表 key 中的指定字段的整数值加上增量 increment
	 * 
	 * @param key
	 * @param field
	 * @param delta
	 * @return
	 */
	public static Double hIncrByFloat(String key, Object field, double delta) {
		return cacheUtils.redisTemplate.opsForHash().increment(prefix+key, field, delta);
	}

	/**
	 * 获取所有哈希表中的字段
	 * 
	 * @param key
	 * @return
	 */
	public static Set<Object> hKeys(String key) {
		return cacheUtils.redisTemplate.opsForHash().keys(prefix+key);
	}

	/**
	 * 获取哈希表中字段的数量
	 * 
	 * @param key
	 * @return
	 */
	public static Long hSize(String key) {
		return cacheUtils.redisTemplate.opsForHash().size(prefix+key);
	}

	/**
	 * 获取哈希表中所有值
	 * 
	 * @param key
	 * @return
	 */
	public static List<Object> hValues(String key) {
		return cacheUtils.redisTemplate.opsForHash().values(prefix+key);
	}

	/**
	 * 迭代哈希表中的键值对
	 * 
	 * @param key
	 * @param options
	 * @return
	 */
	public static Cursor<Entry<Object, Object>> hScan(String key, ScanOptions options) {
		return cacheUtils.redisTemplate.opsForHash().scan(prefix+key, options);
	}

	/** ------------------------list相关操作---------------------------- */

	/**
	 * 通过索引获取列表中的元素
	 * 
	 * @param key
	 * @param index
	 * @return
	 */
	public static String lIndex(String key, long index) {
		return cacheUtils.redisTemplate.opsForList().index(prefix+key, index);
	}

	/**
	 * 获取列表指定范围内的元素
	 * 
	 * @param key
	 * @param start
	 *            开始位置, 0是开始位置
	 * @param end
	 *            结束位置, -1返回所有
	 * @return
	 */
	public static List<String> lRange(String key, long start, long end) {
		return cacheUtils.redisTemplate.opsForList().range(prefix+key, start, end);
	}

	/**
	 * 存储在list头部
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public static Long lLeftPush(String key, String value) {
		return cacheUtils.redisTemplate.opsForList().leftPush(prefix+key, value);
	}

	/**
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public static Long lLeftPushAll(String key, String... value) {
		return cacheUtils.redisTemplate.opsForList().leftPushAll(prefix+key, value);
	}

	/**
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public static Long lLeftPushAll(String key, Collection<String> value) {
		return cacheUtils.redisTemplate.opsForList().leftPushAll(prefix+key, value);
	}

	/**
	 * 当list存在的时候才加入
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public static Long lLeftPushIfPresent(String key, String value) {
		return cacheUtils.redisTemplate.opsForList().leftPushIfPresent(prefix+key, value);
	}

	/**
	 * 如果pivot存在,再pivot前面添加
	 * 
	 * @param key
	 * @param pivot
	 * @param value
	 * @return
	 */
	public static Long lLeftPush(String key, String pivot, String value) {
		return cacheUtils.redisTemplate.opsForList().leftPush(prefix+key, pivot, value);
	}

	/**
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public static Long lRightPush(String key, String value) {
		return cacheUtils.redisTemplate.opsForList().rightPush(prefix+key, value);
	}

	/**
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public static Long lRightPushAll(String key, String... value) {
		return cacheUtils.redisTemplate.opsForList().rightPushAll(prefix+key, value);
	}

	/**
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public static Long lRightPushAll(String key, Collection<String> value) {
		return cacheUtils.redisTemplate.opsForList().rightPushAll(prefix+key, value);
	}

	/**
	 * 为已存在的列表添加值
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public static Long lRightPushIfPresent(String key, String value) {
		return cacheUtils.redisTemplate.opsForList().rightPushIfPresent(prefix+key, value);
	}

	/**
	 * 在pivot元素的右边添加值
	 * 
	 * @param key
	 * @param pivot
	 * @param value
	 * @return
	 */
	public static Long lRightPush(String key, String pivot, String value) {
		return cacheUtils.redisTemplate.opsForList().rightPush(prefix+key, pivot, value);
	}

	/**
	 * 通过索引设置列表元素的值
	 * 
	 * @param key
	 * @param index
	 *            位置
	 * @param value
	 */
	public static void lSet(String key, long index, String value) {
		cacheUtils.redisTemplate.opsForList().set(prefix+key, index, value);
	}

	/**
	 * 移出并获取列表的第一个元素
	 * 
	 * @param key
	 * @return 删除的元素
	 */
	public static String lLeftPop(String key) {
		return cacheUtils.redisTemplate.opsForList().leftPop(prefix+key);
	}

	/**
	 * 移出并获取列表的第一个元素， 如果列表没有元素会阻塞列表直到等待超时或发现可弹出元素为止
	 * 
	 * @param key
	 * @param timeout
	 *            等待时间
	 * @param unit
	 *            时间单位
	 * @return
	 */
	public static String lBLeftPop(String key, long timeout, TimeUnit unit) {
		return cacheUtils.redisTemplate.opsForList().leftPop(prefix+key, timeout, unit);
	}

	/**
	 * 移除并获取列表最后一个元素
	 * 
	 * @param key
	 * @return 删除的元素
	 */
	public static String lRightPop(String key) {
		return cacheUtils.redisTemplate.opsForList().rightPop(prefix+key);
	}

	/**
	 * 移出并获取列表的最后一个元素， 如果列表没有元素会阻塞列表直到等待超时或发现可弹出元素为止
	 * 
	 * @param key
	 * @param timeout
	 *            等待时间
	 * @param unit
	 *            时间单位
	 * @return
	 */
	public static String lBRightPop(String key, long timeout, TimeUnit unit) {
		return cacheUtils.redisTemplate.opsForList().rightPop(prefix+key, timeout, unit);
	}

	/**
	 * 移除列表的最后一个元素，并将该元素添加到另一个列表并返回
	 * 
	 * @param sourceKey
	 * @param destinationKey
	 * @return
	 */
	public static String lRightPopAndLeftPush(String sourceKey, String destinationKey) {
		return cacheUtils.redisTemplate.opsForList().rightPopAndLeftPush(prefix+sourceKey,
                prefix+destinationKey);
	}

	/**
	 * 从列表中弹出一个值，将弹出的元素插入到另外一个列表中并返回它； 如果列表没有元素会阻塞列表直到等待超时或发现可弹出元素为止
	 * 
	 * @param sourceKey
	 * @param destinationKey
	 * @param timeout
	 * @param unit
	 * @return
	 */
	public static String lBRightPopAndLeftPush(String sourceKey, String destinationKey,
			long timeout, TimeUnit unit) {
		return cacheUtils.redisTemplate.opsForList().rightPopAndLeftPush(prefix+sourceKey,
                prefix+destinationKey, timeout, unit);
	}

	/**
	 * 删除集合中值等于value得元素
	 * 
	 * @param key
	 * @param index
	 *            index=0, 删除所有值等于value的元素; index>0, 从头部开始删除第一个值等于value的元素;
	 *            index<0, 从尾部开始删除第一个值等于value的元素;
	 * @param value
	 * @return
	 */
	public static Long lRemove(String key, long index, String value) {
		return cacheUtils.redisTemplate.opsForList().remove(prefix+key, index, value);
	}

	/**
	 * 裁剪list
	 * 
	 * @param key
	 * @param start
	 * @param end
	 */
	public static void lTrim(String key, long start, long end) {
		cacheUtils.redisTemplate.opsForList().trim(prefix+key, start, end);
	}

	/**
	 * 获取列表长度
	 * 
	 * @param key
	 * @return
	 */
	public static Long lLen(String key) {
		return cacheUtils.redisTemplate.opsForList().size(prefix+key);
	}

	/** --------------------set相关操作-------------------------- */

	/**
	 * set添加元素
	 * 
	 * @param key
	 * @param values
	 * @return
	 */
	public static Long sAdd(String key, String... values) {
		return cacheUtils.redisTemplate.opsForSet().add(prefix+key, values);
	}

	/**
	 * set移除元素
	 * 
	 * @param key
	 * @param values
	 * @return
	 */
	public static Long sRemove(String key, Object... values) {
		return cacheUtils.redisTemplate.opsForSet().remove(prefix+key, values);
	}

	/**
	 * 移除并返回集合的一个随机元素
	 * 
	 * @param key
	 * @return
	 */
	public static String sPop(String key) {
		return cacheUtils.redisTemplate.opsForSet().pop(prefix+key);
	}

	/**
	 * 将元素value从一个集合移到另一个集合
	 * 
	 * @param key
	 * @param value
	 * @param destKey
	 * @return
	 */
	public static Boolean sMove(String key, String value, String destKey) {
		return cacheUtils.redisTemplate.opsForSet().move(prefix+key, value, prefix+destKey);
	}

	/**
	 * 获取集合的大小
	 * 
	 * @param key
	 * @return
	 */
	public static Long sSize(String key) {
		return cacheUtils.redisTemplate.opsForSet().size(prefix+key);
	}

	/**
	 * 判断集合是否包含value
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public static Boolean sIsMember(String key, Object value) {
		return cacheUtils.redisTemplate.opsForSet().isMember(prefix+key, value);
	}

	/**
	 * 获取两个集合的交集
	 * 
	 * @param key
	 * @param otherKey
	 * @return
	 */
	public static Set<String> sIntersect(String key, String otherKey) {
		return cacheUtils.redisTemplate.opsForSet().intersect(prefix+key, prefix+otherKey);
	}

	/**
	 * 获取key集合与多个集合的交集
	 * 
	 * @param key
	 * @param otherKeys
	 * @return
	 */
	public static Set<String> sIntersect(String key, Collection<String> otherKeys) {
        Collection<String> realKeys = Sets.newHashSet();
        for(String k : otherKeys){
            realKeys.add(prefix+k);
        }
		return cacheUtils.redisTemplate.opsForSet().intersect(prefix+key, realKeys);
	}

	/**
	 * key集合与otherKey集合的交集存储到destKey集合中
	 * 
	 * @param key
	 * @param otherKey
	 * @param destKey
	 * @return
	 */
	public static Long sIntersectAndStore(String key, String otherKey, String destKey) {
		return cacheUtils.redisTemplate.opsForSet().intersectAndStore(prefix+key, prefix+otherKey,
                prefix+destKey);
	}

	/**
	 * key集合与多个集合的交集存储到destKey集合中
	 * 
	 * @param key
	 * @param otherKeys
	 * @param destKey
	 * @return
	 */
	public static Long sIntersectAndStore(String key, Collection<String> otherKeys,
			String destKey) {
        Collection<String> realKeys = Sets.newHashSet();
        for(String k : otherKeys){
            realKeys.add(prefix+k);
        }
		return cacheUtils.redisTemplate.opsForSet().intersectAndStore(prefix+key, realKeys,
                prefix+destKey);
	}

	/**
	 * 获取两个集合的并集
	 * 
	 * @param key
	 * @param otherKeys
	 * @return
	 */
	public static Set<String> sUnion(String key, String otherKey) {
		return cacheUtils.redisTemplate.opsForSet().union(prefix+key, prefix+otherKey);
	}

	/**
	 * 获取key集合与多个集合的并集
	 * 
	 * @param key
	 * @param otherKeys
	 * @return
	 */
	public static Set<String> sUnion(String key, Collection<String> otherKeys) {
        Collection<String> realKeys = Sets.newHashSet();
        for(String k : otherKeys){
            realKeys.add(prefix+k);
        }
		return cacheUtils.redisTemplate.opsForSet().union(prefix+key, realKeys);
	}

	/**
	 * key集合与otherKey集合的并集存储到destKey中
	 * 
	 * @param key
	 * @param otherKey
	 * @param destKey
	 * @return
	 */
	public static Long sUnionAndStore(String key, String otherKey, String destKey) {
		return cacheUtils.redisTemplate.opsForSet().unionAndStore(prefix+key, prefix+otherKey, prefix+destKey);
	}

	/**
	 * key集合与多个集合的并集存储到destKey中
	 * 
	 * @param key
	 * @param otherKeys
	 * @param destKey
	 * @return
	 */
	public static Long sUnionAndStore(String key, Collection<String> otherKeys,
			String destKey) {
        Collection<String> realKeys = Sets.newHashSet();
        for(String k : otherKeys){
            realKeys.add(prefix+k);
        }
		return cacheUtils.redisTemplate.opsForSet().unionAndStore(prefix+key, realKeys, prefix+destKey);
	}

	/**
	 * 获取两个集合的差集
	 * 
	 * @param key
	 * @param otherKey
	 * @return
	 */
	public static Set<String> sDifference(String key, String otherKey) {
		return cacheUtils.redisTemplate.opsForSet().difference(prefix+key, prefix+otherKey);
	}

	/**
	 * 获取key集合与多个集合的差集
	 * 
	 * @param key
	 * @param otherKeys
	 * @return
	 */
	public static Set<String> sDifference(String key, Collection<String> otherKeys) {
        Collection<String> realKeys = Sets.newHashSet();
        for(String k : otherKeys){
            realKeys.add(prefix+k);
        }
		return cacheUtils.redisTemplate.opsForSet().difference(prefix+key, realKeys);
	}

	/**
	 * key集合与otherKey集合的差集存储到destKey中
	 * 
	 * @param key
	 * @param otherKey
	 * @param destKey
	 * @return
	 */
	public static Long sDifference(String key, String otherKey, String destKey) {
		return cacheUtils.redisTemplate.opsForSet().differenceAndStore(prefix+key, prefix+otherKey,
                prefix+destKey);
	}

	/**
	 * key集合与多个集合的差集存储到destKey中
	 * 
	 * @param key
	 * @param otherKeys
	 * @param destKey
	 * @return
	 */
	public static Long sDifference(String key, Collection<String> otherKeys,
			String destKey) {
        Collection<String> realKeys = Sets.newHashSet();
        for(String k : otherKeys){
            realKeys.add(prefix+k);
        }
		return cacheUtils.redisTemplate.opsForSet().differenceAndStore(prefix+key, realKeys,
                prefix+destKey);
	}

	/**
	 * 获取集合所有元素
	 * 
	 * @param key
	 * @param otherKeys
	 * @param destKey
	 * @return
	 */
	public static Set<String> setMembers(String key) {
		return cacheUtils.redisTemplate.opsForSet().members(prefix+key);
	}

	/**
	 * 随机获取集合中的一个元素
	 * 
	 * @param key
	 * @return
	 */
	public static String sRandomMember(String key) {
		return cacheUtils.redisTemplate.opsForSet().randomMember(prefix+key);
	}

	/**
	 * 随机获取集合中count个元素
	 * 
	 * @param key
	 * @param count
	 * @return
	 */
	public static List<String> sRandomMembers(String key, long count) {
		return cacheUtils.redisTemplate.opsForSet().randomMembers(prefix+key, count);
	}

	/**
	 * 随机获取集合中count个元素并且去除重复的
	 * 
	 * @param key
	 * @param count
	 * @return
	 */
	public static Set<String> sDistinctRandomMembers(String key, long count) {
		return cacheUtils.redisTemplate.opsForSet().distinctRandomMembers(prefix+key, count);
	}

	/**
	 * 
	 * @param key
	 * @param options
	 * @return
	 */
	public static Cursor<String> sScan(String key, ScanOptions options) {
		return cacheUtils.redisTemplate.opsForSet().scan(prefix+key, options);
	}

	/**------------------zSet相关操作--------------------------------*/
	
	/**
	 * 添加元素,有序集合是按照元素的score值由小到大排列
	 * 
	 * @param key
	 * @param value
	 * @param score
	 * @return
	 */
	public static Boolean zAdd(String key, String value, double score) {
		return cacheUtils.redisTemplate.opsForZSet().add(prefix+key, value, score);
	}

	/**
	 * 
	 * @param key
	 * @param values
	 * @return
	 */
	public static Long zAdd(String key, Set<TypedTuple<String>> values) {
		return cacheUtils.redisTemplate.opsForZSet().add(prefix+key, values);
	}

	/**
	 * 
	 * @param key
	 * @param values
	 * @return
	 */
	public static Long zRemove(String key, Object... values) {
		return cacheUtils.redisTemplate.opsForZSet().remove(prefix+key, values);
	}

	/**
	 * 增加元素的score值，并返回增加后的值
	 * 
	 * @param key
	 * @param value
	 * @param delta
	 * @return
	 */
	public static Double zIncrementScore(String key, String value, double delta) {
		return cacheUtils.redisTemplate.opsForZSet().incrementScore(prefix+key, value, delta);
	}

	/**
	 * 返回元素在集合的排名,有序集合是按照元素的score值由小到大排列
	 * 
	 * @param key
	 * @param value
	 * @return 0表示第一位
	 */
	public static Long zRank(String key, Object value) {
		return cacheUtils.redisTemplate.opsForZSet().rank(prefix+key, value);
	}

	/**
	 * 返回元素在集合的排名,按元素的score值由大到小排列
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public static Long zReverseRank(String key, Object value) {
		return cacheUtils.redisTemplate.opsForZSet().reverseRank(prefix+key, value);
	}

	/**
	 * 获取集合的元素, 从小到大排序
	 * 
	 * @param key
	 * @param start
	 *            开始位置
	 * @param end
	 *            结束位置, -1查询所有
	 * @return
	 */
	public static Set<String> zRange(String key, long start, long end) {
		return cacheUtils.redisTemplate.opsForZSet().range(prefix+key, start, end);
	}

	/**
	 * 获取集合元素, 并且把score值也获取
	 * 
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 */
	public static Set<TypedTuple<String>> zRangeWithScores(String key, long start,
			long end) {
		return cacheUtils.redisTemplate.opsForZSet().rangeWithScores(prefix+key, start, end);
	}

	/**
	 * 根据Score值查询集合元素
	 * 
	 * @param key
	 * @param min
	 *            最小值
	 * @param max
	 *            最大值
	 * @return
	 */
	public static Set<String> zRangeByScore(String key, double min, double max) {
		return cacheUtils.redisTemplate.opsForZSet().rangeByScore(prefix+key, min, max);
	}

	/**
	 * 根据Score值查询集合元素, 从小到大排序
	 * 
	 * @param key
	 * @param min
	 *            最小值
	 * @param max
	 *            最大值
	 * @return
	 */
	public static Set<TypedTuple<String>> zRangeByScoreWithScores(String key,
			double min, double max) {
		return cacheUtils.redisTemplate.opsForZSet().rangeByScoreWithScores(prefix+key, min, max);
	}

	/**
	 * 
	 * @param key
	 * @param min
	 * @param max
	 * @param start
	 * @param end
	 * @return
	 */
	public static Set<TypedTuple<String>> zRangeByScoreWithScores(String key,
			double min, double max, long start, long end) {
		return cacheUtils.redisTemplate.opsForZSet().rangeByScoreWithScores(prefix+key, min, max,
				start, end);
	}

	/**
	 * 获取集合的元素, 从大到小排序
	 * 
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 */
	public static Set<String> zReverseRange(String key, long start, long end) {
		return cacheUtils.redisTemplate.opsForZSet().reverseRange(prefix+key, start, end);
	}

	/**
	 * 获取集合的元素, 从大到小排序, 并返回score值
	 * 
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 */
	public static Set<TypedTuple<String>> zReverseRangeWithScores(String key,
			long start, long end) {
		return cacheUtils.redisTemplate.opsForZSet().reverseRangeWithScores(prefix+key, start,
				end);
	}

	/**
	 * 根据Score值查询集合元素, 从大到小排序
	 * 
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 */
	public static Set<String> zReverseRangeByScore(String key, double min,
			double max) {
		return cacheUtils.redisTemplate.opsForZSet().reverseRangeByScore(prefix+key, min, max);
	}

	/**
	 * 根据Score值查询集合元素, 从大到小排序
	 * 
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 */
	public static Set<TypedTuple<String>> zReverseRangeByScoreWithScores(
			String key, double min, double max) {
		return cacheUtils.redisTemplate.opsForZSet().reverseRangeByScoreWithScores(prefix+key,
				min, max);
	}

	/**
	 * 
	 * @param key
	 * @param min
	 * @param max
	 * @param start
	 * @param end
	 * @return
	 */
	public static Set<String> zReverseRangeByScore(String key, double min,
			double max, long start, long end) {
		return cacheUtils.redisTemplate.opsForZSet().reverseRangeByScore(prefix+key, min, max,
				start, end);
	}

	/**
	 * 根据score值获取集合元素数量
	 * 
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 */
	public static Long zCount(String key, double min, double max) {
		return cacheUtils.redisTemplate.opsForZSet().count(prefix+key, min, max);
	}

	/**
	 * 获取集合大小
	 * 
	 * @param key
	 * @return
	 */
	public static Long zSize(String key) {
		return cacheUtils.redisTemplate.opsForZSet().size(prefix+key);
	}

	/**
	 * 获取集合大小
	 * 
	 * @param key
	 * @return
	 */
	public static Long zZCard(String key) {
		return cacheUtils.redisTemplate.opsForZSet().zCard(prefix+key);
	}

	/**
	 * 获取集合中value元素的score值
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public static Double zScore(String key, Object value) {
		return cacheUtils.redisTemplate.opsForZSet().score(prefix+key, value);
	}

	/**
	 * 移除指定索引位置的成员
	 * 
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 */
	public static Long zRemoveRange(String key, long start, long end) {
		return cacheUtils.redisTemplate.opsForZSet().removeRange(prefix+key, start, end);
	}

	/**
	 * 根据指定的score值的范围来移除成员
	 * 
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 */
	public static Long zRemoveRangeByScore(String key, double min, double max) {
		return cacheUtils.redisTemplate.opsForZSet().removeRangeByScore(prefix+key, min, max);
	}

	/**
	 * 获取key和otherKey的并集并存储在destKey中
	 * 
	 * @param key
	 * @param otherKey
	 * @param destKey
	 * @return
	 */
	public static Long zUnionAndStore(String key, String otherKey, String destKey) {
		return cacheUtils.redisTemplate.opsForZSet().unionAndStore(prefix+key, otherKey, destKey);
	}

	/**
	 * 
	 * @param key
	 * @param otherKeys
	 * @param destKey
	 * @return
	 */
	public static Long zUnionAndStore(String key, Collection<String> otherKeys,
			String destKey) {
        Collection<String> realKeys = Sets.newHashSet();
        for(String k : otherKeys){
            realKeys.add(prefix+k);
        }
		return cacheUtils.redisTemplate.opsForZSet()
				.unionAndStore(prefix+key, realKeys, prefix+destKey);
	}

	/**
	 * 交集
	 * 
	 * @param key
	 * @param otherKey
	 * @param destKey
	 * @return
	 */
	public static Long zIntersectAndStore(String key, String otherKey,
			String destKey) {
		return cacheUtils.redisTemplate.opsForZSet().intersectAndStore(prefix+key, prefix+otherKey,
                prefix+destKey);
	}

	/**
	 * 交集
	 * 
	 * @param key
	 * @param otherKeys
	 * @param destKey
	 * @return
	 */
	public static Long zIntersectAndStore(String key, Collection<String> otherKeys,
			String destKey) {
        Collection<String> realKeys = Sets.newHashSet();
        for(String k : otherKeys){
            realKeys.add(prefix+k);
        }
		return cacheUtils.redisTemplate.opsForZSet().intersectAndStore(prefix+key, realKeys,
                prefix+destKey);
	}

	/**
	 * 
	 * @param key
	 * @param options
	 * @return
	 */
	public static Cursor<TypedTuple<String>> zScan(String key, ScanOptions options) {
		return cacheUtils.redisTemplate.opsForZSet().scan(prefix+key, options);
	}

    /**
     * 清理如下缓存:
     * redisson_lock_timeout:{cache:key:nzl:master_lock}
     * redisson_lock_queue:{cache:key:nzl:master_lock}
     * redisson_lock_key_prefix_cache:key:nzl:TaskName
     * @return
     */
	public static Long cleanRedisLock(){
        String searchLockKey = REDISSON_REDIS_LOCK + ApplicationConstants.STAR + appcode + ApplicationConstants.STAR;
        log.debug("按照关键字查询当前应用的分布式锁【{}】", searchLockKey);
        Set<String> lockKeys = RedisUtil.getRedisTemplate().keys(searchLockKey);
        Long ret1 = RedisUtil.getRedisTemplate().delete(lockKeys);
        log.debug("计划删除锁键共计【{}】个，锁键分别是【{}】，共计成功【{}】个", lockKeys.size(), StringUtils.joinWith(ApplicationConstants.COMMA, lockKeys), ret1);
        return ret1;
    }

}