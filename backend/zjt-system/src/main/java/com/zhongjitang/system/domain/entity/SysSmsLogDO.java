package com.zhongjitang.system.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhongjitang.common.mybatis.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_sms_log")
public class SysSmsLogDO extends BaseDO {

    /** 手机号 */
    private String phone;

    /** 模板编码 */
    private String templateCode;

    /** 模板参数(JSON) */
    @TableField(exist = false)
    private String templateParams;

    /** 短信内容 */
    private String content;

    /** 发送状态: 1发送中 2成功 3失败 */
    private Integer sendStatus;

    /** 发送结果 */
    @TableField(exist = false)
    private String sendResult;

    /** 业务类型 */
    @TableField(exist = false)
    private String bizType;

    /** 业务ID */
    private String bizId;

    /** 发送时间 */
    @TableField("send_time")
    private LocalDateTime sendTime;

    /** 回调编码 */
    private String callbackCode;
}
