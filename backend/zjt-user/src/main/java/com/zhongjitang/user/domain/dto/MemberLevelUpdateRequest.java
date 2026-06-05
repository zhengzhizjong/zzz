package com.zhongjitang.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "更新会员等级请求")
public class MemberLevelUpdateRequest {

    @Schema(description = "等级名称")
    private String levelName;

    @Schema(description = "等级编码")
    private String levelCode;

    @Schema(description = "最低消费金额")
    private BigDecimal minSpent;

    @Schema(description = "最低到店次数")
    private Integer minVisits;

    @Schema(description = "折扣率")
    private BigDecimal discountRate;

    @Schema(description = "图标URL")
    private String iconUrl;

    @Schema(description = "颜色")
    private String color;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "状态: 0=禁用, 1=启用")
    private Integer status;
}
