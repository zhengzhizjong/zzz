package com.zhongjitang.trade.controller;

import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.trade.domain.dto.TreatmentCardCreateRequest;
import com.zhongjitang.trade.domain.entity.TradeTreatmentCardDO;
import com.zhongjitang.trade.domain.entity.TradeTreatmentCardUsageDO;
import com.zhongjitang.trade.service.ITreatmentCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/trade/treatment-cards")
@Tag(name = "疗程卡管理")
@RequiredArgsConstructor
public class TreatmentCardController {

    private final ITreatmentCardService treatmentCardService;

    @GetMapping
    @Operation(summary = "疗程卡列表")
    public R<PageResult<TradeTreatmentCardDO>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) Integer status) {
        return treatmentCardService.page(page, pageSize, memberId, status);
    }

    @PostMapping
    @Operation(summary = "创建疗程卡")
    public R<Void> create(@Valid @RequestBody TreatmentCardCreateRequest request) {
        return treatmentCardService.create(request);
    }

    @PutMapping("/{cardId}/use")
    @Operation(summary = "使用疗程卡")
    public R<Void> use(@PathVariable Long cardId,
                       @RequestParam Long appointmentId) {
        return treatmentCardService.use(cardId, appointmentId);
    }

    @GetMapping("/{cardId}/usage-history")
    @Operation(summary = "疗程卡使用记录")
    public R<List<TradeTreatmentCardUsageDO>> getUsageHistory(@PathVariable Long cardId) {
        return treatmentCardService.getUsageHistory(cardId);
    }

    @GetMapping("/my")
    @Operation(summary = "我的疗程卡列表")
    public R<List<TradeTreatmentCardDO>> myCards(@RequestParam Long memberId) {
        return treatmentCardService.myCards(memberId);
    }
}
