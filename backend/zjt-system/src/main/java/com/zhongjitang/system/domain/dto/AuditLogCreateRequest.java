package com.zhongjitang.system.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "审计日志创建请求")
public class AuditLogCreateRequest {

    @Schema(description = "操作用户ID")
    private Long userId;

    @Schema(description = "操作用户名")
    private String userName;

    @NotBlank(message = "操作描述不能为空")
    @Schema(description = "操作描述", example = "创建门店")
    private String operation;

    @Schema(description = "请求方法", example = "POST")
    private String method;

    @NotBlank(message = "模块名不能为空")
    @Schema(description = "模块名", example = "门店管理")
    private String module;

    @Schema(description = "目标ID")
    private String targetId;

    @Schema(description = "目标类型")
    private String targetType;

    @Schema(description = "变更前值")
    private String oldValue;

    @Schema(description = "变更后值")
    private String newValue;

    @Schema(description = "IP地址")
    private String ip;

    @Schema(description = "User-Agent")
    private String userAgent;

    @Schema(description = "耗时(ms)")
    private Long duration;

    @Schema(description = "状态: 0成功 1失败")
    private Integer status;
}
