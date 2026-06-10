<template>
  <div class="step-confirm-page">
    <!-- 步骤条 -->
    <van-steps :active="3" active-color="#07C160">
      <van-step>选门店</van-step>
      <van-step>选技师</van-step>
      <van-step>选时间</van-step>
      <van-step>确认</van-step>
    </van-steps>

    <!-- 5分钟倒计时 -->
    <div class="countdown-bar" :class="{ warning: countdownWarning }">
      <van-icon name="clock-o" size="16" />
      <span>请在 {{ countdownDisplay }} 内完成预约</span>
    </div>

    <!-- 预约摘要卡片 -->
    <div class="summary-card">
      <div class="summary-title">预约摘要</div>

      <div class="summary-row">
        <span class="row-label">门店</span>
        <span class="row-value">{{ storeName || '--' }}</span>
      </div>
      <div class="summary-row">
        <span class="row-label">技师</span>
        <span class="row-value">{{ techName || '--' }}</span>
      </div>
      <div class="summary-row">
        <span class="row-label">日期</span>
        <span class="row-value">{{ formatDate(date) }}</span>
      </div>
      <div class="summary-row">
        <span class="row-label">时段</span>
        <span class="row-value highlight">{{ timeSlot || '--' }}</span>
      </div>
    </div>

    <!-- 服务项目选择 -->
    <div class="service-card" v-if="serviceItems.length > 0">
      <div class="service-header">
        <span class="service-title">服务项目</span>
        <span class="service-optional">可选</span>
      </div>
      <div class="service-list">
        <div
          class="service-item"
          :class="{ selected: selectedServiceItemId === item.id }"
          v-for="item in serviceItems"
          :key="item.id"
          @click="onServiceItemTap(item)"
        >
          <div class="service-left">
            <span class="service-name">{{ item.name }}</span>
            <span class="service-duration" v-if="item.duration">{{ item.duration }}分钟</span>
          </div>
          <div class="service-right">
            <span class="service-price" v-if="item.price">¥{{ item.price }}</span>
            <van-icon v-if="selectedServiceItemId === item.id" name="success" color="#07C160" size="18" />
          </div>
        </div>
      </div>
    </div>

    <!-- 确认预约按钮 -->
    <div class="bottom-bar">
      <van-button
        type="primary"
        block
        round
        :disabled="!canSubmit"
        :loading="submitting"
        loading-text="提交中..."
        @click="onSubmit"
      >
        确认预约
      </van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showToast, showDialog } from 'vant'
import { createAppointment, trackFunnel } from '@/api/appointment'
import { getServiceItemList } from '@/api/service'

const router = useRouter()
const route = useRoute()

const storeId = ref('')
const storeName = ref('')
const techId = ref(0)
const techName = ref('')
const date = ref('')
const timeSlot = ref('')
const lockId = ref('')
const expireAt = ref('')
const sessionId = ref('')

const countdown = ref(300)
const countdownDisplay = ref('05:00')
const countdownWarning = ref(false)

const serviceItems = ref<any[]>([])
const selectedServiceItemId = ref<number | string>('')
const submitting = ref(false)

let timer: ReturnType<typeof setInterval> | null = null

const canSubmit = computed(() => {
  return !!storeId.value && !!date.value && !!timeSlot.value && !!lockId.value && !submitting.value
})

onMounted(() => {
  storeId.value = (route.query.storeId as string) || ''
  storeName.value = decodeURIComponent((route.query.storeName as string) || '')
  techId.value = parseInt(route.query.techId as string) || 0
  techName.value = decodeURIComponent((route.query.techName as string) || '')
  date.value = (route.query.date as string) || ''
  timeSlot.value = decodeURIComponent((route.query.timeSlot as string) || '')
  lockId.value = (route.query.lockId as string) || ''
  expireAt.value = decodeURIComponent((route.query.expireAt as string) || '')
  sessionId.value = (route.query.sessionId as string) || ''

  loadServiceItems()
  startCountdown()
  track('browse_confirm')
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})

async function loadServiceItems() {
  try {
    const res: any = await getServiceItemList({ status: 1 })
    serviceItems.value = res.data || []
  } catch {
    serviceItems.value = []
  }
}

function formatDate(dateStr: string): string {
  if (!dateStr) return '--'
  const d = new Date(dateStr)
  const weekDays = ['日', '一', '二', '三', '四', '五', '六']
  const month = (d.getMonth() + 1).toString().padStart(2, '0')
  const day = d.getDate().toString().padStart(2, '0')
  return `${month}月${day}日 周${weekDays[d.getDay()]}`
}

function startCountdown() {
  if (expireAt.value) {
    const expireTime = new Date(expireAt.value).getTime()
    const remain = Math.max(0, Math.floor((expireTime - Date.now()) / 1000))
    countdown.value = remain
    updateCountdownDisplay(remain)
  }

  timer = setInterval(() => {
    let val = countdown.value - 1
    if (val <= 0) {
      val = 0
      if (timer) clearInterval(timer)
      timer = null
      showDialog({
        title: '提示',
        message: '锁定已过期，请重新选择时段'
      }).then(() => {
        router.replace('/appointment/step3')
      })
    }
    countdown.value = val
    updateCountdownDisplay(val)
  }, 1000)
}

function updateCountdownDisplay(val: number) {
  const minutes = Math.floor(val / 60).toString().padStart(2, '0')
  const seconds = (val % 60).toString().padStart(2, '0')
  countdownDisplay.value = `${minutes}:${seconds}`
  countdownWarning.value = val < 60
}

function onServiceItemTap(item: any) {
  selectedServiceItemId.value = selectedServiceItemId.value === item.id ? '' : item.id
}

async function onSubmit() {
  if (submitting.value) return
  if (!storeId.value) { showToast('请选择门店'); return }
  if (!date.value) { showToast('请选择日期'); return }
  if (!timeSlot.value) { showToast('请选择时段'); return }
  if (!lockId.value) { showToast('时段锁定已失效，请重新选择'); return }

  submitting.value = true
  try {
    await createAppointment({
      storeId: storeId.value,
      technicianId: techId.value,
      date: date.value,
      timeSlot: timeSlot.value,
      lockId: lockId.value,
      serviceItemId: selectedServiceItemId.value || undefined
    })
    showToast('预约成功')
    track('confirm', { storeId: storeId.value, techId: techId.value, date: date.value, timeSlot: timeSlot.value })
    setTimeout(() => {
      router.replace('/my-appointment/list')
    }, 1500)
  } catch {
    submitting.value = false
  }
}

function track(event: string, extra?: Record<string, any>) {
  trackFunnel({ sessionId: sessionId.value, step: 'step4', event, extra: extra || {} }).catch(() => {})
}
</script>

<style scoped lang="scss">
.step-confirm-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 80px;
}

.countdown-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px;
  background: #e6f9ee;
  color: #07C160;
  font-size: 14px;
  font-weight: 500;

  &.warning {
    background: #fef0f0;
    color: #fa5151;
  }
}

.summary-card {
  margin: 12px;
  background: #fff;
  border-radius: 10px;
  padding: 16px;

  .summary-title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 12px;
  }

  .summary-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 0;
    border-bottom: 1px solid #f5f5f5;

    &:last-child {
      border-bottom: none;
    }

    .row-label {
      font-size: 14px;
      color: #909399;
    }

    .row-value {
      font-size: 14px;
      color: #303133;
      font-weight: 500;

      &.highlight {
        color: #07C160;
      }
    }
  }
}

.service-card {
  margin: 0 12px 12px;
  background: #fff;
  border-radius: 10px;
  overflow: hidden;

  .service-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 14px 16px 8px;

    .service-title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }

    .service-optional {
      font-size: 12px;
      color: #909399;
    }
  }

  .service-list {
    padding: 0 16px 12px;
  }

  .service-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 0;
    border-bottom: 1px solid #f5f5f5;

    &:last-child {
      border-bottom: none;
    }

    &.selected {
      background: #f0faf4;
      margin: 0 -16px;
      padding: 10px 16px;
      border-radius: 6px;
    }

    .service-left {
      display: flex;
      flex-direction: column;

      .service-name {
        font-size: 14px;
        color: #303133;
      }

      .service-duration {
        font-size: 12px;
        color: #909399;
        margin-top: 2px;
      }
    }

    .service-right {
      display: flex;
      align-items: center;
      gap: 8px;

      .service-price {
        font-size: 14px;
        color: #fa5151;
        font-weight: 500;
      }
    }
  }
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 12px 16px;
  background: #fff;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.06);
}
</style>
