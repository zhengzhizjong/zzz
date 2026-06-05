package com.zhongjitang.ai.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhongjitang.common.mybatis.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_daily_report")
public class AiDailyReportDO extends BaseDO {

    private LocalDate reportDate;

    private Long storeId;

    private String reportType;

    private String summary;

    private String metricsJson;

    private String suggestions;
}
