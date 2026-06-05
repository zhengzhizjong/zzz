package com.zhongjitang.store.service;

import com.zhongjitang.common.core.result.R;
import com.zhongjitang.store.domain.dto.ScheduleBatchCreateRequest;
import com.zhongjitang.store.domain.dto.ScheduleCreateRequest;
import com.zhongjitang.store.domain.dto.ScheduleUpdateRequest;
import com.zhongjitang.store.domain.entity.StoreScheduleDO;

import java.time.LocalDate;
import java.util.List;

public interface IScheduleService {

    R<List<StoreScheduleDO>> getByTechnicianAndMonth(Long technicianId, String month);

    R<List<StoreScheduleDO>> getByStoreAndDate(Long storeId, LocalDate date);

    R<Void> create(ScheduleCreateRequest request);

    R<Void> batchCreate(ScheduleBatchCreateRequest request);

    R<Void> update(Long id, ScheduleUpdateRequest request);

    R<Void> delete(Long id);
}
