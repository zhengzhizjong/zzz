<template>
  <div class="technician-page">
    <el-card shadow="never">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>技师状态看板</span>
          <el-button @click="loadTechnicians">
            <el-icon><Refresh /></el-icon> 刷新
          </el-button>
        </div>
      </template>

      <div v-loading="loading" class="technician-board">
        <div v-for="tech in technicians" :key="tech.id" class="tech-card" :class="{ 'off-duty': !tech.onDuty }">
          <div class="tech-avatar">
            <el-avatar :size="56" :style="{ background: tech.onDuty ? '#409eff' : '#c0c4cc' }">
              {{ tech.name?.charAt(0) || '?' }}
            </el-avatar>
          </div>
          <div class="tech-info">
            <div class="tech-name">{{ tech.name }}</div>
            <div class="tech-level">{{ tech.level || '技师' }}</div>
            <el-tag :type="tech.onDuty ? 'success' : 'info'" size="small">
              {{ tech.onDuty ? '在岗' : '离岗' }}
            </el-tag>
          </div>
          <div class="tech-status">
            <div v-if="tech.currentAppointment" class="current-task">
              <div class="task-label">当前服务</div>
              <div class="task-customer">{{ tech.currentAppointment.customerName }}</div>
              <div class="task-service">{{ tech.currentAppointment.serviceName }}</div>
            </div>
            <div v-else-if="tech.onDuty" class="idle-status">空闲</div>
          </div>
          <div class="tech-action">
            <el-switch
              :model-value="tech.onDuty"
              active-text="在岗"
              inactive-text="离岗"
              @change="(val: any) => handleToggleDuty(tech, val)"
            />
          </div>
        </div>
      </div>

      <div v-if="technicians.length === 0 && !loading" class="empty-text">暂无技师数据</div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getTechnicianList } from '@/api/technician'
import { put } from '@/utils/request'
import { getStoreId } from '@/utils/auth'

const loading = ref(false)
const technicians = ref<any[]>([])

async function loadTechnicians() {
  loading.value = true
  try {
    const storeId = getStoreId() || ''
    const res: any = await getTechnicianList({ storeId, page: 1, pageSize: 100 })
    technicians.value = res.data?.list || res.data?.records || []
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

async function handleToggleDuty(tech: any, val: boolean) {
  try {
    await put(`/api/v1/store/technicians/${tech.id}/duty`, { onDuty: val })
    tech.onDuty = val
    ElMessage.success(val ? `${tech.name} 已设为在岗` : `${tech.name} 已设为离岗`)
  } catch {
    // handled by interceptor
  }
}

onMounted(() => {
  loadTechnicians()
})
</script>

<style scoped>
.technician-page {
  padding: 4px;
}
.technician-board {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}
.tech-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background: #fff;
  transition: all 0.2s;
}
.tech-card.off-duty {
  opacity: 0.6;
}
.tech-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}
.tech-info {
  flex: 1;
  min-width: 0;
}
.tech-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
.tech-level {
  font-size: 12px;
  color: #909399;
  margin: 2px 0 4px;
}
.tech-status {
  text-align: center;
  min-width: 60px;
}
.current-task {
  font-size: 12px;
}
.task-label {
  color: #909399;
  margin-bottom: 2px;
}
.task-customer {
  color: #303133;
  font-weight: 500;
}
.task-service {
  color: #409eff;
}
.idle-status {
  color: #67c23a;
  font-weight: 600;
  font-size: 14px;
}
.tech-action {
  flex-shrink: 0;
}
.empty-text {
  color: #909399;
  text-align: center;
  padding: 40px 0;
}
</style>
