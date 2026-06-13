package com.zhongjitang.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.system.domain.entity.SysFileDO;
import com.zhongjitang.system.mapper.SysFileMapper;
import com.zhongjitang.system.service.IFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements IFileService {

    private final SysFileMapper fileMapper;

    @Value("${file.upload.path:/tmp/zjt-upload}")
    private String uploadPath;

    @Value("${file.upload.base-url:http://localhost:8080/files}")
    private String baseUrl;

    @Override
    public R<PageResult<SysFileDO>> page(Integer page, Integer pageSize, String name, String type) {
        Page<SysFileDO> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<SysFileDO> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(name)) {
            wrapper.like(SysFileDO::getFileName, name);
        }
        if (StringUtils.hasText(type)) {
            wrapper.eq(SysFileDO::getFileType, type);
        }
        wrapper.orderByDesc(SysFileDO::getCreatedAt);

        Page<SysFileDO> result = fileMapper.selectPage(pageParam, wrapper);
        return R.ok(PageResult.of(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @Override
    public R<SysFileDO> upload(MultipartFile file, String businessType, String businessId) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "上传文件不能为空");
        }

        String originalName = file.getOriginalFilename();
        String ext = "";
        if (StringUtils.hasText(originalName) && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + ext;

        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String relativePath = datePath + "/" + fileName;
        Path fullPath = Paths.get(uploadPath, relativePath);

        try {
            Files.createDirectories(fullPath.getParent());
            file.transferTo(fullPath.toFile());
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "文件上传失败");
        }

        String md5;
        try {
            md5 = DigestUtils.md5DigestAsHex(file.getInputStream());
        } catch (IOException e) {
            md5 = "";
        }

        SysFileDO fileDO = new SysFileDO();
        fileDO.setFileName(fileName);
        fileDO.setOriginalName(originalName);
        fileDO.setFilePath(relativePath);
        fileDO.setFileSize(file.getSize());
        fileDO.setFileType(determineFileType(file.getContentType()));
        fileDO.setMimeType(file.getContentType());
        fileDO.setBucketName("local");
        fileDO.setUrl(baseUrl + "/" + relativePath);
        fileDO.setMd5(md5);
        fileDO.setBusinessType(businessType);
        fileDO.setBusinessId(businessId);
        fileMapper.insert(fileDO);

        return R.ok(fileDO);
    }

    @Override
    public R<SysFileDO> getById(Long id) {
        SysFileDO fileDO = fileMapper.selectById(id);
        if (fileDO == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "文件不存在，ID: " + id);
        }
        return R.ok(fileDO);
    }

    @Override
    public R<Void> delete(Long id) {
        SysFileDO fileDO = fileMapper.selectById(id);
        if (fileDO == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "文件不存在，ID: " + id);
        }

        Path filePath = Paths.get(uploadPath, fileDO.getFilePath());
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("删除物理文件失败: {}", filePath, e);
        }

        fileMapper.deleteById(id);
        return R.ok();
    }

    @Override
    public R<List<SysFileDO>> getByBusiness(String businessType, String businessId) {
        LambdaQueryWrapper<SysFileDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysFileDO::getBusinessType, businessType);
        wrapper.eq(SysFileDO::getBusinessId, businessId);
        wrapper.orderByDesc(SysFileDO::getCreatedAt);
        List<SysFileDO> files = fileMapper.selectList(wrapper);
        return R.ok(files);
    }

    private String determineFileType(String mimeType) {
        if (mimeType == null) {
            return "other";
        }
        if (mimeType.startsWith("image/")) {
            return "image";
        }
        if (mimeType.startsWith("video/")) {
            return "video";
        }
        if (mimeType.contains("pdf") || mimeType.contains("document") ||
                mimeType.contains("word") || mimeType.contains("excel") ||
                mimeType.contains("spreadsheet") || mimeType.contains("text")) {
            return "document";
        }
        return "other";
    }
}
