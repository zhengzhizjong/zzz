package com.zhongjitang.trade.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@Schema(description = "临时锁档请求")
public class LockTempRequest {

    @NotNull(message = "门店ID不能为空")
    @Schema(description = "门店ID", example = "1")
    private Long storeId;

    @NotNull(message = "技师ID不能为空")
    @Schema(description = "技师ID，0表示不指定", example = "1")
    private Long technicianId;

    @NotNull(message = "日期不能为空")
    @Schema(description = "预约日期", example = "2026-06-05")
    private LocalDate date;

    @NotNull(message = "时段不能为空")
    @Schema(description = "时段", example = "09:00")
    private String timeSlot;
}
