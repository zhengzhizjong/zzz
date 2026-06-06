package com.zhongjitang.trade.controller;

import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.trade.domain.dto.ActivityCreateRequest;
import com.zhongjitang.trade.domain.dto.ActivityUpdateRequest;
import com.zhongjitang.trade.domain.entity.TradeActivityDO;
import com.zhongjitang.trade.service.IActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/trade/activities")
@Tag(name = "活动管理")
@RequiredArgsConstructor
public class ActivityController {

    private final IActivityService activityService;

    @GetMapping
    @Operation(summary = "活动列表")
    public R<PageResult<TradeActivityDO>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return activityService.page(page, pageSize, keyword, status);
    }

    @GetMapping("/active")
    @Operation(summary = "进行中活动")
    public R<List<TradeActivityDO>> getActiveList() {
        return activityService.getActiveList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "活动详情")
    public R<TradeActivityDO> getById(@PathVariable Long id) {
        return activityService.getById(id);
    }

    @PostMapping
    @Operation(summary = "创建活动")
    public R<Void> create(@Valid @RequestBody ActivityCreateRequest request) {
        return activityService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新活动")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody ActivityUpdateRequest request) {
        return activityService.update(id, request);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "状态变更")
    public R<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        return activityService.updateStatus(id, status);
    }
}
