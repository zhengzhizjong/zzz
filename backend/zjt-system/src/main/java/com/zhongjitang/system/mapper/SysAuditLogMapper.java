package com.zhongjitang.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhongjitang.system.domain.entity.SysAuditLogDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysAuditLogMapper extends BaseMapper<SysAuditLogDO> {
}
