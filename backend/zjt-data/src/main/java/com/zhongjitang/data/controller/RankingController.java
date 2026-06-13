package com.zhongjitang.data.controller;

import com.zhongjitang.common.core.result.R;
import com.zhongjitang.data.service.RankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/data/ranking")
@RequiredArgsConstructor
@Tag(name = "排行榜")
public class RankingController {

    private final RankingService rankingService;

    @GetMapping("/store")
    @Operation(summary = "门店排行")
    public R<Map<String, Object>> getStoreRanking(
            @RequestParam(defaultValue = "month") String period,
            @RequestParam(defaultValue = "revenue") String dimension,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return rankingService.getStoreRanking(period, dimension, page, pageSize);
    }

    @GetMapping("/technician")
    @Operation(summary = "技师排行")
    public R<Map<String, Object>> getTechnicianRanking(
            @RequestParam(defaultValue = "month") String period,
            @RequestParam(defaultValue = "revenue") String dimension,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return rankingService.getTechnicianRanking(period, dimension, page, pageSize);
    }
}
