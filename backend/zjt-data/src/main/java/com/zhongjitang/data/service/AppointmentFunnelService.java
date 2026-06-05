package com.zhongjitang.data.service;

import com.zhongjitang.common.core.result.R;
import com.zhongjitang.data.domain.dto.FunnelTrackRequest;
import com.zhongjitang.data.domain.vo.FunnelStatisticsVO;

import java.time.LocalDate;

public interface AppointmentFunnelService {

    R<Void> track(FunnelTrackRequest request);

    R<FunnelStatisticsVO> getStatistics(Long storeId, LocalDate startDate, LocalDate endDate, String groupBy);
}
