package com.zhongjitang.trade.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "取消预约请求")
public class AppointmentCancelRequest {

    @NotBlank(message = "取消原因不能为空")
    @Schema(description = "取消原因", example = "临时有事无法到店")
    private String cancelReason;
}
