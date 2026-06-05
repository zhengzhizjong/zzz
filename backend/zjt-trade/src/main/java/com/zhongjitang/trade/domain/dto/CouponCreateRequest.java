package com.zhongjitang.trade.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "创建优惠券请求")
public class CouponCreateRequest {

    @NotBlank(message = "优惠券名称不能为空")
    @Schema(description = "优惠券名称", example = "新客满减券")
    private String couponName;

    @NotNull(message = "优惠券类型不能为空")
    @Schema(description = "优惠券类型: 1满减 2折扣 3体验", example = "1")
    private Integer couponType;

    @NotNull(message = "优惠值不能为空")
    @Schema(description = "优惠值(满减为金额,折扣为折扣率如0.8,体验为0)", example = "50.00")
    private BigDecimal discountValue;

    @Schema(description = "最低消费金额", example = "200.00")
    private BigDecimal minAmount;

    @Schema(description = "最大优惠金额(折扣券用)", example = "100.00")
    private BigDecimal maxDiscount;

    @NotNull(message = "发放总量不能为空")
    @Schema(description = "发放总量", example = "100")
    private Integer totalQuantity;

    @Schema(description = "每人限领数量", example = "1")
    private Integer perLimit;

    @NotNull(message = "开始时间不能为空")
    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "适用项目ID列表，逗号分隔", example = "1,2,3")
    private String applicableItems;
}
