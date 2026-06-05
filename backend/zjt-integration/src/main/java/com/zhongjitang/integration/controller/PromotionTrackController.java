package com.zhongjitang.integration.controller;

import com.zhongjitang.common.core.result.R;
import com.zhongjitang.integration.domain.dto.PromotionTrackRequest;
import com.zhongjitang.integration.domain.vo.PromotionFunnelVO;
import com.zhongjitang.integration.service.PromotionTrackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/integration/promotion")
@Tag(name = "推广追踪")
@RequiredArgsConstructor
public class PromotionTrackController {

    private final PromotionTrackService promotionTrackService;

    @PostMapping("/track")
    @Operation(summary = "追踪事件")
    public R<Void> track(@Valid @RequestBody PromotionTrackRequest request) {
        return promotionTrackService.track(request);
    }

    @GetMapping("/funnel")
    @Operation(summary = "推广漏斗数据")
    public R<PromotionFunnelVO> getFunnel(
            @Parameter(description = "技师ID") @RequestParam(required = false) Long technicianId,
            @Parameter(description = "门店ID") @RequestParam(required = false) Long storeId,
            @Parameter(description = "开始日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return promotionTrackService.getFunnel(technicianId, storeId, startDate, endDate);
    }
}
