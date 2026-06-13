package com.zhongjitang.data.service.impl;

import com.zhongjitang.common.core.result.R;
import com.zhongjitang.data.service.RankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingServiceImpl implements RankingService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public R<Map<String, Object>> getStoreRanking(String period, String dimension, int page, int pageSize) {
        LocalDateTime startTime = getStartTime(period);
        LocalDateTime endTime = LocalDateTime.now();

        // 构建排序字段
        String orderColumn;
        switch (dimension) {
            case "customer":
                orderColumn = "customer_count";
                break;
            case "rating":
                orderColumn = "avg_rating";
                break;
            default:
                orderColumn = "total_revenue";
                break;
        }

        // 查询门店排行数据
        String dataSql = "SELECT si.id AS store_id, si.store_no, si.store_name, " +
                "COALESCE(t.total_revenue, 0) AS revenue, " +
                "COALESCE(t.customer_count, 0) AS customer_count, " +
                "COALESCE(t.avg_rating, 0) AS rating, " +
                "COALESCE(t.revenue_change, 0) AS revenue_change, " +
                "COALESCE(t.customer_change, 0) AS customer_change, " +
                "COALESCE(t.rating_change, 0) AS rating_change " +
                "FROM store_info si " +
                "LEFT JOIN (" +
                "  SELECT store_id, " +
                "    SUM(paid_amount) AS total_revenue, " +
                "    COUNT(DISTINCT member_id) AS customer_count, " +
                "    0 AS avg_rating, " +
                "    0 AS revenue_change, " +
                "    0 AS customer_change, " +
                "    0 AS rating_change " +
                "  FROM trade_order " +
                "  WHERE is_deleted = 0 AND payment_status = 1 " +
                "    AND created_at >= ? AND created_at <= ? " +
                "  GROUP BY store_id" +
                ") t ON si.id = t.store_id " +
                "WHERE si.is_deleted = 0 " +
                "ORDER BY " + orderColumn + " DESC " +
                "LIMIT ? OFFSET ?";

        int offset = (page - 1) * pageSize;

        List<Map<String, Object>> list = jdbcTemplate.queryForList(dataSql, startTime, endTime, pageSize, offset);

        // 查询总数
        String countSql = "SELECT COUNT(*) FROM store_info WHERE is_deleted = 0";
        Long total = jdbcTemplate.queryForObject(countSql, Long.class);

        // 格式化结果
        List<Map<String, Object>> formattedList = new ArrayList<>();
        for (Map<String, Object> row : list) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("storeId", row.get("store_id"));
            item.put("storeNo", row.get("store_no"));
            item.put("storeName", row.get("store_name"));
            item.put("revenue", toDouble(row.get("revenue")));
            item.put("customerCount", toInt(row.get("customer_count")));
            item.put("rating", toDouble(row.get("rating")));
            item.put("revenueChange", toDouble(row.get("revenue_change")));
            item.put("customerChange", toDouble(row.get("customer_change")));
            item.put("ratingChange", toDouble(row.get("rating_change")));
            formattedList.add(item);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", formattedList);
        Map<String, Object> pagination = new LinkedHashMap<>();
        pagination.put("page", page);
        pagination.put("pageSize", pageSize);
        pagination.put("total", total != null ? total : 0);
        pagination.put("totalPages", (int) Math.ceil((double) (total != null ? total : 0) / pageSize));
        result.put("pagination", pagination);

        return R.ok(result);
    }

    @Override
    public R<Map<String, Object>> getTechnicianRanking(String period, String dimension, int page, int pageSize) {
        // 构建排序字段
        String orderColumn;
        switch (dimension) {
            case "service":
                orderColumn = "service_count";
                break;
            case "rating":
                orderColumn = "rating";
                break;
            case "promotion":
                orderColumn = "promotion_count";
                break;
            default:
                orderColumn = "revenue";
                break;
        }

        String dataSql = "SELECT st.id AS technician_id, st.technician_no, " +
                "COALESCE(ue.name, '') AS technician_name, " +
                "COALESCE(si.store_name, '') AS store_name, " +
                "COALESCE(st.month_revenue, 0) AS revenue, " +
                "COALESCE(st.month_service_count, 0) AS service_count, " +
                "COALESCE(st.month_rating, 0) AS rating, " +
                "0 AS promotion_count, " +
                "0 AS revenue_change, " +
                "0 AS service_change, " +
                "0 AS rating_change, " +
                "0 AS promotion_change " +
                "FROM store_technician st " +
                "LEFT JOIN user_employee ue ON st.employee_id = ue.id AND ue.is_deleted = 0 " +
                "LEFT JOIN store_info si ON st.store_id = si.id AND si.is_deleted = 0 " +
                "WHERE st.is_deleted = 0 " +
                "ORDER BY " + orderColumn + " DESC " +
                "LIMIT ? OFFSET ?";

        int offset = (page - 1) * pageSize;

        List<Map<String, Object>> list = jdbcTemplate.queryForList(dataSql, pageSize, offset);

        // 查询总数
        String countSql = "SELECT COUNT(*) FROM store_technician WHERE is_deleted = 0";
        Long total = jdbcTemplate.queryForObject(countSql, Long.class);

        // 格式化结果
        List<Map<String, Object>> formattedList = new ArrayList<>();
        for (Map<String, Object> row : list) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("technicianId", row.get("technician_id"));
            item.put("technicianNo", row.get("technician_no"));
            item.put("technicianName", row.get("technician_name"));
            item.put("storeName", row.get("store_name"));
            item.put("revenue", toDouble(row.get("revenue")));
            item.put("serviceCount", toInt(row.get("service_count")));
            item.put("rating", toDouble(row.get("rating")));
            item.put("promotionCount", toInt(row.get("promotion_count")));
            item.put("revenueChange", toDouble(row.get("revenue_change")));
            item.put("serviceChange", toDouble(row.get("service_change")));
            item.put("ratingChange", toDouble(row.get("rating_change")));
            item.put("promotionChange", toDouble(row.get("promotion_change")));
            formattedList.add(item);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", formattedList);
        Map<String, Object> paginationMap = new LinkedHashMap<>();
        paginationMap.put("page", page);
        paginationMap.put("pageSize", pageSize);
        paginationMap.put("total", total != null ? total : 0);
        paginationMap.put("totalPages", (int) Math.ceil((double) (total != null ? total : 0) / pageSize));
        result.put("pagination", paginationMap);

        return R.ok(result);
    }

    private LocalDateTime getStartTime(String period) {
        LocalDate today = LocalDate.now();
        switch (period) {
            case "week":
                return today.minusWeeks(1).atStartOfDay();
            case "quarter":
                return today.minusMonths(3).atStartOfDay();
            default: // month
                return today.minusMonths(1).atStartOfDay();
        }
    }

    private double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof BigDecimal) return ((BigDecimal) value).doubleValue();
        if (value instanceof Number) return ((Number) value).doubleValue();
        return 0.0;
    }

    private int toInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Number) return ((Number) value).intValue();
        return 0;
    }
}
