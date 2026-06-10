<template>
  <div class="report-page">
    <!-- 日期范围选择 -->
    <el-card shadow="never" class="filter-card">
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
      <!-- 服务热度 -->
      <el-col :span="10">
        <el-card shadow="hover">
          <template #header>
            <span>服务热度</span>
          </template>
          <div ref="serviceChartRef" style="height: 380px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 技师绩效表 -->
    <el-card shadow="hover" style="margin-top: 16px;">
      <template #header>
        <span>技师绩效</span>
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
      <el-empty v-else description="点击"生成日报"获取AI智能分析" :image-size="80" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { getStoreRanking, getTechnicianRanking } from '@/api/ranking'
import { getDailyReport } from '@/api/report'

const loading = ref(false)
const reportLoading = ref(false)
const revenueChartRef = ref<HTMLElement>()
const serviceChartRef = ref<HTMLElement>()

const filters = reactive({
  dateRange: null as string[] | null
})

const technicianPerformance = ref<Array<Record<string, any>>>([])
const dailyReport = ref('')

let revenueChart: echarts.ECharts | null = null
let serviceChart: echarts.ECharts | null = null

function resetFilters() {
  filters.dateRange = null
  loadData()
}

function getDefaultDateRange() {
  const end = new Date()
  const start = new Date()
  start.setDate(start.getDate() - 30)
  return [start.toISOString().slice(0, 10), end.toISOString().slice(0, 10)]
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
    initServiceChart(storeData)
  } finally {
    loading.value = false
  }
}

function initRevenueChart(data: Record<string, any>) {
  if (!revenueChartRef.value) return
  if (!revenueChart) {
    revenueChart = echarts.init(revenueChartRef.value)
  }
  const dates = data.revenueTrend?.dates || generateRecentDates(30)
  const values = data.revenueTrend?.values || generateMockRevenue(30)

  revenueChart.setOption({
    tooltip: { trigger: 'axis' },
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

function initServiceChart(data: Record<string, any>) {
  if (!serviceChartRef.value) return
  if (!serviceChart) {
    serviceChart = echarts.init(serviceChartRef.value)
  }
  const services = data.servicePopularity || [
    { name: '推拿理疗', value: 120 },
    { name: '艾灸养生', value: 85 },
    { name: '拔罐排毒', value: 60 },
    { name: '刮痧疏通', value: 45 },
    { name: '足部保健', value: 75 },
    { name: '肩颈调理', value: 95 }
  ]

  serviceChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
      label: { show: true, formatter: '{b}\n{d}%' },
      data: services
    }]
  })
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

async function loadDailyReport() {
  reportLoading.value = true
  try {
    const dateRange = filters.dateRange?.length === 2 ? filters.dateRange : getDefaultDateRange()
    const res: any = await getDailyReport({ startDate: dateRange[0], endDate: dateRange[1] })
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
  serviceChart?.resize()
}

onMounted(async () => {
  await loadData()
  window.addEventListener('resize', handleResize)
})
</script>

<style scoped>
.report-page {
  padding: 0;
}
.filter-card :deep(.el-card__body) {
  padding-bottom: 2px;
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
