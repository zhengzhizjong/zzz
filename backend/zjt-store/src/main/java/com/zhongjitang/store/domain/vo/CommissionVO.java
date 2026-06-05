package com.zhongjitang.store.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "佣金明细VO")
public class CommissionVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "客户姓名")
    private String customerName;

    @Schema(description = "佣金金额")
    private BigDecimal commissionAmount;

    @Schema(description = "佣金比例")
    private BigDecimal commissionRate;

    @Schema(description = "订单金额")
    private BigDecimal orderAmount;

    @Schema(description = "状态: pending/settled/cancelled")
    private String status;

    @Schema(description = "结算时间")
    private LocalDateTime settledAt;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
