<template>
  <div class="dashboard">
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #409eff"><el-icon :size="28"><Calendar /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.pendingCount }}</div>
            <div class="stat-label">待服务预约</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #e6a23c"><el-icon :size="28"><Timer /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.inServiceCount }}</div>
            <div class="stat-label">服务中</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #67c23a"><el-icon :size="28"><CircleCheck /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.completedCount }}</div>
            <div class="stat-label">已完成</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #f56c6c"><el-icon :size="28"><Money /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">¥{{ stats.todayRevenue }}</div>
            <div class="stat-label">今日营收</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span>在岗技师</span>
          </template>
          <div v-if="technicians.length === 0" class="empty-text">暂无在岗技师</div>
          <div v-else class="technician-grid">
            <div v-for="tech in technicians" :key="tech.id" class="tech-item">
              <el-avatar :size="40" :style="{ background: tech.onDuty ? '#67c23a' : '#909399' }">
                {{ tech.name?.charAt(0) || '?' }}
              </el-avatar>
              <div class="tech-name">{{ tech.name }}</div>
              <el-tag :type="tech.onDuty ? 'success' : 'info'" size="small">
                {{ tech.onDuty ? '在岗' : '离岗' }}
              </el-tag>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span>最近预约</span>
          </template>
          <el-table :data="recentAppointments" size="small" stripe>
            <el-table-column prop="customerName" label="客户" width="80" />
            <el-table-column prop="serviceName" label="项目" />
            <el-table-column prop="technicianName" label="技师" width="80" />
            <el-table-column prop="appointmentTime" label="预约时间" width="100" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getAppointmentList } from '@/api/appointment'
import { getTechnicianList } from '@/api/technician'
import { getStoreId } from '@/utils/auth'

const stats = reactive({
  pendingCount: 0,
  inServiceCount: 0,
  completedCount: 0,
  todayRevenue: '0.00'
})

const technicians = ref<any[]>([])
const recentAppointments = ref<any[]>([])

function statusTagType(status: string) {
  const map: Record<string, string> = {
    pending: 'warning',
    confirmed: '',
    in_service: 'success',
    completed: 'info',
    cancelled: 'danger'
  }
  return map[status] || ''
}

function statusLabel(status: string) {
  const map: Record<string, string> = {
    pending: '待确认',
    confirmed: '已确认',
    in_service: '服务中',
    completed: '已完成',
    cancelled: '已取消'
  }
  return map[status] || status
}

async function loadDashboard() {
  const storeId = getStoreId() || ''
  try {
    const [pendingRes, inServiceRes, completedRes] = await Promise.all([
      getAppointmentList({ storeId, status: 'pending', page: 1, pageSize: 1 }),
      getAppointmentList({ storeId, status: 'in_service', page: 1, pageSize: 1 }),
      getAppointmentList({ storeId, status: 'completed', page: 1, pageSize: 1 })
    ])
    stats.pendingCount = (pendingRes as any).data?.total || 0
    stats.inServiceCount = (inServiceRes as any).data?.total || 0
    stats.completedCount = (completedRes as any).data?.total || 0
  } catch {
    // silently handle
  }

  try {
    const allRes: any = await getAppointmentList({ storeId, page: 1, pageSize: 10 })
    recentAppointments.value = allRes.data?.list || allRes.data?.records || []
  } catch {
    // silently handle
  }

  try {
    const techRes: any = await getTechnicianList({ storeId, page: 1, pageSize: 50 })
    technicians.value = techRes.data?.list || techRes.data?.records || []
  } catch {
    // silently handle
  }
}

onMounted(() => {
  loadDashboard()
})
</script>

<style scoped>
.dashboard {
  padding: 4px;
}
.stat-card {
  display: flex;
  align-items: center;
}
.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: 16px;
  width: 100%;
}
.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}
.stat-info {
  flex: 1;
}
.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
}
.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}
.technician-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}
.tech-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  min-width: 70px;
}
.tech-name {
  font-size: 13px;
  color: #606266;
}
.empty-text {
  color: #909399;
  text-align: center;
  padding: 20px 0;
}
</style>
