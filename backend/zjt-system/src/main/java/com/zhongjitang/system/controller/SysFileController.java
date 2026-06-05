package com.zhongjitang.system.controller;

import com.zhongjitang.common.core.result.R;
import com.zhongjitang.system.domain.entity.SysFileDO;
import com.zhongjitang.system.service.IFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/files")
@Tag(name = "文件管理")
@RequiredArgsConstructor
public class SysFileController {

    private final IFileService fileService;

    @PostMapping("/upload")
    @Operation(summary = "文件上传")
    public R<SysFileDO> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String businessType,
            @RequestParam(required = false) String businessId) {
        return fileService.upload(file, businessType, businessId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "文件信息")
    public R<SysFileDO> getById(@PathVariable Long id) {
        return fileService.getById(id);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除文件")
    public R<Void> delete(@PathVariable Long id) {
        return fileService.delete(id);
    }

    @GetMapping("/business")
    @Operation(summary = "按业务查询")
    public R<List<SysFileDO>> getByBusiness(
            @RequestParam String businessType,
            @RequestParam String businessId) {
        return fileService.getByBusiness(businessType, businessId);
    }
}
