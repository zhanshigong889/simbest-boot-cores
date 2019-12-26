/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.sys.model;

import com.simbest.boot.base.annotations.EntityIdPrefix;
import com.simbest.boot.base.model.LogicModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * 用途：实体自定义字段值
 * 作者: lishuyi
 * 时间: 2017/12/22  15:51
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class SysCustomFieldValue extends LogicModel {

    @Id
    @Column(name = "id", length = 40)
    @GeneratedValue(generator = "snowFlakeId")
    @GenericGenerator(name = "snowFlakeId", strategy = "com.simbest.boot.util.distribution.id.SnowflakeId")
    @EntityIdPrefix(prefix = "V") //主键前缀，此为可选项注解
    private String id;

    //所属实体分类
    @Column(nullable = false)
    private String fieldClassify;

    //所属实体分类主键
    @Column(nullable = false)
    private Long fieldEntityId;

    @Column(nullable = false)
    private String customFieldId;

    @Column(nullable = false)
    private String customFieldValue;
}
