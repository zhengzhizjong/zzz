package com.zhongjitang.ai.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "话术响应")
public class ScriptResponse {

    @Schema(description = "话术内容")
    private String script;

    @Schema(description = "话术技巧提示")
    private List<String> tips;

    @Schema(description = "场景类型")
    private String sceneType;
}
