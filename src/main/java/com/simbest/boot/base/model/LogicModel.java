/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.base.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

/**
 * 用途：逻辑实体
 * 作者: lishuyi
 * 时间: 2018/1/30  21:51
 */
@EqualsAndHashCode(callSuper = true)
@MappedSuperclass
public abstract class LogicModel extends SystemModel {

    @Setter
    @Getter
    @Column(nullable = false)
    //是否可用
    private Boolean enabled;

    @Setter
    @Getter
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime removedTime;

    @Setter
    @Getter
//    @Column(nullable = false, updatable = false)
    @Column(nullable = false) //确保可以更新，在逻辑删除数据后，再次恢复数据有可能创建人不一样
    //创建人 CREATOR
    private String creator;

    @Setter
    @Getter
    @Column(nullable = false)
    //更新人 MODIFIER
    private String modifier;

}
