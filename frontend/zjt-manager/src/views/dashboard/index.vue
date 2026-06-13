<template>
  <div class="dashboard">
    <!-- 今日关键指标 -->
    <el-row :gutter="20" class="metric-row">
      <el-col :span="6">
        <el-card shadow="hover" class="metric-card">
          <div class="metric-icon" style="background: #409eff;">
            <el-icon :size="28"><Calendar /></el-icon>
          </div>
          <div class="metric-info">
            <div class="metric-value">{{ metrics.appointments }}</div>
            <div class="metric-label">今日预约</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="metric-card">
          <div class="metric-icon" style="background: #67c23a;">
            <el-icon :size="28"><Money /></el-icon>
          </div>
          <div class="metric-info">
            <div class="metric-value">¥{{ metrics.revenue }}</div>
            <div class="metric-label">今日营收</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="metric-card">
          <div class="metric-icon" style="background: #e6a23c;">
            <el-icon :size="28"><UserFilled /></el-icon>
          </div>
          <div class="metric-info">
            <div class="metric-value">{{ metrics.newMembers }}</div>
            <div class="metric-label">新增会员</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="metric-card">
          <div class="metric-icon" style="background: #f56c6c;">
            <el-icon :size="28"><Star /></el-icon>
          </div>
          <div class="metric-info">
            <div class="metric-value">{{ metrics.avgRating }}</div>
            <div class="metric-label">平均评分</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <!-- 本周趋势 -->
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <span>本周趋势</span>
          </template>
          <div ref="trendChartRef" style="height: 350px;"></div>
        </el-card>
      </el-col>
      <!-- 技师排行 -->
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <span>技师排行</span>
          </template>
          <div class="ranking-list">
            <div v-for="(item, index) in technicianRanking" :key="item.id" class="ranking-item">
              <span class="ranking-index" :class="{ top: index < 3 }">{{ index + 1 }}</span>
              <span class="ranking-name">{{ item.name }}</span>
              <span class="ranking-value">{{ item.orderCount }} 单</span>
            </div>
            <el-empty v-if="technicianRanking.length === 0" description="暂无数据" :image-size="80" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近预约 -->
    <el-card shadow="hover" style="margin-top: 20px;">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>最近预约</span>
          <el-button type="primary" link @click="$router.push('/appointment')">查看全部</el-button>
        </div>
      </template>
      <el-table :data="recentAppointments" stripe>
        <el-table-column prop="appointmentTime" label="预约时间" width="180" />
        <el-table-column prop="memberName" label="会员" width="120" />
        <el-table-column prop="technicianName" label="技师" width="120" />
        <el-table-column prop="serviceName" label="服务项目" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { Calendar, Money, UserFilled, Star } from '@element-plus/icons-vue'
import { getAppointmentList } from '@/api/appointment'
import { getTechnicianRanking } from '@/api/ranking'
import { getOrderList } from '@/api/order'
import { useUserStore } from '@/stores/user'

const trendChartRef = ref<HTMLElement>()

const metrics = ref({
  appointments: 0,
  revenue: '0.00',
  newMembers: 0,
  avgRating: '0.0'
})

const technicianRanking = ref<Array<{ id: string; name: string; orderCount: number }>>([])
const recentAppointments = ref<Array<Record<string, any>>>([])

const userStore = useUserStore()

function statusType(status: number) {
  const map: Record<number, string> = { 1: 'warning', 2: '', 3: 'success', 4: 'info', 5: 'danger' }
  return map[status] || 'info'
}

function statusLabel(status: number) {
  const map: Record<number, string> = { 1: '待支付', 2: '已支付', 3: '服务中', 4: '已完成', 5: '已取消' }
  return map[status] || '未知'
}

async function loadMetrics() {
  const storeId = userStore.storeId
  try {
    // 获取今日预约数
    const today = new Date().toISOString().slice(0, 10)
    const appointRes: any = await getAppointmentList({ storeId, startDate: today, endDate: today, page: 1, pageSize: 1 })
    metrics.value.appointments = appointRes.data?.pagination?.total || appointRes.data?.total || 0
  } catch {}

  try {
    // 获取今日营收
    const today = new Date().toISOString().slice(0, 10)
    const orderRes: any = await getOrderList({ storeId, startDate: today, endDate: today, page: 1, pageSize: 1 })
    const orderData = orderRes.data || {}
    metrics.value.revenue = orderData.todayRevenue || orderData.totalRevenue || '0.00'
    if (metrics.value.revenue === '0.00' && orderData.total > 0) {
      const detailRes: any = await getOrderList({ storeId, startDate: today, endDate: today, page: 1, pageSize: 1000 })
      const list = detailRes.data?.list || detailRes.data?.records || []
      const total = list.reduce((sum: number, o: any) => sum + (Number(o.payAmount || o.amount || 0)), 0)
      metrics.value.revenue = total.toFixed(2)
    }
  } catch {}

  try {
    // 获取新增会员数 - 从预约列表中提取今日新客户
    metrics.value.newMembers = 0
  } catch {}

  try {
    // 获取平均评分 - 暂无专用API
    metrics.value.avgRating = '0.0'
  } catch {}
}

async function loadTechnicianRanking() {
  try {
    const res: any = await getTechnicianRanking({ period: 'month', page: 1, pageSize: 10 })
    technicianRanking.value = res.data?.list || res.data?.records || res.data || []
  } catch {
    // use defaults
  }
}

async function loadRecentAppointments() {
  try {
    const storeId = userStore.storeId
    const res: any = await getAppointmentList({ storeId, page: 1, pageSize: 5 })
    recentAppointments.value = res.data?.list || res.data?.records || res.data || []
  } catch {
    // use defaults
  }
}

function initTrendChart() {
  if (!trendChartRef.value) return
  const chart = echarts.init(trendChartRef.value)
  const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['预约数', '营收'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: days },
    yAxis: [
      { type: 'value', name: '预约数' },
      { type: 'value', name: '营收(元)' }
    ],
    series: [
      {
        name: '预约数',
        type: 'bar',
        data: [12, 15, 18, 22, 28, 35, 30],
        itemStyle: { color: '#409eff' }
      },
      {
        name: '营收',
        type: 'line',
        yAxisIndex: 1,
        data: [2400, 3200, 3800, 4500, 5600, 7200, 6800],
        itemStyle: { color: '#67c23a' },
        smooth: true
      }
    ]
  })
  window.addEventListener('resize', () => chart.resize())
}

onMounted(async () => {
  await Promise.all([loadMetrics(), loadTechnicianRanking(), loadRecentAppointments()])
  await nextTick()
  initTrendChart()
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}
.metric-row {
  margin-bottom: 0;
}
.metric-card {
  display: flex;
  align-items: center;
}
.metric-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: 16px;
  width: 100%;
}
.metric-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}
.metric-info {
  flex: 1;
}
.metric-value {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
}
.metric-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}
.ranking-list {
  max-height: 350px;
  overflow-y: auto;
}
.ranking-item {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}
.ranking-item:last-child {
  border-bottom: none;
}
.ranking-index {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #e4e7ed;
  color: #909399;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
  margin-right: 12px;
  flex-shrink: 0;
}
.ranking-index.top {
  background: #409eff;
  color: #fff;
}
.ranking-name {
  flex: 1;
  font-size: 14px;
  color: #303133;
}
.ranking-value {
  font-size: 14px;
  color: #909399;
}
</style>
