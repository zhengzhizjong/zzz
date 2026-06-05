package com.zhongjitang.store.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "更新房间请求")
public class StoreRoomUpdateRequest {

    @Schema(description = "房间编号", example = "R001")
    private String roomNo;

    @Schema(description = "房间名称", example = "VIP1号房")
    private String roomName;

    @Schema(description = "房间类型: 1普通 2VIP 3套间", example = "1")
    private Integer roomType;

    @Schema(description = "楼层", example = "2")
    private Integer floor;

    @Schema(description = "容纳人数", example = "2")
    private Integer capacity;

    @Schema(description = "设备描述", example = "空调、电视、独立卫浴")
    private String equipment;
}
