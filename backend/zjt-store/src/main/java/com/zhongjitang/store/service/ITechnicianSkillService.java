package com.zhongjitang.store.service;

import com.zhongjitang.common.core.result.R;
import com.zhongjitang.store.domain.dto.TechnicianSkillRequest;
import com.zhongjitang.store.domain.entity.StoreTechnicianSkillDO;

import java.util.List;

public interface ITechnicianSkillService {

    R<List<StoreTechnicianSkillDO>> getByTechnicianId(Long technicianId);

    R<Void> setSkill(TechnicianSkillRequest request);

    R<Void> removeSkill(Long id);

    R<List<StoreTechnicianSkillDO>> getByServiceItemId(Long serviceItemId);
}
