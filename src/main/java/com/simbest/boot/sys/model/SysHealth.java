/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.sys.model;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 用途：系统健康检查模型
 * 作者: lishuyi
 * 时间: 2019/12/9  9:43
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel(value = "系统管理-系统健康检查模型")
public class SysHealth {

    private Boolean result;

    private String message;

    //应用主机
    private String hostIp;

    //应用主机端口
    private Integer hostPort;

}
