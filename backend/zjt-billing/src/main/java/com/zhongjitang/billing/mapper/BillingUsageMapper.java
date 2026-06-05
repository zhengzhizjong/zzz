package com.zhongjitang.billing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhongjitang.billing.domain.entity.BillingUsageDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BillingUsageMapper extends BaseMapper<BillingUsageDO> {
}
