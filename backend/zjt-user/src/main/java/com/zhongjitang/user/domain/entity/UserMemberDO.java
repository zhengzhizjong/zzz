package com.zhongjitang.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhongjitang.common.mybatis.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_member")
public class UserMemberDO extends BaseDO {

    private String memberNo;

    @TableField(exist = false)
    private String nickname;

    @TableField(exist = false)
    private String realName;

    @TableField("name")
    private String name;

    private String phone;

    private Integer gender;

    private LocalDate birthday;

    @TableField("avatar")
    private String avatarUrl;

    @TableField("constitution_type")
    private String constitutionType;

    @TableField("constitution_score")
    private BigDecimal constitutionScore;

    @TableField("constitution_time")
    private LocalDateTime constitutionTime;

    @TableField("store_id")
    private Long storeId;

    @TableField("member_level_id")
    private Long levelId;

    @TableField("member_type")
    private Integer memberType;

    @TableField("points")
    private Integer points;

    @TableField("balance")
    private BigDecimal balance;

    @TableField("source_channel")
    private String source;

    @TableField("referrer_id")
    private Long referrerId;

    private Integer status;

    @TableField("last_visit_time")
    private LocalDateTime lastVisitAt;

    @TableField("visit_count")
    private Integer totalVisits;

    @TableField(exist = false)
    private BigDecimal totalSpent;
}
