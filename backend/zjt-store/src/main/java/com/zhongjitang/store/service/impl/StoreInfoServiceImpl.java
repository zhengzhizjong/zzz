package com.zhongjitang.store.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.store.domain.dto.StoreCreateRequest;
import com.zhongjitang.store.domain.dto.StoreUpdateRequest;
import com.zhongjitang.store.domain.entity.StoreInfoDO;
import com.zhongjitang.store.mapper.StoreInfoMapper;
import com.zhongjitang.store.service.IStoreInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class StoreInfoServiceImpl implements IStoreInfoService {

    private final StoreInfoMapper storeInfoMapper;

    private static final AtomicInteger SEQUENCE = new AtomicInteger(1);

    @Override
    public R<PageResult<StoreInfoDO>> page(Integer page, Integer pageSize, String keyword, Integer status) {
        Page<StoreInfoDO> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<StoreInfoDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(StoreInfoDO::getStoreName, keyword)
                    .or().like(StoreInfoDO::getStoreNo, keyword)
                    .or().like(StoreInfoDO::getContactPhone, keyword));
        }
        if (status != null) {
            wrapper.eq(StoreInfoDO::getStatus, status);
        }
        wrapper.orderByDesc(StoreInfoDO::getCreatedAt);
        Page<StoreInfoDO> result = storeInfoMapper.selectPage(pageParam, wrapper);
        return R.ok(PageResult.of(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @Override
    public R<StoreInfoDO> getById(Long id) {
        StoreInfoDO storeInfo = storeInfoMapper.selectById(id);
        if (storeInfo == null) {
            throw new BusinessException(ErrorCode.STORE_NOT_FOUND, "门店ID: " + id);
        }
        return R.ok(storeInfo);
    }

    @Override
    public R<Void> create(StoreCreateRequest request) {
        StoreInfoDO storeInfo = new StoreInfoDO();
        storeInfo.setStoreNo(generateStoreNo());
        storeInfo.setStoreName(request.getStoreName());
        storeInfo.setStoreType(request.getStoreType());
        storeInfo.setProvinceCode(request.getProvinceCode());
        storeInfo.setCityCode(request.getCityCode());
        storeInfo.setDistrictCode(request.getDistrictCode());
        storeInfo.setAddress(request.getAddress());
        storeInfo.setLatitude(request.getLatitude());
        storeInfo.setLongitude(request.getLongitude());
        storeInfo.setContactName(request.getContactName());
        storeInfo.setContactPhone(request.getContactPhone());
        storeInfo.setBusinessStartTime(request.getBusinessStartTime());
        storeInfo.setBusinessEndTime(request.getBusinessEndTime());
        storeInfo.setStatus(1);
        storeInfoMapper.insert(storeInfo);
        return R.ok();
    }

    @Override
    public R<Void> update(Long id, StoreUpdateRequest request) {
        StoreInfoDO storeInfo = storeInfoMapper.selectById(id);
        if (storeInfo == null) {
            throw new BusinessException(ErrorCode.STORE_NOT_FOUND, "门店ID: " + id);
        }
        if (request.getStoreName() != null) {
            storeInfo.setStoreName(request.getStoreName());
        }
        if (request.getStoreType() != null) {
            storeInfo.setStoreType(request.getStoreType());
        }
        if (request.getProvinceCode() != null) {
            storeInfo.setProvinceCode(request.getProvinceCode());
        }
        if (request.getCityCode() != null) {
            storeInfo.setCityCode(request.getCityCode());
        }
        if (request.getDistrictCode() != null) {
            storeInfo.setDistrictCode(request.getDistrictCode());
        }
        if (request.getAddress() != null) {
            storeInfo.setAddress(request.getAddress());
        }
        if (request.getLatitude() != null) {
            storeInfo.setLatitude(request.getLatitude());
        }
        if (request.getLongitude() != null) {
            storeInfo.setLongitude(request.getLongitude());
        }
        if (request.getContactName() != null) {
            storeInfo.setContactName(request.getContactName());
        }
        if (request.getContactPhone() != null) {
            storeInfo.setContactPhone(request.getContactPhone());
        }
        if (request.getBusinessStartTime() != null) {
            storeInfo.setBusinessStartTime(request.getBusinessStartTime());
        }
        if (request.getBusinessEndTime() != null) {
            storeInfo.setBusinessEndTime(request.getBusinessEndTime());
        }
        if (request.getRoomCount() != null) {
            storeInfo.setRoomCount(request.getRoomCount());
        }
        if (request.getTechnicianCount() != null) {
            storeInfo.setTechnicianCount(request.getTechnicianCount());
        }
        if (request.getRegionId() != null) {
            storeInfo.setRegionId(request.getRegionId());
        }
        storeInfoMapper.updateById(storeInfo);
        return R.ok();
    }

    @Override
    public R<Void> updateStatus(Long id, Integer status) {
        StoreInfoDO storeInfo = storeInfoMapper.selectById(id);
        if (storeInfo == null) {
            throw new BusinessException(ErrorCode.STORE_NOT_FOUND, "门店ID: " + id);
        }
        if (!isValidStatusTransition(storeInfo.getStatus(), status)) {
            return R.fail("只有营业中(1)和休息中(2)可互转");
        }
        storeInfo.setStatus(status);
        storeInfoMapper.updateById(storeInfo);
        return R.ok();
    }

    private boolean isValidStatusTransition(Integer currentStatus, Integer newStatus) {
        if (currentStatus.equals(1) && newStatus.equals(2)) {
            return true;
        }
        if (currentStatus.equals(2) && newStatus.equals(1)) {
            return true;
        }
        return false;
    }

    private String generateStoreNo() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int seq = SEQUENCE.getAndIncrement();
        if (seq > 9999) {
            SEQUENCE.set(1);
            seq = 1;
        }
        return "S" + datePart + String.format("%04d", seq);
    }
}
