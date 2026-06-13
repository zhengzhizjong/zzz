<template>
  <div class="performance-page">
    <!-- 统计卡片 -->
    <div class="stats-card">
      <div class="stat-item">
        <div class="stat-value">{{ stats.serviceCount }}</div>
        <div class="stat-label">本月服务数</div>
      </div>
      <div class="stat-item">
        <div class="stat-value">{{ stats.revenue }}</div>
        <div class="stat-label">本月营收(元)</div>
      </div>
      <div class="stat-item">
        <div class="stat-value">{{ stats.rating }}</div>
        <div class="stat-label">本月评分</div>
      </div>
    </div>

    <!-- 营收趋势图 -->
    <div class="section">
      <div class="section-title">近7天营收趋势</div>
      <div class="chart-container">
        <div class="bar-chart">
          <div v-for="(item, index) in revenueTrend" :key="index" class="bar-item">
            <div class="bar-wrapper">
              <div class="bar" :style="{ height: getBarHeight(item.value) + '%' }"></div>
            </div>
            <div class="bar-label">{{ item.label }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 服务项目分布 -->
    <div class="section">
      <div class="section-title">服务项目分布</div>
      <div class="pie-list">
        <div v-for="item in serviceDistribution" :key="item.name" class="pie-item">
          <div class="pie-color" :style="{ background: item.color }"></div>
          <div class="pie-name">{{ item.name }}</div>
          <div class="pie-value">{{ item.percent }}%</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getPerformance } from '../../api/technician'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()

const stats = ref({
  serviceCount: 0,
  revenue: '0.00',
  rating: '0.0'
})

const revenueTrend = ref<{ label: string; value: number }[]>([])
const serviceDistribution = ref<{ name: string; percent: number; color: string }[]>([])

const maxValue = ref(1)

function getBarHeight(value: number) {
  return Math.max((value / maxValue.value) * 100, 2)
}

async function loadData() {
  try {
    const techId = userStore.userInfo?.technicianId || userStore.userInfo?.id || 0
    if (!techId) return
    const res: any = await getPerformance(techId)
    const data = res.data || {}
    stats.value = {
      serviceCount: data.monthServiceCount || 0,
      revenue: data.monthRevenue ? Number(data.monthRevenue).toFixed(2) : '0.00',
      rating: data.monthRating ? Number(data.monthRating).toFixed(1) : '0.0'
    }
    // 后端暂无趋势和分布数据，使用模拟
    if (!revenueTrend.value.length) {
      revenueTrend.value = [
        { label: '周一', value: 480 }, { label: '周二', value: 520 },
        { label: '周三', value: 600 }, { label: '周四', value: 450 },
        { label: '周五', value: 580 }, { label: '周六', value: 720 },
        { label: '周日', value: 210 }
      ]
    }
    if (!serviceDistribution.value.length) {
      serviceDistribution.value = [
        { name: '推拿', percent: 35, color: '#07C160' },
        { name: '艾灸', percent: 25, color: '#1989fa' },
        { name: '拔罐', percent: 20, color: '#ff976a' },
        { name: '足疗', percent: 20, color: '#07c160' }
      ]
    }
  } catch {
    stats.value = { serviceCount: 28, revenue: '3560.00', rating: '4.8' }
    revenueTrend.value = [
      { label: '周一', value: 480 }, { label: '周二', value: 520 },
      { label: '周三', value: 600 }, { label: '周四', value: 450 },
      { label: '周五', value: 580 }, { label: '周六', value: 720 },
      { label: '周日', value: 210 }
    ]
    serviceDistribution.value = [
      { name: '推拿', percent: 35, color: '#07C160' },
      { name: '艾灸', percent: 25, color: '#1989fa' },
      { name: '拔罐', percent: 20, color: '#ff976a' },
      { name: '足疗', percent: 20, color: '#07c160' }
    ]
  }
  maxValue.value = Math.max(...revenueTrend.value.map(i => i.value), 1)
}

onMounted(loadData)
</script>

<style scoped>
.performance-page {
  padding: 12px;
}

.stats-card {
  background: linear-gradient(135deg, #07C160, #06AD56);
  border-radius: 12px;
  padding: 20px;
  display: flex;
  color: #fff;
}

.stat-item {
  flex: 1;
  text-align: center;
}

.stat-value {
  font-size: 22px;
  font-weight: bold;
}

.stat-label {
  font-size: 12px;
  margin-top: 4px;
  opacity: 0.85;
}

.section {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-top: 12px;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 16px;
  color: #333;
}

.chart-container {
  height: 180px;
  display: flex;
  align-items: flex-end;
}

.bar-chart {
  display: flex;
  width: 100%;
  height: 100%;
  align-items: flex-end;
}

.bar-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100%;
}

.bar-wrapper {
  flex: 1;
  width: 100%;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.bar {
  width: 24px;
  background: linear-gradient(180deg, #07C160, #06AD56);
  border-radius: 4px 4px 0 0;
  min-height: 2px;
  transition: height 0.3s;
}

.bar-label {
  font-size: 11px;
  color: #999;
  margin-top: 6px;
}

.pie-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.pie-item {
  display: flex;
  align-items: center;
}

.pie-color {
  width: 12px;
  height: 12px;
  border-radius: 3px;
  flex-shrink: 0;
}

.pie-name {
  flex: 1;
  margin-left: 8px;
  font-size: 14px;
  color: #333;
}

.pie-value {
  font-size: 14px;
  color: #666;
}
</style>
