<template>
  <div class="workspace-page">
    <!-- 技师信息卡片 -->
    <div class="info-card">
      <div class="info-header">
        <div class="avatar">{{ technicianInfo.name?.charAt(0) || '技' }}</div>
        <div class="info-detail">
          <div class="name">{{ technicianInfo.name || '技师' }}</div>
          <div class="tags">
            <van-tag type="primary" size="medium">{{ levelText }}</van-tag>
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
          <div class="time">{{ formatTime(item.appointmentTime) }}</div>
          <div class="info">
            <div class="customer">{{ item.memberName || item.memberId || '客户' }}</div>
            <div class="service">{{ item.serviceName || '理疗服务' }}</div>
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
import { getWorkspace, getPerformance, getTechnicianDetail } from '../../api/technician'
import { getTodayAppointments, checkIn, checkOut } from '../../api/appointment'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()
const technicianInfo = ref<any>({})
const appointments = ref<any[]>([])
const isCheckedIn = ref(false)
const techId = ref<number>(0)

const levelMap: Record<number, string> = { 1: '初级技师', 2: '中级技师', 3: '高级技师', 4: '资深技师', 5: '首席技师' }
const levelText = computed(() => levelMap[technicianInfo.value.skillLevel] || '技师')

const statusType = computed(() => isCheckedIn.value ? 'success' : 'warning')
const statusText = computed(() => isCheckedIn.value ? '在岗' : '未签到')

function getStatusType(status: number) {
  const map: Record<number, string> = { 0: 'warning', 1: 'primary', 2: 'success', 3: 'default', 4: 'danger' }
  return map[status] || 'default'
}

function getStatusText(status: number) {
  const map: Record<number, string> = { 0: '待确认', 1: '已确认', 2: '服务中', 3: '已完成', 4: '已取消' }
  return map[status] || '未知'
}

function formatTime(time: string) {
  if (!time) return ''
  return time.substring(11, 16) || time
}

async function loadData() {
  try {
    // 从登录信息获取员工ID，查询对应的技师记录
    const empId = userStore.userInfo?.id
    if (!empId) return

    // 先获取技师详情（通过员工ID关联）
    // 后端技师列表API支持查询，先用workspace获取基本信息
    const wsRes: any = await getWorkspace(empId)
    const wsData = wsRes.data || {}

    // 获取技师详情
    try {
      const detailRes: any = await getTechnicianDetail(empId)
      const detail = detailRes.data || {}
      technicianInfo.value = {
        name: detail.name || userStore.userInfo?.name || '技师',
        skillLevel: detail.skillLevel || 1,
        storeName: detail.storeName || '忠济堂',
        id: detail.id
      }
      techId.value = detail.id || empId
    } catch {
      technicianInfo.value = {
        name: userStore.userInfo?.name || '技师',
        skillLevel: 1,
        storeName: '忠济堂',
        id: empId
      }
      techId.value = empId
    }

    // 获取预约列表
    try {
      const aptRes: any = await getTodayAppointments({ pageSize: 50 })
      const records = aptRes.data?.records || aptRes.data || []
      appointments.value = Array.isArray(records) ? records : []
    } catch {
      appointments.value = []
    }

    isCheckedIn.value = wsData.technicianInfo?.isOnline === 1
  } catch {
    technicianInfo.value = { name: userStore.userInfo?.name || '技师', skillLevel: 1, storeName: '忠济堂' }
  }
}

async function handleCheckIn() {
  try {
    await checkIn(techId.value)
    isCheckedIn.value = true
    showToast('签到成功')
  } catch {
    showToast('签到失败')
  }
}

async function handleCheckOut() {
  try {
    await checkOut(techId.value)
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
