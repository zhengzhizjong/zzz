package com.zhongjitang.content.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhongjitang.common.mybatis.entity.BaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("content_service_item")
@Schema(description = "服务项目")
public class ContentServiceItemDO extends BaseDO {

    @Schema(description = "项目编号")
    private String itemNo;

    @Schema(description = "项目名称")
    private String itemName;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "售价")
    private BigDecimal price;

    @Schema(description = "成本价")
    private BigDecimal costPrice;

    @Schema(description = "时长(分钟)")
    private Integer durationMinutes;

    @Schema(description = "提成类型:1固定 2比例")
    private Integer commissionType;

    @Schema(description = "提成值")
    private BigDecimal commissionValue;

    @Schema(description = "是否套餐:0否 1是")
    private Integer isPackage;

    @Schema(description = "是否AI推荐:0否 1是")
    private Integer isAiRecommended;

    @Schema(description = "标签")
    private String tags;

    @Schema(description = "项目描述")
    private String description;

    @Schema(description = "状态:1上架 2下架 3维护中")
    private Integer status;

    @Schema(description = "排序")
    private Integer sortOrder;
}
