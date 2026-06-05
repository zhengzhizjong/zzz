package com.zhongjitang.store.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Schema(description = "创建房间请求")
public class StoreRoomCreateRequest {

    @NotNull(message = "门店ID不能为空")
    @Schema(description = "门店ID", example = "1")
    private Long storeId;

    @NotBlank(message = "房间编号不能为空")
    @Schema(description = "房间编号", example = "R001")
    private String roomNo;

    @NotBlank(message = "房间名称不能为空")
    @Schema(description = "房间名称", example = "VIP1号房")
    private String roomName;

    @NotNull(message = "房间类型不能为空")
    @Schema(description = "房间类型: 1普通 2VIP 3套间", example = "1")
    private Integer roomType;

    @Schema(description = "楼层", example = "2")
    private Integer floor;

    @Schema(description = "容纳人数", example = "2")
    private Integer capacity;

    @Schema(description = "设备描述", example = "空调、电视、独立卫浴")
    private String equipment;
}
