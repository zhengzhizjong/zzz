package com.zhongjitang.billing.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "创建租户请求")
public class TenantCreateRequest {

    @NotBlank(message = "租户名称不能为空")
    @Schema(description = "租户名称", example = "忠济堂国医馆")
    private String tenantName;

    @Schema(description = "联系人姓名", example = "张三")
    private String contactName;

    @Schema(description = "联系电话", example = "13800138000")
    private String contactPhone;

    @Schema(description = "联系邮箱", example = "admin@zhongjitang.com")
    private String contactEmail;

    @Schema(description = "套餐编码", example = "basic")
    private String planCode;

    @Schema(description = "品牌名称")
    private String brandName;
}
