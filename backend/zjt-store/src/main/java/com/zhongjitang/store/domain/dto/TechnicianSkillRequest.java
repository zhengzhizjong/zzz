package com.zhongjitang.store.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Schema(description = "技师技能设置请求")
public class TechnicianSkillRequest {

    @NotNull(message = "技师ID不能为空")
    @Schema(description = "技师ID", example = "1")
    private Long technicianId;

    @NotNull(message = "服务项目ID不能为空")
    @Schema(description = "服务项目ID", example = "1")
    private Long serviceItemId;

    @NotNull(message = "熟练度不能为空")
    @Schema(description = "熟练度: 1入门 2熟练 3精通 4专家", example = "2")
    private Integer proficiency;

    @Schema(description = "认证时间")
    private LocalDateTime certifiedAt;

    @Schema(description = "认证编号", example = "CERT-2026-001")
    private String certificationNo;
}
