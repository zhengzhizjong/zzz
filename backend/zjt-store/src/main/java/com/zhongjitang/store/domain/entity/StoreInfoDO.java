package com.zhongjitang.store.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhongjitang.common.mybatis.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("store_info")
public class StoreInfoDO extends BaseDO {

    private String storeNo;

    private String storeName;

    private Integer storeType;

    private Long brandId;

    private String provinceCode;

    private String cityCode;

    private String districtCode;

    private String address;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private String contactName;

    private String contactPhone;

    private LocalTime businessStartTime;

    private LocalTime businessEndTime;

    private Integer roomCount;

    private Integer technicianCount;

    private Long regionId;

    private Integer status;

    @TableField(exist = false)
    private Long storeId;
}
