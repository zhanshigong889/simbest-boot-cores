package com.simbest.boot.sys.service.impl;

import com.simbest.boot.base.service.impl.SystemService;
import com.simbest.boot.sys.model.SysLogWeb;
import com.simbest.boot.sys.repository.SysLogWebRepository;
import com.simbest.boot.sys.service.ISysLogWebService;
import com.simbest.boot.util.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

/**
 * 用途：系统Web请求日志
 * 作者: lishuyi
 * 时间: 2018/2/23  10:14
 */
@Slf4j
@Service
public class SysLogWebService extends SystemService<SysLogWeb, String> implements ISysLogWebService {

    @Autowired
    private SysLogWebRepository repository;

    @Autowired
    public SysLogWebService(SysLogWebRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    @Transactional
    public SysLogWeb update (SysLogWeb source ) {
        String creator = SecurityUtils.getCurrentUserName();
        source.setCreator(creator);
        return super.update(source);
    }

}
