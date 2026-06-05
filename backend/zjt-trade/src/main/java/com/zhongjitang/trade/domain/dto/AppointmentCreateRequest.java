package com.zhongjitang.trade.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@Schema(description = "创建预约请求")
public class AppointmentCreateRequest {

    @NotNull(message = "门店ID不能为空")
    @Schema(description = "门店ID", example = "1")
    private Long storeId;

    @NotNull(message = "日期不能为空")
    @Schema(description = "预约日期", example = "2026-06-05")
    private LocalDate date;

    @NotNull(message = "时段不能为空")
    @Schema(description = "时段", example = "09:00")
    private String timeSlot;

    @NotNull(message = "技师ID不能为空")
    @Schema(description = "技师ID，0表示不指定", example = "1")
    private Long technicianId;

    @NotNull(message = "锁档ID不能为空")
    @Schema(description = "临时锁档ID", example = "LOCK_20260605_001")
    private String lockId;

    @Schema(description = "服务项目ID", example = "1")
    private Long serviceItemId;

    @Schema(description = "会员ID", example = "1")
    private Long memberId;

    @Schema(description = "服务时长(分钟)", example = "60")
    private Integer durationMinutes;

    @Schema(description = "来源渠道", example = "mini_app")
    private String sourceChannel;

    @Schema(description = "咨询备注")
    private String consultationNotes;
}
