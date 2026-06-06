package com.zhongjitang.trade.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "更新活动请求")
public class ActivityUpdateRequest {

    @Schema(description = "活动名称")
    private String activityName;

    @Schema(description = "活动类型: 1满减 2折扣 3赠品 4体验")
    private Integer activityType;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "规则配置(JSON)")
    private String rulesJson;

    @Schema(description = "活动描述")
    private String description;

    @Schema(description = "封面图URL")
    private String coverImageUrl;

    @Schema(description = "适用门店ID列表，逗号分隔")
    private String applicableStores;
}
