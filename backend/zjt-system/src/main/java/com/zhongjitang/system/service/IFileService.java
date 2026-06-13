package com.zhongjitang.system.service;

import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.system.domain.entity.SysFileDO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IFileService {

    R<PageResult<SysFileDO>> page(Integer page, Integer pageSize, String name, String type);

    R<SysFileDO> upload(MultipartFile file, String businessType, String businessId);

    R<SysFileDO> getById(Long id);

    R<Void> delete(Long id);

    R<List<SysFileDO>> getByBusiness(String businessType, String businessId);
}
