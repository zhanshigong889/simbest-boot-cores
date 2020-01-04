/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.util;

import cn.hutool.core.io.FileUtil;
import com.google.common.base.Joiner;
import com.simbest.boot.base.exception.Exceptions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ResourceUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用途：读取Boot项目Classpath路径下的文件
 * 作者: lishuyi
 * 时间: 2018/8/2  21:13
 */
@Slf4j
public class BootAppFileReader {

    /**
     * 读取文件并转换为BufferedReader
     * @param filepath
     * @return BufferedReader
     */
    public static BufferedReader getClasspathFile(String filepath){
        filepath = ResourceUtils.CLASSPATH_URL_PREFIX + filepath;
        BufferedReader bufferedReader = null;
        try {
            ClassPathResource resource = new ClassPathResource(filepath);
            InputStream inputStream = resource.getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        } catch (Exception e){
            try {
                bufferedReader = new BufferedReader(new FileReader(ResourceUtils.getFile(filepath)));
            } catch (FileNotFoundException e1) {
            }
        }
        if(bufferedReader == null){
            log.error("严重错误：请注意读取配置文件【{}】失败！", filepath);
        }
        return bufferedReader;
    }

    /**
     * 读取文件并转换为字符串
     * @param filepath
     * @return String
     */
    public static String getClasspathFileToString(String filepath){
        BufferedReader bufferedReader = getClasspathFile(filepath);
        List<String> lines = bufferedReader.lines().collect(Collectors.toList());
        String content = Joiner.on("\n").join(lines);
        return content;
    }

    /**
     * 读取文件
     * @param filepath
     * @return File
     */
    public static File getClasspathFileToFile(String filepath){
        try {
            File targetFile = ResourceUtils.getFile(filepath);
            return targetFile;
        } catch (IOException e) {
            ClassPathResource resource = new ClassPathResource(filepath);
            try {
                InputStream inputStream = resource.getInputStream();
                File dir = new File(System.getProperty("user.home"));
                File tmpFile = FileUtil.createTempFile(dir);
                FileUtils.copyInputStreamToFile(inputStream, tmpFile);
                return tmpFile;
            } catch (IOException e1) {
                Exceptions.printException(e1);
            }
        }
        return null;
    }
}
