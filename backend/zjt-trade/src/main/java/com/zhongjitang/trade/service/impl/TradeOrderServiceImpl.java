package com.zhongjitang.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.trade.domain.entity.TradeAppointmentDO;
import com.zhongjitang.trade.domain.entity.TradeOrderDO;
import com.zhongjitang.trade.domain.entity.TradeOrderItemDO;
import com.zhongjitang.trade.mapper.TradeAppointmentMapper;
import com.zhongjitang.trade.mapper.TradeOrderItemMapper;
import com.zhongjitang.trade.mapper.TradeOrderMapper;
import com.zhongjitang.trade.service.ITradeOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeOrderServiceImpl implements ITradeOrderService {

    private final TradeOrderMapper tradeOrderMapper;
    private final TradeOrderItemMapper tradeOrderItemMapper;
    private final TradeAppointmentMapper tradeAppointmentMapper;

    private static final AtomicInteger ORDER_SEQUENCE = new AtomicInteger(1);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public R<PageResult<TradeOrderDO>> page(Integer page, Integer pageSize, Long storeId, Long memberId, Integer status) {
        Page<TradeOrderDO> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<TradeOrderDO> wrapper = new LambdaQueryWrapper<>();
        if (storeId != null) {
            wrapper.eq(TradeOrderDO::getStoreId, storeId);
        }
        if (memberId != null) {
            wrapper.eq(TradeOrderDO::getMemberId, memberId);
        }
        if (status != null) {
            wrapper.eq(TradeOrderDO::getPaymentStatus, status);
        }
        wrapper.orderByDesc(TradeOrderDO::getCreatedAt);
        Page<TradeOrderDO> result = tradeOrderMapper.selectPage(pageParam, wrapper);
        return R.ok(PageResult.of(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @Override
    public R<TradeOrderDO> getById(Long id) {
        TradeOrderDO order = tradeOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在，ID: " + id);
        }
        return R.ok(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<TradeOrderDO> createFromAppointment(Long appointmentId) {
        // 1. 查询预约
        TradeAppointmentDO appointment = tradeAppointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            throw new BusinessException(ErrorCode.APPOINTMENT_NOT_FOUND, "预约ID: " + appointmentId);
        }

        // 2. 检查是否已创建订单
        LambdaQueryWrapper<TradeOrderDO> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(TradeOrderDO::getAppointmentId, appointmentId)
                .ne(TradeOrderDO::getPaymentStatus, 5); // 排除已取消的订单
        Long existCount = tradeOrderMapper.selectCount(existWrapper);
        if (existCount > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "该预约已创建订单");
        }

        // 3. 生成订单编号: ORD + yyyyMMdd + 6位序号
        String orderNo = generateOrderNo();

        // 4. 创建订单
        TradeOrderDO order = new TradeOrderDO();
        order.setOrderNo(orderNo);
        order.setOrderType(1); // 预约订单
        order.setMemberId(appointment.getMemberId());
        order.setMemberName(appointment.getMemberName());
        order.setTotalAmount(BigDecimal.ZERO); // 初始金额为0，后续由明细计算
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setPaidAmount(BigDecimal.ZERO);
        order.setPointsDeductAmount(BigDecimal.ZERO);
        order.setPaymentStatus(1); // 待支付
        order.setAppointmentId(appointmentId);
        order.setTechnicianId(appointment.getTechnicianId());
        order.setSourceChannel(appointment.getSourceChannel());
        order.setStoreId(appointment.getStoreId());

        tradeOrderMapper.insert(order);

        // 5. 创建订单明细
        TradeOrderItemDO item = new TradeOrderItemDO();
        item.setOrderId(order.getId());
        item.setServiceItemId(appointment.getServiceItemId());
        item.setServiceItemName(appointment.getServiceItemName());
        item.setTechnicianId(appointment.getTechnicianId());
        item.setTechnicianName(appointment.getTechnicianName());
        item.setPrice(BigDecimal.ZERO); // 价格由服务项目确定，此处预留
        item.setQuantity(1);
        item.setSubtotal(BigDecimal.ZERO);
        item.setDurationMinutes(appointment.getDurationMinutes());
        item.setStoreId(appointment.getStoreId());

        tradeOrderItemMapper.insert(item);

        return R.ok(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> complete(Long id) {
        TradeOrderDO order = tradeOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在，ID: " + id);
        }
        // 只有已支付(2)或服务中(3)的订单可以完成
        if (!Arrays.asList(2, 3).contains(order.getPaymentStatus())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "当前订单状态不允许完成");
        }
        order.setPaymentStatus(4); // 已完成
        tradeOrderMapper.updateById(order);
        return R.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> cancel(Long id, String reason) {
        TradeOrderDO order = tradeOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在，ID: " + id);
        }
        // 只有待支付(1)的订单可以直接取消
        if (order.getPaymentStatus() != 1) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "当前订单状态不允许取消，请申请退款");
        }
        order.setPaymentStatus(5); // 已取消
        order.setRemark(reason);
        tradeOrderMapper.updateById(order);
        return R.ok();
    }

    private String generateOrderNo() {
        String datePart = LocalDate.now().format(DATE_FORMATTER);
        int seq = ORDER_SEQUENCE.getAndIncrement();
        if (seq > 999999) {
            ORDER_SEQUENCE.set(1);
            seq = 1;
        }
        return "ORD" + datePart + String.format("%06d", seq);
    }
}
