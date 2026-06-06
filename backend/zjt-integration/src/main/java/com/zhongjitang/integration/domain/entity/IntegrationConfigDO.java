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

    /** 平台: wecom/douyin/meituan/other */
    private String platform;

    /** 配置名称 */
    @TableField(exist = false)
    private String configName;

    /** 平台名称 */
    private String platformName;

    /** 应用ID */
    private String appId;

    /** 应用密钥 */
    private String appSecret;

    /** 授权访问令牌 */
    private String authAccessToken;

    /** 授权过期时间 */
    private LocalDateTime authExpiredAt;

    /** 刷新令牌 */
    private String refreshToken;

    /** 回调地址 */
    private String callbackUrl;

    /** 回调令牌 */
    private String callbackToken;

    /** 回调AES密钥 */
    private String callbackAesKey;

    /** 扩展配置(JSON) */
    private String extraConfig;

    /** 启用模块(JSON) */
    private String enabledModules;

    /** 状态: 1启用 2停用 */
    private Integer status;

    /** 最后同步时间 */
    private LocalDateTime lastSyncTime;
}
