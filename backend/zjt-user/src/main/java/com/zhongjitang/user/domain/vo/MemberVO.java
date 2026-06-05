package com.zhongjitang.user.domain.vo;

import com.zhongjitang.user.domain.entity.UserMemberDO;
import com.zhongjitang.user.domain.entity.UserMemberLevelDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "会员详情VO")
public class MemberVO {

    @Schema(description = "会员信息")
    private UserMemberDO member;

    @Schema(description = "等级信息")
    private UserMemberLevelDO level;

    @Schema(description = "累计消费")
    private BigDecimal totalSpent;

    @Schema(description = "累计到店次数")
    private Integer totalVisits;
}
