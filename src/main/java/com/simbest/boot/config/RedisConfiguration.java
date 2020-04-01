/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Maps;
import com.mzlion.easyokhttp.HttpClient;
import com.simbest.boot.base.enums.StoreLocation;
import com.simbest.boot.base.exception.Exceptions;
import com.simbest.boot.base.web.response.JsonResponse;
import com.simbest.boot.component.distributed.lock.DistributedLockFactoryBean;
import com.simbest.boot.constants.ApplicationConstants;
import com.simbest.boot.sys.model.SysDictValue;
import com.simbest.boot.util.AppFileSftpUtil;
import com.simbest.boot.util.encrypt.Des3Encryptor;
import com.simbest.boot.util.encrypt.RsaEncryptor;
import com.simbest.boot.util.json.JacksonUtils;
import com.simbest.boot.util.redis.RedisUtil;
import com.simbest.boot.uums.api.ApiRequestHandle;
import com.simbest.boot.uums.api.sys.UumsSysDictValueApi;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.MapPropertySource;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.simbest.boot.constants.ApplicationConstants.ADMINISTRATOR;
import static com.simbest.boot.constants.ApplicationConstants.ONE;
import static com.simbest.boot.constants.ApplicationConstants.UUMS_APPCODE;
import static com.simbest.boot.constants.ApplicationConstants.ZERO;

/**
 * 用途：Redis 和 RedissonClient 配置信息
 * 作者: lishuyi
 * 时间: 2018/5/1  18:56
 */
@Slf4j
@Configuration
@EnableCaching
@EnableRedisHttpSession
public class RedisConfiguration extends CachingConfigurerSupport {

    public final static String DICT_VALUE_REDIS = "redis";

    public enum RedisConfigType {
        propertiesRedis,  ftpRedis, sftpRedis, dictValueRedis
    }

    @Autowired
    private AppConfig config;

    @Autowired
    private RedisKeyGenerator redisKeyGenerator;

    @Autowired
    private RedisIndexedSessionRepository sessionRepository;

    private Des3Encryptor encryptor = new Des3Encryptor();

    private ApiRequestHandle<SysDictValue> sysDictValueApiHandle = new ApiRequestHandle();

    private ApiRequestHandle<List<SysDictValue>> sysDictValueApiListHandle = new ApiRequestHandle();

    @Getter
    private RedissonClient redissonClient;

    private String redisClusterNodes;

    @PostConstruct
    private void afterPropertiesSet() {
        log.info("应用超时时间设置为【{}】秒", config.getRedisMaxInactiveIntervalInSeconds());
        sessionRepository.setDefaultMaxInactiveInterval(config.getRedisMaxInactiveIntervalInSeconds());
        // 注释以下代码，配合RedisSessionConfiguration的CookiePath=/可以实现同域名应用间Cookie共享Session
        // 而目前设计必须设置，否则导致不同应用相同Cookie在检查Session到期时间时报错
        // Failed to deserialize object type; nested exception is java.lang.ClassNotFoundException: com.simbest.boot.uums.role.model.SysRole
        log.info("应用Redis缓存命名空间前缀为【{}】 ", config.getRedisNamespace());
        sessionRepository.setRedisKeyNamespace(config.getRedisNamespace());
    }

//    @Bean
//    public JedisPoolConfig jedisPoolConfig() {
//        JedisPoolConfig poolConfig = new JedisPoolConfig();
//        poolConfig.setMaxTotal(100);
//        poolConfig.setTestOnBorrow(true);
//        poolConfig.setTestOnReturn(true);
//        return poolConfig;
//    }

    /**
     * @see RedisClusterConfiguration
     * @return
     */
    @Bean
    public RedisClusterConfiguration redisClusterConfiguration(){
        Map<String, Object> source = Maps.newHashMap();
        source.put("spring.redis.cluster.nodes", redisClusterNodes);
        log.debug("Redis 启动节点为【{}】", redisClusterNodes);
        source.put("spring.redis.cluster.max-redirects", config.getRedisMaxRedirects());
        log.info("Redis 最大重定向次数为为【{}】", config.getRedisMaxRedirects());
        return new RedisClusterConfiguration(new MapPropertySource("RedisClusterConfiguration", source));
    }

//    @Bean
//    public RedisConnectionFactory redisConnectionFactory() {
//        JedisConnectionFactory factory;
//        if (clusterNodes.split(ApplicationConstants.COMMA).length == 1) {
//            factory = new JedisConnectionFactory(jedisPoolConfig());
//            factory.setHostName(clusterNodes.split(ApplicationConstants.COLON)[0]);
//            factory.setPort(Integer.valueOf(clusterNodes.split(ApplicationConstants.COLON)[1]));
//        } else {
//            factory = new JedisConnectionFactory(redisClusterConfiguration(), jedisPoolConfig());
//        }
//        factory.setPassword(password);
//        factory.setUsePool(true);
//        return factory;
//    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        LettuceConnectionFactory factory = null;
        try {
            RedisConfigType redisConfigTypeEnum = Enum.valueOf(RedisConfigType.class, config.getRedisConfigType());
            Assert.notNull(redisConfigTypeEnum, "Redis配置类型不能为空");
            if(RedisConfigType.propertiesRedis.equals(redisConfigTypeEnum)){
                redisClusterNodes = config.getRedisClusterNodes();
            }
            else if(RedisConfigType.dictValueRedis.equals(redisConfigTypeEnum)){
                SysDictValue sysDictValue = SysDictValue.builder().dictType(DICT_VALUE_REDIS).name(DICT_VALUE_REDIS).build();
                String loginuser = StringUtils.replace(encryptor.encrypt(ADMINISTRATOR), "+", "%2B");
                JsonResponse jsonResponse = HttpClient.textBody(config.getUumsAddress() + "/sys/dictValue/sso/findAllNoPage?loginuser="+loginuser+"&appcode="+UUMS_APPCODE)
                        .json(JacksonUtils.obj2json(sysDictValue))
                        .asBean(JsonResponse.class);
//                SysDictValue redisDv = sysDictValueApiHandle.handRemoteResponse(jsonResponse, SysDictValue.class);
                List<SysDictValue> sysDictValueList = sysDictValueApiListHandle.handRemoteTypeReferenceResponse(jsonResponse, new TypeReference<List<SysDictValue>>(){});
                SysDictValue redisDv = sysDictValueList.get(ZERO);
                Assert.notNull(redisDv, "REDIS节点配置不能为空！");
                redisClusterNodes = redisDv.getValue();
            }
            else {
                AppFileSftpUtil appFileSftpUtil = new AppFileSftpUtil();
                appFileSftpUtil.setUsername(config.getRedisFtpUsername());
                appFileSftpUtil.setPassword(config.getRedisFtpPassword());
                appFileSftpUtil.setHost(config.getRedisFtpHost());
                appFileSftpUtil.setPort(config.getRedisFtpPort());
                appFileSftpUtil.setKeyFilePath(config.getRedisFtpKeyFile());
                appFileSftpUtil.setPassphrase(config.getRedisFtpPassphrase());
                if(RedisConfigType.ftpRedis.equals(redisConfigTypeEnum)){
                    appFileSftpUtil.setServerUploadLocation(StoreLocation.ftp);
                }
                if(RedisConfigType.sftpRedis.equals(redisConfigTypeEnum)){
                    appFileSftpUtil.setServerUploadLocation(StoreLocation.sftp);
                }
                redisClusterNodes = new String(appFileSftpUtil.download2Byte(config.getRedisFtpNodeConfigDirectory(),
                        config.getRedisFtpNodeConfigFile()));
            }
            redisClusterNodes = StringUtils.trimAllWhitespace(redisClusterNodes);
            Assert.notNull(redisClusterNodes, "REDIS节点配置不能为空！");
            log.info("==========================Redis节点为【{}】==========================", redisClusterNodes);
            if (redisClusterNodes.split(ApplicationConstants.COMMA).length == 1) {
                RedisStandaloneConfiguration standaloneConfig = new RedisStandaloneConfiguration();
                standaloneConfig.setHostName(redisClusterNodes.split(ApplicationConstants.COLON)[ZERO]);
                standaloneConfig.setPort(Integer.valueOf(redisClusterNodes.split(ApplicationConstants.COLON)[ONE]));
                standaloneConfig.setPassword(RedisPassword.of(config.getRedisPassword()));
                standaloneConfig.setDatabase(0);
                factory = new LettuceConnectionFactory(standaloneConfig);
            } else {
                RedisClusterConfiguration clusterConfig = redisClusterConfiguration();
                clusterConfig.setPassword(RedisPassword.of(config.getRedisPassword()));
                factory = new LettuceConnectionFactory(clusterConfig);
            }
        }
        catch (Exception e){
            log.error("加载Redis配置发生错误，请检查配置文件");
            Exceptions.printException(e);
        }
        return factory;
    }

//    @Bean
//    public RedisCacheConfiguration redisCacheConfiguration() {
//        return RedisCacheConfiguration
//                .defaultCacheConfig()
//                .serializeKeysWith(
//                        RedisSerializationContext
//                                .SerializationPair
//                                .fromSerializer(new StringRedisSerializer()))
//                .serializeValuesWith(RedisSerializationContext.
//                                SerializationPair.
//                                fromSerializer(new JdkSerializationRedisSerializer(this.getClass().getClassLoader())))
//                //默认1小时超时
//                .entryTtl(Duration.ofSeconds(3600));
//    }


    @Bean
    @Override
    public CacheManager cacheManager() {
        // 初始化一个RedisCacheWriter
        RedisCacheWriter cacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory());
        // 设置默认过期时间：60 分钟
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(config.getRedisMaxInactiveIntervalInSeconds()))
                //.prefixKeysWith("cache:key:uums:") //无法区分不同对象相同id时的key
                // .disableCachingNullValues()
                // 使用注解时的序列化、反序列化
                .serializeKeysWith(RedisSerializationContext
                        .SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext
                        .SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
        Map<String, RedisCacheConfiguration> initialCacheConfigurations = new HashMap<>();
        return new RedisCacheManager(cacheWriter, defaultCacheConfig, initialCacheConfigurations);
    }

    @Bean
    @Qualifier("redisTemplate")
    public <T> RedisTemplate<String, T> redisTemplate() {
        /**
         * 解决分离项目报空指针问题
         * 参考：https://www.jianshu.com/p/32d38a7fd20a
         */
        ClassLoader classLoader = this.getClass().getClassLoader();
        RedisTemplate<String, T> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new JdkSerializationRedisSerializer(classLoader));
        template.setHashKeySerializer(new JdkSerializationRedisSerializer(classLoader));
        template.setHashValueSerializer(new JdkSerializationRedisSerializer(classLoader));
        template.setDefaultSerializer(new JdkSerializationRedisSerializer(classLoader));
        template.afterPropertiesSet();
        return template;
    }

    /**
     * 自定义Key生成策略
     * @return
     */
    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return redisKeyGenerator;
    }

    @Bean
    @Override
    public CacheErrorHandler errorHandler() {
        CacheErrorHandler cacheErrorHandler = new CacheErrorHandler() {
            /**
             * 从缓存读取数据报错时，不作处理，由数据库提供服务
             * @param e
             * @param cache
             * @param key
             */
            @Override
            public void handleCacheGetError(RuntimeException e, Cache cache, Object key) {
                if(e instanceof RedisConnectionFailureException){
                    log.warn("redis 丢失连接 connection:",e);
                    return;
                }
                throw e;
            }

            /**
             * 向缓存写入数据报错时，不作处理，由数据库提供服务
             * @param e
             * @param cache
             * @param key
             * @param value
             */
            @Override
            public void handleCachePutError(RuntimeException e, Cache cache, Object key, Object value) {
                if(e instanceof RedisConnectionFailureException){
                    log.warn("redis 丢失连接 connection:",e);
                    return;
                }
                throw e;
            }

            /**
             * 删除缓存报错时，抛出异常
             * @param e
             * @param cache
             * @param key
             */
            @Override
            public void handleCacheEvictError(RuntimeException e, Cache cache, Object key) {
                log.error("handleCacheEvictError缓存时异常---key：-"+key+"异常信息:"+e);
                throw e;
            }

            /**
             * 清理缓存报错时，抛出异常
             * @param e
             * @param cache
             */
            @Override
            public void handleCacheClearError(RuntimeException e, Cache cache) {
                log.error("清除缓存时异常---：-"+"异常信息:"+e);
                throw e;
            }
        };
        return cacheErrorHandler;
    }

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config redissonConfig = new Config();
        if (redisClusterNodes.split(ApplicationConstants.COMMA).length == 1) {
            redissonConfig.useSingleServer().setAddress("redis://"+redisClusterNodes)
            .setPassword(config.getRedisPassword());
        } else {
            String[] nodes = redisClusterNodes.split(ApplicationConstants.COMMA);
            for(int i=0; i<nodes.length; i++){
                nodes[i] = "redis://"+ nodes[i];
            }
            redissonConfig.useClusterServers()
                    .setScanInterval(2000) // cluster state scan interval in milliseconds
                    .setPassword(config.getRedisPassword())
                    .addNodeAddress(nodes);
//                    .addNodeAddress("redis://10.92.80.70:26379", "redis://10.92.80.70:26389", "redis://10.92.80.70:26399")
//                    .addNodeAddress("redis://10.92.80.71:26379", "redis://10.92.80.71:26389", "redis://10.92.80.71:26399");
        }
        redissonClient = Redisson.create(redissonConfig);
        log.debug("Congratulations------------------------------------------------Redis 进程实例已创建成功");
        return redissonClient;
    }

    @Bean
    @DependsOn("redissonClient")
    public DistributedLockFactoryBean distributeLockTemplate(){
        DistributedLockFactoryBean d = new DistributedLockFactoryBean();
        d.setMode("SINGLE");
        return d;
    }

    @PreDestroy
    public void destroy() {
        if(null != redissonClient) {
            log.debug("清理分布式事务锁START................................");
            RedisUtil.cleanRedisLock();
            log.debug("清理分布式事务锁END................................");
//            redissonClient.shutdown(); //RedisConfiguration.redissonClient()申明创建出来的RedissonClient的shutdown执行真正的销毁redissonClient
            log.debug("Congratulations------------------------------------------------Redis 进程实例已销毁成功");
        }
    }

}

