package com.zhongjitang.common.core.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页响应
 */
@Data
@Schema(description = "分页响应")
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "数据列表")
    private List<T> list;

    @Schema(description = "分页信息")
    private Pagination pagination;

    public PageResult() {
    }

    public PageResult(List<T> list, Pagination pagination) {
        this.list = list;
        this.pagination = pagination;
    }

    public static <T> PageResult<T> of(List<T> list, long total, int page, int pageSize) {
        Pagination pagination = new Pagination();
        pagination.setPage(page);
        pagination.setPageSize(pageSize);
        pagination.setTotal(total);
        pagination.setTotalPages((int) Math.ceil((double) total / pageSize));
        return new PageResult<>(list, pagination);
    }

    @Data
    @Schema(description = "分页信息")
    public static class Pagination implements Serializable {

        private static final long serialVersionUID = 1L;

        @Schema(description = "当前页码", example = "1")
        private Integer page;

        @Schema(description = "每页条数", example = "20")
        private Integer pageSize;

        @Schema(description = "总记录数", example = "100")
        private Long total;

        @Schema(description = "总页数", example = "5")
        private Integer totalPages;
    }
}
