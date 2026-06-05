package com.zhongjitang.store.controller;

import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.store.domain.entity.StoreTechnicianPromotionDO;
import com.zhongjitang.store.domain.vo.CommissionVO;
import com.zhongjitang.store.domain.vo.PromotionRankingVO;
import com.zhongjitang.store.domain.vo.PromotionStatsVO;
import com.zhongjitang.store.service.ITechnicianPromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/store/technicians/{id}/promotion")
@Tag(name = "技师推广")
@RequiredArgsConstructor
public class TechnicianPromotionController {

    private final ITechnicianPromotionService technicianPromotionService;

    @GetMapping
    @Operation(summary = "获取推广信息")
    public R<StoreTechnicianPromotionDO> getPromotion(@PathVariable Long id) {
        return technicianPromotionService.getPromotion(id);
    }

    @PostMapping("/generate-poster")
    @Operation(summary = "生成推广海报")
    public R<Map<String, String>> generatePoster(@PathVariable Long id) {
        technicianPromotionService.generatePromoCode(id);
        StoreTechnicianPromotionDO promotion = technicianPromotionService.getPromotion(id).getData();
        Map<String, String> result = new HashMap<>();
        result.put("posterUrl", "/posters/technician_" + id + ".png");
        result.put("promoCode", promotion.getPromoCode());
        result.put("shareLink", promotion.getShareLink());
        return R.ok(result);
    }

    @GetMapping("/stats")
    @Operation(summary = "推广统计")
    public R<PromotionStatsVO> getStats(
            @PathVariable Long id,
            @Parameter(description = "开始日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return technicianPromotionService.getStats(id, startDate, endDate);
    }

    @GetMapping("/commissions")
    @Operation(summary = "佣金明细")
    public R<PageResult<CommissionVO>> getCommissions(
            @PathVariable Long id,
            @Parameter(description = "状态: pending/settled/cancelled") @RequestParam(required = false) String status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") Integer pageSize) {
        return technicianPromotionService.getCommissions(id, status, page, pageSize);
    }

    @GetMapping("/ranking")
    @Operation(summary = "推广排行榜")
    public R<PromotionRankingVO> getRanking(
            @PathVariable Long id,
            @Parameter(description = "门店ID") @RequestParam(required = false) Long storeId,
            @Parameter(description = "统计周期: week/month/quarter") @RequestParam(defaultValue = "month") String period) {
        return technicianPromotionService.getRanking(id, storeId, period);
    }
}
