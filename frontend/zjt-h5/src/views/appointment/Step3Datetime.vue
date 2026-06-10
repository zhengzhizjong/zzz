<template>
  <div class="step-datetime-page">
    <!-- 步骤条 -->
    <van-steps :active="2" active-color="#07C160">
      <van-step>选门店</van-step>
      <van-step>选技师</van-step>
      <van-step>选时间</van-step>
      <van-step>确认</van-step>
    </van-steps>

    <!-- 已选门店和技师 -->
    <div class="selected-info-bar">
      <div class="info-item">
        <span class="label">门店：</span>
        <span class="value">{{ storeName }}</span>
      </div>
      <div class="info-item">
        <span class="label">技师：</span>
        <span class="value">{{ techName }}</span>
      </div>
    </div>

    <!-- 日期横向滚动选择 -->
    <div class="date-scroll">
      <div class="date-scroll-inner">
        <div
          class="date-card"
          :class="{ selected: selectedDate === item.dateStr }"
          v-for="item in dates"
          :key="item.dateStr"
          @click="onDateTap(item.dateStr)"
        >
          <span class="date-weekday">{{ item.weekDay }}</span>
          <span class="date-display">{{ item.display }}</span>
        </div>
      </div>
    </div>

    <!-- 时段网格 -->
    <div class="slots-section" v-if="!slotsLoading">
      <div class="slots-grid">
        <div
          class="slot-item"
          :class="{
            occupied: item.occupied,
            selected: selectedTimeSlot === item.timeSlot,
            locking: lockingSlot === item.timeSlot
          }"
          v-for="item in timeSlots"
          :key="item.timeSlot"
          @click="onSlotTap(item)"
        >
          <template v-if="item.occupied">
            <van-icon name="lock" size="14" color="#c0c4cc" />
            <span class="slot-text occupied-text">{{ item.timeSlot }}</span>
          </template>
          <template v-else-if="lockingSlot === item.timeSlot">
            <van-loading size="16" color="#07C160" />
            <span class="slot-text">锁定中</span>
          </template>
          <template v-else>
            <span class="slot-text">{{ item.timeSlot }}</span>
          </template>
        </div>
      </div>

      <van-empty v-if="timeSlots.length === 0" description="暂无可选时段" />
    </div>

    <!-- 加载中 -->
    <div class="loading-wrap" v-if="slotsLoading">
      <van-loading size="24px">加载时段...</van-loading>
    </div>

    <!-- 提示文字 -->
    <div class="slot-tip">
      <span>点击可用时段将自动锁定，请在5分钟内完成预约</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showToast, showLoadingToast, closeToast } from 'vant'
import { getAvailableSlots, lockTemp, trackFunnel } from '@/api/appointment'

const router = useRouter()
const route = useRoute()

const storeId = ref('')
const storeName = ref('')
const techId = ref(0)
const techName = ref('')
const sessionId = ref('')

const dates = ref<any[]>([])
const selectedDate = ref('')
const timeSlots = ref<any[]>([])
const selectedTimeSlot = ref('')
const slotsLoading = ref(true)
const lockingSlot = ref('')

onMounted(() => {
  storeId.value = (route.query.storeId as string) || ''
  storeName.value = decodeURIComponent((route.query.storeName as string) || '')
  techId.value = parseInt(route.query.techId as string) || 0
  techName.value = decodeURIComponent((route.query.techName as string) || '')
  sessionId.value = (route.query.sessionId as string) || ''
  initDates()
  track('browse_time')
})

function initDates() {
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
  dates.value = result
  selectedDate.value = result.length > 0 ? result[0].dateStr : ''
  if (selectedDate.value) {
    loadTimeSlots(selectedDate.value)
  }
}

function onDateTap(date: string) {
  if (selectedDate.value === date) return
  selectedDate.value = date
  selectedTimeSlot.value = ''
  lockingSlot.value = ''
  timeSlots.value = []
  loadTimeSlots(date)
}

async function loadTimeSlots(date: string) {
  slotsLoading.value = true
  try {
    const res: any = await getAvailableSlots({
      storeId: storeId.value,
      technicianId: techId.value,
      date
    })
    timeSlots.value = res.data || []
  } catch {
    timeSlots.value = []
  } finally {
    slotsLoading.value = false
  }
}

async function onSlotTap(item: any) {
  if (item.occupied) return
  if (lockingSlot.value) return // 正在锁定其他时段

  selectedTimeSlot.value = item.timeSlot
  lockingSlot.value = item.timeSlot
  track('select_time', { date: selectedDate.value, timeSlot: item.timeSlot })

  try {
    const res: any = await lockTemp({
      storeId: storeId.value,
      technicianId: techId.value,
      date: selectedDate.value,
      timeSlot: item.timeSlot
    })
    const lockId = res.data?.lockId || ''
    const expireAt = res.data?.expireAt || ''
    lockingSlot.value = ''

    router.push({
      path: '/appointment/step4',
      query: {
        storeId: storeId.value,
        storeName: encodeURIComponent(storeName.value),
        techId: String(techId.value),
        techName: encodeURIComponent(techName.value),
        date: selectedDate.value,
        timeSlot: encodeURIComponent(item.timeSlot),
        lockId,
        expireAt: encodeURIComponent(expireAt),
        sessionId: sessionId.value
      }
    })
  } catch {
    lockingSlot.value = ''
    selectedTimeSlot.value = ''
    showToast('时段锁定失败，请选择其他时段')
  }
}

function track(event: string, extra?: Record<string, any>) {
  trackFunnel({ sessionId: sessionId.value, step: 'step3', event, extra: extra || {} }).catch(() => {})
}
</script>

<style scoped lang="scss">
.step-datetime-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.selected-info-bar {
  display: flex;
  padding: 10px 16px;
  background: #fff;
  gap: 20px;
  border-bottom: 1px solid #f0f0f0;

  .info-item {
    font-size: 14px;
    .label { color: #909399; }
    .value { color: #303133; font-weight: 500; }
  }
}

.date-scroll {
  background: #fff;
  padding: 12px 0;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;

  &::-webkit-scrollbar { display: none; }

  .date-scroll-inner {
    display: flex;
    padding: 0 12px;
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
    transition: all 0.2s;

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

.slots-section {
  padding: 12px;
}

.slots-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.slot-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 12px 8px;
  text-align: center;
  border-radius: 8px;
  background: #fff;
  border: 1px solid #e4e7ed;
  cursor: pointer;
  transition: all 0.2s;
  min-height: 52px;

  &.selected {
    background: #07C160;
    border-color: #07C160;
    color: #fff;
  }

  &.locking {
    border-color: #07C160;
    background: #f0faf4;
  }

  &.occupied {
    background: #f5f5f5;
    border-color: #e4e7ed;
    cursor: not-allowed;
  }

  .slot-text {
    display: block;
    font-size: 14px;
    margin-top: 2px;

    &.occupied-text {
      color: #c0c4cc;
      font-size: 12px;
    }
  }
}

.loading-wrap {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}

.slot-tip {
  text-align: center;
  padding: 16px;
  font-size: 12px;
  color: #909399;
}
</style>
