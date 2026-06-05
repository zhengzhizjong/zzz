package com.zhongjitang.data.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;

@Data
@Schema(description = "漏斗埋点上报请求")
public class FunnelTrackRequest {

    @NotNull(message = "门店ID不能为空")
    @Schema(description = "门店ID", example = "1")
    private Long storeId;

    @NotBlank(message = "会话ID不能为空")
    @Schema(description = "会话ID，关联同一预约流程", example = "sess_abc123")
    private String sessionId;

    @NotBlank(message = "漏斗步骤不能为空")
    @Pattern(regexp = "browse_store|select_tech|select_time|confirm|arrive|complete", message = "step必须是: browse_store, select_tech, select_time, confirm, arrive, complete")
    @Schema(description = "漏斗步骤", example = "browse_store",
            allowableValues = {"browse_store", "select_tech", "select_time", "confirm", "arrive", "complete"})
    private String step;

    @Schema(description = "会员ID", example = "1001")
    private Long memberId;

    @Schema(description = "技师ID", example = "10")
    private Long technicianId;

    @Schema(description = "预约日期", example = "2026-06-04")
    private LocalDate date;

    @Schema(description = "预约时段", example = "10:00-11:00")
    private String timeSlot;

    @Schema(description = "扩展信息JSON")
    private String extraJson;
}
