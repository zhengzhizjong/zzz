package com.zhongjitang.integration.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "创建线索请求")
public class LeadCreateRequest {

    @NotBlank(message = "来源平台不能为空")
    @Schema(description = "来源平台: wecom/douyin/meituan/other", example = "wecom")
    private String platform;

    @Schema(description = "平台线索ID")
    private String platformLeadId;

    @Schema(description = "来源渠道")
    private String sourceChannel;

    @Schema(description = "活动ID")
    private String campaignId;

    @Schema(description = "推荐人姓名")
    private String referrerName;

    @NotBlank(message = "客户姓名不能为空")
    @Schema(description = "客户姓名", example = "李四")
    private String customerName;

    @Schema(description = "客户电话", example = "13900139000")
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
}
