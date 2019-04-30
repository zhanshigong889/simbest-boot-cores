package com.simbest.boot.sys.model;

import com.simbest.boot.base.annotations.EntityIdPrefix;
import com.simbest.boot.base.model.LogicModel;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

/**
 * <strong>Title : SysOperateLog</strong><br>
 * <strong>Description : 系统操作日志</strong><br>
 * <strong>Create on : 2018/10/10</strong><br>
 * <strong>Modify on : 2018/10/10</strong><br>
 * <strong>Copyright (C) Ltd.</strong><br>
 *
 * @author LJW lijianwu@simbest.com.cn
 * @version <strong>V1.0.0</strong><br>
 * <strong>修改历史:</strong><br>
 * 修改人 修改日期 修改描述<br>
 * -------------------------------------------<br>
 */
@Data
@EqualsAndHashCode (callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "SYS_LOG_OPERATE")
public class SysOperateLog extends LogicModel {

    @Id
    @Column (name = "id", length = 40)
    @GeneratedValue (generator = "snowFlakeId")
    @GenericGenerator (name = "snowFlakeId", strategy = "com.simbest.boot.util.distribution.id.SnowflakeId")
    @EntityIdPrefix (prefix = "L") //主键前缀，此为可选项注解
    private String id;

    @Column(length = 150)
    private String bussinessKey;                    //业务操作主键

    @Column(nullable = false,length = 500)
    private String operateInterface;                //调用接口

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private String interfaceParam;                  //接口参数

    @Column(nullable = false,length = 10)
    private String operateFlag;                     //客户端操作标识

    @Column(length = 1500)
    private String errorMsg;                        //错误信息
}
