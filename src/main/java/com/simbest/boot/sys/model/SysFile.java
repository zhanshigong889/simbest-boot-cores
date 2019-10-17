/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.sys.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.simbest.boot.base.annotations.EntityIdPrefix;
import com.simbest.boot.base.enums.StoreLocation;
import com.simbest.boot.base.model.LogicModel;
import io.swagger.annotations.ApiModel;
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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * 用途：统一文件管理
 * 作者: lishuyi
 * 时间: 2018/3/7  23:10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@ApiModel(value = "系统管理-统一文件管理")
public class SysFile extends LogicModel {

    @Id
    @Column(name = "id", length = 40)
    @GeneratedValue(generator = "snowFlakeId")
    @GenericGenerator(name = "snowFlakeId", strategy = "com.simbest.boot.util.distribution.id.SnowflakeId")
    @EntityIdPrefix(prefix = "V") //主键前缀，此为可选项注解
    private String id;


    @ApiModelProperty(value = "文件名称")
    @Column(nullable = false, length = 200)
    @NonNull
    private String fileName;

    @ApiModelProperty(value = "文件类型")
    @Column(nullable = false, length = 20)
    @NonNull
    private String fileType;

    @ApiModelProperty(value = "文件实际存储服务器路径")
    @Column(nullable = false, length = 500)
    @NonNull
    @JsonIgnore //隐藏不对外暴露内部路径
    private String filePath;

    @ApiModelProperty(value = "文件大小")
    @Column(nullable = false, length = 50)
    @NonNull
    private Long fileSize;

    @ApiModelProperty(value = "归属流程")
    @Column
    private String pmInsType;

    @ApiModelProperty(value = "归属流程ID")
    @Column
    private String pmInsId;

    @ApiModelProperty(value = "归属流程区块")
    @Column
    private String pmInsTypePart;

    @ApiModelProperty(value = "文件下载URL")
    @Column(nullable = false, length = 500)
    @NonNull
    private String downLoadUrl;

    @ApiModelProperty(value = "专门用于标识是否跟随应用，不跟随云存储的文件")
    @Column
    private Boolean isLocal = false;

    @ApiModelProperty(value = "隐藏不对外暴露内部备份路径")
    @Column(length = 500)
    @JsonIgnore
    private String backupPath;

    @ApiModelProperty(value = "手机端下载路径")
    @Column(length = 500)
    private String mobileFilePath;

    @ApiModelProperty(value = "API下载路径")
    @Column(length = 500)
    private String apiFilePath;

    @ApiModelProperty(value = "匿名端下载路径")
    @Column(length = 500)
    private String anonymousFilePath;

    @ApiModelProperty(value = "文件存储方式")
    @Column(length = 10)
    @Enumerated(EnumType.STRING)
    private StoreLocation storeLocation;

}
