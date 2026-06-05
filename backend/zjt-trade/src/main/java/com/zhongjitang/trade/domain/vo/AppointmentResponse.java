package com.zhongjitang.trade.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Schema(description = "预约响应VO")
public class AppointmentResponse {

    @Schema(description = "预约ID", example = "1")
    private Long id;

    @Schema(description = "预约编号", example = "APT20260605000001")
    private String appointmentNo;

    @Schema(description = "会员姓名", example = "张三")
    private String memberName;

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

    @Schema(description = "预约状态: 1待确认 2已确认 3进行中 4已完成 5已取消", example = "1")
    private Integer status;

    @Schema(description = "跳转路径", example = "my_appointments")
    private String redirect;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
