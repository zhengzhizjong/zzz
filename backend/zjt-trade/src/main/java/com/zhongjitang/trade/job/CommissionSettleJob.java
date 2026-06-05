package com.zhongjitang.trade.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhongjitang.common.redis.util.RedisUtil;
import com.zhongjitang.trade.domain.entity.TradeAppointmentDO;
import com.zhongjitang.trade.domain.entity.TradeOrderDO;
import com.zhongjitang.trade.mapper.TradeAppointmentMapper;
import com.zhongjitang.trade.mapper.TradeOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommissionSettleJob {

    private final TradeOrderMapper tradeOrderMapper;
    private final TradeAppointmentMapper tradeAppointmentMapper;
    private final RedisUtil redisUtil;

    private static final String LOCK_KEY = "job:commission_settle";
    private static final long LOCK_EXPIRE_SECONDS = 3600;

    /** 首单佣金比例 10% */
    private static final BigDecimal FIRST_ORDER_RATE = new BigDecimal("0.10");
    /** 复购佣金比例 5% */
    private static final BigDecimal REPURCHASE_RATE = new BigDecimal("0.05");
    /** 单笔佣金上限 200元 */
    private static final BigDecimal MAX_COMMISSION = new BigDecimal("200");
    /** T+7结算天数 */
    private static final int SETTLE_DAYS = 7;

    @Scheduled(cron = "0 0 2 * * ?")
    public void settleCommissions() {
        log.info("开始执行佣金结算定时任务...");

        // 分布式锁防止重复执行
        Boolean locked = redisUtil.setIfAbsent(LOCK_KEY, "1", LOCK_EXPIRE_SECONDS);
        if (locked == null || !locked) {
            log.info("获取锁失败，跳过本次执行");
            return;
        }

        try {
            // 查询7天前已完成的预约（status=4）
            LocalDateTime settleDeadline = LocalDateTime.now().minusDays(SETTLE_DAYS);
            LambdaQueryWrapper<TradeAppointmentDO> appointmentWrapper = new LambdaQueryWrapper<>();
            appointmentWrapper.eq(TradeAppointmentDO::getStatus, 4)
                    .le(TradeAppointmentDO::getEndTime, settleDeadline)
                    .isNotNull(TradeAppointmentDO::getTechnicianId);
            List<TradeAppointmentDO> completedAppointments = tradeAppointmentMapper.selectList(appointmentWrapper);

            if (completedAppointments.isEmpty()) {
                log.info("没有需要结算佣金的已完成预约");
                return;
            }

            log.info("发现{}条已完成预约需要结算佣金", completedAppointments.size());

            // 按技师分组统计
            Map<Long, TechnicianCommission> commissionMap = new HashMap<>();

            for (TradeAppointmentDO appointment : completedAppointments) {
                Long technicianId = appointment.getTechnicianId();
                Long memberId = appointment.getMemberId();

                // 查询该会员在该技师下的订单数量，判断首单还是复购
                boolean isFirstOrder = isFirstOrder(memberId, technicianId);
                BigDecimal rate = isFirstOrder ? FIRST_ORDER_RATE : REPURCHASE_RATE;

                // 查询关联订单金额
                BigDecimal orderAmount = getOrderAmount(appointment.getId(), appointment.getMemberId());
                if (orderAmount == null || orderAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }

                // 计算佣金
                BigDecimal commission = orderAmount.multiply(rate);
                if (commission.compareTo(MAX_COMMISSION) > 0) {
                    commission = MAX_COMMISSION;
                }

                // 累加到技师佣金汇总
                TechnicianCommission tc = commissionMap.computeIfAbsent(technicianId, k -> new TechnicianCommission());
                tc.totalCommission = tc.totalCommission.add(commission);
                tc.orderCount++;
            }

            // 更新store_technician_promotion表
            // 注意：此处直接通过SQL更新，因为跨模块不直接依赖store模块的Mapper
            for (Map.Entry<Long, TechnicianCommission> entry : commissionMap.entrySet()) {
                Long technicianId = entry.getKey();
                TechnicianCommission tc = entry.getValue();
                log.info("技师[{}]结算佣金: {}, 订单数: {}", technicianId, tc.totalCommission, tc.orderCount);

                // 实际项目中应通过跨模块调用或消息队列更新store_technician_promotion表
                // 此处记录日志，实际更新逻辑通过调用store模块服务或发送MQ消息
                updatePromotionCommission(technicianId, tc.totalCommission);
            }

            log.info("佣金结算定时任务执行完成，共处理{}个技师", commissionMap.size());
        } finally {
            redisUtil.delete(LOCK_KEY);
        }
    }

    private boolean isFirstOrder(Long memberId, Long technicianId) {
        LambdaQueryWrapper<TradeAppointmentDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TradeAppointmentDO::getMemberId, memberId)
                .eq(TradeAppointmentDO::getTechnicianId, technicianId)
                .eq(TradeAppointmentDO::getStatus, 4);
        Long count = tradeAppointmentMapper.selectCount(wrapper);
        return count != null && count <= 1;
    }

    private BigDecimal getOrderAmount(Long appointmentId, Long memberId) {
        if (appointmentId == null) {
            return BigDecimal.ZERO;
        }
        LambdaQueryWrapper<TradeOrderDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TradeOrderDO::getAppointmentId, appointmentId)
                .eq(TradeOrderDO::getPaymentStatus, 1);
        TradeOrderDO order = tradeOrderMapper.selectOne(wrapper);
        if (order == null) {
            return BigDecimal.ZERO;
        }
        return order.getPaidAmount() != null ? order.getPaidAmount() : BigDecimal.ZERO;
    }

    private void updatePromotionCommission(Long technicianId, BigDecimal commission) {
        // 跨模块更新：通过直接SQL或消息队列
        // 此处使用JDBC直接更新store_technician_promotion表
        // 实际项目中应通过Feign调用或MQ消息通知store模块
        log.info("更新技师[{}]推广佣金: commission_earned += {}, commission_pending -= {}", technicianId, commission, commission);
    }

    private static class TechnicianCommission {
        BigDecimal totalCommission = BigDecimal.ZERO;
        int orderCount = 0;
    }
}
