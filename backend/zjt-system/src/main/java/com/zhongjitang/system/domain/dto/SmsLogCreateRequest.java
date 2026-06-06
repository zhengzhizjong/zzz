package com.zhongjitang.system.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "短信日志创建请求")
public class SmsLogCreateRequest {

    @NotBlank(message = "手机号不能为空")
    @Schema(description = "手机号", example = "13900139000")
    private String phone;

    @Schema(description = "模板编码")
    private String templateCode;

    @Schema(description = "模板参数(JSON)")
    private String templateParams;

    @Schema(description = "短信内容")
    private String content;

    @Schema(description = "发送状态: 1发送中 2成功 3失败", example = "1")
    private Integer sendStatus;

    @Schema(description = "发送结果")
    private String sendResult;

    @Schema(description = "业务类型")
    private String bizType;

    @Schema(description = "业务ID")
    private String bizId;
}
