package com.zhongjitang.store.service;

import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.store.domain.dto.TechnicianCreateRequest;
import com.zhongjitang.store.domain.dto.TechnicianUpdateRequest;
import com.zhongjitang.store.domain.entity.StoreTechnicianDO;
import com.zhongjitang.store.domain.vo.TechnicianPerformanceVO;
import com.zhongjitang.store.domain.vo.TechnicianWorkspaceVO;

public interface IStoreTechnicianService {

    R<PageResult<StoreTechnicianDO>> page(Integer page, Integer pageSize, Long storeId,
                                          Integer status, Integer isOnline,
                                          Integer skillLevel, String skilledItems);

    R<StoreTechnicianDO> getById(Long id);

    R<Void> create(TechnicianCreateRequest request);

    R<Void> update(Long id, TechnicianUpdateRequest request);

    R<Void> updateStatus(Long id, Integer status);

    R<Void> checkIn(Long id);

    R<Void> checkOut(Long id);

    R<TechnicianWorkspaceVO> getWorkspace(Long technicianId);

    R<TechnicianPerformanceVO> getPerformance(Long technicianId);
}
