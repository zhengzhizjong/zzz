package com.zhongjitang.user.domain.entity;

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

    private String nickname;

    private String realName;

    private Integer gender;

    private LocalDate birthday;

    private String phone;

    private String avatarUrl;

    private Long levelId;

    private BigDecimal totalSpent;

    private Integer totalVisits;

    private LocalDateTime lastVisitAt;

    private String source;

    private Integer status;
}
