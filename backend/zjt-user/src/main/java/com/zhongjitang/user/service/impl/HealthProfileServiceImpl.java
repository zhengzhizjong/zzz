package com.zhongjitang.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhongjitang.common.core.context.UserContext;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.user.domain.dto.HealthProfileRequest;
import com.zhongjitang.user.domain.entity.UserHealthProfileDO;
import com.zhongjitang.user.mapper.UserHealthProfileMapper;
import com.zhongjitang.user.service.IHealthProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HealthProfileServiceImpl implements IHealthProfileService {

    private final UserHealthProfileMapper healthProfileMapper;

    @Override
    public R<UserHealthProfileDO> getByMemberId(Long memberId) {
        LambdaQueryWrapper<UserHealthProfileDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserHealthProfileDO::getMemberId, memberId);
        UserHealthProfileDO profile = healthProfileMapper.selectOne(wrapper);
        return R.ok(profile);
    }

    @Override
    public R<Void> saveOrUpdate(HealthProfileRequest request) {
        LambdaQueryWrapper<UserHealthProfileDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserHealthProfileDO::getMemberId, request.getMemberId());
        UserHealthProfileDO existing = healthProfileMapper.selectOne(wrapper);

        if (existing == null) {
            UserHealthProfileDO profile = new UserHealthProfileDO();
            profile.setMemberId(request.getMemberId());
            profile.setConstitutionType(request.getConstitutionType());
            profile.setConstitutionName(request.getConstitutionName());
            profile.setAllergies(request.getAllergies());
            profile.setContraindications(request.getContraindications());
            profile.setMedicalHistory(request.getMedicalHistory());
            profile.setMedication(request.getMedication());
            profile.setNotes(request.getNotes());
            profile.setLastUpdatedBy(UserContext.getUsername());
            healthProfileMapper.insert(profile);
        } else {
            if (request.getConstitutionType() != null) {
                existing.setConstitutionType(request.getConstitutionType());
            }
            if (request.getConstitutionName() != null) {
                existing.setConstitutionName(request.getConstitutionName());
            }
            if (request.getAllergies() != null) {
                existing.setAllergies(request.getAllergies());
            }
            if (request.getContraindications() != null) {
                existing.setContraindications(request.getContraindications());
            }
            if (request.getMedicalHistory() != null) {
                existing.setMedicalHistory(request.getMedicalHistory());
            }
            if (request.getMedication() != null) {
                existing.setMedication(request.getMedication());
            }
            if (request.getNotes() != null) {
                existing.setNotes(request.getNotes());
            }
            existing.setLastUpdatedBy(UserContext.getUsername());
            healthProfileMapper.updateById(existing);
        }

        return R.ok();
    }
}
