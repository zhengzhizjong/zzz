package com.zhongjitang.common.core.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.Instant;

/**
 * 统一响应体
 *
 * @param <T> 数据类型
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "统一响应体")
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "状态码，0表示成功")
    private Integer code;

    @Schema(description = "提示信息")
    private String message;

    @Schema(description = "响应数据")
    private T data;

    @Schema(description = "请求ID")
    private String requestId;

    @Schema(description = "时间戳")
    private Instant timestamp;

    private R() {
        this.timestamp = Instant.now();
    }

    public static <T> R<T> ok() {
        return ok(null);
    }

    public static <T> R<T> ok(T data) {
        R<T> r = new R<>();
        r.setCode(0);
        r.setMessage("success");
        r.setData(data);
        return r;
    }

    public static <T> R<T> ok(T data, String message) {
        R<T> r = new R<>();
        r.setCode(0);
        r.setMessage(message);
        r.setData(data);
        return r;
    }

    public static <T> R<T> fail(Integer code, String message) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMessage(message);
        return r;
    }

    public static <T> R<T> fail(String message) {
        return fail(50001, message);
    }

    /**
     * 设置请求ID
     */
    public R<T> requestId(String requestId) {
        this.requestId = requestId;
        return this;
    }
}
