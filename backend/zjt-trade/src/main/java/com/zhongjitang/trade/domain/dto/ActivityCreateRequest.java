package com.zhongjitang.trade.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Schema(description = "创建活动请求")
public class ActivityCreateRequest {

    @NotBlank(message = "活动名称不能为空")
    @Schema(description = "活动名称", example = "春季养生满减活动")
    private String activityName;

    @NotNull(message = "活动类型不能为空")
    @Schema(description = "活动类型: 1满减 2折扣 3赠品 4体验", example = "1")
    private Integer activityType;

    @NotNull(message = "开始时间不能为空")
    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "规则配置(JSON)")
    private String rulesJson;

    @Schema(description = "活动描述")
    private String description;

    @Schema(description = "封面图URL")
    private String coverImageUrl;

    @Schema(description = "适用门店ID列表，逗号分隔", example = "1,2,3")
    private String applicableStores;
}
