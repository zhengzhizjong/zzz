package com.zhongjitang.trade.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Schema(description = "退款申请请求")
public class RefundApplyRequest {

    @NotNull(message = "订单ID不能为空")
    @Schema(description = "订单ID", example = "1")
    private Long orderId;

    @NotNull(message = "退款金额不能为空")
    @Schema(description = "退款金额", example = "299.00")
    private BigDecimal refundAmount;

    @NotBlank(message = "退款原因不能为空")
    @Schema(description = "退款原因", example = "服务不满意")
    private String reason;
}
