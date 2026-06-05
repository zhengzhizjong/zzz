<template>
  <div class="promotion-funnel">
    <!-- 筛选栏 -->
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            @change="handleSearch"
          />
        </el-form-item>
        <el-form-item label="门店">
          <el-select v-model="filterForm.storeId" placeholder="全部门店" clearable @change="handleSearch">
            <el-option
              v-for="store in storeOptions"
              :key="store.id"
              :label="store.storeName"
              :value="store.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="技师">
          <el-select v-model="filterForm.technicianId" placeholder="全部技师" clearable filterable @change="handleSearch">
            <el-option
              v-for="tech in technicianOptions"
              :key="tech.id"
              :label="tech.employeeName || tech.technicianNo"
              :value="tech.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- ROI + 总体转化率 -->
    <el-row :gutter="16" class="overview-row">
      <el-col :span="12">
        <el-card shadow="never" class="overview-card">
          <div class="overview-content">
            <div class="overview-label">总体转化率</div>
            <div class="overview-value">{{ overallRate }}%</div>
            <div class="overview-desc">分享 → 复购</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="overview-card">
          <div class="overview-content">
            <div class="overview-label">ROI</div>
            <div class="overview-value roi-value">{{ roiValue }}</div>
            <div class="overview-desc">佣金支出 / 新增营收</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 推广漏斗图 -->
    <el-card class="funnel-card" shadow="never">
      <template #header>
        <span class="card-title">推广转化漏斗</span>
      </template>
      <div class="funnel-container">
        <div
          v-for="(step, index) in funnelSteps"
          :key="step.key"
          class="funnel-step"
        >
          <div class="funnel-bar-wrapper">
            <div
              class="funnel-bar"
              :style="{
                width: getBarWidth(index) + '%',
                backgroundColor: step.color,
              }"
            >
              <div class="funnel-bar-content">
                <span class="step-name">{{ step.label }}</span>
                <span class="step-count">{{ getStepCount(step.key) }}</span>
              </div>
            </div>
          </div>
          <div v-if="index < funnelSteps.length - 1" class="funnel-arrow">
            <div class="conversion-info">
              <span class="conversion-rate">{{ getConversionRate(step.key) }}%</span>
              <span class="conversion-label">转化率</span>
            </div>
            <el-icon><ArrowDown /></el-icon>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 按渠道分组 -->
    <el-card class="channel-card" shadow="never">
      <template #header>
        <span class="card-title">渠道数据明细</span>
      </template>
      <el-table :data="channelData" stripe border>
        <el-table-column prop="channel" label="渠道" min-width="120" />
        <el-table-column prop="click" label="点击" width="100" align="center" />
        <el-table-column prop="register" label="注册" width="100" align="center" />
        <el-table-column prop="firstVisit" label="到店" width="100" align="center" />
        <el-table-column prop="firstOrder" label="首单" width="100" align="center" />
        <el-table-column prop="repurchase" label="复购" width="100" align="center" />
        <el-table-column label="点击→注册" width="110" align="center">
          <template #default="{ row }">
            {{ row.click ? ((row.register / row.click) * 100).toFixed(1) + '%' : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="注册→首单" width="110" align="center">
          <template #default="{ row }">
            {{ row.register ? ((row.firstOrder / row.register) * 100).toFixed(1) + '%' : '-' }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { getPromotionFunnel, type PromotionFunnelData } from '@/api/data/promotion-funnel'
import { getStoreList } from '@/api/store/info'
import { getTechnicianList } from '@/api/store/technician'
import type { StoreInfo } from '@/types/store'
import type { TechnicianInfo } from '@/types/technician'
import dayjs from 'dayjs'

const dateRange = ref<[string, string]>([
  dayjs().subtract(30, 'day').format('YYYY-MM-DD'),
  dayjs().format('YYYY-MM-DD'),
])

const filterForm = reactive({
  storeId: undefined as number | undefined,
  technicianId: undefined as number | undefined,
})

const storeOptions = ref<StoreInfo[]>([])
const technicianOptions = ref<TechnicianInfo[]>([])
const funnelData = ref<PromotionFunnelData | null>(null)
const loading = ref(false)

const funnelSteps = [
  { key: 'share', label: '分享', color: '#07C160' },
  { key: 'click', label: '点击', color: '#36D399' },
  { key: 'register', label: '注册', color: '#6BE0B5' },
  { key: 'firstVisit', label: '到店', color: '#95EACC' },
  { key: 'firstOrder', label: '首单', color: '#BFF0E2' },
  { key: 'repurchase', label: '复购', color: '#E5F9F1' },
]

const overallRate = computed(() => {
  if (!funnelData.value) return 0
  return funnelData.value.conversion?.overall ?? 0
})

const roiValue = computed(() => {
  if (!funnelData.value) return '-'
  const roi = funnelData.value.conversion?.roi
  return roi !== undefined ? roi.toFixed(2) : '-'
})

const channelData = computed(() => {
  return funnelData.value?.byChannel ?? []
})

function getStepCount(key: string): number {
  return funnelData.value?.funnel[key]?.count ?? 0
}

function getConversionRate(key: string): string {
  if (!funnelData.value) return '0.0'
  const keys = funnelSteps.map(s => s.key)
  const idx = keys.indexOf(key)
  if (idx >= keys.length - 1) return '-'
  const current = funnelData.value.funnel[key]?.count ?? 0
  const next = funnelData.value.funnel[keys[idx + 1]]?.count ?? 0
  if (current === 0) return '0.0'
  return ((next / current) * 100).toFixed(1)
}

function getBarWidth(index: number): number {
  if (!funnelData.value) return 100 - index * 12
  const firstCount = funnelData.value.funnel[funnelSteps[0].key]?.count ?? 0
  if (firstCount === 0) return 100 - index * 12
  const currentCount = funnelData.value.funnel[funnelSteps[index].key]?.count ?? 0
  const ratio = currentCount / firstCount
  return Math.max(ratio * 100, 20)
}

async function fetchStoreOptions() {
  try {
    const res: any = await getStoreList({ page: 1, pageSize: 999 })
    storeOptions.value = res.data.list || []
  } catch {
    storeOptions.value = []
  }
}

async function fetchTechnicianOptions() {
  try {
    const res: any = await getTechnicianList({ page: 1, pageSize: 999 })
    technicianOptions.value = res.data.list || []
  } catch {
    technicianOptions.value = []
  }
}

async function fetchData() {
  loading.value = true
  try {
    const params: any = {}
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    if (filterForm.storeId) params.storeId = filterForm.storeId
    if (filterForm.technicianId) params.technicianId = filterForm.technicianId
    const res: any = await getPromotionFunnel(params)
    funnelData.value = res.data || null
  } catch {
    funnelData.value = null
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  fetchData()
}

function handleReset() {
  dateRange.value = [
    dayjs().subtract(30, 'day').format('YYYY-MM-DD'),
    dayjs().format('YYYY-MM-DD'),
  ]
  filterForm.storeId = undefined
  filterForm.technicianId = undefined
  fetchData()
}

onMounted(() => {
  fetchStoreOptions()
  fetchTechnicianOptions()
  fetchData()
})
</script>

<style scoped lang="scss">
.promotion-funnel {
  padding: 20px;
}

.filter-card {
  margin-bottom: 16px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

.overview-row {
  margin-bottom: 16px;
}

.overview-card {
  height: 100%;
}

.overview-content {
  text-align: center;
  padding: 16px 0;
}

.overview-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.overview-value {
  font-size: 42px;
  font-weight: 700;
  color: #07C160;
  line-height: 1.2;
}

.roi-value {
  color: #FF9D00;
}

.overview-desc {
  font-size: 12px;
  color: #C0C4CC;
  margin-top: 8px;
}

.funnel-card {
  margin-bottom: 16px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.funnel-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
}

.funnel-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

.funnel-bar-wrapper {
  display: flex;
  justify-content: center;
  width: 100%;
}

.funnel-bar {
  height: 56px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: width 0.5s ease;
  min-width: 200px;
}

.funnel-bar-content {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #303133;
  font-weight: 500;
}

.step-name {
  font-size: 14px;
}

.step-count {
  font-size: 18px;
  font-weight: 700;
}

.funnel-arrow {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 4px 0;
}

.conversion-info {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.conversion-rate {
  font-size: 16px;
  font-weight: 700;
  color: #07C160;
}

.conversion-label {
  font-size: 11px;
  color: #909399;
}

.channel-card {
  margin-bottom: 16px;
}
</style>
