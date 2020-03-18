/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.util;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.ftp.Ftp;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.simbest.boot.base.enums.StoreLocation;
import com.simbest.boot.base.exception.Exceptions;
import com.simbest.boot.config.AppConfig;
import com.simbest.boot.constants.ApplicationConstants;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import static com.simbest.boot.constants.ApplicationConstants.SLASH;

/**
 * 用途：
 * 作者: lishuyi
 * 时间: 2019/8/12  21:59
 */
@Slf4j
@Component
//@ConditionalOnPropertyNotEmpty("app.file.sftp.username")
//@ConditionalOnProperty(name = "app.file.upload.location", havingValue = "sftp")
@ConditionalOnExpression("'${app.file.upload.location}'=='ftp' || '${app.file.upload.location}'=='sftp' || '${custom.upload.flag:false}'")
public class AppFileSftpUtil {

    /** 本地字符编码 */
    private static String LOCAL_CHARSET = "GBK";

    // FTP协议里面，规定文件名编码为iso-8859-1
    private static String SERVER_CHARSET = "ISO-8859-1";

    @Autowired
    private AppConfig config;

    @Setter
    public StoreLocation serverUploadLocation;

    @Setter
    @Value("${app.file.sftp.username}")
    private String username;

    @Setter
    @Value("${app.file.sftp.password}")
    private String password;

    @Setter
    @Value("${app.file.sftp.host}")
    private String host;

    @Setter
    @Value("${app.file.sftp.port}")
    private int port;

    @Setter
    @Value("${app.file.sftp.keyFilePath}")
    private String keyFilePath;

    @Setter
    @Value("${app.file.sftp.passphrase}")
    private String passphrase;

    private ChannelSftp sftp1;
    private Session sshSession;

    @PostConstruct
    public void init() {
        serverUploadLocation = Enum.valueOf(StoreLocation.class, config.getUploadLocation());
        log.info("上传方式【{}】", serverUploadLocation);
        log.info("用户名【{}】", username);
        log.info("密码【{}】", password);
        log.info("主机【{}】", host);
        log.info("端口【{}】", port);
        log.info("私钥文件【{}】", keyFilePath);
        log.info("私钥密码【{}】", passphrase);
        log.info("Congratulations------------------------------------------------SFTP配置加载完成");
    }

    /**
     * 连接sftp服务器
     * 如果connect过程出现：Kerberos username [xxx]   Kerberos password
     * 解决办法：移步https://blog.csdn.net/a718515028/article/details/80356337
     */
    private void connect() {
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
            sftp1 = (ChannelSftp) channel;
            //设置编码 https://blog.csdn.net/liuhenghui5201/article/details/50970492
            Class cl = ChannelSftp.class;
            Field f1 = cl.getDeclaredField("server_version");
            f1.setAccessible(true);
            f1.set(sftp1, 2);
            sftp1.setFilenameEncoding(ApplicationConstants.UTF_8);
            log.debug("连接到SFTP成功.Host: " + host);
        } catch (Exception e) {
            log.error("连接SFTP失败：" + e.getMessage());
            Exceptions.printException(e);
        }
    }

    /**
     * 关闭连接 server
     */
    private void disconnect() {
        if (sftp1 != null) {
            if (sftp1.isConnected()) {
                sftp1.disconnect();
                sshSession.disconnect();
                log.debug("Congratulations-----------------------------------------------SFTP连接通道已关闭");
            } else if (sftp1.isClosed()) {
                log.debug("Congratulations-----------------------------------------------SFTP连接通道已关闭，不用重复操作");
            }
        }
    }

    /**
     * 将输入流的数据上传到sftp作为文件
     *
     * @param directory    上传到该目录
     * @param fileName  sftp端文件名
     * @param input        输入流
     * @throws Exception
     */
    public void upload(String directory, String fileName, InputStream input) throws Exception {
        Assert.notNull(directory, "路径不能为空");
        Assert.notNull(fileName, "文件名不能为空");
        Assert.notNull(input, "文件流不能为空");
        switch (serverUploadLocation) {
            case ftp:
                ftpUpload(directory, fileName, input);
                break;
            case sftp:
                sftpUpload(directory, fileName, input);
                break;
        }
    }

    private void ftpUpload(String directory, String filename, InputStream input) throws Exception {
        if ( !BooleanUtil.toBoolean( config.getCustomUploadFlag()) ){
            directory = config.getUploadPath()+directory;
        }
        directory = StringUtils.replace(directory, "\\", SLASH);
        FTPClient ftp = new FTPClient();
        try {
            int reply;
            ftp.connect(host, port);// 连接FTP服务器
            // 如果采用默认端口，可以使用ftp.connect(host)的方式直接连接FTP服务器
            ftp.login(username, password);// 登录
            reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                log.error("FTP上传失败！文件名：" + filename);
                throw new RuntimeException("FTP服务器无法连通");
            }
            // 开启服务器对UTF-8的支持，如果服务器支持就用UTF-8编码，否则就使用本地编码（GBK）.
          /*  if (FTPReply.isPositiveCompletion(ftp.sendCommand("OPTS UTF8", "ON"))) {
                LOCAL_CHARSET = "UTF-8";
            }
            //设置中文的文件名称
            ftp.setControlEncoding(LOCAL_CHARSET);*/
            /*FTPClientConfig conf = new FTPClientConfig(FTPClientConfig.SYST_UNIX);
            conf.setServerLanguageCode("zh");
            ftp.configure(conf);*/
            //切换到上传目录
            if (!ftp.changeWorkingDirectory(directory)) {
                //如果目录不存在创建目录
                String[] dirs = directory.split("/");
                String tempPath = "";
                for (String dir : dirs) {
                    if (null == dir || "".equals(dir)) continue;
                    tempPath += "/" + dir;
                    if (!ftp.changeWorkingDirectory(tempPath)) {  //进不去目录，说明该目录不存在
                        if (!ftp.makeDirectory(tempPath)) { //创建目录
                            //如果创建文件目录失败，则返回
                            log.error("FTP上传失败！文件名：" + filename);
                            throw new RuntimeException("创建文件目录" + tempPath + "失败");
                        } else {
                            //目录存在，则直接进入该目录
                            ftp.changeWorkingDirectory(tempPath);
                        }
                    }
                }
            }
            //设置上传文件的类型为二进制类型
            ftp.setFileType(ftp.BINARY_FILE_TYPE);
            //ftp.setFileTransferMode(ftp.STREAM_TRANSFER_MODE);
            //上传文件
            if (!ftp.storeFile(new String(filename.getBytes(LOCAL_CHARSET), StandardCharsets.ISO_8859_1), input)) {
                log.error("FTP上传失败！文件名：" + filename);
                throw new RuntimeException("上传文件失败");
            }
            input.close();
            ftp.logout();
        } catch (IOException e) {
            Exceptions.printException(e);
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException ioe) {
                    Exceptions.printException(ioe);
                }
            }
        }
    }

    private void sftpUpload(String directory, String sftpFileName, InputStream input) throws Exception {
        directory = StringUtils.replace(directory, "\\", SLASH);
        try {
            connect();
            try {// 如果cd报异常，说明目录不存在，就创建目录
                if ( !BooleanUtil.toBoolean( config.getCustomUploadFlag()) ){
                    sftp1.cd(config.getUploadPath());
                }
                sftp1.cd(directory);
            }catch (Exception e) {
                String[] folders = StringUtils.removeFirst(directory, SLASH ).split( SLASH );
                for ( String folder : folders ) {
                    if ( folder.length() > 0 ) {
                        try {
                            sftp1.cd( folder );
                        }
                        catch ( SftpException se ) {
                            sftp1.mkdir( folder );
                            sftp1.cd( folder );
                        }
                    }
                }
            }
            sftp1.put(input, sftpFileName);
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
     * 将输入流的数据上传到sftp作为文件
     *
     * @param directory    上传到该目录
     * @param fileName     sftp端文件名
     * @param uploadFile   文件
     * @throws Exception
     */
    public void upload(String directory, String fileName, File uploadFile) throws Exception {
        FileInputStream in = null;
        try {
            in = new FileInputStream(uploadFile);
            upload(directory, fileName, in);
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
     * @param fileName 文件在sftp端的命名
     * @param byteArr      要上传的字节数组
     * @throws Exception
     */
    public void upload(String directory, String fileName, byte[] byteArr) throws Exception {
        String fileContent = StrUtil.str(byteArr,StandardCharsets.UTF_8);
        //byteArr = StrUtil.bytes( fileContent,StandardCharsets.UTF_8 );
        //log.warn( "转换后>>>>>>ftp文件上传时，输出上传文件byte流【{}】", fileContent);
        upload(directory, fileName, new ByteArrayInputStream(byteArr));
    }


    /**
     * 下载文件
     *
     * @param directory    下载目录
     * @param downloadFile 下载的文件
     * @throws Exception
     */
    public byte[] download2Byte(String directory, String fileName) {
        Assert.notNull(directory, "路径不能为空");
        Assert.notNull(fileName, "文件名不能为空");
        byte[] fileData = null;
        switch (serverUploadLocation) {
            case ftp:
                fileData = ftpDownload2Byte(directory, fileName);
                break;
            case sftp:
                fileData = sftpDownload2Byte(directory, fileName);
                break;
        }
        return fileData;
    }

    private byte[] ftpDownload2Byte(String directory, String fileName) {
        byte[] fileData = null;
        FTPClient ftp = new FTPClient();
        try {
            int reply;
            ftp.connect(host, port);
            // 如果采用默认端口，可以使用ftp.connect(host)的方式直接连接FTP服务器
            ftp.login(username, password);// 登录
            reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                log.error("FTP上传失败！文件名：" + fileName);
                throw new RuntimeException("FTP服务器无法连通");
            }
            // 转移到FTP服务器目录
            ftp.changeWorkingDirectory(directory);
            log.debug("当前FTP工作路径【{}】", ftp.printWorkingDirectory());
            ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
            ftp.retrieveFile(fileName, outSteam);
            fileData = outSteam.toByteArray();
//            FTPFile[] fs = ftp.listFiles();
//            for (FTPFile ff : fs) {
//                if (ff.getName().equals(fileName)) {
//                    ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
//                    ftp.retrieveFile(ff.getName(), outSteam);
//                    fileData = outSteam.toByteArray();
//                }
//            }
            ftp.logout();
        } catch (IOException e) {
            Exceptions.printException(e);
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException ioe) {
                    Exceptions.printException(ioe);
                }
            }
        }
        Assert.notNull(fileData, "下载文件不存在");
        return fileData;
    }

    private byte[] sftpDownload2Byte(String directory, String fileName) {
        connect();
        byte[] fileData = null;
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        InputStream is = null;
        try {
            synchronized (this) {
                sftp1.cd(directory);
                sftp1.get(fileName, outSteam);
                fileData = outSteam.toByteArray();
            }
            log.debug("SFTP下载文件成功！文件名：" + fileName);
        } catch (Exception e) {
            log.debug("SFTP下载文件失败！文件名：" + fileName);
            Exceptions.printException(e);
            return null;
        } finally {
            disconnect();
        }
        Assert.notNull(fileData, "下载文件不存在");
        return fileData;
    }

    /**
     * 下载文件
     *
     * @param directory    下载目录
     * @param fileName 下载的文件
     * @param saveTempFile     存在本地的路径
     * @throws Exception
     */
    public File download2File(String directory, String fileName, File saveTempFile) {
        try {
            FileUtils.writeByteArrayToFile(saveTempFile, download2Byte(directory, fileName));
        } catch (IOException e) {
            Exceptions.printException(e);
            return null;
        }
        return saveTempFile;
    }

    /**
     * 删除文件，传递文件绝对路径地址
     * @param fileFullPath
     */
    public void deleteFile(String fileFullPath){
        Assert.notNull(fileFullPath, "文件路径不能为空");
        String directory = StringUtils.substringBeforeLast(fileFullPath, ApplicationConstants.SEPARATOR);
        String fileName = StringUtils.substringAfterLast(fileFullPath, ApplicationConstants.SEPARATOR);
        switch (serverUploadLocation) {
            case ftp:
                ftpDeleteFile(directory, fileName);
                break;
            case sftp:
                sftpDeleteFile(directory, fileName);
                break;
        }
    }

    private void ftpDeleteFile(String directory, String fileName){
        FTPClient ftp = new FTPClient();
        try {
            int reply;
            ftp.connect(host, port);// 连接FTP服务器
            // 如果采用默认端口，可以使用ftp.connect(host)的方式直接连接FTP服务器
            ftp.login(username, password);// 登录
            reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                log.error("FTP上传失败！文件名：" + fileName);
                throw new RuntimeException("FTP服务器无法连通");
            }
            ftp.changeWorkingDirectory(directory);
            ftp.dele(fileName);
            ftp.logout();
        } catch (IOException e) {
            Exceptions.printException(e);
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException ioe) {
                    Exceptions.printException(ioe);
                }
            }
        }
    }

    private boolean sftpDeleteFile(String directory, String fileName){
        try {
            sftp1.rm(directory+fileName);
            return true;
        }
        catch (SftpException e) {
            return false;
        }
    }
}
