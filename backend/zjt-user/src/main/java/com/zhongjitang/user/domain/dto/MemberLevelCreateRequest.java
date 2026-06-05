package com.zhongjitang.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Data
@Schema(description = "创建会员等级请求")
public class MemberLevelCreateRequest {

    @Schema(description = "等级名称", required = true)
    @NotBlank(message = "等级名称不能为空")
    private String levelName;

    @Schema(description = "等级编码", required = true)
    @NotBlank(message = "等级编码不能为空")
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
}
