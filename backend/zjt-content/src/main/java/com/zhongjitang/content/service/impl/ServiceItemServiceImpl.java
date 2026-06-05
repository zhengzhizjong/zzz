package com.zhongjitang.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.content.domain.dto.ServiceItemCreateRequest;
import com.zhongjitang.content.domain.dto.ServiceItemUpdateRequest;
import com.zhongjitang.content.domain.entity.ContentServiceItemDO;
import com.zhongjitang.content.mapper.ContentServiceItemMapper;
import com.zhongjitang.content.service.IServiceItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceItemServiceImpl implements IServiceItemService {

    private final ContentServiceItemMapper serviceItemMapper;

    private static final AtomicInteger SEQUENCE = new AtomicInteger(1);

    @Override
    public R<PageResult<ContentServiceItemDO>> page(Integer page, Integer pageSize, String keyword, Integer status, Long categoryId) {
        Page<ContentServiceItemDO> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<ContentServiceItemDO> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(ContentServiceItemDO::getItemName, keyword)
                    .or().like(ContentServiceItemDO::getItemNo, keyword));
        }
        if (status != null) {
            wrapper.eq(ContentServiceItemDO::getStatus, status);
        }
        if (categoryId != null) {
            wrapper.eq(ContentServiceItemDO::getCategoryId, categoryId);
        }
        wrapper.orderByAsc(ContentServiceItemDO::getSortOrder)
                .orderByDesc(ContentServiceItemDO::getCreatedAt);

        Page<ContentServiceItemDO> result = serviceItemMapper.selectPage(pageParam, wrapper);
        return R.ok(PageResult.of(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @Override
    public R<ContentServiceItemDO> getById(Long id) {
        ContentServiceItemDO item = serviceItemMapper.selectById(id);
        if (item == null) {
            throw new BusinessException(ErrorCode.SERVICE_ITEM_NOT_FOUND);
        }
        return R.ok(item);
    }

    @Override
    public R<Void> create(ServiceItemCreateRequest request) {
        ContentServiceItemDO item = new ContentServiceItemDO();
        item.setItemNo(generateItemNo());
        item.setItemName(request.getItemName());
        item.setCategoryId(request.getCategoryId());
        item.setCategoryName(request.getCategoryName());
        item.setPrice(request.getPrice());
        item.setCostPrice(request.getCostPrice());
        item.setDurationMinutes(request.getDurationMinutes());
        item.setCommissionType(request.getCommissionType() != null ? request.getCommissionType() : 1);
        item.setCommissionValue(request.getCommissionValue() != null ? request.getCommissionValue() : BigDecimal.ZERO);
        item.setIsPackage(request.getIsPackage() != null ? request.getIsPackage() : 0);
        item.setIsAiRecommended(0);
        item.setTags(request.getTags());
        item.setDescription(request.getDescription());
        item.setStatus(1);
        item.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);

        serviceItemMapper.insert(item);
        return R.ok();
    }

    @Override
    public R<Void> update(Long id, ServiceItemUpdateRequest request) {
        ContentServiceItemDO item = serviceItemMapper.selectById(id);
        if (item == null) {
            throw new BusinessException(ErrorCode.SERVICE_ITEM_NOT_FOUND);
        }
        if (request.getItemName() != null) {
            item.setItemName(request.getItemName());
        }
        if (request.getCategoryId() != null) {
            item.setCategoryId(request.getCategoryId());
        }
        if (request.getCategoryName() != null) {
            item.setCategoryName(request.getCategoryName());
        }
        if (request.getPrice() != null) {
            item.setPrice(request.getPrice());
        }
        if (request.getCostPrice() != null) {
            item.setCostPrice(request.getCostPrice());
        }
        if (request.getDurationMinutes() != null) {
            item.setDurationMinutes(request.getDurationMinutes());
        }
        if (request.getCommissionType() != null) {
            item.setCommissionType(request.getCommissionType());
        }
        if (request.getCommissionValue() != null) {
            item.setCommissionValue(request.getCommissionValue());
        }
        if (request.getIsPackage() != null) {
            item.setIsPackage(request.getIsPackage());
        }
        if (request.getTags() != null) {
            item.setTags(request.getTags());
        }
        if (request.getDescription() != null) {
            item.setDescription(request.getDescription());
        }
        if (request.getSortOrder() != null) {
            item.setSortOrder(request.getSortOrder());
        }
        serviceItemMapper.updateById(item);
        return R.ok();
    }

    @Override
    public R<Void> updateStatus(Long id, Integer status) {
        ContentServiceItemDO item = serviceItemMapper.selectById(id);
        if (item == null) {
            throw new BusinessException(ErrorCode.SERVICE_ITEM_NOT_FOUND);
        }
        if (!isValidStatusTransition(item.getStatus(), status)) {
            return R.fail("只有上架(1)和下架(2)可互转");
        }
        item.setStatus(status);
        serviceItemMapper.updateById(item);
        return R.ok();
    }

    @Override
    public R<List<ContentServiceItemDO>> listAll(Integer status) {
        LambdaQueryWrapper<ContentServiceItemDO> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(ContentServiceItemDO::getStatus, status);
        } else {
            wrapper.eq(ContentServiceItemDO::getStatus, 1);
        }
        wrapper.orderByAsc(ContentServiceItemDO::getSortOrder)
                .orderByDesc(ContentServiceItemDO::getCreatedAt);

        List<ContentServiceItemDO> list = serviceItemMapper.selectList(wrapper);
        return R.ok(list);
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

    private String generateItemNo() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int seq = SEQUENCE.getAndIncrement();
        if (seq > 9999) {
            SEQUENCE.set(1);
            seq = 1;
        }
        return "SI" + datePart + String.format("%04d", seq);
    }
}
