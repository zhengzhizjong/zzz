package com.zhongjitang.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.system.domain.entity.SysConfigDO;
import com.zhongjitang.system.mapper.SysConfigMapper;
import com.zhongjitang.system.service.ISysConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysConfigServiceImpl implements ISysConfigService {

    private final SysConfigMapper sysConfigMapper;

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
}
