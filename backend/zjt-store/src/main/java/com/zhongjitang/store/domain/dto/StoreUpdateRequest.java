package com.zhongjitang.store.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@Schema(description = "更新门店请求")
public class StoreUpdateRequest {

    @Schema(description = "门店名称", example = "忠济堂国医馆(朝阳店)")
    private String storeName;

    @Schema(description = "门店类型:1直营 2加盟", example = "1")
    private Integer storeType;

    @Schema(description = "省份编码", example = "110000")
    private String provinceCode;

    @Schema(description = "城市编码", example = "110105")
    private String cityCode;

    @Schema(description = "区县编码", example = "110105")
    private String districtCode;

    @Schema(description = "详细地址", example = "北京市朝阳区建国路88号")
    private String address;

    @Schema(description = "纬度", example = "39.908823")
    private BigDecimal latitude;

    @Schema(description = "经度", example = "116.397470")
    private BigDecimal longitude;

    @Schema(description = "联系人姓名", example = "张三")
    private String contactName;

    @Schema(description = "联系电话", example = "13800138000")
    private String contactPhone;

    @Schema(description = "营业开始时间", example = "09:00")
    private LocalTime businessStartTime;

    @Schema(description = "营业结束时间", example = "21:00")
    private LocalTime businessEndTime;

    @Schema(description = "房间数", example = "10")
    private Integer roomCount;

    @Schema(description = "技师数", example = "5")
    private Integer technicianCount;

    @Schema(description = "区域ID")
    private Long regionId;
}
