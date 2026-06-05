package com.zhongjitang.data.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.data.domain.dto.FunnelTrackRequest;
import com.zhongjitang.data.domain.entity.DataAppointmentFunnelDO;
import com.zhongjitang.data.domain.vo.FunnelStatisticsVO;
import com.zhongjitang.data.mapper.DataAppointmentFunnelMapper;
import com.zhongjitang.data.service.AppointmentFunnelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentFunnelServiceImpl implements AppointmentFunnelService {

    private final DataAppointmentFunnelMapper funnelMapper;

    private static final List<String> FUNNEL_STEPS = Arrays.asList(
            "browse_store", "select_tech", "select_time", "confirm", "arrive", "complete"
    );

    @Override
    public R<Void> track(FunnelTrackRequest request) {
        DataAppointmentFunnelDO funnelDO = new DataAppointmentFunnelDO();
        funnelDO.setMemberId(request.getMemberId());
        funnelDO.setSessionId(request.getSessionId());
        funnelDO.setStep(request.getStep());
        funnelDO.setStepTime(LocalDateTime.now());
        funnelDO.setExtraJson(request.getExtraJson());

        funnelMapper.insert(funnelDO);
        return R.ok();
    }

    @Override
    public R<FunnelStatisticsVO> getStatistics(Long storeId, LocalDate startDate, LocalDate endDate, String groupBy) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        LambdaQueryWrapper<DataAppointmentFunnelDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(DataAppointmentFunnelDO::getStepTime, startDateTime)
                .le(DataAppointmentFunnelDO::getStepTime, endDateTime);
        if (storeId != null) {
            wrapper.eq(DataAppointmentFunnelDO::getStoreId, storeId);
        }

        List<DataAppointmentFunnelDO> records = funnelMapper.selectList(wrapper);

        FunnelStatisticsVO vo = new FunnelStatisticsVO();

        // 设置统计周期
        FunnelStatisticsVO.Period period = new FunnelStatisticsVO.Period();
        period.setStartDate(startDate);
        period.setEndDate(endDate);
        vo.setPeriod(period);

        // 按step分组统计数量
        Map<String, Long> stepCounts = records.stream()
                .collect(Collectors.groupingBy(DataAppointmentFunnelDO::getStep, Collectors.counting()));

        // 构建漏斗数据
        Map<String, FunnelStatisticsVO.FunnelStep> funnel = new LinkedHashMap<>();
        long firstStepCount = stepCounts.getOrDefault(FUNNEL_STEPS.get(0), 0L);
        for (String step : FUNNEL_STEPS) {
            FunnelStatisticsVO.FunnelStep funnelStep = new FunnelStatisticsVO.FunnelStep();
            long count = stepCounts.getOrDefault(step, 0L);
            funnelStep.setCount(count);
            funnelStep.setRate(firstStepCount > 0 ? round(count * 100.0 / firstStepCount) : 0.0);
            funnel.put(step, funnelStep);
        }
        vo.setFunnel(funnel);

        // 计算转化率
        Map<String, Double> conversion = new LinkedHashMap<>();
        for (int i = 1; i < FUNNEL_STEPS.size(); i++) {
            String prevStep = FUNNEL_STEPS.get(i - 1);
            String currentStep = FUNNEL_STEPS.get(i);
            long prevCount = stepCounts.getOrDefault(prevStep, 0L);
            long currentCount = stepCounts.getOrDefault(currentStep, 0L);
            String key = prevStep + "_to_" + currentStep;
            conversion.put(key, prevCount > 0 ? round(currentCount * 100.0 / prevCount) : 0.0);
        }
        // 整体转化率: complete / browse_store
        long completeCount = stepCounts.getOrDefault("complete", 0L);
        conversion.put("overall", firstStepCount > 0 ? round(completeCount * 100.0 / firstStepCount) : 0.0);
        vo.setConversion(conversion);

        // 按维度分组统计
        if (StringUtils.hasText(groupBy)) {
            List<FunnelStatisticsVO.FunnelGroup> groups = buildGroups(records, groupBy);
            vo.setGroups(groups);
        }

        return R.ok(vo);
    }

    private List<FunnelStatisticsVO.FunnelGroup> buildGroups(List<DataAppointmentFunnelDO> records, String groupBy) {
        Map<String, List<DataAppointmentFunnelDO>> groupedRecords;
        switch (groupBy) {
            case "store":
                groupedRecords = records.stream()
                        .collect(Collectors.groupingBy(r -> String.valueOf(r.getStoreId())));
                break;
            case "technician":
                groupedRecords = records.stream()
                        .filter(r -> r.getExtraJson() != null)
                        .collect(Collectors.groupingBy(r -> extractFromExtraJson(r.getExtraJson(), "technicianId")));
                break;
            case "date":
                groupedRecords = records.stream()
                        .collect(Collectors.groupingBy(r -> r.getStepTime().toLocalDate().toString()));
                break;
            case "channel":
                groupedRecords = records.stream()
                        .filter(r -> r.getExtraJson() != null)
                        .collect(Collectors.groupingBy(r -> extractFromExtraJson(r.getExtraJson(), "channel")));
                break;
            default:
                return Collections.emptyList();
        }

        List<FunnelStatisticsVO.FunnelGroup> groups = new ArrayList<>();
        for (Map.Entry<String, List<DataAppointmentFunnelDO>> entry : groupedRecords.entrySet()) {
            FunnelStatisticsVO.FunnelGroup group = new FunnelStatisticsVO.FunnelGroup();
            group.setDimension(groupBy);
            group.setDimensionValue(entry.getKey());

            Map<String, Long> stepCounts = entry.getValue().stream()
                    .collect(Collectors.groupingBy(DataAppointmentFunnelDO::getStep, Collectors.counting()));

            group.setBrowseStoreCount(stepCounts.getOrDefault("browse_store", 0L));
            group.setSelectTechCount(stepCounts.getOrDefault("select_tech", 0L));
            group.setSelectTimeCount(stepCounts.getOrDefault("select_time", 0L));
            group.setConfirmCount(stepCounts.getOrDefault("confirm", 0L));
            group.setArriveCount(stepCounts.getOrDefault("arrive", 0L));
            group.setCompleteCount(stepCounts.getOrDefault("complete", 0L));

            long browseCount = group.getBrowseStoreCount();
            long completeCnt = group.getCompleteCount();
            group.setOverallConversion(browseCount > 0 ? round(completeCnt * 100.0 / browseCount) : 0.0);

            groups.add(group);
        }
        return groups;
    }

    private String extractFromExtraJson(String extraJson, String key) {
        if (extraJson == null) {
            return "unknown";
        }
        try {
            String searchKey = "\"" + key + "\"";
            int keyIndex = extraJson.indexOf(searchKey);
            if (keyIndex < 0) {
                return "unknown";
            }
            int colonIndex = extraJson.indexOf(":", keyIndex);
            int valueStart = colonIndex + 1;
            while (valueStart < extraJson.length() && extraJson.charAt(valueStart) == ' ') {
                valueStart++;
            }
            int valueEnd = valueStart;
            if (valueStart < extraJson.length() && extraJson.charAt(valueStart) == '"') {
                valueStart++;
                valueEnd = extraJson.indexOf("\"", valueStart);
                if (valueEnd < 0) {
                    valueEnd = extraJson.length();
                }
            } else {
                while (valueEnd < extraJson.length() && extraJson.charAt(valueEnd) != ',' && extraJson.charAt(valueEnd) != '}') {
                    valueEnd++;
                }
            }
            return extraJson.substring(valueStart, valueEnd).trim();
        } catch (Exception e) {
            return "unknown";
        }
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
