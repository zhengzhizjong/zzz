package com.zhongjitang.trade.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Schema(description = "我的预约列表VO")
public class MyAppointmentVO {

    @Schema(description = "待服务预约")
    private Group pending;

    @Schema(description = "已完成预约")
    private Group completed;

    @Schema(description = "已取消预约")
    private Group cancelled;

    @Data
    @Schema(description = "预约分组")
    public static class Group {

        @Schema(description = "数量")
        private Integer count;

        @Schema(description = "预约列表")
        private List<AppointmentItemVO> list;
    }

    @Data
    @Schema(description = "预约项VO")
    public static class AppointmentItemVO {

        @Schema(description = "预约ID", example = "1")
        private Long id;

        @Schema(description = "预约编号", example = "APT20260605000001")
        private String appointmentNo;

        @Schema(description = "门店名称", example = "忠济堂国医馆(朝阳店)")
        private String storeName;

        @Schema(description = "服务项目名称", example = "中医推拿")
        private String serviceItemName;

        @Schema(description = "技师名称", example = "李技师")
        private String technicianName;

        @Schema(description = "预约日期", example = "2026-06-05")
        private LocalDate appointmentDate;

        @Schema(description = "预约时间", example = "09:00")
        private LocalTime appointmentTime;

        @Schema(description = "时段", example = "09:00")
        private String timeSlot;

        @Schema(description = "预约状态: 1待确认 2已确认 3服务中 4已完成 5已取消 6超时未到", example = "1")
        private Integer status;

        @Schema(description = "状态名称", example = "待确认")
        private String statusName;

        @Schema(description = "修改次数", example = "0")
        private Integer modifyCount;

        @Schema(description = "是否可修改", example = "true")
        private Boolean canModify;

        @Schema(description = "是否可取消", example = "true")
        private Boolean canCancel;

        @Schema(description = "取消原因")
        private String cancelReason;

        @Schema(description = "创建时间")
        private LocalDateTime createdAt;
    }
}
