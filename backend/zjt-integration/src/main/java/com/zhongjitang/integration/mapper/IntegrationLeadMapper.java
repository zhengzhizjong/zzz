package com.zhongjitang.integration.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhongjitang.integration.domain.entity.IntegrationLeadDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IntegrationLeadMapper extends BaseMapper<IntegrationLeadDO> {
}
