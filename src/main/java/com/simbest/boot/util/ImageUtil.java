package com.simbest.boot.util;

import com.simbest.boot.base.exception.Exceptions;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * <strong>Title : ImageUtil</strong><br>
 * <strong>Description : 图片压缩工具类</strong><br>
 * <strong>Create on : 2020/8/31</strong><br>
 * <strong>Modify on : 2020/8/31</strong><br>
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
public class ImageUtil {

    /**
     * 根据指定大小和指定精度压缩图片
     * @param srcPath 源图片地址
     * @param desPath 目标图片地址
     * @param desFilesize 指定图片大小，单位kb
     * @param accuracy  精度，递归压缩的比率，建议小于0.9
     * @return
     */
    public void commpressPicForSize(String srcPath, String desPath,long desFileSize, double accuracy) {
        Assert.notNull(srcPath, "源图片路径不能为空！");
        Assert.notNull(desPath, "目标图片路径不能为空！");
        try {
            File srcFile = new File(srcPath);
            long srcFileSize = srcFile.length();
            log.warn("源图片：{}，大小：" ,srcFileSize / 1024 + "kb");
            // 1、先转换成jpg
            Thumbnails.of(srcPath).scale(1f).toFile(desPath);
            // 递归压缩，直到目标文件大小小于desFileSize
            commpressPicCycle(desPath, desFileSize, accuracy);
            File desFile = new File(desPath);
            log.warn("目标图片：{}，大小",desFile.length()/ 1024 + "kb");
        } catch (Exception e) {
            Exceptions.printException(e);
        }
    }

    /**
     * 按照比例进行缩放
     * @param srcPath 源图片地址
     * @param desPath 目标图片地址
     * @param desFilesize 指定图片大小，单位kb
     * @param accuracy  精度，递归压缩的比率，建议小于0.9
     * @return
     */
    public void commpressPicForScale(String srcPath, String desPath,long desFileSize, double accuracy) {
        Assert.notNull(srcPath, "源图片路径不能为空！");
        Assert.notNull(desPath, "目标图片路径不能为空！");
        try {
            File srcFile = new File(srcPath);
            long srcFileSize = srcFile.length();
            log.warn("源图片：{}，大小：" ,srcFileSize / 1024 + "kb");
            // 1、先转换成jpg
            Thumbnails.of(srcPath).scale(1f).toFile(desPath);
            // 按照比例进行缩放
            imgScale(desPath, desFileSize, accuracy);
            File desFile = new File(desPath);
            log.warn("目标图片：{}，大小",desFile.length()/ 1024 + "kb");
        } catch (Exception e) {
            Exceptions.printException(e);
        }
    }

    /**
     * 图片尺寸不变，压缩文件大小
     * @param srcPath 源图片地址
     * @param desPath 目标图片地址
     * @param desFilesize 指定图片大小，单位kb
     * @param accuracy  精度，递归压缩的比率，建议小于0.9
     * @return
     */
    public void commpressPicForScaleSize(String srcPath, String desPath,long desFileSize, double accuracy) {
        Assert.notNull(srcPath, "源图片路径不能为空！");
        Assert.notNull(desPath, "目标图片路径不能为空！");
        try {
            File srcFile = new File(srcPath);
            long srcFileSize = srcFile.length();
            log.warn("源图片：{}，大小：" ,srcFileSize / 1024 + "kb");
            // 1、先转换成jpg
            Thumbnails.of(srcPath).scale(1f).toFile(desPath);
            // 按照比例进行缩放
            imgScaleSize(desPath, desFileSize, accuracy);
            File desFile = new File(desPath);
            log.warn("目标图片：{}，大小",desFile.length()/ 1024 + "kb");
        } catch (Exception e) {
            Exceptions.printException(e);
        }
    }

    /**
     * 图片压缩:按指定大小把图片进行缩放（会遵循原图高宽比例）
     * 并设置图片文件大小
     * @param desPath
     * @param desFileSize
     * @param accuracy
     * @throws IOException
     */
    private void commpressPicCycle(String desPath, long desFileSize, double accuracy){
        try {
            File srcFileJPG = new File(desPath);
            long srcFileSizeJPG = srcFileJPG.length();
            // 2、判断大小，如果小于指定大小，不压缩；如果大于等于指定大小，压缩
            if (srcFileSizeJPG <= desFileSize * 1024) {
                return;
            }
            // 计算宽高
            BufferedImage bim = ImageIO.read(srcFileJPG);
            int srcWdith = bim.getWidth();
            int srcHeigth = bim.getHeight();
            int desWidth = new BigDecimal(srcWdith).multiply(new BigDecimal(accuracy)).intValue();
            int desHeight = new BigDecimal(srcHeigth).multiply(new BigDecimal(accuracy)).intValue();
            Thumbnails.of(desPath).size(desWidth, desHeight).outputQuality(accuracy).toFile(desPath);
            commpressPicCycle(desPath, desFileSize, accuracy);
        }catch (Exception e){
            Exceptions.printException( e );
        }
    }

    /**
     * 按照比例进行压缩
     * @param desPath
     * @param desFileSize
     * @param accuracy
     * @throws IOException
     */
    private void imgScale(String desPath, long desFileSize, double accuracy){
        try {
            File file=new File(desPath);
            long fileSize=file.length();
            //判断大小，如果小于指定大小，不压缩；如果大于等于指定大小，压缩
            if(fileSize<=desFileSize*1024){
                return;
            }
            //按照比例进行缩小
            Thumbnails.of(desPath).scale(accuracy).toFile(desPath);//按比例缩小
            //按照比例进行缩放
            imgScale(desPath, desFileSize, accuracy);
        }catch (Exception e){
            Exceptions.printException( e );
        }
    }

    /**
     * 图片尺寸不变，压缩文件大小
     * @param desPath
     * @param desFileSize
     * @param accuracy
     */
    private void imgScaleSize(String desPath, long desFileSize,double accuracy){
        try {
            File fileName=new File(desPath);
            long fileNameSize=fileName.length();
            //判断大小，如果小于指定大小，不压缩；如果大于等于指定大小，压缩
            if(fileNameSize<=desFileSize*1024){
                return;
            }
            //图片尺寸不变，压缩图片文件大小
            //图片尺寸不变，压缩图片文件大小outputQuality实现,参数1为最高质量
            Thumbnails.of(desPath).scale(1f).outputQuality(accuracy).toFile(desPath);
            //图片尺寸不变，压缩文件大小
            imgScaleSize(desPath, desFileSize, accuracy);
        }catch (Exception e){
            Exceptions.printException( e );
        }
    }


    /**
     * 校验图片是否为图片格式
     * @param multipartFile
     * @return
     */
    public boolean checkImage(MultipartFile multipartFile){
        try {
            BufferedImage bi = ImageIO.read(multipartFile.getInputStream());
            if(bi == null){
                return false;
            }
        } catch (IOException e) {
            Exceptions.printException(e);
        }
        return true;
    }
}
