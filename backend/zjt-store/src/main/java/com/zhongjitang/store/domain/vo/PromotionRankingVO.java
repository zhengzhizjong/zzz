package com.zhongjitang.store.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "推广排行榜VO")
public class PromotionRankingVO {

    @Schema(description = "统计周期")
    private String period;

    @Schema(description = "我的排名")
    private RankItem myRank;

    @Schema(description = "排行榜列表")
    private List<RankItem> ranking;

    @Data
    @Schema(description = "排名项")
    public static class RankItem {

        @Schema(description = "排名")
        private Integer rank;

        @Schema(description = "技师ID")
        private Long technicianId;

        @Schema(description = "技师姓名")
        private String technicianName;

        @Schema(description = "头像")
        private String avatar;

        @Schema(description = "推广总数")
        private Integer totalPromoted;

        @Schema(description = "总佣金")
        private BigDecimal totalCommission;
    }
}
