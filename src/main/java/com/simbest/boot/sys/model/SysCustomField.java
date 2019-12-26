/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.sys.model;

import com.simbest.boot.base.annotations.AnnotationUtils;
import com.simbest.boot.base.annotations.EntityIdPrefix;
import com.simbest.boot.base.enums.SysCustomFieldType;
import com.simbest.boot.base.model.LogicModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;

/**
 * 用途：实体自定义字段
 * 作者: lishuyi
 * 时间: 2018/1/30  17:17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class SysCustomField extends LogicModel {

    @Id
    @Column(name = "id", length = 40)
    @GeneratedValue(generator = "snowFlakeId")
    @GenericGenerator(name = "snowFlakeId", strategy = "com.simbest.boot.util.distribution.id.SnowflakeId")
    @EntityIdPrefix(prefix = "F") //主键前缀，此为可选项注解
    private String id;

    //所属实体分类
    @Column
    private String fieldClassify;

    //显示被EntityCnName注解的类的中文名称
    @Transient
    private String fieldClassifyCn;

    //字段名称
    @Column
    private String fieldName;

    //字段排序值
    @Column
    private Integer orderBy;

    //字段类型
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private SysCustomFieldType fieldType;

    //关联数据字典，fieldType=DICT时有值
    @Column
    private String dictId;

    /**
     * @return 实体类中文名称
     */
    public String getFieldClassifyCn() {
        return AnnotationUtils.getEntityCnNameClassifyMap().get(fieldClassify);
    }
}
