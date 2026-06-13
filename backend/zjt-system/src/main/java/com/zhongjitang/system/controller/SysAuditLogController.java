package com.zhongjitang.system.controller;

import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.system.domain.entity.SysAuditLogDO;
import com.zhongjitang.system.service.IAuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/admin/audit-logs")
@Tag(name = "审计日志")
@RequiredArgsConstructor
public class SysAuditLogController {

    private final IAuditLogService auditLogService;

    @GetMapping
    @Operation(summary = "审计日志列表")
    public R<PageResult<SysAuditLogDO>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return auditLogService.page(page, pageSize, keyword, module, action, startDate, endDate);
    }
}
