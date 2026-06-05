package com.zhongjitang.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.trade.domain.dto.RefundApplyRequest;
import com.zhongjitang.trade.domain.entity.TradeOrderDO;
import com.zhongjitang.trade.domain.entity.TradePaymentDO;
import com.zhongjitang.trade.domain.entity.TradeRefundDO;
import com.zhongjitang.trade.mapper.TradeOrderMapper;
import com.zhongjitang.trade.mapper.TradePaymentMapper;
import com.zhongjitang.trade.mapper.TradeRefundMapper;
import com.zhongjitang.trade.service.IPaymentService;
import com.zhongjitang.trade.service.IRefundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundServiceImpl implements IRefundService {

    private final TradeRefundMapper tradeRefundMapper;
    private final TradeOrderMapper tradeOrderMapper;
    private final TradePaymentMapper tradePaymentMapper;
    private final IPaymentService paymentService;

    private static final AtomicInteger REFUND_SEQUENCE = new AtomicInteger(1);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<TradeRefundDO> apply(RefundApplyRequest request) {
        // 1. 查询订单
        TradeOrderDO order = tradeOrderMapper.selectById(request.getOrderId());
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在，ID: " + request.getOrderId());
        }

        // 2. 校验订单状态：只有已支付(2)、服务中(3)、已完成(4)的订单可以申请退款
        if (!Arrays.asList(2, 3, 4).contains(order.getPaymentStatus())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "当前订单状态不允许申请退款");
        }

        // 3. 校验退款金额
        if (request.getRefundAmount().compareTo(order.getPaidAmount()) > 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "退款金额不能大于实付金额");
        }

        // 4. 查询支付记录
        LambdaQueryWrapper<TradePaymentDO> paymentWrapper = new LambdaQueryWrapper<>();
        paymentWrapper.eq(TradePaymentDO::getOrderId, request.getOrderId())
                .eq(TradePaymentDO::getStatus, 2)
                .last("LIMIT 1");
        TradePaymentDO payment = tradePaymentMapper.selectOne(paymentWrapper);

        // 5. 生成退款编号: REF + yyyyMMdd + 6位序号
        String refundNo = generateRefundNo();

        // 6. 创建退款记录
        TradeRefundDO refund = new TradeRefundDO();
        refund.setRefundNo(refundNo);
        refund.setOrderId(request.getOrderId());
        refund.setPaymentId(payment != null ? payment.getId() : null);
        refund.setMemberId(order.getMemberId());
        refund.setRefundAmount(request.getRefundAmount());
        refund.setReason(request.getReason());
        refund.setStatus(1); // 待审核
        refund.setStoreId(order.getStoreId());

        tradeRefundMapper.insert(refund);

        return R.ok(refund);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> approve(Long id) {
        TradeRefundDO refund = tradeRefundMapper.selectById(id);
        if (refund == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "退款记录不存在，ID: " + id);
        }

        // 只有待审核(1)的退款可以审批通过
        if (refund.getStatus() != 1) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "当前退款状态不允许审批");
        }

        // 更新退款状态
        refund.setStatus(2); // 已通过
        refund.setApprovedBy("system"); // TODO: 从安全上下文获取审批人
        refund.setApprovedAt(LocalDateTime.now());
        tradeRefundMapper.updateById(refund);

        // 审批通过后自动调用退款接口
        if (refund.getPaymentId() != null) {
            try {
                paymentService.refund(refund.getPaymentId(), refund.getRefundAmount(), refund.getReason());
                refund.setStatus(4); // 已退款
                refund.setRefundedAt(LocalDateTime.now());
                tradeRefundMapper.updateById(refund);
            } catch (Exception e) {
                log.error("退款失败，refundId={}", id, e);
                refund.setStatus(5); // 退款失败
                tradeRefundMapper.updateById(refund);
            }
        }

        return R.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> reject(Long id, String rejectReason) {
        TradeRefundDO refund = tradeRefundMapper.selectById(id);
        if (refund == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "退款记录不存在，ID: " + id);
        }

        // 只有待审核(1)的退款可以拒绝
        if (refund.getStatus() != 1) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "当前退款状态不允许操作");
        }

        refund.setStatus(3); // 已拒绝
        refund.setApprovedBy("system"); // TODO: 从安全上下文获取审批人
        refund.setApprovedAt(LocalDateTime.now());
        refund.setRejectReason(rejectReason);
        tradeRefundMapper.updateById(refund);

        return R.ok();
    }

    @Override
    public R<TradeRefundDO> getById(Long id) {
        TradeRefundDO refund = tradeRefundMapper.selectById(id);
        if (refund == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "退款记录不存在，ID: " + id);
        }
        return R.ok(refund);
    }

    @Override
    public R<PageResult<TradeRefundDO>> page(Integer page, Integer pageSize, Long orderId, Long memberId, Integer status) {
        Page<TradeRefundDO> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<TradeRefundDO> wrapper = new LambdaQueryWrapper<>();
        if (orderId != null) {
            wrapper.eq(TradeRefundDO::getOrderId, orderId);
        }
        if (memberId != null) {
            wrapper.eq(TradeRefundDO::getMemberId, memberId);
        }
        if (status != null) {
            wrapper.eq(TradeRefundDO::getStatus, status);
        }
        wrapper.orderByDesc(TradeRefundDO::getCreatedAt);
        Page<TradeRefundDO> result = tradeRefundMapper.selectPage(pageParam, wrapper);
        return R.ok(PageResult.of(result.getRecords(), result.getTotal(), page, pageSize));
    }

    private String generateRefundNo() {
        String datePart = LocalDate.now().format(DATE_FORMATTER);
        int seq = REFUND_SEQUENCE.getAndIncrement();
        if (seq > 999999) {
            REFUND_SEQUENCE.set(1);
            seq = 1;
        }
        return "REF" + datePart + String.format("%06d", seq);
    }
}
