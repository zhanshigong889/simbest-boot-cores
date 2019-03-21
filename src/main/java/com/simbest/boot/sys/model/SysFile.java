/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.sys.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.simbest.boot.base.annotations.EntityIdPrefix;
import com.simbest.boot.base.model.LogicModel;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

/**
 * 用途：统一系统文件管理
 * 作者: lishuyi
 * 时间: 2018/3/7  23:10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class SysFile extends LogicModel {
    @Id
    @Column(name = "id", length = 40)
    @GeneratedValue(generator = "snowFlakeId")
    @GenericGenerator(name = "snowFlakeId", strategy = "com.simbest.boot.util.distribution.id.SnowflakeId")
    @EntityIdPrefix(prefix = "V") //主键前缀，此为可选项注解
    private String id;

    //文件名称
    @Column(nullable = false, length = 200)
    @NonNull
    private String fileName;

    //文件类型
    @Column(nullable = false, length = 20)
    @NonNull
    private String fileType;

    //文件实际存储路径
    @Column(nullable = false, length = 500)
    @NonNull
    @JsonIgnore //隐藏不对外暴露内部路径
    private String filePath;

    //文件大小
    @Column(nullable = false, length = 50)
    @NonNull
    private Long fileSize;

    //归属流程
    @Column
    private String pmInsType;

    //归属流程ID
    @Column
    private String pmInsId;

    //归属流程区块
    @Column
    private String pmInsTypePart;

    //文件下载URL
    @Column(nullable = false, length = 500)
    @NonNull
    private String downLoadUrl;

    //专门用于标识是否跟随应用，不跟随云存储的文件
    @Column
    private Boolean isLocal = false;

    @Column(length = 500)
    @JsonIgnore //隐藏不对外暴露内部备份路径
    private String backupPath;

    //手机端使用
    @Transient
    private String mobileFilePath;
}
