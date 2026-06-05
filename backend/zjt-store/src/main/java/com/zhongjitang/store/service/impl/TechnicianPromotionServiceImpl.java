package com.zhongjitang.store.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.store.domain.entity.StoreTechnicianDO;
import com.zhongjitang.store.domain.entity.StoreTechnicianPromotionDO;
import com.zhongjitang.store.domain.vo.CommissionVO;
import com.zhongjitang.store.domain.vo.PromotionRankingVO;
import com.zhongjitang.store.domain.vo.PromotionStatsVO;
import com.zhongjitang.store.mapper.StoreTechnicianMapper;
import com.zhongjitang.store.mapper.StoreTechnicianPromotionMapper;
import com.zhongjitang.store.service.ITechnicianPromotionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TechnicianPromotionServiceImpl implements ITechnicianPromotionService {

    private final StoreTechnicianPromotionMapper promotionMapper;
    private final StoreTechnicianMapper technicianMapper;

    @Override
    public R<StoreTechnicianPromotionDO> getPromotion(Long technicianId) {
        StoreTechnicianPromotionDO promotion = findOrCreatePromotion(technicianId);
        return R.ok(promotion);
    }

    @Override
    public R<Void> generatePromoCode(Long technicianId) {
        StoreTechnicianDO technician = technicianMapper.selectById(technicianId);
        if (technician == null) {
            throw new BusinessException(ErrorCode.TECHNICIAN_NOT_FOUND, "技师ID: " + technicianId);
        }

        StoreTechnicianPromotionDO promotion = findOrCreatePromotion(technicianId);

        // 生成推广码: ZJT+门店后2位+技师后2位+2位随机校验码
        String storeSuffix = technician.getStoreId() != null
                ? String.format("%02d", technician.getStoreId() % 100)
                : "00";
        String techSuffix = String.format("%02d", technicianId % 100);
        String checkCode = String.format("%02d", ThreadLocalRandom.current().nextInt(100));
        String promoCode = "ZJT" + storeSuffix + techSuffix + checkCode;

        promotion.setPromoCode(promoCode);
        promotion.setShareLink("/pages/promotion?code=" + promoCode);
        promotionMapper.updateById(promotion);

        return R.ok();
    }

    @Override
    public R<PromotionStatsVO> getStats(Long technicianId, LocalDate startDate, LocalDate endDate) {
        StoreTechnicianPromotionDO promotion = findOrCreatePromotion(technicianId);

        PromotionStatsVO vo = new PromotionStatsVO();
        vo.setTechnicianId(technicianId);

        // 设置统计周期
        PromotionStatsVO.Period period = new PromotionStatsVO.Period();
        period.setStartDate(startDate);
        period.setEndDate(endDate);
        vo.setPeriod(period);

        // 构建指标（从promotion表获取汇总数据）
        PromotionStatsVO.Metrics metrics = new PromotionStatsVO.Metrics();
        metrics.setTotalClicks(Long.valueOf(promotion.getClickCount() != null ? promotion.getClickCount() : 0));
        metrics.setTotalViews(Long.valueOf(promotion.getVisitCount() != null ? promotion.getVisitCount() : 0));
        metrics.setTotalAppointments(Long.valueOf(promotion.getOrderCount() != null ? promotion.getOrderCount() : 0));
        metrics.setCompletedAppointments(Long.valueOf(promotion.getOrderCount() != null ? promotion.getOrderCount() : 0));
        metrics.setConversionRate(metrics.getTotalClicks() > 0
                ? Math.round(metrics.getTotalAppointments() * 10000.0 / metrics.getTotalClicks()) / 100.0
                : 0.0);
        metrics.setTotalCommission(promotion.getCommissionEarned() != null ? promotion.getCommissionEarned() : BigDecimal.ZERO);
        vo.setMetrics(metrics);

        // 构建每日趋势（简化实现，按日期范围生成空趋势）
        List<PromotionStatsVO.DailyTrend> dailyTrends = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            PromotionStatsVO.DailyTrend trend = new PromotionStatsVO.DailyTrend();
            trend.setDate(current);
            trend.setClicks(0L);
            trend.setViews(0L);
            trend.setAppointments(0L);
            dailyTrends.add(trend);
            current = current.plusDays(1);
        }
        vo.setDailyTrend(dailyTrends);

        return R.ok(vo);
    }

    @Override
    public R<PageResult<CommissionVO>> getCommissions(Long technicianId, String status, Integer page, Integer pageSize) {
        // 佣金明细从trade_order关联查询，此处返回空列表占位
        // 实际实现需要跨模块查询trade_order表
        List<CommissionVO> list = Collections.emptyList();
        return R.ok(PageResult.of(list, 0L, page, pageSize));
    }

    @Override
    public R<PromotionRankingVO> getRanking(Long technicianId, Long storeId, String period) {
        PromotionRankingVO vo = new PromotionRankingVO();
        vo.setPeriod(period);

        // 查询同门店下所有技师的推广数据并排序
        LambdaQueryWrapper<StoreTechnicianPromotionDO> wrapper = new LambdaQueryWrapper<>();
        if (storeId != null) {
            wrapper.eq(StoreTechnicianPromotionDO::getStoreId, storeId);
        }
        wrapper.eq(StoreTechnicianPromotionDO::getStatus, 1);
        wrapper.orderByDesc(StoreTechnicianPromotionDO::getCommissionEarned);

        List<StoreTechnicianPromotionDO> allPromotions = promotionMapper.selectList(wrapper);

        List<PromotionRankingVO.RankItem> rankingList = new ArrayList<>();
        PromotionRankingVO.RankItem myRankItem = null;
        int rank = 1;
        for (StoreTechnicianPromotionDO p : allPromotions) {
            PromotionRankingVO.RankItem item = new PromotionRankingVO.RankItem();
            item.setRank(rank);
            item.setTechnicianId(p.getTechnicianId());
            item.setTechnicianName(null); // 需关联查询技师姓名
            item.setAvatar(null);
            item.setTotalPromoted(p.getOrderCount() != null ? p.getOrderCount() : 0);
            item.setTotalCommission(p.getCommissionEarned() != null ? p.getCommissionEarned() : BigDecimal.ZERO);
            rankingList.add(item);

            if (p.getTechnicianId().equals(technicianId)) {
                myRankItem = item;
            }
            rank++;
        }

        vo.setMyRank(myRankItem);
        vo.setRanking(rankingList);

        return R.ok(vo);
    }

    private StoreTechnicianPromotionDO findOrCreatePromotion(Long technicianId) {
        LambdaQueryWrapper<StoreTechnicianPromotionDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StoreTechnicianPromotionDO::getTechnicianId, technicianId);
        StoreTechnicianPromotionDO promotion = promotionMapper.selectOne(wrapper);

        if (promotion == null) {
            promotion = new StoreTechnicianPromotionDO();
            promotion.setTechnicianId(technicianId);
            promotion.setClickCount(0);
            promotion.setRegisterCount(0);
            promotion.setVisitCount(0);
            promotion.setOrderCount(0);
            promotion.setCommissionEarned(BigDecimal.ZERO);
            promotion.setCommissionPending(BigDecimal.ZERO);
            promotion.setStatus(1);
            promotionMapper.insert(promotion);
        }

        return promotion;
    }
}
