/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.sys.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.simbest.boot.base.annotations.EntityIdPrefix;
import com.simbest.boot.base.model.GenericModel;
import com.simbest.boot.constants.ApplicationConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;

/**
 * 用途：系统登录日志
 * 作者: lishuyi
 * 时间: 2018/3/7  23:10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class SysLogLogin extends GenericModel {
    @Id
    @Column(name = "id", length = 40)
    @GeneratedValue(generator = "snowFlakeId")
    @GenericGenerator(name = "snowFlakeId", strategy = "com.simbest.boot.util.distribution.id.SnowflakeId")
    @EntityIdPrefix(prefix = "L") //主键前缀，此为可选项注解
    private String id;

    //登录账号
    @Column(nullable = false, length = 40)
    @NonNull
    private String account;

    //账号类型
    // 0 用户名登录方式
    @Column(nullable = false, length = 20)
    @NonNull
    private Integer loginType;

    //登录入口
    // 0 PC登录入口
    // 1 CAS登录入口
    // 2 微信登录入口
    @Column(nullable = false, length = 20)
    @NonNull
    private Integer loginEntry;

    @Column
    private String sessionid;

    @Column
    private String ip;

    @Column
    private String mac;

    @JsonFormat(pattern = ApplicationConstants.FORMAT_DATE_TIME, timezone = ApplicationConstants.FORMAT_TIME_ZONE)
    private Date loginTime;

    @JsonFormat(pattern = ApplicationConstants.FORMAT_DATE_TIME, timezone = ApplicationConstants.FORMAT_TIME_ZONE)
    private Date logoutTime;

    @Column
    private Boolean isSuccess;

    @Column(length = 200)
    private String remark;

    @Column
    private String trueName;

    @Column
    private String belongOrgName;

    @Column
    private String osFamily;

    @Column
    private String os;

    @Column
    private String browserName;

    @Column
    private String browserVersion;

    @Column
    private String browserEngine;

    @Column
    private String browserEngineVersion;

    @Column
    private Boolean isMobile;

}
