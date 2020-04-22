/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.util;

import com.simbest.boot.base.exception.Exceptions;
import com.simbest.boot.constants.ApplicationConstants;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.csource.common.MyException;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.FileInfo;
import org.csource.fastdfs.ProtoCommon;
import org.csource.fastdfs.ServerInfo;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.StorageServer;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 用途：分布式FastDfs https://github.com/happyfish100/fastdfs
 * 作者: lishuyi
 * 时间: 2018/8/2  20:43
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.file.upload.location", havingValue = "fastdfs")
public class FastDfsClient {
    private static TrackerClient tracker = null;
//    @Getter
//    private static TrackerServer trackerServer = null;
//    private static StorageClient storageClient = null;

    @PostConstruct
    public void init() {
        try {
            BufferedReader bufferedReader = BootAppFileReader.getClasspathFile("fastdfs-client.properties");
            Properties props = new Properties();
            props.load(bufferedReader);
            log.info("FastDFS配置信息为【{}】", props.getProperty("fastdfs.tracker_servers"));
            ClientGlobal.initByProperties(props);
            if (tracker == null) {
                tracker = new TrackerClient();
            }
//            if (trackerServer == null) {
//                trackerServer = tracker.getConnection();
//                //给fastdfs发送一个消息,解决fastdfs第一次上传文件报错recv package size -1!=10。参考https://blog.qianxunclub.com/exception/fastdfs-recv-package-size/
//                ProtoCommon.activeTest(trackerServer.getSocket());
//            }
//            storageClient = new StorageClient(trackerServer, null);
            log.info("FastDFS StorageClient 客户端初始化成功!");
        } catch (Exception ex) {
            log.error("FastDFS StorageClient 客户端初始化失败!");
            Exceptions.printException(ex);
        }
    }

//    @PreDestroy
//    public void shutdown(){
//        if(null != trackerServer){
//            try {
//                trackerServer.close();
//                log.info("FASTDFS TrackerServer 关闭成功!");
//            } catch (IOException e) {
//                log.error("FASTDFS TrackerServer 关闭失败!");
//                Exceptions.printException(e);
//            }
//        }
//    }

    public static String uploadFile(byte[] fileContent) throws Exception {
        return uploadFile(fileContent, null, String.valueOf(fileContent.length));
    }

    public static String uploadFile(byte[] fileContent, String fileName, String extName) throws Exception {
        String fileLength = String.valueOf(fileContent.length);
        log.debug("FastDfsClient upload fileName: {} extName: {} with length: {}", fileName, extName, fileLength);
        NameValuePair[] metas = new NameValuePair[3];
        metas[0] = new NameValuePair("fileName", fileName);
        metas[1] = new NameValuePair("extName", extName);
        metas[2] = new NameValuePair("fileSize", fileLength);
        String result = uploadFile(fileContent, extName, metas);
        log.debug("FastDfsClient 上传结果为【{}】", result);
        return result;
    }

    /**
     * 上传文件方法
     * @param fileContent 文件字节流
     * @param extName 文件后缀
     * @param metas 文件元数据
     * @return String
     * @throws Exception
     */
    public static String uploadFile(byte[] fileContent, String extName, NameValuePair[] metas) throws Exception {
        log.debug("FastDfsClient upload file with extName: {} and metadata", extName);
        for(NameValuePair kv : metas){
            log.debug("metadata name: {} value: {}", kv.getName(), kv.getValue());
        }
        Assert.notNull(getStorageClient(), "请检查FasfDFS配置和服务运行状态");
        String[] result = getStorageClient().upload_file(fileContent, extName, metas);
        String group = result[0];
        String filePath = result[1];
        return group + ApplicationConstants.SLASH + filePath;
    }

    /**
     * 删除文件
     * @param storagePath
     * @return Integer
     * @throws IOException
     * @throws MyException
     */
    public static Integer deleteFile(String storagePath) throws IOException, MyException {
        String group = StringUtils.substringBefore(storagePath, ApplicationConstants.SLASH);
        String filename = StringUtils.substringAfter(storagePath, ApplicationConstants.SLASH);
        log.warn("Want remove file at group {}, filepath {}", group, filename);
        Assert.notNull(getStorageClient(), "请检查FasfDFS配置和服务运行状态");
        return getStorageClient().delete_file(group, filename);
    }

    /**
     * 上传本地文件方法
     * <p>Title: uploadFile</p>
     * <p>Description: </p>
     * @param local_fileName 文件全路径
     * @param extName 文件扩展名，不包含（.）
     * @param metas 文件扩展信息
     * @return String
     * @throws Exception
     */
    public static String uploadLocalFile(String local_fileName, String extName, NameValuePair[] metas) throws Exception {
        String[] result = getStorageClient().upload_file(local_fileName, extName, metas);
        String group = result[0];
        String filePath = result[1];
        return group + ApplicationConstants.SLASH + filePath;
    }

    public static String uploadLocalFile(String local_fileName) throws Exception {
        return uploadLocalFile(local_fileName, null, null);
    }

    public static String uploadLocalFile(String local_fileName, String extName) throws Exception {
        return uploadLocalFile(local_fileName, extName, null);
    }

    /**
     *
     * @param group group1
     * @param path M00/00/7F/ClcNWlt2PliAGyTyAAAClS5uADs678.txt
     * @return FileInfo
     */
    public static FileInfo getFileInfo(String group, String path){
        FileInfo fileInfo = null;
        try {
            Assert.notNull(getStorageClient(), "请检查FasfDFS配置和服务运行状态");
            fileInfo = getStorageClient().get_file_info(group, path);
        } catch (IOException e) {
            Exceptions.printException(e);
        } catch (MyException e) {
            Exceptions.printException(e);
        }
        return fileInfo;
    }

    /**
     * 获取文件上传时的元数据
     * @param group group1
     * @param path M00/00/7F/ClcNWlt2PliAGyTyAAAClS5uADs678.txt
     * @return NameValuePair[]
     */
    public static NameValuePair[] getMetadata(String group, String path){
        NameValuePair[] metadata = null;
        try {
            Assert.notNull(getStorageClient(), "请检查FasfDFS配置和服务运行状态");
            metadata = getStorageClient().get_metadata(group, path);
        } catch (IOException e) {
            Exceptions.printException(e);
        } catch (MyException e) {
            Exceptions.printException(e);
        }
        return metadata;
    }

    public static StorageServer[] getStoreStorages(String groupName) throws IOException {
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = trackerClient.getConnection();
        return trackerClient.getStoreStorages(trackerServer, groupName);
    }

    public static ServerInfo[] getFetchStorages(String groupName, String remoteFileName) throws IOException {
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = trackerClient.getConnection();
        return trackerClient.getFetchStorages(trackerServer, groupName, remoteFileName);
    }

    public static String getTrackerUrl() throws IOException {
        String url = "http://"+getTrackerServer().getInetSocketAddress().getHostString()+":"+ClientGlobal.getG_tracker_http_port()+"/";
        log.debug("TrackerServer 路径地址为【{}】", url);
        return url;
    }

    private static StorageClient getStorageClient() throws IOException {
        TrackerServer trackerServer = getTrackerServer();
        StorageClient storageClient = new StorageClient(trackerServer, null);
        return  storageClient;
    }

    public static TrackerServer getTrackerServer() throws IOException {
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = trackerClient.getConnection();
        ProtoCommon.activeTest(trackerServer.getSocket());
        return  trackerServer;
    }


    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        props.setProperty("fastdfs.connect_timeout_in_seconds", "5");
        props.setProperty("fastdfs.network_timeout_in_seconds", "30");
        props.setProperty("fastdfs.charset", "UTF-8");
        props.setProperty("fastdfs.http_anti_steal_token", "false");
        props.setProperty("fastdfs.http_secret_key", "FastDFS1234567890");
        props.setProperty("fastdfs.http_tracker_http_port", "80");
        props.setProperty("fastdfs.tracker_servers", "10.87.13.90:22122, 10.87.13.91:22122");
        ClientGlobal.initByProperties(props);
//        if (tracker == null) {
//            tracker = new TrackerClient();
//        }
//        if (trackerServer == null) {
//            trackerServer = tracker.getConnection();
//        }
//        storageClient = new StorageClient(trackerServer, null);
//        FastDfsClient.storageClient = storageClient;

        //test upload
        File initialFile = new File("C:\\Users\\kynel\\Desktop\\url.txt");
        InputStream targetStream = new FileInputStream(initialFile);
        String uploadResult = FastDfsClient.uploadFile(IOUtils.toByteArray(targetStream),
                AppFileUtil.getFileName(initialFile.getAbsolutePath()), AppFileUtil.getFileSuffix(initialFile.getCanonicalPath()));
        System.out.println(uploadResult);

        //test query
        String group = StringUtils.substringBefore(uploadResult, "/");
        String path = StringUtils.substringAfter(uploadResult, "/");
        FileInfo fileInfo = FastDfsClient.getFileInfo(group, path);
        System.out.println(fileInfo);
        NameValuePair[] metadata =  FastDfsClient.getStorageClient().get_metadata(group, path);
        for(NameValuePair nv : metadata){
            System.out.println(nv.getName()+"----"+nv.getValue());
        }

        //test delete
        int ret = getStorageClient().delete_file(group, path);
        System.out.println(ret);

        System.out.println(getTrackerUrl());
    }



}
