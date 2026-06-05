package com.zhongjitang.trade.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "锁档结果VO")
public class LockResultVO {

    @Schema(description = "锁档ID", example = "LOCK_20260605_001")
    private String lockId;

    @Schema(description = "门店ID", example = "1")
    private Long storeId;

    @Schema(description = "技师ID", example = "1")
    private Long technicianId;

    @Schema(description = "日期", example = "2026-06-05")
    private LocalDate date;

    @Schema(description = "时段", example = "09:00")
    private String timeSlot;

    @Schema(description = "过期时间")
    private LocalDateTime expireAt;
}
