package com.zhongjitang.store.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.store.domain.dto.TechnicianCreateRequest;
import com.zhongjitang.store.domain.dto.TechnicianUpdateRequest;
import com.zhongjitang.store.domain.entity.StoreInfoDO;
import com.zhongjitang.store.domain.entity.StoreTechnicianDO;
import com.zhongjitang.store.domain.vo.TechnicianPerformanceVO;
import com.zhongjitang.store.domain.vo.TechnicianWorkspaceVO;
import com.zhongjitang.store.mapper.StoreInfoMapper;
import com.zhongjitang.store.mapper.StoreTechnicianMapper;
import com.zhongjitang.store.service.IStoreTechnicianService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class StoreTechnicianServiceImpl implements IStoreTechnicianService {

    private final StoreTechnicianMapper storeTechnicianMapper;
    private final StoreInfoMapper storeInfoMapper;
    private final JdbcTemplate jdbcTemplate;

    private static final AtomicInteger SEQUENCE = new AtomicInteger(1);

    @Override
    public R<PageResult<StoreTechnicianDO>> page(Integer page, Integer pageSize, Long storeId,
                                                  Integer status, Integer isOnline,
                                                  Integer skillLevel, String skilledItems) {
        Page<StoreTechnicianDO> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<StoreTechnicianDO> wrapper = new LambdaQueryWrapper<>();
        if (storeId != null) {
            wrapper.eq(StoreTechnicianDO::getStoreId, storeId);
        }
        if (status != null) {
            wrapper.eq(StoreTechnicianDO::getStatus, status);
        }
        if (isOnline != null) {
            wrapper.eq(StoreTechnicianDO::getIsOnline, isOnline);
        }
        if (skillLevel != null) {
            wrapper.eq(StoreTechnicianDO::getSkillLevel, skillLevel);
        }
        if (StringUtils.hasText(skilledItems)) {
            wrapper.like(StoreTechnicianDO::getSkilledItems, skilledItems);
        }
        wrapper.orderByDesc(StoreTechnicianDO::getCreatedAt);
        Page<StoreTechnicianDO> result = storeTechnicianMapper.selectPage(pageParam, wrapper);
        // 填充关联字段
        fillRelatedFields(result.getRecords());
        return R.ok(PageResult.of(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @Override
    public R<StoreTechnicianDO> getById(Long id) {
        StoreTechnicianDO technician = storeTechnicianMapper.selectById(id);
        if (technician == null) {
            throw new BusinessException(ErrorCode.TECHNICIAN_NOT_FOUND, "技师ID: " + id);
        }
        // 填充关联字段
        fillRelatedFields(List.of(technician));
        return R.ok(technician);
    }

    @Override
    public R<Void> create(TechnicianCreateRequest request) {
        StoreTechnicianDO technician = new StoreTechnicianDO();
        technician.setStoreId(request.getStoreId());
        technician.setEmployeeId(request.getEmployeeId());
        technician.setTechnicianNo(generateTechnicianNo());
        technician.setSkillLevel(request.getSkillLevel());
        technician.setSkilledItems(request.getSkilledItems());
        technician.setDefaultSchedule(request.getDefaultSchedule());
        technician.setMonthServiceCount(0);
        technician.setTotalServiceCount(0);
        technician.setStatus(1);
        technician.setIsOnline(0);
        storeTechnicianMapper.insert(technician);
        return R.ok();
    }

    @Override
    public R<Void> update(Long id, TechnicianUpdateRequest request) {
        StoreTechnicianDO technician = storeTechnicianMapper.selectById(id);
        if (technician == null) {
            throw new BusinessException(ErrorCode.TECHNICIAN_NOT_FOUND, "技师ID: " + id);
        }
        if (request.getSkillLevel() != null) {
            technician.setSkillLevel(request.getSkillLevel());
        }
        if (request.getSkilledItems() != null) {
            technician.setSkilledItems(request.getSkilledItems());
        }
        if (request.getDefaultSchedule() != null) {
            technician.setDefaultSchedule(request.getDefaultSchedule());
        }
        storeTechnicianMapper.updateById(technician);
        return R.ok();
    }

    @Override
    public R<Void> updateStatus(Long id, Integer status) {
        StoreTechnicianDO technician = storeTechnicianMapper.selectById(id);
        if (technician == null) {
            throw new BusinessException(ErrorCode.TECHNICIAN_NOT_FOUND, "技师ID: " + id);
        }
        technician.setStatus(status);
        storeTechnicianMapper.updateById(technician);
        return R.ok();
    }

    @Override
    public R<Void> checkIn(Long id) {
        StoreTechnicianDO technician = storeTechnicianMapper.selectById(id);
        if (technician == null) {
            throw new BusinessException(ErrorCode.TECHNICIAN_NOT_FOUND, "技师ID: " + id);
        }
        technician.setIsOnline(1);
        storeTechnicianMapper.updateById(technician);
        return R.ok();
    }

    @Override
    public R<Void> checkOut(Long id) {
        StoreTechnicianDO technician = storeTechnicianMapper.selectById(id);
        if (technician == null) {
            throw new BusinessException(ErrorCode.TECHNICIAN_NOT_FOUND, "技师ID: " + id);
        }
        technician.setIsOnline(0);
        storeTechnicianMapper.updateById(technician);
        return R.ok();
    }

    @Override
    public R<TechnicianWorkspaceVO> getWorkspace(Long technicianId) {
        StoreTechnicianDO technician = storeTechnicianMapper.selectById(technicianId);
        if (technician == null) {
            throw new BusinessException(ErrorCode.TECHNICIAN_NOT_FOUND, "技师ID: " + technicianId);
        }

        TechnicianWorkspaceVO vo = new TechnicianWorkspaceVO();

        // 技师信息
        TechnicianWorkspaceVO.TechnicianInfo info = new TechnicianWorkspaceVO.TechnicianInfo();
        info.setId(technician.getId());
        info.setName(null); // 需关联employee表获取姓名
        info.setAvatar(null);
        info.setSkillLevel(technician.getSkillLevel());
        info.setIsOnline(technician.getIsOnline());
        vo.setTechnicianInfo(info);

        // 当前进行中的服务（status=3服务中）
        // 需跨模块查询trade_appointment表，此处返回null占位
        vo.setCurrentService(null);

        // 待服务预约列表（status=2已确认，按日期排序，最多5条）
        // 需跨模块查询trade_appointment表，此处返回空列表占位
        vo.setPendingAppointments(new ArrayList<>());

        return R.ok(vo);
    }

    @Override
    public R<TechnicianPerformanceVO> getPerformance(Long technicianId) {
        StoreTechnicianDO technician = storeTechnicianMapper.selectById(technicianId);
        if (technician == null) {
            throw new BusinessException(ErrorCode.TECHNICIAN_NOT_FOUND, "技师ID: " + technicianId);
        }

        TechnicianPerformanceVO vo = new TechnicianPerformanceVO();

        // 本月营收
        BigDecimal monthRevenue = technician.getMonthRevenue() != null ? technician.getMonthRevenue() : BigDecimal.ZERO;
        vo.setMonthRevenue(monthRevenue);

        // 本月目标（默认30000元）
        BigDecimal monthTarget = new BigDecimal("30000");
        vo.setMonthTarget(monthTarget);

        // 目标完成率
        double completion = monthTarget.compareTo(BigDecimal.ZERO) > 0
                ? Math.round(monthRevenue.doubleValue() / monthTarget.doubleValue() * 10000.0) / 100.0
                : 0.0;
        vo.setTargetCompletion(completion);

        // 本月服务次数
        vo.setMonthServiceCount(technician.getMonthServiceCount() != null ? technician.getMonthServiceCount() : 0);

        // 客单价
        int serviceCount = vo.getMonthServiceCount();
        if (serviceCount > 0) {
            vo.setAvgPrice(monthRevenue.divide(new BigDecimal(serviceCount), 2, RoundingMode.HALF_UP));
        } else {
            vo.setAvgPrice(BigDecimal.ZERO);
        }

        // 本月评分
        vo.setMonthRating(technician.getMonthRating() != null ? technician.getMonthRating() : BigDecimal.ZERO);

        // 排名（查询同门店技师按monthRevenue排序）
        LambdaQueryWrapper<StoreTechnicianDO> rankWrapper = new LambdaQueryWrapper<>();
        rankWrapper.eq(StoreTechnicianDO::getStoreId, technician.getStoreId())
                .eq(StoreTechnicianDO::getStatus, 1)
                .gt(StoreTechnicianDO::getMonthRevenue, monthRevenue);
        Long aheadCount = storeTechnicianMapper.selectCount(rankWrapper);
        vo.setRank(aheadCount != null ? aheadCount.intValue() + 1 : 1);

        return R.ok(vo);
    }

    private String generateTechnicianNo() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int seq = SEQUENCE.getAndIncrement();
        if (seq > 9999) {
            SEQUENCE.set(1);
            seq = 1;
        }
        return "T" + datePart + String.format("%04d", seq);
    }

    /**
     * 填充技师的关联字段：姓名、头像、门店名称
     */
    private void fillRelatedFields(List<StoreTechnicianDO> technicians) {
        for (StoreTechnicianDO tech : technicians) {
            // 填充员工姓名
            if (tech.getEmployeeId() != null) {
                try {
                    String sql = "SELECT name FROM user_employee WHERE id = ? AND is_deleted = 0";
                    List<String> names = jdbcTemplate.queryForList(sql, String.class, tech.getEmployeeId());
                    if (!names.isEmpty()) {
                        tech.setName(names.get(0));
                    }
                } catch (Exception e) {
                    // ignore
                }
            }
            // 填充门店名称
            if (tech.getStoreId() != null) {
                try {
                    StoreInfoDO store = storeInfoMapper.selectById(tech.getStoreId());
                    if (store != null) {
                        tech.setStoreName(store.getStoreName());
                    }
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }
}
