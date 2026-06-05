package com.zhongjitang.store.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "技师工作台VO")
public class TechnicianWorkspaceVO {

    @Schema(description = "技师信息")
    private TechnicianInfo technicianInfo;

    @Schema(description = "当前进行中的服务")
    private CurrentService currentService;

    @Schema(description = "待服务预约列表")
    private List<PendingAppointment> pendingAppointments;

    @Data
    @Schema(description = "技师基本信息")
    public static class TechnicianInfo {

        @Schema(description = "技师ID")
        private Long id;

        @Schema(description = "姓名")
        private String name;

        @Schema(description = "头像")
        private String avatar;

        @Schema(description = "技能等级")
        private Integer skillLevel;

        @Schema(description = "是否在岗:0休息 1在岗")
        private Integer isOnline;
    }

    @Data
    @Schema(description = "当前进行中的服务")
    public static class CurrentService {

        @Schema(description = "预约ID")
        private Long appointmentId;

        @Schema(description = "预约编号")
        private String appointmentNo;

        @Schema(description = "会员姓名")
        private String memberName;

        @Schema(description = "服务项目名称")
        private String serviceItemName;

        @Schema(description = "开始时间")
        private String startTime;

        @Schema(description = "结束时间")
        private String endTime;
    }

    @Data
    @Schema(description = "待服务预约")
    public static class PendingAppointment {

        @Schema(description = "预约ID")
        private Long appointmentId;

        @Schema(description = "预约编号")
        private String appointmentNo;

        @Schema(description = "会员姓名")
        private String memberName;

        @Schema(description = "服务项目名称")
        private String serviceItemName;

        @Schema(description = "预约时段")
        private String timeSlot;

        @Schema(description = "预约日期")
        private String appointmentDate;
    }
}
