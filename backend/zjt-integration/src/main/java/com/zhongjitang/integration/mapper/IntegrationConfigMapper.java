package com.zhongjitang.integration.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhongjitang.integration.domain.entity.IntegrationConfigDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IntegrationConfigMapper extends BaseMapper<IntegrationConfigDO> {
}
