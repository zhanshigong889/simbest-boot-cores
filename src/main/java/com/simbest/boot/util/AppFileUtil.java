/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.util;


import cn.hutool.core.util.StrUtil;
import com.github.stuxuhai.jpinyin.ChineseHelper;
import com.google.common.collect.Lists;
import com.simbest.boot.base.enums.StoreLocation;
import com.simbest.boot.base.exception.Exceptions;
import com.simbest.boot.config.AppConfig;
import com.simbest.boot.constants.ApplicationConstants;
import com.simbest.boot.sys.model.SysFile;
import com.simbest.boot.util.json.JacksonUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.simbest.boot.constants.ApplicationConstants.SLASH;
import static com.simbest.boot.sys.web.SysFileController.DOWNLOAD_FULL_URL;
import static com.simbest.boot.sys.web.SysFileController.DOWNLOAD_FULL_URL_ANONYMOUS;
import static com.simbest.boot.sys.web.SysFileController.DOWNLOAD_FULL_URL_API;

/**
 * 用途：文件工具类
 * 作者: lishuyi
 * 时间: 2018/3/12  20:45
 */
@Slf4j
@Component
public class AppFileUtil {

    private static final String UPLOAD_FILE_PATTERN =
            "(jpg|jpeg|png|gif|bmp|doc|docx|xls|xlsx|pdf|ppt|pptx|txt|rar|zip|7z|mp4|ogg|swf|webm|html|htm|mov)$";
    private static Pattern pattern = Pattern.compile(UPLOAD_FILE_PATTERN);

    @Autowired
    private AppConfig config;

    @Autowired
    private SpringContextUtil springContextUtil;

    private AppFileSftpUtil sftpUtil;

    @Getter
    public StoreLocation serverUploadLocation;

    @PostConstruct
    public void init() {
        serverUploadLocation = Enum.valueOf(StoreLocation.class, config.getUploadLocation());
        log.info("应用上传文件将基于【{}】方式保存", serverUploadLocation.name());
        try {
            sftpUtil = springContextUtil.getBean(AppFileSftpUtil.class);
        }
        catch (NoSuchBeanDefinitionException e) {
        }
        finally {
            if(null == sftpUtil){
                log.info("请注意应用没有配置SFTP，请检查配置是否需要，如不需要，则忽略该条警告信息！");
            }
        }
    }

    /**
     * 判断是否允许上传
     *
     * @param fileName
     * @return boolean
     */
    public static boolean validateUploadFileType(String fileName) {
        Matcher matcher = pattern.matcher(getFileSuffix(fileName).toLowerCase());
        boolean ret = matcher.matches();
        log.debug("判断文件【{}】是否合法结果为【{}】", fileName, ret);
        return ret;
    }

    /**
     * 根据路径返回文件名，如：http://aaa/bbb.jpg C:/aaa/abc.jpg 返回abc.jpg
     *
     * @param pathToName
     * @return String
     */
    public static String getFileName(String pathToName) {
        Assert.notNull(pathToName, pathToName+"文件名称不能为空");
        String fileName = FilenameUtils.getName(pathToName);
        return StringUtils.isEmpty(fileName) || "0".equals(fileName) ? null : fileName;
    }

    /**
     * 根据路径返回文件名，如：http://aaa/bbb.jpg C:/aaa/abc.jpg 返回abc
     *
     * @param pathToName
     * @return String
     */
    public static String getFileBaseName(String pathToName) {
        Assert.notNull(pathToName, pathToName+"文件名称不能为空");
        String fileBaseName =  FilenameUtils.getBaseName(pathToName);
        return StringUtils.isEmpty(fileBaseName) || "0".equals(fileBaseName) ? null : fileBaseName;
    }

    /**
     * 根据路径返回文件后缀，如：http://aaa/bbb.jpg C:/aaa/abc.jpg 返回jpg
     *
     * @param pathToName
     * @return String
     */
    public static String getFileSuffix(String pathToName) {
        Assert.notNull(pathToName, pathToName+"文件名称不能为空");
        return FilenameUtils.getExtension(pathToName);
    }

    public static String getFileType(File file) {
        try {
            return Files.probeContentType(file.toPath());
        } catch (IOException e) {
            return null;
        }
    }

    public static boolean isImage(File file) {
        Assert.notNull(file, "图片文件不能为空");
        boolean ret = false;
        try {
            Image image = ImageIO.read(file);
            if (image != null) {
                ret = true;
            }
        } catch(Exception ex) {
        }
        log.debug("判断文件【{}】是否为图片结果为【{}】", file.getName(), ret);
        return ret;
    }

    /**
     * 上传单个文件  自定义文件名称
     * @param directory 相对路径
     * @param multipartFile
     * @return SysFile
     * @throws Exception
     */
    public SysFile uploadFileWithFileName(String directory, MultipartFile multipartFile,String fileName) throws Exception {
        Assert.notNull(multipartFile, "上传文件不能为空");
        return uploadFiles(directory, Arrays.asList(multipartFile),fileName).get(0);
    }

    /**
     * 上传单个文件
     * @param directory 相对路径
     * @param multipartFile
     * @return SysFile
     * @throws Exception
     */
    public SysFile uploadFile(String directory, MultipartFile multipartFile) throws Exception {
        Assert.notNull(multipartFile, "上传文件不能为空");
        return uploadFiles(directory, Arrays.asList(multipartFile),null).get(0);
    }

    /**
     * 上传多个文件
     * @param directory 相对路径
     * @param multipartFiles
     * @return List<SysFile>
     * @throws Exception
     */
    public List<SysFile> customUploadFiles(String directory, Collection<MultipartFile> multipartFiles,String fileName) throws Exception {
        Assert.notEmpty(multipartFiles, "上传文件不能为空");
        List<SysFile> fileModels = Lists.newArrayList();
        for (MultipartFile multipartFile : multipartFiles) {
            if (multipartFile.isEmpty()) {
                log.warn("上传文件流为空，继续循环处理上传文件");
                continue;
            } else if (multipartFile.getSize() == 0L){
                log.warn("上传文件流大小为空，继续循环处理上传文件");
                continue;
            }
            String filePath = null;
            String filename = StrUtil.isEmpty( fileName )?getFileName(multipartFile.getOriginalFilename()):fileName;
            //特殊字符过滤，防止XSS漏洞
            filename = JacksonUtils.escapeString(filename);
            if(validateUploadFileType(filename)) {
                log.debug("即将以【{}】方式上传【{}】文件", serverUploadLocation, filename);
                switch (serverUploadLocation) {
                    case disk:
                        File targetFileDirectory = createCustomUploadDirectory(directory);
                        byte[] bytes = multipartFile.getBytes();
                        String pathTmp = targetFileDirectory.getPath() + ApplicationConstants.SLASH + filename;
                        if ( StrUtil.endWithIgnoreCase( directory,ApplicationConstants.SLASH ) ){
                            pathTmp = targetFileDirectory.getPath() + filename;
                        }
                        Path path = Paths.get(pathTmp);
                        Files.write(path, bytes);
                        filePath = path.toString();
                        break;
                    case fastdfs:
                        filePath = FastDfsClient.uploadFile(IOUtils.toByteArray(multipartFile.getInputStream()), filename, getFileSuffix(filename));
                        break;
                    case ftp:
                        log.debug("基于ftp，方式同sftp");
                    case sftp:
                        sftpUtil.upload(directory, filename, multipartFile.getBytes());
                        filePath = directory + ApplicationConstants.SLASH + filename;
                        if ( StrUtil.endWithIgnoreCase( directory,ApplicationConstants.SLASH ) ){
                            filePath = directory + filename;
                        }
                        break;
                }
                Assert.notNull(filePath, String.format("文件以【%s】方式上传失败", serverUploadLocation));
                SysFile sysFile = SysFile.builder().fileName(filename).fileType(getFileSuffix(filename)).storeLocation(serverUploadLocation)
                        .filePath(StringUtils.replace(filePath, ApplicationConstants.SEPARATOR, ApplicationConstants.SLASH))
                        .fileSize(multipartFile.getSize()).downLoadUrl(DOWNLOAD_FULL_URL).apiFilePath(DOWNLOAD_FULL_URL_API)
                        .anonymousFilePath(DOWNLOAD_FULL_URL_ANONYMOUS).build();
                log.debug("【{}】上传成功，具体信息如下: 【{}】", filename, sysFile.toString());
                fileModels.add(sysFile);
            } else {
                log.warn("【{}】文件类型受限，不允许上传！", filename);
            }
        }
        return fileModels;
    }

    /**
     * 上传多个文件
     * @param directory 相对路径
     * @param multipartFiles
     * @return List<SysFile>
     * @throws Exception
     */
    public List<SysFile> uploadFiles(String directory, Collection<MultipartFile> multipartFiles,String fileName) throws Exception {
        Assert.notEmpty(multipartFiles, "上传文件不能为空");
        List<SysFile> fileModels = Lists.newArrayList();
        for (MultipartFile multipartFile : multipartFiles) {
            if (multipartFile.isEmpty()) {
                log.warn("上传文件流为空，继续循环处理上传文件");
                continue;
            } else if (multipartFile.getSize() == 0L){
                log.warn("上传文件流大小为空，继续循环处理上传文件");
                continue;
            }
            String filePath = null;
            String filename = StrUtil.isEmpty( fileName )?getFileName(multipartFile.getOriginalFilename()):fileName;
            //特殊字符过滤，防止XSS漏洞
            filename = JacksonUtils.escapeString(filename);
            if(validateUploadFileType(filename)) {
                log.debug("即将以【{}】方式上传【{}】文件", serverUploadLocation, filename);
                switch (serverUploadLocation) {
                    case disk:
                        File targetFileDirectory = createUploadDirectory(directory);
                        byte[] bytes = multipartFile.getBytes();
                        Path path = Paths.get(targetFileDirectory.getPath() + ApplicationConstants.SLASH + filename);
                        Files.write(path, bytes);
                        filePath = path.toString();

                        break;
                    case fastdfs:
                        filePath = FastDfsClient.uploadFile(IOUtils.toByteArray(multipartFile.getInputStream()),
                                filename, getFileSuffix(filename));
                        break;
                    case ftp:
                        log.debug("基于ftp，方式同sftp");
                    case sftp:
                        String directoryPath = createUploadDirectoryPath(directory);
                        sftpUtil.upload(directoryPath, filename, multipartFile.getBytes());
                        filePath = config.getUploadPath() + directoryPath + ApplicationConstants.SLASH + filename;
                        break;
                }
                Assert.notNull(filePath, String.format("文件以【%s】方式上传失败", serverUploadLocation));
                SysFile sysFile = SysFile.builder().fileName(filename).fileType(getFileSuffix(filename)).storeLocation(serverUploadLocation)
                        .filePath(StringUtils.replace(filePath, ApplicationConstants.SEPARATOR, ApplicationConstants.SLASH))
                        .fileSize(multipartFile.getSize()).downLoadUrl(DOWNLOAD_FULL_URL).apiFilePath(DOWNLOAD_FULL_URL_API)
                        .anonymousFilePath(DOWNLOAD_FULL_URL_ANONYMOUS).build();
                log.debug("【{}】上传成功，具体信息如下: 【{}】", filename, sysFile.toString());
                fileModels.add(sysFile);
            } else {
                log.warn("【{}】文件类型受限，不允许上传！", filename);
            }
        }
        return fileModels;
    }

    /**
     * 上传多个文件   重载上面的方法
     * @param directory 相对路径
     * @param multipartFiles
     * @return List<SysFile>
     * @throws Exception
     */
    public List<SysFile> uploadFiles(String directory, Collection<MultipartFile> multipartFiles) throws Exception {
        return uploadFiles(directory,multipartFiles,null);
    }

    /**
     * 上传多个文件   重载上面的方法
     * @param directory 相对路径
     * @param multipartFiles
     * @return List<SysFile>
     * @throws Exception
     */
    public List<SysFile> customUploadFiles(String directory, Collection<MultipartFile> multipartFiles) throws Exception {
        return customUploadFiles(directory,multipartFiles,null);
    }

    /**
     * 将文件在远程URL读取后，再上传
     * @param fileUrl   远程文件URL
     * @param directory 相对路径
     * @return SysFile
     */
    public SysFile uploadFromUrl(String fileUrl, String directory) throws Exception {
        String filePath = null;
        String fileName = null;
        Long fileSize = null;
        HttpURLConnection urlConnection = null;
        UrlFile urlFile = null;
        try {
            urlFile = openAndConnectUrl(fileUrl);
            urlConnection = urlFile.getConn();
            if(StringUtils.isEmpty(urlFile.getFileName())){
                //URL连接上面没有文件名
                fileName = CodeGenerator.systemUUID();
            }
            fileName = fileName + ApplicationConstants.DOT + urlFile.getFileSuffix();
            switch (serverUploadLocation) {
                case disk:
                    File targetFileDirectory = createUploadDirectory(directory);
                    File storeFile = new File(targetFileDirectory.getAbsolutePath() + ApplicationConstants.SLASH + fileName);
                    FileUtils.touch(storeFile); //覆盖文件
                    FileUtils.copyURLToFile(urlFile.getConnUrl(), storeFile);
                    filePath = storeFile.getAbsolutePath();
                    fileSize = storeFile.length();
                    break;
                case fastdfs:
                    File tmpFile = createTempFile();
                    FileUtils.copyURLToFile(urlFile.getConnUrl(), tmpFile);
                    filePath = FastDfsClient.uploadFile(IOUtils.toByteArray(new FileInputStream(tmpFile)),
                            fileName, getFileSuffix(fileName));
                    fileSize = tmpFile.length();
                    break;
                case ftp:
                    log.debug("基于ftp，方式同sftp");
                case sftp:
                    File tmpFile1 = createTempFile();
                    FileUtils.copyURLToFile(urlFile.getConnUrl(), tmpFile1);
                    sftpUtil.upload(createUploadDirectoryPath(directory), fileName, tmpFile1);
                    filePath = config.getUploadPath() + createUploadDirectoryPath(directory) + ApplicationConstants.SLASH + fileName;
                    break;
            }
            urlConnection.disconnect();
        } catch (Exception e) {
            if (urlConnection != null) {
                urlConnection.disconnect();
                urlConnection = null;
            }
            Exceptions.printException(e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
                urlConnection = null;
            }
        }
        SysFile sysFile = SysFile.builder().fileName(fileName).fileType(urlFile.getFileSuffix()).storeLocation(serverUploadLocation)
                .filePath(filePath).fileSize(fileSize).downLoadUrl(DOWNLOAD_FULL_URL).apiFilePath(DOWNLOAD_FULL_URL_API)
                .anonymousFilePath(DOWNLOAD_FULL_URL_ANONYMOUS).build();
        log.debug("上传文件成功，具体信息如下： {}", sysFile.toString());
        return sysFile;
    }

    /**
     * 将本地文件上传至系统
     * @param localFile  系统临时目录的文件
     * @param directory 相对路径
     * @return SysFile
     */
    public SysFile uploadFromLocal(File localFile, String directory) {
        String filePath = null;
        String fileName = localFile.getName();
        try {
            switch (serverUploadLocation) {
                case disk:
                    File targetFileDirectory = createUploadDirectory(directory);
                    Path path = Paths.get(targetFileDirectory.getPath() + ApplicationConstants.SLASH + localFile.getName());
                    Files.write(path, Files.readAllBytes(Paths.get(localFile.getAbsolutePath())));
                    filePath = path.toString();
                    break;
                case fastdfs:
                    filePath = FastDfsClient.uploadLocalFile(localFile.getAbsolutePath());
                    break;
                case ftp:
                    log.debug("基于ftp，方式同sftp");
                case sftp:
                    sftpUtil.upload(createUploadDirectoryPath(directory), fileName, localFile);
                    filePath = config.getUploadPath() + createUploadDirectoryPath(directory) + ApplicationConstants.SLASH + fileName;
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            Exceptions.printException(e);
        }
        SysFile sysFile = SysFile.builder().fileName(fileName).fileType(getFileSuffix(localFile.getAbsolutePath())).storeLocation(serverUploadLocation)
                .filePath(filePath).fileSize(localFile.length()).downLoadUrl(DOWNLOAD_FULL_URL).apiFilePath(DOWNLOAD_FULL_URL_API)
                .anonymousFilePath(DOWNLOAD_FULL_URL_ANONYMOUS).build();
        log.debug("上传文件成功，具体信息如下： {}", sysFile.toString());
        return sysFile;
    }

    private BufferedImage toBufferedImage(Image image) {
        if (image instanceof BufferedImage) {
            return (BufferedImage) image;
        }
        // This code ensures that all the pixels in the image are loaded
        image = new ImageIcon(image).getImage();
        BufferedImage bimage = null;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        try {
            int transparency = Transparency.OPAQUE;
            GraphicsDevice gs = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gs.getDefaultConfiguration();
            bimage = gc.createCompatibleImage(image.getWidth(null), image.getHeight(null), transparency);
        } catch (HeadlessException e) {
            // The system does not have a screen
        }
        if (bimage == null) {
            // Create a buffered image using the default color model
            int type = BufferedImage.TYPE_INT_RGB;
            bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
        }
        // Copy image to buffered image
        Graphics g = bimage.createGraphics();
        // Paint the image onto the buffered image
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return bimage;
    }

    /**
     * 将应用系统中的图片压缩后，再上传
     * <p>
     * 图片高质量压缩参考：http://www.lac.inpe.br/JIPCookbook/6040-howto-compressimages.jsp
     *
     * @param imageFile 应用系统Context路径下的图片
     * @param quality   压缩质量
     * @param directory 相对路径
     * @return SysFile
     */
    public SysFile uploadCompressImage(File imageFile, float quality, String directory) {
        String filePath = null;
        String fileName = imageFile.getName();
        ByteArrayOutputStream baos = null;
        ImageOutputStream ios = null;
        ByteArrayInputStream bais = null;
        ImageWriter writer;
        try {
            BufferedImage image = null;
            if(getFileSuffix(imageFile.getName()).equalsIgnoreCase("bmp")){
                image = ImageIO.read(imageFile);
            } else {
//            java上传图片，压缩、更改尺寸等导致变色（表层蒙上一层红色）
//            https://blog.csdn.net/qq_25446311/article/details/79140008?tdsourcetag=s_pctim_aiomsg
                Image bufferedImage = Toolkit.getDefaultToolkit().getImage(imageFile.getAbsolutePath());
                image = this.toBufferedImage(bufferedImage);// Image to BufferedImage
            }
            Iterator<ImageWriter> writers = ImageIO.getImageWritersBySuffix("jpg");
            if (!writers.hasNext())
                throw new IllegalStateException("No writers found");
            writer = (ImageWriter) writers.next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);
            baos = new ByteArrayOutputStream(32768);
            switch (serverUploadLocation) {
                case disk:
                    File targetFileDirectory = createUploadDirectory(directory);
                    File compressedFile = new File(targetFileDirectory.getAbsolutePath() + ApplicationConstants.SLASH + imageFile.getName());
                    log.debug("压缩图片保存路径为 :" + compressedFile.getPath());
                    ios = ImageIO.createImageOutputStream(baos);
                    FileImageOutputStream output = new FileImageOutputStream(compressedFile);
                    writer.setOutput(output);
                    writer.write(null, new IIOImage(image, null, null), param);
                    output.flush();
                    writer.dispose();
                    ios.flush();
                    ios.close();
                    baos.close();
                    output = null;
                    writer = null;
                    ios = null;
                    baos = null;
                    filePath = compressedFile.getAbsolutePath();
                    break;
                case fastdfs:
                    ios = ImageIO.createImageOutputStream(baos);
                    File tmpFile = createTempFile(getFileSuffix(imageFile.getName()));
                    output = new FileImageOutputStream(tmpFile);
                    writer.setOutput(output);
                    writer.write(null, new IIOImage(image, null, null), param);
                    output.flush();
                    writer.dispose();
                    ios.flush();
                    ios.close();
                    baos.close();
                    output = null;
                    writer = null;
                    ios = null;
                    baos = null;
                    filePath = FastDfsClient.uploadLocalFile(tmpFile.getAbsolutePath());
                    tmpFile.deleteOnExit();
                    break;
                case ftp:
                    log.debug("基于ftp，方式同sftp");
                case sftp:
                    ios = ImageIO.createImageOutputStream(baos);
                    tmpFile = createTempFile(getFileSuffix(imageFile.getName()));
                    output = new FileImageOutputStream(tmpFile);
                    writer.setOutput(output);
                    writer.write(null, new IIOImage(image, null, null), param);
                    output.flush();
                    writer.dispose();
                    ios.flush();
                    ios.close();
                    baos.close();
                    output = null;
                    writer = null;
                    ios = null;
                    baos = null;
                    sftpUtil.upload(createUploadDirectoryPath(directory), fileName, tmpFile);
                    filePath = config.getUploadPath() + createUploadDirectoryPath(directory) + ApplicationConstants.SLASH + fileName;
                    tmpFile.deleteOnExit();
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            Exceptions.printException(e);
        } finally {
            if (baos != null)
                try {
                    baos.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
        }
        SysFile sysFile = SysFile.builder().fileName(fileName).fileType(getFileSuffix(imageFile.getAbsolutePath())).storeLocation(serverUploadLocation)
                .filePath(filePath).fileSize(imageFile.length()).downLoadUrl(DOWNLOAD_FULL_URL).apiFilePath(DOWNLOAD_FULL_URL_API)
                .anonymousFilePath(DOWNLOAD_FULL_URL_ANONYMOUS).build();
        log.debug("上传文件成功，具体信息为【{}】", sysFile.toString());
        return sysFile;
    }

    /**
     * 从远程URL上传压缩图片
     * @param imageUrl
     * @param quality
     * @param directory 相对路径
     * @return SysFile
     */
    public SysFile uploadCompressImageFromUrl(String imageUrl, float quality, String directory) {
        File imageFile = downloadFromUrl(imageUrl);
        return uploadCompressImage(imageFile, quality, directory);
    }

    /**
     * 设置文件上传路径
     */
    public String createUploadDirectoryPath(String directory) {
        return ApplicationConstants.SEPARATOR + DateUtil.getDateStr("yyyy")
                + ApplicationConstants.SEPARATOR + DateUtil.getDateStr("MM")
                + ApplicationConstants.SEPARATOR + config.getAppcode()
                + ApplicationConstants.SEPARATOR + directory;
    }

    /**
     * 设置本地文件上传目录（适用于disk方式）
     */
    public File createCustomUploadDirectory(String directory) throws IOException {
        String storePath = directory;
        File targetFileDirectory = new File(storePath);
        if (!targetFileDirectory.exists()) {
            FileUtils.forceMkdir(targetFileDirectory);
            log.debug("目录不存在，即将强制创建路径【{}】", storePath);
        }
        return targetFileDirectory;
    }

    /**
     * 设置本地文件上传目录（适用于disk方式）
     */
    public File createUploadDirectory(String directory) throws IOException {
        String storePath = config.getUploadPath() + createUploadDirectoryPath(directory);
        File targetFileDirectory = new File(storePath);
        if (!targetFileDirectory.exists()) {
            FileUtils.forceMkdir(targetFileDirectory);
            log.debug("目录不存在，即将强制创建路径【{}】", storePath);
        }
        return targetFileDirectory;
    }

    /**
     * 创建临时目录
     * @param dir
     * @return File
     */
    public File createTempDirectory(String dir){
        File tempDirectory = new File(config.getUploadTmpFileLocation().concat(ApplicationConstants.SEPARATOR)
                .concat(CodeGenerator.systemUUID()).concat(ApplicationConstants.SEPARATOR).concat(dir));
        try {
            FileUtils.forceMkdir(tempDirectory);
        } catch (IOException e) {
            Exceptions.printException(e);
            return null;
        }
        log.debug("创建的临时目录为【{}】", tempDirectory.getAbsolutePath());
        return tempDirectory;
    }

    /**
     * 创建指定文件名称的临时文件
     * @param dir
     * @param filename
     * @return File
     */
    public File createTempFile(String dir, String filename){
        File tempFile = null;
        try {
            tempFile = new File(config.getUploadTmpFileLocation().concat(ApplicationConstants.SEPARATOR)
                    .concat(CodeGenerator.systemUUID()).concat(ApplicationConstants.SEPARATOR)
                    .concat(dir).concat(ApplicationConstants.SEPARATOR).concat(filename));
            FileUtils.forceDeleteOnExit(tempFile);
            FileUtils.touch(tempFile);
        } catch (IOException e) {
            Exceptions.printException(e);
        }
        log.debug("创建的临时文件路径为【{}】", tempFile.getAbsolutePath());
        return tempFile;
    }

    /**
     * 创建临时后缀临时文件
     * @return File
     */
    public File createTempFile(){
        return createTempFile(CodeGenerator.randomChar(4));
    }

    /**
     * 创建带后缀临时文件
     * @param suffix
     * @return File
     */
    public File createTempFile(String suffix){
        File tempFile = null;
        try {
            tempFile = new File(config.getUploadTmpFileLocation().concat(ApplicationConstants.SEPARATOR).concat(CodeGenerator.systemUUID()+ApplicationConstants.DOT+suffix));
            FileUtils.touch(tempFile);
        } catch (IOException e) {
            Exceptions.printException(e);
        }
        log.debug("创建的临时文件路径为【{}】", tempFile.getAbsolutePath());
        return tempFile;
    }

    /**
     * 从系统中下载文件
     * @param sysFile
     * @return File
     */
    public File getFileFromSystem(SysFile sysFile){
        if(null == sysFile.getStoreLocation()){
            log.debug("文件记录的存储位置为空，将通过【{}】方式下载文件【{}】", serverUploadLocation, sysFile.getFilePath());
            return getFileFromSystem(serverUploadLocation, sysFile.getFilePath());
        }
        else{
            log.debug("将通过【{}】方式下载文件【{}】", sysFile.getStoreLocation(), sysFile.getFilePath());
            return getFileFromSystem(sysFile.getStoreLocation(), sysFile.getFilePath());
        }
    }

    public File getFileFromSystem(StoreLocation serverUploadLocation, String filePath){
        Assert.notNull(serverUploadLocation, "文件保存位置不能为空!");
        Assert.notNull(serverUploadLocation, "文件路径不能为空!");
        File realFile = null;
        log.debug("即将以【{}】方式读取文件【{}】",serverUploadLocation, filePath);
        switch (serverUploadLocation) {
            case disk:
                realFile = new File(filePath);
                if(!realFile.exists()){
                    realFile = null;
                }
                break;
            case fastdfs:
                realFile = downloadFromUrl(getFileUrlFromFastDfs(filePath));
                break;
            case ftp:
                log.debug("基于ftp，方式同sftp");
            case sftp:
                //下载文件时每次打开-关闭FTP太费资源。因此，隐藏下面代码，改为配置Nginx的虚拟目录，直接提供URL进行下载
//                String directory = StringUtils.substringBeforeLast(filePath, ApplicationConstants.SLASH);
//                String downloadFile = StringUtils.substringAfterLast(filePath, ApplicationConstants.SLASH);
//                realFile = sftpUtil.download2File(directory, downloadFile, createTempFile(getFileSuffix(filePath)));

                //Nginx的虚拟目录参考
//                location  /staticfiles/ {
//                        alias ${app.file.upload.path};
//                        autoindex on;
//                }
                String nginxFileUrl = StringUtils.replace(filePath, config.getUploadPath(), config.getAppHostPort().concat("/staticfiles"));
                if ( BooleanUtil.toBoolean( config.getCustomUploadFlag()) ){
                    nginxFileUrl = StringUtils.replace(filePath, config.getCustomUploadBashPath(), config.getShareHostPost());
                }
                nginxFileUrl = StringUtils.replace(nginxFileUrl, "\\", SLASH);
                log.debug("SFTP上传的文件即将通过URL：【{}】进行下载", nginxFileUrl);
                realFile = downloadFromUrl(nginxFileUrl);
                break;
        }
        return realFile;
    }

    /**
     * 获取保存在FastDfs中的文件访问路径(免登陆)
     * @param filePath
     * @return String
     */
    public String getFileUrlFromFastDfs(String filePath){
        return config.getAppHostPort() + ApplicationConstants.SLASH + filePath;
    }

    /**
     * 根据远程文件的url下载文件
     * @param fileUrl
     * @return File
     */
    public File downloadFromUrl(String fileUrl) {
        Assert.notNull(fileUrl, "下载链接地址不可为空");
        File targetFile = null;
        HttpURLConnection urlConnection = null;
        try {
            UrlFile urlFile = openAndConnectUrl(fileUrl);
            urlConnection = urlFile.getConn();
            if (null != urlConnection && urlConnection.getContentLengthLong() != 0) {
                try {
                    targetFile = createTempFile(urlFile.getFileSuffix());
                    FileUtils.copyURLToFile(urlFile.getConnUrl(), targetFile);
                    log.debug("即将从路径【{}】 下载文件保存至【{}】", fileUrl, targetFile.getAbsolutePath());
                }catch (FileNotFoundException e){
                    log.warn("URL目标文件不存在【{}】，错误信息为【{}】", urlFile.getConnUrl(), e.getMessage());
                    targetFile = null;
                }
            } else {
                log.error("从路径【{}】下载文件发生异常", fileUrl);
            }
        } catch (Exception e) {
            if (urlConnection!= null) {
                urlConnection.disconnect();
                urlConnection = null;
            }
            Exceptions.printException(e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return targetFile;
    }

    private UrlFile openAndConnectUrl(String remoteFileUrl) throws Exception {
        String urlStr = FilenameUtils.getFullPath(remoteFileUrl);
        //URL路径中有/分割的文件名，进行URL编码处理中文，否则不做处理
        String fileName = getFileName(remoteFileUrl);
        if(StringUtils.isNotEmpty(fileName)){
            if(ChineseHelper.containsChinese(fileName)){
                urlStr += URLEncoder.encode(fileName);
            }else {
                urlStr += fileName;
            }
        }
        log.debug("即将从地址【{}】下载文件", urlStr);
        URL connUrl = null;
        HttpURLConnection urlConnection = null;
        String suffix = null;
        try {
            connUrl = new URL(urlStr);
            urlConnection = (HttpURLConnection) connUrl.openConnection();
            urlConnection.setDoInput(true);
            urlConnection.setRequestMethod(ApplicationConstants.HTTPGET);
            urlConnection.connect();
            suffix = getFileSuffix(remoteFileUrl);
            if(StringUtils.isEmpty(suffix)){
                BufferedInputStream bis = new BufferedInputStream(urlConnection.getInputStream());
                String contentType = HttpURLConnection.guessContentTypeFromStream(bis);
                if(contentType.trim().startsWith("image")){
                    suffix = StringUtils.substringAfter(contentType, ApplicationConstants.SLASH);
                }
            }
        } catch (MalformedURLException e){
            log.warn("URL地址错误【{}】，错误信息为【{}】", urlStr, e.getMessage());
        }

        return UrlFile.builder().remoteFileUrl(remoteFileUrl).conn(urlConnection).connUrl(connUrl).fileName(fileName).fileSuffix(suffix).build();
    }

    /**
     * 物理删除文件
     * @param sysFile
     * @return boolean
     */
    public boolean deleteFile(SysFile sysFile) {
        if(null == sysFile.getStoreLocation()){
            return deleteFile(serverUploadLocation, sysFile.getFilePath());
        }
        else{
            return deleteFile(sysFile.getStoreLocation(), sysFile.getFilePath());
        }
    }

    public boolean deleteFile(StoreLocation serverUploadLocation, String filePath) {
        Assert.notNull(serverUploadLocation, "文件保存位置不能为空!");
        Assert.notNull(serverUploadLocation, "文件路径不能为空!");
        boolean result = ApplicationConstants.TRUE;
        log.debug("即将以【{}】方式删除文件【{}】",serverUploadLocation, filePath);
        switch (serverUploadLocation) {
            case disk:
                try {
                    FileUtils.forceDelete(new File(filePath));
                } catch (Exception e) {
                    result = ApplicationConstants.FALSE;
                    Exceptions.printException(e);
                }
                break;
            case fastdfs:
                try {
                    FastDfsClient.deleteFile(filePath);
                } catch (Exception e) {
                    result = ApplicationConstants.FALSE;
                    Exceptions.printException(e);
                }
                break;
            case ftp:
                log.debug("基于ftp，方式同sftp");
            case sftp:
                try {
                    sftpUtil.deleteFile(filePath);
                } catch (Exception e) {
                    result = ApplicationConstants.FALSE;
                    Exceptions.printException(e);
                }
                break;
            default:
                break;
        }
        return result;
    }

    /**
     * 读取放置在项目resource目录下的文件，以二进制流的方式方法，
     *  因为项目打包后在读取项目里面的文件必须用流的方式获取，否则用其他方式获取会提示找不到文件
     * @param filePath
     * @return byte[]
     */
    public static byte[] getFileByte(String filePath){
        byte[] fileByte = null;
        try {
            if ( StringUtils.isEmpty( filePath ) ){
                return null;
            }
            ClassPathResource classPathResource = new ClassPathResource( filePath );
            BufferedInputStream bufferedInputStream = new BufferedInputStream( classPathResource.getInputStream() );
            fileByte = new byte[bufferedInputStream.available()];
            bufferedInputStream.read(fileByte);
            bufferedInputStream.close();
        } catch ( IOException e ) {
            Exceptions.printException(e);
        }
        return fileByte;
    }

    public static void main(String[] args) throws Exception {
//        String fileUrl = "http://mmbiz.qpic.cn/mmbiz_jpg/MyDnHITZqkiaoqpMdyFh84RP6pDZ4dMIHa2d4JFJWO5R6nGPVN1EA9GyVnfqiaxZ9EY5L3L0CBpAvRheQlxgvJ5Q/0";
//        String fileUrl = "http://10.92.81.163:8088/group1/M00/00/00/ClxQR1uWKAyAM4c4AAAyAsyPzjY83.docx";
//        String fileUrl = "http://shmhzs.free.idcfengye.com/anon/file/anonymous/sys/file/download?id=V569213798079004672";
        String fileUrl = "http://10.87.42.136:8088/maipdocument/夏季防洪防汛.doc";
        AppFileUtil util = new AppFileUtil();
        File files = util.downloadFromUrl(fileUrl);
        System.out.println(getFileName(fileUrl));
        System.out.println(getFileBaseName(fileUrl));
        System.out.println(getFileSuffix(fileUrl));

        System.out.println(StrUtil.endWithIgnoreCase("/home/oaapp/simbestboot/uploadFiles/uploadFiles/anddoc/doc/","/") );
    }
}
