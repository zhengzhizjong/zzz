package com.zhongjitang.trade.service;

import com.zhongjitang.common.core.result.R;
import com.zhongjitang.trade.domain.vo.AvailableSlotVO;

import java.time.LocalDate;

public interface AppointmentSlotService {

    R<AvailableSlotVO> getAvailableSlots(Long storeId, Long technicianId, LocalDate date);
}
