package com.zhongjitang.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhongjitang.user.domain.entity.UserMemberDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMemberMapper extends BaseMapper<UserMemberDO> {
}
