<template>
  <div class="daily-report">
    <!-- 操作栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="门店">
          <el-select v-model="searchForm.storeId" placeholder="请选择门店" filterable clearable>
            <el-option
              v-for="item in storeOptions"
              :key="item.id"
              :label="item.storeName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker
            v-model="searchForm.date"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleGenerate">生成日报</el-button>
          <el-button @click="fetchReportList">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 日报列表 - 卡片式展示 -->
    <div class="report-cards" v-loading="loading">
      <el-empty v-if="reportList.length === 0 && !loading" description="暂无日报数据" />
      <el-row :gutter="16">
        <el-col :xs="24" :sm="12" :md="8" v-for="report in reportList" :key="report.id">
          <el-card shadow="hover" class="report-card" @click="handleViewDetail(report)">
            <div class="report-card-header">
              <div class="report-store">{{ report.storeName || '全部门店' }}</div>
              <el-tag type="success" size="small">{{ report.date }}</el-tag>
            </div>
            <div class="report-card-body">
              <div class="report-summary">{{ report.summary || '暂无摘要' }}</div>
            </div>
            <div class="report-card-footer">
              <span class="report-time">生成时间：{{ report.createdAt }}</span>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div class="pagination-wrapper" v-if="pagination.total > 0">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[9, 18, 36]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchReportList"
        @current-change="fetchReportList"
      />
    </div>

    <!-- 日报详情弹窗 -->
    <el-dialog
      v-model="detailVisible"
      title="日报详情"
      width="700px"
      destroy-on-close
    >
      <div v-loading="detailLoading" class="report-detail">
        <template v-if="reportDetail">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="门店">{{ reportDetail.storeName || '全部门店' }}</el-descriptions-item>
            <el-descriptions-item label="日期">{{ reportDetail.date }}</el-descriptions-item>
          </el-descriptions>

          <div class="detail-section" v-if="reportDetail.summary">
            <h4>摘要</h4>
            <p>{{ reportDetail.summary }}</p>
          </div>

          <div class="detail-section" v-if="reportDetail.keyMetrics">
            <h4>关键指标</h4>
            <el-row :gutter="12">
              <el-col :span="6" v-for="(value, key) in reportDetail.keyMetrics" :key="key">
                <div class="metric-item">
                  <div class="metric-value">{{ value }}</div>
                  <div class="metric-label">{{ key }}</div>
                </div>
              </el-col>
            </el-row>
          </div>

          <div class="detail-section" v-if="reportDetail.analysis">
            <h4>分析</h4>
            <p>{{ reportDetail.analysis }}</p>
          </div>

          <div class="detail-section" v-if="reportDetail.suggestions">
            <h4>建议</h4>
            <p>{{ reportDetail.suggestions }}</p>
          </div>
        </template>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { generateReport, getReportList, getReportDetail } from '@/api/ai/report'
import dayjs from 'dayjs'

interface StoreOption {
  id: number
  storeName: string
}

const loading = ref(false)
const detailLoading = ref(false)
const reportList = ref<any[]>([])
const reportDetail = ref<any>(null)
const detailVisible = ref(false)
const storeOptions = ref<StoreOption[]>([])

const searchForm = reactive({
  storeId: undefined as number | undefined,
  date: dayjs().format('YYYY-MM-DD'),
})

const pagination = reactive({
  page: 1,
  pageSize: 9,
  total: 0,
})

async function fetchStoreOptions() {
  // 模拟门店选项，实际应从门店API获取
  try {
    const { getStoreList } = await import('@/api/store/info')
    const res: any = await getStoreList({ page: 1, pageSize: 999 })
    storeOptions.value = (res.data.list || []).map((item: any) => ({
      id: item.id,
      storeName: item.storeName,
    }))
  } catch {
    storeOptions.value = []
  }
}

async function fetchReportList() {
  loading.value = true
  try {
    const res: any = await getReportList({
      page: pagination.page,
      pageSize: pagination.pageSize,
      storeId: searchForm.storeId || undefined,
      date: searchForm.date || undefined,
    })
    reportList.value = res.data?.list || res.data || []
    pagination.total = res.data?.pagination?.total || 0
  } catch {
    reportList.value = []
  } finally {
    loading.value = false
  }
}

async function handleGenerate() {
  if (!searchForm.date) {
    ElMessage.warning('请选择日期')
    return
  }
  try {
    await generateReport({
      storeId: searchForm.storeId || undefined,
      date: searchForm.date,
    })
    ElMessage.success('日报生成成功')
    fetchReportList()
  } catch {
    // 错误已在拦截器中处理
  }
}

async function handleViewDetail(report: any) {
  detailVisible.value = true
  detailLoading.value = true
  try {
    const res: any = await getReportDetail(report.id)
    reportDetail.value = res.data || null
  } catch {
    reportDetail.value = null
  } finally {
    detailLoading.value = false
  }
}

onMounted(() => {
  fetchStoreOptions()
  fetchReportList()
})
</script>

<style scoped lang="scss">
.daily-report {
  padding: 20px;
}

.search-card {
  margin-bottom: 16px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

.report-cards {
  min-height: 200px;
}

.report-card {
  margin-bottom: 16px;
  cursor: pointer;
  transition: transform 0.2s;

  &:hover {
    transform: translateY(-2px);
  }
}

.report-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.report-store {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.report-card-body {
  margin-bottom: 12px;
}

.report-summary {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.report-card-footer {
  border-top: 1px solid #ebeef5;
  padding-top: 8px;
}

.report-time {
  font-size: 12px;
  color: #909399;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.report-detail {
  min-height: 200px;
}

.detail-section {
  margin-top: 20px;

  h4 {
    font-size: 15px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 10px;
    padding-left: 10px;
    border-left: 3px solid #07C160;
  }

  p {
    font-size: 14px;
    color: #606266;
    line-height: 1.8;
    white-space: pre-wrap;
  }
}

.metric-item {
  text-align: center;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 8px;
}

.metric-value {
  font-size: 22px;
  font-weight: 700;
  color: #07C160;
}

.metric-label {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
