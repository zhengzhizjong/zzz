package com.zhongjitang.ai.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhongjitang.common.mybatis.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_daily_report")
public class AiDailyReportDO extends BaseDO {

    private LocalDate reportDate;

    private String reportNo;

    private String summary;

    private String highlights;

    private String concerns;

    private String suggestions;

    private String metricsJson;

    private String aiInsight;

    private Integer pushStatus;

    private LocalDateTime pushTime;

    private String pushChannels;

    @TableField(exist = false)
    private String reportType;
}
