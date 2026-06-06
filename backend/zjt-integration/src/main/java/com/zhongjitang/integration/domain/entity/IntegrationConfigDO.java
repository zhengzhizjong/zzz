package com.zhongjitang.integration.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhongjitang.common.mybatis.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("integration_config")
public class IntegrationConfigDO extends BaseDO {

    @TableField(exist = false)
    private Long storeId;

    /** 平台: wecom/douyin/meituan/other */
    private String platform;

    /** 配置名称 */
    private String configName;

    /** 应用ID */
    private String appId;

    /** 应用密钥 */
    private String appSecret;

    /** 回调地址 */
    private String callbackUrl;

    /** 扩展配置(JSON) */
    private String extraConfig;

    /** 状态: 1启用 2停用 */
    private Integer status;

    /** 最后同步时间 */
    private LocalDateTime lastSyncAt;
}
