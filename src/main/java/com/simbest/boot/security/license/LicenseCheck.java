package com.simbest.boot.security.license;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * <strong>Title : LicenseCheck.java</strong><br>
 * <strong>Description : </strong><br>
 * <strong>Create on : 2020年7月1日下午6:45:59</strong><br>
 * <strong>Modify on : 2020年7月1日下午6:45:59</strong><br>
 * <strong>Copyright (C) Ltd.</strong><br>
 * 
 * @author LJW lijianwu@simbest.com.cn
 * @version <strong>V1.0.0</strong><br>
 *          <strong>修改历史:</strong><br>
 *          修改人 修改日期 修改描述<br>
 *          -------------------------------------------<br>
 */
@Slf4j
@Component
public class LicenseCheck {
	
	@Autowired
	private VerifyLicense verifyLicense;
	
	/**
	 * @Title: init
	 * @Description: 验证License是否生效
	 * @param:    
	 * @return: void   
	 * @throws
	 */
	@PostConstruct
    public void init() {
        if (verifyLicense.vertify()) {
			log.warn("License expired, please contact the administrator！");
            Runtime.getRuntime().halt(1);
        }
    }
}
