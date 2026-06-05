package com.zhongjitang.store.service;

import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.store.domain.entity.StoreTechnicianPromotionDO;
import com.zhongjitang.store.domain.vo.CommissionVO;
import com.zhongjitang.store.domain.vo.PromotionRankingVO;
import com.zhongjitang.store.domain.vo.PromotionStatsVO;

import java.time.LocalDate;

public interface ITechnicianPromotionService {

    R<StoreTechnicianPromotionDO> getPromotion(Long technicianId);

    R<Void> generatePromoCode(Long technicianId);

    R<PromotionStatsVO> getStats(Long technicianId, LocalDate startDate, LocalDate endDate);

    R<PageResult<CommissionVO>> getCommissions(Long technicianId, String status, Integer page, Integer pageSize);

    R<PromotionRankingVO> getRanking(Long technicianId, Long storeId, String period);
}
