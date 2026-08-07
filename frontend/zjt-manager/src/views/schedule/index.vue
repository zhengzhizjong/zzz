<template>
  <div class="schedule-page">
    <!-- 顶部筛选 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filters">
        <el-form-item label="门店">
          <el-select v-model="filters.storeId" placeholder="选择门店" style="width: 180px;" disabled>
            <el-option :label="storeName" :value="filters.storeId" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button :icon="ArrowLeft" circle @click="goPrevWeek" />
          <span style="min-width: 200px; text-align: center; font-weight: 600;">{{ weekLabel }}</span>
          <el-button :icon="ArrowRight" circle @click="goNextWeek" />
        </el-form-item>
        <el-form-item>
          <el-button type="success" @click="batchDialogVisible = true">批量排班</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 排班表格 -->
    <el-card shadow="never" style="margin-top: 16px;">
      <div class="schedule-table-wrapper">
        <table class="schedule-table">
          <thead>
            <tr>
              <th class="tech-col">技师</th>
              <th v-for="day in weekDays" :key="day.date" class="day-col" :class="{ 'is-today': day.isToday }">
                <div class="day-header">
                  <div class="day-weekday">{{ day.weekday }}</div>
                  <div class="day-date">{{ day.day }}</div>
                </div>
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="tech in technicianOptions" :key="tech.id">
              <td class="tech-col">
                <div class="tech-name">{{ tech.name }}</div>
              </td>
              <td
                v-for="day in weekDays"
                :key="day.date"
                class="day-col"
                :class="{ 'is-today': day.isToday }"
                @click="handleCellClick(tech, day)"
              >
                <div class="cell-content">
                  <template v-if="getSchedule(tech.id, day.date)">
                    <el-tag
                      :type="getScheduleTagType(tech.id, day.date)"
                      size="small"
                      effect="dark"
                      class="shift-tag"
                    >
                      {{ getScheduleLabel(tech.id, day.date) }}
                    </el-tag>
                  </template>
                  <template v-else>
                    <span class="empty-cell">+</span>
                  </template>
                </div>
              </td>
            </tr>
            <tr v-if="technicianOptions.length === 0">
              <td :colspan="weekDays.length + 1" class="empty-row">
                <el-empty description="暂无技师数据" :image-size="60" />
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </el-card>

    <!-- 单元格编辑弹窗 -->
    <el-dialog v-model="cellDialogVisible" title="修改班次" width="360px" destroy-on-close>
      <div class="shift-options">
        <div
          v-for="opt in shiftOptions"
          :key="opt.scheduleType + '-' + opt.shiftType"
          class="shift-option"
          :class="{ 'is-active': isOptionSelected(opt), [opt.className]: true }"
          @click="handleSelectShift(opt)"
        >
          <div class="shift-icon">{{ opt.icon }}</div>
          <div class="shift-info">
            <div class="shift-name">{{ opt.label }}</div>
            <div class="shift-time">{{ opt.time }}</div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="cellDialogVisible = false">取消</el-button>
        <el-button type="danger" plain v-if="currentCell.scheduleId" @click="handleDeleteCell">删除排班</el-button>
      </template>
    </el-dialog>

    <!-- 批量排班弹窗 -->
    <el-dialog v-model="batchDialogVisible" title="批量排班" width="500px" destroy-on-close>
      <el-form :model="batchForm" label-width="80px">
        <el-form-item label="选择技师">
          <el-select v-model="batchForm.technicianIds" multiple placeholder="请选择技师" style="width: 100%;">
            <el-option
              v-for="tech in technicianOptions"
              :key="tech.id"
              :label="tech.name"
              :value="tech.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排班类型">
          <el-select v-model="batchForm.scheduleType" placeholder="请选择" style="width: 100%;">
            <el-option label="上班" :value="1" />
            <el-option label="休息" :value="2" />
            <el-option label="请假" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="班次" v-if="batchForm.scheduleType === 1">
          <el-select v-model="batchForm.shiftType" placeholder="请选择班次" style="width: 100%;">
            <el-option label="早班 (09:00-17:00)" value="morning" />
            <el-option label="中班 (13:00-21:00)" value="afternoon" />
            <el-option label="晚班 (17:00-01:00)" value="evening" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="batchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 100%;"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleBatchSave" :loading="batchLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getScheduleByTechnician, getScheduleByStore, createSchedule, batchCreateSchedule, updateSchedule, deleteSchedule } from '@/api/schedule'
import { getTechnicianList } from '@/api/technician'
import { getStoreDetail } from '@/api/store'

const userStore = useUserStore()
const loading = ref(false)
const batchLoading = ref(false)
const cellDialogVisible = ref(false)
const batchDialogVisible = ref(false)

const storeName = ref('当前门店')
const scheduleMap = ref<Record<number, Record<string, any>>>({})

const filters = reactive({
  storeId: Number(userStore.storeId) || 0,
  weekStart: getMonday(new Date())
})

const technicianOptions = ref<Array<Record<string, any>>>([])

const shiftOptions = [
  { label: '早班', scheduleType: 1, shiftType: 'morning', icon: '🌅', time: '09:00 - 17:00', className: 'opt-morning' },
  { label: '中班', scheduleType: 1, shiftType: 'afternoon', icon: '☀️', time: '13:00 - 21:00', className: 'opt-afternoon' },
  { label: '晚班', scheduleType: 1, shiftType: 'evening', icon: '🌙', time: '17:00 - 01:00', className: 'opt-evening' },
  { label: '休息', scheduleType: 2, shiftType: '', icon: '💤', time: '全天休息', className: 'opt-rest' },
  { label: '请假', scheduleType: 3, shiftType: '', icon: '📝', time: '已请假', className: 'opt-leave' },
]

const currentCell = reactive({
  scheduleId: null as number | null,
  technicianId: null as number | null,
  technicianName: '',
  date: '',
  scheduleType: 0,
  shiftType: '',
})

const batchForm = reactive({
  dateRange: null as string[] | null,
  technicianIds: [] as number[],
  scheduleType: 1,
  shiftType: 'morning',
})

const weekdayLabels = ['日', '一', '二', '三', '四', '五', '六']

function getMonday(d: Date): string {
  const date = new Date(d)
  const day = date.getDay()
  const diff = date.getDate() - day + (day === 0 ? -6 : 1)
  date.setDate(diff)
  return formatDate(date)
}

function formatDate(d: Date): string {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${dd}`
}

const weekDays = computed(() => {
  const days: Array<{ date: string; day: number; weekday: string; isToday: boolean }> = []
  const todayStr = formatDate(new Date())
  for (let i = 0; i < 7; i++) {
    const d = new Date(filters.weekStart)
    d.setDate(d.getDate() + i)
    days.push({
      date: formatDate(d),
      day: d.getDate(),
      weekday: '周' + weekdayLabels[d.getDay()],
      isToday: formatDate(d) === todayStr,
    })
  }
  return days
})

const weekLabel = computed(() => {
  if (weekDays.value.length === 0) return ''
  return `${weekDays.value[0].date} ~ ${weekDays.value[6].date}`
})

function getSchedule(techId: number, date: string) {
  return scheduleMap.value[techId]?.[date] || null
}

function getScheduleLabel(techId: number, date: string): string {
  const s = getSchedule(techId, date)
  if (!s) return ''
  if (s.scheduleType === 2) return '休息'
  if (s.scheduleType === 3) return '请假'
  if (s.shiftType === 'morning') return '早班'
  if (s.shiftType === 'afternoon') return '中班'
  if (s.shiftType === 'evening') return '晚班'
  return '上班'
}

function getScheduleTagType(techId: number, date: string): string {
  const s = getSchedule(techId, date)
  if (!s) return 'info'
  if (s.scheduleType === 2) return 'info'
  if (s.scheduleType === 3) return 'warning'
  if (s.shiftType === 'morning') return 'success'
  if (s.shiftType === 'afternoon') return ''
  if (s.shiftType === 'evening') return 'danger'
  return 'success'
}

function isOptionSelected(opt: typeof shiftOptions[0]): boolean {
  if (currentCell.scheduleType !== opt.scheduleType) return false
  if (opt.scheduleType === 1 && currentCell.shiftType !== opt.shiftType) return false
  return true
}

function goPrevWeek() {
  const d = new Date(filters.weekStart)
  d.setDate(d.getDate() - 7)
  filters.weekStart = formatDate(d)
  loadScheduleData()
}

function goNextWeek() {
  const d = new Date(filters.weekStart)
  d.setDate(d.getDate() + 7)
  filters.weekStart = formatDate(d)
  loadScheduleData()
}

async function loadStoreName() {
  if (!filters.storeId) return
  try {
    const res: any = await getStoreDetail(filters.storeId)
    storeName.value = res.data?.storeName || res.data?.name || '当前门店'
  } catch {
    storeName.value = '当前门店'
  }
}

async function loadTechnicians() {
  try {
    const res: any = await getTechnicianList({ storeId: filters.storeId, pageSize: 100 })
    technicianOptions.value = res.data?.list || res.data?.records || res.data || []
  } catch {
    technicianOptions.value = []
  }
}

async function loadScheduleData() {
  if (!filters.storeId) {
    scheduleMap.value = {}
    return
  }
  const date = filters.weekStart
  try {
    const res: any = await getScheduleByStore(filters.storeId, date)
    const list: any[] = res.data || []
    const map: Record<number, Record<string, any>> = {}
    list.forEach((item: any) => {
      const techId = item.technicianId
      const dateStr = item.scheduleDate
      if (!techId || !dateStr) return
      if (!map[techId]) map[techId] = {}
      map[techId][dateStr] = {
        id: item.id,
        scheduleType: item.scheduleType,
        shiftType: item.shiftType || '',
      }
    })
    scheduleMap.value = map
  } catch {
    scheduleMap.value = {}
  }
}

function handleCellClick(tech: Record<string, any>, day: typeof weekDays.value[0]) {
  currentCell.technicianId = tech.id
  currentCell.technicianName = tech.name
  currentCell.date = day.date
  const s = getSchedule(tech.id, day.date)
  if (s) {
    currentCell.scheduleId = s.id
    currentCell.scheduleType = s.scheduleType
    currentCell.shiftType = s.shiftType
  } else {
    currentCell.scheduleId = null
    currentCell.scheduleType = 0
    currentCell.shiftType = ''
  }
  cellDialogVisible.value = true
}

async function handleSelectShift(opt: typeof shiftOptions[0]) {
  try {
    const shiftTimeMap: Record<string, { start: string; end: string }> = {
      morning: { start: '09:00', end: '17:00' },
      afternoon: { start: '13:00', end: '21:00' },
      evening: { start: '17:00', end: '01:00' },
    }
    const time = opt.scheduleType === 1 ? shiftTimeMap[opt.shiftType] : { start: '00:00', end: '00:00' }

    if (currentCell.scheduleId) {
      await updateSchedule(currentCell.scheduleId, {
        scheduleType: opt.scheduleType,
        shiftType: opt.scheduleType === 1 ? opt.shiftType : '',
        startTime: time.start,
        endTime: time.end,
      })
    } else {
      await createSchedule({
        storeId: filters.storeId,
        technicianId: currentCell.technicianId,
        scheduleDate: currentCell.date,
        startTime: time.start,
        endTime: time.end,
        scheduleType: opt.scheduleType,
        shiftType: opt.scheduleType === 1 ? opt.shiftType : '',
      })
    }
    ElMessage.success('排班更新成功')
    cellDialogVisible.value = false
    loadScheduleData()
  } catch {
    ElMessage.error('排班更新失败')
  }
}

async function handleDeleteCell() {
  if (!currentCell.scheduleId) return
  try {
    await deleteSchedule(currentCell.scheduleId)
    ElMessage.success('排班已删除')
    cellDialogVisible.value = false
    loadScheduleData()
  } catch {
    ElMessage.error('删除失败')
  }
}

async function handleBatchSave() {
  if (!batchForm.dateRange || batchForm.technicianIds.length === 0 || !batchForm.scheduleType) {
    ElMessage.warning('请完整填写批量排班信息')
    return
  }
  batchLoading.value = true
  try {
    const shiftTimeMap: Record<string, { start: string; end: string }> = {
      morning: { start: '09:00', end: '17:00' },
      afternoon: { start: '13:00', end: '21:00' },
      evening: { start: '17:00', end: '01:00' },
    }
    const time = batchForm.scheduleType === 1 ? shiftTimeMap[batchForm.shiftType] : { start: '09:00', end: '18:00' }
    await batchCreateSchedule({
      storeId: filters.storeId,
      technicianIds: batchForm.technicianIds,
      scheduleType: batchForm.scheduleType,
      shiftType: batchForm.scheduleType === 1 ? batchForm.shiftType : '',
      startDate: batchForm.dateRange[0],
      endDate: batchForm.dateRange[1],
      startTime: time.start,
      endTime: time.end,
      restDays: [0, 6],
    })
    ElMessage.success('批量排班成功')
    batchDialogVisible.value = false
    batchForm.dateRange = null
    batchForm.technicianIds = []
    batchForm.scheduleType = 1
    batchForm.shiftType = 'morning'
    loadScheduleData()
  } catch {
    ElMessage.error('批量排班失败')
  } finally {
    batchLoading.value = false
  }
}

onMounted(async () => {
  await loadStoreName()
  await loadTechnicians()
  loadScheduleData()
})
</script>

<style scoped>
.schedule-page {
  padding: 0;
}
.filter-card :deep(.el-card__body) {
  padding-bottom: 2px;
}
.schedule-table-wrapper {
  overflow-x: auto;
}
.schedule-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}
.schedule-table th,
.schedule-table td {
  border: 1px solid #ebeef5;
  text-align: center;
  vertical-align: middle;
}
.schedule-table th {
  background: #f5f7fa;
  font-weight: 600;
  padding: 8px 4px;
}
.schedule-table td {
  padding: 6px 4px;
  cursor: pointer;
  transition: background 0.2s;
}
.schedule-table td:hover {
  background: #f0f9eb;
}
.tech-col {
  width: 100px;
  min-width: 100px;
  text-align: left !important;
  padding-left: 12px !important;
}
.day-col {
  width: 120px;
  min-width: 120px;
}
.day-col.is-today {
  background: #f0f9eb;
}
.day-header .day-weekday {
  font-size: 12px;
  color: #909399;
}
.day-header .day-date {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
.is-today .day-date {
  color: #409eff;
}
.tech-name {
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.cell-content {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 32px;
}
.shift-tag {
  width: 100%;
  text-align: center;
}
.empty-cell {
  color: #c0c4cc;
  font-size: 18px;
  font-weight: 300;
}
.empty-row {
  text-align: center;
  padding: 40px !important;
}

/* 班次选择弹窗 */
.shift-options {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.shift-option {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border: 2px solid #ebeef5;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}
.shift-option:hover {
  border-color: #409eff;
  background: #f0f9eb;
}
.shift-option.is-active {
  border-color: #409eff;
  background: #ecf5ff;
}
.shift-option.opt-morning.is-active { border-color: #67c23a; background: #f0f9eb; }
.shift-option.opt-afternoon.is-active { border-color: #409eff; background: #ecf5ff; }
.shift-option.opt-evening.is-active { border-color: #e6a23c; background: #fdf6ec; }
.shift-option.opt-rest.is-active { border-color: #909399; background: #f4f4f5; }
.shift-option.opt-leave.is-active { border-color: #f56c6c; background: #fef0f0; }
.shift-icon { font-size: 24px; width: 36px; text-align: center; }
.shift-info { flex: 1; }
.shift-name { font-size: 15px; font-weight: 600; color: #303133; }
.shift-time { font-size: 12px; color: #909399; margin-top: 2px; }
</style>
