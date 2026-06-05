package com.zhongjitang.integration.service;

import com.zhongjitang.common.core.result.R;
import com.zhongjitang.integration.domain.dto.PromotionTrackRequest;
import com.zhongjitang.integration.domain.vo.PromotionFunnelVO;

import java.time.LocalDate;

public interface PromotionTrackService {

    R<Void> track(PromotionTrackRequest request);

    R<PromotionFunnelVO> getFunnel(Long technicianId, Long storeId, LocalDate startDate, LocalDate endDate);
}
