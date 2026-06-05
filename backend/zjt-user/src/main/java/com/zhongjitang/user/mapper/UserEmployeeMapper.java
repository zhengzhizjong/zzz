package com.zhongjitang.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhongjitang.user.domain.entity.UserEmployeeDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserEmployeeMapper extends BaseMapper<UserEmployeeDO> {
}
