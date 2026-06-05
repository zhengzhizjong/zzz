package com.zhongjitang.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.common.redis.util.RedisUtil;
import com.zhongjitang.trade.domain.dto.AppointmentCancelRequest;
import com.zhongjitang.trade.domain.dto.AppointmentCreateRequest;
import com.zhongjitang.trade.domain.dto.AppointmentModifyRequest;
import com.zhongjitang.trade.domain.entity.TradeAppointmentDO;
import com.zhongjitang.trade.domain.entity.TradeAppointmentLockDO;
import com.zhongjitang.trade.domain.vo.AppointmentResponse;
import com.zhongjitang.trade.domain.vo.MyAppointmentVO;
import com.zhongjitang.trade.mapper.TradeAppointmentLockMapper;
import com.zhongjitang.trade.mapper.TradeAppointmentMapper;
import com.zhongjitang.trade.service.AppointmentLockService;
import com.zhongjitang.trade.service.AppointmentSlotService;
import com.zhongjitang.trade.service.IAppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements IAppointmentService {

    private final TradeAppointmentMapper tradeAppointmentMapper;
    private final TradeAppointmentLockMapper tradeAppointmentLockMapper;
    private final AppointmentLockService appointmentLockService;
    private final AppointmentSlotService appointmentSlotService;
    private final RedisUtil redisUtil;

    private static final AtomicInteger SEQUENCE = new AtomicInteger(1);
    private static final String SLOT_CACHE_PREFIX = "slot:";
    private static final String REDIS_LOCK_PREFIX = "lock:";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final Map<Integer, String> STATUS_NAME_MAP = new HashMap<>();

    static {
        STATUS_NAME_MAP.put(1, "待确认");
        STATUS_NAME_MAP.put(2, "已确认");
        STATUS_NAME_MAP.put(3, "服务中");
        STATUS_NAME_MAP.put(4, "已完成");
        STATUS_NAME_MAP.put(5, "已取消");
        STATUS_NAME_MAP.put(6, "超时未到");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<AppointmentResponse> create(AppointmentCreateRequest request) {
        // 1. 校验lockId有效性（从DB查锁记录，确认未过期且status=1）
        LambdaQueryWrapper<TradeAppointmentLockDO> lockWrapper = new LambdaQueryWrapper<>();
        lockWrapper.eq(TradeAppointmentLockDO::getLockId, request.getLockId())
                .eq(TradeAppointmentLockDO::getStatus, 1)
                .gt(TradeAppointmentLockDO::getExpireAt, LocalDateTime.now());
        TradeAppointmentLockDO lockDO = tradeAppointmentLockMapper.selectOne(lockWrapper);
        if (lockDO == null) {
            throw new BusinessException(ErrorCode.SLOT_TEMP_LOCKED, "锁档已过期或无效");
        }

        // 2. 防重复预约：查trade_appointment表，同一客户同一门店同一天最多3个有效预约
        if (request.getMemberId() != null) {
            LambdaQueryWrapper<TradeAppointmentDO> countWrapper = new LambdaQueryWrapper<>();
            countWrapper.eq(TradeAppointmentDO::getMemberId, request.getMemberId())
                    .eq(TradeAppointmentDO::getStoreId, request.getStoreId())
                    .eq(TradeAppointmentDO::getAppointmentDate, request.getDate())
                    .in(TradeAppointmentDO::getStatus, Arrays.asList(1, 2));
            Long count = tradeAppointmentMapper.selectCount(countWrapper);
            if (count >= 3) {
                throw new BusinessException(ErrorCode.APPOINTMENT_LIMIT);
            }
        }

        // 3. 生成预约编号: APT + yyyyMMdd + 6位序号
        String appointmentNo = generateAppointmentNo();

        // 4. 插入trade_appointment记录（version=0）
        TradeAppointmentDO appointment = new TradeAppointmentDO();
        appointment.setAppointmentNo(appointmentNo);
        appointment.setMemberId(request.getMemberId());
        appointment.setTechnicianId(request.getTechnicianId());
        appointment.setServiceItemId(request.getServiceItemId());
        appointment.setAppointmentDate(request.getDate());
        appointment.setTimeSlot(request.getTimeSlot());
        appointment.setAppointmentTime(parseTimeSlot(request.getTimeSlot()));
        appointment.setDurationMinutes(request.getDurationMinutes() != null ? request.getDurationMinutes() : 60);
        appointment.setVersion(0);
        appointment.setStatus(1);
        appointment.setSourceChannel(request.getSourceChannel());
        appointment.setConsultationNotes(request.getConsultationNotes());
        appointment.setStoreId(request.getStoreId());

        // 计算startTime和endTime
        LocalDateTime startTime = LocalDateTime.of(request.getDate(), parseTimeSlot(request.getTimeSlot()));
        appointment.setStartTime(startTime);
        appointment.setEndTime(startTime.plusMinutes(appointment.getDurationMinutes()));

        tradeAppointmentMapper.insert(appointment);

        // 5. 更新锁记录status=3(已转预约)
        lockDO.setStatus(3);
        tradeAppointmentLockMapper.updateById(lockDO);

        // 6. 删除Redis锁标记和时段缓存
        String redisLockKey = REDIS_LOCK_PREFIX + request.getStoreId() + ":"
                + request.getTechnicianId() + ":" + request.getDate() + ":" + request.getTimeSlot();
        redisUtil.delete(redisLockKey);

        String slotCacheKey = SLOT_CACHE_PREFIX + request.getStoreId() + ":"
                + request.getTechnicianId() + ":" + request.getDate();
        redisUtil.delete(slotCacheKey);

        // 7. 返回AppointmentResponse
        AppointmentResponse response = new AppointmentResponse();
        response.setId(appointment.getId());
        response.setAppointmentNo(appointmentNo);
        response.setAppointmentDate(request.getDate());
        response.setAppointmentTime(parseTimeSlot(request.getTimeSlot()));
        response.setStatus(1);
        response.setRedirect("my_appointments");
        response.setCreatedAt(LocalDateTime.now());

        return R.ok(response);
    }

    @Override
    public R<PageResult<TradeAppointmentDO>> page(Integer page, Integer pageSize, Long storeId,
                                                    Long memberId, Integer status) {
        Page<TradeAppointmentDO> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<TradeAppointmentDO> wrapper = new LambdaQueryWrapper<>();
        if (storeId != null) {
            wrapper.eq(TradeAppointmentDO::getStoreId, storeId);
        }
        if (memberId != null) {
            wrapper.eq(TradeAppointmentDO::getMemberId, memberId);
        }
        if (status != null) {
            wrapper.eq(TradeAppointmentDO::getStatus, status);
        }
        wrapper.orderByDesc(TradeAppointmentDO::getCreatedAt);
        Page<TradeAppointmentDO> result = tradeAppointmentMapper.selectPage(pageParam, wrapper);
        return R.ok(PageResult.of(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @Override
    public R<TradeAppointmentDO> getById(Long id) {
        TradeAppointmentDO appointment = tradeAppointmentMapper.selectById(id);
        if (appointment == null) {
            throw new BusinessException(ErrorCode.APPOINTMENT_NOT_FOUND, "预约ID: " + id);
        }
        return R.ok(appointment);
    }

    @Override
    public R<MyAppointmentVO> getMyAppointments(Long memberId, String statusGroup) {
        MyAppointmentVO result = new MyAppointmentVO();

        // 查询该会员所有预约
        LambdaQueryWrapper<TradeAppointmentDO> baseWrapper = new LambdaQueryWrapper<>();
        baseWrapper.eq(TradeAppointmentDO::getMemberId, memberId)
                .orderByDesc(TradeAppointmentDO::getCreatedAt);
        List<TradeAppointmentDO> allAppointments = tradeAppointmentMapper.selectList(baseWrapper);

        // 按状态分组
        List<TradeAppointmentDO> pendingList = allAppointments.stream()
                .filter(a -> Arrays.asList(1, 2, 3).contains(a.getStatus()))
                .collect(Collectors.toList());
        List<TradeAppointmentDO> completedList = allAppointments.stream()
                .filter(a -> a.getStatus() == 4)
                .collect(Collectors.toList());
        List<TradeAppointmentDO> cancelledList = allAppointments.stream()
                .filter(a -> Arrays.asList(5, 6).contains(a.getStatus()))
                .collect(Collectors.toList());

        // 根据statusGroup过滤
        if ("pending".equals(statusGroup)) {
            result.setPending(buildGroup(pendingList));
        } else if ("completed".equals(statusGroup)) {
            result.setCompleted(buildGroup(completedList));
        } else if ("cancelled".equals(statusGroup)) {
            result.setCancelled(buildGroup(cancelledList));
        } else {
            // all: 返回全部分组
            result.setPending(buildGroup(pendingList));
            result.setCompleted(buildGroup(completedList));
            result.setCancelled(buildGroup(cancelledList));
        }

        return R.ok(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<AppointmentResponse> modify(Long id, AppointmentModifyRequest request) {
        // 1. 查询预约，校验存在且状态IN(1,2)
        TradeAppointmentDO appointment = tradeAppointmentMapper.selectById(id);
        if (appointment == null) {
            throw new BusinessException(ErrorCode.APPOINTMENT_NOT_FOUND, "预约ID: " + id);
        }
        if (!Arrays.asList(1, 2).contains(appointment.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "当前预约状态不允许修改");
        }

        // 2. 校验修改次数：modifyCount < 2（通过modifyReason非空次数判断）
        int modifyCount = calculateModifyCount(appointment);
        if (modifyCount >= 2) {
            throw new BusinessException(ErrorCode.APPOINTMENT_MODIFY_LIMIT);
        }

        // 3. 校验距预约时间 > 2小时
        LocalDateTime appointmentTime = LocalDateTime.of(appointment.getAppointmentDate(),
                appointment.getAppointmentTime());
        if (LocalDateTime.now().plusHours(2).isAfter(appointmentTime)) {
            throw new BusinessException(ErrorCode.APPOINTMENT_CANCEL_TIMEOUT, "距预约时间不足2小时，不可修改");
        }

        // 4. 判断是否修改了时段或技师
        boolean slotChanged = !request.getTimeSlot().equals(appointment.getTimeSlot())
                || !request.getDate().equals(appointment.getAppointmentDate());
        boolean technicianChanged = !request.getTechnicianId().equals(appointment.getTechnicianId());

        if (slotChanged || technicianChanged) {
            // 4.1 先锁定新时段
            LockTempRequest lockRequest = new LockTempRequest();
            lockRequest.setStoreId(appointment.getStoreId());
            lockRequest.setTechnicianId(request.getTechnicianId());
            lockRequest.setDate(request.getDate());
            lockRequest.setTimeSlot(request.getTimeSlot());
            appointmentLockService.lockTemp(lockRequest, appointment.getMemberId());

            // 4.2 释放旧时段（删除旧时段的Redis缓存和锁记录）
            releaseSlot(appointment.getStoreId(), appointment.getTechnicianId(),
                    appointment.getAppointmentDate(), appointment.getTimeSlot());
        }

        // 5. 更新预约记录
        appointment.setAppointmentDate(request.getDate());
        appointment.setTimeSlot(request.getTimeSlot());
        appointment.setAppointmentTime(parseTimeSlot(request.getTimeSlot()));
        appointment.setTechnicianId(request.getTechnicianId());
        appointment.setModifyReason(request.getModifyReason());

        // 计算新的startTime和endTime
        LocalDateTime newStartTime = LocalDateTime.of(request.getDate(), parseTimeSlot(request.getTimeSlot()));
        appointment.setStartTime(newStartTime);
        appointment.setEndTime(newStartTime.plusMinutes(appointment.getDurationMinutes()));

        // version+1由乐观锁自动处理
        tradeAppointmentMapper.updateById(appointment);

        // 6. 清除时段Redis缓存
        String slotCacheKey = SLOT_CACHE_PREFIX + appointment.getStoreId() + ":"
                + request.getTechnicianId() + ":" + request.getDate();
        redisUtil.delete(slotCacheKey);

        // 7. 返回AppointmentResponse
        AppointmentResponse response = new AppointmentResponse();
        response.setId(appointment.getId());
        response.setAppointmentNo(appointment.getAppointmentNo());
        response.setAppointmentDate(request.getDate());
        response.setAppointmentTime(parseTimeSlot(request.getTimeSlot()));
        response.setStatus(appointment.getStatus());
        response.setRedirect("my_appointments");
        response.setCreatedAt(appointment.getCreatedAt());

        return R.ok(response);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> cancel(Long id, AppointmentCancelRequest request) {
        // 1. 查询预约，校验存在且状态IN(1,2)
        TradeAppointmentDO appointment = tradeAppointmentMapper.selectById(id);
        if (appointment == null) {
            throw new BusinessException(ErrorCode.APPOINTMENT_NOT_FOUND, "预约ID: " + id);
        }
        if (!Arrays.asList(1, 2).contains(appointment.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "当前预约状态不允许取消");
        }

        // 2. 校验距预约时间 > 2小时（不足2小时抛APPOINTMENT_CANCEL_TIMEOUT）
        LocalDateTime appointmentTime = LocalDateTime.of(appointment.getAppointmentDate(),
                appointment.getAppointmentTime());
        if (LocalDateTime.now().plusHours(2).isAfter(appointmentTime)) {
            throw new BusinessException(ErrorCode.APPOINTMENT_CANCEL_TIMEOUT);
        }

        // 3. 更新状态为5(已取消)
        appointment.setStatus(5);

        // 4. 设置cancelReason
        appointment.setCancelReason(request.getCancelReason());

        tradeAppointmentMapper.updateById(appointment);

        // 5. 释放时段（清除Redis缓存和锁记录）
        releaseSlot(appointment.getStoreId(), appointment.getTechnicianId(),
                appointment.getAppointmentDate(), appointment.getTimeSlot());

        return R.ok();
    }

    // ========== 私有方法 ==========

    private MyAppointmentVO.Group buildGroup(List<TradeAppointmentDO> appointments) {
        MyAppointmentVO.Group group = new MyAppointmentVO.Group();
        group.setCount(appointments.size());
        group.setList(appointments.stream().map(this::convertToItemVO).collect(Collectors.toList()));
        return group;
    }

    private MyAppointmentVO.AppointmentItemVO convertToItemVO(TradeAppointmentDO appointment) {
        MyAppointmentVO.AppointmentItemVO item = new MyAppointmentVO.AppointmentItemVO();
        item.setId(appointment.getId());
        item.setAppointmentNo(appointment.getAppointmentNo());
        item.setServiceItemName(appointment.getServiceItemName());
        item.setTechnicianName(appointment.getTechnicianName());
        item.setAppointmentDate(appointment.getAppointmentDate());
        item.setAppointmentTime(appointment.getAppointmentTime());
        item.setTimeSlot(appointment.getTimeSlot());
        item.setStatus(appointment.getStatus());
        item.setStatusName(STATUS_NAME_MAP.getOrDefault(appointment.getStatus(), "未知"));
        item.setCancelReason(appointment.getCancelReason());
        item.setCreatedAt(appointment.getCreatedAt());

        // 计算modifyCount
        int modifyCount = calculateModifyCount(appointment);
        item.setModifyCount(modifyCount);

        // 计算canModify和canCancel
        boolean isPendingStatus = Arrays.asList(1, 2).contains(appointment.getStatus());
        LocalDateTime appointmentTime = LocalDateTime.of(appointment.getAppointmentDate(),
                appointment.getAppointmentTime());
        boolean isMoreThan2Hours = LocalDateTime.now().plusHours(2).isBefore(appointmentTime);

        item.setCanModify(isPendingStatus && modifyCount < 2 && isMoreThan2Hours);
        item.setCanCancel(isPendingStatus && isMoreThan2Hours);

        return item;
    }

    private int calculateModifyCount(TradeAppointmentDO appointment) {
        // 通过modifyReason非空次数判断修改次数
        // 查询该预约的所有历史记录中modifyReason非空的次数
        // 简化实现：当前记录的modifyReason非空则+1
        int count = 0;
        if (appointment.getModifyReason() != null && !appointment.getModifyReason().isEmpty()) {
            count++;
        }
        return count;
    }

    private void releaseSlot(Long storeId, Long technicianId, LocalDate date, String timeSlot) {
        // 删除Redis锁标记
        String redisLockKey = REDIS_LOCK_PREFIX + storeId + ":" + technicianId + ":" + date + ":" + timeSlot;
        redisUtil.delete(redisLockKey);

        // 删除时段缓存
        String slotCacheKey = SLOT_CACHE_PREFIX + storeId + ":" + technicianId + ":" + date;
        redisUtil.delete(slotCacheKey);

        // 释放对应的锁记录
        LambdaQueryWrapper<TradeAppointmentLockDO> lockWrapper = new LambdaQueryWrapper<>();
        lockWrapper.eq(TradeAppointmentLockDO::getStoreId, storeId)
                .eq(TradeAppointmentLockDO::getTechnicianId, technicianId)
                .eq(TradeAppointmentLockDO::getLockDate, date)
                .eq(TradeAppointmentLockDO::getTimeSlot, timeSlot)
                .eq(TradeAppointmentLockDO::getStatus, 1);
        TradeAppointmentLockDO lockDO = tradeAppointmentLockMapper.selectOne(lockWrapper);
        if (lockDO != null) {
            appointmentLockService.releaseLock(lockDO.getLockId());
        }
    }

    private String generateAppointmentNo() {
        String datePart = LocalDate.now().format(DATE_FORMATTER);
        int seq = SEQUENCE.getAndIncrement();
        if (seq > 999999) {
            SEQUENCE.set(1);
            seq = 1;
        }
        return "APT" + datePart + String.format("%06d", seq);
    }

    private LocalTime parseTimeSlot(String timeSlot) {
        return LocalTime.parse(timeSlot, DateTimeFormatter.ofPattern("HH:mm"));
    }
}
