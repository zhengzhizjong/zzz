package com.zhongjitang.store.service;

import com.zhongjitang.common.core.result.R;
import com.zhongjitang.store.domain.dto.TimeSlotConfigCreateRequest;
import com.zhongjitang.store.domain.dto.TimeSlotConfigUpdateRequest;
import com.zhongjitang.store.domain.entity.StoreTimeSlotConfigDO;
import com.zhongjitang.store.domain.vo.TimeSlotVO;

import java.time.LocalDate;
import java.util.List;

public interface ITimeSlotService {

    R<StoreTimeSlotConfigDO> getByStoreId(Long storeId);

    R<Void> create(TimeSlotConfigCreateRequest request);

    R<Void> update(Long id, TimeSlotConfigUpdateRequest request);

    R<List<TimeSlotVO>> getAvailableSlots(Long storeId, Long technicianId, LocalDate date);
}
