/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.util;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.simbest.boot.base.annotations.ConditionalOnPropertyNotEmpty;
import com.simbest.boot.base.exception.Exceptions;
import com.simbest.boot.config.AppConfig;
import com.simbest.boot.constants.ApplicationConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * 用途：
 * 作者: lishuyi
 * 时间: 2019/8/12  21:59
 */
@Slf4j
@Component
@ConditionalOnPropertyNotEmpty("app.file.sftp.username")
public class AppFileSftpUtil {

    @Autowired
    private AppConfig config;

    @Value("${app.file.sftp.username}")
    private String username;

    @Value("${app.file.sftp.password}")
    private String password;

    @Value("${app.file.sftp.host}")
    private String host;

    @Value("${app.file.sftp.port}")
    private int port;

    @Value("${app.file.sftp.keyFilePath}")
    private String keyFilePath;

    @Value("${app.file.sftp.passphrase}")
    private String passphrase;

    private ChannelSftp sftp;
    private Session sshSession;

    @PostConstruct
    public void init() {
        log.info("Congratulations------------------------------------------------SFTP配置加载完成");
        log.info("用户名【{}】", username);
        log.info("密码【{}】", password);
        log.info("主机【{}】", host);
        log.info("端口【{}】", port);
        log.info("私钥文件【{}】", keyFilePath);
        log.info("私钥密码【{}】", passphrase);
    }

    /**
     * 连接sftp服务器
     * 如果connect过程出现：Kerberos username [xxx]   Kerberos password
     * 解决办法：移步https://blog.csdn.net/a718515028/article/details/80356337
     */
    public void connect() {
        try {
            JSch jsch = new JSch();
            log.debug("即将通过用户名【{}】、密码【{}】、私钥文件【{}】、私钥密码【{}】连接SFTP服务器主机【{}】端口【{}】", username,password,keyFilePath,passphrase,host,port);
            if (StringUtils.isNotEmpty(keyFilePath)) {
                log.debug("即将尝试通过【{}】读取私钥", keyFilePath);
                if (StringUtils.isNotEmpty(passphrase)) {
                    // 设置私钥
                    jsch.addIdentity(null, BootAppFileReader.getClasspathFileToString(keyFilePath).getBytes(), null, passphrase.getBytes());
                } else {
                    jsch.addIdentity(null, BootAppFileReader.getClasspathFileToString(keyFilePath).getBytes(), null, null);
                }
            }
            log.debug("SFTP 主机: " + host + "; 用户名:" + username);
            sshSession = jsch.getSession(username, host, port);
            log.debug("Congratulations-----------------------------------------------SFTP连接通道已建立");
            if (StringUtils.isNotEmpty(password)) {
                sshSession.setPassword(password);
            }
            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", "no");
            sshSession.setConfig(sshConfig);
            sshSession.setConfig("kex", "diffie-hellman-group1-sha1");
            sshSession.connect();
            Channel channel = sshSession.openChannel("sftp");
            channel.connect();
            sftp = (ChannelSftp) channel;
            log.debug("连接到SFTP成功.Host: " + host);
        } catch (Exception e) {
            log.error("连接SFTP失败：" + e.getMessage());
            Exceptions.printException(e);
        }
    }

    /**
     * 关闭连接 server
     */
    public void disconnect() {
        if (sftp != null) {
            if (sftp.isConnected()) {
                sftp.disconnect();
                sshSession.disconnect();
                log.debug("Congratulations-----------------------------------------------SFTP连接通道已关闭");
            } else if (sftp.isClosed()) {
                log.debug("Congratulations-----------------------------------------------SFTP连接通道已关闭，不用重复操作");
            }
        }
    }

    /**
     * 将输入流的数据上传到sftp作为文件
     *
     * @param directory    上传到该目录
     * @param sftpFileName sftp端文件名
     * @param input        输入流
     * @throws Exception
     */
    public void upload(String directory, String sftpFileName, InputStream input) throws Exception {
        try {
            connect();
            try {// 如果cd报异常，说明目录不存在，就创建目录
                sftp.cd(config.getUploadPath());
                sftp.cd(directory);
            }
            catch (Exception e) {
                String[] folders = StringUtils.removeFirst(directory,ApplicationConstants.SLASH ).split( ApplicationConstants.SLASH );
                for ( String folder : folders ) {
                    if ( folder.length() > 0 ) {
                        try {
                            sftp.cd( folder );
                        }
                        catch ( SftpException se ) {
                            sftp.mkdir( folder );
                            sftp.cd( folder );
                        }
                    }
                }
            }
            sftp.put(input, sftpFileName);
            log.debug("SFTP上传成功！文件名：" + sftpFileName);
        } catch (Exception e) {
            log.error("SFTP上传失败！文件名：" + sftpFileName);
            Exceptions.printException(e);
            throw e;
        }
        finally {
            disconnect();
        }
    }

    /**
     * 上传单个文件
     *
     * @param directory  上传到sftp目录
     * @param uploadFile 要上传的文件,包括路径
     * @throws Exception
     */
    public void upload(String directory, String sftpFileName, File uploadFile) throws Exception {
        FileInputStream in = null;
        try {
            in = new FileInputStream(uploadFile);
            upload(directory, sftpFileName, in);
        } catch (Exception ex) {
            if (in != null) {
                in.close();
            }
        }
    }

    /**
     * 将byte[]上传到sftp，作为文件。注意:从String生成byte[]是，要指定字符集。
     *
     * @param directory    上传到sftp目录
     * @param sftpFileName 文件在sftp端的命名
     * @param byteArr      要上传的字节数组
     * @throws Exception
     */
    public void upload(String directory, String sftpFileName, byte[] byteArr) throws Exception {
        upload(directory, sftpFileName, new ByteArrayInputStream(byteArr));
    }

    /**
     * 下载文件
     *
     * @param directory    下载目录
     * @param downloadFile 下载的文件
     * @throws Exception
     */
    public byte[] download2Byte(String directory, String downloadFile) {
        connect();
        byte[] fileData = null;
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        InputStream is = null;
        try {
            synchronized (this) {
                sftp.cd(directory);
                sftp.get(downloadFile, outSteam);
                fileData = outSteam.toByteArray();
            }
            log.debug("SFTP下载文件成功！文件名：" + downloadFile);
            return fileData;
        } catch (Exception e) {
            log.debug("SFTP下载文件失败！文件名：" + downloadFile);
            Exceptions.printException(e);
            return null;
        } finally {
            disconnect();
        }
    }

    /**
     * 下载文件
     *
     * @param directory    下载目录
     * @param downloadFile 下载的文件
     * @param saveFile     存在本地的路径
     * @throws Exception
     */
    public File download2File(String directory, String downloadFile, File saveFile) {
        try {
            FileUtils.writeByteArrayToFile(saveFile, download2Byte(directory, downloadFile));
        } catch (IOException e) {
            Exceptions.printException(e);
            return null;
        }
        return saveFile;
    }

    /**
     * 删除文件
     * @param filePathAndName
     * @return
     */
    public boolean delFile(String filePathAndName){
        Assert.notNull(filePathAndName, "文件不能为空");
        try {
            sftp.rm(filePathAndName);
            return true;
        }
        catch (SftpException e) {
            return false;
        }
    }
}
