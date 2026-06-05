package com.zhongjitang.integration.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhongjitang.common.mybatis.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("integration_promotion_track")
public class IntegrationPromotionTrackDO extends BaseDO {

    private String promoCode;

    private Long sourceTechnicianId;

    private String visitorId;

    private String eventType;

    private LocalDateTime eventTime;

    private String deviceInfo;
}
