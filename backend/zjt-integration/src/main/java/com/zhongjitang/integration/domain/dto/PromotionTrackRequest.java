package com.zhongjitang.integration.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@Schema(description = "推广追踪请求")
public class PromotionTrackRequest {

    @NotBlank(message = "事件类型不能为空")
    @Pattern(regexp = "click|register|first_visit|first_order|repurchase", message = "eventType必须是: click, register, first_visit, first_order, repurchase")
    @Schema(description = "事件类型: click/register/first_visit/first_order/repurchase", example = "click")
    private String eventType;

    @NotBlank(message = "推广码不能为空")
    @Schema(description = "推广码", example = "ZJT010101")
    private String promoCode;

    @NotNull(message = "技师ID不能为空")
    @Schema(description = "技师ID", example = "1")
    private Long technicianId;

    @Schema(description = "来源渠道", example = "wechat")
    private String sourceChannel;

    @Schema(description = "客户ID", example = "1001")
    private Long customerId;

    @Schema(description = "客户IP")
    private String customerIp;

    @Schema(description = "用户代理")
    private String userAgent;

    @Schema(description = "扩展信息")
    private String extra;
}
