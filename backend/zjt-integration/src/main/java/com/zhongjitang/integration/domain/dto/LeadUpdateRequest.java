package com.zhongjitang.integration.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "更新线索请求")
public class LeadUpdateRequest {

    @Schema(description = "客户姓名")
    private String customerName;

    @Schema(description = "客户电话")
    private String customerPhone;

    @Schema(description = "客户性别:0女 1男 2未知")
    private Integer customerGender;

    @Schema(description = "客户年龄")
    private Integer customerAge;

    @Schema(description = "客户意向")
    private String customerIntent;

    @Schema(description = "咨询内容")
    private String consultationContent;

    @Schema(description = "线索分类:1A类 2B类 3C类")
    private Integer leadClass;

    @Schema(description = "跟进状态:1待跟进 2跟进中 3已转化 4无效")
    private Integer followStatus;

    @Schema(description = "AI摘要")
    private String aiSummary;
}
