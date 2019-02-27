/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.component.distributed.lock;

import com.simbest.boot.constants.ApplicationConstants;
import com.simbest.boot.util.redis.RedisUtil;
import com.simbest.boot.util.server.HostUtil;
import com.simbest.boot.util.server.SocketUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 用途：集群环境中的应用Master
 * 作者: lishuyi
 * 时间: 2018/6/22  21:33
 */
@Slf4j
@Component
@DependsOn(value = {"redisUtil", "hostUtil"})
public class AppRuntimeMaster {

    @Autowired
    private HostUtil hostUtil;

    @Getter
    private String myHost;

    @Getter
    private Integer myPort;

    @Getter
    private String masterHost;

    @Getter
    private Integer masterPort;

    @PostConstruct
    private void init() {
        checkMasterIsMe();
    }

    public boolean checkMasterIsMe() {
        if(StringUtils.isEmpty(myHost))
            myHost = HostUtil.getHostAddress();
        if(myPort == null || ApplicationConstants.ZERO==myPort)
            myPort = hostUtil.getRunningPort();
        log.debug("My host is {} ", myHost);
        log.debug("Cluster master is {} ", RedisUtil.get(ApplicationConstants.MASTER_HOST));
        log.debug("My host port is {}", myPort);
        log.debug("Cluster master port is {}", RedisUtil.getBean(ApplicationConstants.MASTER_PORT, Integer.class));
        boolean isMaster = myHost.equals(RedisUtil.get(ApplicationConstants.MASTER_HOST))
                && myPort.equals(RedisUtil.getBean(ApplicationConstants.MASTER_PORT, Integer.class));
        log.debug("Check master is me result is {} ", isMaster);
        return isMaster;
    }

    /**
     * 每隔五分钟调用的表达式为： "0 0/5 * * * ?" 参考 TaskTriggerDefinition
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    public void becameMasertIfNotExist() {
        //获取锁
        DistributedRedisLock.lock(ApplicationConstants.MASTER_LOCK);

        //提取应用主机端口信息
        masterHost = RedisUtil.get(ApplicationConstants.MASTER_HOST);
        masterPort = RedisUtil.getBean(ApplicationConstants.MASTER_PORT, Integer.class);
        //1.没有Master
        if (StringUtils.isEmpty(masterHost) || null==masterPort || ApplicationConstants.ZERO==masterPort) {
            makeMeAsMaster(myHost, myPort);
            log.debug("I'm the master, and is already running at IP: {} on port {} ...", myHost, myPort);
        }

        //检查应用主机端口是否存活
        boolean masterIsAvailable = SocketUtil.checkHeartConnection(masterHost, Integer.valueOf(masterPort));
        //2.Master不可用
        if (!masterIsAvailable) {
            myPort = hostUtil.getRunningPort();
            makeMeAsMaster(myHost, myPort);
            log.debug("I will become cluster master with IP: {} on port {} ...", myHost, myPort);
        } else {
            log.debug("Master is already at IP: {} on port {} ...", masterHost, masterPort);
        }

        //释放锁
        DistributedRedisLock.unlock(ApplicationConstants.MASTER_LOCK);
    }

    /**
     * 当前主机作为MASTER
     * @param myHost
     * @param myPort
     */
    private void makeMeAsMaster(String myHost, Integer myPort){
        masterHost = myHost;
        masterPort = myPort;
        RedisUtil.set(ApplicationConstants.MASTER_HOST, myHost);   //设置我为Master
        RedisUtil.setBean(ApplicationConstants.MASTER_PORT, myPort);
    }

}
