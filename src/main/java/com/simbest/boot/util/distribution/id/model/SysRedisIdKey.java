/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.util.distribution.id.model;

import com.simbest.boot.base.model.GenericModel;
import io.swagger.annotations.ApiModelProperty;
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
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * 用途：Redis序列号前缀实体
 * 作者: lishuyi
 * 时间: 2020/5/14  15:51
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "sys_redis_id_key", uniqueConstraints = {
        @UniqueConstraint(name="sys_redis_id_day_name", columnNames = {"day", "name"})
})
public class SysRedisIdKey extends GenericModel {

    @Id
    @Column(name = "id", length = 40)
    @GeneratedValue(generator = "snowFlakeId")
    @GenericGenerator(name = "snowFlakeId", strategy = "com.simbest.boot.util.distribution.id.SnowflakeId")
    private String id;

    @ApiModelProperty(value = "发生日期")
    @Column
    private String day;

    @NonNull
    @ApiModelProperty(value = "ID名称")
    @Column(nullable = false)
    private String name;

    @NonNull
    @ApiModelProperty(value = "ID值")
    @Column(nullable = false)
    private Long value;

}
