package com.zhongjitang.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.system.domain.dto.FeatureFlagCreateRequest;
import com.zhongjitang.system.domain.dto.FeatureFlagUpdateRequest;
import com.zhongjitang.system.domain.entity.SysFeatureFlagDO;
import com.zhongjitang.system.mapper.SysFeatureFlagMapper;
import com.zhongjitang.system.service.ISysFeatureFlagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysFeatureFlagServiceImpl implements ISysFeatureFlagService {

    private final SysFeatureFlagMapper sysFeatureFlagMapper;

    @Override
    public R<PageResult<SysFeatureFlagDO>> page(Integer page, Integer pageSize, String keyword) {
        Page<SysFeatureFlagDO> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<SysFeatureFlagDO> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysFeatureFlagDO::getFlagKey, keyword)
                    .or().like(SysFeatureFlagDO::getFlagName, keyword));
        }
        wrapper.orderByAsc(SysFeatureFlagDO::getFlagKey);

        Page<SysFeatureFlagDO> result = sysFeatureFlagMapper.selectPage(pageParam, wrapper);
        return R.ok(PageResult.of(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @Override
    public R<SysFeatureFlagDO> getById(Long id) {
        SysFeatureFlagDO flag = sysFeatureFlagMapper.selectById(id);
        if (flag == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "功能开关不存在");
        }
        return R.ok(flag);
    }

    @Override
    public R<List<SysFeatureFlagDO>> list() {
        LambdaQueryWrapper<SysFeatureFlagDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SysFeatureFlagDO::getFlagKey);
        List<SysFeatureFlagDO> list = sysFeatureFlagMapper.selectList(wrapper);
        return R.ok(list);
    }

    @Override
    public R<SysFeatureFlagDO> getByKey(String key) {
        LambdaQueryWrapper<SysFeatureFlagDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysFeatureFlagDO::getFlagKey, key);
        SysFeatureFlagDO flag = sysFeatureFlagMapper.selectOne(wrapper);
        if (flag == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "功能开关不存在");
        }
        return R.ok(flag);
    }

    @Override
    public R<Void> create(FeatureFlagCreateRequest request) {
        // 检查开关键唯一性
        LambdaQueryWrapper<SysFeatureFlagDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysFeatureFlagDO::getFlagKey, request.getFlagKey());
        if (sysFeatureFlagMapper.selectCount(wrapper) > 0) {
            return R.fail("开关键已存在");
        }

        SysFeatureFlagDO flag = new SysFeatureFlagDO();
        flag.setFlagKey(request.getFlagKey());
        flag.setFlagName(request.getFlagName());
        flag.setDescription(request.getDescription());
        flag.setDefaultValue(request.getDefaultValue() != null ? request.getDefaultValue() : 0);
        flag.setType(request.getType() != null ? request.getType() : 1);
        flag.setPercentage(request.getPercentage());
        flag.setRulesJson(request.getRulesJson());

        sysFeatureFlagMapper.insert(flag);
        return R.ok();
    }

    @Override
    public R<Void> update(String key, FeatureFlagUpdateRequest request) {
        LambdaQueryWrapper<SysFeatureFlagDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysFeatureFlagDO::getFlagKey, key);
        SysFeatureFlagDO flag = sysFeatureFlagMapper.selectOne(wrapper);
        if (flag == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "功能开关不存在");
        }

        applyUpdate(flag, request);
        sysFeatureFlagMapper.updateById(flag);
        return R.ok();
    }

    @Override
    public R<Void> updateById(Long id, FeatureFlagUpdateRequest request) {
        SysFeatureFlagDO flag = sysFeatureFlagMapper.selectById(id);
        if (flag == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "功能开关不存在");
        }

        applyUpdate(flag, request);
        sysFeatureFlagMapper.updateById(flag);
        return R.ok();
    }

    @Override
    public R<Void> toggle(Long id, Boolean enabled) {
        SysFeatureFlagDO flag = sysFeatureFlagMapper.selectById(id);
        if (flag == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "功能开关不存在");
        }

        flag.setDefaultValue(Boolean.TRUE.equals(enabled) ? 1 : 0);
        sysFeatureFlagMapper.updateById(flag);
        return R.ok();
    }

    @Override
    public R<Void> delete(Long id) {
        SysFeatureFlagDO flag = sysFeatureFlagMapper.selectById(id);
        if (flag == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "功能开关不存在");
        }

        sysFeatureFlagMapper.deleteById(id);
        return R.ok();
    }

    private void applyUpdate(SysFeatureFlagDO flag, FeatureFlagUpdateRequest request) {
        if (request.getFlagName() != null) {
            flag.setFlagName(request.getFlagName());
        }
        if (request.getDescription() != null) {
            flag.setDescription(request.getDescription());
        }
        if (request.getDefaultValue() != null) {
            flag.setDefaultValue(request.getDefaultValue());
        }
        if (request.getType() != null) {
            flag.setType(request.getType());
        }
        if (request.getPercentage() != null) {
            flag.setPercentage(request.getPercentage());
        }
        if (request.getRulesJson() != null) {
            flag.setRulesJson(request.getRulesJson());
        }
    }

    @Override
    public R<Boolean> isEnabled(String key, Long tenantId) {
        LambdaQueryWrapper<SysFeatureFlagDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysFeatureFlagDO::getFlagKey, key);
        SysFeatureFlagDO flag = sysFeatureFlagMapper.selectOne(wrapper);
        if (flag == null) {
            return R.ok(false);
        }

        boolean enabled;
        switch (flag.getType()) {
            case 1: // 全局
                enabled = flag.getDefaultValue() != null && flag.getDefaultValue() == 1;
                break;
            case 2: // 租户
                enabled = checkTenantRule(flag, tenantId);
                break;
            case 5: // 百分比
                enabled = checkPercentage(flag, tenantId);
                break;
            default:
                enabled = flag.getDefaultValue() != null && flag.getDefaultValue() == 1;
                break;
        }

        return R.ok(enabled);
    }

    private boolean checkTenantRule(SysFeatureFlagDO flag, Long tenantId) {
        if (tenantId == null) {
            return flag.getDefaultValue() != null && flag.getDefaultValue() == 1;
        }
        // 简单实现：如果rulesJson中包含该tenantId则启用
        if (flag.getRulesJson() != null && flag.getRulesJson().contains(String.valueOf(tenantId))) {
            return true;
        }
        return flag.getDefaultValue() != null && flag.getDefaultValue() == 1;
    }

    private boolean checkPercentage(SysFeatureFlagDO flag, Long tenantId) {
        if (flag.getPercentage() == null || flag.getPercentage() <= 0) {
            return false;
        }
        if (flag.getPercentage() >= 100) {
            return true;
        }
        // 基于tenantId的简单hash取模实现百分比
        if (tenantId == null) {
            return flag.getDefaultValue() != null && flag.getDefaultValue() == 1;
        }
        return Math.abs(tenantId.hashCode()) % 100 < flag.getPercentage();
    }
}
