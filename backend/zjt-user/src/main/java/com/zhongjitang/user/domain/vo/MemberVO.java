package com.zhongjitang.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "会员详情VO")
public class MemberVO {

    @Schema(description = "会员信息")
    private Object member;

    @Schema(description = "等级信息")
    private Object level;
}
