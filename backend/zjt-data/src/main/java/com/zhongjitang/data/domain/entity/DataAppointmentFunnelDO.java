package com.zhongjitang.data.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhongjitang.common.mybatis.entity.BaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("data_appointment_funnel")
@Schema(description = "预约漏斗埋点")
public class DataAppointmentFunnelDO extends BaseDO {

    @Schema(description = "会员ID")
    private Long memberId;

    @Schema(description = "会话ID，关联同一预约流程")
    private String sessionId;

    @Schema(description = "漏斗步骤: browse_store, select_tech, select_time, confirm, arrive, complete")
    private String step;

    @Schema(description = "步骤发生时间")
    private LocalDateTime stepTime;

    @Schema(description = "停留时长(毫秒)")
    private Integer durationMs;

    @Schema(description = "扩展信息JSON")
    private String extraJson;
}
