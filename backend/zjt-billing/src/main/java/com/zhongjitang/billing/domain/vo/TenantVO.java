package com.zhongjitang.billing.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "租户VO")
public class TenantVO {

    @Schema(description = "租户ID")
    private Long id;

    @Schema(description = "租户编号")
    private String tenantNo;

    @Schema(description = "租户名称")
    private String tenantName;

    @Schema(description = "联系人姓名")
    private String contactName;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "联系邮箱")
    private String contactEmail;

    @Schema(description = "状态:1试用 2正式 3欠费 4停用")
    private Integer status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "套餐编码")
    private String planCode;

    @Schema(description = "套餐名称")
    private String planName;

    @Schema(description = "套餐过期日期")
    private LocalDate planExpireDate;

    @Schema(description = "试用过期日期")
    private LocalDate trialExpireDate;

    @Schema(description = "是否启用白标")
    private Integer whiteLabelEnabled;

    @Schema(description = "品牌名称")
    private String brandName;

    @Schema(description = "品牌Logo")
    private String brandLogo;

    @Schema(description = "品牌主题色")
    private String brandTheme;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
