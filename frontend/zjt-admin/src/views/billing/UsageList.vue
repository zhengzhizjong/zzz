<template>
  <div class="usage-list">
    <!-- 筛选栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="选择租户">
          <el-select v-model="searchForm.tenantId" placeholder="请选择租户" filterable clearable @change="handleSearch">
            <el-option
              v-for="item in tenantOptions"
              :key="item.id"
              :label="item.tenantName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="月份">
          <el-date-picker
            v-model="searchForm.month"
            type="month"
            placeholder="选择月份"
            format="YYYY-MM"
            value-format="YYYY-MM"
            @change="handleSearch"
          />
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 用量汇总 -->
    <el-card v-if="summary" shadow="never" class="summary-card">
      <template #header>
        <span class="card-title">用量汇总</span>
      </template>
      <el-row :gutter="20">
        <el-col :span="8" v-for="item in summaryItems" :key="item.label">
          <div class="usage-item">
            <div class="usage-label">{{ item.label }}</div>
            <el-progress
              :percentage="item.percentage"
              :color="getProgressColor(item.percentage)"
              :stroke-width="18"
              :text-inside="true"
              :format="() => `${item.current} / ${item.quota}`"
            />
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 用量明细 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <span class="card-title">用量明细</span>
      </template>
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="resourceType" label="资源类型" width="130" />
        <el-table-column prop="action" label="操作" width="150" />
        <el-table-column prop="quantity" label="用量" width="100" align="center" />
        <el-table-column prop="quota" label="配额" width="100" align="center" />
        <el-table-column label="使用率" width="200" align="center">
          <template #default="{ row }">
            <el-progress
              :percentage="Math.min(Math.round((row.quantity / row.quota) * 100), 100)"
              :color="getProgressColor(Math.round((row.quantity / row.quota) * 100))"
              :stroke-width="14"
            />
          </template>
        </el-table-column>
        <el-table-column prop="recordedAt" label="记录时间" min-width="170" />
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchUsageDetail"
          @current-change="fetchUsageDetail"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { getTenantList, type TenantInfo } from '@/api/billing/tenant'
import { getUsage, getUsageSummary } from '@/api/billing/usage'
import dayjs from 'dayjs'

const loading = ref(false)
const tenantOptions = ref<TenantInfo[]>([])
const summary = ref<any>(null)
const tableData = ref<any[]>([])

const searchForm = reactive({
  tenantId: undefined as number | undefined,
  month: dayjs().format('YYYY-MM'),
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

const summaryItems = computed(() => {
  if (!summary.value) return []
  return [
    {
      label: '门店数',
      current: summary.value.currentStores || 0,
      quota: summary.value.maxStores || 1,
      percentage: Math.min(Math.round(((summary.value.currentStores || 0) / (summary.value.maxStores || 1)) * 100), 100),
    },
    {
      label: '技师数',
      current: summary.value.currentTechnicians || 0,
      quota: summary.value.maxTechnicians || 1,
      percentage: Math.min(Math.round(((summary.value.currentTechnicians || 0) / (summary.value.maxTechnicians || 1)) * 100), 100),
    },
    {
      label: 'AI调用量',
      current: summary.value.aiUsage || 0,
      quota: summary.value.aiQuota || 1,
      percentage: Math.min(Math.round(((summary.value.aiUsage || 0) / (summary.value.aiQuota || 1)) * 100), 100),
    },
  ]
})

function getProgressColor(percentage: number): string {
  if (percentage > 100) return '#FA5151'
  if (percentage >= 80) return '#FF9D00'
  return '#07C160'
}

async function fetchTenantOptions() {
  try {
    const res: any = await getTenantList({ page: 1, pageSize: 999 })
    tenantOptions.value = res.data.list || []
  } catch {
    tenantOptions.value = []
  }
}

async function fetchSummary() {
  if (!searchForm.tenantId || !searchForm.month) {
    summary.value = null
    return
  }
  try {
    const res: any = await getUsageSummary(searchForm.tenantId, searchForm.month)
    summary.value = res.data || null
  } catch {
    summary.value = null
  }
}

async function fetchUsageDetail() {
  if (!searchForm.tenantId) {
    tableData.value = []
    return
  }
  loading.value = true
  try {
    const res: any = await getUsage(searchForm.tenantId, {
      page: pagination.page,
      pageSize: pagination.pageSize,
      month: searchForm.month || undefined,
    })
    tableData.value = res.data.list || []
    pagination.total = res.data.pagination?.total || 0
  } catch {
    tableData.value = []
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  fetchSummary()
  fetchUsageDetail()
}

onMounted(() => {
  fetchTenantOptions()
})
</script>

<style scoped lang="scss">
.usage-list {
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

.summary-card {
  margin-bottom: 16px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
}

.usage-item {
  padding: 12px 0;
}

.usage-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
