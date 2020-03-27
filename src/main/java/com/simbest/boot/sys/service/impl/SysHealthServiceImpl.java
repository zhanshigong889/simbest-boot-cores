/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.sys.service.impl;

import cn.hutool.core.io.FileUtil;
import com.simbest.boot.base.enums.StoreLocation;
import com.simbest.boot.base.exception.Exceptions;
import com.simbest.boot.config.AppConfig;
import com.simbest.boot.config.EmbeddedServletConfiguration;
import com.simbest.boot.config.ExtraConfig;
import com.simbest.boot.constants.ApplicationConstants;
import com.simbest.boot.sys.model.SysFile;
import com.simbest.boot.sys.model.SysHealth;
import com.simbest.boot.sys.service.IHeartTestService;
import com.simbest.boot.sys.service.ISimpleSmsService;
import com.simbest.boot.sys.service.ISysHealthService;
import com.simbest.boot.util.AppFileUtil;
import com.simbest.boot.util.FastDfsClient;
import com.simbest.boot.util.redis.RedisUtil;
import com.simbest.boot.util.server.HostUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.csource.fastdfs.ProtoCommon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.List;

import static com.simbest.boot.constants.ApplicationConstants.UTF_8;
import static com.simbest.boot.constants.ApplicationConstants.ZERO;

/**
 * 用途：系统健康检查服务层
 * 作者: lishuyi
 * 时间: 2019/12/6  9:51
 */
@Slf4j
@Service
public class SysHealthServiceImpl implements ISysHealthService, IHeartTestService {

    @Autowired
    private AppConfig config;

    @Autowired
    private ExtraConfig extraConfig;

    @Autowired
    @Qualifier("jdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    @Value("${app.sys.database.validation-query:SELECT 1 FROM DUAL}")
    private String validationQuery;

    @Autowired
    private AppFileUtil appFileUtil;

    @Autowired
    private ISimpleSmsService smsService;

    private SysFile testSysFile;


    @PostConstruct
    public void init(){
        log.info("------------系统健康检查器开始初始化--------------");
        StoreLocation serverUploadLocation = appFileUtil.getServerUploadLocation();
        if(!StoreLocation.fastdfs.equals(serverUploadLocation)) {
            File notExistLocalFile = new File(config.getUploadTmpFileLocation().concat(ApplicationConstants.SEPARATOR).concat("heartCheckFile.txt"));
            if(null == testSysFile) {
                testSysFile = SysFile.builder().fileName(notExistLocalFile.getName()).fileType("txt").filePath(notExistLocalFile.getAbsolutePath())
                        .fileSize(notExistLocalFile.length()).downLoadUrl(notExistLocalFile.getAbsolutePath()).build();
            }
            File downloadTestFile = null;
            try {
                downloadTestFile = appFileUtil.getFileFromSystem(testSysFile);
            }
            catch (Exception e){
                if (null == downloadTestFile) {
                    FileUtil.writeString(ApplicationConstants.MSG_FILE_CHECK, notExistLocalFile, UTF_8);
                    testSysFile = appFileUtil.uploadFromLocalAutoServerDirectory(notExistLocalFile, "hearttemps");
                    log.info("测试文件已上传：【{}】", testSysFile);
                }
            }
        }
    }

    @Override
    public SysHealth databaseCheck(){
        try {
            List result = jdbcTemplate.queryForList(validationQuery);
            if(null != result && result.size() > ZERO) {
                log.info("数据库连接【{}】测试OK", config.getDatasourceUrl());
                return SysHealth.builder().result(true).build();
            }
            else {
                log.error("数据库连接【{}】测试Fail", config.getDatasourceUrl());
                return SysHealth.builder().result(false).message("validation-query返回错误").build();
            }
        }
        catch (Exception e){
            Exceptions.printException(e);
            return SysHealth.builder().result(false).message(e.getMessage()).build();
        }
    }

    @Override
    public SysHealth redisCheck(){
        log.info("开始对Redis节点【{}】进行可用性测试", config.getRedisClusterNodes());
        if (config.getRedisClusterNodes().split(ApplicationConstants.COMMA).length == 1) {
            String redisHost = config.getRedisClusterNodes().split(ApplicationConstants.COLON)[0];
            Integer redisPort = Integer.valueOf(config.getRedisClusterNodes().split(ApplicationConstants.COLON)[1]);
            return redisCheckAvailable(redisHost, redisPort);
        } else {
            SysHealth sysHealth = SysHealth.builder().result(true).build();
            StringBuffer sb = new StringBuffer();
            String[] redisNodes = config.getRedisClusterNodes().split(ApplicationConstants.COMMA);
            for(String redisNode : redisNodes){
                String redisHost = redisNode.split(ApplicationConstants.COLON)[0];
                Integer redisPort = Integer.valueOf(redisNode.split(ApplicationConstants.COLON)[1]);
                SysHealth result = redisCheckAvailable(redisHost, redisPort);
                if(!result.getResult()){
                    sysHealth.setResult(false);
                    sb.append(result.getMessage());
                }
            }
            sysHealth.setMessage(sb.toString());
            return sysHealth;
        }
    }

    private SysHealth redisCheckAvailable(String redisHost, Integer redisPort){
        if(HostUtil.checkTelnet(redisHost, redisPort)){
            String masterHost = RedisUtil.get(ApplicationConstants.MASTER_HOST);
            Integer masterPort = RedisUtil.getBean(ApplicationConstants.MASTER_PORT, Integer.class);
            if(StringUtils.isNotEmpty(masterHost) && null != masterPort){
                log.info("Redis主机【{}】端口【{}】测试OK，集群主控节点为【{}】端口【{}】", redisHost, redisPort, masterHost, masterPort);
                return SysHealth.builder().result(true).build();
            }
            else{
                log.error("Redis主机【{}】端口【{}】测试Fail，集群主控节点为【{}】端口【{}】", redisHost, redisPort, masterHost, masterPort);
                return SysHealth.builder().result(false).message("缓存读取错误").build();
            }
        }
        else {
            log.error("Redis主机【{}】端口【{}】测试Fail，心跳不通，集群主控节点为【{}】端口【{}】", redisHost, redisPort);
            return SysHealth.builder().result(false).message("Redis主机".concat(redisHost).concat("端口").concat(redisPort.toString()).concat("心跳不通")).build();
        }
    }

    @Override
    public SysHealth fileSystemCheck(){
        SysHealth sysHealth = SysHealth.builder().result(true).build();
        StoreLocation serverUploadLocation = appFileUtil.getServerUploadLocation();
        switch (serverUploadLocation) {
            case disk:
                File diskFile = appFileUtil.getFileFromSystem(testSysFile);
                if(null == diskFile){
                    log.error("文件基于【{}】读取文件Fail", serverUploadLocation);
                    sysHealth.setResult(false);
                    sysHealth.setMessage("文件基于"+serverUploadLocation+"读取文件失败");
                }
                else{
                    String content = FileUtil.readString(diskFile, UTF_8);
                    if(ApplicationConstants.MSG_FILE_CHECK.equals(content)) {
                        log.info("文件系统基于disk方式，读取文件【{}】测试OK", diskFile);
                    }
                    else{
                        log.error("文件基于【{}】读取文件内容Fail", serverUploadLocation);
                        sysHealth.setResult(false);
                        sysHealth.setMessage("文件基于"+serverUploadLocation+"读取文件内容失败");
                    }
                }
                break;
            case fastdfs:
                String fastdfsServerStr = extraConfig.getValue("fastdfs.tracker_servers");
                String[] fastdfsServers = StringUtils.split(fastdfsServerStr, ApplicationConstants.COMMA);
                StringBuffer sb = new StringBuffer();
                for(String fastdfsServer : fastdfsServers){
                    String fastdfsHost = fastdfsServer.split(ApplicationConstants.COLON)[0];
                    Integer fastdfsPort = Integer.valueOf(fastdfsServer.split(ApplicationConstants.COLON)[1]);
                    if(HostUtil.checkTelnet(fastdfsHost, fastdfsPort)){
                        try {
                            boolean fastdfsResult = ProtoCommon.activeTest(FastDfsClient.getTrackerServer().getSocket());
                            if(!fastdfsResult){
                                log.error("FastDFS主机".concat(fastdfsHost).concat("端口").concat(fastdfsPort.toString())+"官方SDK检测失败!");
                                sysHealth.setResult(false);
                                sb.append("FastDFS主机".concat(fastdfsHost).concat("端口").concat(fastdfsPort.toString())+"官方SDK检测失败!");
                            }
                            else{
                                log.info("FastDfs主机【{}】端口【{}】测试OK", fastdfsHost, fastdfsPort);
                            }
                        }
                        catch(Exception e){
                            log.error("FastDFS主机".concat(fastdfsHost).concat("端口").concat(fastdfsPort.toString())+"检测异常：".concat(e.getMessage()));
                            sysHealth.setResult(false);
                            sb.append("FastDFS主机".concat(fastdfsHost).concat("端口").concat(fastdfsPort.toString())+"检测异常：".concat(e.getMessage()));
                        }
                    }
                    else{
                        log.error("FastDFS主机".concat(fastdfsHost).concat("端口").concat(fastdfsPort.toString()).concat("网络连接不通"));
                        sysHealth.setResult(false);
                        sb.append("FastDFS主机".concat(fastdfsHost).concat("端口").concat(fastdfsPort.toString()).concat("网络连接不通"));
                    }
                }
                sysHealth.setMessage(sb.toString());
                break;
            case ftp:
                log.info("基于ftp，方式同sftp");
            case sftp:
                File sftpFile = appFileUtil.getFileFromSystem(testSysFile);
                if(null == sftpFile){
                    log.error("文件基于"+serverUploadLocation+"读取取文件失败");
                    sysHealth.setResult(false);
                    sysHealth.setMessage("文件基于"+serverUploadLocation+"读取取文件失败");
                }
                else{
                    String content = FileUtil.readString(sftpFile, UTF_8);
                    if(ApplicationConstants.TEST.equals(content)) {
                        log.info("文件系统基于【{}】方式，读取文件【{}】测试OK", serverUploadLocation, sftpFile);
                    }
                    else{
                        log.error("文件基于【{}】读取文件内容Fail", serverUploadLocation);
                        sysHealth.setResult(false);
                        sysHealth.setMessage("文件基于"+serverUploadLocation+"读取文件内容失败");
                    }
                }
                break;
        }
        return sysHealth;
    }

    @Override
    public SysHealth doTest() {
        SysHealth databaseHealth = databaseCheck();
        SysHealth redisHealth = redisCheck();
        SysHealth fileSystemHealth = fileSystemCheck();
        SysHealth resultHealth = SysHealth.builder().message(ApplicationConstants.EMPTY)
                .hostIp(HostUtil.getHostAddress()).hostPort(EmbeddedServletConfiguration.serverPort).build();
        if(StringUtils.isNotEmpty(databaseHealth.getMessage())){
            resultHealth.setMessage(resultHealth.getMessage().concat(ApplicationConstants.VERTICAL).concat(databaseHealth.getMessage()));
        }
        if(StringUtils.isNotEmpty(redisHealth.getMessage())){
            resultHealth.setMessage(resultHealth.getMessage().concat(ApplicationConstants.VERTICAL).concat(redisHealth.getMessage()));
        }
        if(StringUtils.isNotEmpty(fileSystemHealth.getMessage())){
            resultHealth.setMessage(resultHealth.getMessage().concat(ApplicationConstants.VERTICAL).concat(fileSystemHealth.getMessage()));
        }
        if(databaseHealth.getResult().equals(redisHealth.getResult()) &&
                databaseHealth.getResult().equals(fileSystemHealth.getResult()) &&
                databaseHealth.getResult().equals(true)) {
            resultHealth.setResult(true);
        }
        else{
            resultHealth.setResult(false);
            smsService.sendHealthCheckMessage(resultHealth.getMessage());
        }
        return resultHealth;
    }

}
