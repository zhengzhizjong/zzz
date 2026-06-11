<template>
  <div class="schedule-page">
    <van-nav-bar title="排班查看" left-arrow @click-left="router.back()" />

    <!-- 月份选择 -->
    <div class="month-selector">
      <van-icon name="arrow-left" @click="prevMonth" />
      <span class="month-text">{{ currentMonth }}</span>
      <van-icon name="arrow" @click="nextMonth" />
    </div>

    <!-- 日历视图 -->
    <div class="calendar">
      <div class="week-header">
        <span v-for="day in weekDays" :key="day" class="week-day">{{ day }}</span>
      </div>
      <div class="calendar-grid">
        <div
          v-for="(day, index) in calendarDays"
          :key="index"
          class="calendar-cell"
          :class="{ empty: !day.day, today: day.isToday }"
        >
          <div v-if="day.day" class="day-num">{{ day.day }}</div>
          <div v-if="day.shift" class="shift-tag" :class="day.shiftClass">{{ day.shift }}</div>
        </div>
      </div>
    </div>

    <!-- 图例 -->
    <div class="legend">
      <div class="legend-item"><span class="legend-dot morning"></span>早班</div>
      <div class="legend-item"><span class="legend-dot afternoon"></span>中班</div>
      <div class="legend-item"><span class="legend-dot evening"></span>晚班</div>
      <div class="legend-item"><span class="legend-dot rest"></span>休息</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getSchedule } from '../../api/schedule'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()

const weekDays = ['日', '一', '二', '三', '四', '五', '六']
const currentYear = ref(new Date().getFullYear())
const currentMonthNum = ref(new Date().getMonth() + 1)
const scheduleData = ref<Record<number, string>>({})

const currentMonth = computed(() => `${currentYear.value}年${currentMonthNum.value}月`)

interface CalendarDay {
  day: number
  shift: string
  shiftClass: string
  isToday: boolean
}

const calendarDays = computed(() => {
  const firstDay = new Date(currentYear.value, currentMonthNum.value - 1, 1).getDay()
  const daysInMonth = new Date(currentYear.value, currentMonthNum.value, 0).getDate()
  const today = new Date()
  const days: CalendarDay[] = []

  for (let i = 0; i < firstDay; i++) {
    days.push({ day: 0, shift: '', shiftClass: '', isToday: false })
  }

  for (let d = 1; d <= daysInMonth; d++) {
    const shift = scheduleData.value[d] || ''
    let shiftClass = ''
    if (shift === '早班') shiftClass = 'morning'
    else if (shift === '中班') shiftClass = 'afternoon'
    else if (shift === '晚班') shiftClass = 'evening'
    else if (shift === '休息') shiftClass = 'rest'

    const isToday = currentYear.value === today.getFullYear() && currentMonthNum.value === today.getMonth() + 1 && d === today.getDate()

    days.push({ day: d, shift, shiftClass, isToday })
  }

  return days
})

function prevMonth() {
  if (currentMonthNum.value === 1) {
    currentYear.value--
    currentMonthNum.value = 12
  } else {
    currentMonthNum.value--
  }
  loadSchedule()
}

function nextMonth() {
  if (currentMonthNum.value === 12) {
    currentYear.value++
    currentMonthNum.value = 1
  } else {
    currentMonthNum.value++
  }
  loadSchedule()
}

async function loadSchedule() {
  try {
    const techId = userStore.userInfo?.id || 1
    const month = `${currentYear.value}-${String(currentMonthNum.value).padStart(2, '0')}`
    const res: any = await getSchedule(techId, month)
    const data = res.data || []
    const map: Record<number, string> = {}
    data.forEach((item: any) => {
      const day = new Date(item.date).getDate()
      map[day] = item.shift
    })
    scheduleData.value = map
  } catch {
    // 模拟数据
    const map: Record<number, string> = {}
    const daysInMonth = new Date(currentYear.value, currentMonthNum.value, 0).getDate()
    const shifts = ['早班', '中班', '晚班', '休息']
    for (let d = 1; d <= daysInMonth; d++) {
      const dow = new Date(currentYear.value, currentMonthNum.value - 1, d).getDay()
      if (dow === 0 || dow === 6) {
        map[d] = '休息'
      } else {
        map[d] = shifts[d % 3]
      }
    }
    scheduleData.value = map
  }
}

onMounted(loadSchedule)
</script>

<style scoped>
.schedule-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.month-selector {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  background: #fff;
  gap: 20px;
}

.month-text {
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.calendar {
  background: #fff;
  margin: 12px;
  border-radius: 12px;
  padding: 12px;
}

.week-header {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  text-align: center;
  margin-bottom: 8px;
}

.week-day {
  font-size: 13px;
  color: #999;
  padding: 4px 0;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
}

.calendar-cell {
  text-align: center;
  padding: 6px 2px;
  min-height: 50px;
  border-radius: 6px;
}

.calendar-cell.today {
  background: #e8f7ef;
}

.day-num {
  font-size: 14px;
  color: #333;
}

.shift-tag {
  font-size: 10px;
  margin-top: 2px;
  padding: 1px 4px;
  border-radius: 3px;
  color: #fff;
}

.shift-tag.morning { background: #07C160; }
.shift-tag.afternoon { background: #1989fa; }
.shift-tag.evening { background: #ff976a; }
.shift-tag.rest { background: #c8c9cc; }

.legend {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding: 12px;
  background: #fff;
  margin: 0 12px;
  border-radius: 12px;
}

.legend-item {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #666;
}

.legend-dot {
  width: 8px;
  height: 8px;
  border-radius: 2px;
  margin-right: 4px;
}

.legend-dot.morning { background: #07C160; }
.legend-dot.afternoon { background: #1989fa; }
.legend-dot.evening { background: #ff976a; }
.legend-dot.rest { background: #c8c9cc; }
</style>
