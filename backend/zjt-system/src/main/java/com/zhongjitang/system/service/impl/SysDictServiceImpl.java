package com.zhongjitang.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.system.domain.dto.SysDictCreateRequest;
import com.zhongjitang.system.domain.dto.SysDictItemCreateRequest;
import com.zhongjitang.system.domain.dto.SysDictItemUpdateRequest;
import com.zhongjitang.system.domain.dto.SysDictUpdateRequest;
import com.zhongjitang.system.domain.entity.SysDictDO;
import com.zhongjitang.system.domain.entity.SysDictItemDO;
import com.zhongjitang.system.mapper.SysDictItemMapper;
import com.zhongjitang.system.mapper.SysDictMapper;
import com.zhongjitang.system.service.ISysDictService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysDictServiceImpl implements ISysDictService {

    private final SysDictMapper sysDictMapper;
    private final SysDictItemMapper sysDictItemMapper;

    @Override
    public R<PageResult<SysDictDO>> page(Integer page, Integer pageSize, String keyword) {
        Page<SysDictDO> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<SysDictDO> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysDictDO::getDictName, keyword)
                    .or().like(SysDictDO::getDictCode, keyword));
        }
        wrapper.orderByDesc(SysDictDO::getCreatedAt);

        Page<SysDictDO> result = sysDictMapper.selectPage(pageParam, wrapper);
        return R.ok(PageResult.of(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @Override
    public R<SysDictDO> getById(Long id) {
        SysDictDO dict = sysDictMapper.selectById(id);
        if (dict == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "字典不存在");
        }
        return R.ok(dict);
    }

    @Override
    public R<Void> create(SysDictCreateRequest request) {
        // 检查字典编码唯一性
        LambdaQueryWrapper<SysDictDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDictDO::getDictCode, request.getDictCode());
        if (sysDictMapper.selectCount(wrapper) > 0) {
            return R.fail("字典编码已存在");
        }

        SysDictDO dict = new SysDictDO();
        dict.setDictCode(request.getDictCode());
        dict.setDictName(request.getDictName());
        dict.setDescription(request.getDescription());
        dict.setStatus(request.getStatus() != null ? request.getStatus() : 1);

        sysDictMapper.insert(dict);
        return R.ok();
    }

    @Override
    public R<Void> update(Long id, SysDictUpdateRequest request) {
        SysDictDO dict = sysDictMapper.selectById(id);
        if (dict == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "字典不存在");
        }

        if (request.getDictName() != null) {
            dict.setDictName(request.getDictName());
        }
        if (request.getDescription() != null) {
            dict.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            dict.setStatus(request.getStatus());
        }

        sysDictMapper.updateById(dict);
        return R.ok();
    }

    @Override
    public R<List<SysDictItemDO>> getItems(Long dictId) {
        SysDictDO dict = sysDictMapper.selectById(dictId);
        if (dict == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "字典不存在");
        }

        LambdaQueryWrapper<SysDictItemDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDictItemDO::getDictId, dictId)
                .orderByAsc(SysDictItemDO::getSortOrder)
                .orderByDesc(SysDictItemDO::getCreatedAt);

        List<SysDictItemDO> items = sysDictItemMapper.selectList(wrapper);
        return R.ok(items);
    }

    @Override
    public R<Void> addItem(SysDictItemCreateRequest request) {
        SysDictDO dict = sysDictMapper.selectById(request.getDictId());
        if (dict == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "字典不存在");
        }

        SysDictItemDO item = new SysDictItemDO();
        item.setDictId(request.getDictId());
        item.setItemCode(request.getItemCode());
        item.setItemName(request.getItemName());
        item.setItemValue(request.getItemValue());
        item.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        item.setStatus(request.getStatus() != null ? request.getStatus() : 1);

        sysDictItemMapper.insert(item);
        return R.ok();
    }

    @Override
    public R<Void> updateItem(Long id, SysDictItemUpdateRequest request) {
        SysDictItemDO item = sysDictItemMapper.selectById(id);
        if (item == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "字典项不存在");
        }

        if (request.getItemCode() != null) {
            item.setItemCode(request.getItemCode());
        }
        if (request.getItemName() != null) {
            item.setItemName(request.getItemName());
        }
        if (request.getItemValue() != null) {
            item.setItemValue(request.getItemValue());
        }
        if (request.getSortOrder() != null) {
            item.setSortOrder(request.getSortOrder());
        }
        if (request.getStatus() != null) {
            item.setStatus(request.getStatus());
        }

        sysDictItemMapper.updateById(item);
        return R.ok();
    }

    @Override
    public R<Void> deleteItem(Long id) {
        SysDictItemDO item = sysDictItemMapper.selectById(id);
        if (item == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "字典项不存在");
        }

        sysDictItemMapper.deleteById(id);
        return R.ok();
    }

    @Override
    public R<List<SysDictItemDO>> getItemsByCode(String dictCode) {
        LambdaQueryWrapper<SysDictDO> dictWrapper = new LambdaQueryWrapper<>();
        dictWrapper.eq(SysDictDO::getDictCode, dictCode);
        SysDictDO dict = sysDictMapper.selectOne(dictWrapper);
        if (dict == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "字典不存在");
        }

        LambdaQueryWrapper<SysDictItemDO> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(SysDictItemDO::getDictId, dict.getId())
                .eq(SysDictItemDO::getStatus, 1)
                .orderByAsc(SysDictItemDO::getSortOrder);

        List<SysDictItemDO> items = sysDictItemMapper.selectList(itemWrapper);
        return R.ok(items);
    }
}
