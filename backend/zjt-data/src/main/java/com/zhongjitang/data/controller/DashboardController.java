package com.zhongjitang.data.controller;

import com.zhongjitang.common.core.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/data/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard数据")
public class DashboardController {

    private final DataSource dataSource;

    @GetMapping("/summary")
    @Operation(summary = "Dashboard汇总数据")
    public R<Map<String, Object>> getSummary(
            @RequestParam(required = false) Long storeId) {
        Map<String, Object> result = new HashMap<>();
        try (Connection conn = dataSource.getConnection()) {
            // 今日订单数
            int todayOrders = count(conn, "SELECT COUNT(*) FROM trade_order WHERE DATE(created_at) = CURDATE()"
                    + (storeId != null ? " AND store_id = " + storeId : ""));
            result.put("todayOrders", todayOrders);

            // 今日营收（已支付订单金额总和）
            double todayRevenue = sum(conn, "SELECT COALESCE(SUM(paid_amount),0) FROM trade_order WHERE DATE(created_at) = CURDATE() AND status IN (2,3,4)"
                    + (storeId != null ? " AND store_id = " + storeId : ""));
            result.put("todayRevenue", todayRevenue);

            // 今日预约数
            int todayAppointments = count(conn, "SELECT COUNT(*) FROM trade_appointment WHERE appointment_date = CURDATE() AND deleted = 0"
                    + (storeId != null ? " AND store_id = " + storeId : ""));
            result.put("todayAppointments", todayAppointments);

            // 今日新增会员
            int newMembers = count(conn, "SELECT COUNT(*) FROM user_member WHERE DATE(created_at) = CURDATE() AND deleted = 0"
                    + (storeId != null ? " AND store_id = " + storeId : ""));
            result.put("newMembers", newMembers);

            // 在岗技师数
            int onlineTechnicians = count(conn, "SELECT COUNT(*) FROM store_technician WHERE is_online = 1 AND deleted = 0"
                    + (storeId != null ? " AND store_id = " + storeId : ""));
            result.put("onlineTechnicians", onlineTechnicians);

            // 总会员数
            int totalMembers = count(conn, "SELECT COUNT(*) FROM user_member WHERE deleted = 0"
                    + (storeId != null ? " AND store_id = " + storeId : ""));
            result.put("totalMembers", totalMembers);

            // 总门店数
            int totalStores = count(conn, "SELECT COUNT(*) FROM store_info WHERE deleted = 0 AND status = 1");
            result.put("totalStores", totalStores);

        } catch (Exception e) {
            result.put("error", e.getMessage());
        }
        return R.ok(result);
    }

    private int count(Connection conn, String sql) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private double sum(Connection conn, String sql) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0;
        }
    }
}
