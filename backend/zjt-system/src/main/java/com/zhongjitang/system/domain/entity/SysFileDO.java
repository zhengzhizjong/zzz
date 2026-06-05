package com.zhongjitang.system.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhongjitang.common.mybatis.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_file")
public class SysFileDO extends BaseDO {

    private String fileName;

    private String originalName;

    private String filePath;

    private Long fileSize;

    /** 文件类型: image/document/video/other */
    private String fileType;

    private String mimeType;

    private String bucketName;

    private String url;

    private String md5;

    private String businessType;

    private String businessId;
}
