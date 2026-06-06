package com.zhongjitang.system.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhongjitang.common.mybatis.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_file")
public class SysFileDO extends BaseDO {

    private String fileName;

    @TableField(exist = false)
    private String originalName;

    private String filePath;

    private Long fileSize;

    /** 文件类型: image/document/video/other */
    private String fileType;

    private String mimeType;

    @TableField(exist = false)
    private String bucketName;

    @TableField(exist = false)
    private String url;

    @TableField(exist = false)
    private String md5;

    @TableField("biz_type")
    private String businessType;

    @TableField("biz_id")
    private String businessId;
}
