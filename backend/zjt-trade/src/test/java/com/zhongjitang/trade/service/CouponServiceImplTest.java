package com.zhongjitang.trade.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.trade.domain.dto.CouponCreateRequest;
import com.zhongjitang.trade.domain.entity.TradeCouponDO;
import com.zhongjitang.trade.domain.entity.TradeCouponRecordDO;
import com.zhongjitang.trade.mapper.TradeCouponMapper;
import com.zhongjitang.trade.mapper.TradeCouponRecordMapper;
import com.zhongjitang.trade.service.impl.CouponServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceImplTest {

    @Mock
    private TradeCouponMapper tradeCouponMapper;

    @Mock
    private TradeCouponRecordMapper tradeCouponRecordMapper;

    @InjectMocks
    private CouponServiceImpl couponService;

    private TradeCouponDO coupon;
    private CouponCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        coupon = new TradeCouponDO();
        coupon.setId(1L);
        coupon.setCouponName("新客满减券");
        coupon.setCouponType(1);
        coupon.setDiscountValue(new BigDecimal("50.00"));
        coupon.setMinAmount(new BigDecimal("200.00"));
        coupon.setTotalQuantity(100);
        coupon.setRemainingQuantity(100);
        coupon.setPerLimit(1);
        coupon.setStartTime(LocalDateTime.now().minusDays(1));
        coupon.setEndTime(LocalDateTime.now().plusDays(30));
        coupon.setStatus(1);

        createRequest = new CouponCreateRequest();
        createRequest.setCouponName("新客满减券");
        createRequest.setCouponType(1);
        createRequest.setDiscountValue(new BigDecimal("50.00"));
        createRequest.setMinAmount(new BigDecimal("200.00"));
        createRequest.setTotalQuantity(100);
        createRequest.setPerLimit(1);
        createRequest.setStartTime(LocalDateTime.now().minusDays(1));
        createRequest.setEndTime(LocalDateTime.now().plusDays(30));
    }

    @Test
    void testCreateSuccess() {
        when(tradeCouponMapper.insert(any(TradeCouponDO.class))).thenReturn(1);

        R<Void> result = couponService.create(createRequest);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        verify(tradeCouponMapper, times(1)).insert(any(TradeCouponDO.class));
    }

    @Test
    void testClaimSuccess() {
        when(tradeCouponMapper.selectById(1L)).thenReturn(coupon);
        when(tradeCouponRecordMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(tradeCouponRecordMapper.insert(any(TradeCouponRecordDO.class))).thenReturn(1);
        when(tradeCouponMapper.updateById(any(TradeCouponDO.class))).thenReturn(1);

        R<Void> result = couponService.claim(1L, 100L);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        verify(tradeCouponRecordMapper, times(1)).insert(any(TradeCouponRecordDO.class));
        verify(tradeCouponMapper, times(1)).updateById(any(TradeCouponDO.class));
    }

    @Test
    void testClaimCouponNotFound() {
        when(tradeCouponMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            couponService.claim(999L, 100L);
        });
        assertEquals(ErrorCode.NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void testClaimExceedLimit() {
        when(tradeCouponMapper.selectById(1L)).thenReturn(coupon);
        when(tradeCouponRecordMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            couponService.claim(1L, 100L);
        });
        assertEquals(ErrorCode.PARAM_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testClaimSoldOut() {
        coupon.setRemainingQuantity(0);
        when(tradeCouponMapper.selectById(1L)).thenReturn(coupon);
        when(tradeCouponRecordMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            couponService.claim(1L, 100L);
        });
        assertEquals(ErrorCode.PARAM_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testIssueSuccess() {
        when(tradeCouponMapper.selectById(1L)).thenReturn(coupon);
        when(tradeCouponRecordMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(tradeCouponRecordMapper.insert(any(TradeCouponRecordDO.class))).thenReturn(1);
        when(tradeCouponMapper.updateById(any(TradeCouponDO.class))).thenReturn(1);

        R<Void> result = couponService.issue(1L, Arrays.asList(100L, 101L));

        assertNotNull(result);
        assertEquals(0, result.getCode());
        verify(tradeCouponRecordMapper, times(2)).insert(any(TradeCouponRecordDO.class));
    }

    @Test
    void testUseSuccess() {
        TradeCouponRecordDO record = new TradeCouponRecordDO();
        record.setId(1L);
        record.setCouponId(1L);
        record.setMemberId(100L);
        record.setStatus(1); // 未使用
        record.setExpireAt(LocalDateTime.now().plusDays(30));

        when(tradeCouponRecordMapper.selectById(1L)).thenReturn(record);
        when(tradeCouponRecordMapper.updateById(any(TradeCouponRecordDO.class))).thenReturn(1);

        R<Void> result = couponService.use(1L, 1L);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        verify(tradeCouponRecordMapper, times(1)).updateById(any(TradeCouponRecordDO.class));
    }

    @Test
    void testUseAlreadyUsed() {
        TradeCouponRecordDO record = new TradeCouponRecordDO();
        record.setId(1L);
        record.setStatus(2); // 已使用

        when(tradeCouponRecordMapper.selectById(1L)).thenReturn(record);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            couponService.use(1L, 1L);
        });
        assertEquals(ErrorCode.PARAM_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testUseExpired() {
        TradeCouponRecordDO record = new TradeCouponRecordDO();
        record.setId(1L);
        record.setStatus(1); // 未使用
        record.setExpireAt(LocalDateTime.now().minusDays(1)); // 已过期

        when(tradeCouponRecordMapper.selectById(1L)).thenReturn(record);
        when(tradeCouponRecordMapper.updateById(any(TradeCouponRecordDO.class))).thenReturn(1);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            couponService.use(1L, 1L);
        });
        assertEquals(ErrorCode.PARAM_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testMyCouponsSuccess() {
        TradeCouponRecordDO record = new TradeCouponRecordDO();
        record.setId(1L);
        record.setCouponId(1L);
        record.setMemberId(100L);
        record.setStatus(1);

        when(tradeCouponRecordMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(record));

        R<java.util.List<TradeCouponRecordDO>> result = couponService.myCoupons(100L, 1);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertEquals(1, result.getData().size());
    }
}
