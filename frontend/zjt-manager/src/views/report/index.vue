<template>
  <div class="report-page">
    <!-- 顶部统计卡片 -->
    <el-row :gutter="16">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-label">今日营收</div>
            <div class="stat-value revenue">¥{{ stats.todayRevenue }}</div>
          </div>
          <el-icon class="stat-icon" :size="40" color="#409eff"><Coin /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-label">今日客流</div>
            <div class="stat-value customer">{{ stats.todayCustomers }}</div>
          </div>
          <el-icon class="stat-icon" :size="40" color="#67c23a"><User /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-label">今日预约数</div>
            <div class="stat-value appointment">{{ stats.todayAppointments }}</div>
          </div>
          <el-icon class="stat-icon" :size="40" color="#e6a23c"><Calendar /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-label">平均客单价</div>
            <div class="stat-value avg-price">¥{{ stats.avgOrderPrice }}</div>
          </div>
          <el-icon class="stat-icon" :size="40" color="#f56c6c"><TrendCharts /></el-icon>
        </el-card>
      </el-col>
    </el-row>

    <!-- 日期范围选择 -->
    <el-card shadow="never" class="filter-card" style="margin-top: 16px;">
      <el-form :inline="true" :model="filters">
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="filters.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-radio-group v-model="filters.quickRange" @change="handleQuickRange">
            <el-radio-button value="7">近7天</el-radio-button>
            <el-radio-button value="30">近30天</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
          <el-button type="success" @click="handleExport">导出报表</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="20" style="margin-top: 16px;">
      <!-- 营收趋势 -->
      <el-col :span="14">
        <el-card shadow="hover">
          <template #header>
            <span>营收趋势</span>
          </template>
          <div ref="revenueChartRef" style="height: 380px;"></div>
        </el-card>
      </el-col>
      <!-- 服务项目排行 -->
      <el-col :span="10">
        <el-card shadow="hover">
          <template #header>
            <span>服务项目排行</span>
          </template>
          <div ref="serviceRankChartRef" style="height: 380px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 技师业绩排行 -->
    <el-card shadow="hover" style="margin-top: 16px;">
      <template #header>
        <span>技师业绩排行</span>
      </template>
      <div ref="technicianRankChartRef" style="height: 350px;"></div>
    </el-card>

    <!-- 技师绩效表 -->
    <el-card shadow="hover" style="margin-top: 16px;">
      <template #header>
        <span>技师绩效明细</span>
      </template>
      <el-table :data="technicianPerformance" stripe v-loading="loading">
        <el-table-column prop="name" label="技师" width="100" />
        <el-table-column prop="orderCount" label="订单数" width="100" />
        <el-table-column prop="revenue" label="营收" width="120">
          <template #default="{ row }">¥{{ row.revenue }}</template>
        </el-table-column>
        <el-table-column prop="avgRating" label="平均评分" width="100" />
        <el-table-column prop="checkInDays" label="出勤天数" width="100" />
        <el-table-column prop="completionRate" label="完成率" width="100">
          <template #default="{ row }">{{ row.completionRate }}%</template>
        </el-table-column>
        <el-table-column prop="complaintCount" label="投诉数" width="80" />
      </el-table>
    </el-card>

    <!-- AI 日报 -->
    <el-card shadow="hover" style="margin-top: 16px;">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>AI 智能日报</span>
          <el-button type="primary" size="small" @click="loadDailyReport" :loading="reportLoading">生成日报</el-button>
        </div>
      </template>
      <div v-if="dailyReport" class="daily-report" v-html="dailyReport"></div>
      <el-empty v-else description="点击生成日报获取AI智能分析" :image-size="80" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { Coin, User, Calendar, TrendCharts } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getStoreRanking, getTechnicianRanking } from '@/api/ranking'
import { getDailyReport } from '@/api/report'
import { getStoreDetail } from '@/api/store'
import { getOrderList } from '@/api/order'

const userStore = useUserStore()
const loading = ref(false)
const reportLoading = ref(false)
const revenueChartRef = ref<HTMLElement>()
const serviceRankChartRef = ref<HTMLElement>()
const technicianRankChartRef = ref<HTMLElement>()

const filters = reactive({
  dateRange: null as string[] | null,
  quickRange: '7'
})

const stats = reactive({
  todayRevenue: '0.00',
  todayCustomers: 0,
  todayAppointments: 0,
  avgOrderPrice: '0.00'
})

const technicianPerformance = ref<Array<Record<string, any>>>([])
const dailyReport = ref('')

let revenueChart: echarts.ECharts | null = null
let serviceRankChart: echarts.ECharts | null = null
let technicianRankChart: echarts.ECharts | null = null

function resetFilters() {
  filters.dateRange = null
  filters.quickRange = '7'
  loadData()
}

function handleQuickRange(val: string | number) {
  const days = Number(val)
  const end = new Date()
  const start = new Date()
  start.setDate(start.getDate() - days + 1)
  filters.dateRange = [start.toISOString().slice(0, 10), end.toISOString().slice(0, 10)]
  loadData()
}

function getDefaultDateRange() {
  const end = new Date()
  const start = new Date()
  start.setDate(start.getDate() - 6)
  return [start.toISOString().slice(0, 10), end.toISOString().slice(0, 10)]
}

function generateRecentDates(days: number) {
  return Array.from({ length: days }, (_, i) => {
    const d = new Date()
    d.setDate(d.getDate() - (days - 1 - i))
    return d.toISOString().slice(0, 10)
  })
}

function generateMockRevenue(days: number) {
  return Array.from({ length: days }, () => Math.round(2000 + Math.random() * 5000))
}

async function loadStats() {
  try {
    const storeId = userStore.storeId
    if (storeId) {
      const storeRes: any = await getStoreDetail(storeId)
      const data = storeRes.data || storeRes
      stats.todayRevenue = data.todayRevenue?.toFixed(2) || '0.00'
      stats.todayCustomers = data.todayCustomers || 0
      stats.todayAppointments = data.todayAppointments || 0
      stats.avgOrderPrice = data.avgOrderPrice?.toFixed(2) || '0.00'
    }
    const today = new Date().toISOString().slice(0, 10)
    const orderRes: any = await getOrderList({ startDate: today, endDate: today, pageSize: 1 })
    const orderData = orderRes.data || orderRes
    if (orderData.total > 0 && stats.todayCustomers === 0) {
      stats.todayCustomers = orderData.total
    }
  } catch {
    // 静默处理，保留默认值
  }
}

async function loadData() {
  loading.value = true
  try {
    const dateRange = filters.dateRange?.length === 2 ? filters.dateRange : getDefaultDateRange()
    const params = { startDate: dateRange[0], endDate: dateRange[1] }
    const [storeRes, techRes]: any[] = await Promise.all([
      getStoreRanking(params),
      getTechnicianRanking(params)
    ])
    const storeData = storeRes.data || storeRes
    const techData = techRes.data || techRes

    technicianPerformance.value = techData.list || techData || []

    await nextTick()
    initRevenueChart(storeData)
    initServiceRankChart(storeData)
    initTechnicianRankChart(techData)
  } finally {
    loading.value = false
  }
}

function initRevenueChart(data: Record<string, any>) {
  if (!revenueChartRef.value) return
  if (!revenueChart) {
    revenueChart = echarts.init(revenueChartRef.value)
  }
  const dates = data.revenueTrend?.dates || generateRecentDates(7)
  const values = data.revenueTrend?.values || generateMockRevenue(7)

  revenueChart.setOption({
    tooltip: { trigger: 'axis', formatter: '{b}<br/>营收: ¥{c}' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: dates, axisLabel: { rotate: 45 } },
    yAxis: { type: 'value', name: '营收(元)' },
    series: [{
      type: 'line',
      data: values,
      smooth: true,
      areaStyle: { opacity: 0.3 },
      itemStyle: { color: '#409eff' }
    }]
  })
}

function initServiceRankChart(data: Record<string, any>) {
  if (!serviceRankChartRef.value) return
  if (!serviceRankChart) {
    serviceRankChart = echarts.init(serviceRankChartRef.value)
  }
  const services = data.servicePopularity || [
    { name: '推拿理疗', value: 120 },
    { name: '艾灸养生', value: 85 },
    { name: '拔罐排毒', value: 60 },
    { name: '刮痧疏通', value: 45 },
    { name: '足部保健', value: 75 },
    { name: '肩颈调理', value: 95 }
  ]

  serviceRankChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: services.map((s: any) => s.name), axisLabel: { rotate: 30 } },
    yAxis: { type: 'value', name: '次数' },
    series: [{
      type: 'bar',
      data: services.map((s: any) => s.value),
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#409eff' },
          { offset: 1, color: '#79bbff' }
        ]),
        borderRadius: [4, 4, 0, 0]
      },
      barWidth: '40%'
    }]
  })
}

function initTechnicianRankChart(data: Record<string, any>) {
  if (!technicianRankChartRef.value) return
  if (!technicianRankChart) {
    technicianRankChart = echarts.init(technicianRankChartRef.value)
  }
  const list = data.list || data || []
  const top10 = list.slice(0, 10)
  const names = top10.map((t: any) => t.name)
  const revenues = top10.map((t: any) => t.revenue || 0)

  technicianRankChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, formatter: '{b}<br/>营收: ¥{c}' },
    grid: { left: '3%', right: '8%', bottom: '3%', containLabel: true },
    xAxis: { type: 'value', name: '营收(元)' },
    yAxis: { type: 'category', data: names.reverse(), inverse: false },
    series: [{
      type: 'bar',
      data: revenues.reverse(),
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#67c23a' },
          { offset: 1, color: '#95d475' }
        ]),
        borderRadius: [0, 4, 4, 0]
      },
      barWidth: '50%',
      label: { show: true, position: 'right', formatter: '¥{c}' }
    }]
  })
}

async function loadDailyReport() {
  reportLoading.value = true
  try {
    const dateRange = filters.dateRange?.length === 2 ? filters.dateRange : getDefaultDateRange()
    const res: any = await getDailyReport({ storeId: userStore.storeId, startDate: dateRange[0], endDate: dateRange[1] })
    dailyReport.value = res.data?.report || res.data?.content || res.data || ''
  } catch {
    dailyReport.value = '<p style="color: #909399;">日报生成失败，请稍后重试</p>'
  } finally {
    reportLoading.value = false
  }
}

function handleExport() {
  ElMessage.info('导出功能开发中，敬请期待')
}

function handleResize() {
  revenueChart?.resize()
  serviceRankChart?.resize()
  technicianRankChart?.resize()
}

onMounted(async () => {
  loadStats()
  handleQuickRange(7)
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  revenueChart?.dispose()
  serviceRankChart?.dispose()
  technicianRankChart?.dispose()
})
</script>

<style scoped>
.report-page {
  padding: 0;
}
.filter-card :deep(.el-card__body) {
  padding-bottom: 2px;
}
.stat-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 20px;
}
.stat-content {
  flex: 1;
}
.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}
.stat-value {
  font-size: 28px;
  font-weight: 700;
}
.stat-value.revenue {
  color: #409eff;
}
.stat-value.customer {
  color: #67c23a;
}
.stat-value.appointment {
  color: #e6a23c;
}
.stat-value.avg-price {
  color: #f56c6c;
}
.stat-icon {
  opacity: 0.2;
}
.daily-report {
  line-height: 1.8;
  color: #303133;
  font-size: 14px;
}
.daily-report :deep(h3) {
  margin: 16px 0 8px;
  color: #409eff;
}
.daily-report :deep(ul) {
  padding-left: 20px;
}
.daily-report :deep(li) {
  margin-bottom: 4px;
}
</style>
