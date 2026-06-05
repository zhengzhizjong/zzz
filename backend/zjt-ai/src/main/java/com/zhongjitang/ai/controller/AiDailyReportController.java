package com.zhongjitang.ai.controller;

import com.zhongjitang.ai.domain.entity.AiDailyReportDO;
import com.zhongjitang.ai.service.AiDailyReportService;
import com.zhongjitang.common.core.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai/daily-report")
@Tag(name = "AI日报")
@RequiredArgsConstructor
public class AiDailyReportController {

    private final AiDailyReportService aiDailyReportService;

    @PostMapping("/generate")
    @Operation(summary = "生成日报")
    public R<AiDailyReportDO> generate(@RequestBody Map<String, Object> body) {
        Long storeId = Long.parseLong(body.get("storeId").toString());
        LocalDate date = body.get("date") != null
                ? LocalDate.parse(body.get("date").toString())
                : LocalDate.now();
        return R.ok(aiDailyReportService.generateDailyReport(storeId, date));
    }

    @GetMapping
    @Operation(summary = "查询日报列表")
    public R<List<AiDailyReportDO>> list(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return R.ok(aiDailyReportService.listReports(storeId, startDate, endDate));
    }

    @GetMapping("/{id}")
    @Operation(summary = "日报详情")
    public R<AiDailyReportDO> getById(@PathVariable Long id) {
        return R.ok(aiDailyReportService.getReportById(id));
    }
}
