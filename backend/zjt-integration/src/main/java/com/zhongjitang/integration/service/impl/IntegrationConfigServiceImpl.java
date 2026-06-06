package com.zhongjitang.integration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.integration.domain.dto.ConfigCreateRequest;
import com.zhongjitang.integration.domain.dto.ConfigUpdateRequest;
import com.zhongjitang.integration.domain.entity.IntegrationConfigDO;
import com.zhongjitang.integration.mapper.IntegrationConfigMapper;
import com.zhongjitang.integration.service.IIntegrationConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IntegrationConfigServiceImpl implements IIntegrationConfigService {

    private final IntegrationConfigMapper integrationConfigMapper;

    @Override
    public R<List<IntegrationConfigDO>> list() {
        LambdaQueryWrapper<IntegrationConfigDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(IntegrationConfigDO::getCreatedAt);
        List<IntegrationConfigDO> list = integrationConfigMapper.selectList(wrapper);
        return R.ok(list);
    }

    @Override
    public R<IntegrationConfigDO> getByPlatform(String platform) {
        LambdaQueryWrapper<IntegrationConfigDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IntegrationConfigDO::getPlatform, platform);
        IntegrationConfigDO config = integrationConfigMapper.selectOne(wrapper);
        if (config == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "平台配置不存在: " + platform);
        }
        return R.ok(config);
    }

    @Override
    public R<Void> create(ConfigCreateRequest request) {
        IntegrationConfigDO config = new IntegrationConfigDO();
        config.setPlatform(request.getPlatform());
        config.setConfigName(request.getConfigName());
        config.setAppId(request.getAppId());
        config.setAppSecret(request.getAppSecret());
        config.setCallbackUrl(request.getCallbackUrl());
        config.setExtraConfig(request.getExtraConfig());
        config.setStatus(1); // 默认启用
        integrationConfigMapper.insert(config);
        return R.ok();
    }

    @Override
    public R<Void> update(Long id, ConfigUpdateRequest request) {
        IntegrationConfigDO config = integrationConfigMapper.selectById(id);
        if (config == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "配置ID: " + id);
        }
        if (StringUtils.hasText(request.getConfigName())) {
            config.setConfigName(request.getConfigName());
        }
        if (request.getAppId() != null) {
            config.setAppId(request.getAppId());
        }
        if (request.getAppSecret() != null) {
            config.setAppSecret(request.getAppSecret());
        }
        if (request.getCallbackUrl() != null) {
            config.setCallbackUrl(request.getCallbackUrl());
        }
        if (request.getExtraConfig() != null) {
            config.setExtraConfig(request.getExtraConfig());
        }
        integrationConfigMapper.updateById(config);
        return R.ok();
    }

    @Override
    public R<Void> updateStatus(Long id, Integer status) {
        IntegrationConfigDO config = integrationConfigMapper.selectById(id);
        if (config == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "配置ID: " + id);
        }
        if (status != 1 && status != 2) {
            return R.fail("状态值无效，有效值: 1启用 2停用");
        }
        config.setStatus(status);
        integrationConfigMapper.updateById(config);
        return R.ok();
    }
}
