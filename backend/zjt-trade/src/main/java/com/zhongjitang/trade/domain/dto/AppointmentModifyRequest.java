package com.zhongjitang.trade.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@Schema(description = "修改预约请求")
public class AppointmentModifyRequest {

    @NotNull(message = "日期不能为空")
    @Schema(description = "预约日期", example = "2026-06-05")
    private LocalDate date;

    @NotBlank(message = "时段不能为空")
    @Schema(description = "时段", example = "09:00")
    private String timeSlot;

    @NotNull(message = "技师ID不能为空")
    @Schema(description = "技师ID", example = "1")
    private Long technicianId;

    @NotBlank(message = "修改原因不能为空")
    @Schema(description = "修改原因", example = "临时有事需要调整时间")
    private String modifyReason;
}
