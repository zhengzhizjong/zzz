package com.zhongjitang.trade.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "创建疗程卡请求")
public class TreatmentCardCreateRequest {

    @NotBlank(message = "疗程卡名称不能为空")
    @Schema(description = "疗程卡名称", example = "推拿10次卡")
    private String cardName;

    @NotNull(message = "服务项目ID不能为空")
    @Schema(description = "服务项目ID", example = "1")
    private Long serviceItemId;

    @NotNull(message = "总次数不能为空")
    @Schema(description = "总次数", example = "10")
    private Integer totalSessions;

    @NotNull(message = "会员ID不能为空")
    @Schema(description = "会员ID", example = "1")
    private Long memberId;

    @Schema(description = "订单ID", example = "1")
    private Long orderId;

    @NotNull(message = "价格不能为空")
    @Schema(description = "价格", example = "2680.00")
    private BigDecimal price;

    @Schema(description = "过期时间")
    private LocalDateTime expireAt;
}
