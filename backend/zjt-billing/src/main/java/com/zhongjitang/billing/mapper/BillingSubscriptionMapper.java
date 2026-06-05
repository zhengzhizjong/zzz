package com.zhongjitang.billing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhongjitang.billing.domain.entity.BillingSubscriptionDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BillingSubscriptionMapper extends BaseMapper<BillingSubscriptionDO> {
}
