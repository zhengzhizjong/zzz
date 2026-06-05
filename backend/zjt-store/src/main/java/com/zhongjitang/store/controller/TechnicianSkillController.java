package com.zhongjitang.store.controller;

import com.zhongjitang.common.core.result.R;
import com.zhongjitang.store.domain.dto.TechnicianSkillRequest;
import com.zhongjitang.store.domain.entity.StoreTechnicianSkillDO;
import com.zhongjitang.store.service.ITechnicianSkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/store/technician-skills")
@Tag(name = "技师技能管理")
@RequiredArgsConstructor
public class TechnicianSkillController {

    private final ITechnicianSkillService technicianSkillService;

    @GetMapping("/technician/{technicianId}")
    @Operation(summary = "技师技能列表")
    public R<List<StoreTechnicianSkillDO>> getByTechnicianId(@PathVariable Long technicianId) {
        return technicianSkillService.getByTechnicianId(technicianId);
    }

    @GetMapping("/service-item/{serviceItemId}")
    @Operation(summary = "按项目查技师")
    public R<List<StoreTechnicianSkillDO>> getByServiceItemId(@PathVariable Long serviceItemId) {
        return technicianSkillService.getByServiceItemId(serviceItemId);
    }

    @PostMapping
    @Operation(summary = "设置技能")
    public R<Void> setSkill(@Valid @RequestBody TechnicianSkillRequest request) {
        return technicianSkillService.setSkill(request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "移除技能")
    public R<Void> removeSkill(@PathVariable Long id) {
        return technicianSkillService.removeSkill(id);
    }
}
