package com.zhongjitang.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhongjitang.common.mybatis.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_member_account")
public class UserMemberAccountDO extends BaseDO {

    private Long memberId;

    private String accountType;

    private String accountId;

    private String accessToken;

    private String refreshToken;

    private LocalDateTime expiresAt;

    private String unionId;

    private String openId;

    @TableField(exist = false)
    private Long storeId;
}
