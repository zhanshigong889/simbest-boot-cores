/**
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.base.enums;

import lombok.Getter;
import lombok.Setter;

/**
 * 用途：文件上传类型
 * 作者: lishuyi 
 * 时间: 2019/10/16  14:39
 */
public enum StoreLocation implements GenericEnum {

    disk("disk"),  fastdfs("fastdfs"), sftp("sftp");

    @Setter @Getter
    private String value;

    StoreLocation(String value) {
        this.value = value;
    }

}
