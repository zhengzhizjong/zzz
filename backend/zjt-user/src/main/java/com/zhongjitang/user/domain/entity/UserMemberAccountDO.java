package com.zhongjitang.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhongjitang.common.mybatis.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_member_account")
public class UserMemberAccountDO extends BaseDO {

    private Long memberId;

    private Integer accountType;

    private BigDecimal balance;

    private BigDecimal frozenAmount;

    @TableField(exist = false)
    private String accountId;

    @TableField(exist = false)
    private String openId;

    @TableField(exist = false)
    private String unionId;
}
