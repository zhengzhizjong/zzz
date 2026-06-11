<template>
  <div class="workspace-page">
    <!-- 技师信息卡片 -->
    <div class="info-card">
      <div class="info-header">
        <div class="avatar">{{ technicianInfo.name?.charAt(0) || '技' }}</div>
        <div class="info-detail">
          <div class="name">{{ technicianInfo.name || '技师' }}</div>
          <div class="tags">
            <van-tag type="primary" size="medium">{{ technicianInfo.level || '初级技师' }}</van-tag>
            <van-tag plain size="medium">{{ technicianInfo.storeName || '忠济堂' }}</van-tag>
          </div>
        </div>
        <van-tag :type="statusType" size="large" class="status-tag">{{ statusText }}</van-tag>
      </div>
    </div>

    <!-- 快捷操作 -->
    <div class="action-bar">
      <van-button type="primary" size="small" :disabled="isCheckedIn" @click="handleCheckIn" icon="checked">签到</van-button>
      <van-button type="danger" size="small" :disabled="!isCheckedIn" @click="handleCheckOut" icon="cross">签退</van-button>
    </div>

    <!-- 今日预约 -->
    <div class="section">
      <div class="section-title">今日待服务</div>
      <van-empty v-if="appointments.length === 0" description="暂无预约" />
      <div v-else class="appointment-list">
        <div v-for="item in appointments" :key="item.id" class="appointment-item" @click="goDetail(item.id)">
          <div class="time">{{ item.appointmentTime }}</div>
          <div class="info">
            <div class="customer">{{ item.customerName }}</div>
            <div class="service">{{ item.serviceName }}</div>
          </div>
          <van-tag :type="getStatusType(item.status)">{{ getStatusText(item.status) }}</van-tag>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { showToast } from 'vant'
import { getWorkspace, getPerformance } from '../../api/technician'
import { getTodayAppointments, checkIn, checkOut } from '../../api/appointment'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()
const technicianInfo = ref<any>({})
const appointments = ref<any[]>([])
const isCheckedIn = ref(false)

const statusType = computed(() => isCheckedIn.value ? 'success' : 'warning')
const statusText = computed(() => isCheckedIn.value ? '在岗' : '未签到')

function getStatusType(status: string) {
  const map: Record<string, string> = { pending: 'warning', confirmed: 'primary', in_service: 'success', completed: 'default' }
  return map[status] || 'default'
}

function getStatusText(status: string) {
  const map: Record<string, string> = { pending: '待确认', confirmed: '已确认', in_service: '服务中', completed: '已完成' }
  return map[status] || status
}

async function loadData() {
  try {
    const techId = userStore.userInfo?.id || 1
    const [wsRes, aptRes] = await Promise.all([
      getWorkspace(techId),
      getTodayAppointments(techId)
    ])
    technicianInfo.value = (wsRes as any).data || { name: '技师', level: '初级技师', storeName: '忠济堂' }
    appointments.value = (aptRes as any).data?.records || (aptRes as any).data || []
    isCheckedIn.value = (wsRes as any).data?.checkedIn || false
  } catch {
    technicianInfo.value = { name: '技师', level: '初级技师', storeName: '忠济堂' }
  }
}

async function handleCheckIn() {
  try {
    const techId = userStore.userInfo?.id || 1
    await checkIn(techId)
    isCheckedIn.value = true
    showToast('签到成功')
  } catch {
    showToast('签到失败')
  }
}

async function handleCheckOut() {
  try {
    const techId = userStore.userInfo?.id || 1
    await checkOut(techId)
    isCheckedIn.value = false
    showToast('签退成功')
  } catch {
    showToast('签退失败')
  }
}

function goDetail(id: number) {
  // 可扩展跳转预约详情
}

onMounted(loadData)
</script>

<style scoped>
.workspace-page {
  padding: 12px;
}

.info-card {
  background: linear-gradient(135deg, #07C160, #06AD56);
  border-radius: 12px;
  padding: 20px;
  color: #fff;
}

.info-header {
  display: flex;
  align-items: center;
}

.avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: rgba(255,255,255,0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: bold;
  flex-shrink: 0;
}

.info-detail {
  flex: 1;
  margin-left: 12px;
}

.name {
  font-size: 18px;
  font-weight: bold;
}

.tags {
  margin-top: 6px;
  display: flex;
  gap: 6px;
}

.tags .van-tag {
  background: rgba(255,255,255,0.2) !important;
  color: #fff !important;
  border-color: rgba(255,255,255,0.4) !important;
}

.status-tag {
  flex-shrink: 0;
}

.action-bar {
  display: flex;
  gap: 12px;
  margin: 16px 0;
}

.action-bar .van-button {
  flex: 1;
}

.section {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 12px;
  color: #333;
}

.appointment-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.appointment-item:last-child {
  border-bottom: none;
}

.time {
  font-size: 14px;
  color: #07C160;
  font-weight: 500;
  width: 60px;
  flex-shrink: 0;
}

.info {
  flex: 1;
  margin-left: 8px;
}

.customer {
  font-size: 14px;
  color: #333;
}

.service {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}
</style>
