package com.simbest.boot.util;

import cn.hutool.core.io.FileUtil;
import com.simbest.boot.base.exception.Exceptions;
import com.simbest.boot.constants.ApplicationConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


/**
 * <strong>Title : FileUtils</strong><br>
 * <strong>Description : 文件操作工具</strong><br>
 * <strong>Create on : 2020/7/8</strong><br>
 * <strong>Modify on : 2020/7/8</strong><br>
 * <strong>Copyright (C) Ltd.</strong><br>
 *
 * @author LJW lijianwu@simbest.com.cn
 * @version <strong>V1.0.0</strong><br>
 * <strong>修改历史:</strong><br>
 * 修改人 修改日期 修改描述<br>
 * -------------------------------------------<br>
 */
@Slf4j
@Component
public class FileUtils {

    public boolean writeContentToFile(String filePath,String fileContent){
        try {
            FileUtil.writeString(fileContent,filePath, ApplicationConstants.UTF_8);
            return Boolean.TRUE;
        }catch (Exception e){
            Exceptions.printException( e );
        }
        return Boolean.FALSE;
    }

    public static void main(String[] args) {
        FileUtils fileUtils = new FileUtils();
        fileUtils.writeContentToFile("d:/simbest.pem","aaa");
    }
}
