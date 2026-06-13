<template>
  <div class="home-container">
    <div class="home-header">
      <h1>忠济堂·中医养生连锁管理系统</h1>
      <p class="welcome-text">欢迎回来，{{ userStore.userInfo?.name || '管理员' }}</p>
    </div>
    <el-row :gutter="20" class="dashboard-cards">
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>今日预约</template>
          <div class="card-value">{{ dashboardData.todayAppointments }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>今日营收</template>
          <div class="card-value">¥{{ dashboardData.todayRevenue }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>在岗技师</template>
          <div class="card-value">{{ dashboardData.onlineTechnicians }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>门店数量</template>
          <div class="card-value">{{ dashboardData.storeCount }}</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { reactive, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getAppointmentList } from '@/api/trade/appointment'
import { getTechnicianList } from '@/api/store/technician'
import { getStoreList } from '@/api/store/info'
import { getOrderList } from '@/api/trade/order'

const userStore = useUserStore()

const dashboardData = reactive({
  todayAppointments: '--' as string | number,
  todayRevenue: '--' as string | number,
  onlineTechnicians: '--' as string | number,
  storeCount: '--' as string | number,
})

async function fetchDashboardData() {
  const today = new Date()
  const todayStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`

  try {
    // 今日预约数
    const appointmentRes: any = await getAppointmentList({
      page: 1,
      pageSize: 1,
      startDate: todayStr,
      endDate: todayStr,
    })
    dashboardData.todayAppointments = appointmentRes.data?.pagination?.total ?? 0
  } catch {
    dashboardData.todayAppointments = 0
  }

  try {
    // 今日营收
    const orderRes: any = await getOrderList({
      page: 1,
      pageSize: 1,
      startDate: todayStr,
      endDate: todayStr,
    })
    dashboardData.todayRevenue = (orderRes.data?.pagination?.total ?? 0).toFixed(2)
  } catch {
    dashboardData.todayRevenue = '0.00'
  }

  try {
    // 在岗技师数
    const technicianRes: any = await getTechnicianList({
      page: 1,
      pageSize: 1,
      isOnline: 1,
    })
    dashboardData.onlineTechnicians = technicianRes.data?.pagination?.total ?? 0
  } catch {
    dashboardData.onlineTechnicians = 0
  }

  try {
    // 门店数量
    const storeRes: any = await getStoreList({ page: 1, pageSize: 1 })
    dashboardData.storeCount = storeRes.data?.pagination?.total ?? 0
  } catch {
    dashboardData.storeCount = 0
  }
}

onMounted(() => {
  fetchDashboardData()
})
</script>

<style scoped lang="scss">
.home-container {
  padding: 20px;
}

.home-header {
  margin-bottom: 30px;

  h1 {
    font-size: 24px;
    color: #303133;
    margin: 0 0 8px;
  }
}

.welcome-text {
  color: #909399;
  font-size: 14px;
}

.card-value {
  font-size: 28px;
  font-weight: bold;
  color: #07C160;
  text-align: center;
  padding: 20px 0;
}
</style>
