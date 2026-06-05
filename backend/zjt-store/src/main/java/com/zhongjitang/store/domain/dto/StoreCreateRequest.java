package com.zhongjitang.store.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@Schema(description = "创建门店请求")
public class StoreCreateRequest {

    @NotBlank(message = "门店名称不能为空")
    @Schema(description = "门店名称", example = "忠济堂国医馆(朝阳店)")
    private String storeName;

    @NotNull(message = "门店类型不能为空")
    @Schema(description = "门店类型:1直营 2加盟", example = "1")
    private Integer storeType;

    @NotBlank(message = "省份编码不能为空")
    @Schema(description = "省份编码", example = "110000")
    private String provinceCode;

    @NotBlank(message = "城市编码不能为空")
    @Schema(description = "城市编码", example = "110105")
    private String cityCode;

    @NotBlank(message = "区县编码不能为空")
    @Schema(description = "区县编码", example = "110105")
    private String districtCode;

    @NotBlank(message = "详细地址不能为空")
    @Schema(description = "详细地址", example = "北京市朝阳区建国路88号")
    private String address;

    @Schema(description = "纬度", example = "39.908823")
    private BigDecimal latitude;

    @Schema(description = "经度", example = "116.397470")
    private BigDecimal longitude;

    @NotBlank(message = "联系人姓名不能为空")
    @Schema(description = "联系人姓名", example = "张三")
    private String contactName;

    @NotBlank(message = "联系电话不能为空")
    @Schema(description = "联系电话", example = "13800138000")
    private String contactPhone;

    @NotNull(message = "营业开始时间不能为空")
    @Schema(description = "营业开始时间", example = "09:00")
    private LocalTime businessStartTime;

    @NotNull(message = "营业结束时间不能为空")
    @Schema(description = "营业结束时间", example = "21:00")
    private LocalTime businessEndTime;
}
