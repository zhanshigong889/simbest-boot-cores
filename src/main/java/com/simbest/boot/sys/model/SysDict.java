/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.sys.model;


import com.simbest.boot.base.annotations.EntityIdPrefix;
import com.simbest.boot.base.model.LogicModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

/**
 * 用途：数据字典
 * 作者: lishuyi
 * 时间: 2018/1/30  17:17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "sys_dict", uniqueConstraints = {
        @UniqueConstraint(name="blocid_corpid_dictType", columnNames = {"blocid", "corpid", "dictType"})
})
public class SysDict extends LogicModel {

    @Id
    @Column(name = "id", length = 40)
    @GeneratedValue(generator = "snowFlakeId")
    @GenericGenerator(name = "snowFlakeId", strategy = "com.simbest.boot.util.distribution.id.SnowflakeId")
    @EntityIdPrefix(prefix = "D") //主键前缀，此为可选项注解
    private String id;

    @ApiModelProperty(value = "字典类型")
    @Column(nullable = false)
    private String dictType;

    @ApiModelProperty(value = "字典名称")
    @Column(nullable = false, length = 50)
    private String name;

    @ApiModelProperty(value = "字典描述")
    @Column
    private String description;

    @ApiModelProperty(value = "字典排序")
    @Column
    private Integer displayOrder;

    @ApiModelProperty(value = "父亲节点外键")
    @Column
    private String parentId;

    @ApiModelProperty(value = "流程类型标识")
    @Column(length = 50)
    private String flag;

    @ApiModelProperty(value = "扩展字段1")
    @Column(length = 200)
    private String spare1;

    @ApiModelProperty(value = "扩展字段2")
    @Column(length = 200)
    private String spare2;

    //公共字典，blocid和corpid保持为空，所有集团、企业通用
    //非公共字段，blocid和corpid不能为空
    //默认公共，即isPublic=true，数据库字段值=1
    @ApiModelProperty(value = "是否公共字典")
    @Column
    private Boolean isPublic;

    @ApiModelProperty(value = "集团id")
    @Column(length = 40)
    private String blocid;

    @ApiModelProperty(value = "企业id")
    @Column(length = 40)
    private String corpid;

}
