package com.zhongjitang.billing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhongjitang.billing.domain.entity.BillingTenantDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BillingTenantMapper extends BaseMapper<BillingTenantDO> {
}
