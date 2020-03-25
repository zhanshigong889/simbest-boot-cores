package com.simbest.boot.sys.service.impl;

import com.google.common.collect.Lists;
import com.simbest.boot.base.service.impl.LogicService;
import com.simbest.boot.sys.model.SysOperateLog;
import com.simbest.boot.sys.repository.SysOperateLogRepository;
import com.simbest.boot.sys.service.ISysOperateLogService;
import com.simbest.boot.util.json.JacksonUtils;
import com.simbest.boot.util.security.LoginUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * <strong>Title : ISysOperateLogService</strong><br>
 * <strong>Description : 系统操作日志</strong><br>
 * <strong>Create on : 2018/10/10</strong><br>
 * <strong>Modify on : 2018/10/10</strong><br>
 * <strong>Copyright (C) Ltd.</strong><br>
 *
 * @author LJW lijianwu@simbest.com.cn
 * @version <strong>V1.0.0</strong><br>
 * <strong>修改历史:</strong><br>
 * 修改人 修改日期 修改描述<br>
 * -------------------------------------------<br>
 */
@Slf4j
@Service
public class SysOperateLogService extends LogicService<SysOperateLog,String> implements ISysOperateLogService {


    private SysOperateLogRepository sysOperateLogRepository;

    @Autowired
    private LoginUtils loginUtils;

    @Autowired
    public SysOperateLogService ( SysOperateLogRepository repository ) {
        super( repository );
        this.sysOperateLogRepository = repository;
    }

    /**
     * 保存系统操作日志
     * @param sysOperateLog         操作日志对象
     * @return SysOperateLog
     */
    @Override
    public SysOperateLog saveLog ( SysOperateLog sysOperateLog ) {
        return insert( sysOperateLog );
    }

    @Override
    public SysOperateLog saveLog ( Map<String, Object> sysOperateLogParam ) {
        log.warn( "操作日志，参数为：{}", JacksonUtils.obj2json( sysOperateLogParam ) );
        SysOperateLog sysOperateLog = new SysOperateLog();
        // 接收到请求，记录请求内容
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        // 记录下请求内容
        String reqUrl = request.getRequestURL().toString() + request.getMethod();
        List<Object> paramList = Lists.newArrayList();
        for(Map.Entry<String,Object> entry:sysOperateLogParam.entrySet()){
            if(!entry.getKey().equals("errorMsg")){
                paramList.add( entry.getKey() + "=" + entry.getValue() );
            }
        }
        String params = StringUtils.join( paramList );
        String source = (String)sysOperateLogParam.get( "source" );
        if ( "MOBILE".equals( source ) ){
            String currentUser = (String)sysOperateLogParam.get( "currentUser" );
            String appCode = (String)sysOperateLogParam.get( "appCode" );
            loginUtils.manualLogin( currentUser,appCode );
        }
        sysOperateLog.setInterfaceParam( params );
        sysOperateLog.setOperateInterface( reqUrl );
        sysOperateLog.setOperateFlag( source );
        sysOperateLog.setErrorMsg( (String)sysOperateLogParam.get( "errorMsg" ) );
        sysOperateLog = saveLog( sysOperateLog );
        return sysOperateLog;
    }


}
