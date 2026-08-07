<template>
  <div class="schedule-list">
    <!-- 筛选栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="门店">
          <el-select v-model="searchForm.storeId" placeholder="请选择门店" @change="handleStoreChange">
            <el-option
              v-for="store in storeOptions"
              :key="store.id"
              :label="store.storeName"
              :value="store.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button :icon="ArrowLeft" circle @click="handlePrevWeek" />
          <span class="week-label">{{ weekLabel }}</span>
          <el-button :icon="ArrowRight" circle @click="handleNextWeek" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleBatchCreate">批量排班</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 排班表格 -->
    <el-card shadow="never" class="table-card">
      <div class="schedule-table-wrapper">
        <table class="schedule-table">
          <thead>
            <tr>
              <th class="tech-col">技师</th>
              <th v-for="day in weekDays" :key="day.dateStr" class="day-col" :class="{ 'is-today': day.isToday }">
                <div class="day-header">
                  <div class="day-weekday">{{ day.weekday }}</div>
                  <div class="day-date">{{ day.day }}</div>
                </div>
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="tech in technicians" :key="tech.id">
              <td class="tech-col">
                <div class="tech-name">{{ tech.name }}</div>
              </td>
              <td
                v-for="day in weekDays"
                :key="day.dateStr"
                class="day-col"
                :class="{ 'is-today': day.isToday }"
                @click="handleCellClick(tech, day)"
              >
                <div class="cell-content">
                  <template v-if="getSchedule(tech.id, day.dateStr)">
                    <el-tag
                      :type="getScheduleTagType(tech.id, day.dateStr)"
                      size="small"
                      effect="dark"
                      class="shift-tag"
                    >
                      {{ getScheduleLabel(tech.id, day.dateStr) }}
                    </el-tag>
                  </template>
                  <template v-else>
                    <span class="empty-cell">+</span>
                  </template>
                </div>
              </td>
            </tr>
            <tr v-if="technicians.length === 0">
              <td :colspan="weekDays.length + 1" class="empty-row">
                <el-empty description="请先选择门店" :image-size="60" />
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </el-card>

    <!-- 班次选择弹窗 -->
    <el-dialog
      v-model="shiftDialogVisible"
      :title="`${currentCell.technicianName} - ${currentCell.dateLabel}`"
      width="360px"
      destroy-on-close
    >
      <div class="shift-options">
        <div
          v-for="opt in shiftOptions"
          :key="opt.scheduleType + '-' + opt.shiftType"
          class="shift-option"
          :class="{ 'is-active': isSelected(opt), [opt.className]: true }"
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
        <el-button @click="shiftDialogVisible = false">取消</el-button>
        <el-button type="danger" plain v-if="currentCell.scheduleId" @click="handleDeleteSchedule">删除排班</el-button>
      </template>
    </el-dialog>

    <!-- 批量排班弹窗 -->
    <el-dialog
      v-model="batchDialogVisible"
      title="批量排班"
      width="560px"
      destroy-on-close
    >
      <el-form ref="batchFormRef" :model="batchForm" :rules="batchFormRules" label-width="100px">
        <el-form-item label="技师" prop="technicianIds">
          <el-select v-model="batchForm.technicianIds" multiple placeholder="请选择技师" style="width: 100%">
            <el-option
              v-for="t in technicians"
              :key="t.id"
              :label="t.name"
              :value="t.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排班类型" prop="scheduleType">
          <el-select v-model="batchForm.scheduleType" placeholder="请选择" style="width: 100%">
            <el-option label="上班" :value="1" />
            <el-option label="休息" :value="2" />
            <el-option label="请假" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="班次" v-if="batchForm.scheduleType === 1">
          <el-select v-model="batchForm.shiftType" placeholder="请选择班次" style="width: 100%">
            <el-option label="早班 (09:00-17:00)" value="morning" />
            <el-option label="中班 (13:00-21:00)" value="afternoon" />
            <el-option label="晚班 (17:00-01:00)" value="evening" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围" prop="dateRange">
          <el-date-picker
            v-model="batchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="batchLoading" @click="handleBatchSubmit">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { getScheduleByStore, batchCreateSchedule, updateSchedule, createSchedule, deleteSchedule } from '@/api/store/schedule'
import { getStoreList } from '@/api/store/info'
import { getTechnicianList } from '@/api/store/technician'
import type { StoreInfo } from '@/types/store'

interface TechnicianInfo {
  id: number
  name: string
}

interface DayInfo {
  dateStr: string
  day: number
  weekday: string
  isToday: boolean
}

interface ScheduleData {
  id: number
  scheduleType: number
  shiftType: string
}

interface ShiftOption {
  label: string
  scheduleType: number
  shiftType: string
  icon: string
  time: string
  className: string
}

const weekdayLabels = ['日', '一', '二', '三', '四', '五', '六']

const shiftOptions: ShiftOption[] = [
  { label: '早班', scheduleType: 1, shiftType: 'morning', icon: '🌅', time: '09:00 - 17:00', className: 'opt-morning' },
  { label: '中班', scheduleType: 1, shiftType: 'afternoon', icon: '☀️', time: '13:00 - 21:00', className: 'opt-afternoon' },
  { label: '晚班', scheduleType: 1, shiftType: 'evening', icon: '🌙', time: '17:00 - 01:00', className: 'opt-evening' },
  { label: '休息', scheduleType: 2, shiftType: '', icon: '💤', time: '全天休息', className: 'opt-rest' },
  { label: '请假', scheduleType: 3, shiftType: '', icon: '📝', time: '已请假', className: 'opt-leave' },
]

const storeOptions = ref<StoreInfo[]>([])
const technicians = ref<TechnicianInfo[]>([])
const scheduleMap = ref<Record<string, Record<string, ScheduleData>>>({})

const searchForm = reactive({
  storeId: undefined as number | undefined,
})

const weekStart = ref(getMonday(new Date()))

const shiftDialogVisible = ref(false)
const batchDialogVisible = ref(false)
const batchLoading = ref(false)
const batchFormRef = ref<FormInstance>()
const saving = ref(false)

const currentCell = reactive({
  technicianId: 0,
  technicianName: '',
  dateStr: '',
  dateLabel: '',
  scheduleId: 0,
  scheduleType: 0,
  shiftType: '',
})

const batchForm = reactive({
  technicianIds: [] as number[],
  scheduleType: 1,
  shiftType: 'morning',
  dateRange: null as [string, string] | null,
})

const batchFormRules: FormRules = {
  technicianIds: [{ required: true, message: '请选择技师', trigger: 'change' }],
  scheduleType: [{ required: true, message: '请选择排班类型', trigger: 'change' }],
  dateRange: [{ required: true, message: '请选择日期范围', trigger: 'change' }],
}

function getMonday(d: Date): Date {
  const date = new Date(d)
  const day = date.getDay()
  const diff = date.getDate() - day + (day === 0 ? -6 : 1)
  date.setDate(diff)
  date.setHours(0, 0, 0, 0)
  return date
}

const weekDays = computed<DayInfo[]>(() => {
  const days: DayInfo[] = []
  const today = new Date()
  const todayStr = formatDate(today)
  for (let i = 0; i < 7; i++) {
    const d = new Date(weekStart.value)
    d.setDate(d.getDate() + i)
    days.push({
      dateStr: formatDate(d),
      day: d.getDate(),
      weekday: weekdayLabels[d.getDay()],
      isToday: formatDate(d) === todayStr,
    })
  }
  return days
})

const weekLabel = computed(() => {
  const start = weekDays.value[0]
  const end = weekDays.value[6]
  if (!start || !end) return ''
  return `${start.dateStr} ~ ${end.dateStr}`
})

function formatDate(d: Date): string {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${dd}`
}

function getSchedule(techId: number, dateStr: string): ScheduleData | null {
  return scheduleMap.value[techId]?.[dateStr] || null
}

function getScheduleLabel(techId: number, dateStr: string): string {
  const s = getSchedule(techId, dateStr)
  if (!s) return ''
  if (s.scheduleType === 2) return '休息'
  if (s.scheduleType === 3) return '请假'
  if (s.shiftType === 'morning') return '早班'
  if (s.shiftType === 'afternoon') return '中班'
  if (s.shiftType === 'evening') return '晚班'
  return '上班'
}

function getScheduleTagType(techId: number, dateStr: string): string {
  const s = getSchedule(techId, dateStr)
  if (!s) return 'info'
  if (s.scheduleType === 2) return 'info'
  if (s.scheduleType === 3) return 'warning'
  if (s.shiftType === 'morning') return 'success'
  if (s.shiftType === 'afternoon') return ''
  if (s.shiftType === 'evening') return 'danger'
  return 'success'
}

function isSelected(opt: ShiftOption): boolean {
  if (currentCell.scheduleType !== opt.scheduleType) return false
  if (opt.scheduleType === 1 && currentCell.shiftType !== opt.shiftType) return false
  return true
}

async function fetchStoreOptions() {
  try {
    const res: any = await getStoreList({ page: 1, pageSize: 999 })
    storeOptions.value = res.data.list || []
    if (storeOptions.value.length > 0 && !searchForm.storeId) {
      searchForm.storeId = storeOptions.value[0].id
    }
  } catch {
    storeOptions.value = []
  }
}

async function fetchTechnicians() {
  if (!searchForm.storeId) return
  try {
    const res: any = await getTechnicianList({ page: 1, pageSize: 999, storeId: searchForm.storeId })
    technicians.value = (res.data.list || []).map((t: any) => ({ id: t.id, name: t.name }))
  } catch {
    technicians.value = []
  }
}

async function fetchScheduleData() {
  if (!searchForm.storeId || technicians.value.length === 0) {
    scheduleMap.value = {}
    return
  }
  const date = weekDays.value[0]?.dateStr || formatDate(new Date())
  try {
    const res: any = await getScheduleByStore(searchForm.storeId, date)
    const list: any[] = res.data || []
    const map: Record<string, Record<string, ScheduleData>> = {}
    list.forEach((item) => {
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

function handleStoreChange() {
  fetchTechnicians().then(() => fetchScheduleData())
}

function handlePrevWeek() {
  const d = new Date(weekStart.value)
  d.setDate(d.getDate() - 7)
  weekStart.value = d
  fetchScheduleData()
}

function handleNextWeek() {
  const d = new Date(weekStart.value)
  d.setDate(d.getDate() + 7)
  weekStart.value = d
  fetchScheduleData()
}

function handleCellClick(tech: TechnicianInfo, day: DayInfo) {
  currentCell.technicianId = tech.id
  currentCell.technicianName = tech.name
  currentCell.dateStr = day.dateStr
  currentCell.dateLabel = `${day.dateStr} 周${day.weekday}`
  const s = getSchedule(tech.id, day.dateStr)
  if (s) {
    currentCell.scheduleId = s.id
    currentCell.scheduleType = s.scheduleType
    currentCell.shiftType = s.shiftType
  } else {
    currentCell.scheduleId = 0
    currentCell.scheduleType = 0
    currentCell.shiftType = ''
  }
  shiftDialogVisible.value = true
}

async function handleSelectShift(opt: ShiftOption) {
  saving.value = true
  try {
    if (currentCell.scheduleId) {
      // 更新已有排班
      await updateSchedule(currentCell.scheduleId, {
        scheduleType: opt.scheduleType,
        shiftType: opt.scheduleType === 1 ? opt.shiftType : '',
      })
    } else {
      // 创建新排班
      const shiftTimeMap: Record<string, { start: string; end: string }> = {
        morning: { start: '09:00', end: '17:00' },
        afternoon: { start: '13:00', end: '21:00' },
        evening: { start: '17:00', end: '01:00' },
      }
      const time = opt.scheduleType === 1 ? shiftTimeMap[opt.shiftType] : { start: '00:00', end: '00:00' }
      await createSchedule({
        technicianId: currentCell.technicianId,
        scheduleDate: currentCell.dateStr,
        startTime: time.start,
        endTime: time.end,
        scheduleType: opt.scheduleType,
        shiftType: opt.scheduleType === 1 ? opt.shiftType : '',
      })
    }
    ElMessage.success('排班更新成功')
    shiftDialogVisible.value = false
    fetchScheduleData()
  } catch {
    ElMessage.error('排班更新失败')
  } finally {
    saving.value = false
  }
}

async function handleDeleteSchedule() {
  if (!currentCell.scheduleId) return
  saving.value = true
  try {
    await deleteSchedule(currentCell.scheduleId)
    ElMessage.success('排班已删除')
    shiftDialogVisible.value = false
    fetchScheduleData()
  } catch {
    ElMessage.error('删除失败')
  } finally {
    saving.value = false
  }
}

function handleBatchCreate() {
  if (!searchForm.storeId) {
    ElMessage.warning('请先选择门店')
    return
  }
  batchForm.technicianIds = []
  batchForm.scheduleType = 1
  batchForm.shiftType = 'morning'
  batchForm.dateRange = null
  batchDialogVisible.value = true
}

async function handleBatchSubmit() {
  if (!batchFormRef.value) return
  await batchFormRef.value.validate()
  if (!batchForm.dateRange || batchForm.dateRange.length !== 2) return

  batchLoading.value = true
  try {
    const shiftTimeMap: Record<string, { start: string; end: string }> = {
      morning: { start: '09:00', end: '17:00' },
      afternoon: { start: '13:00', end: '21:00' },
      evening: { start: '17:00', end: '01:00' },
    }
    const time = batchForm.scheduleType === 1 ? shiftTimeMap[batchForm.shiftType] : { start: '09:00', end: '18:00' }
    await batchCreateSchedule({
      storeId: searchForm.storeId,
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
    fetchScheduleData()
  } catch {
    ElMessage.error('批量排班失败')
  } finally {
    batchLoading.value = false
  }
}

onMounted(async () => {
  await fetchStoreOptions()
  if (searchForm.storeId) {
    await fetchTechnicians()
    fetchScheduleData()
  }
})
</script>

<style scoped lang="scss">
.schedule-list {
  padding: 20px;
}

.search-card {
  margin-bottom: 16px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

.week-label {
  font-size: 14px;
  font-weight: 600;
  margin: 0 8px;
  min-width: 200px;
  text-align: center;
}

.schedule-table-wrapper {
  overflow-x: auto;
}

.schedule-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;

  th, td {
    border: 1px solid #ebeef5;
    text-align: center;
    vertical-align: middle;
  }

  th {
    background: #f5f7fa;
    font-weight: 600;
    padding: 8px 4px;
  }

  td {
    padding: 6px 4px;
    cursor: pointer;
    transition: background 0.2s;

    &:hover {
      background: #f0f9eb;
    }
  }
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

  &.is-today {
    background: #f0f9eb;
  }
}

.day-header {
  .day-weekday {
    font-size: 12px;
    color: #909399;
  }
  .day-date {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }
}

.is-today .day-date {
  color: #409eff;
}

.tech-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
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

  &:hover {
    border-color: #409eff;
    background: #f0f9eb;
  }

  &.is-active {
    border-color: #409eff;
    background: #ecf5ff;
  }

  &.opt-morning.is-active {
    border-color: #67c23a;
    background: #f0f9eb;
  }

  &.opt-afternoon.is-active {
    border-color: #409eff;
    background: #ecf5ff;
  }

  &.opt-evening.is-active {
    border-color: #e6a23c;
    background: #fdf6ec;
  }

  &.opt-rest.is-active {
    border-color: #909399;
    background: #f4f4f5;
  }

  &.opt-leave.is-active {
    border-color: #f56c6c;
    background: #fef0f0;
  }
}

.shift-icon {
  font-size: 24px;
  width: 36px;
  text-align: center;
}

.shift-info {
  flex: 1;
}

.shift-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.shift-time {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}
</style>
