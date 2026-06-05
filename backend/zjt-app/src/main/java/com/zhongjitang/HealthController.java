package com.zhongjitang;

import com.zhongjitang.common.core.result.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {
    @GetMapping("/health")
    public R<Map<String, Object>> health() {
        Map<String, Object> info = new HashMap<>();
        info.put("status", "UP");
        info.put("timestamp", LocalDateTime.now());
        info.put("application", "zjt-platform");
        info.put("version", "1.0.0");
        return R.ok(info);
    }
}
