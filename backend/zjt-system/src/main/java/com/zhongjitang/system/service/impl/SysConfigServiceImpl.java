package com.zhongjitang.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.system.domain.entity.SysConfigDO;
import com.zhongjitang.system.mapper.SysConfigMapper;
import com.zhongjitang.system.service.ISysConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysConfigServiceImpl implements ISysConfigService {

    private final SysConfigMapper sysConfigMapper;

    @Override
    public R<PageResult<SysConfigDO>> page(Integer page, Integer pageSize, String keyword) {
        Page<SysConfigDO> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<SysConfigDO> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysConfigDO::getConfigKey, keyword)
                    .or().like(SysConfigDO::getConfigName, keyword));
        }
        wrapper.orderByAsc(SysConfigDO::getConfigKey);

        Page<SysConfigDO> result = sysConfigMapper.selectPage(pageParam, wrapper);
        return R.ok(PageResult.of(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @Override
    public R<SysConfigDO> getById(Long id) {
        SysConfigDO config = sysConfigMapper.selectById(id);
        if (config == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "配置不存在");
        }
        return R.ok(config);
    }

    @Override
    public R<List<SysConfigDO>> list() {
        LambdaQueryWrapper<SysConfigDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SysConfigDO::getConfigKey);
        List<SysConfigDO> list = sysConfigMapper.selectList(wrapper);
        return R.ok(list);
    }

    @Override
    public R<SysConfigDO> getByKey(String key) {
        LambdaQueryWrapper<SysConfigDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfigDO::getConfigKey, key);
        SysConfigDO config = sysConfigMapper.selectOne(wrapper);
        if (config == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "配置不存在");
        }
        return R.ok(config);
    }

    @Override
    public R<Void> update(String key, String value) {
        LambdaQueryWrapper<SysConfigDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfigDO::getConfigKey, key);
        SysConfigDO config = sysConfigMapper.selectOne(wrapper);
        if (config == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "配置不存在");
        }

        config.setConfigValue(value);
        sysConfigMapper.updateById(config);
        return R.ok();
    }

    @Override
    public R<Void> updateById(Long id, String configValue) {
        SysConfigDO config = sysConfigMapper.selectById(id);
        if (config == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "配置不存在");
        }

        config.setConfigValue(configValue);
        sysConfigMapper.updateById(config);
        return R.ok();
    }
}
