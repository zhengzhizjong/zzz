package com.zhongjitang.store.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "更新技师请求")
public class TechnicianUpdateRequest {

    @Schema(description = "技能等级:1初级 2中级 3高级 4专家", example = "2")
    private Integer skillLevel;

    @Schema(description = "擅长项目(JSON)", example = "[\"推拿\",\"艾灸\",\"拔罐\"]")
    private String skilledItems;

    @Schema(description = "默认班次", example = "早班")
    private String defaultSchedule;
}
