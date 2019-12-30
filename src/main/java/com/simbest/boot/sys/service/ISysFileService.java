package com.simbest.boot.sys.service;

import com.simbest.boot.base.service.ILogicService;
import com.simbest.boot.sys.model.SysFile;
import com.simbest.boot.sys.model.UploadFileResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * 用途：统一系统文件管理逻辑层
 * 作者: lishuyi
 * 时间: 2018/2/23  10:14
 */
public interface ISysFileService extends ILogicService<SysFile, String> {

    /**
     * 上传并保存单个文件
     * @param multipartFile 上传文件
     * @param pmInsType 流程类型
     * @param pmInsId 流程ID
     * @param pmInsTypePart 流程区块
     * @return SysFile
     */
    SysFile uploadProcessFile(MultipartFile multipartFile, String pmInsType, String pmInsId, String pmInsTypePart);

    /**
     * 上传并保存多个文件
     * @param multipartFiles 上传文件
     * @param pmInsType 流程类型
     * @param pmInsId 流程ID
     * @param pmInsTypePart 流程区块
     * @return List<SysFile>
     */
    List<SysFile> uploadProcessFiles(Collection<MultipartFile> multipartFiles, String pmInsType, String pmInsId, String pmInsTypePart);

    /**
     * 导入Excel文件--指定某个sheet页
     * @param multipartFile 上传文件
     * @param pmInsType 流程类型
     * @param pmInsId 流程ID
     * @param pmInsTypePart 流程区块
     * @param clazz 导入对象类
     * @param sheetName sheet页名称
     * @param <T>
     * @return UploadFileResponse
     */
    <T> UploadFileResponse importExcel(MultipartFile multipartFile, String pmInsType, String pmInsId, String pmInsTypePart, Class<T> clazz, String sheetName);

    /**
     * 导入Excel文件--指定某个sheet页
     * @param multipartFile 上传文件
     * @param pmInsType 流程类型
     * @param pmInsId 流程ID
     * @param pmInsTypePart 流程区块
     * @param clazz 导入对象类
     * @param sheetName sheet页名称
     * @param inputRow 起始导入行数
     * @param <T>
     * @return UploadFileResponse
     */
    <T> UploadFileResponse importExcel(MultipartFile multipartFile, String pmInsType, String pmInsId, String pmInsTypePart, Class<T> clazz, String sheetName,int inputRow);

    /**
     * 导入Excel文件--支持多个sheet页
     * @param multipartFile 上传文件
     * @param pmInsType 流程类型
     * @param pmInsId 流程ID
     * @param pmInsTypePart 流程区块
     * @param clazz 导入对象类
     * @param <T>
     * @return UploadFileResponse
     */
    <T> UploadFileResponse importExcel(MultipartFile multipartFile, String pmInsType, String pmInsId, String pmInsTypePart, Class<T> clazz);

    /**
     * 通过SysFile的ID获取实际文件
     * @param id
     * @return File
     */
    File getRealFileById(String id);
}
