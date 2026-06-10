<template>
  <div class="my-appointment-page">
    <!-- Tab栏 -->
    <van-tabs v-model:active="currentTabIndex" @change="onTabChange" color="#07C160">
      <van-tab v-for="tab in tabs" :key="tab.key" :title="tab.name" />
    </van-tabs>

    <!-- 预约列表 -->
    <div class="list-container">
      <div v-for="item in appointments" :key="item.id" class="appointment-card">
        <div class="card-header">
          <span class="store-name">{{ item.storeName }}</span>
          <van-tag
            :type="(getStatusType(item.status) as any)"
            size="medium"
            round
          >{{ getStatusDisplay(item.status) }}</van-tag>
        </div>

        <div class="card-body">
          <div class="info-row">
            <span class="info-label">服务项目</span>
            <span class="info-value">{{ item.serviceItemName || '-' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">技师</span>
            <span class="info-value">{{ item.techName || '待分配' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">日期</span>
            <span class="info-value">{{ item.date }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">时段</span>
            <span class="info-value">{{ item.timeSlot }}</span>
          </div>
        </div>

        <div class="card-footer" v-if="isPending(item.status)">
          <van-button
            size="small"
            plain
            type="success"
            icon="clock-o"
            @click="onModifyTap(item)"
          >修改时间</van-button>
          <van-button
            size="small"
            plain
            type="danger"
            icon="cross"
            @click="onCancelTap(item)"
          >取消预约</van-button>
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-wrap">
        <van-loading type="spinner" color="#07C160">加载中...</van-loading>
      </div>

      <!-- 空状态 -->
      <van-empty
        v-if="!loading && appointments.length === 0"
        image="search"
        description="暂无预约记录"
      />
    </div>

    <!-- 修改预约弹窗 -->
    <van-popup
      v-model:show="showModifyPopup"
      round
      position="bottom"
      :style="{ maxHeight: '80%' }"
      closeable
      close-icon="cross"
    >
      <div class="popup-content">
        <div class="popup-header">
          <span class="popup-title">修改预约时间</span>
        </div>
        <div class="popup-body" v-if="currentAppointment">
          <!-- 日期选择 -->
          <div class="popup-section-title">选择日期</div>
          <div class="date-scroll">
            <div class="date-scroll-inner">
              <div
                class="date-card"
                :class="{ selected: modifyDate === item.dateStr }"
                v-for="item in modifyDates"
                :key="item.dateStr"
                @click="onModifyDateTap(item.dateStr)"
              >
                <span class="date-weekday">{{ item.weekDay }}</span>
                <span class="date-display">{{ item.display }}</span>
              </div>
            </div>
          </div>

          <!-- 时段选择 -->
          <div class="popup-section-title">选择时段</div>
          <div v-if="modifySlotsLoading" class="slots-loading">
            <van-loading size="20px" color="#07C160">加载时段...</van-loading>
          </div>
          <div v-else class="slots-grid">
            <div
              class="slot-item"
              :class="{
                occupied: slot.occupied,
                selected: modifyTimeSlot === slot.timeSlot
              }"
              v-for="slot in modifyTimeSlots"
              :key="slot.timeSlot"
              @click="onModifySlotTap(slot)"
            >
              <template v-if="slot.occupied">
                <span class="slot-lock">🔒</span>
                <span class="slot-text occupied-text">已约</span>
              </template>
              <template v-else>
                <span class="slot-text">{{ slot.timeSlot }}</span>
              </template>
            </div>
            <van-empty
              v-if="modifyTimeSlots.length === 0"
              image="search"
              description="暂无可选时段"
            />
          </div>

          <!-- 确认按钮 -->
          <div class="popup-btn-wrap">
            <van-button
              type="success"
              block
              round
              :disabled="!modifyDate || !modifyTimeSlot"
              :loading="modifySubmitting"
              loading-text="提交中..."
              @click="confirmModify"
            >
              确认修改
            </van-button>
          </div>
        </div>
      </div>
    </van-popup>

    <!-- 取消预约弹窗 -->
    <van-popup
      v-model:show="showCancelPopup"
      round
      position="bottom"
      :style="{ maxHeight: '60%' }"
      closeable
      close-icon="cross"
    >
      <div class="popup-content">
        <div class="popup-header">
          <span class="popup-title">取消预约</span>
        </div>
        <div class="popup-body" v-if="currentAppointment">
          <div class="cancel-tip">
            <van-icon name="warning-o" color="#FA5151" size="18" />
            <span>确认取消该预约？取消后需重新预约。</span>
          </div>
          <van-field
            v-model="cancelReason"
            label="取消原因"
            type="textarea"
            placeholder="请输入取消原因（可选）"
            rows="3"
            maxlength="200"
            show-word-limit
          />
          <div class="popup-btn-wrap">
            <van-button
              type="danger"
              block
              round
              :loading="cancelSubmitting"
              loading-text="提交中..."
              @click="confirmCancel"
            >
              确认取消
            </van-button>
          </div>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { showToast, showConfirmDialog } from 'vant'
import {
  getMyAppointments,
  modifyAppointment,
  cancelAppointment,
  getAvailableSlots
} from '@/api/appointment'

const tabs = [
  { key: 'all', name: '全部' },
  { key: 'pending', name: '待服务' },
  { key: 'completed', name: '已完成' },
  { key: 'cancelled', name: '已取消' }
]

const currentTabIndex = ref(0)
const appointments = ref<any[]>([])
const loading = ref(false)

// 修改预约相关
const showModifyPopup = ref(false)
const currentAppointment = ref<any>(null)
const modifyDate = ref('')
const modifyTimeSlot = ref('')
const modifyDates = ref<any[]>([])
const modifyTimeSlots = ref<any[]>([])
const modifySlotsLoading = ref(false)
const modifySubmitting = ref(false)

// 取消预约相关
const showCancelPopup = ref(false)
const cancelReason = ref('')
const cancelSubmitting = ref(false)

onMounted(() => {
  loadAppointments()
})

function onTabChange() {
  appointments.value = []
  loadAppointments()
}

async function loadAppointments() {
  if (loading.value) return
  loading.value = true
  try {
    const statusGroup = tabs[currentTabIndex.value].key
    const res: any = await getMyAppointments({ statusGroup })
    const data = res.data || {}
    let list: any[] = []
    if (Array.isArray(data)) {
      list = data
    } else if (statusGroup === 'all') {
      list = [
        ...(data.pendingList || []),
        ...(data.completedList || []),
        ...(data.cancelledList || [])
      ]
    } else if (statusGroup === 'pending') {
      list = data.pendingList || data.list || []
    } else if (statusGroup === 'completed') {
      list = data.completedList || data.list || []
    } else if (statusGroup === 'cancelled') {
      list = data.cancelledList || data.list || []
    } else {
      list = data.list || []
    }
    appointments.value = list
  } catch {
    appointments.value = []
  } finally {
    loading.value = false
  }
}

function isPending(status: string) {
  return status === 'pending' || status === '待服务' || status === 'confirmed' || status === '已确认'
}

function getStatusType(status: string) {
  const map: Record<string, string> = {
    pending: 'primary',
    confirmed: 'success',
    completed: 'success',
    cancelled: 'danger',
    no_show: 'warning',
    '待服务': 'primary',
    '已确认': 'success',
    '已完成': 'success',
    '已取消': 'danger',
    '未到店': 'warning'
  }
  return map[status] || 'default'
}

function getStatusDisplay(status: string) {
  const map: Record<string, string> = {
    pending: '待服务',
    confirmed: '已确认',
    completed: '已完成',
    cancelled: '已取消',
    no_show: '未到店',
    '待服务': '待服务',
    '已确认': '已确认',
    '已完成': '已完成',
    '已取消': '已取消',
    '未到店': '未到店'
  }
  return map[status] || status
}

// ============ 修改预约 ============

function initModifyDates() {
  const result: any[] = []
  const weekDays = ['日', '一', '二', '三', '四', '五', '六']
  const now = new Date()
  for (let i = 0; i < 7; i++) {
    const d = new Date(now)
    d.setDate(now.getDate() + i)
    const month = (d.getMonth() + 1).toString().padStart(2, '0')
    const day = d.getDate().toString().padStart(2, '0')
    result.push({
      dateStr: `${d.getFullYear()}-${month}-${day}`,
      display: `${month}/${day}`,
      weekDay: i === 0 ? '今天' : `周${weekDays[d.getDay()]}`
    })
  }
  modifyDates.value = result
}

function onModifyTap(item: any) {
  currentAppointment.value = item
  modifyDate.value = item.date || ''
  modifyTimeSlot.value = item.timeSlot || ''
  modifyTimeSlots.value = []
  modifySubmitting.value = false
  showModifyPopup.value = true
  initModifyDates()
  if (modifyDate.value) {
    loadModifySlots(modifyDate.value)
  } else if (modifyDates.value.length > 0) {
    modifyDate.value = modifyDates.value[0].dateStr
    loadModifySlots(modifyDate.value)
  }
}

function onModifyDateTap(date: string) {
  if (modifyDate.value === date) return
  modifyDate.value = date
  modifyTimeSlot.value = ''
  modifyTimeSlots.value = []
  loadModifySlots(date)
}

async function loadModifySlots(date: string) {
  if (!currentAppointment.value) return
  modifySlotsLoading.value = true
  try {
    const res: any = await getAvailableSlots({
      storeId: currentAppointment.value.storeId,
      technicianId: currentAppointment.value.technicianId,
      date
    })
    modifyTimeSlots.value = res.data || []
  } catch {
    modifyTimeSlots.value = []
  } finally {
    modifySlotsLoading.value = false
  }
}

function onModifySlotTap(slot: any) {
  if (slot.occupied) return
  modifyTimeSlot.value = slot.timeSlot
}

async function confirmModify() {
  if (!currentAppointment.value || !modifyDate.value || !modifyTimeSlot.value) return
  modifySubmitting.value = true
  try {
    await modifyAppointment(currentAppointment.value.id, {
      storeId: currentAppointment.value.storeId,
      technicianId: currentAppointment.value.technicianId,
      date: modifyDate.value,
      timeSlot: modifyTimeSlot.value,
      serviceItemId: currentAppointment.value.serviceItemId
    })
    showToast('修改成功')
    showModifyPopup.value = false
    currentAppointment.value = null
    loadAppointments()
  } catch {
    // request.ts 已处理错误提示
  } finally {
    modifySubmitting.value = false
  }
}

// ============ 取消预约 ============

function onCancelTap(item: any) {
  currentAppointment.value = item
  cancelReason.value = ''
  cancelSubmitting.value = false
  showCancelPopup.value = true
}

async function confirmCancel() {
  if (!currentAppointment.value) return
  cancelSubmitting.value = true
  try {
    await cancelAppointment(currentAppointment.value.id, { reason: cancelReason.value })
    showToast('取消成功')
    showCancelPopup.value = false
    currentAppointment.value = null
    loadAppointments()
  } catch {
    // request.ts 已处理错误提示
  } finally {
    cancelSubmitting.value = false
  }
}
</script>

<style scoped lang="scss">
.my-appointment-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.list-container {
  padding-bottom: 20px;
}

.appointment-card {
  margin: 10px 12px;
  background: #fff;
  border-radius: 10px;
  overflow: hidden;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 14px 16px 8px;

    .store-name {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }
  }

  .card-body {
    padding: 4px 16px 8px;

    .info-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 6px 0;
      font-size: 14px;

      .info-label {
        color: #909399;
      }

      .info-value {
        color: #303133;
        font-weight: 500;
      }
    }
  }

  .card-footer {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    padding: 8px 16px 14px;
    border-top: 1px solid #f5f5f5;
  }
}

.loading-wrap {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}

// 弹窗通用样式
.popup-content {
  .popup-header {
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 16px;
    border-bottom: 1px solid #f0f0f0;

    .popup-title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }
  }

  .popup-body {
    padding: 16px;
  }
}

// 修改预约弹窗
.popup-section-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 10px;
}

.date-scroll {
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  margin-bottom: 16px;

  &::-webkit-scrollbar { display: none; }

  .date-scroll-inner {
    display: flex;
    padding: 0 2px;
    gap: 8px;
  }

  .date-card {
    flex-shrink: 0;
    width: 56px;
    padding: 8px 4px;
    text-align: center;
    border-radius: 8px;
    background: #f5f5f5;
    cursor: pointer;

    &.selected {
      background: #07C160;
      color: #fff;
    }

    .date-weekday {
      display: block;
      font-size: 12px;
      margin-bottom: 4px;
    }

    .date-display {
      display: block;
      font-size: 14px;
      font-weight: 500;
    }
  }
}

.slots-loading {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

.slots-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  margin-bottom: 16px;
}

.slot-item {
  padding: 12px 8px;
  text-align: center;
  border-radius: 8px;
  background: #fff;
  border: 1px solid #e4e7ed;
  cursor: pointer;

  &.selected {
    background: #07C160;
    border-color: #07C160;
    color: #fff;
  }

  &.occupied {
    background: #f5f5f5;
    border-color: #e4e7ed;
    cursor: not-allowed;
  }

  .slot-lock { font-size: 12px; }

  .slot-text {
    display: block;
    font-size: 14px;
    margin-top: 2px;

    &.occupied-text { color: #c0c4cc; font-size: 12px; }
  }
}

.popup-btn-wrap {
  padding-top: 12px;
}

// 取消预约弹窗
.cancel-tip {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  padding: 12px;
  background: #fff8f0;
  border-radius: 8px;
  margin-bottom: 12px;
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
}
</style>
