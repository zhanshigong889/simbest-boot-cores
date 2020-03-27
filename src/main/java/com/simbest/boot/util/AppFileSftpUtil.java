/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.util;

import cn.hutool.extra.ftp.Ftp;
import cn.hutool.extra.ssh.JschUtil;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.simbest.boot.base.enums.StoreLocation;
import com.simbest.boot.base.exception.Exceptions;
import com.simbest.boot.constants.ApplicationConstants;
import com.simbest.boot.exceptions.AppRuntimeException;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
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

import static com.simbest.boot.constants.ApplicationConstants.SLASH;
import static com.simbest.boot.constants.ApplicationConstants.ZERO;
import static org.apache.commons.io.Charsets.UTF_8;

/**
 * 用途：FTP和SFTP文件操作工具类
 * 作者: lishuyi
 * 时间: 2019/8/12  21:59
 */
@Slf4j
@Component
//@ConditionalOnPropertyNotEmpty("app.file.sftp.username")
//@ConditionalOnProperty(name = "app.file.upload.location", havingValue = "sftp")
@ConditionalOnExpression("'${app.file.upload.location}'=='ftp' || '${app.file.upload.location}'=='sftp' || '${custom.upload.flag:false}'")
public class AppFileSftpUtil {

    @Setter
    public StoreLocation serverUploadLocation;

    @Value("${app.file.upload.location}")
    private String uploadLocation;

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

    @PostConstruct
    public void init() {
        serverUploadLocation = Enum.valueOf(StoreLocation.class, uploadLocation);
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
     * 将输入流的数据上传到sftp作为文件
     *
     * @param directory    上传到该目录
     * @param fileName     sftp端文件名
     * @param uploadFile   文件
     * @throws Exception
     */
    public String upload(String directory, String fileName, File uploadFile) throws Exception {
        return upload(directory, fileName, new FileInputStream(uploadFile));
    }

    /**
     * 将byte[]上传到sftp，作为文件。注意:从String生成byte[]是，要指定字符集。
     *
     * @param directory    上传到sftp目录
     * @param fileName     文件在sftp端的命名
     * @param byteArr      要上传的字节数组
     */
    public String upload(String directory, String fileName, byte[] byteArr) {
        return upload(directory, fileName, new ByteArrayInputStream(byteArr));
    }

    /**
     * 将输入流的数据上传到sftp作为文件
     *
     * @param directory    上传到该目录
     * @param fileName  sftp端文件名
     * @param input        输入流
     * @throws Exception
     */
    public String upload(String directory, String fileName, InputStream inputStream){
        String fullFilePath = null;
        Assert.notNull(directory, "路径不能为空");
        Assert.notNull(fileName, "文件名不能为空");
        Assert.notNull(inputStream, "文件流不能为空");
        directory = AppFileUtil.replaceSlash(directory);
        switch (serverUploadLocation) {
            case ftp:
                fullFilePath = ftpUpload(directory, fileName, inputStream);
                break;
            case sftp:
                fullFilePath = sftpUpload(directory, fileName, inputStream);
                break;
        }
        return fullFilePath;
    }

    private String ftpUpload(String directory, String fileName, InputStream inputStream) {
        try {
            log.debug("FTP即将在目录【{}】上传文件【{}】", directory, fileName);
            Ftp ftp = new Ftp(host, port, username, password, UTF_8 );
            boolean ret = ftp.upload(directory, fileName, inputStream);
            if(!ret){
                throw new AppRuntimeException(String.format("FTP在路径【%s】上传文件【%s】发生异常返回【%s】", directory, fileName, ret));
            }
            inputStream.close();
            ftp.close();
            log.debug("FTP在路径【{}】上传文件【{}】成功", directory, fileName);
            String fullFilePath = directory+SLASH+fileName;
            return fullFilePath;
        } catch (Exception e){
            Exceptions.printException(e);
            throw new AppRuntimeException(String.format("FTP在路径【%s】上传文件【%s】发生异常【%s】", directory, fileName, e.getMessage()));
        }
    }

    private String sftpUpload(String directory, String fileName, InputStream input) {
        try{
            try {
                sftpConnect();
                sftpChannel.cd(directory);
            }catch (Exception e){
                String[] folders = StringUtils.removeFirst(directory, SLASH).split( SLASH );
                for ( String folder : folders ) {
                    if ( folder.length() > 0 ) {
                        try {
                            sftpChannel.cd( folder );
                        }
                        catch ( SftpException se ) {
                            sftpChannel.mkdir( folder );
                            sftpChannel.cd( folder );
                        }
                    }
                }
            }
            directory = sftpChannel.pwd();
            log.debug("SFTP即将在目录【{}】上传文件【{}】", directory, fileName);
            //上传文件
            sftpChannel.put(input, fileName);
            //检查文件是否上传成功
            String fullFilePath = directory+SLASH+fileName;
            sftpChannel.ls(fullFilePath);
            log.debug("SFTP在路径【{}】上传文件【{}】成功", directory, fileName);
            return fullFilePath;
        } catch (Exception e){
            Exceptions.printException(e);
            throw new AppRuntimeException(String.format("SFTP在路径【%s】上传文件【%s】发生异常【%s】", directory, fileName, e.getMessage()));
        }
        finally {
            sftpDisconnect();
        }
    }

    /**
     * 下载文件
     *
     * @param directory        下载目录
     * @param fileName         下载的文件
     * @param saveTempFile     存在本地的路径
     */
    public File download2File(String directory, String fileName, File saveTempFile) {
        try {
            FileUtils.writeByteArrayToFile(saveTempFile, download2Byte(directory, fileName));
        } catch (IOException e) {
            Exceptions.printException(e);
            throw new AppRuntimeException(String.format("在路径【%s】下载文件【%s】发生异常【%s】", directory, fileName, e.getMessage()));
        }
        return saveTempFile;
    }

    /**
     * 下载文件
     *
     * @param directory    下载目录
     * @param downloadFile 下载的文件
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
        try {
            ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
            Ftp ftp = new Ftp(host, port, username, password, UTF_8 );
            ftp.download(directory, fileName, outSteam);
            byte[] fileData = outSteam.toByteArray();
            ftp.close();
            if(null == fileData || fileData.length == ZERO){
                throw new AppRuntimeException(String.format("FTP在路径【%s】下载文件【%s】失败", directory, fileName));
            }
            log.debug("FTP在路径【{}】下载文件【{}】成功", directory, fileName);
            return fileData;
        } catch (Exception e){
            Exceptions.printException(e);
            throw new AppRuntimeException(String.format("FTP在路径【%s】下载文件【%s】发生异常【%s】", directory, fileName, e.getMessage()));
        }
    }

    private byte[] sftpDownload2Byte(String directory, String fileName) {
        sftpConnect();
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        InputStream is = null;
        try {
            synchronized (this) {
                sftpChannel.cd(directory);
                sftpChannel.get(fileName, outSteam);
                byte[] fileData = outSteam.toByteArray();
                if(null == fileData || fileData.length == ZERO){
                    throw new AppRuntimeException(String.format("SFTP在路径【%s】下载文件【%s】失败", directory, fileName));
                }
                log.debug("SFTP在路径【{}】下载文件【{}】成功", directory, fileName);
                return fileData;
            }
        } catch (Exception e) {
            Exceptions.printException(e);
            throw new AppRuntimeException(String.format("SFTP在路径【%s】下载文件【%s】发生异常【%s】", directory, fileName, e.getMessage()));
        } finally {
            sftpDisconnect();
        }
    }

    /**
     * 删除文件，传递文件绝对路径地址
     * @param fileFullPath
     */
    public void deleteFile(String fileFullPath){
        Assert.notNull(fileFullPath, "文件路径不能为空");
        String directory = StringUtils.substringBeforeLast(fileFullPath, ApplicationConstants.SLASH);
        String fileName = StringUtils.substringAfterLast(fileFullPath, ApplicationConstants.SLASH);
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
        try {
            Ftp ftp = new Ftp(host, port, username, password, UTF_8 );
            boolean ret = ftp.delFile(directory + ApplicationConstants.SLASH + fileName);
            if(!ret){
                throw new AppRuntimeException(String.format("FTP在路径【%s】删除文件【%s】发生异常返回【%s】", directory, fileName, ret));
            }
            ftp.close();
        } catch (Exception e){
            Exceptions.printException(e);
            throw new AppRuntimeException(String.format("FTP在路径【%s】删除文件【%s】发生异常【%s】", directory, fileName, e.getMessage()));
        }
    }

    private void sftpDeleteFile(String directory, String fileName){
        try {
            sftpConnect();
            log.debug("即将在路径【{}】删除文件【{}】", directory, fileName);
            sftpChannel.rm(directory+SLASH+fileName);
            sftpDisconnect();
            log.debug("成功在路径【{}】删除文件【{}】", directory, fileName);
        }
        catch (SftpException e) {
            log.error("在路径【{}】删除文件【{}】失败", directory, fileName);
            Exceptions.printException(e);
            throw new AppRuntimeException(String.format("SFTP在路径【%s】删除文件【{}】发生异常【%s】", directory, fileName, e.getMessage()));
        }
    }





    private ChannelSftp sftpChannel;
    private Session sftpSession;

    /**
     * 打开连接SFTP server
     */
    private void sftpConnect(){
        log.debug("即将通过用户名【{}】、密码【{}】、私钥文件【{}】、私钥密码【{}】连接SFTP服务器主机【{}】端口【{}】", username,password,keyFilePath,passphrase,host,port);
        try {
            if (StringUtils.isNotEmpty(keyFilePath)) {
                log.debug("即将尝试通过【{}】读取私钥", keyFilePath);
                JSch jsch = new JSch();
                jsch.addIdentity(null, BootAppFileReader.getClasspathFileToString(keyFilePath).getBytes(), null, passphrase.getBytes());
                sftpSession = JschUtil.createSession(jsch, host, port, username);
            } else {
                sftpSession = JschUtil.createSession(host, port, username, password);
            }
            sftpChannel = JschUtil.openSftp(sftpSession);
        }
        catch (Exception e){
            log.error("打开SFTP连接发生异常【{}】", e.getMessage());
            Exceptions.printException(e);
        }
    }


    /**
     * 关闭连接SFTP server
     */
    private void sftpDisconnect() {
        try {
            JschUtil.close(sftpChannel);
            JschUtil.close(sftpSession);
        }
        catch (Exception e){
            log.error("关闭SFTP连接发生异常【{}】", e.getMessage());
            Exceptions.printException(e);
        }
    }

}
