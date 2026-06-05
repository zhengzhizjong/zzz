package com.zhongjitang.store.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.store.domain.dto.TechnicianSkillRequest;
import com.zhongjitang.store.domain.entity.StoreTechnicianSkillDO;
import com.zhongjitang.store.mapper.StoreTechnicianSkillMapper;
import com.zhongjitang.store.service.ITechnicianSkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TechnicianSkillServiceImpl implements ITechnicianSkillService {

    private final StoreTechnicianSkillMapper technicianSkillMapper;

    @Override
    public R<List<StoreTechnicianSkillDO>> getByTechnicianId(Long technicianId) {
        LambdaQueryWrapper<StoreTechnicianSkillDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StoreTechnicianSkillDO::getTechnicianId, technicianId);
        wrapper.orderByDesc(StoreTechnicianSkillDO::getProficiency);
        List<StoreTechnicianSkillDO> skills = technicianSkillMapper.selectList(wrapper);
        return R.ok(skills);
    }

    @Override
    public R<Void> setSkill(TechnicianSkillRequest request) {
        LambdaQueryWrapper<StoreTechnicianSkillDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StoreTechnicianSkillDO::getTechnicianId, request.getTechnicianId());
        wrapper.eq(StoreTechnicianSkillDO::getServiceItemId, request.getServiceItemId());
        StoreTechnicianSkillDO existing = technicianSkillMapper.selectOne(wrapper);

        if (existing != null) {
            existing.setProficiency(request.getProficiency());
            if (request.getCertifiedAt() != null) {
                existing.setCertifiedAt(request.getCertifiedAt());
            }
            if (request.getCertificationNo() != null) {
                existing.setCertificationNo(request.getCertificationNo());
            }
            technicianSkillMapper.updateById(existing);
        } else {
            StoreTechnicianSkillDO skill = new StoreTechnicianSkillDO();
            skill.setTechnicianId(request.getTechnicianId());
            skill.setServiceItemId(request.getServiceItemId());
            skill.setProficiency(request.getProficiency());
            skill.setCertifiedAt(request.getCertifiedAt());
            skill.setCertificationNo(request.getCertificationNo());
            technicianSkillMapper.insert(skill);
        }
        return R.ok();
    }

    @Override
    public R<Void> removeSkill(Long id) {
        StoreTechnicianSkillDO skill = technicianSkillMapper.selectById(id);
        if (skill == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "技师技能不存在，ID: " + id);
        }
        technicianSkillMapper.deleteById(id);
        return R.ok();
    }

    @Override
    public R<List<StoreTechnicianSkillDO>> getByServiceItemId(Long serviceItemId) {
        LambdaQueryWrapper<StoreTechnicianSkillDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StoreTechnicianSkillDO::getServiceItemId, serviceItemId);
        wrapper.orderByDesc(StoreTechnicianSkillDO::getProficiency);
        List<StoreTechnicianSkillDO> skills = technicianSkillMapper.selectList(wrapper);
        return R.ok(skills);
    }
}
