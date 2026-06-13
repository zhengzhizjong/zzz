package com.zhongjitang.data.service;

import com.zhongjitang.common.core.result.R;

import java.util.Map;

public interface RankingService {

    R<Map<String, Object>> getStoreRanking(String period, String dimension, int page, int pageSize);

    R<Map<String, Object>> getTechnicianRanking(String period, String dimension, int page, int pageSize);
}
