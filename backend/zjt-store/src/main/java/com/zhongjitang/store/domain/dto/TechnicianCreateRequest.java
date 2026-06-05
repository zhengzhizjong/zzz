package com.zhongjitang.store.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(description = "创建技师请求")
public class TechnicianCreateRequest {

    @NotNull(message = "门店ID不能为空")
    @Schema(description = "门店ID", example = "1")
    private Long storeId;

    @NotNull(message = "员工ID不能为空")
    @Schema(description = "员工ID", example = "1")
    private Long employeeId;

    @Schema(description = "技能等级:1初级 2中级 3高级 4专家", example = "1")
    private Integer skillLevel;

    @Schema(description = "擅长项目(JSON)", example = "[\"推拿\",\"艾灸\"]")
    private String skilledItems;

    @Schema(description = "默认班次", example = "早班")
    private String defaultSchedule;
}
