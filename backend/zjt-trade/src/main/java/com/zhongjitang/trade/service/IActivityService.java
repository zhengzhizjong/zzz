package com.zhongjitang.trade.service;

import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.trade.domain.dto.ActivityCreateRequest;
import com.zhongjitang.trade.domain.dto.ActivityUpdateRequest;
import com.zhongjitang.trade.domain.entity.TradeActivityDO;

import java.util.List;

public interface IActivityService {

    R<PageResult<TradeActivityDO>> page(Integer page, Integer pageSize, String keyword, Integer status);

    R<TradeActivityDO> getById(Long id);

    R<Void> create(ActivityCreateRequest request);

    R<Void> update(Long id, ActivityUpdateRequest request);

    R<Void> updateStatus(Long id, Integer status);

    R<List<TradeActivityDO>> getActiveList();
}
