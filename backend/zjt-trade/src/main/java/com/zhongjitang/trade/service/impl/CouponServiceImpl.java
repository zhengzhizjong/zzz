package com.zhongjitang.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.trade.domain.dto.CouponCreateRequest;
import com.zhongjitang.trade.domain.entity.TradeCouponDO;
import com.zhongjitang.trade.domain.entity.TradeCouponRecordDO;
import com.zhongjitang.trade.mapper.TradeCouponMapper;
import com.zhongjitang.trade.mapper.TradeCouponRecordMapper;
import com.zhongjitang.trade.service.ICouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements ICouponService {

    private final TradeCouponMapper tradeCouponMapper;
    private final TradeCouponRecordMapper tradeCouponRecordMapper;

    @Override
    public R<PageResult<TradeCouponDO>> page(Integer page, Integer pageSize, Integer couponType, Integer status) {
        Page<TradeCouponDO> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<TradeCouponDO> wrapper = new LambdaQueryWrapper<>();
        if (couponType != null) {
            wrapper.eq(TradeCouponDO::getCouponType, couponType);
        }
        if (status != null) {
            wrapper.eq(TradeCouponDO::getStatus, status);
        }
        wrapper.orderByDesc(TradeCouponDO::getCreatedAt);
        Page<TradeCouponDO> result = tradeCouponMapper.selectPage(pageParam, wrapper);
        return R.ok(PageResult.of(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> create(CouponCreateRequest request) {
        TradeCouponDO coupon = new TradeCouponDO();
        coupon.setCouponName(request.getCouponName());
        coupon.setCouponType(request.getCouponType());
        coupon.setDiscountValue(request.getDiscountValue());
        coupon.setMinAmount(request.getMinAmount() != null ? request.getMinAmount() : java.math.BigDecimal.ZERO);
        coupon.setMaxDiscount(request.getMaxDiscount());
        coupon.setTotalQuantity(request.getTotalQuantity());
        coupon.setRemainingQuantity(request.getTotalQuantity());
        coupon.setPerLimit(request.getPerLimit() != null ? request.getPerLimit() : 1);
        coupon.setStartTime(request.getStartTime());
        coupon.setEndTime(request.getEndTime());
        coupon.setApplicableItems(request.getApplicableItems());
        coupon.setStatus(1); // 启用

        tradeCouponMapper.insert(coupon);
        return R.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> issue(Long couponId, List<Long> memberIds) {
        TradeCouponDO coupon = tradeCouponMapper.selectById(couponId);
        if (coupon == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "优惠券不存在，ID: " + couponId);
        }

        for (Long memberId : memberIds) {
            // 检查每人限领数量
            LambdaQueryWrapper<TradeCouponRecordDO> countWrapper = new LambdaQueryWrapper<>();
            countWrapper.eq(TradeCouponRecordDO::getCouponId, couponId)
                    .eq(TradeCouponRecordDO::getMemberId, memberId);
            Long count = tradeCouponRecordMapper.selectCount(countWrapper);
            if (count >= coupon.getPerLimit()) {
                log.warn("会员{}已达到优惠券{}的领取上限", memberId, couponId);
                continue;
            }

            // 检查剩余数量
            if (coupon.getRemainingQuantity() <= 0) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "优惠券已发完");
            }

            // 创建领取记录
            TradeCouponRecordDO record = new TradeCouponRecordDO();
            record.setCouponId(couponId);
            record.setMemberId(memberId);
            record.setStatus(1); // 未使用
            record.setExpireAt(coupon.getEndTime());
            tradeCouponRecordMapper.insert(record);

            // 扣减剩余数量
            coupon.setRemainingQuantity(coupon.getRemainingQuantity() - 1);
        }

        tradeCouponMapper.updateById(coupon);
        return R.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> claim(Long couponId, Long memberId) {
        TradeCouponDO coupon = tradeCouponMapper.selectById(couponId);
        if (coupon == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "优惠券不存在，ID: " + couponId);
        }

        // 校验优惠券状态
        if (coupon.getStatus() != 1) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "优惠券已禁用");
        }

        // 校验有效期
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getStartTime()) || now.isAfter(coupon.getEndTime())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "优惠券不在有效期内");
        }

        // 检查剩余数量
        if (coupon.getRemainingQuantity() <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "优惠券已领完");
        }

        // 检查每人限领数量
        LambdaQueryWrapper<TradeCouponRecordDO> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(TradeCouponRecordDO::getCouponId, couponId)
                .eq(TradeCouponRecordDO::getMemberId, memberId);
        Long count = tradeCouponRecordMapper.selectCount(countWrapper);
        if (count >= coupon.getPerLimit()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "已达到领取上限");
        }

        // 创建领取记录
        TradeCouponRecordDO record = new TradeCouponRecordDO();
        record.setCouponId(couponId);
        record.setMemberId(memberId);
        record.setStatus(1); // 未使用
        record.setExpireAt(coupon.getEndTime());
        tradeCouponRecordMapper.insert(record);

        // 扣减剩余数量
        coupon.setRemainingQuantity(coupon.getRemainingQuantity() - 1);
        tradeCouponMapper.updateById(coupon);

        return R.ok();
    }

    @Override
    public R<List<TradeCouponRecordDO>> myCoupons(Long memberId, Integer status) {
        LambdaQueryWrapper<TradeCouponRecordDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TradeCouponRecordDO::getMemberId, memberId);
        if (status != null) {
            wrapper.eq(TradeCouponRecordDO::getStatus, status);
        }
        wrapper.orderByDesc(TradeCouponRecordDO::getCreatedAt);
        List<TradeCouponRecordDO> records = tradeCouponRecordMapper.selectList(wrapper);
        return R.ok(records);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> use(Long recordId, Long orderId) {
        TradeCouponRecordDO record = tradeCouponRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "优惠券记录不存在，ID: " + recordId);
        }

        // 只有未使用(1)的优惠券可以使用
        if (record.getStatus() != 1) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "当前优惠券状态不允许使用");
        }

        // 检查是否过期
        if (record.getExpireAt() != null && LocalDateTime.now().isAfter(record.getExpireAt())) {
            record.setStatus(3); // 已过期
            tradeCouponRecordMapper.updateById(record);
            throw new BusinessException(ErrorCode.PARAM_ERROR, "优惠券已过期");
        }

        record.setStatus(2); // 已使用
        record.setOrderId(orderId);
        record.setUsedAt(LocalDateTime.now());
        tradeCouponRecordMapper.updateById(record);

        return R.ok();
    }
}
