package com.zhongjitang.integration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.integration.domain.dto.PromotionTrackRequest;
import com.zhongjitang.integration.domain.entity.IntegrationPromotionTrackDO;
import com.zhongjitang.integration.domain.vo.PromotionFunnelVO;
import com.zhongjitang.integration.mapper.IntegrationPromotionTrackMapper;
import com.zhongjitang.integration.service.PromotionTrackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromotionTrackServiceImpl implements PromotionTrackService {

    private final IntegrationPromotionTrackMapper trackMapper;

    private static final List<String> FUNNEL_STEPS = Arrays.asList(
            "click", "register", "first_visit", "first_order", "repurchase"
    );

    private static final int ATTRIBUTION_WINDOW_DAYS = 30;

    @Override
    public R<Void> track(PromotionTrackRequest request) {
        String eventType = request.getEventType();
        String visitorId = buildVisitorId(request);

        // 30天归因窗口：查询30天内是否有同一visitorId的click记录
        if (!"click".equals(eventType)) {
            LocalDateTime windowStart = LocalDateTime.now().minusDays(ATTRIBUTION_WINDOW_DAYS);
            LambdaQueryWrapper<IntegrationPromotionTrackDO> clickWrapper = new LambdaQueryWrapper<>();
            clickWrapper.eq(IntegrationPromotionTrackDO::getVisitorId, visitorId)
                    .eq(IntegrationPromotionTrackDO::getEventType, "click")
                    .ge(IntegrationPromotionTrackDO::getEventTime, windowStart);
            Long clickCount = trackMapper.selectCount(clickWrapper);

            if (clickCount == 0) {
                log.info("访客[{}]在30天归因窗口内无click记录，跳过归因", visitorId);
                return R.ok();
            }
        }

        // 首次点击归因：如果已有归因记录则跳过
        if ("click".equals(eventType)) {
            LambdaQueryWrapper<IntegrationPromotionTrackDO> existWrapper = new LambdaQueryWrapper<>();
            existWrapper.eq(IntegrationPromotionTrackDO::getVisitorId, visitorId)
                    .eq(IntegrationPromotionTrackDO::getEventType, "click");
            Long existCount = trackMapper.selectCount(existWrapper);

            if (existCount > 0) {
                log.info("访客[{}]已有click归因记录，跳过重复归因", visitorId);
                return R.ok();
            }
        }

        // 记录追踪事件
        IntegrationPromotionTrackDO trackDO = new IntegrationPromotionTrackDO();
        trackDO.setPromoCode(request.getPromoCode());
        trackDO.setSourceTechnicianId(request.getTechnicianId());
        trackDO.setVisitorId(visitorId);
        trackDO.setEventType(eventType);
        trackDO.setEventTime(LocalDateTime.now());
        trackDO.setDeviceInfo(request.getUserAgent());
        trackMapper.insert(trackDO);

        return R.ok();
    }

    @Override
    public R<PromotionFunnelVO> getFunnel(Long technicianId, Long storeId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        LambdaQueryWrapper<IntegrationPromotionTrackDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(IntegrationPromotionTrackDO::getEventTime, startDateTime)
                .le(IntegrationPromotionTrackDO::getEventTime, endDateTime);
        if (technicianId != null) {
            wrapper.eq(IntegrationPromotionTrackDO::getSourceTechnicianId, technicianId);
        }
        if (storeId != null) {
            wrapper.eq(IntegrationPromotionTrackDO::getStoreId, storeId);
        }

        List<IntegrationPromotionTrackDO> records = trackMapper.selectList(wrapper);

        PromotionFunnelVO vo = new PromotionFunnelVO();

        // 设置统计周期
        PromotionFunnelVO.Period period = new PromotionFunnelVO.Period();
        period.setStartDate(startDate);
        period.setEndDate(endDate);
        vo.setPeriod(period);

        // 按eventType分组统计数量
        Map<String, Long> stepCounts = records.stream()
                .collect(Collectors.groupingBy(IntegrationPromotionTrackDO::getEventType, Collectors.counting()));

        // 构建漏斗数据
        Map<String, PromotionFunnelVO.FunnelStep> funnel = new LinkedHashMap<>();
        long firstStepCount = stepCounts.getOrDefault("click", 0L);
        for (String step : FUNNEL_STEPS) {
            PromotionFunnelVO.FunnelStep funnelStep = new PromotionFunnelVO.FunnelStep();
            long count = stepCounts.getOrDefault(step, 0L);
            funnelStep.setCount(count);
            funnelStep.setRate(firstStepCount > 0 ? round(count * 100.0 / firstStepCount) : 0.0);
            funnel.put(step, funnelStep);
        }
        vo.setFunnel(funnel);

        // 计算各步骤间转化率
        Map<String, Double> conversion = new LinkedHashMap<>();
        for (int i = 1; i < FUNNEL_STEPS.size(); i++) {
            String prevStep = FUNNEL_STEPS.get(i - 1);
            String currentStep = FUNNEL_STEPS.get(i);
            long prevCount = stepCounts.getOrDefault(prevStep, 0L);
            long currentCount = stepCounts.getOrDefault(currentStep, 0L);
            String key = prevStep + "_to_" + currentStep;
            conversion.put(key, prevCount > 0 ? round(currentCount * 100.0 / prevCount) : 0.0);
        }
        // 整体转化率: repurchase / click
        long repurchaseCount = stepCounts.getOrDefault("repurchase", 0L);
        conversion.put("overall", firstStepCount > 0 ? round(repurchaseCount * 100.0 / firstStepCount) : 0.0);
        vo.setConversion(conversion);

        // 按渠道统计（从deviceInfo中提取渠道信息，此处简化处理）
        vo.setByChannel(Collections.emptyList());

        return R.ok(vo);
    }

    private String buildVisitorId(PromotionTrackRequest request) {
        if (request.getCustomerId() != null) {
            return "customer_" + request.getCustomerId();
        }
        if (request.getCustomerIp() != null) {
            return "ip_" + request.getCustomerIp();
        }
        return "promo_" + request.getPromoCode() + "_" + System.currentTimeMillis();
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
